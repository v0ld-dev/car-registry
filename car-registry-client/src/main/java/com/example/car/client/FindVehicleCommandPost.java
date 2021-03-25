package com.example.car.client;

import com.example.car.messages.VehicleOuterClass;
import com.exonum.binding.common.crypto.*;
import com.exonum.client.ExonumClient;
import com.google.common.net.UrlEscapers;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.file.Files.readString;

@CommandLine.Command(name = "find-vehicle-post",
        aliases = {"fvp"},
        description = {"Finds a vehicle in the registry by its ID and signer like proof"})
public class FindVehicleCommandPost implements Callable<Integer> {

    private static final CryptoFunction DEFAULT_CRYPTO_FUNCTION = CryptoFunctions.ed25519();

    private KeyPair keys;
    private CryptoFunction cryptoFunction;

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    ServiceIds serviceIds;
    @CommandLine.Parameters(index = "0", description = "Vehicle ID in the registry")
    String vehicleId;

    @Override
    public Integer call() throws Exception {

        var keyPair = readKeyPair();
        this.keys = checkNotNull(keyPair);
        this.cryptoFunction = checkNotNull(DEFAULT_CRYPTO_FUNCTION);

        //TODO: should be hash  ?
        byte[] exonumMessage =  new String("{\"id\": " + vehicleId + ", \"seed\": 1}").getBytes();
        byte[] signature = cryptoFunction.signMessage(exonumMessage, keys.getPrivateKey());

        String plainJson = "{\"data\": {\"id\": " + vehicleId + ", \"seed\": 1}, \"sign\": "+ new String(Base64.getEncoder().encode(signature)) +"}";

        var serviceName = findServiceName();
        var httpClient = HttpClient.newHttpClient();
        var findVehicleRequest = HttpRequest.newBuilder()
                .uri(buildRequestUri(serviceName))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(plainJson))
                .build();
        System.out.println(plainJson);
        var response = httpClient.send(findVehicleRequest, HttpResponse.BodyHandlers.ofByteArray());
        var statusCode = response.statusCode();
        if (statusCode == HTTP_OK) {
            var vehicle = VehicleOuterClass.Vehicle.parseFrom(response.body());
            System.out.println("Vehicle: " + vehicle);
            return 0;
        } else if (statusCode == HTTP_NOT_FOUND) {
            System.out.printf("Vehicle with id (%s) is not found.%n", vehicleId);
            return statusCode;
        } else {
            System.out.println("Status code: " + statusCode);
            return statusCode;
        }
    }


    private String findServiceName() {
        if (serviceIds.hasName()) {
            // The user graciously provided the name
            return serviceIds.name;
        }
        // The name is unset, look it up by ID using the node public API.
        var exonumClient = ExonumClient.newBuilder()
                .setExonumHost(Config.NODE_PUBLIC_API_HOST)
                .build();
        var serviceInfoFinder = new ServiceIdResolver(serviceIds, exonumClient);
        return serviceInfoFinder.getName();
    }

    private URI buildRequestUri(String serviceName) throws IOException {

        var escaper = UrlEscapers.urlPathSegmentEscaper();
        var uri = String.format("%s/api/services/%s/vehicle/get-by-pk",
                Config.NODE_JAVA_API_HOST, escaper.escape(serviceName), escaper.escape(vehicleId));
        System.out.println("uri: "+ uri);
        return URI.create(uri);
    }

    private static KeyPair readKeyPair() throws IOException {
        var privateKeyStr = readString(Path.of(GenerateKeyCommand.EXONUM_ID_FILENAME));
        var privateKey = PrivateKey.fromHexString(privateKeyStr);

        var pubKeyStr = readString(Path.of(GenerateKeyCommand.EXONUM_ID_PUB_FILENAME));
        var pubKey = PublicKey.fromHexString(pubKeyStr);

        return KeyPair.newInstance(privateKey, pubKey);
    }
}

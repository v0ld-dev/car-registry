/*
 * Copyright 2020 The Exonum Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.car.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.file.Files.readString;

import com.example.car.messages.VehicleOuterClass.Vehicle;
import com.exonum.binding.common.crypto.*;
import com.exonum.client.ExonumClient;
import com.google.common.net.UrlEscapers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "find-vehicle",
    aliases = {"fv"},
    description = {"Finds a vehicle in the registry by its ID"})
public class FindVehicleCommand implements Callable<Integer> {

  private static final CryptoFunction DEFAULT_CRYPTO_FUNCTION = CryptoFunctions.ed25519();

  private KeyPair keys;
  private CryptoFunction cryptoFunction;

  @ArgGroup(exclusive = true, multiplicity = "1")
  ServiceIds serviceIds;

  @Parameters(index = "0", description = "Vehicle ID in the registry")
  String vehicleId;

  @Override
  public Integer call() throws Exception {
    var serviceName = findServiceName();
    var httpClient = HttpClient.newHttpClient();
    var findVehicleRequest = HttpRequest.newBuilder()
        .uri(buildRequestUri(serviceName))
        .GET()
        .build();
    var response = httpClient.send(findVehicleRequest, BodyHandlers.ofByteArray());
    var statusCode = response.statusCode();
    if (statusCode == HTTP_OK) {
      var vehicle = Vehicle.parseFrom(response.body());
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

                              var keyPair = readKeyPair();
                              this.keys = checkNotNull(keyPair);
                              this.cryptoFunction = checkNotNull(DEFAULT_CRYPTO_FUNCTION);

                              //TODO should be some seed/nonce
                              byte[] exonumMessage =  new byte[20];
                              ThreadLocalRandom.current().nextBytes(exonumMessage);

                              byte[] signature = cryptoFunction.signMessage(exonumMessage, keys.getPrivateKey());
                              System.out.println("signature: " + signature);

                              //TEMPORARY check sign for proof of concept
                              var res = cryptoFunction.verify(exonumMessage, signature, keyPair.getPublicKey());
                              System.out.println("result verify: " + res);


    // TODO should pass signature and exonumMessage to uri. Will be good if it's will be post request

    var escaper = UrlEscapers.urlPathSegmentEscaper();
    var uri = String.format("%s/api/services/%s/vehicle/example1/%s",
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

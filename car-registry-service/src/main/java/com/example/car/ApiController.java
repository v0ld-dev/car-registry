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

package com.example.car;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.exonum.binding.common.serialization.json.JsonSerializer.json;
import static com.google.common.base.Preconditions.checkArgument;


import com.example.car.messages.VehicleOuterClass;
import com.exonum.binding.common.crypto.*;
import com.exonum.binding.core.service.Node;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;
import java.util.function.Function;


final class ApiController {

  private static final CryptoFunction DEFAULT_CRYPTO_FUNCTION = CryptoFunctions.ed25519();

  private final MyService service;
  private final Node node;
  private CryptoFunction cryptoFunction;

  ApiController(MyService service, Node node) {
    this.service = service;
    this.node = node;
  }

  void mount(Router router) {
    router.get("/vehicle/example1/:id").handler(this::findVehicle);
    router.get("/vehicle/example2/:id").handler(this::findVehicleSEC);
    router.post("/vehicle/get-by-pk").handler(this::findVehiclePOST);

  }

  private void findVehiclePOST(RoutingContext routingContext) {

    this.cryptoFunction = checkNotNull(DEFAULT_CRYPTO_FUNCTION);
    /*
    {
     "data": {"id": vehicleId, "seed": 1,  }
     "sign": "AZASsadSA2DWS..."
     }
     */

    JsonObject queryPost = routingContext.getBodyAsJson();

    var vehicleOpt = node.withBlockchainData((blockchainData) -> service.findVehicle(queryPost.getJsonObject("data").getString("id"), blockchainData));
    var pubKey = vehicleOpt.get().getPubKey();
    var res = cryptoFunction.verify(queryPost.getBinary("data"), queryPost.getBinary("sign"),  PublicKey.fromBytes(pubKey.getData().toByteArray()));

    if (vehicleOpt.isPresent() && res) {
      var vehicle = vehicleOpt.get();
      routingContext.response()
              .putHeader("Content-Type", "application/octet-stream")
              .end(Buffer.buffer(vehicle.toByteArray()));
    } else {
      // Respond that the vehicle with such ID is not found
      routingContext.response()
              .setStatusCode(HTTP_NOT_FOUND)
              .end();
    }
  }

  /**
   * Find by id, where id is public key
   * @param routingContext
   */
  private void findVehicleSEC(RoutingContext routingContext) {
    var _vehicleId = routingContext.pathParam("id");
    PublicKey valletId =  getRequiredParameter(routingContext.request(), _vehicleId, PublicKey::fromHexString);

    Optional<VehicleOuterClass.Vehicle> vehicle = service.findVehicle(valletId, node);

    if (vehicle.isPresent()) {
      routingContext.response()
              .putHeader(CONTENT_TYPE, "application/json")
              .end(json().toJson(vehicle.get()));
    } else {
      routingContext.response()
              .setStatusCode(HTTP_NOT_FOUND)
              .end();
    }


  }


  private void findVehicle(RoutingContext routingContext) {
    // Extract the requested vehicle ID
    var vehicleId = routingContext.pathParam("id");
    // Find it in the registry. The Node#withBlockchainData provides
    // the required context with the current, immutable database state.
    var vehicleOpt = node.withBlockchainData(
        (blockchainData) -> service.findVehicle(vehicleId, blockchainData));
    if (vehicleOpt.isPresent()) {
      var vehicle = vehicleOpt.get();
      routingContext.response()
          .putHeader("Content-Type", "application/octet-stream")
          .end(Buffer.buffer(vehicle.toByteArray()));
    } else {
      // Respond that the vehicle with such ID is not found
      routingContext.response()
          .setStatusCode(HTTP_NOT_FOUND)
          .end();
    }
  }

  private static <T> T getRequiredParameter(HttpServerRequest request, String key,  Function<String, T> converter) {
    return getRequiredParameter(request.params(), key, converter);
  }

  private static <T> T getRequiredParameter(MultiMap parameters, String key,  Function<String, T> converter) {
    checkArgument(parameters.contains(key), "No required key (%s) in request parameters: %s",
            key, parameters);
    String parameter = parameters.get(key);
    try {
      return converter.apply(parameter);
    } catch (Exception e) {
      String message = String.format("Failed to convert parameter (%s): %s", key, e.getMessage());
      throw new IllegalArgumentException(message);
    }
  }

}

package com.example.car.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import static java.net.HttpURLConnection.HTTP_OK;

/*
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class MockServerTest extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        Router router = Router.router(vertx);
        router.route(HttpMethod.POST,"/vehicle/get-by-pk")
                .handler(CorsHandler.create("*").allowCredentials(false))
                .handler(BodyHandler.create())
                .handler(ctx -> {

            JsonObject queryPost = ctx.getBodyAsJson();
            System.out.println("queryPost "+ queryPost);
            ctx.response()
                    .setStatusCode(HTTP_OK)
                    .end();
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8989);
    }


    public static void main(String[] args) throws Exception {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MockServerTest());
    }
}

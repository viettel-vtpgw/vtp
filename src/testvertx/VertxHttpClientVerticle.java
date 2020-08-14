/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testvertx;

/**
 *
 * @author anPV
 */

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;

public class VertxHttpClientVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        System.out.println("VertxHttpClientVerticle started!");
        HttpClient httpClient = vertx.createHttpClient();
        
        httpClient.getNow(9696, "10.60.155.108", "/vtpws/ws?wsdl", new Handler<HttpClientResponse>() {
            @Override
            public void handle(HttpClientResponse httpClientResponse) {
                System.out.println("Response received");
                
                httpClientResponse.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {
                        System.out.println("Response (" + buffer.length() + "): ");
                        System.out.println(buffer.getString(0, buffer.length()));
                    }
                });
            }
        });
    }
}

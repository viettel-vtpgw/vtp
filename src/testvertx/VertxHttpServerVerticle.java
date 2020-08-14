/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class VertxHttpServerVerticle extends AbstractVerticle {

    private HttpServer httpServer = null;

    @Override
    public void start() throws Exception {
        System.out.println("VertxHttpServerVerticle started!");
        
        httpServer = vertx.createHttpServer();
        httpServer.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                System.out.println("incoming request!");
                
                Buffer fullRequestBody = Buffer.buffer();
                if(request.method() == HttpMethod.POST){
                    request.handler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {
                            fullRequestBody.appendBuffer(buffer);                             
                        }
                    });
                    
                    request.endHandler(new Handler<Void>() {                        
                        @Override
                        public void handle(Void e) {
                            // here you can access the 
                            // fullRequestBody Buffer instance.
                            System.out.println("request: " + fullRequestBody.toString());
                            HttpServerResponse response = request.response();
                            
                            if(request.method() == HttpMethod.POST){                            
                                HttpClient httpClient = vertx.createHttpClient();  
                                String uri = request.uri();
                                httpClient.getNow(8888, "10.60.102.181", uri, new Handler<HttpClientResponse>() {
                                    @Override
                                    public void handle(HttpClientResponse httpClientResponse) {
                                        System.out.println("Response received");

                                        httpClientResponse.bodyHandler(new Handler<Buffer>() {
                                            @Override
                                            public void handle(Buffer buffer) {                                                
                                                System.out.println("Response (" + buffer.length() + "): ");
                                                System.out.println(buffer.getString(0, buffer.length()));
                                                
                                                // thuc hien hoan doi cac link trong wsdl
                                                String str = buffer.getString(0, buffer.length());
                                                
                                                    
                                                response.setStatusCode(200);
                                                response.headers()
                                                    .add("Content-Length", "" + str.length())
                                                    .add("Content-Type", "text/html")
                                                ;
                                                response.write(str);
                                                response.end();
                                            }
                                        });
                                    }
                                });
                            }
                            
                            
                            
                        }
                    });                    
                }
            }
        });

        httpServer.listen(9999);
    }
    
    private void handleHttpRequest(HttpServerRequest request) {
        MultiMap headers = request.headers();
        String remoteIP;                                                
        remoteIP = request.remoteAddress().host();
        String contentType = headers.get(HttpHeaders.CONTENT_TYPE);

        
    }
}
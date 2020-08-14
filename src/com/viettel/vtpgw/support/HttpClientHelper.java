package com.viettel.vtpgw.support;

import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientRequest;

public class HttpClientHelper {
  private final static Logger LOG = LogManager.getLogger(HttpClientHelper.class);
  private HttpClientHelper(){}
  
  //TODO https://github.com/eclipse/vert.x/issues/1218
  public static HttpClientRequest setTimeout(HttpClientRequest req, long timeout, Handler<Throwable> handler) {
    if (timeout > 0) {
      req.exceptionHandler(th -> {
        req.exceptionHandler(null);
        handler.handle(th);
        if (th instanceof TimeoutException) {          
          try{
          	req.connection().close();
          }catch(Exception e){
            LOG .debug(e);
          }
        }
      });
      req.setTimeout(timeout);
    } else {
      req.exceptionHandler(handler);
    }
    return req;
  }
}

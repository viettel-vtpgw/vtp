/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.util;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author anPV
 */
public class LogUtil {
    public static void print(Logger logger, String uri, String method, long start, long duration, int code, int success, int hit, String request, String response){
        JsonObject json = new JsonObject();
        json.put("uri", uri);
        json.put("method", method);
        json.put("start", start);
        json.put("duration", duration);
        json.put("code", code);
        json.put("success", success);
        json.put("hit", hit);
        json.put("request", request);
        json.put("response", response);
        
        logger.info(json.toString());        
    }
    
    public static void transaction(Logger logger, String uri, String method, String service, long start, long duration, int code, int success, String TransactionCode, String request, String response){
        JsonObject json = new JsonObject();
        json.put("uri", uri);
        json.put("method", method);
        json.put("service", service);
        json.put("start", start);
        json.put("duration", duration);
        json.put("code", code);
        json.put("success", success);
        json.put("TransactionCode", TransactionCode);
        json.put("request", request);
        json.put("response", response);
        
        logger.info(json.toString());        
    }
}

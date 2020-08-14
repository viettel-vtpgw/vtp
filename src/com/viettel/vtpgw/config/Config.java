/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.config;

import com.viettel.vtpgw.GatewayProcess;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author anPV
 */
public class Config {
    private static Logger logger = LogManager.getLogger(Config.class);
    
    public static String getJedisCluster(JsonObject config) {
        JsonObject json = config;
        String jedisConnection = null;
        
        try{
            json = json.getJsonObject("jedis-cluster");
            JsonArray arr = json.getJsonArray("host");
            jedisConnection = "";
            for (int i = 0; i < arr.size(); i++) {
                jedisConnection += arr.getString(i);
                if (i != (arr.size() - 1)) {
                    jedisConnection += ",";
                }
            }
        }
        catch(Exception ex){
            logger.error("getJedisCluster(): ", ex);            
        }
        
        return jedisConnection;
    }

    public static int getHttpServerPort(JsonObject config) {
        JsonObject json = config;
        int port = 8686;
        
        try{
            json = json.getJsonObject("gateway");
            port = json.getInteger("port", 8686);
        }
        catch(Exception ex){
            logger.error("getHttpServerPort(): ", ex);
        }
        
        return port;
    }
    
    public static String getLbAddress(JsonObject config) {
        JsonObject json = config;
        String s = null;
        
        try{
            json = json.getJsonObject("proxy");
            s = json.getString("lb-address");
        }
        catch(Exception ex){
            logger.error("getLbAddress(): ", ex);
        }
        
        return s;
    }
    
     public static String getVtpgwAddress(JsonObject config) {
        JsonObject json = config;
        String s = null;
        
        try{
            json = json.getJsonObject("proxy");
            s = json.getString("vtpgateway");
        }
        catch(Exception ex){
            logger.error("getVtpgwAddress(): ", ex);
        }
        
        return s;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.util;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.vertx.core.json.JsonObject;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import org.apache.commons.lang.StringEscapeUtils; 

/**
 *
 * @author anPV
 */
public class JedisUtil {
    private static Logger logger = LogManager.getLogger(JedisUtil.class);
    /**
     * Ham: thuc hien lay doi tuong redis cluster tu chuoi ket noi
     * conCluster: chuoi ket noi dang "host1:port1,host2:port2,.."
     * return: khac null trong truong hop thanh cong. that bai: null
     */
    public static JedisCluster getJedisCluster(String conCluster){
        JedisCluster jc = null;
        try{
            String[] serverArray = conCluster.split(",");
            Set<HostAndPort> nodes = new HashSet<>();

            for (String ipPort : serverArray) {
                String[] ipPortPair = ipPort.split(":");
                nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
            }        
            jc = new JedisCluster(nodes);
        }
        catch(Exception ex){
            logger.error("getJedisCluster(): ", ex);            
        }
        
        return jc;
    }
    
    /**
     * Ham: thuc hien thiet dat gia tri cho khoa va gia tri voi viec chuyen doi khoa ve dang base64
     * key: khoa can thiet dat o dang ro - raw
     * value: gia tri khoa can thiet dat
     * expireTime: thoi han luu khoa tinh bang  giay
     * return: true neu thanh cong, false neu khong thiet dat duoc gia tri
     */
    public static boolean setKeyBase64(JedisCluster jc, String key, String value, String objectId, int expireTime){        
        try{            
            String s = key;
            jc.set(s, value);
            Long l = jc.expire(s, expireTime);
            if(l < 0){
                l = jc.del(s);
            }
            
            // giu object id de xoa du lieu sau nay
            if(objectId != null){
                String str = objectId + "@" + key;
                Date dt = new Date();
                jc.set(str, "" + dt.getTime());
                l = jc.expire(str, expireTime);
                if(l < 0){
                    l = jc.del(s);
                    l = jc.del(str);
                }
            }
        }
        catch(Exception ex){
            logger.error("JedisUtil - setKeyBase64(): ", ex);
            return false;
        }
        
        return true;
    }
   
     /**
     * Ham: thuc hien lay gia tri cho khoa, biet rang khoa da duoc chuyen doi khoa ve dang base64
     * key: khoa can thiet dat o dang ro - raw     
     * return: gia tri da thiet dat truoc do. null neu khong co hoac that bai.
     */
    public static String getKeyBase64(JedisCluster jc, String key, String objectId){
        try{
            String s = key;
            String value = null;
            //s = Base64.encode(key.getBytes());
//            JsonObject obj = new JsonObject(); 
//            if(objectId == null){
//                obj.put("objectId", "*");
//                obj.put("key", key);
//                s = obj.toString();
//                
//                Set<String> list = JedisUtil.keys(jc, s);
//                if(list != null && list.size() > 0){
//                    Object l[] = list.toArray();
//                    s = l[0].toString();
//                    //s = StringEscapeUtils.escapeHtml(s);
//                    value = jc.get(s);
//                }
//            }
//            else{
//                obj.put("objectId", objectId);
//                obj.put("key", key);
//                s = obj.toString();
//                //s = StringEscapeUtils.escapeHtml(s);
//                value = jc.get(s);
//            }
            
            value = jc.get(s);
            return value;
        }
        catch(Exception ex){
            logger.error("getKeyBase64(): ", ex);
            return null;
        }
    }
    
    public static Set<String> keys(JedisCluster jc, String pattern){
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, JedisPool> entry : jc.getClusterNodes().entrySet()) {
            Jedis jedis = entry.getValue().getResource();
            Set<String> l =  jedis.keys(pattern);
            keys.addAll(l);
        }
        
        return keys;
    }
    
    public static void test(){
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        //Jedis Cluster will attempt to discover cluster nodes automatically
        jedisClusterNodes.add(new HostAndPort("10.60.155.107", 7000));
        jedisClusterNodes.add(new HostAndPort("10.60.155.107", 7001));
        jedisClusterNodes.add(new HostAndPort("10.60.155.107", 7002));
        jedisClusterNodes.add(new HostAndPort("10.60.155.108", 7003));
        jedisClusterNodes.add(new HostAndPort("10.60.155.108", 7004));
        jedisClusterNodes.add(new HostAndPort("10.60.155.108", 7005));
        
        JedisCluster jc = new JedisCluster(jedisClusterNodes);
        //JedisCluster jc = new JedisCluster(jedisClusterNode, redis.clients.jedis.Protocol.DEFAULT_TIMEOUT, redis.clients.jedis.Protocol.DEFAULT_TIMEOUT, redis.clients.jedis.Protocol.DEFAULT_REDIRECTIONS, "cluster", redis.clients.jedis.Protocol.DEFAULT_CONFIG);
        
        String key = "foo";
        String value;
        
        jc.set(key, "bar");
        value = jc.get(key);
        System.out.println(key + ": " + value);
        
        key = "test";
        jc.set(key, "1");
        value = jc.get(key);
        System.out.println(key + ": " + value);
        
        key = "hello";
        jc.set(key, "world");
        value = jc.get(key);
        System.out.println(key + ": " + value);        
    }
}

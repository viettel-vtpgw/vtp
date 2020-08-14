/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author anPV
 */
public class JsonUtil {
    static final Logger logger = LogManager.getLogger(JsonUtil.class.getName());
    /**
    *
    * @author anPV
    */
    public static JsonElement XML2Json(String xml, boolean keepNameSpace){
        JsonElement obj;
        try{
            JSONObject soap = XML.toJSONObject(xml);
            obj = convertJSON2Json(soap, false);
        }
        catch(Exception ex){
            logger.error("XML2Json(): ", ex);
            return null;
        }
        return obj;
    }
    
    public static JsonElement convertJSON2Json(Object parent, boolean keepNameSpace){
        JsonElement ret;
        JsonElement retChild;
        JsonObject retObject;
        JsonArray retArray;
        
        JSONObject obj;
        JSONArray ar;
        
        Iterator<String> it;
        String key;
        String newKey;
        Object child;
        int i;
        
        if(parent instanceof JSONObject){
            obj = (JSONObject) parent;
            retObject = new JsonObject();
            
            it = obj.keys();
            while(it.hasNext()) {
                key = (String)it.next();
                child = obj.get(key);

                newKey = key;
                if(!keepNameSpace){
                    i = key.indexOf(":");
                    if(i == -1){
                        newKey = key;
                    }
                    else{
                        newKey = key.substring(i + 1);
                    }
                }
                          
                if(child instanceof JSONObject){
                    retChild = convertJSON2Json((JSONObject)child, keepNameSpace);
                    retObject.add(newKey, retChild);
                }
                else if(child instanceof JSONArray){
                    retChild = convertJSON2Json((JSONArray)child, keepNameSpace);
                    retObject.add(newKey, retChild);
                }
                else{
                    retObject.add(newKey, new JsonPrimitive(child.toString()));
                }                
            }
            ret = retObject;
        }        
        else if (parent instanceof JSONArray){                        
            ar = (JSONArray)parent;           
            retArray = new JsonArray();
            for(i = 0; i < ar.length(); i++){
                child = ar.get(i);
                if ((child instanceof JSONObject) || (child instanceof JSONArray)){
                    retChild = convertJSON2Json(child, keepNameSpace);
                    retArray.add(retChild);
                }
                else{
                    retArray.add(new JsonPrimitive(child.toString()));
                }
            }
            ret = retArray;
        }
        else{
            ret = new JsonPrimitive(parent.toString()); 
        }
        
        return ret;
    }
    
    /**
     * Lay doi tuong JSON bang duong dan ten - ten moi node luu trong doi tuong danh sach
     */
    public static JsonElement getElementByPath(JsonObject root, List<String> list, JsonElement[] parent){
        JsonElement ret = root;
        int i = 0;
        String s;
        try{
            do{
                if(i == list.size()){
                    return ret;
                }
                
                s = list.get(i++);
                if(parent != null){
                    parent[0] = ret;
                }                
                ret = ((JsonObject)ret).get(s);                
                
                if(ret == null || ret.isJsonNull()){
                    return null;
                }
                else if(ret.isJsonPrimitive()){
                    if(i < (list.size() - 1)){
                        return null;
                    }
                    else{
                        s = list.get(i);
                        String str = ret.getAsString();
                        if(s.equals(str)){
                            return ret;
                        }
                        else{
                            return null;
                        }
                    }
                }
                else if (ret.isJsonArray()){
                    // tao lap duong dan con
                    List<String> l = new ArrayList<>();
                    int j;
                    for(j = i; j < list.size(); j++){
                        l.add(list.get(j));
                    }
                    
                    // tim kiem bat ky doi tuong con nao thao man
                    JsonArray arr = (JsonArray)ret;
                    boolean bFound = false;                    
                    for(j = 0; j < arr.size(); j++){
                        ret = arr.get(j);                        
                        ret = getElementByPath((JsonObject)ret, l, parent);
                        if(ret != null){
                            bFound = true;
                            break;  
                        }
                    }
                    
                    if(!bFound){
                        ret = null;
                    }
                    return ret;
                }
            }        
            while(true);
        }
        catch(Exception ex){
            return null;
        }
    }
    
    public static JsonElement getElementByPath(JsonObject root, String path, JsonElement[] parent){
        List<String> list = new ArrayList<>();
        String[] token = path.split("\\@");
        if(token == null){
            return null;
        }
        
        for(String s: token){
            list.add(s);
        }        
           
        JsonElement obj = getElementByPath(root, list, parent);
        return obj;
    }
    
    /**
     * Ham thuc hien lay duong dan den doi tuong goc - duong dan phan cach boi '@'
     * Trong do loai duong dan cua doi tuong co gia tri 1, duong dan chi bao gom ten den doi tuong la 0
     * @Param node: doi tuong goc
     * @Param depth do sau tinh tu goc (do sau cua goc la 0 zero)
     * @Param map: danh sach duong dan den doi tuong con luu
     */
    public static void getDir(JsonObject node, int depth, HashMap<String, Integer> map ){        
        if(node == null || depth < 1 || !node.isJsonObject()){
            return;
        }        
        
        for (Map.Entry<String, JsonElement> e : node.entrySet()) {
            String key = e.getKey();
            JsonElement el = e.getValue();
            
            // cho dinh doi tuong dang xet vao danh sach
            map.put(key, 0);
            
            if(el.isJsonPrimitive()){                
                map.put(key + "@" + el.getAsString(), 1);
            }
            else if(el.isJsonObject()){                
                HashMap<String, Integer> map1 = new HashMap<String, Integer>();
                
                // lay duong dan den ten doi tuong con
                getDir((JsonObject)el, depth - 1, map1);
                
                // them ten doi tuong con vao duong dan cua cha
                for (Map.Entry<String, Integer> item : map1.entrySet()) {
                    map.put(key + "@" + item.getKey(), item.getValue());
                }
                
                map1.clear();
            }
        }        
    }
}

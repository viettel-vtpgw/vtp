package com.viettel.vtpgw.util;

import com.google.gson.JsonElement;
import com.viettel.vtpgw.db.entity.Setting;
import com.viettel.vtpgw.db.entity.SettingShareable;
import com.viettel.vtpgw.support.soap.KxmlSoapExtractor;
import io.vertx.core.Vertx;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Utils {
    public static final int CACHE_WSDL_EXPIRE_TIME  = 300000;   // milli second - thoi gian luu tru noi dung file wsdl tren cache
    public static final String CACHE_SHARED_MAP_KEY_SCHEMA = "my-schema-wsdl";
    public static final String CACHE_KEY_TIME = "time";
    public static final String CACHE_KEY_BODY = "body";
    
    public static final String CACHE_SHARED_MAP_KEY_DB_SETTING = "my-db-setting";
    public static final String CACHE_SHARED_MAP_KEY_DB_SETTING_BACKUP = "my-db-setting-backup";
    public static final int CACHE_SHARED_XML_WS_MAX_DEPTH = 3;

    public static final String CACHE_SHARED_MAP_KEY_REQ_RESP_LIST = "my-request-response";
            
    private static final Pattern ENCODING = Pattern.compile("charset=([^;]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern JAVA_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Logger LOG = LogManager.getLogger(Utils.class);
    private static final Map<String, String> SUPPORTED_CHARSETS = new HashMap<>();

    static {
        SUPPORTED_CHARSETS.put("utf-8", "UTF8");
    }

    public static Element findChild(Node n, String namespace, String tagName) {
        Node node = n.getFirstChild();
        while (node != null) {
            if (namespace.equals(node.getNamespaceURI()) && tagName.equals(node.getLocalName())) {
                return (Element) node;
            }
            node = node.getNextSibling();
        }
        return null;
    }

    public static String getContentAsString(Buffer buff, String charset, String encoding) {
        if ("gzip".equals(encoding)) {
            try (GZIPInputStream gzip = new GZIPInputStream(new BufferInputStream(buff))) {
                Buffer out = Buffer.buffer(buff.length() * 3 / 2);
                byte[] buffer = new byte[1024];
                int r;
                while ((r = gzip.read(buffer)) != -1) {
                    out.appendBytes(buffer, 0, r);
                }
                return out.toString(charset);
            } catch (Exception e) {
                LOG.error("Can not deflate content", e);
            }
        }
        if ("deflate".equals(encoding)) {
            try {
                Inflater inflater = new Inflater();
                inflater.setInput(buff.getBytes());
                Buffer out = Buffer.buffer(buff.length() * 3 / 2);
                byte[] buffer = new byte[1024];
                while (!inflater.finished()) {
                    int count = inflater.inflate(buffer);
                    out.appendBytes(buffer, 0, count);
                }
                return out.toString(charset);
            } catch (Exception e) {
                LOG.error("Can not deflate content", e);
            }
        }
        return buff.toString(charset);
    }

    public static String getEncoding(String contentType, String def) {
        if (contentType != null) {
            Matcher m = ENCODING.matcher(contentType);
            if (m.find()) {
                return m.group(1).replaceAll("[\"']", "");
            }
        }
        return def;
    }

    public static String getNodeValue(Node node) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return getNodeValue(node.getFirstChild());
            case Node.TEXT_NODE:
                return node.getNodeValue();
            case Node.CDATA_SECTION_NODE:
                return node.getNodeValue();
            default:
                return null;
        }
    }

    public static String javaName(String id) {
        Matcher m = JAVA_NAME.matcher(id);
        if (m.find()) {
            return m.group();
        }
        throw new IllegalArgumentException("Invalid #" + id);
    }

    /**
     * Ham thuc hien luu tru noi dung file wsdl vao bo nho cuc bo cua chuong trinh
     * voi viec thiet lap thoi gian luu tru co dinh tren cache. 
     * Ham chi cap nhat du lieu khi thoi gian luu tru tren cache het thoi han.
     * @Param vertx: doi tuong vertx dung cho viec luu tru du lieu
     * @Param url: url cua file wsdl dung de lam key tham chieu noi dung file
     * @Param body: noi dung file wsdl can luu tru
     */
    public static void updateSchema(Vertx vertx, String url, String body) {
        Date dt = new Date();
        SharedData sd = vertx.sharedData();
        LocalMap<String, String> map = sd.getLocalMap(CACHE_SHARED_MAP_KEY_SCHEMA);
        JsonObject json; // = new JsonObject();

        String val = map.get(url);
        if (val == null) {
            json = new JsonObject();
            json.put(CACHE_KEY_TIME, dt.getTime());
            json.put(CACHE_KEY_BODY, body);
            map.put(url, json.toString());
        } 
        else {
            json = new JsonObject(val);
            long time = json.getInteger(CACHE_KEY_TIME);
            if((time + CACHE_WSDL_EXPIRE_TIME) > dt.getTime()){
                json.put(CACHE_KEY_TIME, dt.getTime());
                json.put(CACHE_KEY_BODY, body);
                map.put(url, json.toString());
            }
        }
    }
    
    /**
     * Ham thuc hien lay noi dung file wsdl tren cache voi url xac dinh.     
     * @Param vertx: doi tuong vertx dung cho viec luu tru du lieu
     * @Param url: url cua file wsdl dung de lam key tham chieu noi dung file
     * Return: Ham tra ve noi dung wsdl luu tru tren cache
     */
    public static String getSchema(Vertx vertx, String url) {
        Date dt = new Date();
        SharedData sd = vertx.sharedData();
        LocalMap<String, String> map = sd.getLocalMap(CACHE_SHARED_MAP_KEY_SCHEMA);
        JsonObject json; // = new JsonObject();

        String val = map.get(url);
        if (val == null) {
            return null;
        } 
        else {
            json = new JsonObject(val);
            long time = json.getInteger(CACHE_KEY_TIME);
//            val = json.getString(CACHE_KEY_BODY);
//            return val;
            
            if((time + CACHE_WSDL_EXPIRE_TIME) > dt.getTime()){
                map.remove(url);
                return null;
            }
            else{
                val = json.getString(CACHE_KEY_BODY);
                return val;
            }
        }
    }
        
    /**
     * Ham thuc hien lay noi dung file wsdl tren cache voi url xac dinh.     
     * @Param vertx: doi tuong vertx dung cho viec luu tru du lieu
     * @Param url: url cua file wsdl dung de lam key tham chieu noi dung file
     * Return: Ham tra ve noi dung wsdl luu tru tren cache
     */
    public static SettingShareable getSettingExpire(String url, JsonElement root, Vertx vertx) {        
        SharedData sd = vertx.sharedData();
        //ConcurrentLinkedDeque<Setting> listSetting;
        LocalMap<String, SettingShareable> map = sd.getLocalMap(CACHE_SHARED_MAP_KEY_DB_SETTING);
        SettingShareable setting = null;
        
        // @url@operation@param@param_value
        //JsonElement root = JsonUtil.XML2Json(xmlContent, false);   
        JsonElement e = JsonUtil.getElementByPath((com.google.gson.JsonObject)root, "Envelope@Body", null);
        
        HashMap<String, Integer> paths = new HashMap<String, Integer>();
        JsonUtil.getDir((com.google.gson.JsonObject) e, CACHE_SHARED_XML_WS_MAX_DEPTH, paths);
        
        // Kiem tra co thiet dat thoi gian het hieu luc trong DB
        for (Map.Entry<String, Integer> item : paths.entrySet()) {
            setting = map.get(url + "@" + item.getKey());
//            if((setting != null) && (setting.getType() == item.getValue())){
//                break;
//            }
        }
        
        return setting;
    }
    
    public static void save2Cache(String requestBody, String respBody, Vertx vertx){
        SharedData sd = vertx.sharedData();        
        LocalMap<String, String> map = sd.getLocalMap(CACHE_SHARED_MAP_KEY_REQ_RESP_LIST);
        map.put(requestBody, respBody);
    }
    
    public static void send2Process(String node, String message, Vertx vertx){        
        vertx.eventBus().send(node, message);
    }
}

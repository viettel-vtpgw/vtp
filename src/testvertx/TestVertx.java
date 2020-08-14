/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testvertx;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.viettel.vtpgw.GatewayProcess;
import com.viettel.vtpgw.GatewayService;
import com.viettel.vtpgw.db.entity.Setting;
import com.viettel.vtpgw.db.entity.SettingShareable;
import com.viettel.vtpgw.db.session.SettingSession;
import com.viettel.vtpgw.util.JedisUtil;
import com.viettel.vtpgw.util.JsonUtil;
import com.viettel.vtpgw.util.Utils;
import static com.viettel.vtpgw.util.Utils.CACHE_SHARED_MAP_KEY_DB_SETTING;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
/**
 *
 * @author anPV
 */
public class TestVertx {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        
        
        //Launcher.main(Stream.concat(Stream.of("run", GatewayService.class.getName()), Stream.of(args)).toArray(String[]::new));
        //Launcher.main(Stream.concat(Stream.of("run", GatewayProcess.class.getName()), Stream.of(args)).toArray(String[]::new));
        
        //VertxOptions options = new VertxOptions();
        //options.setClusterHost("test");
        
        //vertx.deployVerticle(new MyVerticle());
        
        //vertx.deployVerticle(new VertxHttpServerVerticle());        
        //vertx.deployVerticle(new VertxHttpClientVerticle());
        
        /*
        vertx.deployVerticle(new EventBusReceiverVerticle("R1"));
        vertx.deployVerticle(new EventBusReceiverVerticle("R2"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestVertx.class.getName()).log(Level.SEVERE, null, ex);
        }
        vertx.deployVerticle(new EventBusSenderVerticle());
        */
        
        //vertx.deployVerticle(new MainVerticle());
        
               
//        Vertx vertx2 = Vertx.vertx();
//        // set value
//        vertx2.deployVerticle(new MemoVerticle("Test1"));
//        
//        // another vertx
//        vertx.deployVerticle(new MyVerticle("MY"));
//        
//        // same vertx
//        vertx2.deployVerticle(new MemoVerticle("Test2"));

        vertx.deployVerticle(new GatewayService());
        //vertx.deployVerticle(new GatewayProcess());
        
        
        //JedisUtil.test();
        
        //test();
        //testJson();
        
        //testLocalMap(vertx);
        //testGetSettingExpire(vertx);
        //testLocalMap(vertx);
        
        //testGetPath();
        
    }    
    
    static void testLocalMap(Vertx vertx){
        SharedData sd = vertx.sharedData();        
        LocalMap<String, SettingShareable> map = sd.getLocalMap(CACHE_SHARED_MAP_KEY_DB_SETTING);
        for(LocalMap.Entry<String, SettingShareable> bc: map.entrySet()){
            String key = bc.getKey();
            SettingShareable sh = bc.getValue();
        }
        
        SettingShareable setting = null;
    }
    
    static void test(){
        ConcurrentLinkedDeque<Integer> list = new ConcurrentLinkedDeque<Integer>();
        list.add(1);
        list.add(2);
        
        Integer i = list.remove();
        i = list.remove();
    }
    
    static void testJson(){
        JsonParser parser = new JsonParser();
//        String json = 
//            "{\n" +
//            "  \"a\": \"Hello\",\n" +
//            "  \"b\": {\n" +
//            "    \"c:\": \"World\"\n" +
//            "  }\n" +
//            "}";
        String json = 
            "{\n" +
            "	\"a\": \"Hello\",\n" +
            "	\"b\": {\n" +
            "		\"c\": \"World\"\n" +
            "	},  \n" +
            "	\"d\": {\n" +
            "		\"e\": {\n" +
            "			\"f\": \"World\",\n" +
            "			\"g\": {\n" +
            "				\"h\": \"First\"\n" +
            "			}\n" +
            "		},\n" +
            "		\"i\": \"test\"\n" +
            "	}\n" +
            "}";

        JsonElement jsonTree = parser.parse(json);
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        JsonUtil.getDir((JsonObject) jsonTree, 2, map);
        
        for (Map.Entry<String, Integer> item : map.entrySet()) {                    
            System.out.println(item.getKey() + ": " + item.getValue());            
        }    
    }
    
//    static void testLocalMap(Vertx vertx){
//        SharedData sd = vertx.sharedData();        
//        LocalMap<String, SettingShareable> map = sd.getLocalMap(CACHE_SHARED_MAP_KEY_DB_SETTING);
////        //SettingShareable test = new SettingShareable(1);
////        
////        SettingShareable test1 = test;
////        //test1 = new SettingShareable();
////        //test1 = test;
////        
////        map.put("1", test);
////        
////        test1 = map.get("1");
//    }
    
    static void feedSetting(Vertx vertx){
        SharedData sd = vertx.sharedData();        
        LocalMap<String, SettingShareable> map = sd.getLocalMap(CACHE_SHARED_MAP_KEY_DB_SETTING);
        
        SettingSession settingSession = new SettingSession();
//        List<Setting> list = settingSession.findAll();
        
        String s;        
//        for(Setting setting: list){
//            if(setting.getStatus() == 0){
//                continue;
//            }
//            
//            s = setting.getUrl();
//            if(setting.getType() == 0){
//                s += "@" + setting.getOperation();                
//            }
//            else {
//                s += "@" + setting.getOperation() + "@" + setting.getParam() + "@" + setting.getParamValue();
//            }
//            
//            map.put(s, new SettingShareable(setting));
//        }
    }
    
    static void testGetSettingExpire(Vertx vertx){
        feedSetting(vertx);
        
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservice.bccsgw.viettel.com/\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <web:gwOperation>\n" +
            "         <Input>\n" +
            "            <username>8c17b9c4499dbf77</username>\n" +
            "            <password>77f0bc7481204bc44687ccba21e07165</password>\n" +
            "            <wscode>bccs2_getInfoSubV4</wscode>\n" +
            "            <param name=\"isdn\" value=\"1686252255\"/>\n" +
            "         </Input>\n" +
            "      </web:gwOperation>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
        String url = "http://10.60.102.181:8888/vtp/bccs-ws/8e442952-afcc-451b-a5fc-821addfc4804";
        
        JsonElement root = JsonUtil.XML2Json(xml, false);
        SettingShareable setting = Utils.getSettingExpire(url, root, vertx);
//        int expire = setting.getExpire();
//        System.out.println("Expire: " + expire);
    }
    
    static void testGetPath(){
        JsonParser parser = new JsonParser();

        String json = 
            "{\n" +
            "  \"Envelope\": {\n" +
            "    \"Body\": {\n" +
            "      \"gwOperation\": {\n" +
            "        \"Input\": {\n" +
            "          \"password\": \"77f0bc7481204bc44687ccba21e07165\",\n" +
            "          \"param\": [\n" +
            "            {\n" +
            "              \"name\": \"isdn\",\n" +
            "              \"value\": \"1689720826\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"name\": \"test\",\n" +
            "              \"value\": \"1\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"wscode\": \"bccs2_getInfoSubV4\",\n" +
            "          \"username\": \"8c17b9c4499dbf77\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"soapenv\": \"http://schemas.xmlsoap.org/soap/envelope/\",\n" +
            "    \"Header\": \"\",\n" +
            "    \"web\": \"http://webservice.bccsgw.viettel.com/\"\n" +
            "  }\n" +
            "}";
        
        JsonObject jsonTree = (JsonObject) parser.parse(json);
        JsonElement[] parent = new JsonElement[1];
        parent[0] = null;
        JsonElement test = JsonUtil.getElementByPath(jsonTree, "Envelope@Body@gwOperation@Input@param@name@isdn", parent);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw;

import com.google.gson.JsonElement;
import com.viettel.vtpgw.config.Config;
import com.viettel.vtpgw.db.entity.SettingShareable;
import com.viettel.vtpgw.db.entity.VtpgwApp;
import com.viettel.vtpgw.db.entity.VtpgwNodes;
import com.viettel.vtpgw.db.entity.VtpgwPermission;
import com.viettel.vtpgw.db.entity.VtpgwService;
import com.viettel.vtpgw.db.session.SettingSession;
import com.viettel.vtpgw.util.JedisUtil;
import com.viettel.vtpgw.util.JsonUtil;
import com.viettel.vtpgw.util.Utils;
import static com.viettel.vtpgw.util.Utils.CACHE_SHARED_MAP_KEY_DB_SETTING;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.JedisCluster;

/**
 *
 * @author anPV
 */
public class GatewayProcess extends AbstractVerticle {

    static final int TIMER_UPDATE_SETTING_LONG = 300; // don vi giay second
    static final int CACHE_SETTING_EXPIRE_TIME = 300; // don vi giay second

    static Logger logger = LogManager.getLogger(GatewayProcess.class);

    String node;
    int updateSettingsCount;
    JedisCluster jedis; // = JedisUtil.getJedisCluster("10.60.155.107:7000,10.60.155.107:7001,10.60.155.107:7002,10.60.155.108:7003,10.60.155.108:7004,10.60.155.108:7005");    

    public GatewayProcess() {
        node = "IP_XXX"; // theo cau hinh
    }

    @Override
    public void start(Future<Void> startFuture) {
        //Set<String> keys = JedisUtil.keys(jedis, "test*");
        logger.info("GatewayProcess - start()");

        String s = Config.getJedisCluster(config());
        if (s == null) {
            logger.error("GatewayProcess - start() - Invalid config for Jedis Cluster ");
            return;
        }
//        jedis = JedisUtil.getJedisCluster(s);

        // thiet dat thoi gian cap nhat cau hinh moi
        updateSettingsCount = 1;
        updateSetting();
        long timerID = vertx.setPeriodic(TIMER_UPDATE_SETTING_LONG * 1000, new Handler<Long>() {
            @Override
            public void handle(Long aLong) {
                updateSetting();
            }
        });

        // cahce process
        vertx.eventBus().consumer(node + "-" + com.viettel.vtpgw.builtin.SoapGateway.CACHE_EVENT_BUS_CHANNEL_SOAP, message -> {
            processMessage(message.body().toString());
        });

        // clear cache process
        vertx.eventBus().consumer(node + "-" + com.viettel.vtpgw.builtin.SoapGateway.CACHE_EVENT_BUS_CHANNEL_CLEAR, message -> {
            processClear(message.body().toString());
        });
    }

    @Override
    public void stop(Future<Void> startFuture) {
        logger.info("GatewayProcess - stop()");
    }

    void updateSetting() {
        Date idTimer = new Date();
        logger.info("GatewayProcess - updateSetting() - Start - id: {}", idTimer);
        feedSetting(this.getVertx(), idTimer);
        logger.info("GatewayProcess - updateSetting() - End - id: {}", idTimer);
    }

    void processMessage(String message) {
        logger.info("GatewayProcess - processMessage() - Start - message: {}", message);
        JsonObject json = new JsonObject(message);
        String uri = json.getString("uri");
        String reqBody = json.getString("request");
        String respBody = json.getString("response");

        JsonElement root = JsonUtil.XML2Json(reqBody, false);
        SettingShareable setting = Utils.getSettingExpire(uri, root, vertx);

//        if (setting == null) {
//            JedisUtil.setKeyBase64(jedis, reqBody, respBody, null, CACHE_SETTING_EXPIRE_TIME);
//        }
        logger.info("GatewayProcess - processMessage() - End - message: {}", message);
    }

    void processClear(String message) {
        logger.info("GatewayProcess - processClear() - Start - message: {}", message);
        JsonObject json = new JsonObject(message);
        String reqBody = json.getString("request");

        JsonElement root = JsonUtil.XML2Json(reqBody, false);
        JsonElement e = JsonUtil.getElementByPath((com.google.gson.JsonObject) root, "Envelope@Body@gwOperation@Input@param", null);
        if (e == null || !e.isJsonObject()) {
            return;
        }

        com.google.gson.JsonObject gson = (com.google.gson.JsonObject) e;
        e = gson.get("value");
        String s;
        if (e != null) {
            s = e.getAsString();
            Set<String> list = JedisUtil.keys(jedis, s + "@" + "*");
            if (list != null && list.size() > 0) {
                for (String key : list) {
                    jedis.del(key);
                    int i = key.indexOf("@");
                    if (i != -1) {
                        s = key.substring(i + 1);
                        jedis.del(s);
                    }
                }
            }
        }
        logger.info("GatewayProcess - processClear() - End - message: {}", message);
    }

    void feedSetting(Vertx vertx, Date idTimer) {
        logger.info("GatewayProcess - feedSetting() - Start - id: {}", idTimer);
        SettingSession settingSession = new SettingSession();
        if(updateSettingsCount > 1){
            // check DB xem co thay doi hay ko
            Date newestUpdate = settingSession.findNewestUpdate(idTimer);
            long diffSeconds = idTimer.getTime() - newestUpdate.getTime();
            logger.info("GatewayProcess - feedSetting() - newestUpdate: {}", newestUpdate);
            logger.info("GatewayProcess - feedSetting() - diffSeconds: {}", diffSeconds);
            if(diffSeconds < 300000){
                logger.info("GatewayProcess - feedSetting() - update Settings - id: {}", idTimer);
                updateSettingsCount = 1;
            } else {
                logger.info("GatewayProcess - feedSetting() - Don't update Settings - id: {}", idTimer);
            }
        }
        if(updateSettingsCount == 1){
            updateSettingsCount++;
            SharedData sd = vertx.sharedData();
            LocalMap<String, SettingShareable> map = sd.getLocalMap(CACHE_SHARED_MAP_KEY_DB_SETTING);
            LocalMap<String, SettingShareable> map_backup = sd.getLocalMap(Utils.CACHE_SHARED_MAP_KEY_DB_SETTING_BACKUP);

            vertx.executeBlocking(future -> {
                List<VtpgwPermission> list = settingSession.findAll(idTimer);
                if (list != null && list.size() > 0) {
                    map.clear();
                    String s;
                    for (VtpgwPermission permission : list) {
                        String serviceId = permission.getServiceId();
                        List<VtpgwService> services = settingSession.findByServiceId(serviceId, idTimer);
                        if (services != null && services.size() > 0) {
                            for(VtpgwService service : services){
                                Collection<VtpgwNodes> nodes = service.getVtpgwNodesCollection();
                                List<String> listRrUrl = new ArrayList<>();
                                if (nodes != null && nodes.size() > 0) {
                                    for (VtpgwNodes aNode : nodes) {
                                        if(aNode.getStatus() == 1){
                                            listRrUrl.add(aNode.getUrl());
                                        }
                                    }
                                }
                                VtpgwApp app = permission.getAppId();
                                s = permission.getPermissionId() + "$" + service.getName() + "$" + app.getAppId() + "$" + app.getToken();
                                map.put(s, new SettingShareable(permission, service, app, listRrUrl));
                                
                            }
                        }

                    }
                    map_backup.clear();
                    map_backup.putAll(map);
                }
                
                future.complete("Success");

            }, res -> {
//                Object obj = res.result();
//                String s = obj.toString();
            });
        }
        logger.info("GatewayProcess - feedSetting() - End - id: {}", idTimer);
    }
}

package com.viettel.vtpgw;

import com.viettel.vtpgw.builtin.RestGateway;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.viettel.vtpgw.builtin.SoapGateway;
import com.viettel.vtpgw.config.Config;
import com.viettel.vtpgw.db.entity.SettingShareable;
import com.viettel.vtpgw.http.HttpServerRequestWrapper;

import com.viettel.vtpgw.model.BpmnService;
import com.viettel.vtpgw.model.HttpService;
import com.viettel.vtpgw.model.impl.DefaultEndpoint;
import com.viettel.vtpgw.util.Utils;
import static com.viettel.vtpgw.util.Utils.CACHE_SHARED_MAP_KEY_DB_SETTING;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.jdbc.JDBCClient;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang.StringUtils;

public class GatewayService extends AbstractVerticle {

    private static final Pattern CONTENT_TYPE = Pattern.compile("^([\\w\\-_]+/[\\w\\-_]+)(?:\\s*;\\s*charset=([^;]+))",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern PATH = Pattern.compile("^/([\\w\\-_]+)/([\\w\\-_]+)/([\\w\\-_]+)(?:/.*)?$");
    static Logger logger = LogManager.getLogger(GatewayService.class);
    static String node;

    private HttpClient httpClient;
    private HttpServer httpServer;
    private Buffer pong;
    private String statusPath;
    private String jedisConnection;
    private String lbAddress = "10.58.71.187:8686";
    private String vtpgwAddress = "10.58.71.187:8686";
    private String rrUrl;

    SoapGateway soapGateway;
    JDBCClient jdbcClient;

    @PersistenceContext
    public EntityManager em;

    public GatewayService() {
        node = "IP_XXX"; // theo cau hinh
    }

    private void forward(HttpServerRequestWrapper req, String module) {
        logger.info("GatewayService - forward() - Start - AccessId: {}", req.getAccessId());
        
        // can truy van thiet dat service tren elastic de biet
        // module tuong ung voi uri request la module nao ...
        switch (module) {
            case HttpService.BUILT_IN_SOAP:
                logger.info("GatewayService - forward() - BUILT IN SOAP - AccessId: {}", req.getAccessId());
                processSoap(req);
                break;
            case HttpService.BUILT_IN_REST:
                logger.info("GatewayService - forward() - BUILD IN REST - AccessId: {}", req.getAccessId());
                processRest(req);
                break;
            case BpmnService.BUILT_IN_BPMN:
                logger.info("GatewayService - forward() - BUILD IN BPMN - AccessId: {}", req.getAccessId());
                processBpmn(req);
                break;
            default:
                logger.info("GatewayService - forward() - build default - AccessId: {}", req.getAccessId());
                req.bodyHandler(body -> {
                    processModule(module, body, req);
                });
                req.resume();
        }
        logger.info("GatewayService - forward() - End - AccessId: {}", req.getAccessId());
    }

    private void handleHttpRequest(HttpServerRequest originRequest) {
        UUID rd =UUID.randomUUID();
        String uuId = String.valueOf(rd);
        logger.info("GatewayService - handleHttpRequest() - Start - AccessId: {}", uuId);
        System.out.println("GatewayService - handleHttpRequest() - Start - AccessId: {}" + uuId);
        // uri: http://10.61.100.108:8080/vtp/getSubscriberInfo/062c6761-537a-4d75-aaa9-9522fd1f1c16/test?wsdl
        // => path: "/vtp/getSubscriberInfo/062c6761-537a-4d75-aaa9-9522fd1f1c16"
        String path = originRequest.path();
        SharedData sd = vertx.sharedData();        
        LocalMap<String, SettingShareable> map = sd.getLocalMap(CACHE_SHARED_MAP_KEY_DB_SETTING);  
        LocalMap<String, SettingShareable> map_backup = sd.getLocalMap(Utils.CACHE_SHARED_MAP_KEY_DB_SETTING_BACKUP);  
        
        if ("/ping".equals(path)) {
            originRequest.response().end(pong);
            return;
        }
        if (statusPath.equals(path)) {
            originRequest.response().end(pong);
            return;
        }
        if ("/favicon.ico".equals(path)) {
            originRequest.response().setStatusCode(404).end("Not found");
            return;
        }
        originRequest.pause();
        MultiMap headers = originRequest.headers();
        String contentType = headers.get(HttpHeaders.CONTENT_TYPE);
        System.out.println("Content Type: " + contentType);

        Matcher m = PATH.matcher(path);
        if (m.find()) {
            String user = m.group(1);
            String auth = headers.get(HttpHeaders.AUTHORIZATION);
            String username, password;

            if (auth != null) {
                String accessAccount = new String(Base64.getDecoder().decode(auth.substring(6)));
                int pos = accessAccount.indexOf(':');

                if (pos > 0) {
                    username = accessAccount.substring(0, pos);
                    password = accessAccount.substring(pos + 1);
                } else {
                    username = accessAccount;
                    password = null;
                }
            } else {
                username = user;
                password = null;
            }

            String externalServiceName = m.group(2);
            String permissionId = m.group(3);

            if (HttpMethod.TRACE == originRequest.method()) {
                originRequest.resume();
            } else {
                HttpServerRequestWrapper req = new HttpServerRequestWrapper(originRequest, vertx);

                // thiet dat mac dinh duoc xac thuc - bypass
                req.setAuthorized(true);
                req.setAccessId(uuId);
                req.setStartTime(System.currentTimeMillis());
                req.setTargetService(externalServiceName);

                 String encoding = null;
                 if (contentType != null) {
                     Matcher matcher = CONTENT_TYPE.matcher(contentType);
                     if (matcher.find()) {
                         encoding = matcher.group(2);
                     }
                 }
          
                 if (encoding != null) {
                     encoding = encoding.replaceAll("[\"']", "");
                 } else {
                     encoding = "UTF-8";
                 }
                 
                 req.setCharset(encoding);
                 req.setUsername(username);
                 
                 // ... => lay duong dan sau "http://<host:port>/<account>/<ws id>", ket qua: "/test/..."
                 req.setPath(path.substring(m.end(3)));
                 if (lbAddress != null) {
                     req.setContext("http://" + lbAddress + path.substring(0, m.end(3)));
                     logger.info("Request - http://" + lbAddress + path.substring(0, m.end(3)));
                 } else {
                     req.setContext((req.isSSL() ? "https://" : "http://") + req.localAddress().host() + ":"
                             + req.localAddress().port() + path.substring(0, m.end(3)));
                 }
                 // Check SOAP, REST from uri Start - ToprateDev
                 String module = "REST";
                 String uriReq = req.uri();
                 if(StringUtils.substring(uriReq, -5).equals("?wsdl") || StringUtils.substring(uriReq, -6).equals("?xsd=1")){
                    logger.info("GatewayService - handleHttpRequest() - module: SOAP - AccessId: {}", uuId);
                    module = "SOAP";
                 } else {
                     if(contentType != null && contentType.contains("text/xml")){
                        module = "SOAP";
                        logger.info("GatewayService - handleHttpRequest() - module: SOAP - AccessId: {}", uuId);
                     } else {
                         logger.info("GatewayService - handleHttpRequest() - module: REST - AccessId: {}", uuId);
                     }
                 }
                 // Check SOAP, REST from uri End - ToprateDev
                 
                 // Check permission Start - ToprateDev
                 String key = permissionId + "$" + externalServiceName + "$" + user + "$" + password;
                SettingShareable settingShareable = map.get(key);
                SettingShareable settingShareableBackup = map_backup.get(key);
                if (settingShareable != null || settingShareableBackup!= null) {
                    if(settingShareable == null){
                        settingShareable = settingShareableBackup;
                    }
                    if(!module.equals(settingShareable.getModule())){
                        logger.info("GatewayService - handleHttpRequest() - Permission fail - AccessId: {}", uuId);
                        originRequest.response().end("Permission Fail !!!");
                    }
                    if("REST".equals(module)){
                        if("GET".equals(req.method().toString()) || "GET".equals(settingShareable.getMethod())){
                            logger.info("GatewayService - handleHttpRequest() - Permission fail - AccessId: {}", uuId);
                            originRequest.response().end("Permission Fail !!!");
                        }
                    }
                    logger.info("GatewayService - handleHttpRequest() - Permission pass - AccessId: {}", uuId);
                    // Round Robin URL Start - ToprateDev
                    List<String> listRrUrl = settingShareable.getListRrUrl();
                    SecureRandom rnd = new SecureRandom();
                    int random = rnd.nextInt(listRrUrl.size());
                    rrUrl = listRrUrl.get(random);
                    // Round Robin URL End - ToprateDev
                } else {
                    logger.info("GatewayService - handleHttpRequest() - Permission fail - AccessId: {}", uuId);
                    originRequest.response().end("Permission Fail !!!");
                }
                // Check permission End - ToprateDev
                 forward(req, module);
             }

        } else {
            originRequest.resume();
            originRequest.response().setStatusCode(404).end("Not found");
        }
        logger.info("GatewayService - handleHttpRequest() - End - AccessId: {}", uuId);
    }

    private void processBpmn(HttpServerRequestWrapper req) {
        logger.info("GatewayService - processBpmn() - Start - AccessId: {}", req.getAccessId());
        boolean debug = true;
        /*
        switch (req.method()) {
            case GET:
                req.resume().response().end(service.process().wsdl().replace("${endpoint}", req.getContext()));
                break;
            case POST:
                if (req.isAuthorized()) {
                    req.exceptionHandler(th -> {
                        req.response().setStatusCode(400).end("Bad request");
                    });
                    req.bodyHandler(body -> {
                        String bodyAsString = body.toString(req.getCharset());
                        if (debug) {
                            try {
                                req.setParams("\"raw\":\"" + StringEscapeUtils.escapeJava(bodyAsString) + '"');
                            } catch (Exception e) {
                                logger.error("Can not escape {}", bodyAsString, e);
                            }
                        }
                        Map<String, String> params = service.params();
                        if (params != null) {
                            KxmlSoapExtractor.INSTANCE.extract(params, bodyAsString, req);
                        }
                       
                    service.process().process(bodyAsString, req, httpClient, httpsClient);
                    });
                                    
                    req.resume();
                } else {
                    req.resume().response().setStatusCode(403).end("Not authorized");
                }
                break;
            default:
                req.resume().response().setStatusCode(405).end("Method Not Allowed");
        }
         */
        logger.info("GatewayService - processBpmn() - End - AccessId: {}", req.getAccessId());
    }

    private void processModule(String module, Buffer body, HttpServerRequestWrapper req) {
        logger.info("GatewayService - processModule() - AccessId: {}", req.getAccessId());
    }

    private void processRest(HttpServerRequestWrapper req) {
        logger.info("GatewayService - processRest() - Start - AccessId: {}", req.getAccessId());

//        HttpClient httpClient = true? this.httpsClient : this.httpClient;
//        String uri  = req.absoluteURI();
//        String path = req.getPath();
        // thay the ip:port cua proxy => ip:port cua LB la dia chi dich
//        uri = uri.replace(lbAddress, vtpgwAddress);
         DefaultEndpoint endpoint = new DefaultEndpoint();

        //endpoint.setAuthorization("Basic dnRwOnBhc3N3b3Jk");
        endpoint.setAuthorization(req.getHeader(HttpHeaders.AUTHORIZATION));
        endpoint.setUrl(rrUrl);
        endpoint.setActivated(true);
        endpoint.setHost(req.localAddress().host());
        endpoint.setNode(node);
        logger.info("GatewayService - setEndPoint:  {} - AccessId: {}", endpoint, req.getAccessId());

        req.exceptionHandler(th -> {
            logger.error("GatewayService - processSoap() - exceptionHandler :{}", th);
            req.response().setStatusCode(400).end("Bad request");
        });

        if (!req.isAuthorized()) {
            logger.info("GatewayService - processSoap() - Not authorized");
            req.resume();
            req.response().setStatusCode(403).end("Not authorized");
        }

        if (req.method() == HttpMethod.POST || req.method() == HttpMethod.PUT) {
            req.bodyHandler(body -> {
                String bodyAsString = body.toString(req.getCharset());
                req.setParams(bodyAsString);

                boolean debug = true;
                if (debug) {
                    try {
                        String reqJson = req.getParams();
                        req.setParams(StringEscapeUtils.escapeJava(body.toString(req.getCharset())));
                    } catch (Exception e) {
                        logger.debug(e);
                    }
                }
                logger.info("GatewayService - bodyHandler:  {} - AccessId: {}", body, req.getAccessId());
                RestGateway.process(httpClient, endpoint, req, body, debug);
            });
            req.resume();
        } else {
            req.resume();
            req.response().setStatusCode(403).end("Not authorized");
        }
        logger.info("GatewayService - processRest() - End - AccessId: {}", req.getAccessId());
    }

    /**
     * Ham: thuc hien lay thong tin SOAP tu http request tu client req: http
     * request tu phia client
     */
    private void processSoap(HttpServerRequestWrapper req) {
        logger.info("GatewayService - processSoap() - Start - AccessId: {}", req.getAccessId());
//        HttpClient httpClient = false? this.httpsClient : this.httpClient;
//        String uri  = req.absoluteURI();
//        String path = req.getPath();

        // thay the ip:port cua proxy => ip:port cua LB la dia chi dich
//        uri = uri.replace(lbAddress, vtpgwAddress);
        DefaultEndpoint endpoint = new DefaultEndpoint();

        //endpoint.setAuthorization("Basic dnRwOnBhc3N3b3Jk");
        endpoint.setAuthorization(req.getHeader(HttpHeaders.AUTHORIZATION));
        endpoint.setUrl(rrUrl);
        endpoint.setActivated(true);
        endpoint.setHost(req.localAddress().host());
        endpoint.setNode(node);

        switch (req.method()) {
            case GET:
                logger.info("GatewayService - processSoap() - GET - AccessId: {}", req.getAccessId());
                req.resume();                
                soapGateway.processSchema(httpClient, rrUrl, endpoint, req);
                break;
            case POST:
                logger.info("GatewayService - processSoap() - POST - AccessId: {}", req.getAccessId());
                if (req.isAuthorized()) {
                    req.exceptionHandler(th -> {
                        logger.error("GatewayService - processSoap() - POST - exceptionHandler :{} - AccessId: {}", th, req.getAccessId());
                        req.response().setStatusCode(400).end("Bad request");
                    });

                    boolean debug = true;
                    req.bodyHandler(body -> {
                        String bodyAsString = body.toString(req.getCharset());
                        if (debug) {
                            try {
                                req.setParams("\"raw\":\"" + StringEscapeUtils.escapeJava(bodyAsString) + '"');
                            } catch (Exception e) {
                                logger.error("GatewayService - processSoap() - POST - bodyHandler() - Can not escape {} - AccessId: {}", bodyAsString, e, req.getAccessId());
                            }
                        }
                        soapGateway.process(httpClient, endpoint, req, body, debug);
                    });
                    req.resume();
                }
                break;
            default:
                logger.info("GatewayService - processSoap() - default - AccessId: {}", req.getAccessId());
                req.resume();
                req.response().setStatusCode(405).end("Method Not Allowed");
        }
        logger.info("GatewayService - processSoap() - End - AccessId: {}", req.getAccessId());
    }

    @Override
    public void start(Future<Void> startFuture) {
        logger.info("GatewayService - start()");
        JsonObject test = config();
        jedisConnection = Config.getJedisCluster(test);
        if(jedisConnection == null){
            logger.error("GatewayService - start() - Invalid config for Jedis Cluster ");
            return;
        }

        lbAddress = Config.getLbAddress(test);
        if(lbAddress == null){
            logger.error("GatewayService - start() - Invalid config for LB address ");
        }

        vtpgwAddress = Config.getVtpgwAddress(test);
        if(vtpgwAddress == null){
            logger.error("GatewayService - start() - Invalid config for VTPGW address ");
        }

        soapGateway = SoapGateway.getInstance(node, jedisConnection, lbAddress);

        HttpClientOptions clientOpts = new HttpClientOptions(config().getJsonObject("httpClient", new JsonObject()));
        httpClient = vertx.createHttpClient(clientOpts);
//        httpsClient = vertx
//                .createHttpClient(new HttpClientOptions(clientOpts).setSsl(true).setTrustAll(true).setVerifyHost(false));

        int port = Config.getHttpServerPort(config());

//        ipHeader = config().getString("ipHeader");
        statusPath = config().getString("status", "/nginx_status");
//        passProxy = ipHeader != null && !ipHeader.isEmpty();
        JsonObject httpConfig = config().getJsonObject("http", new JsonObject());
        HttpServerOptions options = new HttpServerOptions(httpConfig);

        pong = Buffer.buffer("pong");
        httpServer = vertx.createHttpServer(options).requestHandler(req -> this.handleHttpRequest(req)).listen(port,
                result -> {
                    if (result.succeeded()) {
                        logger.info("GatewayService - start() - createHttpServer succeeded, port :{}", port);
                        startFuture.complete();
                    } else {
                        logger.error("GatewayService - start() - createHttpServer failed, port :{}", port, result.cause());
                        startFuture.fail(result.cause());
                    }
                });

        GatewayProcess verticleGW = new GatewayProcess();
        DeploymentOptions deployOptions = new DeploymentOptions();
        deployOptions.setConfig(test);
        vertx.deployVerticle(verticleGW, deployOptions);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        logger.info("GatewayService - stop()");
        if (httpServer != null) {
            httpServer.close();
        }

        stopFuture.complete();
    }
}

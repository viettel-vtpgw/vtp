package com.viettel.vtpgw.builtin;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.viettel.vtpgw.http.HttpServerRequestWrapper;

import com.viettel.vtpgw.model.Endpoint;
import com.viettel.vtpgw.model.HttpService;
import com.viettel.vtpgw.support.HttpClientHelper;
import com.viettel.vtpgw.support.soap.KxmlSoapExtractor;
import com.viettel.vtpgw.util.LogUtil;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.TimeoutException;

public class RestGateway extends HttpGateway {

    private static final Logger LOG = LogManager.getLogger(RestGateway.class);
    static Logger loggerIO = LogManager.getLogger("InputOutput");

    static Logger loggerGWIO = LogManager.getLogger("Gateway-Out");

    public static void process(HttpClient client, HttpService service, Endpoint endpoint, HttpServerRequestWrapper req,
            boolean debug) {

        String endpointUrl = endpoint.url();
        String uri = endpointUrl + req.getPath();
        String uriWithQuery = req.query() != null ? uri + (endpointUrl.indexOf('?') > 0 ? "" : "?") + req.query() : uri;
        long start = System.currentTimeMillis();
        Buffer reqBody = Buffer.buffer();
        HttpClientRequest cReq = client.requestAbs(req.method(), uriWithQuery, cResp -> {
            cResp.pause();
            HttpServerResponse resp = req.response();
            postResponse(cResp, resp);
            resp.setChunked(true);
            cResp.exceptionHandler(th -> {
                if (!req.response().ended()) {
                    resp.setStatusCode(502).end("Bad gateway");
                    req.out(start, System.currentTimeMillis() - start, req.method().name(), uriWithQuery,
                            "fail to get response from external service", th);
                }
            });
            Buffer respBuff = Buffer.buffer();
            cResp.handler(buff -> {
                respBuff.appendBuffer(buff);
                resp.write(buff);
            });
            cResp.endHandler(v -> {
                long duration = System.currentTimeMillis() - start;
                resp.end();
                boolean check;
                int statusCode = cResp.statusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    String contentAsString = respBuff.toString(StandardCharsets.UTF_8);
                    Map<String, String> params = service.respParams();
                    if (params != null && "*".equals(params.get("*"))) {
                        req.setRespParams(contentAsString);
                    }
                    if (debug) {
                        try {
                            String respJson = req.getRespParams();
                            req.setRespParams((respJson != null && !respJson.isEmpty() ? ",\"raw\":\"" : "\"raw\":\"")
                                    + StringEscapeUtils.escapeJava(contentAsString) + '"');
                        } catch (Exception e) {
                            LOG.error("Can not escape {}", contentAsString, e);
                        }
                    }
                    check = !service.check() || service.check(contentAsString);
                } else {
                    check = false;
                }
                req.out(start, duration, req.method().name(), uriWithQuery, cResp.statusCode(), cResp.statusMessage(), check);
                if (duration > service.standardDuration()) {
                    List<String> alertReceivers = service.alertReceivers();
                    if (alertReceivers != null) {
                    }
                }
            });
            cResp.resume();
        });
        preRequest(req, cReq, endpoint);
        cReq.setChunked(true);
        req.handler(buff -> {
            reqBody.appendBuffer(buff);
            cReq.write(buff);
        });
        req.endHandler(v -> {
            cReq.end();
            if (service.params() != null) {
                KxmlSoapExtractor.INSTANCE.extract(service.params(), reqBody.toString(req.getCharset()), req);
            }
        });
        long timeout = service.timeout();

        HttpClientHelper.setTimeout(cReq, timeout, th -> {

            List<String> alertReceivers = service.alertReceivers();
            if (alertReceivers != null) {

            }
            if (!req.response().ended()) {
                req.out(start, System.currentTimeMillis() - start, req.method().name(), uriWithQuery,
                        "fail to request external service", th);
                req.response().setStatusCode(502).end("Bad gateway");
            }
        });
        // cReq.setTimeout(timeout);    
        req.resume();
    }

    public static void process(HttpClient client, HttpService service, Endpoint endpoint, HttpServerRequestWrapper req,
            Buffer body, boolean debug) {
        String endpointUrl = endpoint.url();
        String uri = endpointUrl + req.getPath();
        String uriWithQuery = req.query() != null ? uri + (endpointUrl.indexOf('?') > 0 ? "" : "?") + req.query() : uri;
        long start = System.currentTimeMillis();
        HttpClientRequest cReq = client.requestAbs(req.method(), uriWithQuery, cResp -> {
            cResp.pause();
            HttpServerResponse resp = req.response();
            postResponse(cResp, resp);
            resp.setChunked(true);
            cResp.exceptionHandler(th -> {
                if (!req.response().ended()) {
                    resp.setStatusCode(502).end("Bad gateway");
                    req.out(start, System.currentTimeMillis() - start, req.method().name(), uriWithQuery,
                            "fail to get response from external service", th);
                }
            });
            Buffer respBuff = Buffer.buffer();
            cResp.handler(buff -> {
                respBuff.appendBuffer(buff);
                resp.write(buff);
            });
            cResp.endHandler(v -> {
                long duration = System.currentTimeMillis() - start;
                resp.end();
                boolean check;
                int statusCode = cResp.statusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    String contentAsString = respBuff.toString(StandardCharsets.UTF_8);
                    Map<String, String> params = service.respParams();
                    if (params != null && "*".equals(params.get("*"))) {
                        req.setRespParams(contentAsString);
                    }
                    if (debug) {
                        try {
                            String respJson = req.getRespParams();
                            req.setRespParams((respJson != null && !respJson.isEmpty() ? ",\"raw\":\"" : "\"raw\":\"")
                                    + StringEscapeUtils.escapeJava(contentAsString) + '"');
                        } catch (Exception e) {
                            LOG.error("Can not escape {}", contentAsString, e);
                        }
                    }
                    // req.setRespParams(contentAsString);
                    check = !service.check() || service.check(contentAsString);

                } else {
                    check = false;

                }
                req.out(start, duration, req.method().name(), uriWithQuery, cResp.statusCode(), cResp.statusMessage(), check);
                if (duration > service.standardDuration()) {
                    List<String> alertReceivers = service.alertReceivers();
                    if (alertReceivers != null) {

                    }
                }
            });
            cResp.resume();
        });
        preRequest(req, cReq, endpoint);
        long timeout = service.timeout();
        HttpClientHelper.setTimeout(cReq, timeout, th -> {

            List<String> alertReceivers = service.alertReceivers();
            if (alertReceivers != null) {

            }
            if (!req.response().ended()) {
                req.out(start, System.currentTimeMillis() - start, req.method().name(), uriWithQuery,
                        "fail to request external service", th);
                req.response().setStatusCode(502).end("Bad gateway");
            }
        });
        cReq.end(body);
    }

    public static void process(
            HttpClient client,
            //            HttpService service, 
            Endpoint endpoint,
            HttpServerRequestWrapper req,
            Buffer body,
            boolean debug) {
        LOG.info("RestGateway - process() - Start - AccessId: {}", req.getAccessId());
        String endpointUrl = endpoint.url();
        String uri = endpointUrl + req.getPath();
        String uriWithQuery = req.query() != null ? uri + (endpointUrl.indexOf('?') > 0 ? "" : "?") + req.query() : uri;
        long start = System.currentTimeMillis();
        Buffer bodyResp = Buffer.buffer();
        LOG.info("RestGateway - process() - setUriWithQuery - AccessId: {}", req.getAccessId());
        // trong truong hop chua co trong cache
        // tien hanh gui request den may dich de lay thong tin ...    
        HttpClientRequest cReq = client.requestAbs(req.method(), uriWithQuery, cResp -> {
            cResp.pause();
            HttpServerResponse resp = req.response();

            // thuc hien sao chep headers tu response den may
            postResponse(cResp, resp);
//            resp.setChunked(true);

            cResp.exceptionHandler(th -> {
                if (!req.response().ended()) {
                    Buffer buff = Buffer.buffer();
                    buff.appendString("Bad gateway");
                    resp.setStatusCode(502).end(buff.toString());

                    resp.setStatusCode(502).end("Bad gateway");
                    // write log
//                    req.out(start, System.currentTimeMillis() - start, req.method().name(), uriWithQuery,
//                            "fail to get response from external service", th);
                    LogUtil.transaction(loggerIO, uriWithQuery, req.method().name(), req.getTargetService(), start, System.currentTimeMillis() - start, 502, 0, req.getAccessId(), body.toString(), bodyResp.toString());
                    LogUtil.transaction(loggerGWIO, uriWithQuery, req.method().name(), req.getTargetService(), req.getStartTime(), System.currentTimeMillis() - req.getStartTime(), 502, 0, req.getAccessId(), body.toString(), bodyResp.toString());
                }
            });

            // lay thong tin reponse tu may dich            
            {
                cResp.handler(buff -> {
                    bodyResp.appendBuffer(buff);
                    resp.write(buff);
                });

                cResp.endHandler(v -> {
                    long duration = System.currentTimeMillis() - start;
                    resp.end();
                    // Can phan tich noi dung body de lay thoi gian luu du lieu tren bo dem
                    // theo web service va ham cua web service
                    // ???
                    //JedisUtil.setKeyBase64(jedis, body.toString(), bodyResp.toString(), 600);
                    // dong doi du lieu de thuc hien phan tich xu ly luu tru o luong khac
                    // toi uu hoa hoat dong cua CacheGW
                    JsonObject message = new JsonObject();
                    message.put("uri", uri);
                    message.put("request", body.toString());
                    message.put("response", bodyResp.toString());
                    //jedis.publish(CACHE_SOAP_JEDIS_CHANNEL_1, message.toString());
//                    Utils.send2Process(endpoint.node() + "-" + CACHE_EVENT_BUS_CHANNEL_SOAP, message.toString(), req.getVertx());

//                    String TransactionCode = req.getParam("transaction");
                    LogUtil.transaction(loggerIO, uriWithQuery, req.method().name(), req.getTargetService(), start, duration, resp.getStatusCode(), 1, req.getAccessId(), body.toString(), bodyResp.toString());
                    LogUtil.transaction(loggerGWIO, uriWithQuery, req.method().name(), req.getTargetService(), req.getStartTime(), System.currentTimeMillis() - req.getStartTime(), resp.getStatusCode(), 1, req.getAccessId(), body.toString(), bodyResp.toString());
                    boolean check;

                    // thuc hien luu cache thong tin tu diem nay vao redis cluster
                    int statusCode = cResp.statusCode();
                    if (check = (statusCode >= 200 && statusCode < 300)) {

                    } else {

                    }
                });
            }
            cResp.resume();
        });

        cReq.exceptionHandler(th -> {
            if (!req.response().ended()) {
                Buffer buff = Buffer.buffer();
                buff.appendString("Bad gateway");
                req.response().setStatusCode(502).end(buff.toString());
                long duration = System.currentTimeMillis() - start;
//                LogUtil.print(loggerIO, uriWithQuery, req.method().name(), start, System.currentTimeMillis() - start, 502, 0, 0, body.toString(), buff.toString());
                LogUtil.transaction(loggerIO, uriWithQuery, req.method().name(), req.getTargetService(), start, duration, 502, 0, req.getAccessId(), body.toString(), bodyResp.toString());
                LogUtil.transaction(loggerGWIO, uriWithQuery, req.method().name(), req.getTargetService(), req.getStartTime(), System.currentTimeMillis() - req.getStartTime(), 502, 0, req.getAccessId(), body.toString(), bodyResp.toString());
            }
        });
        preRequest(req, cReq, endpoint);
        long timeout = 60000;

        Handler<Throwable> handler = (th) -> {
            Buffer buff = Buffer.buffer();
            int code;

            if (!req.response().ended()) {
                req.out(start, System.currentTimeMillis() - start, req.method().name(), uriWithQuery,
                        "fail to request external service", th);
                if (th instanceof TimeoutException) {
                    buff.appendString("Proxy Timeout");
                    code = 504;
                } else {
                    code = 502;
                    buff.appendString("Bad gateway");
                }

                req.response().setStatusCode(code).end(buff.toString());
//                LogUtil.print(loggerIO, uriWithQuery, req.method().name(), start, System.currentTimeMillis() - start, code, 0, 0, body.toString(), buff.toString());
                LogUtil.transaction(loggerIO, uriWithQuery, req.method().name(), req.getTargetService(), start, System.currentTimeMillis() - start, code, 0, req.getAccessId(), body.toString(), bodyResp.toString());
                LogUtil.transaction(loggerGWIO, uriWithQuery, req.method().name(), req.getTargetService(), req.getStartTime(), System.currentTimeMillis() - req.getStartTime(), code, 0, req.getAccessId(), body.toString(), bodyResp.toString());
            }
        };

        if (timeout > 0) {
            HttpClientHelper.setTimeout(cReq, timeout, handler);
        } else {
            cReq.exceptionHandler(handler);
        }
        LOG.info("RestGateway - process() - End - AccessId: {}", req.getAccessId());
        cReq.end(body);
    }

    public static void process(HttpClient client, HttpService service, Endpoint endpoint, HttpServerRequestWrapper req,
            Future<Buffer> body, boolean debug) {

        String endpointUrl = endpoint.url();
        String uri = endpointUrl + req.getPath();
        String uriWithQuery = req.query() != null ? uri + (endpointUrl.indexOf('?') > 0 ? "" : "?") + req.query() : uri;
        long start = System.currentTimeMillis();
        HttpClientRequest cReq = client.requestAbs(req.method(), uriWithQuery, cResp -> {
            cResp.pause();
            HttpServerResponse resp = req.response();
            postResponse(cResp, resp);
            resp.setChunked(true);
            cResp.exceptionHandler(th -> {
                if (!req.response().ended()) {
                    resp.setStatusCode(502).end("Bad gateway");
                    req.out(start, System.currentTimeMillis() - start, req.method().name(), uriWithQuery,
                            "fail to get response from external service", th);
                }
            });
            Buffer respBuff = Buffer.buffer();
            cResp.handler(buff -> {
                respBuff.appendBuffer(buff);
                resp.write(buff);
            });
            cResp.endHandler(v -> {
                long duration = System.currentTimeMillis() - start;
                resp.end();
                boolean check;
                int statusCode = cResp.statusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    String contentAsString = respBuff.toString(StandardCharsets.UTF_8);
                    Map<String, String> params = service.respParams();
                    if (params != null && "*".equals(params.get("*"))) {
                        req.setRespParams(contentAsString);
                    }
                    if (debug) {
                        try {
                            String respJson = req.getRespParams();
                            req.setRespParams((respJson != null && !respJson.isEmpty() ? ",\"raw\":\"" : "\"raw\":\"")
                                    + StringEscapeUtils.escapeJava(contentAsString) + '"');
                        } catch (Exception e) {
                            LOG.error("Can not escape {}", contentAsString, e);
                        }
                    }
                    check = !service.check() || service.check(contentAsString);

                } else {
                    check = false;

                }
                req.out(start, duration, req.method().name(), uriWithQuery, cResp.statusCode(), cResp.statusMessage(), check);
                if (duration > service.standardDuration()) {
                    List<String> alertReceivers = service.alertReceivers();
                    if (alertReceivers != null) {

                    }
                }
            });
            cResp.resume();
        });
        preRequest(req, cReq, endpoint);
        cReq.setChunked(true);
        cReq.sendHead();
        body.setHandler(bodyResult -> {
            if (bodyResult.succeeded()) {
                long timeout = service.timeout();

                HttpClientHelper.setTimeout(cReq, timeout, th -> {

                    List<String> alertReceivers = service.alertReceivers();
                    if (alertReceivers != null) {
                    }

                    if (!req.response().ended()) {
                        req.out(start, System.currentTimeMillis() - start, req.method().name(), uriWithQuery,
                                "fail to request external service", th);
                        req.response().setStatusCode(502).end("Bad gateway");
                    }
                });
                //cReq.setTimeout(timeout);
                cReq.end(bodyResult.result());
            } else {
                cReq.setTimeout(1);
                req.response().setStatusCode(400).end("Bad request");
            }
        });
    }
}

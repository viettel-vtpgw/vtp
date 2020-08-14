package com.viettel.vtpgw.context;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.viettel.vtpgw.util.NumberUtils;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;

public abstract class RequestContext {

    protected String accessId;

    protected String appCode;

    protected boolean authorized;

    protected String charset;

    protected String clientIp;

    protected String context;

    protected boolean debug;

    Long externalCall;

    protected String func;

    protected HttpMethod method;
    protected String node;
    protected String params;
    protected String path;
    protected String query;
    protected String respParams;
    protected boolean sandbox;
    protected String targetService;

    protected String traceId;
    protected String username = "";
    protected String authorization = "";
    
    protected long startTime;

    protected RequestContext(boolean authorized, HttpMethod method, String context, String path,
            String query, String charset, String username, String clientIp, String traceId, String func, String params,
            String appCode, String targetService, String accessId, boolean debug, boolean sandbox) {

        this.authorized = authorized;
        this.method = method;
        this.context = context;
        this.path = path;
        this.query = query;
        this.charset = charset;
        this.username = username;
        this.clientIp = clientIp;
        this.traceId = traceId;
        this.func = func;
        this.params = params;
        this.appCode = appCode;
        this.targetService = targetService;
        this.accessId = accessId;
        this.debug = debug;
        this.sandbox = sandbox;
    }

    protected RequestContext(HttpMethod method, String query) {
        this.method = method;
        this.query = query;

        traceId = NumberUtils.generateUUID();
    }

    public MultiMap exportTo(MultiMap map) {
        NumberUtils.setLongToMultiMap(map, "-e", externalCall);
        return map;
    }

    public final void forward(EventBus bus, String module, Buffer body, long timeout) {
        MultiMap map = MultiMap.caseInsensitiveMultiMap();

        map.set("-m", Integer.toHexString(method.ordinal()));
        if (context != null) {
            map.set("-x", context);
        }
        if (path != null) {
            map.set("-p", path);
        }
        if (query != null) {
            map.set("-q", query);
        }
        if (charset != null) {
            map.set("-c", charset);
        }
        if (username != null) {
            map.set("-u", username);
        }
        if (clientIp != null) {
            map.set("-i", clientIp);
        }
        if (traceId != null) {
            map.set("-t", traceId);
        }
        if (func != null) {
            map.set("-f", func);
        }
        if (params != null) {
            map.set("-ps", params);
        }
        if (appCode != null) {
            map.set("-ac", appCode);
        }
        if (authorized) {
            map.set("-au", "1");
        }
        if (targetService != null) {
            map.set("-s", targetService);
        }
        if (accessId != null) {
            map.set("-a", accessId);
        }
        if (sandbox) {
            map.set("-sb", "1");
        }
        if (debug) {
            map.set("-d", "1");
        }

        DeliveryOptions opts = new DeliveryOptions().setHeaders(map);
        if (timeout > 0) {
            opts.setSendTimeout(timeout);
        }
        bus.<Buffer>send(module, body, opts, rs -> {
            ResponseContext response = response();
            if (rs.succeeded()) {
                response.merge(rs.result());
            } else {
                response.setStatusCode(500).end("Internal Server Error " + rs.cause().getMessage());
            }
        });
    }

    public final String getAccessId() {
        return accessId;
    }

    public final String getAppCode() {
        return appCode;
    }

    public final String getCharset() {
        return charset;
    }

    public final String getClientIp() {
        return clientIp;
    }

    public final String getContext() {
        return context;
    }

    public final String getFunc() {
        return func;
    }

    public final HttpMethod getMethod() {
        return method;
    }

    public final String getNode() {
        return node;
    }

    public final String getParams() {
        return params;
    }

    public final String getPath() {
        return path;
    }

    public final String getQuery() {
        return query;
    }

    public String getRespParams() {
        return respParams;
    }

    public final String getTargetService() {
        return targetService;
    }

    public final String getTraceId() {
        return traceId;
    }

    public final String getUsername() {
        return username;
    }
    
    public final long getStartTime() {
        return startTime;
    }

    public abstract MultiMap headers();

    public final boolean isAuthorized() {
        return authorized;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void out(long time, long duration, String method, String uri, int statusCode, String statusMessage,
            boolean success) {
        externalCall = externalCall == null ? duration : (externalCall + duration);
//        ioLog.out(traceId, time, duration, targetService, method, uri, func, params, statusCode, statusMessage, respParams,
//                success);
    }

    public final void out(long time, long duration, String method, String uri, String func, String params, int statusCode,
            String statusMessage, boolean success) {
        externalCall = externalCall == null ? duration : (externalCall + duration);
//        ioLog.out(traceId, time, duration, targetService, method, uri, func, params, statusCode, statusMessage, respParams,
//                success);
    }

    public final void out(long time, long duration, String method, String uri, String func, String params,
            String errorMessage, Throwable th) {
        externalCall = externalCall == null ? duration : (externalCall + duration);
//        ioLog.out(traceId, time, duration, targetService, method, uri, func, params, errorMessage, th);
    }

    public void out(long time, long duration, String method, String uri, String errorMessage, Throwable th) {
        externalCall = externalCall == null ? duration : (externalCall + duration);
//        ioLog.out(traceId, time, duration, targetService, method, uri, func, null, errorMessage, th);
    }

    public abstract ResponseContext response();

    public final void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    public final void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public final void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public final void setCharset(String charset) {
        this.charset = charset;
    }

    public final void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public final void setContext(String context) {
        this.context = context;
    }

    public final void setExternalCall(Long externalCall) {
        this.externalCall = externalCall;
    }

    public final void setFunc(String func) {
        this.func = func;
    }

    public final void setMethod(HttpMethod method) {
        this.method = method;
    }

    public final void setNode(String node) {
        this.node = node;
    }

    public final void setParams(String params) {
        this.params = params;
    }

    public final void setPath(String path) {
        this.path = path;
    }

    public void setRespParams(String respParams) {
        this.respParams = respParams;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public final void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public final void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public final void setUsername(String username) {
        this.username = username;
    }
    
    public final void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
    
    public final String getAuthorization() {
        return authorization;
    }
    
    public final void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}

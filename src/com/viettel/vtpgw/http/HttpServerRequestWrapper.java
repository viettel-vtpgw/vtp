package com.viettel.vtpgw.http;

import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;

import com.viettel.vtpgw.GatewayService;
import com.viettel.vtpgw.builtin.SoapGateway;
import com.viettel.vtpgw.context.RequestContext;

import com.viettel.vtpgw.model.Service;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpFrame;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import javax.net.ssl.SSLSession;

public class HttpServerRequestWrapper extends RequestContext implements HttpServerRequest {

    List<String> alertReceivers;
    long alertTimeout;
    private final HttpServerRequest delegate;
    private Long externalCall;
    
    private final AbstractHttpServerResponseWrapper response;
    private long startTime;
    private final Vertx vertx;

    public HttpServerRequestWrapper(HttpServerRequest request, Vertx vertx) {
        super(request.method(), request.query());
        this.vertx = vertx;
        delegate = request;
        startTime = System.currentTimeMillis();
        
        boolean noContent = false;
        response = noContent ? new NoContentHttpServerResponse(this) : new HttpServerResponseWrapper(this, delegate.response());
    }

    @Override
    public String absoluteURI() {
        return delegate.absoluteURI();
    }
    
    @Override
    public HttpServerRequest bodyHandler(Handler<Buffer> handler) {
        delegate.bodyHandler(handler);
        return this;
    }

    @Override
    public HttpConnection connection() {
        return delegate.connection();
    }

    @Override
    public HttpServerRequest customFrameHandler(Handler<HttpFrame> handler) {
        delegate.customFrameHandler(handler);
        return this;
    }

    @Override
    public HttpServerRequest endHandler(Handler<Void> handler) {
        delegate.endHandler(handler);
        return this;
    }

    @Override
    public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
        delegate.exceptionHandler(handler);
        return this;
    }

    public void finishTransaction() {
        int statusCode = response.getStatusCode();
        String statusMesage = response.getStatusMessage();
        if (statusCode >= 200 && statusCode < 300) {
//            qd168.startAction(appCode, startTime, username, clientIp, delegate.uri(), delegate.uri(), params, SoapGateway.class, System.currentTimeMillis() - startTime, "");
        } else {
//            qd168.error(GatewayService.class, response.getStatusMessage(), delegate.uri(), String.valueOf(statusCode));
        }
//        ioLog.in(traceId, startTime, System.currentTimeMillis() - startTime, externalCall, clientIp, username, targetService, delegate.method(), delegate.uri(), func, params, statusCode, statusMesage);
    }

    @Override
    public MultiMap formAttributes() {
        return delegate.formAttributes();
    }

    public HttpServerRequest getDelegate() {
        return delegate;
    }

    @Override
    public String getFormAttribute(String s) {
        return delegate.getFormAttribute(s);
    }

    @Override
    public String getHeader(CharSequence charSequence) {
        return delegate.getHeader(charSequence);
    }

    @Override
    public String getHeader(String s) {
        return delegate.getHeader(s);
    }

    @Override
    public String getParam(String s) {
        return delegate.getParam(s);
    }

    @Override
    public HttpServerRequest handler(Handler<Buffer> handler) {
        delegate.handler(handler);
        return this;
    }

    @Override
    public MultiMap headers() {
        return delegate.headers();
    }

    @Override
    public String host() {
        return delegate.host();
    }

    /*]]Vertx 3.3.x*/

    @Override
    public boolean isEnded() {
        return delegate.isEnded();
    }

    @Override
    public boolean isExpectMultipart() {
        return delegate.isExpectMultipart();
    }

    public boolean isSSL() {
        return delegate.isSSL();
    }

    @Override
    public SocketAddress localAddress() {
        return delegate.localAddress();
    }

    @Override
    public HttpMethod method() {
        return delegate.method();
    }

    @Override
    public NetSocket netSocket() {
        return delegate.netSocket();
    }

    public void out(long time, long duration, String method, String uri, int statusCode, String statusMessage, boolean success) {
        externalCall = externalCall == null ? duration : (externalCall + duration);
//        ioLog.out(traceId, time, duration, targetService, method, uri, func, null, statusCode, statusMessage, respParams, success);
    }

    public void out(long time, long duration, String method, String uri, String errorMessage, Throwable th) {
        externalCall = externalCall == null ? duration : (externalCall + duration);
//        ioLog.out(traceId, time, duration, targetService, method, uri, func, null, errorMessage, th);
    }

    @Override
    public MultiMap params() {
        return delegate.params();
    }

    @Override
    public String path() {
        return delegate.path();
    }

    @Override
    public HttpServerRequest pause() {
        delegate.pause();
        return this;
    }

    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        return delegate.peerCertificateChain();
    }

    @Override
    public String query() {
        return delegate.query();
    }

    @Override
    public String rawMethod() {
        return delegate.rawMethod();
    }

    @Override
    public SocketAddress remoteAddress() {
        return delegate.remoteAddress();
    }

    @Override
    public AbstractHttpServerResponseWrapper response() {
        return response;
    }

    @Override
    public HttpServerRequest resume() {
        delegate.resume();
        return this;
    }

    @Override
    public String scheme() {
        return delegate.scheme();
    }

    public HttpServerRequestWrapper setAlertReceivers(List<String> alertReceivers, long alertTimeout) {
        this.alertReceivers = alertReceivers;
        this.alertTimeout = alertTimeout;
        return this;
    }

    @Override
    public HttpServerRequest setExpectMultipart(boolean b) {
        delegate.setExpectMultipart(b);
        return this;
    }

    @Override
    public ServerWebSocket upgrade() {
        return delegate.upgrade();
    }

    @Override
    public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> handler) {
        delegate.uploadHandler(handler);
        return this;
    }

    @Override
    public String uri() {
        return delegate.uri();
    }

    @Override
    public HttpVersion version() {
        return delegate.version();
    }

    @Override
    public SSLSession sslSession() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Vertx getVertx(){
        return vertx;
    }
}

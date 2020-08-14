package com.viettel.vtpgw.http;

import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;

public class HttpServerResponseWrapper extends AbstractHttpServerResponseWrapper implements HttpServerResponse {

    private static final Logger LOG = LogManager.getLogger(HttpServerResponseWrapper.class);
    private HttpServerResponse delegate;
    HttpServerRequestWrapper request;

    public HttpServerResponseWrapper(HttpServerRequestWrapper request, HttpServerResponse delegate) {
        this.request = request;
        this.delegate = delegate;
    }

    @Override
    public HttpServerResponse bodyEndHandler(Handler<Void> handler) {
        delegate.bodyEndHandler(handler);
        return this;
    }

    @Override
    public long bytesWritten() {
        return delegate.bytesWritten();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean closed() {
        return delegate.closed();
    }

    @Override
    public HttpServerResponse closeHandler(Handler<Void> handler) {
        delegate.closeHandler(handler);
        return delegate;
    }

    @Override
    public HttpServerResponse drainHandler(Handler<Void> handler) {
        delegate.drainHandler(handler);
        return this;
    }

    @Override
    public void end() {
        delegate.end();
        this.request.finishTransaction();
        this.request = null;
    }

    @Override
    public void end(Buffer buff) {
        delegate.end(buff);
        this.request.finishTransaction();
        this.request = null;
    }

    @Override
    public void end(String buff) {
        delegate.end(buff);
        this.request.finishTransaction();
        this.request = null;
    }

    @Override
    public void end(String buff, String charset) {
        delegate.end(buff, charset);
        this.request.finishTransaction();
        this.request = null;
    }

    @Override
    public boolean ended() {
        return delegate.ended();
    }

    @Override
    public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
        delegate.exceptionHandler(handler);
        return this;
    }

    @Override
    public int getStatusCode() {
        return delegate.getStatusCode();
    }

    @Override
    public String getStatusMessage() {
        return delegate.getStatusMessage();
    }

    @Override
    public MultiMap headers() {
        return delegate.headers();
    }

    @Override
    public HttpServerResponse headersEndHandler(Handler<Void> handler) {
        delegate.headersEndHandler(handler);
        return this;
    }

    @Override
    public boolean headWritten() {
        return delegate.headWritten();
    }

    @Override
    public boolean isChunked() {
        return delegate.isChunked();
    }

    public void merge(Message<Buffer> msg) {
        MultiMap headers = msg.headers();
        String code = headers.get("-c");
        if (code != null) {
            setStatusCode(Integer.parseInt(code, 16));
        }
        MultiMap target = headers();
        String alertSubject = null;
        String issue = null;

        for (Entry<String, String> e : headers.entries()) {
            String key = e.getKey();
            switch (key) {
                case "-s":
                    alertSubject = e.getValue();
                    break;
                case "-pl":
                    issue = e.getValue();
                    break;
                case "-l":
                    try {
//                        level = Level.values()[Integer.parseInt(e.getValue(), 16)];
                    } catch (Exception ex) {
                        LOG.error("Invalid alert level", ex);
                    }
                    break;
                case "-c":
                    try {
                        setStatusCode(Integer.parseInt(e.getValue(), 16));
                    } catch (Exception ex) {
                        LOG.error("Invalid status code", ex);
                    }
                    break;
                case "-e":
                    try {
                        setExternalCall(new Long(e.getValue()));
                    } catch (Exception ex) {
                        LOG.error("Invalid external call duration", ex);
                    }
                    break;
                case "-f":
                    setFunc(e.getValue());
                    break;
                case "-ps":
                    setParams(e.getValue());
                    break;
                default:
                    if (!key.startsWith("-")) {
                        target.set(key, e.getValue());
                    }
            }
        }
        
        end(msg.body());
    }

    @Override
    public HttpServerResponseWrapper push(HttpMethod method, String path, Handler<AsyncResult<HttpServerResponse>> handler) {
        delegate.push(method, path, handler);
        return this;
    }

    @Override
    public HttpServerResponseWrapper push(HttpMethod method, String path, MultiMap headers,
            Handler<AsyncResult<HttpServerResponse>> handler) {
        delegate.push(method, path, headers, handler);
        return this;
    }

    /*Vertx 3.3.x [[*/
    @Override
    public HttpServerResponseWrapper push(HttpMethod method, String host, String path,
            Handler<AsyncResult<HttpServerResponse>> handler) {
        delegate.push(method, host, path, handler);
        return this;
    }

    @Override
    public HttpServerResponseWrapper push(HttpMethod method, String host, String path, MultiMap headers,
            Handler<AsyncResult<HttpServerResponse>> handler) {
        delegate.push(method, host, path, headers, handler);
        return this;
    }

    @Override
    public HttpServerResponseWrapper putHeader(CharSequence name, CharSequence value) {
        delegate.putHeader(name, value);
        return this;
    }

    @Override
    public HttpServerResponseWrapper putHeader(CharSequence name, Iterable<CharSequence> values) {
        delegate.putHeader(name, values);
        return this;
    }

    @Override
    public HttpServerResponseWrapper putHeader(String name, Iterable<String> values) {
        delegate.putHeader(name, values);
        return this;
    }

    @Override
    public HttpServerResponseWrapper putHeader(String name, String value) {
        delegate.putHeader(name, value);
        return this;
    }

    @Override
    public HttpServerResponseWrapper putTrailer(CharSequence name, CharSequence value) {
        delegate.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponseWrapper putTrailer(CharSequence name, Iterable<CharSequence> values) {
        delegate.putTrailer(name, values);
        return this;
    }

    @Override
    public HttpServerResponseWrapper putTrailer(String name, Iterable<String> values) {
        delegate.putTrailer(name, values);
        return this;
    }

    @Override
    public HttpServerResponseWrapper putTrailer(String name, String value) {
        delegate.putTrailer(name, value);
        return this;
    }

    @Override
    public void reset(long code) {
        delegate.reset(code);
    }

    @Override
    public HttpServerResponseWrapper sendFile(String filename, long offset, long length) {
        delegate.sendFile(filename, offset, length);
        return this;
    }

    @Override
    public HttpServerResponseWrapper sendFile(String filename, long offset, long length, Handler<AsyncResult<Void>> handler) {
        delegate.sendFile(filename, offset, length, handler);
        return this;
    }

    @Override
    public HttpServerResponseWrapper setChunked(boolean chunked) {
        delegate.setChunked(chunked);
        return this;
    }

    @Override
    protected void setExternalCall(Long externalCall) {
//        request.setExternalCall(externalCall);
    }

    @Override
    protected void setFunc(String func) {
//        request.setFunc(func);
    }

    @Override
    protected void setParams(String params) {
//        request.setParams(params);
    }

    @Override
    public HttpServerResponseWrapper setStatusCode(int code) {
        delegate.setStatusCode(code);
        return this;
    }

    @Override
    public HttpServerResponseWrapper setStatusMessage(String msg) {
        delegate.setStatusMessage(msg);
        return this;
    }

    @Override
    public HttpServerResponseWrapper setWriteQueueMaxSize(int size) {
        delegate.setWriteQueueMaxSize(size);
        return this;
    }

    @Override
    public int streamId() {
        return delegate.streamId();
    }

    @Override
    public MultiMap trailers() {
        return delegate.trailers();
    }

    @Override
    public HttpServerResponseWrapper write(Buffer buff) {
        delegate.write(buff);
        return this;
    }

    @Override
    public HttpServerResponseWrapper write(String buff) {
        delegate.write(buff);
        return this;
    }

    @Override
    public HttpServerResponseWrapper write(String buff, String charset) {
        delegate.write(buff, charset);
        return this;
    }

    @Override
    public HttpServerResponseWrapper writeContinue() {
        delegate.writeContinue();
        return this;
    }

    @Override
    public HttpServerResponseWrapper writeCustomFrame(int type, int flags, Buffer payload) {
        delegate.writeCustomFrame(type, flags, payload);
        return this;
    }

    /*]]Vertx 3.3.x*/
    @Override
    public boolean writeQueueFull() {
        return delegate.writeQueueFull();
    }

    @Override
    public HttpServerResponse endHandler(Handler<Void> hndlr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

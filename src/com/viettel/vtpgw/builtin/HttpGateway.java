package com.viettel.vtpgw.builtin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.viettel.vtpgw.model.Endpoint;

import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class HttpGateway {

    private static final Logger LOG = LogManager.getLogger(HttpGateway.class);
    private static final Set<String> REMOVED_REQ_HEADERS = new HashSet<String>(
            Arrays.asList("host", "authorization", "content-encoding", "accept-encoding", "x-forwarded-for"));
    private static final Set<String> REMOVED_RESP_HEADERS = new HashSet<String>(
            Arrays.asList("content-encoding", "transfer-encoding"));

    public static void postResponse(HttpClientResponse source, HttpServerResponse target) {
        target.setStatusCode(source.statusCode()).setStatusMessage(source.statusMessage());
        source.headers().forEach(entry -> {
            String key = entry.getKey().toLowerCase();
            if (!REMOVED_RESP_HEADERS.contains(key.toLowerCase())) {
                target.putHeader(key, entry.getValue());
            }
        });
    }

    /**
     * Function: thuc hien sao chep thong tin header tu http request nguon sang
     * http request dich source: http request nguon target http request dich
     * endpoint: chua mot so thong tin cua may client (xac thuc, ip host,..)
     */
    public static void preRequest(HttpServerRequest source, HttpClientRequest target, Endpoint endpoint) {
        // thuc hien the mot so headers co ban
        source.headers().forEach(entry -> {
            String key = entry.getKey().toLowerCase();
            if (!REMOVED_REQ_HEADERS.contains(key.toLowerCase())) {
                target.putHeader(key, entry.getValue());
            }
        });

        // thuc hien thay doi mot so headers cua proxy
        //
        
        String authorization = endpoint.authorization();        
        if (authorization != null) {
            target.putHeader(HttpHeaders.AUTHORIZATION, authorization);
        }

        String host = endpoint.host();
        if (host != null) {
            target.putHeader("Host", host);
        }
    }
}

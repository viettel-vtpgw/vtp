package com.viettel.vtpgw.model.impl;

import java.util.List;
import java.util.Map;

import com.viettel.vtpgw.model.Endpoint;
import com.viettel.vtpgw.model.Service;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class DefaultService implements Service {

    List<String> alertReceivers;

    private long alertTimeout;

    private Endpoint[] endpoints;

    private String id;
    private int lastIndex = -1;
    private String module;
    private String name;
    Map<String, String> params;
    private Long rateLimitCapacity;
    private Long rateLimitPeriod;
    Map<String, String> respParams;
    Endpoint sandboxEndpoint;
    private long standardDuration;
    private long timeout;
    private long version;

    @Override
    public List<String> alertReceivers() {
        return alertReceivers;
    }

    public long alertTimeout() {
        return alertTimeout;
    }

    @Override
    public Endpoint endpoint() {
        int index = lastIndex = (lastIndex + 1) % endpoints.length;
        for (int i = 0; i < endpoints.length; i++) {
            Endpoint rs = endpoints[index];
            if (rs.activated()) {
                return rs;
            }
            index = (index + 1) % endpoints.length;
        }
        return endpoints[index];
    }

    public Endpoint[] getEndpoints() {
        return endpoints;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String module() {
        return module;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Map<String, String> params() {
        return params;
    }

    @Override
    public Long rateLimitCapacity() {
        return rateLimitCapacity;
    }

    @Override
    public Long rateLimitPeriod() {
        return rateLimitPeriod;
    }

    @Override
    public Map<String, String> respParams() {
        return respParams;
    }

    @Override
    public Endpoint sandboxEndpoint() {
        return sandboxEndpoint;
    }

    public void setAlertReceivers(List<String> alertReceivers) {
        this.alertReceivers = alertReceivers;
    }

    public void setAlertTimeout(long alertTimeout) {
        this.alertTimeout = alertTimeout;
    }

    public void setEndpoints(Endpoint[] endpoints) {
        this.endpoints = endpoints;
        if (endpoints != null && endpoints.length > 0) {
            this.sandboxEndpoint = endpoints[0];
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParams(Map<String, String> paramPatterns) {
        if (paramPatterns != null && !paramPatterns.isEmpty()) {
            this.params = paramPatterns;
        } else {
            this.params = null;
        }
    }

    public void setRateLimitCapacity(Long rateLimitCapacity) {
        this.rateLimitCapacity = rateLimitCapacity;
    }

    public void setRateLimitPeriod(Long rateLimitPeriod) {
        this.rateLimitPeriod = rateLimitPeriod;
    }

    public void setRespParams(Map<String, String> respParams) {
        if (respParams != null && !respParams.isEmpty()) {
            this.respParams = respParams;
        } else {
            this.respParams = null;
        }
    }

    public void setSandboxEndpoint(Endpoint sandboxEndpoint) {
        this.sandboxEndpoint = sandboxEndpoint;
    }

    public void setStandardDuration(long standardDuration) {
        this.standardDuration = standardDuration;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public long standardDuration() {
        return standardDuration;
    }

    public long timeout() {
        return timeout;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject().put("name", name)
                .put("module", module)
                .put("standardDuration", standardDuration);
        if (endpoints != null) {
            JsonArray array = new JsonArray();
            for (Endpoint endpoint : endpoints) {
                array.add(endpoint.url());
            }
            json.put("endpoints", array);
        }
        if (params != null) {
            json.put("params", new JsonObject((Map) params));
        }
        if (respParams != null) {
            json.put("respParams", new JsonObject((Map) respParams));
        }
        if (alertReceivers != null) {
            json.put("alertReceivers", new JsonArray(alertReceivers)).put("alertTimeout", alertTimeout);
        }
        return json;
    }
}

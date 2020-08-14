package com.viettel.vtpgw.model.impl;

import com.viettel.vtpgw.model.AppPermission;
import com.viettel.vtpgw.model.ClientApp;
import com.viettel.vtpgw.model.Service;
import com.viettel.vtpgw.security.ClientVerifier;
import com.viettel.vtpgw.security.impl.MultiValueVerifier;
import com.viettel.vtpgw.security.impl.SingleValueVerifier;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class DefaultAppPermission implements AppPermission, ClientVerifier<String> {

  private boolean acceptMethod;
  private ClientApp client;

  private boolean debug;

  private String id;

  private ClientVerifier<String> ipVerifier;
  private ClientVerifier<String> methodVerifier;
  boolean noContent;

  private Long rateLimitCapacity;
  private Long rateLimitPeriod;
  private boolean sandbox;
  Service service;

  long timeout;

  @Override
  public boolean accept(String ip) {
    return true;
  }

  @Override
  public boolean acceptIp(String ip) {
    return ipVerifier.accept(ip);
  }

  @Override
  public boolean acceptMethod() {
    return acceptMethod;
  }

  @Override
  public boolean acceptMethod(String method) {
    return (!acceptMethod) || methodVerifier.accept(method);
  }

  @Override
  public ClientApp client() {
    return client;
  }

  @Override
  public boolean debug() {
    return debug;
  }

  public String id() {
    return id;
  }

  @Override
  public boolean noContent() {
    return noContent;
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
  public final boolean sandbox() {
    return sandbox;
  }

  @Override
  public Service service() {
    return service;
  }

  public void setClient(ClientApp client) {
    this.client = client;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setIps(JsonArray ips) {
    if (ips == null || ips.isEmpty())
      ipVerifier = this;
    else if (ips.size() >= 1) {
      MultiValueVerifier<String> mv = new MultiValueVerifier<>(ips.size());
      ips.forEach(ip -> {
        mv.add((String) ip);
      });
      ipVerifier = mv;
    } else {
      ipVerifier = new SingleValueVerifier<>(ips.getString(0));
    }
  }

  public void setMethods(JsonArray methods) {
    if (methods == null || methods.isEmpty()) {
      acceptMethod = false;
      methodVerifier = this;
    } else if (methods.size() >= 1) {
      MultiValueVerifier<String> mv = new MultiValueVerifier<>(methods.size());
      methods.forEach(method -> {
        mv.add((String) method);
      });
      acceptMethod = true;
      methodVerifier = mv;
    } else {
      acceptMethod = true;
      methodVerifier = new SingleValueVerifier<>(methods.getString(0));
    }
  }

  public void setNoContent(boolean noContent) {
    this.noContent = noContent;
  }

  public void setRateLimitCapacity(Long rateLimitCapacity) {
    this.rateLimitCapacity = rateLimitCapacity;
  }

  public void setRateLimitPeriod(Long rateLimitPeriod) {
    this.rateLimitPeriod = rateLimitPeriod;
  }

  public final void setSandbox(boolean sandbox) {
    this.sandbox = sandbox;
  }

  public void setService(Service service) {
    this.service = service;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public long timeout() {
    return timeout;
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put("id", id()).put("rateLimitCapacity", rateLimitCapacity())
        .put("rateLimitPeriod", rateLimitPeriod()).put("sandbox", sandbox()).put("debug", debug())
        .put("service", service.toJson()).put("app", client.toJson());
  }

}

package com.viettel.vtpgw.model.impl;

import java.util.List;

import com.viettel.vtpgw.model.ClientApp;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class DefaultClientApp implements ClientApp {
  List<String> alertReceivers;
  long alertTimeout;

  private String token;

  private String username;

  @Override
  public List<String> alertReceivers() {
    return alertReceivers;
  }

  @Override
  public long alertTimeout() {
    return alertTimeout;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj instanceof ClientApp) {
      return username.equals(((ClientApp) obj).username());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return username.hashCode();
  }

  public void setAlertReceivers(List<String> alertReceivers) {
    this.alertReceivers = alertReceivers;
  }

  public void setAlertTimeout(long alertTimeout) {
    this.alertTimeout = alertTimeout;
  }

  public void setToken(String password) {
    this.token = password;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject().put("username", username);
    if (alertReceivers != null) {
      json.put("alertReceivers", new JsonArray(alertReceivers)).put("alertTimeout", alertTimeout);
    }
    return json;
  }

  @Override
  public String token() {
    return token;
  }

  @Override
  public String username() {
    return username;
  }
}

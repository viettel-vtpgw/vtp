/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.db.entity;

import io.vertx.core.shareddata.Shareable;
import java.util.List;

/**
 *
 * @author anPV
 */
public class SettingShareable implements Shareable {

    private Long permissionId;
    private String serviceName;
    private Long serviceId;
    private String appId;
    private String token;
    private List<String> listRrUrl;
    private String module;
    private String method;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getListRrUrl() {
        return listRrUrl;
    }

    public void setListRrUrl(List<String> listRrUrl) {
        this.listRrUrl = listRrUrl;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public SettingShareable(VtpgwPermission permission, VtpgwService service, VtpgwApp app, List<String> listRrUrl) {
        this.permissionId = permission.getId();
        this.serviceName = service.getName();
        this.serviceId = service.getId();
        this.appId = app.getAppId();
        this.token = app.getToken();
        this.listRrUrl = listRrUrl;
        this.method = permission.getMethods();
        this.module = service.getModule();
    }
}

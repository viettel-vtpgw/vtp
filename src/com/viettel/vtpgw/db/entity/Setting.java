/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.db.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author anPV
 */
@Entity
@Table(name = "setting")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Setting.findAll", query = "SELECT s FROM Setting s")
    , @NamedQuery(name = "Setting.findById", query = "SELECT s FROM Setting s WHERE s.id = :id")
    , @NamedQuery(name = "Setting.findByWebService", query = "SELECT s FROM Setting s WHERE s.webService = :webService")
    , @NamedQuery(name = "Setting.findByUrl", query = "SELECT s FROM Setting s WHERE s.url = :url")
    , @NamedQuery(name = "Setting.findByType", query = "SELECT s FROM Setting s WHERE s.type = :type")
    , @NamedQuery(name = "Setting.findByOperation", query = "SELECT s FROM Setting s WHERE s.operation = :operation")
    , @NamedQuery(name = "Setting.findByParam", query = "SELECT s FROM Setting s WHERE s.param = :param")
    , @NamedQuery(name = "Setting.findByParamValue", query = "SELECT s FROM Setting s WHERE s.paramValue = :paramValue")
    , @NamedQuery(name = "Setting.findByExpire", query = "SELECT s FROM Setting s WHERE s.expire = :expire")
    , @NamedQuery(name = "Setting.findByStatus", query = "SELECT s FROM Setting s WHERE s.status = :status")
    , @NamedQuery(name = "Setting.findByObjectId", query = "SELECT s FROM Setting s WHERE s.objectId = :objectId")})
public class Setting implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "web_service")
    private String webService;
    @Basic(optional = false)
    @Column(name = "url")
    private String url;
    @Basic(optional = false)
    @Column(name = "type")
    private int type;
    @Column(name = "operation")
    private String operation;
    @Column(name = "param")
    private String param;
    @Column(name = "param_value")
    private String paramValue;
    @Column(name = "expire")
    private Integer expire;
    @Column(name = "status")
    private Integer status;
    @Column(name = "object_id")
    private String objectId;

    public Setting() {
    }

    public Setting(Integer id) {
        this.id = id;
    }

    public Setting(Integer id, String webService, String url, int type) {
        this.id = id;
        this.webService = webService;
        this.url = url;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWebService() {
        return webService;
    }

    public void setWebService(String webService) {
        this.webService = webService;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public Integer getExpire() {
        return expire;
    }

    public void setExpire(Integer expire) {
        this.expire = expire;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Setting)) {
            return false;
        }
        Setting other = (Setting) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.viettel.vtpgw.db.entity.Setting[ id=" + id + " ]";
    }
    
}

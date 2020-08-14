/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.db.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Dell
 */
@Entity
@Table(name = "vtpgw_permission")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VtpgwPermission.findAll", query = "SELECT v FROM VtpgwPermission v")
    , @NamedQuery(name = "VtpgwPermission.findById", query = "SELECT v FROM VtpgwPermission v WHERE v.id = :id")
    , @NamedQuery(name = "VtpgwPermission.findByCreatedBy", query = "SELECT v FROM VtpgwPermission v WHERE v.createdBy = :createdBy")
    , @NamedQuery(name = "VtpgwPermission.findByCreated", query = "SELECT v FROM VtpgwPermission v WHERE v.created = :created")
    , @NamedQuery(name = "VtpgwPermission.findByUpdatedBy", query = "SELECT v FROM VtpgwPermission v WHERE v.updatedBy = :updatedBy")
    , @NamedQuery(name = "VtpgwPermission.findByUpdated", query = "SELECT v FROM VtpgwPermission v WHERE v.updated = :updated")
    , @NamedQuery(name = "VtpgwPermission.findByActivated", query = "SELECT v FROM VtpgwPermission v WHERE v.activated = :activated")
    , @NamedQuery(name = "VtpgwPermission.findByCapacity", query = "SELECT v FROM VtpgwPermission v WHERE v.capacity = :capacity")
    , @NamedQuery(name = "VtpgwPermission.findByDebug", query = "SELECT v FROM VtpgwPermission v WHERE v.debug = :debug")
    , @NamedQuery(name = "VtpgwPermission.findByIps", query = "SELECT v FROM VtpgwPermission v WHERE v.ips = :ips")
    , @NamedQuery(name = "VtpgwPermission.findByMethods", query = "SELECT v FROM VtpgwPermission v WHERE v.methods = :methods")
    , @NamedQuery(name = "VtpgwPermission.findByNoContent", query = "SELECT v FROM VtpgwPermission v WHERE v.noContent = :noContent")
    , @NamedQuery(name = "VtpgwPermission.findByPeriod", query = "SELECT v FROM VtpgwPermission v WHERE v.period = :period")
    , @NamedQuery(name = "VtpgwPermission.findByPermissionId", query = "SELECT v FROM VtpgwPermission v WHERE v.permissionId = :permissionId")
    , @NamedQuery(name = "VtpgwPermission.findBySandBox", query = "SELECT v FROM VtpgwPermission v WHERE v.sandBox = :sandBox")
    , @NamedQuery(name = "VtpgwPermission.findByServiceId", query = "SELECT v FROM VtpgwPermission v WHERE v.serviceId = :serviceId")})
public class VtpgwPermission implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "createdBy")
    private String createdBy;
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name = "updatedBy")
    private String updatedBy;
    @Column(name = "updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;
    @Column(name = "activated")
    private BigInteger activated;
    @Column(name = "capacity")
    private BigInteger capacity;
    @Column(name = "debug")
    private Integer debug;
    @Column(name = "ips")
    private String ips;
    @Column(name = "methods")
    private String methods;
    @Column(name = "noContent")
    private Integer noContent;
    @Column(name = "period")
    private BigInteger period;
    @Column(name = "permissionId")
    private String permissionId;
    @Column(name = "sandBox")
    private Integer sandBox;
    @Basic(optional = false)
    @Column(name = "serviceId")
    private String serviceId;
    @JoinColumn(name = "appId", referencedColumnName = "appId")
    @ManyToOne(optional = false)
    private VtpgwApp appId;

    public VtpgwPermission() {
    }

    public VtpgwPermission(Long id) {
        this.id = id;
    }

    public VtpgwPermission(Long id, String serviceId) {
        this.id = id;
        this.serviceId = serviceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public BigInteger getActivated() {
        return activated;
    }

    public void setActivated(BigInteger activated) {
        this.activated = activated;
    }

    public BigInteger getCapacity() {
        return capacity;
    }

    public void setCapacity(BigInteger capacity) {
        this.capacity = capacity;
    }

    public Integer getDebug() {
        return debug;
    }

    public void setDebug(Integer debug) {
        this.debug = debug;
    }

    public String getIps() {
        return ips;
    }

    public void setIps(String ips) {
        this.ips = ips;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public Integer getNoContent() {
        return noContent;
    }

    public void setNoContent(Integer noContent) {
        this.noContent = noContent;
    }

    public BigInteger getPeriod() {
        return period;
    }

    public void setPeriod(BigInteger period) {
        this.period = period;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public Integer getSandBox() {
        return sandBox;
    }

    public void setSandBox(Integer sandBox) {
        this.sandBox = sandBox;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public VtpgwApp getAppId() {
        return appId;
    }

    public void setAppId(VtpgwApp appId) {
        this.appId = appId;
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
        if (!(object instanceof VtpgwPermission)) {
            return false;
        }
        VtpgwPermission other = (VtpgwPermission) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.viettel.vtpgw.db.entity.VtpgwPermission[ id=" + id + " ]";
    }
    
}

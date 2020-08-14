/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.db.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Dell
 */
@Entity
@Table(name = "vtpgw_app")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VtpgwApp.findAll", query = "SELECT v FROM VtpgwApp v")
    , @NamedQuery(name = "VtpgwApp.findById", query = "SELECT v FROM VtpgwApp v WHERE v.id = :id")
    , @NamedQuery(name = "VtpgwApp.findByCreatedBy", query = "SELECT v FROM VtpgwApp v WHERE v.createdBy = :createdBy")
    , @NamedQuery(name = "VtpgwApp.findByCreated", query = "SELECT v FROM VtpgwApp v WHERE v.created = :created")
    , @NamedQuery(name = "VtpgwApp.findByUpdatedBy", query = "SELECT v FROM VtpgwApp v WHERE v.updatedBy = :updatedBy")
    , @NamedQuery(name = "VtpgwApp.findByUpdated", query = "SELECT v FROM VtpgwApp v WHERE v.updated = :updated")
    , @NamedQuery(name = "VtpgwApp.findByAppId", query = "SELECT v FROM VtpgwApp v WHERE v.appId = :appId")
    , @NamedQuery(name = "VtpgwApp.findByApplicationId", query = "SELECT v FROM VtpgwApp v WHERE v.applicationId = :applicationId")
    , @NamedQuery(name = "VtpgwApp.findByContact", query = "SELECT v FROM VtpgwApp v WHERE v.contact = :contact")
    , @NamedQuery(name = "VtpgwApp.findByStatus", query = "SELECT v FROM VtpgwApp v WHERE v.status = :status")
    , @NamedQuery(name = "VtpgwApp.findByToken", query = "SELECT v FROM VtpgwApp v WHERE v.token = :token")})
public class VtpgwApp implements Serializable {

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
    @Column(name = "appId")
    private String appId;
    @Column(name = "applicationId")
    private String applicationId;
    @Column(name = "contact")
    private String contact;
    @Column(name = "status")
    private Integer status;
    @Column(name = "token")
    private String token;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "appId")
    private Collection<VtpgwPermission> vtpgwPermissionCollection;

    public VtpgwApp() {
    }

    public VtpgwApp(Long id) {
        this.id = id;
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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @XmlTransient
    public Collection<VtpgwPermission> getVtpgwPermissionCollection() {
        return vtpgwPermissionCollection;
    }

    public void setVtpgwPermissionCollection(Collection<VtpgwPermission> vtpgwPermissionCollection) {
        this.vtpgwPermissionCollection = vtpgwPermissionCollection;
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
        if (!(object instanceof VtpgwApp)) {
            return false;
        }
        VtpgwApp other = (VtpgwApp) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.viettel.vtpgw.db.entity.VtpgwApp[ id=" + id + " ]";
    }
    
}

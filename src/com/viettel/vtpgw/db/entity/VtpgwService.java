/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.db.entity;

import java.io.Serializable;
import java.math.BigInteger;
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
@Table(name = "vtpgw_service")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VtpgwService.findAll", query = "SELECT v FROM VtpgwService v")
    , @NamedQuery(name = "VtpgwService.findById", query = "SELECT v FROM VtpgwService v WHERE v.id = :id")
    , @NamedQuery(name = "VtpgwService.findByCreatedBy", query = "SELECT v FROM VtpgwService v WHERE v.createdBy = :createdBy")
    , @NamedQuery(name = "VtpgwService.findByCreated", query = "SELECT v FROM VtpgwService v WHERE v.created = :created")
    , @NamedQuery(name = "VtpgwService.findByUpdatedBy", query = "SELECT v FROM VtpgwService v WHERE v.updatedBy = :updatedBy")
    , @NamedQuery(name = "VtpgwService.findByUpdated", query = "SELECT v FROM VtpgwService v WHERE v.updated = :updated")
    , @NamedQuery(name = "VtpgwService.findByCapacity", query = "SELECT v FROM VtpgwService v WHERE v.capacity = :capacity")
    , @NamedQuery(name = "VtpgwService.findByConnectTimeout", query = "SELECT v FROM VtpgwService v WHERE v.connectTimeout = :connectTimeout")
    , @NamedQuery(name = "VtpgwService.findByContact", query = "SELECT v FROM VtpgwService v WHERE v.contact = :contact")
    , @NamedQuery(name = "VtpgwService.findByDescription", query = "SELECT v FROM VtpgwService v WHERE v.description = :description")
    , @NamedQuery(name = "VtpgwService.findByIdleTimeout", query = "SELECT v FROM VtpgwService v WHERE v.idleTimeout = :idleTimeout")
    , @NamedQuery(name = "VtpgwService.findByModule", query = "SELECT v FROM VtpgwService v WHERE v.module = :module")
    , @NamedQuery(name = "VtpgwService.findByName", query = "SELECT v FROM VtpgwService v WHERE v.name = :name")
    , @NamedQuery(name = "VtpgwService.findByPeriod", query = "SELECT v FROM VtpgwService v WHERE v.period = :period")
    , @NamedQuery(name = "VtpgwService.findByReportInterval", query = "SELECT v FROM VtpgwService v WHERE v.reportInterval = :reportInterval")
    , @NamedQuery(name = "VtpgwService.findBySandboxEndpoint", query = "SELECT v FROM VtpgwService v WHERE v.sandboxEndpoint = :sandboxEndpoint")
    , @NamedQuery(name = "VtpgwService.findByServiceId", query = "SELECT v FROM VtpgwService v WHERE v.serviceId = :serviceId")
    , @NamedQuery(name = "VtpgwService.findByStandardDuration", query = "SELECT v FROM VtpgwService v WHERE v.standardDuration = :standardDuration")
    , @NamedQuery(name = "VtpgwService.findByStatus", query = "SELECT v FROM VtpgwService v WHERE v.status = :status")})
public class VtpgwService implements Serializable {

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
    @Column(name = "capacity")
    private BigInteger capacity;
    @Column(name = "connectTimeout")
    private BigInteger connectTimeout;
    @Column(name = "contact")
    private String contact;
    @Column(name = "description")
    private String description;
    @Column(name = "idleTimeout")
    private BigInteger idleTimeout;
    @Column(name = "module")
    private String module;
    @Column(name = "name")
    private String name;
    @Column(name = "period")
    private BigInteger period;
    @Column(name = "reportInterval")
    private Integer reportInterval;
    @Column(name = "sandboxEndpoint")
    private String sandboxEndpoint;
    @Column(name = "serviceId")
    private String serviceId;
    @Column(name = "standardDuration")
    private BigInteger standardDuration;
    @Column(name = "status")
    private Integer status;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "serviceId")
    private Collection<VtpgwNodes> vtpgwNodesCollection;

    public VtpgwService() {
    }

    public VtpgwService(Long id) {
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

    public BigInteger getCapacity() {
        return capacity;
    }

    public void setCapacity(BigInteger capacity) {
        this.capacity = capacity;
    }

    public BigInteger getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(BigInteger connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigInteger getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(BigInteger idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getPeriod() {
        return period;
    }

    public void setPeriod(BigInteger period) {
        this.period = period;
    }

    public Integer getReportInterval() {
        return reportInterval;
    }

    public void setReportInterval(Integer reportInterval) {
        this.reportInterval = reportInterval;
    }

    public String getSandboxEndpoint() {
        return sandboxEndpoint;
    }

    public void setSandboxEndpoint(String sandboxEndpoint) {
        this.sandboxEndpoint = sandboxEndpoint;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public BigInteger getStandardDuration() {
        return standardDuration;
    }

    public void setStandardDuration(BigInteger standardDuration) {
        this.standardDuration = standardDuration;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @XmlTransient
    public Collection<VtpgwNodes> getVtpgwNodesCollection() {
        return vtpgwNodesCollection;
    }

    public void setVtpgwNodesCollection(Collection<VtpgwNodes> vtpgwNodesCollection) {
        this.vtpgwNodesCollection = vtpgwNodesCollection;
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
        if (!(object instanceof VtpgwService)) {
            return false;
        }
        VtpgwService other = (VtpgwService) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.viettel.vtpgw.db.entity.VtpgwService[ id=" + id + " ]";
    }
    
}

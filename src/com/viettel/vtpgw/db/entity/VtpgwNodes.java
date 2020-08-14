/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.db.entity;

import java.io.Serializable;
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
@Table(name = "vtpgw_nodes")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VtpgwNodes.findAll", query = "SELECT v FROM VtpgwNodes v")
    , @NamedQuery(name = "VtpgwNodes.findById", query = "SELECT v FROM VtpgwNodes v WHERE v.id = :id")
    , @NamedQuery(name = "VtpgwNodes.findByCreatedBy", query = "SELECT v FROM VtpgwNodes v WHERE v.createdBy = :createdBy")
    , @NamedQuery(name = "VtpgwNodes.findByCreated", query = "SELECT v FROM VtpgwNodes v WHERE v.created = :created")
    , @NamedQuery(name = "VtpgwNodes.findByUpdatedBy", query = "SELECT v FROM VtpgwNodes v WHERE v.updatedBy = :updatedBy")
    , @NamedQuery(name = "VtpgwNodes.findByUpdated", query = "SELECT v FROM VtpgwNodes v WHERE v.updated = :updated")
    , @NamedQuery(name = "VtpgwNodes.findByCheckUrl", query = "SELECT v FROM VtpgwNodes v WHERE v.checkUrl = :checkUrl")
    , @NamedQuery(name = "VtpgwNodes.findByStatus", query = "SELECT v FROM VtpgwNodes v WHERE v.status = :status")
    , @NamedQuery(name = "VtpgwNodes.findByUrl", query = "SELECT v FROM VtpgwNodes v WHERE v.url = :url")})
public class VtpgwNodes implements Serializable {

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
    @Column(name = "checkUrl")
    private String checkUrl;
    @Column(name = "status")
    private Integer status;
    @Column(name = "url")
    private String url;
    @JoinColumn(name = "serviceId", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private VtpgwService serviceId;

    public VtpgwNodes() {
    }

    public VtpgwNodes(Long id) {
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

    public String getCheckUrl() {
        return checkUrl;
    }

    public void setCheckUrl(String checkUrl) {
        this.checkUrl = checkUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public VtpgwService getServiceId() {
        return serviceId;
    }

    public void setServiceId(VtpgwService serviceId) {
        this.serviceId = serviceId;
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
        if (!(object instanceof VtpgwNodes)) {
            return false;
        }
        VtpgwNodes other = (VtpgwNodes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.viettel.vtpgw.db.entity.VtpgwNodes[ id=" + id + " ]";
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.db.session;

import com.viettel.vtpgw.db.entity.VtpgwPermission;
import com.viettel.vtpgw.db.entity.VtpgwService;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author anPV
 */
public class SettingSession extends BaseSession {

    public List<VtpgwPermission> findAll(Date idTimer) {
        logger.info("SettingSession: findAll() - Start - id: {}", idTimer);
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("VtpgwPermission.findAll");
        List<VtpgwPermission> results = null;
        try {
            results = q.getResultList();
        } catch (Exception ex) {
            logger.error("findAll(): - id: {}", ex, idTimer);
        }
        logger.info("SettingSession: findAll() - End - id: {}", idTimer);
        return results;
    }
    
    public List<VtpgwService> findByServiceId(String serviceId, Date idTimer) {
        logger.info("SettingSession: findByServiceId() - Start - id: {}", idTimer);
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("VtpgwService.findByServiceId");
        q.setParameter("serviceId", serviceId);
        List<VtpgwService> results = null;
        try {
            results = q.getResultList();
        } catch (Exception ex) {
            logger.error("findByServiceId(): - id: {}", ex, idTimer);
        }
        logger.info("SettingSession: findByServiceId() - End - id: {}", idTimer);
        return results;
    }
    
    public Date findNewestUpdate(Date idTimer) {
        logger.info("SettingSession: findNewestUpdate() - Start - id: {}", idTimer);
        EntityManager em = emf.createEntityManager();
        Query q = em.createNativeQuery("SELECT updated from vtpgw_permission ORDER BY updated desc limit 1");
        Date results = null;
        try {
            results = (Date) q.getSingleResult();
        } catch (Exception ex) {
            logger.error("findNewestUpdate(): - id: {}", ex, idTimer);
        }
        logger.info("SettingSession: findNewestUpdate() - End - id: {}", idTimer);
        return results;
    }

}

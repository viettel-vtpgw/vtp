/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.db.session;

import com.viettel.vtpgw.db.util.Util;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author anPV
 */
public class BaseSession {
    protected static EntityManagerFactory emf;
    static final Logger logger = LogManager.getLogger(BaseSession.class.getName());
    static{
        try{            
            String unit = "TestVertxPU";
            emf = Persistence.createEntityManagerFactory(unit);
            
            logger.info("Persistence Unit: " + unit);
        } catch (Exception e) {
            logger.error("BaseSession: ", e);
        }        
    }
    
    public BaseSession(){
    }
    
    public boolean save(Object object){
        boolean b = true;
        EntityTransaction tx = null;
        EntityManager em = emf.createEntityManager();
        
        try{
            tx = em.getTransaction();
            if(!tx.isActive()){
                tx.begin();
            }
            em.persist(object);
            tx.commit();
        }
        catch(Exception ex){
            b = false;
            //ex.printStackTrace();
            logger.error("save()", ex);
            try{
                 if(tx != null && tx.isActive()){
                     tx.rollback();
                 }
            } catch(Exception e){
                //e.printStackTrace();
                logger.error("save()", e);
            }
        }
        finally{
            em.close();
        }
        
        return b;
    }
    
    public boolean edit(Object object){
        boolean b = true;
        EntityTransaction tx = null;
        EntityManager em = emf.createEntityManager();
        
        try{
            tx = em.getTransaction();
            if(!tx.isActive()){
                tx.begin();
            }
            em.merge(object);
            tx.commit();
        }
        catch(Exception ex){
           b = false;
           //ex.printStackTrace();
           logger.error("edit()", ex);
           try{
                if(tx != null && tx.isActive()){
                    tx.rollback();
                }
           } catch(Exception e){
               //e.printStackTrace();
               logger.error("edit()", e);
           }
        }
        finally{
            em.close();
        }
        
        return b;
    }
        
    public boolean remove(Object object){
        boolean b = true;
        EntityTransaction tx = null;
        EntityManager em = emf.createEntityManager();
        
        try{
            tx = em.getTransaction();
            if(!tx.isActive()){
                tx.begin();
            }
            em.remove(object);
            tx.commit();
        }
        catch(Exception ex){
           b = false;
           //ex.printStackTrace();
           logger.error("", ex);
           try{
                if(tx != null && tx.isActive()){
                    tx.rollback();
                }
           } catch(Exception e){
               //e.printStackTrace();
               logger.error("", e);
           }
        }
        finally{
            em.close();
        }
        
        return b;
    }
    
    public boolean saveBulk(List<Object> list){
        boolean b = true;
        EntityTransaction tx = null;
        EntityManager emBulk = emf.createEntityManager();
        
        try{
            tx = emBulk.getTransaction();
            if(!tx.isActive()){
                tx.begin();
            }
            for(Object obj: list){
                emBulk.persist(obj);
            }
            tx.commit();
        }
        catch(Exception ex){            
            b = false;      
            if(ex instanceof MySQLIntegrityConstraintViolationException){
                logger.error("saveBulk(): " + ex.getMessage());
            }
            else{
                logger.error("saveBulk()" +  ex.getMessage());
            }
            
            try{
                 if(tx != null && tx.isActive()){
                     tx.rollback();
                 }
            } catch(Exception e){                
                logger.error("saveBulk() - rollback: ", e);
            }
        }
        finally{
            emBulk.close();
        }
        
        return b;
    }
    
    public int saveBulk2(List<Object> list){
        boolean b = saveBulk(list);
        if(b){
            return list.size();
        }
        
        // trong truong hop khong luu lo thanh cong
        // co gang luu tung doi tuong 1
        logger.info("try save one by one...");
        int ncount = 0;
        
        EntityTransaction tx = null;
        EntityManager emBulk = emf.createEntityManager();
        try{
            for(Object obj: list){
                try{
                    tx = emBulk.getTransaction();
                    if(!tx.isActive()){
                        tx.begin();
                    }            
                    emBulk.persist(obj);
                    tx.commit();
                    ncount++;
                }
                catch(Exception ex){            
                    b = false;      
                    if(ex instanceof MySQLIntegrityConstraintViolationException){
                        logger.error("saveBulk2(): " + ex.getMessage());
                    }
                    else{
                        logger.error("saveBulk2()" +  ex.getMessage());
                    }

                    try{
                         if(tx != null && tx.isActive()){
                             tx.rollback();
                         }
                    } catch(Exception e){                
                        logger.error("saveBulk2() - rollback: ", e);
                    }
                }
            }
        }
        finally{
            emBulk.close();
        }
        
        return ncount;
    }
    
    public boolean editBulk(List<Object> list){
        boolean b = true;
        EntityTransaction tx = null;
        EntityManager emBulk = emf.createEntityManager();
        
        try{
            tx = emBulk.getTransaction();
            if(!tx.isActive()){
                tx.begin();
            }
            for(Object obj: list){
                emBulk.merge(obj);
            }
            tx.commit();
        }
        catch(Exception ex){
            b = false;            
            logger.error("editBulk()", ex);
            try{
                 if(tx != null && tx.isActive()){
                     tx.rollback();
                 }
            } catch(Exception e){                
                logger.error("editBulk() - rollback: ", e);
            }
        }
        finally{
            emBulk.close();
        }
        
        return b;
    }
    
    public int editBulk2(List<Object> list){
        boolean b = editBulk(list);
        if(b){
            return list.size();
        }
        
        // trong truong hop khong the cap nhat toan bo danh sach
        // thi tien hanh cap nhat tung doi tuong
        logger.info("try edit one by one ...");
        int ncount = 0;
        
        EntityTransaction tx = null;
        EntityManager emBulk = emf.createEntityManager();
        try{
            for(Object obj: list){
                try{
                    tx = emBulk.getTransaction();
                    if(!tx.isActive()){
                        tx.begin();
                    }
                    emBulk.merge(obj);
                    tx.commit();
                    ncount++;
                }
                catch(Exception ex){
                    b = false;            
                    logger.error("editBulk2()", ex);
                    try{
                         if(tx != null && tx.isActive()){
                            tx.rollback();
                         }
                    } catch(Exception e){                
                        logger.error("editBulk2() - rollback: ", e);
                    }
                }
            }
        }        
        finally{
            emBulk.close();
        }
        
        return ncount;
    }
    
    public boolean removeAll(String table){
        boolean b = true;
        EntityManager em = emf.createEntityManager();
        
        String query = "DELETE From " + table 
                + " WHERE 1=1";        
        EntityTransaction tx = null;
        
        try{
            tx = em.getTransaction();
            if(!tx.isActive()){
                tx.begin();
            }
            Query q = em.createNativeQuery(query);
            q.executeUpdate();
            tx.commit();
        }
        catch(Exception ex){
            b = false;            
            logger.error("removeAll()", ex);
            try{
                 if(tx != null && tx.isActive()){
                     tx.rollback();
                 }
            } catch(Exception e){                
                logger.error("removeAll() - rollback: ", e);
            }
        }
        finally{
            em.close();
        }
        
        return b;
    }
    
    
    /*
        Ham thuc hien cap nhat doi tuong,
        neu doi tuong khong ton tai thi se tao doi tuong moi
    */
    public boolean update(Object obj, String field){
        boolean b = true;
        EntityTransaction tx = null;
        Object found, value;
        EntityManager em = emf.createEntityManager();
        
        try{
            tx = em.getTransaction();
            if(!tx.isActive()){
                tx.begin();
            }
            
            if(field == null){
                save(obj);
            }
            else{
                value = Util.getValueByField(obj, field);
                found = findById(obj.getClass(), field, (String)value);
                if(found != null){
                    em.merge(obj);
                }
                else{
                    em.persist(obj);
                }
            }
            
            tx.commit();
        }
        catch(Exception ex){
            b = false;            
            logger.error("update()", ex);
            try{
                 if(tx != null && tx.isActive()){
                     tx.rollback();
                 }
            } catch(Exception e){                
                logger.error("update() - rollback: ", e);
            }
        }
        finally{
            em.close();
        }
        
        return b;
    }
    
    
    /*
        Ham thuc hien cap nhat doi tuong,
        neu doi tuong khong ton tai thi se tao doi tuong moi
    */
    public boolean updateBulk(List<Object> list, String field){
        boolean b = true;
        EntityTransaction tx = null;
        EntityManager emBulk = emf.createEntityManager();
                
        //Object obj;
        List<Object> listNew = new ArrayList<>();
        List<Object> listEdit = new ArrayList<>();
        Object found, value;
        
        try{
            tx = emBulk.getTransaction();
            if(!tx.isActive()){
                tx.begin();
            }
            
            if(field == null){
                for(Object obj: list){
                    emBulk.persist(obj);
                }
            }
            else{
                // tach danh sach lam 2 de cap nhat
                for(Object obj: list){
                    value = Util.getValueByField(obj, field);
                    found = findById(obj.getClass(), field, (String)value);
                    if(found != null){                        
                        listEdit.add(obj);
                    }
                    else{
                        listNew.add(obj);
                    }                    
                }
                
                // tao doi tuong neu moi
                for(Object obj: listNew){
                    emBulk.persist(obj);
                }
                
                // cap nhat doi tuong neu da ton tai
                for(Object obj: listEdit){
                    emBulk.merge(obj);
                }
            }
            
            tx.commit();
        }
        catch(Exception ex){
            b = false;            
            logger.error("updateBuilk()", ex);
            try{
                 if(tx != null && tx.isActive()){
                     tx.rollback();
                 }
            } catch(Exception e){                
                logger.error("updateBuilk() - rollback: ", e);
            }
        }
        finally{
            emBulk.close();
        }
        
        return b;
    }
    
    public Object findById(Class clazz, String field, String value){
        boolean b = true;
        Object obj = null;
        EntityManager em = emf.createEntityManager();
        
        String table = Util.getTableName(em, clazz);
        String query = "SELECT * FROM " +  table
                + " WHERE " + field + " = ?";
        Query q = em.createNativeQuery(query, clazz);
        q.setParameter(1, value);
        
        List<Object> results = null;
        try{
            //obj = q.getSingleResult();
            results = q.getResultList();
            obj = results.isEmpty()? null : results.get(0);
        } 
//        catch(NoResultException ne){
//           logger.info("Khong co doi tuong nao: ", ne);
//        }
        catch(Exception ex){                
            logger.error("findById(): ", ex);
        }
        finally{
            em.close();
        }
        
        return obj;
    }
    
    public int getCount(String table){
        int count = 0;
        EntityManager em = emf.createEntityManager();
        Object obj = null;
        
        String query = "Select Count(*) From " + table;
        Query q = em.createNativeQuery(query);
               
        try{
            obj = q.getSingleResult();
        }
        catch(NoResultException noexp){
            logger.info("NoResultException - getCount(): ", noexp);
        }
        catch(Exception ex){            
            logger.error("EXCEPTION - getCount(): ", ex);
        }
        
        if(obj != null){
            count = (int) ((Number) obj).intValue();
        }
        
        return count;
    }
    
    public boolean removeBulk(List<Object> list){
        boolean b = true;
        EntityTransaction tx = null;
        EntityManager emBulk = emf.createEntityManager();
        
        try{
            tx = emBulk.getTransaction();
            if(!tx.isActive()){
                tx.begin();
            }
            for(Object obj: list){
                emBulk.remove(obj);
            }
            tx.commit();
        }
        catch(Exception ex){
            b = false;            
            logger.error("removeBulk(): ", ex);
            try{
                 if(tx != null && tx.isActive()){
                     tx.rollback();
                 }
            } catch(Exception e){                
                logger.error("removeBulk() - rollback: ", e);
            }
        }
        finally{
            emBulk.close();
        }
        
        return b;
    }
}

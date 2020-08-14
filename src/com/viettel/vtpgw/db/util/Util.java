/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.vtpgw.db.util;

import com.google.gson.JsonObject;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 *
 * @author anPV
 */
public class Util {
    static final Logger logger = LogManager.getLogger(Util.class.getName());
    public static boolean setValue(Object obj, String fieldName, String value){
        boolean b = true;
        java.lang.reflect.Method method;
        try {
            b = setValueByField(obj, fieldName, value);
        }
        catch (Exception e) {
            b = false;            
            logger.error("EXCEPTION: setValue()", e);
        }
        
        return b;
    }
    
    public static String getStringValue(Object obj, String fieldName){
        String value = null;
        java.lang.reflect.Method method;
        
        if(obj == null){
            return null;
        }
        
        try {
            String name = normalizeName(fieldName);
            name = "get" + name;
            
            method = obj.getClass().getMethod(name);
            value = (String) method.invoke(obj);
        } 
        catch (NoSuchMethodException e) {
            logger.error("EXCEPTION: getStringValue()", e);
        }
        catch (Exception e) {            
            logger.error("EXCEPTION: getStringValue()", e);
        }
        
        return value;
    }
    
    public static boolean setValue(Object obj, String fieldName, Integer value){
        boolean b = true;
        java.lang.reflect.Method method;
        
        if(obj == null){
            return false;
        }
        
        try {
            String v = "" + value;
            b = setValueByField(obj, fieldName, v);
        }
        catch (Exception e) {
            logger.info(e);
            b = false;
        }
        
        return b;
    }
    
    public static Integer getIntValue(Object obj, String fieldName){
        Integer value = null;
        java.lang.reflect.Method method;
        
        if(obj == null){
            return null;
        }
        
        try {
            String name = normalizeName(fieldName);
            name = "get" + name;
            
            method = obj.getClass().getMethod(name);
            value = (Integer) method.invoke(value);
        } 
        catch (NoSuchMethodException e) {             
            logger.error("EXCEPTION: getIntValue()", e);
        }
        catch (Exception e) {            
            logger.error("EXCEPTION: getIntValue()", e);
        }
        
        return value;
    }
    
    public static Object getValueByField(Object obj, String fieldName){
        Object value = null;
        java.lang.reflect.Method method;
        
        if(obj == null){
            return null;
        }
        
        try {
            String name = normalizeName(fieldName);
            name = "get" + name;
            
            method = obj.getClass().getMethod(name);            
            value = (Object) method.invoke(obj);
        } 
        catch (NoSuchMethodException e) {             
            logger.error("EXCEPTION: getValueByField()", e);
        }
        catch (Exception e) {            
            logger.error("EXCEPTION: getValueByField()", e);
        }
        
        return value;
    }
    
    public static String normalizeName(String name){
        if(name == null || name.length() <= 0){
            return name;
        }
        
        char ch;
        String str = "";
        boolean bCapital = false;
        for(int i = 0; i < name.length(); i++){
            ch = name.charAt(i);
            if(ch == '_'){
                // bo qua ky tu gach chan duoi
                bCapital = true;
            }
            else{
                if(bCapital){
                    str += Character.toUpperCase(ch);
                    bCapital = false;
                }
                else{
                    str += ch;
                }
            }
        }
        
        str = str.substring(0, 1).toUpperCase() + str.substring(1);
        return str;
    }
    
    
    public static boolean setValueByField(Object obj, String fieldName, String value){
        boolean b = false;
        
        if(obj == null){
            return false;
        }
        
        String name = normalizeName(fieldName);
        name = "set" + name;
        
        try {
            for (Method m : obj.getClass().getMethods()) {
                if (name.equals(m.getName())) {
                    Class<?>[] params = m.getParameterTypes();
                    Object[] pa = new Object[1];
                    
                    if (params.length == 1) {
                        String paramClass = params[0].getName();
                        if (params[0].isInstance(value)){                            
                            pa[0] = value;
                            m.invoke(obj, pa);
                            b = true;
                        }
                        else if ("java.lang.Integer".equals(paramClass)){
                            Integer v = parseInt(value);
                            //logger.info("Convert to int: fieldName = " + fieldName + ", value = " + v);
                            
                            pa[0] = v;
                            m.invoke(obj, pa);
                            b = true;
                        }
                        else{
                            b = false;
                        }
                        
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            logger.error("EXCEPTION: setValueByField()", ex);
        }        

        return b;
    }
    
    
    /**
    * Returns the table name for a given entity type in the {@link EntityManager}.
    * @param em
    * @param entityClass
    * @return
    */
    public static <T> String getTableName(EntityManager em, Class<T> entityClass) {
        /*
         * Check if the specified class is present in the metamodel.
         * Throws IllegalArgumentException if not.
         */
        try{
        Metamodel meta = em.getMetamodel();
        EntityType<T> entityType = meta.entity(entityClass);

        //Check whether @Table annotation is present on the class.
        Table t = entityClass.getAnnotation(Table.class);

        String tableName = (t == null)
                            ? entityType.getName().toUpperCase()
                            : t.name();
        return tableName;
        }
        catch(Exception ex){
            logger.info(ex);
            return null;
        }
    }
    
    
    public static List<String> parseKeyword(String keyword){        
        List<String> items = Arrays.asList(keyword.split(","));        
        return items;        
    }
    
    public static boolean saveResource(String surl, String filePath){        
		InputStream in = null;
        try {
            if(surl == null || surl.isEmpty()){
                logger.info("URL not set: url = " + surl);
                return false;
            }
            
            URL url = new URL(surl);        
            in = new BufferedInputStream(url.openStream());
            Files.copy(in, Paths.get(filePath));            
        }
        catch (Exception e) {            
            logger.error("EXCEPTION: saveResource(): surl =" + surl + ", filePath = " + filePath, e);
            return false;
        }
		finally {
                    if(in != null){
                        try {
                            in.close();
                        } catch (IOException ex) {
                             logger.error("EXCEPTION: ", ex);
                        }
                    }			
		}
		
        return true;
    }
    
    public static boolean removeResource(String surl, String filePath){        
        try {
            if(surl == null || surl.isEmpty()){
                return true;
            }
            
            int pos = surl.lastIndexOf('/');
            String filename = "";
            if(pos != -1){
                filename = surl.substring(pos + 1);
            }
            
            if(!filename.isEmpty()){
                filename = filePath + "/" + filename;
                Files.deleteIfExists(Paths.get(filename));
            }
        } catch (Exception e) {            
            logger.error("EXCEPTION: removeResource(): surl =" + surl + ", filePath = " + filePath, e);
            return false;
        }
        return true;
    }
    
    public static String performMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] enc = md.digest();
            String md5Sum = new sun.misc.BASE64Encoder().encode(enc);
            return md5Sum;
        } catch (NoSuchAlgorithmException nsae) {
            //System.out.println(nsae.getMessage());
            logger.info(nsae);
            return null;
        }
    }
    
    static Integer parseInt(String value){
        Integer val = 0;
        try{
            //val = Integer.parseInt(value);
            
            // for arpu
            Double d = Double.parseDouble(value);
            val = (int)d.intValue();
        }
        catch(NumberFormatException e){
            logger.info("parseInt(): " + value + ", EXC: + " + e.getMessage());
        }
        catch(Exception ex){
            logger.info("parseInt(): " + value, ex);
        }
        
        return val;
    }
    
    public static Set<Class<?>> getAllClasses(String packageName){
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
            .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
            .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName))));

        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        
//        for(Class c: classes){
//            System.out.println("Class: " + c.getName());
//        }
        return classes;
    }
    
    public static Class<?> getEntityClassByTableName(EntityManager em, String tableName, String packageName){
        //Class<?> clazz = null;
        Set<Class<?>> list = getAllClasses(packageName);
        for(Class c: list){            
            String name = getTableName(em, c);
            if(tableName.equals(name)){
                return c;
            }
        }
        
        return null;
    }
    
    public static Class<?> getEntityClassByName(String className, String packageName){
        //Class<?> clazz = null;
        Set<Class<?>> list = getAllClasses(packageName);
        for(Class c: list){            
            String name = c.getName();
            if(className.equals(name)){
                return c;
            }
        }
        
        return null;
    }
    
    public static String getClassName(Object obj, String fieldName){
        boolean b = false;
        
        if(obj == null){
            return null;
        }
        
        String name = normalizeName(fieldName);
        name = "set" + name;
        
        try {
            for (Method m : obj.getClass().getMethods()) {
                if (name.equals(m.getName())) {
                    Class<?>[] params = m.getParameterTypes();
                    //Object[] pa = new Object[1];
                    
                    if (params.length == 1) {
                        String paramClass = params[0].getName();
                        return paramClass;                        
                    }
                }
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            logger.error("EXCEPTION: setValueByField()", ex);
        }        

        return null;
    }
    
    public static void setValueByField(JsonObject jsonObject, Object product, String fieldName, String value){
        String s = getClassName(product, fieldName);
        if(s != null && "java.lang.Integer".equals(s)){            
            Integer v = parseInt(value);
            jsonObject.addProperty(fieldName, v);
            return;
        }
        
        jsonObject.addProperty(fieldName, value);
    }
        
    public static String getProperty(Properties prop, String key, String defaultValue){
        String mode = prop.getProperty("mode", "deploy");
        String value = prop.getProperty(mode + "." + key, defaultValue);
        return value;
    }
    
    public static int getInt(Properties prop, String key, int defaultValue){
        String value = getProperty(prop, key, "0");
        int val = 0;
        try{
            val = Integer.parseInt(value);
        }
        catch(Exception ex){
            logger.error("getInt(): ", ex);
            val = defaultValue;
        }
        
        return val;
    }
}

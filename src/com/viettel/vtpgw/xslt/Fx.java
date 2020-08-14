package com.viettel.xslt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.vertx.core.MultiMap;

public class Fx{
	public final String base64(String text){
		return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8)); 
	}
	public final String getProp(Object properties,String name){
		return ((MultiMap)properties).get(name);
	}
	public final void setProp(Object properties,String name,String value){
		((MultiMap)properties).set(name,value);
	}	 
}
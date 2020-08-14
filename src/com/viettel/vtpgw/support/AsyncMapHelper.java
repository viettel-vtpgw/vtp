package com.viettel.vtpgw.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hazelcast.core.IMap;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.spi.cluster.hazelcast.impl.HazelcastAsyncMap;

public class AsyncMapHelper {
	public static <K,V>void getKeys(Vertx vertx,AsyncMap<K,V> map,Handler<AsyncResult<Set<K>>> handler){
		vertx.<Set<K>>executeBlocking(fut->{
			getKeysBlocking(map,fut);
		},handler);
	}
	@SuppressWarnings("unchecked")
	private static <K,V> void getKeysBlocking(AsyncMap<K,V> map,Future<Set<K>>fut){
		if(map instanceof HazelcastAsyncMap){
			try{
				Field field = HazelcastAsyncMap.class.getDeclaredField("map");
				field.setAccessible(true);
				Method convertReturn = HazelcastAsyncMap.class.getDeclaredMethod("convertReturn",Object.class);
				convertReturn.setAccessible(true);
				@SuppressWarnings({ "rawtypes" })
				IMap imap = (IMap)field.get(map);
				Set<K> rs = new HashSet<>();
				for(Object key:imap.keySet()){
					rs.add((K)convertReturn.invoke(map,key));
				}
				fut.complete(rs);
			}catch(Exception e){
				fut.fail(e);
			}
		} else {
			Class<?> clazz = map.getClass();
			try{
				Field field = clazz.getDeclaredField("delegate");
				field.setAccessible(true);
				@SuppressWarnings({ "rawtypes" })
				AsyncMap<K,V> imap = (AsyncMap)field.get(map);
				getKeysBlocking(imap,fut);
			}catch(Exception e){
				fut.fail(e);
			}
		}
	}
	public static <K,V>void getValues(Vertx vertx,AsyncMap<K,V> map,Handler<AsyncResult<Collection<V>>> handler){
		vertx.<Collection<V>>executeBlocking(fut->{
			getValuesBlocking(map,fut);
		},handler);
	}
	@SuppressWarnings("unchecked")
	private static <K,V> void getValuesBlocking(AsyncMap<K,V> map,Future<Collection<V>>fut){
		if(map instanceof HazelcastAsyncMap){
			try{
				Field field = HazelcastAsyncMap.class.getDeclaredField("map");
				field.setAccessible(true);
				Method convertReturn = HazelcastAsyncMap.class.getDeclaredMethod("convertReturn",Object.class);
				convertReturn.setAccessible(true);
				@SuppressWarnings({ "rawtypes" })
				IMap imap = (IMap)field.get(map);
				Collection<V> rs = new ArrayList<>();
				for(Object value:imap.values()){
					rs.add((V)convertReturn.invoke(map,value));
				}
				fut.complete(rs);
			}catch(Exception e){
				fut.fail(e);
			}
		} else {
			Class<?> clazz = map.getClass();
			try{
				Field field = clazz.getDeclaredField("delegate");
				field.setAccessible(true);
				@SuppressWarnings({"rawtypes" })
				AsyncMap<K,V> imap = (AsyncMap)field.get(map);
				getValuesBlocking(imap,fut);
			}catch(Exception e){
				fut.fail(e);
			}
		}
	}
}

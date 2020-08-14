package com.viettel.vtpgw.model;

import java.util.List;
import java.util.Map;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

public interface Service extends Shareable {
	List<String> alertReceivers();
	long alertTimeout();
	Endpoint endpoint();
	String id();
	String module();
	String name();
	Map<String, String> params();
	Long rateLimitCapacity();
	Long rateLimitPeriod();	
	Map<String, String> respParams();
	Endpoint sandboxEndpoint();
	long standardDuration();
	long timeout();
	JsonObject toJson();
}

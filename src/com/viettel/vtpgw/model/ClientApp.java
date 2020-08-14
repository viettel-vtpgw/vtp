package com.viettel.vtpgw.model;

import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

public interface ClientApp extends Shareable{
	List<String> alertReceivers();
	long alertTimeout();
	JsonObject toJson();
	String token();
	String username();
}

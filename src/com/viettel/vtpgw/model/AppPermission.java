package com.viettel.vtpgw.model;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

public interface AppPermission extends Shareable{
	boolean acceptIp(String ip);
	boolean acceptMethod();
	boolean acceptMethod(String method);
	ClientApp client();
	boolean debug();
	String id();
	boolean noContent();
	Long rateLimitCapacity();
	Long rateLimitPeriod();
	boolean sandbox();
	Service service();
	long timeout();
	JsonObject toJson();
}

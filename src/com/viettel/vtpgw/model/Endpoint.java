package com.viettel.vtpgw.model;

import io.vertx.core.shareddata.Shareable;

public interface Endpoint extends Shareable {
	boolean activated();
	String authorization();
	String host();
	boolean ssl();
	String url();
        String node();
}

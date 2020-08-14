package com.viettel.vtpgw.model.impl;

import com.viettel.vtpgw.model.Endpoint;

public class DefaultEndpoint implements Endpoint{	
	private boolean activated=true;
	private String authorization;
	private String host;
	private boolean ssl;
	private String url;
        String node;
        
	public boolean activated() {
		return activated;
	}
	public String authorization() {
		return authorization;
	}
	public String host() {
		return host;
	}
	public void setActivated(boolean activated) {
		this.activated = activated;
	}
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}
	public void setHost(String host) {
		this.host = host;
	}

	public void setUrl(String url) {
		this.url = url;
		ssl = url.startsWith("https://");
	}
        
        public void setNode(String node) {
            this.node = node;
	}
        
        public String node() {
            return this.node;
	}
        
	@Override
	public boolean ssl() {
		return ssl;
	}
	public String url() {
		return url;
	}
	
}

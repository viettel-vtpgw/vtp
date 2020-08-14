package com.viettel.vtpgw.model;

public interface HttpService extends Service{
	String BUILT_IN_REST="REST";
	String BUILT_IN_SOAP="SOAP";
	//HttpClientRequest requestAbs(Vertx vertx,Context context,HttpMethod method, String uri, Handler<HttpClientResponse> responseHandler);	
	boolean check();
	boolean check(String json);
}

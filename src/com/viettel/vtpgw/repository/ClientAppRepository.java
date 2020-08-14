package com.viettel.vtpgw.repository;

import com.viettel.vtpgw.model.ClientApp;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface ClientAppRepository {
	void get(String username,Handler<AsyncResult<ClientApp>>handler);
}

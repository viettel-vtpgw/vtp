package com.viettel.vtpgw.repository;

import com.viettel.vtpgw.model.Service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface ServiceRepository {
	void get(String id,Handler<AsyncResult<Service>>handler);
}

package com.viettel.vtpgw.repository;

import java.util.List;

import com.viettel.vtpgw.model.AppPermission;
import com.viettel.vtpgw.model.ClientApp;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface AppPermissionRepository {
	void findByClientApp(ClientApp client,Handler<AsyncResult<List<AppPermission>>>handler);
	void get(String id,Handler<AsyncResult<AppPermission>>handler);
}

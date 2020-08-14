package com.viettel.vtpgw.http;

import com.viettel.vtpgw.context.ResponseContext;

import io.vertx.core.http.HttpServerResponse;

public abstract class AbstractHttpServerResponseWrapper extends ResponseContext implements HttpServerResponse {
	@Override
	public AbstractHttpServerResponseWrapper putHeader(CharSequence header, CharSequence value) {
		return this;
	}
	@Override
	public AbstractHttpServerResponseWrapper setStatusCode(int code) {
		return this;
	}
}

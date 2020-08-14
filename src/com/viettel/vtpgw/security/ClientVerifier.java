package com.viettel.vtpgw.security;

public interface ClientVerifier<T> {
	boolean accept(T clientInfo);
}

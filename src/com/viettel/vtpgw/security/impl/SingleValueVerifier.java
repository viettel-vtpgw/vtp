package com.viettel.vtpgw.security.impl;

import com.viettel.vtpgw.security.ClientVerifier;

public class SingleValueVerifier<T> implements ClientVerifier<T> {
	T ip;
	public SingleValueVerifier(T ip){
		this.ip = ip;
	}
	@Override
	public boolean accept(T ip) {
		return this.ip.equals(ip);
	}
}

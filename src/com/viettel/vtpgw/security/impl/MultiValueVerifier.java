package com.viettel.vtpgw.security.impl;

import java.util.HashSet;

import com.viettel.vtpgw.security.ClientVerifier;

public class MultiValueVerifier<T> extends HashSet<T> implements ClientVerifier<T> {
	private static final long serialVersionUID = 1165531716147021083L;
	public MultiValueVerifier(int size){
		super(size);
	}
	@Override
	public boolean accept(T clientInfo) {
		return this.contains(clientInfo);
	}
}

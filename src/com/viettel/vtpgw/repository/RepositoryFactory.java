package com.viettel.vtpgw.repository;

public interface RepositoryFactory {
	ClientAppRepository getClientAppRepository();
	ServiceRepository getExternalServiceRepository();
	AppPermissionRepository getServiceAccessRepository();
}

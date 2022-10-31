package com.ntt.tc.data.tenants;

import com.ntt.tc.data.C8yData;

/**
 * TenantReference class
 * docs 内にないため、新規作成。
 */
public class TenantReference extends C8yData {
	/**
	 * Link to tenant's resource.
	 */
	public String self;
	
	/**
	 * Tenant
	 */
	public Tenant tenant;
}

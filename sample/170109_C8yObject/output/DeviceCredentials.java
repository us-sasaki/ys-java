package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

public class DeviceCredentials extends C8yData {
	/**
	 * Device identifier, e.g. IMEI
	 *
	 * Occurs : 1
	 */
	public String id;
	
	/**
	 * Tenant id for authentication
	 *
	 * Occurs : 1
	 */
	public String tenantId;
	
	/**
	 * New username
	 *
	 * Occurs : 1
	 */
	public String username;
	
	/**
	 * New password
	 *
	 * Occurs : 1
	 */
	public String password;
	
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
}

package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.Statistics;

/**
 * GET /tenant/statistics のレスポンスの要素
 */
public class TenantUsageStatistics extends C8yData {
	/**
	 * Date of statistics.
	 */
	public String day;
	
	/**
	 * Number of devices in the tenant (c8y_IsDevice).
	 * @see	com.ntt.tc.data.ManagedObject
	 */
	public int deviceCount;
	
	/**
	 * Number of requests that were issued only by devices against the tenant.
	 */
	public int deviceRequestCount;
	
	/**
	 * Number of devices with all children.
	 */
	public int deviceWithChildrenCount;
	
	/**
	 * Number of requests that were issued against the tenant.
	 */
	public int requestCount;
	
	/**
	 * Database storage in use by the tenant, in bytes.
	 */
	public long storageSize;
	
	/**
	 * Names of tenant subscribed applications.
	 */
	public String[] subscribedApplications;
}

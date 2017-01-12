package com.ntt.tc.data.tenants;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.dataabdom.data.json.JsonObject;

/**
 * UsageStatistics class
 * This source is machine-generated.
 */
public class UsageStatistics extends C8yData {
	/**
	 * Date of statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String day;
	
	/**
	 * Number of devices in the tenant (c8y\_IsDevice).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public Double deviceCount;
	
	/**
	 * Number of requests that were issued only by devices against the tenant.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public Double deviceRequestCount;
	
	/**
	 * Number of devices with all children.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public Double deviceWithChildrenCount;
	
	/**
	 * Number of requests that were issued against the tenant.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public Double requestCount;
	
	/**
	 * Database storage in use by the tenant, in bytes.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public Double storageSize;
	
	/**
	 * Names of tenant subscribed applications.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public JsonObject subscribedApplications;
	
}

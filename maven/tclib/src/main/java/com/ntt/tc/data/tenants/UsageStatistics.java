package com.ntt.tc.data.tenants;

import com.ntt.tc.data.C8yData;
import abdom.data.json.JsonObject;

/**
 * UsageStatistics class
 * This source is machine-generated from c8y-markdown docs.
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
	public double deviceCount;
	
	/**
	 * Number of requests that were issued only by devices against the tenant.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public double deviceRequestCount;
	
	/**
	 * Number of devices with all children.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public double deviceWithChildrenCount;
	
	/**
	 * Number of requests that were issued against the tenant.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public double requestCount;
	
	/**
	 * Database storage in use by the tenant, in bytes.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public double storageSize;
	
	/**
	 * Names of tenant subscribed applications.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String[] subscribedApplications;
	
}

package com.ntt.tc.data.tenants;

import com.ntt.tc.data.C8yData;
import abdom.data.json.JsonObject;

/**
 * UsageStatistics class
 * This source is machine-generated from c8y-markdown docs.
 * Number 型が一律 double に変換されているため、修正。(2017/11/2)
 * tenantId を追加し、allTenantsSummary でも利用できるようにした。(2018/8/20)
 */
public class UsageStatistics extends C8yData {
	/**
	 * Date of statistics.
	 * allTenantsSummary では null となります。
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
	public int deviceCount;
	
	/**
	 * Number of requests that were issued only by devices against the tenant.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public long deviceRequestCount;
	
	/**
	 * Number of devices with all children.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public int deviceWithChildrenCount;
	
	/**
	 * Number of requests that were issued against the tenant.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public long requestCount;
	
	/**
	 * Database storage in use by the tenant, in bytes.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public long storageSize;
	
	/**
	 * Names of tenant subscribed applications.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String[] subscribedApplications;
	
	/**
	 * allTenantsSummary でも本オブジェクトを利用するため追加。
	 */
	public String tenantId;
	
}

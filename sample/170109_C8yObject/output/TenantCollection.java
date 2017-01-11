package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.PagingStatistics;

/**
 * TenantCollection class
 * This source is machine-generated.
 */
public class TenantCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of Tenant, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Tenant tenants;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of tenants.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of tenants.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

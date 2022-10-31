package com.ntt.tc.data.retention;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.retention.RetentionRule;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * RetentionRuleCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class RetentionRuleCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of Retention rule, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public RetentionRule[] retentionRules;
	
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

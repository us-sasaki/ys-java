package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.PagingStatistics;

/**
 * GroupReferenceCollection class
 * This source is machine-generated.
 */
public class GroupReferenceCollection extends C8yData {
	/**
	 * Link to this Resource
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of group references
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public GroupReference groups;
	
	/**
	 * Information about the paging statistics
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a possible previous page with additional group references
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a possible next page with additional group references
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

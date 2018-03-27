package com.ntt.tc.data.applications;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.applications.Application;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * ApplicationCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class ApplicationCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of applications, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Application[] applications;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of applications.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of applications.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

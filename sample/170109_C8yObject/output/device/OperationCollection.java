package com.ntt.tc.data.device;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * OperationCollection class
 * This source is machine-generated.
 */
public class OperationCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of operations, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Operations[] operations;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of operations.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of operations.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.Operations;
import com.ntt.tc.data.PagingStatistics;

public class BulkOperationCollection extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * List of bulk operations, see below.
	 *
	 * Occurs : 0..n
	 */
	public Operations bulkOperations;
	
	/**
	 * Information about paging statistics.
	 *
	 * Occurs : 1
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of bulk operations.
	 *
	 * Occurs : 0..1
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of bulk operations.
	 *
	 * Occurs : 0..1
	 */
	public String next;
	
}

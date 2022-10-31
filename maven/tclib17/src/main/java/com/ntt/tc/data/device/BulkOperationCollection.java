package com.ntt.tc.data.device;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.device.Operation;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * BulkOperationCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class BulkOperationCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of bulk operations, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Operation[] bulkOperations;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of bulk operations.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of bulk operations.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

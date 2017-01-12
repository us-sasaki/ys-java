package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * PagingStatistics class
 * This source is machine-generated.
 */
public class PagingStatistics extends C8yData {
	/**
	 * The approximate total number of records.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public Int totalRecords;
	
	/**
	 * Maximum number of records contained in this query.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public Int pageSize;
	
	/**
	 * The current returned page within the full result set, starting at "1".
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public Int currentPage;
	
}

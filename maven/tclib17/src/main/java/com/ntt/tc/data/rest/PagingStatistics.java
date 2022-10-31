package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Int;

/**
 * PagingStatistics class
 * This source is machine-generated from c8y-markdown docs.
 */
public class PagingStatistics extends C8yData {
	/**
	 * The approximate total number of records.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public TC_Int totalPages;
	
	/**
	 * Maximum number of records contained in this query.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public int pageSize;
	
	/**
	 * The current returned page within the full result set, starting at "1".
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public int currentPage;
	
}

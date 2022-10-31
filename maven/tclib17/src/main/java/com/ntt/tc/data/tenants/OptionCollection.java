package com.ntt.tc.data.tenants;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.tenants.Option;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * OptionCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class OptionCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of Options, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Option[] options;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of options.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of options.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

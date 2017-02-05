package com.ntt.tc.data.users;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.users.RoleReference;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * RoleReferenceCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class RoleReferenceCollection extends C8yData {
	/**
	 * Link to this Resource
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of role references
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public RoleReference[] references;
	
	/**
	 * Information about the paging statistics
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a possible previous page with additional role references
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a possible next page with additional role references
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

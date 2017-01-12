package com.ntt.tc.data.users;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.users.Role;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * RoleCollection class
 * This source is machine-generated.
 */
public class RoleCollection extends C8yData {
	/**
	 * Link to this Resource
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of Roles
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Role[] roles;
	
	/**
	 * Information about the paging statistics
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a possible previous page with additional roles
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a possible next page with additional roles
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

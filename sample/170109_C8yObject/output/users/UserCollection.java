package com.ntt.tc.data.users;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.users.User;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * UserCollection class
 * This source is machine-generated.
 */
public class UserCollection extends C8yData {
	/**
	 * Link to this Resource
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of users
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public User[] users;
	
	/**
	 * Information about the paging statistics
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a possible previous page with additional users
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a possible next page with additional users
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

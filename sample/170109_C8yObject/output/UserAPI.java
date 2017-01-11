package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * UserAPI class
 * This source is machine-generated.
 */
public class UserAPI extends C8yData {
	/**
	 * Link to this Resource
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * A reference to a resource of type User. The template contains a
	 * placeholders {realm} and {userName}.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String userByName;
	
	/**
	 * A collection of all users belonging to a given realm. The template
	 * contains a placeholder {realm}.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String users;
	
	/**
	 * A reference to the resource of the logged in User.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String currentUser;
	
	/**
	 * A reference to a resource of type Group. The template contains a
	 * placeholders {realm} and {groupName}.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String groupByName;
	
	/**
	 * A collection of all users belonging to a given realm. The template
	 * contains a placeholder {realm}.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String groups;
	
	/**
	 * A collection of all roles.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String roles;
	
}

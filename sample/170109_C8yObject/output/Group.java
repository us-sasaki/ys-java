package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * Group class
 * This source is machine-generated.
 */
public class Group extends C8yData {
	/**
	 * Uniquely identifies a Group
	 * <pre>
	 * Allowed in PUT/POST request : not allowed
	 * Occurs : 1
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this Resource
	 * <pre>
	 * Allowed in PUT/POST request : not allowed
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Descriptive Name of the Group
	 * <pre>
	 * Allowed in PUT/POST request : mandatory
	 * Occurs : 1
	 * </pre>
	 */
	public String name;
	
	/**
	 * List of role references
	 * <pre>
	 * Allowed in PUT/POST request : not allowed
	 * Occurs : 1
	 * </pre>
	 */
	public RoleReferenceCollection roles;
	
	/**
	 * List of device permissions
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public JsonObject devicePermissions;
	
}

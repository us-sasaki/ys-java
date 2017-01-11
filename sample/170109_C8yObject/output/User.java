package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.Boolean;

/**
 * User class
 * This source is machine-generated.
 */
public class User extends C8yData {
	/**
	 * Uniquely identifies a user
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
	 * User name, unique for a given domain. Max: 1000 characters. Whitespaces,
	 * slashes, +$: characters not allowed
	 * <pre>
	 * Allowed in PUT/POST request : POST:mandatory PUT:not allowed
	 * Occurs : 1
	 * </pre>
	 */
	public String userName;
	
	/**
	 * User password. Min: 6, max: 32 characters. Only Latin1 chars allowed.
	 * <pre>
	 * Allowed in PUT/POST request : POST:mandatory PUT:optional
	 * Occurs : 1
	 * </pre>
	 */
	public String password;
	
	/**
	 * User first name.
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public String firstName;
	
	/**
	 * User last name.
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public String lastName;
	
	/**
	 * User phone number. Format: "+[country code][number]", has to be a valid
	 * MSISDN
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public String phone;
	
	/**
	 * User email address.
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public String email;
	
	/**
	 * User activation status (true/false)
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public Boolean enabled;
	
	/**
	 * Keeps a list of custom properties
	 * <pre>
	 * Allowed in PUT/POST request : optional
	 * Occurs : 1
	 * </pre>
	 */
	public JsonObject customProperties;
	
	/**
	 * List of group references
	 * <pre>
	 * Allowed in PUT/POST request : not allowed
	 * Occurs : 1
	 * </pre>
	 */
	public GroupReferenceCollection groups;
	
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

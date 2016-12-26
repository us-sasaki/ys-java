package com.ntt.tc.data;

import abdom.data.json.JsonObject;

/**
 * User
 */
public class User extends C8yData {
	/**
	 * Uniquely identifies a user
	 * PUT/POST not allowed
	 */
	public String id;
	public String self;
	public String userName;
	public String password;
	public String firstName;
	public String lastName;
	public String phone;
	public String email;
	public Boolean enabled;
	public Boolean sendPasswordResetEmail;
	public JsonObject customProperties;
	public JsonObject groups;
	public JsonObject devicePermissions;
	public JsonObject roles;
	public JsonObject[] effectiveRoles;
	
	public User() {
	}
	
	public User(String userName,
				String password,
				String firstName,
				String lastName,
				String phone,
				JsonObject customProperties,
				String email,
				boolean enabled,
				boolean sendPasswordResetEmail ) {
		this.userName = userName;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.customProperties = customProperties;
		this.email = email;
		this.enabled = new Boolean(enabled);
		this.sendPasswordResetEmail = new Boolean(sendPasswordResetEmail);
	}
}

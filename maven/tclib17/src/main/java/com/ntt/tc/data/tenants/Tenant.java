package com.ntt.tc.data.tenants;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.tenants.ApplicationReferenceCollection;
import abdom.data.json.JsonObject;

/**
 * Tenant class
 * This source is machine-generated from c8y-markdown docs.
 */
public class Tenant extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 0..1
	 * Visibility : Public
	 * </pre>
	 */
	public String self;
	
	/**
	 * Tenant id
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "32"
	 * </pre>
	 */
	public String id;
	
	/**
	 * Status of tenant, possible values [ACTIVE, SUSPENDED].
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public String status;
	
	/**
	 * Administrator user name. Whitespaces, slashes, +$: characters not
	 * allowed
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * max length : "50"
	 * </pre>
	 */
	public String adminName;
	
	/**
	 * Administrator Email
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public String adminEmail;
	
	/**
	 * Can this tenant create its own tenants.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public boolean allowCreateTenants;
	
	/**
	 * Storage quota per device the user has.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public long storageLimitPerDevice;
	
	/**
	 * Administrator Password
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public String adminPassword;
	
	/**
	 * Enable password reset
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public boolean sendPasswordResetEmail;
	
	/**
	 * URL of tenants domain.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "256"
	 * </pre>
	 */
	public String domain;
	
	/**
	 * Tenants company name.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "256"
	 * </pre>
	 */
	public String company;
	
	/**
	 * Contact person name.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "30"
	 * </pre>
	 */
	public String contactName;
	
	/**
	 * Contact person phone number.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "20"
	 * </pre>
	 */
	public String contactPhone;
	
	/**
	 * Collection of tenant subscribed, applications.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public ApplicationReferenceCollection applications;
	
	/**
	 * Collection of tenant owned, applications.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public - only applications with availability MARKET
	 * </pre>
	 */
	public ApplicationReferenceCollection ownedApplications;
	
	/**
	 * Keeps a list of custom properties
	 * <pre>
	 * Occurs : 1
	 * Visibility : optional
	 * </pre>
	 */
	public JsonObject customProperties;
	
	/**
	 * Name of parent tenant, the creator of this tenant.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * </pre>
	 */
	public String parent;
	
}

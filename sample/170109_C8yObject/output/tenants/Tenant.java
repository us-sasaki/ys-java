package com.ntt.tc.data.tenants;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.tenants.ApplicationReferenceCollection;

/**
 * Tenant class
 * This source is machine-generated.
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
	public Boolean allowCreateTenants;
	
	/**
	 * Storage quota per device the user has.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public Number storageLimitPerDevice;
	
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
	public Boolean sendPasswordResetEmail;
	
	/**
	 * URL of tenants domain.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * </pre>
	 */
	public String domain;
	
	/**
	 * Tenants company name.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * </pre>
	 */
	public String company;
	
	/**
	 * Contact person name.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * </pre>
	 */
	public String contactName;
	
	/**
	 * Contact person phone number.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
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

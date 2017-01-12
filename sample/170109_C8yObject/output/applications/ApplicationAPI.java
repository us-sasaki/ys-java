package com.ntt.tc.data.applications;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.applications.ApplicationCollection;

/**
 * ApplicationAPI class
 * This source is machine-generated.
 */
public class ApplicationAPI extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * A reference to resource of type Application (placeholder {id})
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String applicationById;
	
	/**
	 * Collection of all applications
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public ApplicationCollection applications;
	
	/**
	 * Read-only collection of all applications with a particular name
	 * (placeholder {name}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String applicationsByName;
	
	/**
	 * Read-only collection of all?applications subscribed by particular tenant
	 * (placeholder {tenant}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String applicationsByTenant;
	
	/**
	 * Read-only collection of all?applications owned by particular tenant
	 * (placeholder {tenant}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String applicationsByOwner;
	
}

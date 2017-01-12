package com.ntt.tc.data.applications;

import com.ntt.tc.data.C8yData;

/**
 * Application class
 * This source is machine-generated.
 */
public class Application extends C8yData {
	/**
	 * Link to this Resource
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String self;
	
	/**
	 * Unique identifier for an application
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String id;
	
	/**
	 * Name of the application
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: Optional
	 * </pre>
	 */
	public String name;
	
	/**
	 * Shared secret of the application
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: Optional
	 * </pre>
	 */
	public String key;
	
	/**
	 * Type of application. Possible values are : EXTERNAL, HOSTED,
	 * MICROSERVICE
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String type;
	
	/**
	 * Access level for other tenants.??Possible values are : "MARKET",
	 * "PRIVATE"(default)
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : Optional
	 * </pre>
	 */
	public String availability;
	
	/**
	 * Reference to tenant owning this application
	 * <pre>
	 * Occurs : ?1
	 * PUT/POST : No?
	 * </pre>
	 */
	public TenantReference owner;
	
	/**
	 * contextPath of hosted application?
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : POST: Mandatory (when application type is HOSTED) PUT: Optional
	 * </pre>
	 */
	public String contextPath;
	
	/**
	 * URL to application base directory hosted on external server
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : POST: Mandatory (when application type is HOSTED) PUT: Optional
	 * </pre>
	 */
	public String resourcesUrl;
	
	/**
	 * authorization username to access resourcesUrl?
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : Optional
	 * </pre>
	 */
	public String resourcesUsername;
	
	/**
	 * authorization password to access resourcesUrl?
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : Optional
	 * </pre>
	 */
	public String resourcesPassword;
	
	/**
	 * URL to external application
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : POST: Mandatory (when application type is EXTERNAL) PUT: Optional
	 * </pre>
	 */
	public String externalUrl;
	
}

package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * Module class
 * This source is machine-generated.
 */
public class Module extends C8yData {
	/**
	 * Uniquely identifies a module.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String self;
	
	/**
	 * Time when module was created or modified.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String lastModified;
	
	/**
	 * The module name.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST:�?Mandatory PUT:�?Optional
	 * </pre>
	 */
	public String name;
	
	/**
	 * The module status: DEPLOYED, NOT\_DEPLOYED (default).
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST:�?No PUT:�?Optional
	 * </pre>
	 */
	public String status;
	
}

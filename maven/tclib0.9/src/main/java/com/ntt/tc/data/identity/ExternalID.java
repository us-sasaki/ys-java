package com.ntt.tc.data.identity;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ManagedObject;

/**
 * ExternalID class
 * This source is machine-generated from c8y-markdown docs.
 */
public class ExternalID extends C8yData {
	/**
	 * The identifier used in the external system that Cumulocity interfaces
	 * with.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 * </pre>
	 */
	public String externalId;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String self;
	
	/**
	 * The type of the external identifier as string, e.g.,
	 * "com\_cumulocity\_model\_idtype\_SerialNumber".
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 * </pre>
	 */
	public String type;
	
	/**
	 * The ManagedObject linked to the external ID.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 * </pre>
	 */
	public ManagedObject managedObject;
	
}

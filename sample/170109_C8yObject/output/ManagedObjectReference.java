package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.ManagedObject;

public class ManagedObjectReference extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * The ManagedObject being referenced.
	 *
	 * Occurs : 1
	 */
	public ManagedObject managedObject;
	
}

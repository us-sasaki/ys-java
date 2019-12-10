package com.ntt.tc.data.tenants;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.applications.Application;

/**
 * ApplicationReference class
 * This source is machine-generated from c8y-markdown docs.
 */
public class ApplicationReference extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * The Application being referenced
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Application application;
}

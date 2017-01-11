package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * ApplicationReference class
 * This source is machine-generated.
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
	public Application reference;
	
}

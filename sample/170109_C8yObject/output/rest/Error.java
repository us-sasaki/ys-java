package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * Error class
 * This source is machine-generated.
 */
public class Error extends C8yData {
	/**
	 * Error type formatted as "&lt;&lt;resource type&gt;&gt;/&lt;&lt;error
	 * name&gt;&gt;". For example, an object not found in the inventory is
	 * reported as "inventory/notFound".
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String error;
	
	/**
	 * Short text description of the error
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String message;
	
	/**
	 * URL to an error description on the Internet.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String info;
	
	/**
	 * Error details. Only available in DEBUG mode.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public ErrorDetails details;
	
}

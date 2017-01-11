package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.ExternalId;

/**
 * ExternalIDCollection class
 * This source is machine-generated.
 */
public class ExternalIDCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of external IDs, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public ExternalId externalIds;
	
	/**
	 * Link to a potential previous page of external IDs.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of external IDs.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

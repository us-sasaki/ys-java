package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.ManagedObject;
import com.ntt.tc.data.Statistics;

/**
 * GET /inventory/binaries ‚ÌƒŒƒXƒ|ƒ“ƒX
 */
public class BinariesResp extends C8yData {
	/**
	 * Link to this resource.
	 */
	public String self;
	
	/**
	 * List of managed objects
	 * @see	com.ntt.tc.data.ManagedObject
	 */
	public ManagedObject[] managedObjects;
	
	/**
	 * Information about paging statistics.
	 */
	public Statistics statistics;
	
	/**
	 * Link to a potential previous page of managed objects.
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of managed objects.
	 */
	public String next;
}

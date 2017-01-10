package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.ManagedObject;
import com.ntt.tc.data.PagingStatistics;

public class ManagedObjectCollection extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * List of managed objects, see below.
	 *
	 * Occurs : 0..n
	 */
	public ManagedObject managedObjects;
	
	/**
	 * Information about paging statistics.
	 *
	 * Occurs : 1
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of managed objects.
	 *
	 * Occurs : 0..1
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of managed objects.
	 *
	 * Occurs : 0..1
	 */
	public String next;
	
}

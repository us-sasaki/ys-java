package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ManagedObject;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * ManagedObjectCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class ManagedObjectCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of managed objects, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public ManagedObject[] managedObjects;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of managed objects.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of managed objects.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ManagedObjectCollection;

/**
 * InventoryAPI class
 * This source is machine-generated.
 */
public class InventoryAPI extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Collection of all managed objects.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public ManagedObjectCollection managedObjects;
	
	/**
	 * Read-only collection of all managed objects of a particular type
	 * (placeholder {type}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String managedObjectsForType;
	
	/**
	 * Read-only collection of all managed objects with a particular fragment
	 * type or capability (placeholder {fragmentType}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String managedObjectsForFragmentType;
	
	/**
	 * Read-only collection of managed objects fetched for a given list of ids
	 * (placeholder {ids}),for example "?ids=41,43,68".
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String managedObjectsForListOfIds;
	
	/**
	 * Read-only collection of managed objects containing a text value starting
	 * with the given text (placeholder {text}). Text value is any alphanumeric
	 * string starting with a latin letter (A-Z or a-z).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String managedObjectsForText;
	
}

package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.ManagedObjectCollection;
import com.ntt.tc.data.ManagedObjectCollectionURI-Template;

public class InventoryAPI extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * Collection of all managed objects.
	 *
	 * Occurs : 1
	 */
	public ManagedObjectCollection managedObjects;
	
	/**
	 * Read-only collection of all managed objects of a particular type
	 * (placeholder {type}).
	 *
	 * Occurs : 1
	 */
	public ManagedObjectCollectionURI-Template managedObjectsForType;
	
	/**
	 * Read-only collection of all managed objects with a particular fragment
	 * type or capability (placeholder {fragmentType}).
	 *
	 * Occurs : 1
	 */
	public ManagedObjectCollectionURI-Template managedObjectsForFragmentType;
	
	/**
	 * Read-only collection of managed objects fetched for a given list of ids
	 * (placeholder {ids}),for example "?ids=41,43,68".
	 *
	 * Occurs : 1
	 */
	public ManagedObjectCollectionURI-Template managedObjectsForListOfIds;
	
	/**
	 * Read-only collection of managed objects containing a text value starting
	 * with the given text (placeholder {text}). Text value is any alphanumeric
	 * string starting with a latin letter (A-Z or a-z).
	 *
	 * Occurs : 1
	 */
	public ManagedObjectCollectionURI-Template managedObjectsForText;
	
}

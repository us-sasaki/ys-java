package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TimeStamp;
import com.ntt.tc.data.ManagedObjectReferenceCollection;

public class ManagedObject extends C8yData {
	/**
	 * Unique identifier of the object, automatically allocated when the object
	 * is created (see above).
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String self;
	
	/**
	 * The most specific type of the managed object as fully qualified
	 * Java-style type name, dots replaced by underscores.
	 *
	 * Occurs : 0..1
	 * PUT/POST : Optional
	 */
	public String type;
	
	/**
	 * Human-readable name that is used for representing the object in user
	 * interfaces.
	 *
	 * Occurs : 0..1
	 * PUT/POST : Optional
	 */
	public String name;
	
	/**
	 * Additional properties associated with the specific ManagedObject.
	 *
	 * Occurs : 0..n
	 * PUT/POST : Optional
	 */
	public Object *;
	
	/**
	 * The time when the object was last updated.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public TimeStamp lastUpdated;
	
	/**
	 * A collection of references to child devices.
	 *
	 * Occurs : 0..1
	 * PUT/POST : No
	 */
	public ManagedObjectReferenceCollection childDevices;
	
	/**
	 * A collection of references to child assets.
	 *
	 * Occurs : 0..1
	 * PUT/POST : No
	 */
	public ManagedObjectReferenceCollection childAssets;
	
	/**
	 * A collection of references to device parent objects.
	 *
	 * Occurs : 0..1
	 * PUT/POST : No
	 */
	public ManagedObjectReferenceCollection deviceParents;
	
	/**
	 * A collection of references to asset parent objects.
	 *
	 * Occurs : 0..1
	 * PUT/POST : No
	 */
	public ManagedObjectReferenceCollection assetParents;
	
}

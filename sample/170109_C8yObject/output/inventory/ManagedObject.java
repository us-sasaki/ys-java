package inventory;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.inventory.ManagedObjectReferenceCollection;

/**
 * ManagedObject class
 * This source is machine-generated from c8y-markdown docs.
 */
public class ManagedObject extends C8yData {
	/**
	 * Unique identifier of the object, automatically allocated when the object
	 * is created (see above).
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String self;
	
	/**
	 * The most specific type of the managed object as fully qualified
	 * Java-style type name, dots replaced by underscores.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : Optional
	 * </pre>
	 */
	public String type;
	
	/**
	 * Human-readable name that is used for representing the object in user
	 * interfaces.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : Optional
	 * </pre>
	 */
	public String name;
	
	/**
	 * Additional properties associated with the specific ManagedObject.
	 * <pre>
	 * Occurs : 0..n
	 * PUT/POST : Optional
	 * </pre>
	 */
	//omitted since type, field equals "*"
	
	/**
	 * The time when the object was last updated.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public TC_Date lastUpdated;
	
	/**
	 * A collection of references to child devices.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public ManagedObjectReferenceCollection childDevices;
	
	/**
	 * A collection of references to child assets.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public ManagedObjectReferenceCollection childAssets;
	
	/**
	 * A collection of references to device parent objects.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public ManagedObjectReferenceCollection deviceParents;
	
	/**
	 * A collection of references to asset parent objects.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public ManagedObjectReferenceCollection assetParents;
	
}

package binaries;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ManagedObject;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * BinariesCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class BinariesCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of binary objects, see below.
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
	 * Link to a potential previous page of binary objects.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of binary objects.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

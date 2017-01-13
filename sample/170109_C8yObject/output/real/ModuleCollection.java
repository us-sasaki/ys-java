package real;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * ModuleCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class ModuleCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of modules, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Collection[] modules;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of modules.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of modules.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

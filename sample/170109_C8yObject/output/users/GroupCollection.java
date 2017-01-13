package users;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.users.Group;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * GroupCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class GroupCollection extends C8yData {
	/**
	 * Link to this Resource
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of Groups
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Group[] groups;
	
	/**
	 * Information about the paging statistics
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a possible previous page with additional groups
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a possible next page with additional groups
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

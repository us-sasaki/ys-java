package tenants;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.tenants.UsageStatistics;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * TenantUsageStatisticsCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class TenantUsageStatisticsCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of usage statistics, see above.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public UsageStatistics[] usageStatistics;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of tenants.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of tenants.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

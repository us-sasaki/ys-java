package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.Statistics;

/**
 * GET /tenant/statistics のレスポンス
 */
public class TenantStatisticsResp extends C8yData {
	/**
	 * Link to this resource.
	 */
	public String self;
	
	/**
	 * List of usage statistics, see above.
	 * @see	com.ntt.tc.data.rest.TenantUsageStatistics
	 */
	public TenantUsageStatistics[] usageStatistics;
	
	/**
	 * Information about paging statistics.
	 */
	public Statistics statistics;
	
	/**
	 * Link to a potential previous page of measurements.
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of measurements.
	 */
	public String next;
}

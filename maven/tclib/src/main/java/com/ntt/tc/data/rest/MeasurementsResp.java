package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.Measurement;
import com.ntt.tc.data.Statistics;

/**
 * GET /measurement/measurements のレスポンス
 */
public class MeasurementsResp extends C8yData {
	/**
	 * Link to this resource.
	 */
	public String self;
	
	/**
	 * List of measurements
	 * @see	com.ntt.tc.data.Measurement
	 */
	public Measurement[] measurements;
	
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

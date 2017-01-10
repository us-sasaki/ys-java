package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.Measurement;
import com.ntt.tc.data.PagingStatistics;

public class MeasurementCollection extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * List of measurements, see below.
	 *
	 * Occurs : 0..n
	 */
	public Measurement measurements;
	
	/**
	 * Information about paging statistics.
	 *
	 * Occurs : 1
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of measurements.
	 *
	 * Occurs : 0..1
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of measurements.
	 *
	 * Occurs : 0..1
	 */
	public String next;
	
}

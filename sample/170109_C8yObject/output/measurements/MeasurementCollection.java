package com.ntt.tc.data.measurements;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.measurements.Measurement;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * MeasurementCollection class
 * This source is machine-generated.
 */
public class MeasurementCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of measurements, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Measurement[] measurements;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of measurements.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of measurements.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

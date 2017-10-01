package com.ntt.tc.data.measurements;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.measurements.Measurement;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * MeasurementCollection class
 * This source is machine-generated from c8y-markdown docs.
 * 内部的に List で保持し、add メソッドを追加
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
	protected List<Measurement> measurements;
	
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
	
/*-------------
 * constructor
 */
	public MeasurementCollection() {
		measurements = new ArrayList<Measurement>();
	}
	
/*------------------
 * instance methods
 */
	public Measurement[] getMeasurements() {
		return measurements.toArray(new Measurement[0]);
	}
	
	public void setMeasurements(Measurement[] measurements) {
		this.measurements.addAll(Arrays.asList(measurements));
	}
	
	public void add(Measurement measurement) {
		measurements.add(measurement);
	}
	
}

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
	 * toJson() などで get が連続的に呼ばれた際、高速化のため toArray
	 * を呼ばないようキャッシュしておく。
	 * measurements が変更されたときには、null とする必要がある。
	 */
	protected Measurement[] measurementsCache;
	
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
	/**
	 * 空の MeasurementCollection を作成します。
	 */
	public MeasurementCollection() {
		measurements = new ArrayList<Measurement>();
		measurementsCache = null;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * JSON化のプロパティ定義(get)メソッドです。
	 */
	public Measurement[] getMeasurements() {
		if (measurementsCache == null)
			measurementsCache = measurements.toArray(new Measurement[0]);
		return measurementsCache;
	}
	
	/**
	 * JSON化のプロパティ定義(set)メソッドです。
	 */
	public void setMeasurements(Measurement[] measurements) {
		this.measurements.clear();
		this.measurements.addAll(Arrays.asList(measurements));
		measurementsCache = null;
	}
	
	/**
	 * 指定された Measurement を追加します。
	 *
	 * @param		measurement		追加する measurement
	 */
	public void add(Measurement measurement) {
		measurements.add(measurement);
		measurementsCache = null;
	}
}

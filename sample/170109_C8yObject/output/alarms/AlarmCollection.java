package com.ntt.tc.data.alarms;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.alarms.Alarm;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * AlarmCollection class
 * This source is machine-generated.
 */
public class AlarmCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of alarms, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Alarm[] alarms;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of alarms.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of alarms.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

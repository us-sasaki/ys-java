package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.Alarm;
import com.ntt.tc.data.PagingStatistics;

public class AlarmCollection extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * List of alarms, see below.
	 *
	 * Occurs : 0..n
	 */
	public Alarm alarms;
	
	/**
	 * Information about paging statistics.
	 *
	 * Occurs : 1
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of alarms.
	 *
	 * Occurs : 0..1
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of alarms.
	 *
	 * Occurs : 0..1
	 */
	public String next;
	
}

package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.AlarmCollection;
import com.ntt.tc.data.String;

public class AlarmAPI extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * Collection of all alarms.
	 *
	 * Occurs : 1
	 */
	public AlarmCollection alarms;
	
	/**
	 * Read-only collection of all alarms in a particular status (placeholder
	 * {status}, see "Alarm" resource below for permitted values).
	 *
	 * Occurs : 1
	 */
	public String alarmsForStatus;
	
	/**
	 * Read-only collection of all alarms for a particular source object
	 * (placeholder {source}, unique ID of an object in the inventory).
	 *
	 * Occurs : 1
	 */
	public String alarmsForSource;
	
	/**
	 * Read-only collection of all alarms for a particular source object in a
	 * particular status (placeholder {source} and {status}).
	 *
	 * Occurs : 1
	 */
	public String alarmsForSourceAndStatus;
	
	/**
	 * Read-only collection of all alarms for a particular time range
	 * (placeholder {dateFrom} and {dateTo}).
	 *
	 * Occurs : 1
	 */
	public String alarmsForTime;
	
	/**
	 * Read-only collection of all alarms for a particular status and time
	 * range (placeholder {status}, {dateFrom} and {dateTo}).
	 *
	 * Occurs : 1
	 */
	public String alarmsForStatusAndTime;
	
	/**
	 * Read-only collection of all alarms for a particular source and time
	 * range (placeholder {source}, {dateFrom} and {dateTo};).
	 *
	 * Occurs : 1
	 */
	public String alarmsForSourceAndTime;
	
	/**
	 * Read-only collection of all alarms for a particular source, status and
	 * time range (placeholder {source}, {status}, {dateFrom} and {dateTo};).
	 *
	 * Occurs : 1
	 */
	public String alarmsForSourceAndStatusAndTime;
	
}

package com.ntt.tc.data.alarms;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.alarms.AlarmCollection;

/**
 * AlarmAPI class
 * This source is machine-generated from c8y-markdown docs.
 */
public class AlarmAPI extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Collection of all alarms.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public AlarmCollection alarms;
	
	/**
	 * Read-only collection of all alarms in a particular status (placeholder
	 * {status}, see "Alarm" resource below for permitted values).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String alarmsForStatus;
	
	/**
	 * Read-only collection of all alarms for a particular source object
	 * (placeholder {source}, unique ID of an object in the inventory).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String alarmsForSource;
	
	/**
	 * Read-only collection of all alarms for a particular source object in a
	 * particular status (placeholder {source} and {status}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String alarmsForSourceAndStatus;
	
	/**
	 * Read-only collection of all alarms for a particular time range
	 * (placeholderﾂ?{dateFrom} andﾂ?{dateTo}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String alarmsForTime;
	
	/**
	 * Read-only collection of all alarms for a particular status and time
	 * range (placeholderﾂ?{status},ﾂ?{dateFrom} andﾂ?{dateTo}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String alarmsForStatusAndTime;
	
	/**
	 * Read-only collection of all alarms for a particular source and time
	 * range (placeholderﾂ?{source},ﾂ?{dateFrom} andﾂ?{dateTo};).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String alarmsForSourceAndTime;
	
	/**
	 * Read-only collection of all alarms for a particular source, status and
	 * time range (placeholderﾂ?{source},ﾂ?{status},ﾂ?{dateFrom}
	 * andﾂ?{dateTo};).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String alarmsForSourceAndStatusAndTime;
	
}

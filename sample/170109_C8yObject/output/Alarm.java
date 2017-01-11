package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * Alarm class
 * This source is machine-generated.
 */
public class Alarm extends C8yData {
	/**
	 * Uniquely identifies an alarm.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String self;
	
	/**
	 * Time when alarm was created in the database.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String creationTime;
	
	/**
	 * Identifies the type of this alarm, e.g.,
	 * "com\_cumulocity\_events\_TamperEvent".
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String type;
	
	/**
	 * Time of the alarm.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String time;
	
	/**
	 * Text description of the alarm.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String text;
	
	/**
	 * The ManagedObject that the alarm originated from, as object containing
	 * the "id" property.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public ManagedObject source;
	
	/**
	 * The status of the alarm: ACTIVE, ACKNOWLEDGED or CLEARED. If status was
	 * not appeared, new alarm will have status ACTIVE. Must be upper-case.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : POST: Optional PUT: Optional
	 * </pre>
	 */
	public String status;
	
	/**
	 * The severity of the alarm: CRITICAL, MAJOR, MINOR or WARNING. Must be
	 * upper-case.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: Optional
	 * </pre>
	 */
	public String severity;
	
	/**
	 * The number of times this alarm has been sent.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public Long count;
	
	/**
	 * The first time that this alarm occurred (i.e., where "count" was 1).
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String firstOccurenceTime;
	
	/**
	 * History of modifications tracing property changes.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public AuditRecordCollection history;
	
	/**
	 * Additional properties of the event.
	 * <pre>
	 * Occurs : 0..n
	 * PUT/POST :  
	 * </pre>
	 */
	//omitted since type, field equals "*"
	
}

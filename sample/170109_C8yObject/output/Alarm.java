package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.ManagedObject;
import com.ntt.tc.data.AuditRecordCollection;

public class Alarm extends C8yData {
	/**
	 * Uniquely identifies an alarm.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String self;
	
	/**
	 * Time when alarm was created in the database.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String creationTime;
	
	/**
	 * Identifies the type of this alarm, e.g.,
	 * "com_cumulocity_events_TamperEvent".
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public String type;
	
	/**
	 * Time of the alarm.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public String time;
	
	/**
	 * Text description of the alarm.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public String text;
	
	/**
	 * The ManagedObject that the alarm originated from, as object containing
	 * the "id" property.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public ManagedObject source;
	
	/**
	 * The status of the alarm: ACTIVE, ACKNOWLEDGED or CLEARED. If status was
	 * not appeared, new alarm will have status ACTIVE. Must be upper-case.
	 *
	 * Occurs : 0..1
	 * PUT/POST : POST: Optional PUT: Optional
	 */
	public String status;
	
	/**
	 * The severity of the alarm: CRITICAL, MAJOR, MINOR or WARNING. Must be
	 * upper-case.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: Optional
	 */
	public String severity;
	
	/**
	 * The number of times this alarm has been sent.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public Long count;
	
	/**
	 * The first time that this alarm occurred (i.e., where "count" was 1).
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String firstOccurenceTime;
	
	/**
	 * History of modifications tracing property changes.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public AuditRecordCollection history;
	
	/**
	 * Additional properties of the event.
	 *
	 * Occurs : 0..n
	 * PUT/POST : -
	 */
	public Object *;
	
}

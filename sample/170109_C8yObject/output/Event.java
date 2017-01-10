package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.ManagedObject;

public class Event extends C8yData {
	/**
	 * Uniquely identifies an event.
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
	 * Time when event was created in the database.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String creationTime;
	
	/**
	 * Identifies the type of this event.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public String type;
	
	/**
	 * Time of the event.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public String time;
	
	/**
	 * Text description of the event.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: Optional
	 */
	public String text;
	
	/**
	 * The ManagedObject that the event originated from, as object containing
	 * properties "id", "self", "name", and "type".
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public ManagedObject source;
	
	/**
	 * Additional properties of the event.
	 *
	 * Occurs : 0..n
	 * PUT/POST : POST: Optional PUT: Optional
	 */
	public Object *;
	
}

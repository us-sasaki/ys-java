package com.ntt.tc.data.events;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.inventory.ManagedObject;
import abdom.data.json.JsonObject;

/**
 * Event class, 
 * post Event の時に使用できるコンストラクタを追加。
 */
public class Event extends C8yData {
	/**
	 * Uniquely identifies an event.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Time when event was created in the database.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public TC_Date creationTime;
	
	/**
	 * Identifies the type of this event.
	 * "com_cumulocity_modek_DoorSensorEvent" のような型
	 * CREATE 時必須
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String type;
	
	/**
	 * Time of the event.
	 * <pre>
	 * Occurs : 1
	 * CREATE 時必須
	 * </pre>
	 */
	public TC_Date time;
	
	/**
	 * Text description of the event.
	 * <pre>
	 * Occurs : 1
	 * CREATE 時必須
	 * </pre>
	 */
	public String text;
	
	/**
	 * The ManagedObject that the event originated from, as object containing
	 * properties "id", "self", "name", and "type".
	 * <pre>
	 * Occurs : 1
	 * CREATE 時必須
	 * </pre>
	 */
	public ManagedObject source;
	
	/**
	 * Additional properties of the event.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	//This field has omitted because of type and field = "*"
	
/*-------------
 * Constructor
 */
	public Event() {
		super();
	}
	
	public Event(ManagedObject mo, String type, String text) {
		super();
		this.time = new TC_Date();
		this.type = type;
		this.text = text;
		this.source = new ManagedObject();
		this.source.id = mo.id;
		this.source.name = mo.name;
		this.source.type = mo.type;
	}
}

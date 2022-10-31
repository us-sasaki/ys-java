package com.ntt.tc.data.events;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.inventory.ID;
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
	public ID source;
	
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
	
	/**
	 * 指定された ManagedObject をソースとする Event を生成します。
	 * Event POST に必須のデータのみをコピーし、通信量を節約します。
	 *
	 * @param		mo		sourceとなる Managed object
	 * @param		type	Event 型(必須とされているが null も可)
	 * @param		text	イベントの説明
	 */
	public Event(ManagedObject mo, String type, String text) {
		super();
		this.time = new TC_Date();
		this.type = type;
		this.text = text;
		this.source = new ID();
		if (mo.id == null)
				throw new NullPointerException("指定された ManagedObject の id に null を指定することはできません:"+mo);
		this.source.id = mo.id;
		if (mo.name != null) this.source.set("name", mo.name);
		if (mo.type != null) this.source.set("type", mo.type);
	}
	
	/**
	 * 指定された ManagedObject id をソースとする Event を生成します。
	 * Event POST に必須のデータのみをコピーし、通信量を節約します。
	 *
	 * @param		source	source となる Managed object の id
	 * @param		type	Event 型(必須とされているが null も可)
	 * @param		text	イベントの説明
	 */
	public Event(String source, String type, String text) {
		super();
		this.time = new TC_Date();
		this.type = type;
		this.text = text;
		this.source = new ID();
		this.source.id = source;
	}
}

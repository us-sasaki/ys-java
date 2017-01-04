package com.ntt.tc.data;

import com.ntt.tc.data.Id;
import com.ntt.tc.data.sensor.C8y_SignalStrength;

/**
 * 単一の event.
 * POST /event/events の要求、応答にも利用される。
 */
public class Event extends C8yData {
	public String	id;
	public String	self;
	/** データベースにイベントが生成された時間 */
	public TC_Date	creationTime;
	/**
	 * "com_cumulocity_modek_DoorSensorEvent" のような型
	 * CREATE 時必須
	 */
	public String	type;
	/**
	 * CREATE 時必須
	 */
	public TC_Date	time;
	/**
	 * CREATE 時必須
	 */
	public String	text;
	/**
	 * CREATE 時必須
	 */
	public EventSource	source;
	
	public C8y_Position	c8y_Position;
	
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
		this.source = new EventSource();
		this.source.id = mo.id;
		this.source.name = mo.name;
		this.source.type = mo.type;
	}
}

package com.ntt.tc.data;

import com.ntt.tc.data.Id;
import com.ntt.tc.data.sensor.C8y_SignalStrength;

/**
 * �P��� event.
 * POST /event/events �̗v���A�����ɂ����p�����B
 */
public class Event extends C8yData {
	public String	id;
	public String	self;
	/** �f�[�^�x�[�X�ɃC�x���g���������ꂽ���� */
	public TC_Date	creationTime;
	/**
	 * "com_cumulocity_modek_DoorSensorEvent" �̂悤�Ȍ^
	 * CREATE ���K�{
	 */
	public String	type;
	/**
	 * CREATE ���K�{
	 */
	public TC_Date	time;
	/**
	 * CREATE ���K�{
	 */
	public String	text;
	/**
	 * CREATE ���K�{
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

package com.ntt.tc.data;

/**
 * Event �̏ꍇ�� ID �� ID �݂̂łȂ��Aname, type, self ���K�v�Ȃ���
 * �ʃN���X�Ƃ��Ď���
 */
public class EventSource extends Id {
	public String	self;
	public String	name;
	public String	type;
	
/*-------------
 * Constructor
 */
	public EventSource() {
		super();
	}
	public EventSource(ManagedObject mo) {
		super();
		id		= mo.id;
		name	= mo.name;
		type	= mo.type;
	}
	
	public EventSource(String id, String name, String type) {
		super();
		this.id		= id;
		this.name	= name;
		this.type	= type;
	}
}

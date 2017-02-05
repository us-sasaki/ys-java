package com.ntt.tc.data;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.object.IllegalFieldTypeException;

public class Boolean extends C8yValue {
	protected boolean value;
	
	public void fill(JsonType arg) {
		if (arg.getType() != JsonType.TYPE_BOOLEAN)
			throw new IllegalFieldTypeException();
			
		value = arg.getValue().equals("true");
	}
	
	public JsonType toJson() {
		return new JsonValue(value);
	}
	
	public Boolean() {
	}
	
	public Boolean(boolean value) {
		this.value = value;
	}
	
}

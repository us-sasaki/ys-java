package com.ntt.tc.data;

import abdom.data.json.object.JData;
import com.ntt.tc.data.sensor.Value;

public class C8y_Battery extends C8yData {
	public C8y_Battery() {
		super();
	}
	/**
	 * c8y_Battery shows the current battery fill level.
	 * It is used as part of a measurement.
	 * unit:"%"
	 */
	public Value level;
	
	public C8y_Battery(double value) {
		this.level = new Value(value, "%");
	}
}

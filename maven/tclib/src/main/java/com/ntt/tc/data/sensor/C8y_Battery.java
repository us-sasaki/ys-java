package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

/**
 * c8y_Battery shows the current battery fill level.
 * It is used as part of a measurement.
 * inventory / ManagedObject では Object * 扱いで、
 * Device Management Library に記載されるフィールド。
 */
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

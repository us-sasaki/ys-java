package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

public class C8y_TemperatureMeasurement extends C8yData {
	/**
	 * Measured temperature. Units is "C"
	 */
	public Value T;
	
	public C8y_TemperatureMeasurement() {
		super();
		this.T = new Value(0d, "C");
	}
	
	public C8y_TemperatureMeasurement(double value) {
		super();
		this.T = new Value(value, "C");
	}
}

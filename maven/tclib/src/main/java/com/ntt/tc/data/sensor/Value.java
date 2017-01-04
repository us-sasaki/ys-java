package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

public class Value extends C8yData {
	public double value;
	public String unit;
	
	public Value() {
		super();
	}
	public Value(double value, String unit) {
		super();
		this.value	= value;
		this.unit	= unit;
	}
}

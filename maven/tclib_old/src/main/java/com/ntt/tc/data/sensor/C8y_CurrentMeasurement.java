package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

/**
 * A current sensor measures the current flowing through it.
 */
public class C8y_CurrentMeasurement extends C8yData {
	/**
	 * Current measurement
	 * "unit":"A"
	 */
	public Value current;
}

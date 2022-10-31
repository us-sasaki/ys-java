package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

public class C8y_MotionMeasurement extends C8yData {
	/**
	 * Boolean value indicating if motion has been detected
	 * (non-zero value) or not (zero value).
	 * "unit":"" and has fragment "type":"BOOLEAN"
	 */
	public Value motionDetected;
	
	/**
	 * Measured speed towards (+ve) or away (-ve) from the sensor.
	 * "unit":"km/h"
	 */
	public Value speed;
}

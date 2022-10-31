package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

/**
 * A distance sensor measures the distance between itself and the closest
 * object in certain direction.
 */
public class C8y_DistanceMeasurement extends C8yData {
	/**
	 * Distance measurement
	 * "unit":"mm"
	 */
	public Value distance;
}

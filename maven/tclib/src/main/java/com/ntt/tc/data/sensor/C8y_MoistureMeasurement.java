package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

/**
 * There are three main measurements of moisture: absolute, relative and
 * specific. Absolute moisture is the absolute water content of a substance.
 * Relative moisture, expressed as a percent, measures the current absolute
 * moisture relative to the maximum for that temperature. Specific humidity
 * is a ratio of the water vapour content of the mixture to the total
 * substance content on a mass basis.
 */
public class C8y_MoistureMeasurement extends C8yData {
	/**
	 * Relative Moisture measurement
	 * "unit":"%"
	 */
	public Value moisture;
}

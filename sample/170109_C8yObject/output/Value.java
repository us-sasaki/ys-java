package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

public class Value extends C8yData {
	/**
	 * The value of the individual measurement.
	 *
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 */
	public Double value;
	
	/**
	 * The unit of the measurement, such as "Wh" or "C".
	 *
	 * Occurs : 1
	 * PUT/POST : Optional
	 */
	public String unit;
	
}

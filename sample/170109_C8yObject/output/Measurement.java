package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.ManagedObject;
import com.ntt.tc.data.*;

public class Measurement extends C8yData {
	/**
	 * Uniquely identifies a measurement.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String self;
	
	/**
	 * Time of the measurement.
	 *
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 */
	public String time;
	
	/**
	 * The most specific type of this entire measurement.
	 *
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 */
	public String type;
	
	/**
	 * The ManagedObject which is the source of this measurement, as object
	 * containing the properties "id" and "self".
	 *
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 */
	public ManagedObject source;
	
	/**
	 * List of measurement fragments.
	 *
	 * Occurs : 0..n
	 * PUT/POST : Optional
	 */
	public * *;
	
}

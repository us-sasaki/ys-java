package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * NewDeviceRequest class
 * This source is machine-generated.
 */
public class NewDeviceRequest extends C8yData {
	/**
	 * Device identifier. Max: 1000 characters. E.g. IMEI
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String id;
	
	/**
	 * Status of registration, one of: WAITING\_FOR\_CONNECTION,
	 * PENDING\_ACCEPTANCE, ACCEPTED
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String status;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
}

package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

public class NewDeviceRequest extends C8yData {
	/**
	 * Device identifier. Max: 1000 characters. E.g. IMEI
	 *
	 * Occurs : 1
	 */
	public String id;
	
	/**
	 * Status of registration, one of: WAITING_FOR_CONNECTION,
	 * PENDING_ACCEPTANCE, ACCEPTED
	 *
	 * Occurs : 1
	 */
	public String status;
	
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
}

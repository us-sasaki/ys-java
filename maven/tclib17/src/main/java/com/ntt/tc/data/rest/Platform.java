package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.InventoryAPI;
import com.ntt.tc.data.identity.IdentityAPI;
import com.ntt.tc.data.events.EventAPI;
import com.ntt.tc.data.measurements.MeasurementAPI;
import com.ntt.tc.data.auditing.AuditAPI;
import com.ntt.tc.data.alarms.AlarmAPI;
import com.ntt.tc.data.users.UserAPI;
import com.ntt.tc.data.device.DeviceControlAPI;

/**
 * Platform class
 * This source is machine-generated from c8y-markdown docs.
 */
public class Platform extends C8yData {
	/**
	 * Link to this Resource
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * See [inventory](/guides/reference/inventory) interface.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public InventoryAPI inventory;
	
	/**
	 * See [identity](/guides/reference/identity) interface.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public IdentityAPI identity;
	
	/**
	 * See [event](/guides/reference/events) interface.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public EventAPI event;
	
	/**
	 * See [measurement](/guides/reference/measurements) interface.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public MeasurementAPI measurement;
	
	/**
	 * See [auditing](/guides/reference/auditing) interface.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public AuditAPI audit;
	
	/**
	 * See [alarm](/guides/reference/alarms) interface.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public AlarmAPI alarm;
	
	/**
	 * See [user](/guides/reference/users) interface.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public UserAPI user;
	
	/**
	 * See [device control](/guides/reference/device-control) interface.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public DeviceControlAPI deviceControl;
	
}

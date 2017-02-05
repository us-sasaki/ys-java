package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;

/**
 * c8y_SoftwareList is a List of software entries that define the name,
 * version and url for the software.
 * In the inventory, "c8y_SoftwareList" represents the currently installed
 * software components on the device.
 */
public class C8y_SoftwareList extends C8yData {
	/**
	 * Name of the software.
	 */
	public String name;
	/**
	 * A version identifier of the software.
	 */
	public String version;
	/**
	 * A location to download the software from.
	 */
	public String url;
}

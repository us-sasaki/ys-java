package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;

/**
 * c8y_Configuration permits a text-based configuration of the device.
 * Most devices support a textual system configuration file that can be
 * presented and edited using this mechanism. In the inventory,
 * "c8y_Configuration" represents the currently active configuration on
 * the device. As part of an operation, "c8y_Configuration" requests the
 * device to make the transmitted configuration the currently active one.
 * To enable configuration through the user interface, add
 * "c8y_Configuration" to the list of supported operations as described
 * above.
 * inventory / ManagedObject では Object * 扱いで、
 * Device Management Library に記載されるフィールド。
 */
public class C8y_Configuration extends C8yData {
	/**
	 * A text in a device-specific format, representing the configuration of
	 * the device.
	 */
	public String config;
}

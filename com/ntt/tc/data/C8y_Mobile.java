package com.ntt.tc.data;

import com.ntt.tc.data.sensor.Value;

/**
 * c8y_Mobile holds the information about the mobile connection status
 * (e.g. cell information) and the sim card (e.g. ICCID) of the device.
 * Whenever the status changes the fragment in the device should be updated.
 * The assumption for not moving devices is that these values rarely change.
 * For more frequently changing mobile information (e.g. signal strength)
 * a measurement can be used.
 */
public class C8y_Mobile extends C8yData {
	public String imsi;
	public String imei;
	public String currentOperator;
	public String currentBand;
	public String connType;
	
	/**
	 * RSSI measurement
	 * "unit":dBm
	 */
	public Value rssi;
	public String ecn0;
	public String rcsp;
	public String mnc;
	public String lac;
	public String cellId;
	public String msisdn;
	public String iccid;
}

package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;
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
	/**
	 * The equipment identifier (IMEI) of the modem in the device.
	 */
	public String imei;
	public String currentOperator;
	public String currentBand;
	public String connType;
	
	/**
	 * RSSI measurement
	 * "unit":dBm
	 * 文字列として value が与えられる場合があるため、extra に変更
	 * (2018/12/25, O社のManagedObject で発見、多分NTC6200)
	 */
	//public Value rssi;
	
	public String ecn0;
	public String rcsp;
	public String mnc;
	public String lac;
	
	/**
	 * The identifier of the cell in the mobile network that the device is
	 * currently connected with.
	 */
	public String cellId;
	public String msisdn;
	/**
	 * The identifier of the SIM card that is currently in the device (often
	 * printed on the card).
	 */
	public String iccid;
}

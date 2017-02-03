package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;

/**
 * c8y_Position reports the geographical location of an asset in terms of
 * latitude, longitude and altitude. Altitude is given in meters. To report
 * the current location of an asset or a device, "c8y_Position" is added to
 * the managed object representing the asset or device. To trace the position
 * of an asset or a device, "c8y_Position" is sent as part of an event of
 * type "c8y_LocationUpdate".
 * コンストラクタを追加
 *
 * @version		February 3, 2017
 */
public class C8y_Position extends C8yData {
	/** 高度(m) */
	public double alt;
	/** 経度(度) */
	public double lng;
	/** 緯度(度) */
	public double lat;
	
	/**
	 * e.g. "TELIC"
	 * Properties "trackingProtocol" and "reportReason" are used by tracker
	 * agent and describes tracking context of positioning report: why the
	 * report was sent and in which protocol.
	 */
	public String trackingProtocol;
	
	/**
	 * e.g. "Time Event"
	 */
	public String reportReason;
	
/*-------------
 * constructor
 */
	public C8y_Position() {
		super();
	}
	
	public C8y_Position(double lat, double lng, double alt) {
		this(lat, lng, alt, "-", "-");
	}
	
	public C8y_Position(double  lat, double lng, double alt, String trackingProtocol, String reportReason) {
		this.alt = alt;
		this.lat = lat;
		this.lng = lng;
		this.trackingProtocol = trackingProtocol;
		this.reportReason = reportReason;
	}
}

package com.ntt.tc.data;

import com.ntt.tc.data.Id;
import com.ntt.tc.data.sensor.C8y_SignalStrength;
import com.ntt.tc.data.sensor.C8y_TemperatureMeasurement;
import com.ntt.tc.data.C8y_Battery;


/**
 * 単一の measurement.
 * POST /measurement/measurements の要求、応答にも利用される。
 */
public class Measurement extends C8yData {
	public String id;
	public String self;
	public Id source;
	public TC_Date time;
	public String type;
	public C8y_SignalStrength c8y_SignalStrength;
	public C8y_TemperatureMeasurement c8y_TemperatureMeasurement;
	public C8y_Battery c8y_Battery;
	
	public Measurement() {
		super();
	}
	public Measurement(ManagedObject mo, String type) {
		super();
		source = new Id();
		source.id = mo.id;
		time = new TC_Date();
		this.type = type;
		
	}
}

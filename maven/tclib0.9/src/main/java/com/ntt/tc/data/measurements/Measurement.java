package com.ntt.tc.data.measurements;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.inventory.ManagedObject;
import com.ntt.tc.data.sensor.C8y_Battery;
import com.ntt.tc.data.sensor.C8y_AccelerationMeasurement;
import com.ntt.tc.data.sensor.C8y_CurrentMeasurement;
import com.ntt.tc.data.sensor.C8y_DistanceMeasurement;
import com.ntt.tc.data.sensor.C8y_HumidityMeasurement;
import com.ntt.tc.data.sensor.C8y_LightMeasurement;
import com.ntt.tc.data.sensor.C8y_MoistureMeasurement;
import com.ntt.tc.data.sensor.C8y_MotionMeasurement;
import com.ntt.tc.data.sensor.C8y_SinglePhaseElectricityMeasurement;
import com.ntt.tc.data.sensor.C8y_TemperatureMeasurement;
import com.ntt.tc.data.sensor.C8y_ThreePhaseElectricityMeasurement;
import com.ntt.tc.data.sensor.C8y_VoltageMeasurement;
import com.ntt.tc.data.sensor.C8y_SignalStrength;

/**
 * Measurement class
 * 単一の measurement.
 * POST /measurement/measurements の要求、応答にも利用される。
 * インスタンス変数として fragment は c8y docs に定義しているものを
 * pre-defined としているが、他の fragment を定義、利用する場合、
 * putExtra() により追加すること。
 */
public class Measurement extends C8yData {
	/**
	 * Uniquely identifies a measurement.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String self;
	
	/**
	 * Time of the measurement.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 * </pre>
	 */
	public TC_Date time;
	
	/**
	 * The most specific type of this entire measurement.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 * </pre>
	 */
	public String type;
	
	/**
	 * The ManagedObject which is the source of this measurement, as object
	 * containing the properties "id" and "self".
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 * </pre>
	 */
	public ManagedObject source;
	
	/**
	 * List of measurement fragments.
	 * <pre>
	 * Occurs : 0..n
	 * PUT/POST : Optional
	 * </pre>
	 */
	//This field has omitted because of type and field = "*"
	
	// 以下 c8y 定義の fragment
	public C8y_AccelerationMeasurement c8y_AccelerationMeasurement;
	public C8y_CurrentMeasurement c8y_CurrentMeasurement;
	public C8y_DistanceMeasurement c8y_DistanceMeasurement;
	public C8y_HumidityMeasurement c8y_HumidityMeasurement;
	public C8y_LightMeasurement c8y_LightMeasurement;
	public C8y_MoistureMeasurement c8y_MoistureMeasurement;
	public C8y_MotionMeasurement c8y_MotionMeasurement;
	public C8y_SinglePhaseElectricityMeasurement c8y_SinglePhaseElectricityMeasurement;
	public C8y_TemperatureMeasurement c8y_TemperatureMeasurement;
	public C8y_ThreePhaseElectricityMeasurement c8y_ThreePhaseElectricityMeasurement;
	public C8y_VoltageMeasurement c8y_VoltageMeasurement;
	public C8y_SignalStrength c8y_SignalStrength;
	public C8y_Battery c8y_Battery;
	
/*-------------
 * constructor
 */
	public Measurement() {
		super();
	}
	
	/**
	 * Measurement のテンプレートを指定したパラメータで生成します。
	 * 実際の測定データは別途追加する必要があります。
	 *
	 * @param	mo		source となる managed object
	 * @param	type	measurement type
	 */
	public Measurement(ManagedObject mo, String type) {
		super();
		source = new ManagedObject();
		source.id = mo.id;
		time = new TC_Date();
		this.type = type;
		
	}
}

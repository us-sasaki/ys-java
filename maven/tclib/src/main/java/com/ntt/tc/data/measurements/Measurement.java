package com.ntt.tc.data.measurements;

import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.C8yFormatException;
import com.ntt.tc.data.inventory.ID;
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
 * fragment を定義、利用する場合、
 * put(), putExtra() や set() により追加すること。
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
	public ID source;
	
	/**
	 * List of measurement fragments.
	 * <pre>
	 * Occurs : 0..n
	 * PUT/POST : Optional
	 * </pre>
	 */
	//This field has omitted because of type and field = "*"
	
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
		this(mo.id, type);
	}
	
	/**
	 * Measurement のテンプレートを指定したパラメータで生成します。
	 * 実際の測定データは別途追加する必要があります。
	 *
	 * @param	source	source となる managed object id
	 * @param	type	measurement type
	 */
	public Measurement(String source, String type) {
		super();
		this.source = new ID();
		this.source.id = source;
		time = new TC_Date();
		this.type = type;
	}
	
/*-----------------
 * instance method
 */
	/**
	 * この Measurement に指定された fragment を追加します。
	 * fragment は プロパティ、extra のいずれでも設定できます。
	 * 単に、JData#set("fragment.measurementName.value", new JsonValue(2d))
	 * などとするのと同等です。
	 *
	 * @param	fragment	フラグメント名(c8y_TemperatureMeasurement など)
	 * @param	measurementName	メジャーメント名(T など)
	 * @param	value		メジャーメントの値
	 * @param	unit		メジャーメントの単位
	 */
	public void put(String fragment, String measurementName,
					double value, String unit) {
		int c = fragment.indexOf('.');
		if (c > 0)
			throw new C8yFormatException("fragment には . を含められません:"+fragment);
		c = measurementName.indexOf('.');
		if (c > 0)
			throw new C8yFormatException("measurementName には . を含められません:"+measurementName);
		put(fragment+"."+measurementName, value, unit);
	}
	
	/**
	 * この Measurement に指定された fragment を追加します。
	 * fragment は プロパティ、extra のいずれでも設定できます。
	 * 単に、JData#set("fragment.measurementName.value", new JsonValue(2d))
	 * などとするのと同等です。
	 *
	 * @param	measurementPath	メジャーメントのJSONパス
	 * 							(c8y_TemperatureMeasurement.T など)
	 * @param	value		メジャーメントの値
	 * @param	unit		メジャーメントの単位
	 */
	public void put(String measurementPath, double value, String unit) {
		int c1 = measurementPath.indexOf('.');
		if (c1 == -1)
			throw new C8yFormatException("measurementPath は fragment, シリーズの name を指定する必要があります。fragment.name の形で指定してください");
		int c2 = measurementPath.indexOf('.', c1+1);
		if (c2 != -1)
			throw new C8yFormatException("measurementPath は fragment, シリーズの name を指定する必要があります。fragment.name の形で指定してください");
		set(measurementPath+".value", new JsonValue(value));
		set(measurementPath+".unit", new JsonValue(unit));
	}
	
}

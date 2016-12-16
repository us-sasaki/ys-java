package com.ntt.tc.data;

import com.ntt.tc.data.Id;
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
import com.ntt.tc.data.C8y_Battery;

/**
 * �P��� measurement.
 * POST /measurement/measurements �̗v���A�����ɂ����p�����B
 * �C���X�^���X�ϐ��Ƃ��� fragment �� c8y docs �ɒ�`���Ă�����̂�
 * pre-defined �Ƃ��Ă��邪�A���� fragment ���`�A���p����ꍇ�A
 * putExtra() �ɂ��ǉ����邱�ƁB
 */
public class Measurement extends C8yData {
	public String id;
	public String self;
	public Id source;
	public TC_Date time;
	public String type;
	
	// �ȉ� c8y ��`�� fragment
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

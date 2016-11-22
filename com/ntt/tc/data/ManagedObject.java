package com.ntt.tc.data;

import abdom.data.json.JsonObject;

public class ManagedObject extends C8yData {
	public String id;
	public TC_Date lastUpdated;
	public String name;
	public String owner;
	public String self;
	public String type;
	public String[] c8y_SupportedOperations;
	public Assets assetParents;
	public Assets childAssets;
	public Assets deviceParents;
	public Assets childDevices;
	
	public C8y_Hardware c8y_Hardware;
	public C8y_Configuration c8y_Configuration;
	public C8y_Mobile c8y_Mobile;
	public C8y_Firmware c8y_Firmware;
	public C8y_Software c8y_Software;
	public C8y_Position c8y_Position;
	
	// { } の値しか持たないフィールド
	public JsonObject c8y_IsDevice;
	public JsonObject com_cumulocity_model_Agent;
	
	/**
	 * @see	com.ntt.tc.data.sensor.C8y_TemperatureMeasurement
	 */
	public JsonObject c8y_TemperatureSensor;
	/**
	 * @see	com.ntt.tc.data.sensor.C8y_MotionMeasurement
	 */
	public JsonObject c8y_MotionSensor;
	/**
	 * @see	com.ntt.tc.data.sensor.C8y_AccelerationMeasurement
	 */
	public JsonObject c8y_AccelerationSensor;
	
	/**
	 * @see	com.ntt.tc.data.sensor.C8y_LightMeasurement
	 */
	public JsonObject c8y_LightSensor;
	
	/**
	 * @see	com.ntt.tc.data.sensor.C8y_HumidityMeasurement
	 */
	public JsonObject c8y_HumiditySensor;
	
	/**
	 * @see	com.ntt.tc.data.sensor.C8y_MoistureMeasurement
	 */
	public JsonObject c8y_MoistureSensor;
	
	/**
	 * @see com.ntt.tc.data.sensor.C8y_DistanceMeasurement
	 */
	public JsonObject c8y_DistanceSensor;
	
	/**
	 * @see com.ntt.tc.data.sensor.C8y_SinglePhaseElectricityMeasurement
	 */
	public JsonObject c8y_SinglePhaseElectricitySensor;
	
	/**
	 * @see com.ntt.tc.data.sensor.C8y_ThreePhaseElectricityMeasurement
	 */
	public JsonObject c8y_ThreePhaseElectricitySensor;
	
	/**
	 * @see com.ntt.tc.data.sensor.C8y_CurrentMeasurement
	 */
	public JsonObject c8y_CurrentSensor;
	
	/**
	 * リファレンスでは書かれていない（忘れた？）
	 * @see com.ntt.tc.data.sensor.C8y_VoltageMeasurement
	 */
	public JsonObject c8y_VoltageSensor;
	
	/**
	 * この位置でいいのか？(一個のスイッチが ManagedObject の要素？)
	 */
//	public C8y_Relay c8y_Relay;
}

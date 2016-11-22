package com.ntt.tc.data.rest;

import abdom.data.json.JsonObject;
import abdom.data.json.object.JData;

import com.ntt.tc.data.Id;

/**
 * POST /event/events の要求、およびレスポンス
 */
public class EventResp extends JData {
	/**
	 * この Event の id
	 */
	public String id;
	
	/**
	 * この Event リソースへの URI
	 */
	public String self;
	
	/**
	 * イベント生成時間(デバイス側)。要求時必須。
	 */
	public String time;
	
	/**
	 * イベントのDB登録時間
	 */
	public String creationTime;
	
	/**
	 * com_cumulocity_model_DoorSensorEvent のような文字列。
	 * 要求時必須。
	 */
	public String type;
	
	/**
	 * Event の説明文です。要求時必須。
	 */
	public String text;
	
	/**
	 * イベントの発生元です。要求時必須。
	 */
	public Id source;
}

package com.ntt.tc.data;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.Id;
import com.ntt.tc.data.sensor.C8y_SignalStrength;

/**
 * 単一の alarm.
 * POST /alarm/alarms の要求、応答にも利用される。
 */
public class Alarm extends C8yData {
	/** アラームを一意に識別します */
	public String	id;
	/** このリソースへのリンク */
	public String	self;
	/** データベースにアラームが生成された時間 */
	public TC_Date	creationTime;
	/** アラームの型を識別します。e.g. "com_cumulocity_events_TamperEvent" */
	public String	type;
	/** アラームの時刻 */
	public TC_Date	time;
	/** アラームの説明文 */
	public String	text;
	/** アラームを生成したmanagedObjectのid フィールド */
	public Id		source;
	/**
	 * アラーム発生時のステータス
	 * The status of the alarm: ACTIVE, ACKNOWLEDGED or CLEARED.
	 * If status was not appeared, new alarm will have status ACTIVE.
	 * Must be upper-case.
	 */
	public String	status;
	/**
	 * アラームの重大度
	 * CRITICAL / MAJOR / MINOR / WARNING のいずれかを設定してください。
	 */
	public String	severity;
	/** アラームを送信した回数 */
	public int	count;
	/** アラームが最初に起こった時刻(i.e. "count"が1の時間) */
	public TC_Date	firstOccurenceTime;
	/**
	 * トレースするプロパティの変更履歴
	 * AuditRecordCollection 型。
	 * 暫定的に JsonObject としておく。
	 */
	public JsonObject	history;
	
	/**
	 * 空のオブジェクトを生成します。
	 */
	public Alarm() {
	}
	
	/**
	 * 与えられた引数を保持するオブジェクトを生成します。
	 */
	public Alarm(String sourceId, String text) {
		source = new Id(sourceId);
		time = new TC_Date();
		this.type = "c8y_PowerAlarm"; // for example
		this.text = text;
		this.status = "ACTIVE";
		this.severity = "MINOR";
	}
}

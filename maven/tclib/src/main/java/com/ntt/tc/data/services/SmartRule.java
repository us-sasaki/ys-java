package com.ntt.tc.data.services;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ID;

/**
 * SmartRule class.
 * 非公開の API /service/ で使用されるオブジェクト。
 * 基本は ManagedObject だが、ManagedObject は大きいので継承しないこととする。
 * パラメータ値はすべて推測。
 * 本クラスは、各 SmartRule を生成するためのファクトリメソッドを提供します。
 * type として c8y_SmartRule または c8y_PrivateSmartRule が設定されますが、
 * update する場合 null を設定して下さい。
 * 同様に cepModuleId, lastUpdated も update 時、null とする必要があります。
 * private smart rule では、fragment c8y_Context が付与されます。
 * c8y_Context.id には関連するデバイス id が格納されます。
 *
 * @version		February 8, 2019
 * @author		Yusuke Sasaki
 */
public class SmartRule extends C8yData {
	/**
	 * id
	 */
	public String id;
	
	/**
	 * 有効か否かのフラグ
	 */
	public boolean enabled;
	
	/**
	 * 監視対象の Managed Object id の配列
	 */
	public String[] enabledSources;
	
	/**
	 * ui で設定された名前
	 */
	public String name;
	
	/**
	 * type "c8y_SmartRule" 固定
	 */
	public String type = "c8y_SmartRule";
	
	/**
	 * CEP のテンプレート名と思われる。
	 * "thresholdSmartRule" : measurement の閾値で alarm 生成
	 * "sendExportViaEmail" : レポートの定期メール送信スケジューラー
	 */
	public String ruleTemplateName;
	
	/**
	 * 対応する CEP モジュールID
	 */
	public String cepModuleId;
	/**
	 * 生成時間
	 */
	public TC_Date creationTime;
	/**
	 * 更新時間
	 */
	public TC_Date lastUpdated;
	
/*-----------------
 * Factory methods
 */
	/**
	 * データポイントライブラリで指定される measurement の閾値で
	 * alarm を生成する enabled な SmartRule オブジェクトを生成します。
	 *
	 * @param		name		SmartRule 名
	 * @param		alarmType	alarm の type
	 * @param		alarmText	alarm の text
	 * @param		kpiid		データポイントの managedObject ID
	 * @param		enabledSources 対象とするデバイスのIDの列
	 * @return		生成された SmartRule オブジェクト
	 */
	public static SmartRule ofThreshold(String name,
										String alarmType,
										String alarmText,
										String kpiid,
										String... enabledSources) {
		SmartRule s = new SmartRule();
		s.set("config.alarmText", alarmText);
		s.set("config.alarmType", alarmType);
		s.set("config.kpiId", kpiid);
		s.enabled = true;
		s.enabledSources = enabledSources;
		s.name = name;
		s.ruleTemplateName = "thresholdSmartRule";
		return s;
	}
	
	/**
	 * 明示的に指定される measurement の閾値で CRITICAL alarm を生成する
	 * enabled な SmartRule オブジェクトを生成します。
	 * CEP を見ると、yellowRangeMin/yellowRangeMax を指定することで
	 * MINOR alarm が生成されるように見えます。(SmartRule は未対応)
	 *
	 * @param		name		SmartRule 名
	 * @param		alarmType	alarm の type
	 * @param		alarmText	alarm の text
	 * @param		fragment	fragment名(c8y_Temperature など)
	 * @param		series		series名(T など)
	 * @param		redRangeMin	閾値最小値
	 * @param		redRangeMax 閾値最大値
	 * @param		enabledSources 対象とするデバイスのIDの列
	 * @return		生成された SmartRule オブジェクト
	 */
	public static SmartRule ofExplicitThreshold(
								String name,
								String alarmType,
								String alarmText,
								String fragment,
								String series,
								double redRangeMin,
								double redRangeMax,
								String... enabledSources) {
		if (fragment.contains("."))
			throw new IllegalArgumentException("fragment に . は含められません");
		if (series.contains("."))
			throw new IllegalArgumentException("series に . は含められません");
		SmartRule s = new SmartRule();
		s.set("config.alarmText", alarmText);
		s.set("config.alarmType", alarmType);
		s.set("config.fragment", fragment);
		s.set("config.series", series);
		s.set("config.redRangeMax", redRangeMax);
		s.set("config.redRangeMin", redRangeMin);
		s.set("config.explicitVariant", true);
		s.enabled = true;
		s.enabledSources = enabledSources;
		s.name = name;
		s.ruleTemplateName = "explicitThresholdSmartRule";
		return s;
	}
	
	/**
	 * 指定される type の alarm を受信したとき、メールを送信する enabled な
	 * SmartRule オブジェクトを生成します。
	 * 利用実績はありません。
	 *
	 * @param		name		SmartRule 名
	 * @param		alarmType	alarm の type(カンマ区切りで複数指定可能)
	 * @param		to			メール宛先
	 * @param		cc			メールのcc(null で省略可能)
	 * @param		bcc			メールのbcc(null で省略可能)
	 * @param		replyTo		メールの返信先(null で省略可能)
	 * @param		subject		メール件名
	 * @param		text		メール本文 #{source.name} #{severity}
	 * 							#{text} などの alarm オブジェクト内容を参照可能
	 * @param		enabledSources 対象とするデバイスのIDの列
	 * @return		生成された SmartRule オブジェクト
	 */
	public static SmartRule ofAlarmSendEmail(
								String name,
								String alarmType,
								String to,
								String cc,
								String bcc,
								String replyTo,
								String subject,
								String text,
								String... enabledSources) {
		SmartRule s = new SmartRule();
		s.set("config.alarmType", alarmType);
		s.set("config.to", to);
		if (cc != null) s.set("config.cc", cc);
		if (bcc != null) s.set("config.bcc", bcc);
		if (replyTo != null) s.set("config.replyTo", replyTo);
		s.set("config.subject", subject);
		s.set("config.text", text);
		s.enabled = true;
		s.enabledSources = enabledSources;
		s.name = name;
		s.ruleTemplateName = "onAlarmSendEmail";
		return s;
	}
	
}

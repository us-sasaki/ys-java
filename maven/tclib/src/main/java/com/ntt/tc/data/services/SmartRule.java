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
 * 各 SmartRule を生成するためのファクトリメソッドを含みます。
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
	 * measurement の閾値で alarm を生成します。
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
	
}

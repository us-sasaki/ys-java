package com.ntt.tc.net;

import java.io.IOException;
import java.util.Iterator;

import abdom.data.json.JsonType;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.inventory.ManagedObject;
import com.ntt.tc.data.inventory.ManagedObjectCollection;
import com.ntt.tc.data.services.SmartRule;
import com.ntt.tc.data.services.Simulator;

import static com.ntt.tc.net.Rest.Response;

/**
 * Things Cloud の ui 向けの非公開API(/service/) へのアクセス、および
 * ui と密接に関連するデータ(データポイントライブラリなど)を操作するクラスです。
 * v9.16 上で動作確認を行いますが、非公開のため、将来仕様変更される可能性が
 * あります。
 * このクラスでは、非公開 API に関連した便利メソッドも提供します。
 *
 * @author		Yusuke Sasaki
 * @version		February 8, 2019
 * @see			com.ntt.tc.data.services
 */
public class ServiceAPI {
	protected API api;
	protected Rest rest;
	
/*-------------
 * constructor
 */
	public ServiceAPI(API api) {
		this.api = api;
		this.rest = api.getRest();
	}

/*------------------
 * instance methods
 */
 
/*----------------
 * smart rule API
 */
	/**
	 * SmartRule のリスト(配列)を取得します。smartRules は Collection API
	 * と異なる返却値(pageがない)となっているため。
	 *
	 * @return		SmartRule の配列
	 */
	public SmartRule[] smartRules() {
		try {
			Response resp = rest.get("/service/smartrule/smartrules");
			JsonType jt = resp.toJson();
			return Jsonizer.toArray(jt.get("rules"), new SmartRule[0]);
		} catch (IOException ioe) {
			throw new C8yRestRuntimeException(ioe);
		}
	}
	
	/**
	 * 指定された id の SmartRule を取得します。
	 *
	 * @param		id		SmartRule id
	 * @return		SmartRule
	 * @throws		java.io.IOException	REST異常
	 */
	public SmartRule readSmartRule(String id) throws IOException {
		Response resp = rest.get("/service/smartrule/smartrules/"+id);
		return Jsonizer.fromJson(resp, SmartRule.class);
	}
	
	/**
	 * 指定された SmartRule を新規作成します。
	 *
	 * @param		smartRule		登録する SmartRule
	 * @return		登録され、id が付与された SmartRule(引数のオブジェクト)
	 * @throws		java.io.IOException REST異常
	 */
	public SmartRule createSmartRule(SmartRule smartRule)
									throws IOException {
		Response resp = rest.post("/service/smartrule/smartrules", smartRule);
		smartRule.fill(resp);
		return smartRule;
	}
	
	/**
	 * 動作未確認
	 * 指定された SmartRule を更新します。
	 *
	 * @param		updater		更新する SmartRule
	 * @return		更新された新規 SmartRule
	 * @throws		java.io.IOException REST異常
	 */
	public SmartRule updateSmartRule(SmartRule updater)
									throws IOException {
		Response resp = rest.put("/service/smartrule/smartrules/"+updater.id
									,updater);
		return Jsonizer.fromJson(resp, SmartRule.class);
	}
	
	/**
	 * 指定された SmartRule を削除します。
	 *
	 * @param		id			更新する SmartRule の id
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteSmartRule(String id) throws IOException {
		Response resp = rest.delete("/service/smartrule/smartrules/"+id);
	}
	
/*------------------
 * Smart rule utils
 */
	/**
	 * 登録されているすべての Smart Rule を削除します。
	 *
	 * @throws		java.io.IOException		REST異常
	 */
	public void deleteAllSmartRules() throws IOException {
		for (SmartRule s : smartRules()) {
			deleteSmartRule(s.id);
		}
	}
	
/*---------------
 * simulator API
 */
	/**
	 * Simulator のリスト(配列)を取得します。simulator は Collection API
	 * と異なる返却値(object の array)となっているため。
	 *
	 * @return		Simulator の配列
	 */
	public Simulator[] simulators() {
		try {
			Response resp = rest.get("/service/device-simulator/simulators");
			return Jsonizer.toArray(resp, new Simulator[0]);
		} catch (IOException ioe) {
			throw new C8yRestRuntimeException(ioe);
		}
	}
	
	/**
	 * 指定された id の Simulator を取得します。
	 *
	 * @param		id		Simulator id
	 * @return		Simulator
	 * @throws		java.io.IOException	REST異常
	 */
	public Simulator readSimulator(String id) throws IOException {
		Response resp = rest.get("/service/smartrule/smartrules/"+id);
		return Jsonizer.fromJson(resp, Simulator.class);
	}
	
	/**
	 * 動作未確認
	 * 指定された Simulator を新規作成します。
	 *
	 * @param		simulator		登録する Simulator
	 * @return		登録され、id が付与された Simulator(引数のオブジェクト)
	 * @throws		java.io.IOException REST異常
	 */
	public Simulator createSimulator(Simulator simulator)
									throws IOException {
		Response resp = rest.post("/service/device-simulator/simulators", simulator);
		simulator.fill(resp);
		return simulator;
	}
	
	/**
	 * 動作未確認
	 * 指定された Simulator を更新します。
	 *
	 * @param		updater		更新する Simulator
	 * @return		更新された新規 Simulator
	 * @throws		java.io.IOException REST異常
	 */
	public Simulator updateSimulator(Simulator updater)
									throws IOException {
		Response resp = rest.put("/service/device-simulator/simulators/"+updater.id, updater);
		return Jsonizer.fromJson(resp, Simulator.class);
	}
	
	/**
	 * 動作未確認
	 * 指定された Simulator を削除します。
	 *
	 * @param		id			更新する Simulator の id
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteSimulator(String id) throws IOException {
		Response resp = rest.delete("/service/device-simulator/simulators/"+id);
	}
	
/*-------------------
 * KPI related utils
 */
	/**
	 * Kpi が存在するかテストします。内部では、fragmentType=c8y_Kpi となる
	 * managed object があるかどうかを判定しています。
	 *
	 * @return		Kpi が存在する場合、true
	 * @throws		java.io.IOException		REST異常
	 */
	public boolean KpiExists() throws IOException {
		ManagedObjectCollection moc
				= api.readManagedObjectCollection("fragmentType=c8y_Kpi");
		return (moc.managedObjects.length != 0);
	}
	
	/**
	 * 登録されているすべての KPI (データポイントライブラリ) を削除します。
	 * 内部では、fragmentType=c8y_Kpi となる
	 * managed object を削除しています。
	 *
	 * @throws		java.io.IOException		REST異常
	 */
	public void deleteAllKpis() throws IOException {
		for (ManagedObject kpi : api.managedObjects("fragmentType=c8y_Kpi")) {
			api.deleteManagedObject(kpi.id);
		}
	}
}

package com.ntt.tc.net;

import java.io.IOException;
import java.util.Iterator;

import abdom.data.json.JsonType;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.services.SmartRule;
import com.ntt.tc.data.services.Simulator;

import static com.ntt.tc.net.Rest.Response;

/**
 * Things Cloud の ui 向けの非公開API(/service/) にアクセスする
 * クラスです。v9.16 上で動作確認を行いますが、将来仕様変更される可能性が
 * あります。
 *
 * @author		Yusuke Sasaki
 * @version		February 8, 2019
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
	 * 動作未確認
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
		Response resp = rest.put("/service/smartrule/smartrules/"+updater.id, updater);
		return Jsonizer.fromJson(resp, SmartRule.class);
	}
	
	/**
	 * 動作未確認
	 * 指定された SmartRule を削除します。
	 *
	 * @param		id			更新する SmartRule の id
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteSmartRule(String id) throws IOException {
		Response resp = rest.delete("/service/smartrule/smartrules/"+id);
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
			return Jsonizer.toArray(rest.get("/service/device-simulator/simulators"), new Simulator[0]);
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
}

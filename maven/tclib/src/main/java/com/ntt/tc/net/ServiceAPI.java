package com.ntt.tc.net;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import abdom.data.json.JsonType;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.inventory.ManagedObject;
import com.ntt.tc.data.inventory.ManagedObjectCollection;
import com.ntt.tc.data.services.SmartRule;
import com.ntt.tc.data.services.Simulator;
import com.ntt.tc.data.services.Dashboard;

import static com.ntt.tc.net.Rest.Response;

/**
 * Things Cloud の ui 向けの非公開API(/service/) へのアクセス、および
 * ui と密接に関連するデータ(データポイントライブラリなど)を操作するクラスです。
 * v9.16 上で動作確認を行いますが、非公開APIのため、将来の仕様変更により、
 * 期待されない動作をする可能性があります。
 * このクラスでは、非公開 API に関連した便利メソッドも提供します。
 * 動作確認バージョンの記載をしていますが、今後のリリースで必ずメンテナンス
 * されるとは限りません。シビアな要件で利用する場合にはテストするか、別の実装
 * を行ってください。
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
	 * Global SmartRule のリスト(配列)を取得します。smartRules は Collection API
	 * と異なる返却値(pageがない)となっているため。
	 * 動作確認バージョン：8.15, 9.12, 9.16
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
	 * 指定した device と関連する Private SmartRule の配列を取得します。
	 * smartRules は Collection APIと異なる返却値(pageがない)となっているため。
	 * 動作確認バージョン：8.15, 9.12, 9.16
	 *
	 * @param		deviceId		デバイス ID
	 * @return		SmartRule の配列
	 */
	public SmartRule[] privateSmartRules(String deviceId) {
		try {
			Response resp = rest.get("/service/smartrule/managedObjects/"+deviceId+"/smartrules");
			JsonType jt = resp.toJson();
			// Private Smart Rule では、c8y_Context fragment がつく。
			// 値は {"context":"device","id":"10319"} の形式。
			return Jsonizer.toArray(jt.get("rules"), new SmartRule[0]);
		} catch (IOException ioe) {
			throw new C8yRestRuntimeException(ioe);
		}
	}
	
	/**
	 * 全ての private SmartRule を検索し、List として返却します。
	 * ManagedObject を横断的に検索するため、時間がかかります。
	 * 動作確認バージョン：9.16
	 *
	 * @return		SmartRule の List
	 */
	public List<SmartRule> privateSmartRules() {
		List<SmartRule> result = new ArrayList<>();
		for (ManagedObject mo : api.managedObjects("pageSize=2000&query%3dhas(childAdditions)")) {
			JsonType refs = mo.get("childAdditions.references");
			if (refs == null) continue;
			if (refs.size() == 0) continue;
			result.addAll(Arrays.asList(privateSmartRules(mo.id)));
		}
		return result;
	}
	
	/**
	 * 指定された id の SmartRule を取得します。
	 * 動作確認バージョン：8.15, 9.12, 9.16
	 * Global では確認済み、Private では未確認
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
	 * 指定された SmartRule を新規作成しますが、
	 * Global では確認済み、Private では未確認。
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
	 * 指定された SmartRule(Global/Private) を更新します。
	 * たたき方は ui と同様。(id や他のパラメータも指定している)
	 * 動作確認バージョン：8.15, 9.12, 9.16
	 * Private であることは、c8y_Context fragment の存在により判定します。
	 * update では、type, cepModuleId, lastUpdated はすべて null である
	 * 必要があるため、updater のこれらのフィールドは null に設定されます。
	 *
	 * @param		updater		更新する SmartRule
	 * @return		更新された新規 SmartRule
	 * @throws		java.io.IOException REST異常
	 */
	public SmartRule updateSmartRule(SmartRule updater)
									throws IOException {
		updater.type = null;
		updater.cepModuleId = null;
		updater.lastUpdated = null;
		
		JsonType context = updater.get("c8y_Context");
		if (context == null) {
			Response resp = rest.put("/service/smartrule/smartrules/"+
									updater.id	,updater);
			return Jsonizer.fromJson(resp, SmartRule.class);
		}
		String id = context.get("id").getValue();
		Response resp = rest.put("/service/smartrule/managedObjects/"+id+
							"/smartrules/"+updater.id, updater);
		return Jsonizer.fromJson(resp, SmartRule.class);
	}
	
	/**
	 * 指定された SmartRule(Global/Private) を削除します。
	 * 動作確認バージョン：8.15, 9.12, 9.16
	 * Private であることは、c8y_Context fragment の存在により判定します。
	 *
	 * @param		id			更新する SmartRule の id
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteSmartRule(String id) throws IOException {
		// readSmartRule は global/private 共通であることを前提
		SmartRule sr = readSmartRule(id);
		JsonType context = sr.get("c8y_Context");
		if (context == null) {
			Response resp = rest.delete("/service/smartrule/smartrules/"+id);
		} else {
			String mid = context.get("id").getValue();
			Response resp = rest.delete("/service/smartrule/managedObjects/"+
							mid+"/smartrules/"+id);
		}
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
	 * 動作確認バージョン：8.15, 9.16
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
	 * 動作確認バージョン：8.15, 9.16
	 *
	 * @param		id		Simulator id
	 * @return		Simulator
	 * @throws		java.io.IOException	REST異常
	 */
	public Simulator readSimulator(String id) throws IOException {
		Response resp = rest.get("/service/smartrule/simulators/"+id);
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
	 * 動作確認バージョン：8.15, 9.16
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
	 * 動作確認バージョン：8.15, 9.16
	 *
	 * @throws		java.io.IOException		REST異常
	 */
	public void deleteAllKpis() throws IOException {
		for (ManagedObject kpi : api.managedObjects("fragmentType=c8y_Kpi")) {
			api.deleteManagedObject(kpi.id);
		}
	}

/*-------------------
 * Dashboard related
 */
	/**
	 * 指定されたグループ id のもとに指定されたダッシュボードオブジェクトを
	 * 紐づけます。
	 * 未検証
	 *
	 * @param		gid		紐づけ先のグループ id
	 * @param		d		Dashboard オブジェクト
	 * @return		生成された Dashboard(指定されたオブジェクトが更新されます)
	 * @throws		java.io.IOException		REST異常
	 */
	public Dashboard createDashboard(String gid, Dashboard d) throws IOException {
		Response resp = rest.post("/inventory/managedObjects/"+gid+"/childAdditions", d);
		d.fill(resp);
		return d;
	}
	
	/**
	 * 指定された Dashboard id のものを変更します。
	 * 未検証
	 *
	 * @param		did		変更対象のダッシュボード id
	 * @param		updater	Dashboard オブジェクト
	 * @return		変更後の Dashboard オブジェクト(新規生成されます)
	 * @throws		java.io.IOException		REST異常
	 */
	public Dashboard updateDashboard(String did, Dashboard updater) throws IOException {
		Response resp = rest.post("inventory/managedObjects/"+did, updater);
		return Jsonizer.fromJson(resp, Dashboard.class);
	}
	
/*--------
 * tenant
 */
	/**
	 * このテナントにおけるサポートユーザーの on / off を切り替えます
	 * v10.5 で発見
	 *
	 * @param		enable		true..有効 false..無効
	 * @throws		java.io.IOException		REST異常
	 */
	public void enableSupportUser(boolean enable) throws IOException {
		String e = enable?"enable":"disable";
		Response resp = rest.put("/tenant/support-user/"+e, "");
	}
	
}

package com.ntt.tc.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.TC_Int;
import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.C8yJsonData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.alarms.*;
import com.ntt.tc.data.device.*;
import com.ntt.tc.data.events.*;
import com.ntt.tc.data.identity.*;
import com.ntt.tc.data.inventory.*;
import com.ntt.tc.data.measurements.*;
import com.ntt.tc.data.real.*;
import com.ntt.tc.data.real.Module;
import com.ntt.tc.data.rest.*;
import com.ntt.tc.data.tenants.*;
import com.ntt.tc.data.users.*;

import static com.ntt.tc.net.Rest.Response;

/**
 * Things Cloud の Rest API でよく使う一連の処理をまとめた便利クラスです。
 * 判断ロジックを含む複数の API コールをまとめることを意図しています。
 * API / APIUtil / ServiceAPI の使い分けの基本的な考えとして、<br>
 * API は c8y API のそのままのラッパーおよび Iterable 生成、
 * APIUtil は c8y API を利用した便利な/よく使う処理、
 * ServiceAPI は非公開 c8y API を利用した処理(Simulator/SmartRule 等)<br>
 * としています。
 *
 * @author		Yusuke Sasaki
 * @version		December 14, 2017
 */
public class APIUtil {
	protected API api;
	
/*-------------
 * constructor
 */
	public APIUtil(API api) {
		this.api = api;
	}
	
/*------------------
 * instance methods
 */

/*--------------------
 * managedObject 関連
 */
	/**
	 * 指定された外部IDで、マネージドオブジェクトが存在すればそれを返し、
	 * なければ asDefault で指定されたマネージドオブジェクトを指定された
	 * 外部IDで登録します。
	 *
	 * @param		type		外部ID の type
	 * @param		extId		外部ID
	 * @param		asDefault	ない場合に初期値として登録する
	 *							マネージドオブジェクト
	 * @return		取得された、または生成されたマネージドオブジェクト
	 * @throws		java.io.IOException	REST異常
	 */
	public ManagedObject
				createManagedObjectIfAbsent(
					String type, String extId, ManagedObject asDefault)
								 throws IOException {
		String moid = api.readIDByExternalID(type, extId);
		if (moid != null) return api.readManagedObject(moid);
		ManagedObject mo = api.createManagedObject(asDefault);
		api.createExternalID(mo.id, type, extId);
		return mo;
	}
	
	/**
	 * 指定された外部IDで、マネージドオブジェクトが存在すればそれを返し、
	 * なければ asDefault で指定されたマネージドオブジェクトを指定された
	 * 外部IDで登録します。type は c8y_Serial 固定とします。
	 *
	 * @param		extId		外部ID(type は c8y_Serial 固定)
	 * @param		asDefault	ない場合に初期値として登録する
	 *							マネージドオブジェクト
	 * @return		取得された、または生成されたマネージドオブジェクト
	 * @throws		java.io.IOException	REST異常
	 */
	public ManagedObject
				createManagedObjectIfAbsent(
					String extId, ManagedObject asDefault)
								 throws IOException {
		return createManagedObjectIfAbsent("c8y_Serial", extId, asDefault);
	}
	
	/**
	 * 外部IDによる ManagedObject の削除を行います。
	 * 
	 * @param		type		External ID の type
	 * @param		extId		External ID 値
	 * @return		external ID が見つかり削除を実行したら true
	 * @throws		java.io.IOException REST異常
	 */
	public boolean deleteManagedObjectByExternalID(String type,
									String extId) throws IOException {
		String moid = api.readIDByExternalID(type, extId);
		if (moid != null) {
			api.deleteManagedObject(moid);
			return true;
		}
		return false;
	}
	
	/**
	 * 外部IDによる ManagedObject の削除を行います。
	 * type として暗黙的に c8y_Serial を指定します。
	 * 
	 * @param		extId		External ID 値
	 * @return		external ID が見つかり削除を実行したら true
	 * @throws		java.io.IOException REST異常
	 */
	public boolean deleteManagedObjectByExternalID(String extId)
												throws IOException {
		return deleteManagedObjectByExternalID("c8y_Serial", extId);
	}
	
	/**
	 * 指定された外部IDで、マネージドオブジェクトが存在すればそれを返し、
	 * なければ asDefault で指定されたマネージドオブジェクトを指定された
	 * 外部IDで登録します。
	 * asDefault に c8y_IsDevice 属性がない場合、自動的に付加します。
	 *
	 * @param		type		外部ID の type
	 * @param		extId		外部ID
	 * @param		asDefault	ない場合に初期値として登録する
	 *							デバイス
	 * @return		取得された、または生成されたマネージドオブジェクト
	 * @throws		java.io.IOException	REST異常
	 */
	public ManagedObject
				createDeviceIfAbsent(
					String type, String extId, ManagedObject asDefault)
								throws IOException {
		if (asDefault.get("c8y_IsDevice") == null)
			asDefault.set("c8y_IsDevice", new JsonObject());
		return createManagedObjectIfAbsent(type, extId, asDefault);
	}
	
	/**
	 * 指定された外部IDで、マネージドオブジェクトが存在すればそれを返し、
	 * なければ asDefault で指定されたマネージドオブジェクトを指定された
	 * 外部IDで登録します。type は c8y_Serial 固定とします。
	 * asDefault に c8y_IsDevice 属性がない場合、自動的に付加します。
	 *
	 * @param		extId		外部ID(type は c8y_Serial 固定)
	 * @param		asDefault	ない場合に初期値として登録する
	 *							デバイス
	 * @return		取得された、または生成されたマネージドオブジェクト
	 * @throws		java.io.IOException	REST異常
	 */
	public ManagedObject
				createDeviceIfAbsent(
					String extId, ManagedObject asDefault)
								throws IOException {
		return createDeviceIfAbsent("c8y_Serial", extId, asDefault);
	}
	
	/**
	 * External ID を指定して ManagedObject を取得します。
	 *
	 * @param		type		extId の type
	 * @param		extId		extId
	 * @return		ManagedObject (存在しない場合 null)
	 * @throws		java.io.IOException		REST異常
	 */
	public ManagedObject readManagedObjectForExtId(String type, String extId)
								throws IOException {
		String moid = api.readIDByExternalID(type, extId);
		if (moid == null) return null;
		return api.readManagedObject(moid);
	}
	
	/**
	 * External ID を指定して ManagedObject を取得します。
	 * type は c8y_Serial 固定です。
	 *
	 * @param		extId		extId
	 * @return		ManagedObject (存在しない場合 null)
	 * @throws		java.io.IOException		REST異常
	 */
	public ManagedObject readManagedObjectForExtId(String extId)
								throws IOException {
		return readManagedObjectForExtId("c8y_Serial", extId);
	}
	
/*--------------
 * modules 関連
 */
	/**
	 * 指定されたモジュール名の CEP モジュールを取得します。
	 * 内部ではモジュールを全て読み込み、名称が合致するものを取得しています。
	 *
	 * @param		moduleName		CEP モジュール名
	 * @return		取得されたモジュール
	 * @throws		C8yNoSuchObjectException	モジュールが見つからない場合
	 * @throws		java.io.IOException	REST異常
	 */
	public Module readModuleForName(final String moduleName)
							throws IOException {
		List<Module> modules = new ArrayList<>();
		api.modules().iterator().forEachRemaining(modules::add);
		
		Module m = modules.stream().
						filter( a -> a.name.equals(moduleName) ).
						reduce(null, (a, b) -> {
							if (a != null) return a;
							return b; // 最初に見つかったものとする
						});
		if (m == null)
				throw new C8yNoSuchObjectException("Module " + moduleName
							+ "was not found.");
		return m;
	}
	
	/**
	 * 指定されたモジュール名の CEP モジュールを undeploy 状態にします。
	 *
	 * @param		moduleName		CEP モジュール名
	 * @throws		C8yNoSuchObjectException	モジュールが見つからない場合
	 * @throws		java.io.IOException	REST異常
	 */
	public void undeployModuleForName(final String moduleName)
							throws IOException {
		api.updateModule(readModuleForName(moduleName).id, false);
	}
	
/*-------------------
 * Device credential
 */
	/**
	 * デバイスクレデンシャルをシーケンスシミュレートして取得します。
	 * ユーザー名は "device_"+deviceId となります。
	 * 
	 * @param	deviceId	デバイス登録時に用いるデバイス ID
	 * @return	取得されたデバイスクレデンシャル
	 * @throws	java.io.IOException REST異常
	 */
	public DeviceCredentials createDeviceCredentials(String deviceId)
								throws IOException {
		// 新規デバイスリクエストの作成
		NewDeviceRequest req = new NewDeviceRequest();
		req.id = deviceId;
		NewDeviceRequest ndr = api.createNewDeviceRequest(req);
		
		// クレデンシャル要求
		DeviceCredentials cred = new DeviceCredentials();
		cred.id = deviceId;
		cred = api.createDeviceCredentials(cred);
		if (cred.isValid())
			throw new C8yRestException("承認前にクレデンシャルが取得されました:"+cred);
		
		// 承認する
		NewDeviceRequest r = api.readNewDeviceRequest(deviceId);
		if (r.getStatus().equals(NewDeviceRequest.PENDING_ACCEPTANCE)) {
			api.updateNewDeviceRequest(deviceId, NewDeviceRequest.ACCEPTED);
		} else {
			throw new C8yRestException("デバイスリクエストのステータスが異常です:"+r);
		}
		
		// クレデンシャル取得
		cred = api.createDeviceCredentials(cred);
		if (!cred.isValid())
			throw new C8yRestException("承認してもクレデンシャルが付与されませんでした:"+cred);
			
		return cred;
	}
	
	/**
	 * デバイスをシミュレートして初期登録を行います。
	 * 指定された ManagedObject を指定されたデバイス名で登録します。
	 * 登録は credential 要求によって取得されたデバイスクレデンシャルを
	 * 用いて行われます。
	 * デバイスオーナーは通常デバイス同様、デバイス自身になります。
	 * 事前に該当する External ID でデバイスが存在した場合、削除、上書き
	 * されます。
	 *
	 * @param	deviceId	デバイス登録時に用いるデバイス ID
	 * @param	type		設定する External ID の type
	 * @param	extId		設定する External ID
	 * @param	managedObject	登録するデバイス情報(成功時、更新されます)
	 * @return	デバイスクレデンシャル。指定した managedObject も更新され、
	 *			ID などが付与されます。
	 * @throws		java.io.IOException	REST異常
	 */
	public DeviceCredentials registerDevice(
					String deviceId,
					String type,
					String extId,
					ManagedObject managedObject) throws IOException {
		// デバイスが存在するか確認する
		String moid = api.readIDByExternalID(type, extId);
		if (moid != null) {
			// デバイスを削除する
			api.deleteExternalID(type, extId); // 不要と思われるが実行しておく
			api.deleteManagedObject(moid);
		}
		
		// クレデンシャル要求
		DeviceCredentials cred = createDeviceCredentials(deviceId);
		
		// 取得されたクレデンシャルを用いて
		// ManagedObject を登録(managedObject は更新される)
		// location, tenant は現在のものを流用
		Rest rest = api.getRest();
		API devapi = new API(rest.getLocation(), rest.getTenant(),
							cred.username, cred.password);
		APIUtil devutil = new APIUtil(devapi);
		ManagedObject dbmo = devutil.createDeviceIfAbsent(type, extId, managedObject);
		managedObject.fill(dbmo);
		
		return cred;
	}
	
	/**
	 * デバイスをシミュレートして初期登録を行います。
	 * 指定された ManagedObject を指定されたデバイス名で登録します。
	 * 登録は credential 要求によって取得されたデバイスクレデンシャルを
	 * 用いて行われます。
	 * デバイスオーナーは通常デバイス同様、デバイス自身になります。
	 * 事前に該当する External ID でデバイスが存在した場合、削除、上書き
	 * されます。
	 * External ID の type は c8y_Serial が使用されます。
	 *
	 * @param	deviceId	デバイス登録時に用いるデバイス ID
	 * @param	extId		設定する External ID
	 * @param	managedObject	登録するデバイス情報(成功時、更新されます)
	 * @return	デバイスクレデンシャル。指定した managedObject も更新され、
	 *			ID などが付与されます。
	 * @throws		java.io.IOException	REST異常
	 */
	public DeviceCredentials registerDevice(
					String deviceId,
					String extId,
					ManagedObject managedObject) throws IOException {
		return registerDevice(deviceId, "c8y_Serial", extId, managedObject);
	}
	
	/**
	 * Device Credential の認証情報を用いて API を生成します。
	 *
	 * @param		cred		デバイスクレデンシャル情報
	 * @return		指定されたクレデンシャル情報による API オブジェクト
	 * @exception	IllegalStateException cred が user または password 情報を
	 *				持たない場合
	 */
	public API deviceAPI(DeviceCredentials cred) {
		if (cred.username == null || cred.username.equals(""))
			throw new IllegalStateException("device credential has no user.");
		if (cred.password == null || cred.password.equals(""))
			throw new IllegalStateException("device credential has no password.");
		Rest rest = api.getRest();
		API devapi = new API(rest.getLocation(), rest.getTenant(),
							cred.username, cred.password);
		return devapi;
	}
	
/*------------------
 * Measurement 関連
 */
	/**
	 * Measurement パラメータ直接指定して Measurement を生成します。
	 *
	 * @param	source 	source device id
	 * @param	type	measurement type
	 * @param	measurementPath	fragment と series を . つなぎで指定します
	 * @param	value	value
	 * @param	unit	unit
	 */
	public void createMeasurement(String source, String type, String measurementPath, double value, String unit) throws IOException {
		Measurement m = new Measurement(source, type);
		m.put(measurementPath, value, unit);
		api.createMeasurement(m);
	}
	
/*------------
 * Event 関連
 */
	/**
	 * Event パラメータ直接指定して Event を生成します。
	 *
	 * @param	source 	source device id
	 * @param	type	event type
	 * @param	text	text
	 */
	public Event createEvent(String source, String type, String text) throws IOException {
		Event e = new Event(source, type, text);
		return api.createEvent(e);
	}
	
/*------------
 * Alarm 関連
 */
	/**
	 * alarm のリストを取得し、firstOccurrenceTime(ない場合 time) から
	 * creationTime までの時間を算出します。
	 * CEP 遅延でよく利用するため、API に組み込みました。
	 * CEP で alarm を作成してから実際に mongo に書き込まれるまでの時間を
	 * 計測する目的で利用できます。alarm.time が current_timestamp() で設定
	 * されていることを確認して下さい。
	 *
	 * @param		from		開始時間
	 * @param		to			終了時間
	 * @param		param		type や source 指定など、追加の指定
	 *							null/"" を指定すると追加指定を設定しません
	 * @return		firstOccurrenceTime(or time), creationTime の TreeMap
	 */
	public TreeMap<Long, Long> getAlarmCreationElapsed(TC_Date from, TC_Date to, String param) {
		TreeMap<Long, Long> result = new TreeMap<>();
		
		if (param == null) param = "";
		if (!param.equals("") && !param.startsWith("&"))
			param = "&" + param;
		
		for (Alarm alarm : api.alarms("dateFrom="+from.getValue()+"&dateTo="+to.getValue()+param)) {
			TC_Date creationTime = alarm.creationTime;
			TC_Date time = alarm.firstOccurrenceTime;
			if (time == null) time = alarm.time;
			result.put(time.getTime(), creationTime.getTime());
		}
		return result;
	}
	
	/**
	 * alarm のリストを取得し、firstOccurrenceTime(ない場合 time) から
	 * creationTime までの時間を算出します。
	 * CEP 遅延でよく利用するため、API に組み込みました。
	 * CEP で alarm を作成してから実際に mongo に書き込まれるまでの時間を
	 * 計測する目的で利用できます。alarm.time が current_timestamp() で設定
	 * されていることを確認して下さい。
	 *
	 * @param		from		開始時間
	 * @param		to			終了時間
	 * @return		firstOccurrenceTime(or time), creationTime の TreeMap
	 */
	public TreeMap<Long, Long> getAlarmCreationElapsed(TC_Date from, TC_Date to) {
		return getAlarmCreationElapsed(from, to, null);
	}
	
	/**
	 * Alarm パラメータ直接指定して　Alarm を生成します。
	 *
	 * @param	source 	source device id
	 * @param	type	alarm type
	 * @param	text	text
	 * @param	status		Alarm status
	 * @param	severity	Alarm severity
	 * @return	生成された Alarm
	 */
	public Alarm createAlarm(String source, String type, String text, String status, String severity) throws IOException {
		Alarm a = new Alarm(source, type, text, status, severity);
		return api.createAlarm(a);
	}
	
/*------------------
 * Map 取得メソッド
 */
	/**
	 * Managed Object の type 一覧を返却します。
	 * 返却される map は type 値と、存在数です。
	 * 数が多い場合、時間がかかることがあります。
	 *
	 * @param		query	取得する ManagedObject に条件を追加します
	 * @return		(type, count) からなる Map です。null キーではキー値が
	 *				"null" となります。
	 */
	public Map<String, Integer> getManagedObjectTypes(String query) {
		Map<String, Integer> result = new TreeMap<String, Integer>();
		
		for (ManagedObject m : api.managedObjects(query)) {
			String type = m.type;
			if (type == null) type = "null";
			Integer c = result.get(type);
			if (c == null) c = 0;
			result.put(type, c+1);
		}
		return result;
	}
	
	/**
	 * Managed Object の type 一覧を返却します。
	 * 返却される map は type 値と、存在数です。
	 * 数が多い場合、時間がかかることがあります。
	 *
	 * @return		(type, count) からなる Map です。null キーではキー値が
	 *				"null" となります。
	 */
	public Map<String, Integer> getManagedObjectTypes() {
		return getManagedObjectTypes("pageSize=1000");
	}
	
	/**
	 * Managed Object 全体に含まれるプロパティ(fragmentを含みます)の
	 * 一覧を取得します。
	 *
	 * @param		query	取得する ManagedObject の検索キー
	 * @return		(fragment, count) からなる Map です。null キーではキー値が
	 *				"null" となります。
	 */
	public Map<String, Integer> getManagedObjectProperties(String query) {
		Map<String, Integer> result = new TreeMap<String, Integer>();
		
		for (ManagedObject m : api.managedObjects(query)) {
			JsonType j = m.toJson(); // JsonObject に変換
			for (String key : j.keySet()) {
				if (key == null) key = "null";
				Integer c = result.get(key);
				if (c == null) c = 0;
				result.put(key, c+1);
			}
		}
		return result;
	}
	
	/**
	 * Managed Object 全体に含まれるプロパティ(fragmentを含みます)の
	 * 一覧を取得します。
	 *
	 * @return		(fragment, count) からなる Map です。null キーではキー値が
	 *				"null" となります。
	 */
	public Map<String, Integer> getManagedObjectProperties() {
		return getManagedObjectProperties("pageSize=1000");
	}
	
	/**
	 * Measurement の type 一覧を返却します。
	 * 返却される map は type 値と、存在数です。
	 * Measurement は数が多く、一般に時間がかかります。
	 * (数時間以上かかる場合もあります)
	 *
	 * @param		query	取得する Measurement に条件を追加します
	 * @return		(type, count) からなる Map です。
	 */
	public Map<String, Integer> getMeasurementTypes(String query) {
		Map<String, Integer> result = new TreeMap<String, Integer>();
		
		for (Measurement m : api.measurements(query)) {
			String type = m.type;
			if (type == null) type = "null";
			Integer c = result.get(type);
			if (c == null) c = 0;
			result.put(type, c+1);
		}
		return result;
	}
	
	/**
	 * Measurement の type 一覧を返却します。
	 * 返却される map は type 値と、存在数です。
	 * Measurement は数が多く、一般に時間がかかります。
	 * (数時間以上かかる場合もあります)
	 *
	 * @return		(type, count) からなる Map です。
	 */
	public Map<String, Integer> getMeasurementTypes() {
		return getMeasurementTypes("pageSize=2000");
	}
	
	/**
	 * Event の type 一覧を返却します。
	 * 返却される map は type 値と、存在数です。
	 * Event は数が多く、一般に時間がかかります。
	 * (数時間以上かかる場合もあります)
	 *
	 * @param		query	取得する Event に条件を追加します
	 * @return		(type, count) からなる Map です。
	 */
	public Map<String, Integer> getEventTypes(String query) {
		Map<String, Integer> result = new TreeMap<String, Integer>();
		
		for (Event e : api.events(query)) {
			String type = e.type;
			if (type == null) type = "null";
			Integer c = result.get(type);
			if (c == null) c = 0;
			result.put(type, c+1);
		}
		return result;
	}
	
	/**
	 * Event の type 一覧を返却します。
	 * 返却される map は type 値と、存在数です。
	 * Event は数が多く、一般に時間がかかります。
	 * (数時間以上かかる場合もあります)
	 *
	 * @return		(type, count) からなる Map です。
	 */
	public Map<String, Integer> getEventTypes() {
		return getEventTypes("pageSize=2000");
	}
	
	/**
	 * Alarm の type 一覧を返却します。
	 * 返却される map は type 値と、存在数です。
	 * 数が多い場合、時間がかかります。
	 *
	 * @param		query	取得する Alarm に条件を追加します
	 * @return		(type, count) からなる Map です。
	 */
	public Map<String, Integer> getAlarmTypes(String query) {
		Map<String, Integer> result = new TreeMap<String, Integer>();
		
		for (Alarm a : api.alarms(query)) {
			String type = a.type;
			if (type == null) type = "null";
			Integer c = result.get(type);
			if (c == null) c = 0;
			result.put(type, c+1);
		}
		return result;
	}
	
	/**
	 * Alarm の type 一覧を返却します。
	 * 返却される map は type 値と、存在数です。
	 * 数が多い場合、時間がかかります。
	 *
	 * @return		(type, count) からなる Map です。
	 */
	public Map<String, Integer> getAlarmTypes() {
		return getAlarmTypes("pageSize=1000");
	}
	
/*---------------------
 * Stream 生成メソッド
 */
	/**
	 * ManagedObject のストリームを取得します。
	 * API#managedObjects を利用した直列ストリームで最適化は行われていません。
	 *
	 * @param		query		取得する managedObject のクエリ
	 * @return		managedObject のストリーム
	 */
	public Stream<ManagedObject> managedObjectStream(String query) {
		Iterable<ManagedObject> i = api.managedObjects(query);
		return StreamSupport.stream(i.spliterator(), false);
	}
	
	/**
	 * ManagedObject のストリームを取得します。
	 * API#managedObjects を利用した直列ストリームで最適化は行われていません。
	 *
	 * @return		managedObject のストリーム
	 */
	public Stream<ManagedObject> managedObjectStream() {
		Iterable<ManagedObject> i = api.managedObjects();
		return StreamSupport.stream(i.spliterator(), false);
	}
	
	/**
	 * Measurement のストリームを取得します。
	 * API#measurements を利用した直列ストリームで最適化は行われていません。
	 *
	 * @param		query		取得する measurement のクエリ
	 * @return		measurement のストリーム
	 */
	public Stream<Measurement> measurementStream(String query) {
		Iterable<Measurement> i = api.measurements(query);
		return StreamSupport.stream(i.spliterator(), false);
	}
	
	/**
	 * Measurement のストリームを取得します。
	 * API#measurements を利用した直列ストリームで最適化は行われていません。
	 *
	 * @return		measurement のストリーム
	 */
	public Stream<Measurement> measurementStream() {
		Iterable<Measurement> i = api.measurements();
		return StreamSupport.stream(i.spliterator(), false);
	}
	
	/**
	 * Event のストリームを取得します。
	 * API#events を利用した直列ストリームで最適化は行われていません。
	 *
	 * @param		query		取得する event のクエリ
	 * @return		event のストリーム
	 */
	public Stream<Event> eventStream(String query) {
		Iterable<Event> i = api.events(query);
		return StreamSupport.stream(i.spliterator(), false);
	}
	
	/**
	 * Event のストリームを取得します。
	 * API#events を利用した直列ストリームで最適化は行われていません。
	 *
	 * @return		event のストリーム
	 */
	public Stream<Event> eventStream() {
		Iterable<Event> i = api.events();
		return StreamSupport.stream(i.spliterator(), false);
	}
	
	/**
	 * Alarm のストリームを取得します。
	 * API#events を利用した直列ストリームで最適化は行われていません。
	 *
	 * @param		query		取得する event のクエリ
	 * @return		event のストリーム
	 */
	public Stream<Alarm> alarmStream(String query) {
		Iterable<Alarm> i = api.alarms(query);
		return StreamSupport.stream(i.spliterator(), false);
	}
	
	/**
	 * Alarm のストリームを取得します。
	 * API#events を利用した直列ストリームで最適化は行われていません。
	 *
	 * @return		event のストリーム
	 */
	public Stream<Alarm> alarmStream() {
		Iterable<Alarm> i = api.alarms();
		return StreamSupport.stream(i.spliterator(), false);
	}
	
/*---------------
 * class methods
 */
	/**
	 * createDeviceIfAbsent などで使用できるテスト用のデフォルトデバイス
	 * オブジェクトを生成します。
	 * com_cumulocity_model_Agent, CellInfo, c8y_Hardware 等
	 * 各種パラメータが設定されています。
	 * type = "APIUtil_device" となっています。
	 *
	 * @param		name		デバイス名、name, c8y_Hardware.model に設定
	 *							されます。
	 * @return		デフォルトデバイスオブジェクト
	 */
	public static ManagedObject defaultDevice(String name) {
		ManagedObject mo = new ManagedObject();
		mo.name = name;
		mo.type = "APIUtil_device";
		
		mo.set("com_cumulocity_model_Agent", new JsonObject());
		mo.set("c8y_AccelerationSensor", new JsonObject());
		
		mo.c8y_CellInfo = new C8y_CellInfo();
		mo.c8y_CellInfo.cellTowers = new C8y_CellTower[1];
		mo.c8y_CellInfo.cellTowers[0] = new C8y_CellTower();
		mo.c8y_CellInfo.cellTowers[0].cellId = 123;
		mo.c8y_CellInfo.cellTowers[0].locationAreaCode = 12;
		mo.c8y_CellInfo.cellTowers[0].mobileCountryCode = 99;
		mo.c8y_CellInfo.cellTowers[0].mobileNetworkCode = 1;
		mo.c8y_CellInfo.cellTowers[0].primaryScramblingCode = null;
		mo.c8y_CellInfo.cellTowers[0].radioType = "radioType";
		mo.c8y_CellInfo.cellTowers[0].serving = new TC_Int(1);
		mo.c8y_CellInfo.cellTowers[0].signalStrength = new TC_Int(64);
		mo.c8y_CellInfo.cellTowers[0].timingAdvance = new TC_Int(10);
		
		mo.c8y_Configuration = new C8y_Configuration();
		mo.c8y_Configuration.config = "#Thu Apr 25 17:31:11 JST 2019\n"
				+"c8y.log.eventLevel=INFO\n"
				+"nttcom.enabledSensors=movement,humidity,battery,optical\n"
				+"c8y.log.alarmLevel=ERROR\n"
				+"nttcom.send.interval=60000\n"
				+"nttcom.scan.interval=60000\n"
				+"nttcom.collect.interval=60000\n"
				+"nttcom.whiteList=B0\\:B4\\:48\\:BE\\:95\\:06,24\\:71\\:89\\:BD\\:08\\:02,24\\:71\\:89\\:C1\\:2F\\:07\n";
		
		mo.set("c8y_CurrentSensor", new JsonObject());
		
		mo.set("c8y_DistanceSensor", new JsonObject());
		
		mo.c8y_Firmware = new C8y_Firmware();
		mo.c8y_Firmware.name = "firm name";
		mo.c8y_Firmware.version = "firm version 1.0";
		
		mo.set("c8y_Hardware.model", "IoT-GW_"+name);
		mo.set("c8y_Hardware.revision", "0000");
		mo.set("c8y_Hardware.serialNumber", "0000-1234-5678");

		// skip c8y_HumiditySensor
		
		mo.set("c8y_IsDevice", new JsonObject());
		
		// skip LightSensor
		
		mo.c8y_Mobile = new C8y_Mobile();
		mo.c8y_Mobile.cellId = "cellId";
		mo.c8y_Mobile.connType = "connType";
		mo.c8y_Mobile.currentBand = "currentBand";
		mo.c8y_Mobile.currentOperator = "operator";
		mo.c8y_Mobile.ecn0 = "ecn0";
		mo.c8y_Mobile.iccid = "iccid";
		mo.c8y_Mobile.imei = "imei";
		mo.c8y_Mobile.imsi = "imsi";
		mo.c8y_Mobile.lac = "lac";
		mo.c8y_Mobile.mnc = "mnc";
		mo.c8y_Mobile.msisdn = "msisdn";
		mo.c8y_Mobile.rcsp = "rcsp";
		
		// skip moisture/motion sensors
		mo.c8y_Position = new C8y_Position();
		mo.c8y_Position.alt = 10;
		mo.c8y_Position.lat = 35.686502827977364;
		mo.c8y_Position.lng = 139.7662103176117;
		mo.c8y_Position.reportReason = "normal";
		mo.c8y_Position.trackingProtocol = "GPS";
		
		// skip
		
		mo.c8y_Software = new C8y_Software();
		mo.c8y_Software.set("pi-driver", "pi-driver-3.4.5.jar");
		mo.c8y_Software.set("pi4j-gpio-extension" , "pi4j-gpio-extension-0.0.5.jar");
		
		mo.c8y_SoftwareList = new C8y_SoftwareList[1];
		mo.c8y_SoftwareList[0] = new C8y_SoftwareList();
		mo.c8y_SoftwareList[0].name = "software name";
		mo.c8y_SoftwareList[0].url = "http://software.url.com";
		mo.c8y_SoftwareList[0].version = "1.0.2";
		
		// skip
		
		C8y_SupportedOperation[] op = new C8y_SupportedOperation[] {
			C8y_SupportedOperation.c8y_Configuration,
			C8y_SupportedOperation.c8y_Firmware,
			C8y_SupportedOperation.c8y_Geofence,
			C8y_SupportedOperation.c8y_LogfileRequest,
			C8y_SupportedOperation.c8y_MotionTracking,
			C8y_SupportedOperation.c8y_Restart,
			C8y_SupportedOperation.c8y_SendConfiguration,
			C8y_SupportedOperation.c8y_Software,
			C8y_SupportedOperation.c8y_SoftwareList};
			// c8y_SupportedLogs フィールドは入れていない
		mo.set("c8y_SupportedOperations", Jsonizer.toJson(op));
		return mo;
	}
	
}

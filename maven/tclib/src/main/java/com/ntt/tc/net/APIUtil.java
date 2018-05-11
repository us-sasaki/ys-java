package com.ntt.tc.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.alarms.*;
import com.ntt.tc.data.auditing.*;
import com.ntt.tc.data.binaries.*;
import com.ntt.tc.data.device.*;
import com.ntt.tc.data.events.*;
import com.ntt.tc.data.identity.*;
import com.ntt.tc.data.inventory.*;
import com.ntt.tc.data.measurements.*;
import com.ntt.tc.data.real.*;
import com.ntt.tc.data.rest.*;
import com.ntt.tc.data.retention.*;
import com.ntt.tc.data.sensor.*;
import com.ntt.tc.data.tenants.*;
import com.ntt.tc.data.users.*;
import com.ntt.tc.util.Base64;

import static com.ntt.tc.net.Rest.Response;

/**
 * Things Cloud の Rest API でよく使う一連の処理をまとめた便利クラスです。
 * 判断ロジックを含む複数の API コールをまとめることを意図しています。
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
	 */
	public ManagedObject
				createManagedObjectIfAbsent(
					String extId, ManagedObject asDefault)
								 throws IOException {
		return createManagedObjectIfAbsent("c8y_Serial", extId, asDefault);
	}
	
	/**
	 * Managed Object の type 一覧を返却します。
	 * 返却される map は type 値と、存在数です。
	 * 数が多い場合、時間がかかることがあります。
	 *
	 * @param		query	取得する ManagedObject に条件を追加します
	 * @return		(type, count) からなる Map です。一般に null キーを含みます。
	 */
	public Map<String, Integer> getManagedObjectTypes(String query) {
		Map<String, Integer> result = new TreeMap<String, Integer>();
		
		for (ManagedObject m : api.managedObjects(query)) {
			String type = m.type;
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
	 * @return		(type, count) からなる Map です。一般に null キーを含みます。
	 */
	public Map<String, Integer> getManagedObjectTypes() {
		return getManagedObjectTypes("pageSize=1000");
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
	
	public void undeployModuleForName(final String moduleName)
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
				throw new IllegalArgumentException("Module " + moduleName
							+ "was not found.");
		api.updateModule(m.id, false);
	}
	
	
}

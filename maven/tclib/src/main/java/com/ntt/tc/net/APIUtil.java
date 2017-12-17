package com.ntt.tc.net;

import java.io.IOException;
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
 * Things Cloud の Rest API でよく使う一連の処理をまとめた
 * 便利クラスです。
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
}

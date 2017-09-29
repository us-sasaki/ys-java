package com.ntt.tc.net;

import java.io.IOException;

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

import static com.ntt.tc.net.Rest.Response;

/**
 * Things Cloud の Rest API の Java ラッパーです。
 *
 * 命名ルールは、以下のようにします。(Measurementの例)
 * POST : createMeasurement()
 * PUT  : updateMeasurement()
 * GET  : readMeasurement()
 * DELETE:deleteMeasurement()
 *
 * @author		Yusuke Sasaki
 */
public class API {

	protected Rest rest;
	
/*-------------
 * constructor
 */
	public API(Rest rest) {
		this.rest = rest;
	}
	
	public API(String location, String tenant, String user, String pass) {
		this.rest = new Rest(location, tenant, user, pass);
	}
	
/*------------------
 * instance methods
 */

/*------------
 * Device API
 */
	/**
	 * デバイスクレデンシャルを要求します。
	 * bootstrap ユーザにする必要があると思われる。
	 *
	 * @param	req		デバイスクレデンシャルのオブジェクト。id は必須です。
	 * @return	更新されたデバイスクレデンシャルが返却されます。
	 *			承認された場合、req.isValid() が true となります。
	 *			承認されなかった場合は、値は変化しません。
	 */
	public DeviceCredentials createDeviceCredential(DeviceCredentials req)
				throws IOException {
		if (req.isValid()) return req;
		if (req.id == null || req.id.equals(""))
			throw new IllegalArgumentException("DeviceCredentials の id に値がありません");
		Response resp = rest.post("/devicecontrol/deviceCredentials", "deviceCredentials", req);
		if (resp.code != 404) req.fill(resp);
		return req;
	}
	
/*--------------
 * Identity API
 */
	/**
	 * 外部IDから、Managed Object ID を検索します。
	 *
	 * @param	type	external ID の type (c8y_Serial等)
	 * @param	externalId	external ID の値
	 * @return	Managed Object ID, 存在しない場合 null
	 */
	public String readIDByExternalID(String type, String externalId)
				throws IOException {
		Response resp = rest.get("/identity/externalIds/"+type+"/"+externalId, "externalId");
		if (resp.code == 404) return null;	// not found
		
		return resp.toJson().get("managedObject.id").getValue();
	}
	
	/**
	 * 指定された Managed Object ID に指定された type で外部IDを追加します。
	 *
	 * @param	id		追加対象の Managed Object ID
	 * @param	type	外部ID(externalId)のtype
	 * @param	externalId	外部IDの値
	 */
	public void createExternalID(String id, String type, String externalId)
				throws IOException {
		JsonObject jo = JsonType.o("type",type).put("externalId",externalId);
		Response resp = rest.post("/identity/globalIds/"+id+"/externalIds", "externalId", jo);
	}
	
/*---------------
 * Inventory API
 */
	/**
	 * Managed Object を登録します。
	 * 登録後、渡した ManagedObject は id などが追加され、更新されます。
	 *
	 * @param	mo		登録したい Managed Object
	 * @return	登録後、更新された Managed Object
	 */
	public ManagedObject createManagedObject(ManagedObject mo)
				throws IOException {
		Response resp = rest.post("/inventory/managedObjects", "managedObjects", mo);
		mo.fill(resp);
		return mo;
	}
	
	/**
	 * Managed Object を更新します。
	 * 更新後、渡した updater は変更されません。
	 *
	 * @param	id		更新対象の Managed Object ID
	 * @param	updater	Managed Object の変更(追加)内容
	 * @return	登録後、更新された Managed Object(updater とは別インスタンス)
	 */
	public ManagedObject updateManagedObject(String id, ManagedObject updater)
				throws IOException {
		Response resp = rest.put("/inventory/managedObjects/"+id, "managedObject", updater);
		return Jsonizer.fromJson(resp, ManagedObject.class);
	}
	
	/**
	 * Managed Object の位置情報を更新する便利メソッドです。
	 *
	 * @param	id		更新対象の Managed Object ID
	 * @param	lat		緯度(latitude)
	 * @param	lng		経度(longitude)
	 * @param	alt		高度(altitude)
	 */
	public void updateManagedObjectLocation(String id, double lat, double lng, double alt)
				throws IOException {
		JsonObject jo = new JsonObject();
		jo.put("c8y_Position.lat", lat);
		jo.put("c8y_Position.lng", lng);
		jo.put("c8y_Position.alt", alt);
		System.out.println(jo.toString("  "));
		
		Response resp = rest.put("/inventory/managedObjects/" + id, "managedObject", jo);
	}
	
/*
 * main
 */
	public static void main(String[] args) throws Exception {
		API api = new API("https://sasa.iot-trialpack.com", "sasa", "sasaki", "appuri89");
		
		String id = api.readIDByExternalID("c8y_Serial", "operationTest");
		System.out.println(id);
		api.createExternalID(id, "extTest", "extTestValue");
		api.updateManagedObjectLocation(id, 10,20,30);
		
	}
}

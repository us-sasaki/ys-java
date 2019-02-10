package com.ntt.tc.net;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.stream.Collectors;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.TC_Date;
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
 * Things Cloud の Rest API の Java ラッパーです。
 *
 * 命名ルールは、以下のようにします。(Measurementの例) <br>
 * POST : createMeasurement() <br>
 * PUT  : updateMeasurement() <br>
 * GET  : readMeasurement() <br>
 * DELETE:deleteMeasurement() <br>
 * <br>
 * Rest API にない独自 API は命名も独自とします。
 *
 * @author		Yusuke Sasaki
 * @version		October 19, 2017
 */
public class API {
	static final String BUSR = "ZGV2aWNlYm9vdHN0cmFw";
	static final String BPSS = "RmhkdDFiYjFm";
	static final Charset UTF8 = StandardCharsets.UTF_8;
	
	protected Rest rest;
	protected Rest bootstrapRest;
	
/*-------------
 * constructor
 */
	/**
	 * 指定された Rest オブジェクトを使ってアクセスするオブジェクトを作成します。
	 *
	 * @param	rest	接続に利用する Rest オブジェクト
	 */
	public API(Rest rest) {
		this.rest = rest;
		bootstrapRest = new Rest(rest.getLocation(), "management", new String(Base64.decodeFromString(BUSR)), new String(Base64.decodeFromString(BPSS)));
	}
	
	/**
	 * 指定された URL, tenant, user, password により接続する API オブジェクト
	 * を生成します。
	 *
	 * @param	location	URL https://hogehoge.je1.thingscloud.ntt.com 等
	 * @param	tenant		テナント (null または null文字を指定すると URL
	 *						のテナントになります)
	 * @param	user		ログインユーザー名
	 * @param	pass		ログインパスワード
	 */
	public API(String location, String tenant, String user, String pass) {
		if (tenant == null) tenant = "";
		this.rest = new Rest(location, tenant, user, pass);
		bootstrapRest = new Rest(rest.getLocation(), "management", new String(Base64.decodeFromString(BUSR)), new String(Base64.decodeFromString(BPSS)));
	}
	
	/**
	 * 指定された URL, user, password により接続する API オブジェクト
	 * を生成します。テナント名は URL から取得されます。
	 *
	 * @param	location	URL https://hogehoge.je1.thingscloud.ntt.com 等
	 * @param	user		ログインユーザー名
	 * @param	pass		ログインパスワード
	 */
	public API(String location, String user, String pass) {
		this.rest = new Rest(location, user, pass);
		bootstrapRest = new Rest(rest.getLocation(), "management", new String(Base64.decodeFromString(BUSR)), new String(Base64.decodeFromString(BPSS)));
	}
	
	/**
	 * URL, tenant, user, pass を供給する辞書(Map)から API オブジェクトを
	 * 生成します。
	 * 
	 * @param	account		Map で "url", "tenant", "user",
	 *						"password" をキーとしてそれぞれの値を持つもの
	 */
	public API(Map<String, String> account) {
		this(account.get("url"),
				account.get("tenant"),
				account.get("user"),
				account.get("password"));
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 保持している REST オブジェクトを返却します。
	 *
	 * @return REST オブジェクト
	 */
	public Rest getRest() {
		return rest;
	}
	
	/**
	 * 保持している Bootstrap 用の REST オブジェクトを返却します。
	 *
	 * @return	REST と同一ドメインに対する Bootstrap 用の REST オブジェクト
	 */
	public Rest getBootstrapRest() {
		return bootstrapRest;
	}
	
/*------------
 * Device API
 */
	/**
	 * 新規デバイスリクエストを取得します。
	 *
	 * @param	id		新規デバイスリクエストのオブジェクト
	 * @return	結果となる NewDeviceRequest オブジェクト(存在しない場合 null)
	 * @throws	java.io.IOException	REST異常
	 */
	public NewDeviceRequest readNewDeviceRequest(String id)
				throws IOException {
		// 存在しない場合、500 Server Error が出ることに対する対処
		try {
			Response resp = rest.get("/devicecontrol/newDeviceRequests/"+id,
									"newDeviceRequest");
			if (resp.status == 404) return null;
			return Jsonizer.fromJson(resp, NewDeviceRequest.class);
		} catch (C8yRestException c8ye) {
			Response resp = c8ye.getResponse();
			if (resp.status == 500 &&
				resp.toJson().get("message").getValue().
				startsWith("Could not find entity NewDeviceRequest")) {
				// ↑イマイチ
				//
				//Could not find entity NewDeviceRequest by ID {id}!
				return null;
			}
			System.out.println("readNewDeviceRequest() c8y error message : " +
								resp.toJson().get("message"));
			throw c8ye;
		}
	}
	
	/**
	 * 新規デバイスリクエストを登録します。
	 * 新規登録後、NewDeviceRequest の status は "WAITING_FOR_CONNECTION"
	 * になります。
	 *
	 * @param	req		新規デバイスリクエストのオブジェクト。id は必須です。
	 * @return	結果を反映した、元のオブジェクトの参照が返却されます
	 * @throws	java.io.IOException	REST異常
	 */
	public NewDeviceRequest createNewDeviceRequest(NewDeviceRequest req)
				throws IOException {
		Response resp = rest.post("/devicecontrol/newDeviceRequests",
									"newDeviceRequest", req);
		req.fill(resp);
		return req;
	}
	
	/**
	 * 新規デバイスリクエストを登録します。
	 * 新規登録後、NewDeviceRequest の status は "WAITING_FOR_CONNECTION"
	 * になります。
	 *
	 * @param	id		新規デバイスリクエストの id。
	 * @throws	java.io.IOException	REST異常
	 */
	public void createNewDeviceRequest(String id) throws IOException {
		Response resp = rest.post("/devicecontrol/newDeviceRequests",
									"newDeviceRequest", "{\"id\":\""+id+"\"}");
	}
	
	/**
	 * デバイスクレデンシャルの承認ステータスを変更します。
	 *
	 * @param	updater		デバイスリクエストの更新オブジェクト
	 * @return	結果を反映した新規オブジェクト
	 * @throws	java.io.IOException	REST異常
	 */
	public NewDeviceRequest updateNewDeviceRequest(NewDeviceRequest updater)
					throws IOException {
		Response resp = rest.put("/devicecontrol/newDeviceRequests/"+updater.id,
									"newDeviceRequest", updater);
		return Jsonizer.fromJson(resp, NewDeviceRequest.class);
	}
	
	/**
	 * デバイスクレデンシャルの承認ステータスを変更します。
	 *
	 * @param	id		デバイスリクエストの id
	 * @param	status	ステータス(@see com.ntt.tc.data.DeviceNewRequest)
	 * @throws	java.io.IOException	REST異常
	 */
	public void updateNewDeviceRequest(String id, String status)
					throws IOException {
		Response resp = rest.put("/devicecontrol/newDeviceRequests/"+id,
									"newDeviceRequest", "{\"status\":\""+status+"\"}");
	}
	
	/** 
	 * デバイスクレデンシャルの承認ステータスを変更します。
	 *
	 * @param	id		削除対象のデバイスリクエストの id
	 * @throws	java.io.IOException	REST異常
	 */
	public void deleteNewDeviceRequest(String id) throws IOException {
		Response resp = rest.delete("/devicecontrol/newDeviceRequests/"+id,
									"newDeviceRequest");
	}
	
	/**
	 * デバイスクレデンシャルを要求します。
	 * bootstrap ユーザにする必要があると思われる。
	 *
	 * @param	req		デバイスクレデンシャルのオブジェクト。id は必須です。
	 * @return	更新されたデバイスクレデンシャルが返却されます。
	 *			承認された場合、req.isValid() が true となります。
	 *			承認されなかった場合は、値は変化しません。
	 * @throws	java.io.IOException	REST異常
	 */
	public DeviceCredentials createDeviceCredentials(DeviceCredentials req)
				throws IOException {
		if (req.isValid()) return req;
		if (req.id == null || req.id.equals(""))
			throw new IllegalArgumentException("DeviceCredentials の id 値は必須です");
		Response resp = bootstrapRest.post("/devicecontrol/deviceCredentials", "deviceCredentials", req);
		if (resp.status != 404) req.fill(resp);
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
	 * @throws	java.io.IOException	REST異常
	 */
	public String readIDByExternalID(String type, String externalId)
				throws IOException {
		Response resp = rest.get("/identity/externalIds/"+type+"/"+externalId, "externalId");
		if (resp.status == 404) return null;	// not found
		
		return resp.toJson().get("managedObject.id").getValue();
	}
	
	/**
	 * type c8y_Serial の外部IDから、Managed Object ID を検索します。
	 *
	 * @param	externalId	external ID の値
	 * @return	Managed Object ID, 存在しない場合 null
	 * @throws	java.io.IOException	REST異常
	 */
	public String readIDByExternalID(String externalId) throws IOException {
		return readIDByExternalID("c8y_Serial", externalId);
	}
	
	/**
	 * 指定された Managed Object ID に指定された type で外部IDを追加します。
	 *
	 * @param	id		追加対象の Managed Object ID
	 * @param	type	外部ID(externalId)のtype
	 * @param	externalId	外部IDの値
	 * @throws	java.io.IOException	REST異常
	 */
	public void createExternalID(String id, String type, String externalId)
				throws IOException {
		JsonObject jo = JsonType.o("type",type).put("externalId",externalId);
		Response resp = rest.post("/identity/globalIds/"+id+"/externalIds", "externalId", jo);
	}
	
	/**
	 * 指定された type, 外部ID を削除します。 
	 *
	 * @param	type	external ID の type (c8y_Serial等)
	 * @param	externalId	external ID の値
	 * @throws	java.io.IOException	REST異常
	 */
	public void deleteExternalID(String type, String externalId)
				throws IOException {
		Response resp = rest.delete("/identity/externalIds/" + type +
										"/" + externalId);
		if (resp.status != 204) throw new IOException("externalId 削除失敗" + type + "/" + externalId + ":" + resp);	}
	
	/**
	 * 外部IDコレクションを取得します。
	 *
	 * @param	globalId	グローバルID(デバイスID)
	 * @return	取得された外部IDコレクション
	 * @throws	java.io.IOException	REST異常
	 */
	public ExternalIDCollection readExternalIDCollection(String globalId)
										throws IOException {
		Response resp = rest.get("/identity/globalIds/"+globalId+"/externalIds");
		return Jsonizer.fromJson(resp, ExternalIDCollection.class);
	}
	
	/**
	 * 外部IDコレクションAPIを用いて、Javaのforループで使える
	 * ExternalID の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (ExternalID id : api.externalIDs("487931") {
	 * 		( id に対する処理 )
	 * }
	 * </pre>
	 * 
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。<br>
	 *
	 * @param		globalId	Managed Object ID
	 * @return		ExternalID の iterator
	 */
	public Iterable<ExternalID> externalIDs(String globalId) {
		return ( new Iterable<ExternalID>() {
			public Iterator<ExternalID> iterator() {
				return new CollectionIterator<ExternalID>(rest,
								"/identity/globalIds/"+globalId+"/externalIds",
								"externalIds", ExternalID.class);
			}
		} );
	}
/*---------------
 * Inventory API
 */
	/**
	 * Managed Object を登録します。
	 * 登録後、渡した ManagedObject は id などが追加され、更新されます。
	 * 返却値は渡した ManagedObject です。
	 *
	 * @param	mo		登録したい Managed Object
	 * @return	登録後、更新された Managed Object
	 * @throws	java.io.IOException	REST異常
	 */
	public ManagedObject createManagedObject(ManagedObject mo)
				throws IOException {
		Response resp = rest.post("/inventory/managedObjects", "managedObject", mo);
		mo.fill(resp);
		return mo;
	}
	
	/**
	 * Managed Object を更新します。
	 * 更新後、渡した updater は変更されず、新しいインスタンスが返却されます。
	 *
	 * @param	id		更新対象の Managed Object ID
	 * @param	updater	Managed Object の変更(追加)内容
	 * @return	登録後、更新された Managed Object(updater とは別インスタンス)
	 * @throws	java.io.IOException	REST異常
	 */
	public ManagedObject updateManagedObject(String id, ManagedObject updater)
				throws IOException {
		Response resp = rest.put("/inventory/managedObjects/"+id, "managedObject", updater);
		return Jsonizer.fromJson(resp, ManagedObject.class);
	}
	
	/**
	 * Managed Object を削除します。
	 * 削除に失敗する(レスポンスコードが204(No Content)でない)と、
	 * IOException がスローされます。
	 *
	 * @param	id		削除対象の Managed Object ID
	 * @throws	java.io.IOException	REST異常
	 */
	public void deleteManagedObject(String id) throws IOException {
		Response resp = rest.delete("/inventory/managedObjects/" + id, "managedObject");
		if (resp.status != 204) throw new IOException("managed object 削除失敗" + id + ":" + resp);
	}
	
	/**
	 * Managed Object の位置情報を更新する便利メソッドです。
	 *
	 * @param	id		更新対象の Managed Object ID
	 * @param	lat		緯度(latitude)
	 * @param	lng		経度(longitude)
	 * @param	alt		高度(altitude)
	 * @throws	java.io.IOException	REST異常
	 */
	public void updateManagedObjectLocation(String id, double lat, double lng, double alt)
				throws IOException {
		JsonObject pos = new JsonObject();
		pos.put("lat", lat);
		pos.put("lng", lng);
		pos.put("alt", alt);
		JsonObject jo = JsonType.o("c8y_Position", pos);
		
		Response resp = rest.put("/inventory/managedObjects/" + id,
									"managedObject", jo);
	}
	
	/**
	 * ID から ManagedObject を取得します。
	 *
	 * @param	id		取得対象の Managed Object ID
	 * @return	ManagedObject
	 * @throws	java.io.IOException	REST異常
	 */
	public ManagedObject readManagedObject(String id) throws IOException {
		Response resp = rest.get("/inventory/managedObjects/"+id,
									"managedObject");
		return Jsonizer.fromJson(resp, ManagedObject.class);
	}
	
	/**
	 * マネージドオブジェクトコレクションを取得します。
	 * Collection API では、結果のアトミック性が保証されていないことに注意して
	 * 下さい。
	 *
	 * @param	queryString	pageSize=5&amp;currentPage=1 など
	 * @return	取得された ManagedObjectCollection
	 * @throws	java.io.IOException	REST異常
	 */
	public ManagedObjectCollection readManagedObjectCollection(String queryString)
						throws IOException {
		Response resp = rest.get("/inventory/managedObjects/?"+queryString);
		return Jsonizer.fromJson(resp, ManagedObjectCollection.class);
	}
	
	/**
	 * マネージドオブジェクトコレクションAPIを用いて、Javaのforループで使える
	 * ManagedObject の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (ManagedObject m : api.managedObjects("source=41117&amp;pageSize=15")) {
	 * 		( m に対する処理 )
	 * }
	 * </pre>
	 * 
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。<br>
	 * 利用可能な検索条件は以下の通りです。<br>
	 * type={type} 指定された type の managedObject を取得<br>
	 * fragmentType={fragmantType} 指定された fragmentType を含むもの<br>
	 * ids={ids} managedObjectId をカンマ区切りで指定(ids=41,43,68)<br>
	 * text={text} 指定値から始まるテキスト値を含むものを取得<br>
	 * query={query} クエリ条件を指定<br>
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&amp;dateTo={to}&amp;revert=true"
	 * @return	ManagedObject の iterable
	 */
	public Iterable<ManagedObject> managedObjects(final String queryString) {
		return ( new Iterable<ManagedObject>() {
			public Iterator<ManagedObject> iterator() {
				return new CollectionIterator<ManagedObject>(rest,
								"/inventory/managedObjects/?"+queryString,
								"managedObjects", ManagedObject.class);
			}
		} );
	}
	
	/**
	 * 全 ManagedObject を取得する便利メソッドです。
	 *
	 * @return		全 ManagedObject を取得する iterable
	 * @return	ManagedObject の iterable
	 */
	public Iterable<ManagedObject> managedObjects() {
		return managedObjects("");
	}
	
	/**
	 * 子デバイスを ManagedObject に追加します。
	 *
	 * @param		id		親となる managedObject の id
	 * @param		child	追加する子デバイス情報(self または id のみでもよい)
	 * @throws		java.io.IOException REST異常
	 */
	public void createChildDevice(String id, ManagedObjectReference child)
									throws IOException {
		Response resp = rest.post("/inventory/managedObjects/"+id+"/childDevices", "managedObjectReference", child);
		if (resp.status != 201)
			throw new C8yRestException("child device 追加失敗"+resp.status+resp.message+"/"+resp);
	}
	
	/**
	 * 子デバイスを ManagedObject に追加します。
	 *
	 * @param		id		親となる managedObject の id
	 * @param		childId	追加する子デバイス の id
	 * @throws		java.io.IOException REST異常
	 */
	public void createChildDevice(String id, String childId)
									throws IOException {
		ManagedObjectReference mor = new ManagedObjectReference();
		mor.managedObject = new ManagedObject();
		mor.managedObject.id = childId;
		createChildDevice(id, mor);
	}
	
	/**
	 * 子デバイスを ManagedObject から削除します。
	 *
	 * @param		id		親 managedObject の id
	 * @param		child	削除対象の 子 managedObject Reference
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteChildDevice(String id, ManagedObjectReference child)
									throws IOException {
		String cid = child.managedObject.id; // may throw Nulpo
		if (cid == null) {
			cid = child.managedObject.self;
			int i = cid.lastIndexOf("/");
			if (i == -1) throw new NullPointerException("child device 削除では、id または self を含む ManagedObjectReference を指定して下さい:"+child);
			cid = cid.substring(i+1);
		}
		
		Response resp = rest.delete("/inventory/managedObjects/"+id+"/childDevices/"+cid);
		if (resp.status != 204)
			throw new C8yRestException("child device 削除失敗"+resp.status+resp.message+"/"+resp);
	}
	
	/**
	 * 子デバイスを ManagedObject から削除します。
	 *
	 * @param		id		親 managedObject の id
	 * @param		childId	削除対象の 子 managedObject の id
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteChildDevice(String id, String childId)
									throws IOException {
		ManagedObjectReference mor = new ManagedObjectReference();
		mor.managedObject = new ManagedObject();
		mor.managedObject.id = childId;
		
		deleteChildDevice(id, mor);
	}

/*-----------------
 * Measurement API
 */
	/**
	 * ID から Measurement を取得します。
	 *
	 * @param	id		取得対象の Measurement ID
	 * @return	Measurement
	 * @throws	java.io.IOException	REST異常
	 */
	public Measurement readMeasurement(String id) throws IOException {
		Response resp = rest.get("/measurement/measurements/"+id, "measurement");
		return Jsonizer.fromJson(resp, Measurement.class);
	}
	
	/**
	 * メジャーメントを送信します。
	 *
	 * @param	measurement		送信対象のメジャーメント
	 * @throws	java.io.IOException	REST異常
	 */
	public void createMeasurement(Measurement measurement) throws IOException {
		Response resp = rest.post("/measurement/measurements/", "measurement", measurement);
	}
	
	/**
	 * 複数メジャーメントの一括送信を行います。
	 *
	 * @param	collection	送信対象のメジャーメントコレクション。
	 * @throws	java.io.IOException	REST異常
	 */
	public void createMeasurementCollection(MeasurementCollection collection)
						throws IOException {
		Response resp = rest.post("/measurement/measurements/", "measurementCollection", collection);
	}
	
	/**
	 * メジャーメントコレクションを取得します。
	 * Collection API では、結果のアトミック性が保証されていないことに注意して
	 * 下さい。
	 * 利用可能な検索条件は以下の通りです。<br>
	 * source : 指定された source の measurement を取得<br>
	 * dateFrom, dateTo : 指定された期間の measurement を取得<br>
	 * type : 指定された type の measurement を取得<br>
	 * fragmentType : 指定された fragmentType を含む measurement を取得<br>
	 * valueFragmentType : c8y_Humidity など(v8.15 では利用できず)<br>
	 * valueFragmentSeries raw など(v8.15 では利用できず)<br>
	 *
	 * @param	queryString	pageSize=5&amp;currentPage=1 など
	 * @return	取得された MeasurementCollection
	 * @throws	java.io.IOException	REST異常
	 */
	public MeasurementCollection readMeasurementCollection(String queryString)
						throws IOException {
//		Response resp = rest.get("/measurement/measurements/?"+queryString);
//		return Jsonizer.fromJson(resp, MeasurementCollection.class);
//	}
	
//	public MeasurementCollection readMeasurementCollectionByStream(String queryString)
//						throws IOException {
		Response resp = rest.getByStream("/measurement/measurements/?"+queryString);
		return Jsonizer.fromJson(resp, MeasurementCollection.class);
	}
	
	/**
	 * 指定されたデバイスで最後に報告された Measurement を取得します。
	 * query として "pageSize=1&amp;dateFrom=1970-01-01&amp;revert=true" を用います。
	 *
	 * @param		source	デバイスID
	 * @return		最後に報告された Measurement (存在しない場合 null)
	 * @throws		java.io.IOException REST異常
	 */
	public Measurement readLastMeasurement(String source) throws IOException {
		Response resp = rest.getByStream("/measurement/measurements/?source="
						+source+"&dateFrom=1970-01-01&revert=true&pageSize=1");
		MeasurementCollection mc = Jsonizer.fromJson(resp, MeasurementCollection.class);
		Measurement[] ms = mc.getMeasurements();
		if (ms.length == 0) return null;
		return ms[0];
	}
	
	/**
	 * 指定された条件で MeasurementCollection を削除します。
	 * 204 No content 以外のステータスコードが返却された場合、IOException
	 * がスローされます。
	 * このコマンドは、queryString の指定によって想定外の削除を引き起こす
	 * 可能性があるため、利用時は最新の注意を払ってください。
	 * accept として、measurement / measurementCollection いずれも
	 * 受け付けますが、measurement を指定しています。
	 *
	 * @param	queryString		クエリ文字列(source=, dateFrom= 等)
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteMeasurementCollection(String queryString)
						throws IOException {
		if (queryString == null || queryString.equals(""))
			throw new IllegalArgumentException("queryString の指定は必須です");
		Response resp = rest.delete("/measurement/measurements/?" + queryString);
		if (resp.status != 204)
			throw new IOException("MeasurementCollection 削除失敗 : " + resp);
	}
	
	/**
	 * Measurement ID を指定して特定の Measurement を削除します。
	 * 204 No Content 以外のステータスコードが返却された場合、IOException
	 * がスローされます。
	 *
	 * @param	id		Measurement ID
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteMeasurement(String id) throws IOException {
		Response resp = rest.delete("/measurement/measurements/"+id);
		if (resp.status != 204)
			throw new IOException("MeasurementCollection 削除失敗 : " + resp);
	}
	
	/**
	 * メジャーメントコレクションAPIを用いて、Javaのforループで使える
	 * Measurement の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (Measurement m : api.measurements("source=41117&amp;pageSize=15")) {
	 * 		( m に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&amp;dateTo={to}&amp;revert=true"
	 * @return	Measurement の iterable
	 */
	public Iterable<Measurement> measurements(final String queryString) {
		return new Iterable<Measurement>() {
			public Iterator<Measurement> iterator() {
				return new CollectionIterator<Measurement>(rest,
							"/measurement/measurements/?"+queryString,
							"measurements", Measurement.class);
			}
		};
	}
	
	/**
	 * 全 Measurement を取得する便利メソッドです。
	 *
	 * @return		全 Measurement を取得する iterable
	 */
	public Iterable<Measurement> measurements() {
		return measurements("");
	}
	
	/**
	 * 特定のデバイスから Measurement Series Collection を取得します。
	 * 
	 * @param	queryString	source, dateFrom, dateTo が必須, aggregationType
	 *						として DAILY, HOURLY, MINUTELY が利用可能
	 * @return	取得された MeasurementSeriesCollection (Max 5000件)
	 * @throws	java.io.IOException	REST異常
	 */
	public MeasurementSeriesCollection
			readMeasurementSeriesCollection(String queryString)
						throws IOException {
		Response resp = rest.get("/measurement/measurements/series?"+queryString);
		//String[] queries = queryString.split("&");
		//String source = null;
		//for (String query : queries) {
		//	String[] kv = query.split("=");
		//	if (kv[0].equals("source")) source = kv[1];
		//}
		MeasurementSeriesCollection result = 
				Jsonizer.fromJson(resp, MeasurementSeriesCollection.class);
		return result;
	}
	
	/**
	 * 特定のデバイスから Measurement Series Collection を取得します。
	 * 
	 * @param	source		デバイスID
	 * @param	dateFrom	取得開始日時
	 * @param	dateTo		取得終了日時
	 * @return	取得された MeasurementSeriesCollection (Max 5000件)
	 * @throws	java.io.IOException	REST異常
	 */
	public MeasurementSeriesCollection
			readMeasurementSeriesCollection(String source,
											TC_Date dateFrom,
											TC_Date dateTo)
						throws IOException {
		return readMeasurementSeriesCollection("source="+source
					+"&dateFrom="+dateFrom.getValue()
					+"&dateTo="+dateTo.getValue());
	}
	
	public MeasurementSeriesCollection
			readMeasurementSeriesCollection(String source,
											TC_Date dateFrom,
											TC_Date dateTo,
											String aggregationType)
						throws IOException {
		if (!aggregationType.equals("DAILY")
			&& !aggregationType.equals("HOURLY")
			&& !aggregationType.equals("MINUTELY") )
					throw new IllegalArgumentException("aggregationType は"
							+ " DAILY / HOURLY / MINUTELY のいずれかを"
							+ "指定してください: " + aggregationType);
		return readMeasurementSeriesCollection("source="+source
					+"&dateFrom="+dateFrom.getValue()
					+"&dateTo="+dateTo.getValue()
					+"&aggregationType="+aggregationType);
	}
	
	public Iterable<Measurement> measurementsBySeries(String source,
													TC_Date dateFrom,
													TC_Date dateTo) {
		try {
			MeasurementSeriesCollection c = readMeasurementSeriesCollection(
												source,
												dateFrom,
												dateTo);
			c.source = new ID(source);
//System.out.println(c.toString("  "));
			return c.measurements();
		} catch (IOException e) {
			throw new C8yRestRuntimeException(e);
		}
	}
	
/*-----------
 * Event API
 */
	/**
	 * ID から Event を取得します。
	 *
	 * @param	id		取得対象の Event ID
	 * @return	Event
	 * @throws	java.io.IOException	REST異常
	 */
	public Event readEvent(String id) throws IOException {
		Response resp = rest.get("/event/events/"+id, "event");
		return Jsonizer.fromJson(resp, Event.class);
	}
	
	/**
	 * イベントを送信します。
	 *
	 * @param	event		送信対象のイベント
	 * @return	送信後、id などが付与された Event
	 * @throws	java.io.IOException	REST異常
	 */
	public Event createEvent(Event event) throws IOException {
		Response resp = rest.post("/event/events/", "event", event);
		event.fill(resp);
		return event;
	}
	
	/**
	 * 位置更新イベントを送信する便利メソッドです。
	 * 次の内容の Event を POST します。
	 * <pre>
	 * {
	 *   type:"c8y_LocationUpdate",
	 *   id:{指定された source},
	 *   text:"location changed event",
	 *   c8y_Position:{lat:{指定された lat},
	 *                 lng:{指定された lng},
	 *                 alt:{指定された alt},
	 *                 trackingProtocol:{指定されたもの},
	 *                 reportReason:{指定されたもの}
	 *   }
	 * }
	 * </pre>
	 *
	 * @param		source	イベント送信元の Managed Object id
	 * @param		lat		緯度(degree)
	 * @param		lng		経度(degree)
	 * @param		alt		高度(m)
	 * @param		trackingProtocol	追跡プロトコル(TELIC など)
	 * @param		reportReason		位置情報送信理由(Time Eventなど)
	 * @return	送信後、id などが付与された Event
	 * @throws	java.io.IOException	REST異常
	 */
	public Event createLocationUpdateEvent(
						String source,
						double lat, double lng, double alt,
						String trackingProtocol,
						String reportReason) throws IOException {
		C8y_Position p = new C8y_Position();
		p.alt = alt;
		p.lat = lat;
		p.lng = lng;
		p.trackingProtocol = trackingProtocol;
		p.reportReason = reportReason;
		
		Event e = new Event(source, "c8y_LocationUpdate", "location changed event.");
		e.putExtra("c8y_Position", p);
		
		return createEvent(e);
	}
	
	/**
	 * 位置更新イベントを送信する便利メソッドです。
	 * trackingProtocol として GPS, reportReason として Normal が設定されます。
	 *
	 * @param		source	イベント送信元の Managed Object id
	 * @param		lat		緯度(degree)
	 * @param		lng		経度(degree)
	 * @param		alt		高度(m)
	 * @return	送信後、id などが付与された Event
	 * @throws	java.io.IOException	REST異常
	 */
	public Event createLocationUpdateEvent(
						String source,
						double lat, double lng, double alt) throws IOException {
		return createLocationUpdateEvent(source, lat, lng, alt, "GPS", "Normal");
	}
	
	/**
	 * イベントコレクションを取得します。
	 * Collection API では、結果のアトミック性が保証されていないことに注意して
	 * 下さい。
	 *
	 * @param	queryString	pageSize=5&amp;currentPage=1 など
	 * @return	取得された EventCollection
	 * @throws	java.io.IOException	REST異常
	 */
	public EventCollection readEventCollection(String queryString)
						throws IOException {
		Response resp = rest.get("/event/events/?"+queryString);
		return Jsonizer.fromJson(resp, EventCollection.class);
	}
	
	/**
	 * 指定されたデバイスで最後に報告された Event を取得します。
	 * query として "pageSize=1&amp;dateFrom=1970-01-01" を用います。
	 *
	 * @param		source	デバイスID
	 * @return		最後に報告された Event (存在しない場合 null)
	 * @throws		java.io.IOException REST異常
	 */
	public Event readLastEvent(String source) throws IOException {
		Response resp = rest.get("/event/events/?source="
						+source+"&dateFrom=1970-01-01&pageSize=1");
		EventCollection ec = Jsonizer.fromJson(resp, EventCollection.class);
		if (ec.events.length == 0) return null;
		return ec.events[0];
	}
	
	/**
	 * 指定された条件で EventCollection を削除します。
	 * 204 No content 以外のステータスコードが返却された場合、IOException
	 * がスローされます。
	 * このコマンドは、queryString の指定によって想定外の削除を引き起こす
	 * 可能性があるため、利用時は最新の注意を払ってください。
	 * accept として、event / measurementCollection いずれも
	 * 受け付けますが、event を指定しています。
	 *
	 * @param	queryString		クエリ文字列(source=, dateFrom= 等)
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteEventCollection(String queryString)
						throws IOException {
		if (queryString == null || queryString.equals(""))
			throw new IllegalArgumentException("queryString の指定は必須です");
		Response resp = rest.delete("/event/events/?" + queryString);
		if (resp.status != 204)
			throw new IOException("EventCollection 削除失敗 : " + resp);
	}
	
	/**
	 * Event ID を指定して特定の Measurement を削除します。
	 * 204 No Content 以外のステータスコードが返却された場合、IOException
	 * がスローされます。
	 *
	 * @param	id		イベントID
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteEvent(String id) throws IOException {
		Response resp = rest.delete("/event/events/"+id);
		if (resp.status != 204)
			throw new IOException("EventCollection 削除失敗 : " + resp);
	}
	
	/**
	 * Event に添付されているバイナリを取得します。
	 * 添付ファイルが存在しない場合、null が返却されます。
	 *
	 * @param	eventId		イベントID
	 * @return	バイナリ(添付がない場合、null)
	 * @throws		java.io.IOException REST異常
	 */
	public byte[] readBinaryOfEvent(String eventId) throws IOException {
		Response resp = rest.get("/event/events/"+eventId+"/binaries");
		if (resp.status == 404) return null;
		return resp.body;
	}
	
	/**
	 * Event に対し、ファイルを添付します。
	 * ファイル添付は、1 Event に 1 つまで可能です。
	 *
	 * @param		eventId		添付先の Event の id
	 * @param		contentType	添付ファイルの Content-Type
	 * @param		filename	ファイル名
	 * @param		binary		添付対象のバイナリ(50MByteまで)
	 * @throws		java.io.IOException REST異常
	 */
	public void createBinaryOfEvent(String eventId, String contentType,
									String filename, byte[] binary)
										throws IOException {
		Response resp = rest.postMultipart("/event/events/"+eventId+"/binaries",
								filename, contentType, binary);
		if (resp.status == 404)
			throw new C8yNoSuchObjectException("添付対象イベント "+eventId+" がありません");
		if (resp.status == 409)
			throw new IOException("イベント"+eventId+"にはすでに添付ファイルがあります");
	}
	
	/**
	 * Event に対し、添付ファイルを変更します。(multipartは不可なので修正要)
	 * ファイル添付は、1 Event に 1 つまで可能です。
	 * ファイル名は Event Id になります。
	 *
	 * @param		eventId		添付先の Event の id
	 * @param		contentType	添付ファイルの Content-Type
	 * @param		binary		添付対象のバイナリ(50MByteまで)
	 * @throws		java.io.IOException REST異常
	 */
	public void updateBinaryOfEvent(String eventId, String contentType,
									byte[] binary)
										throws IOException {
		rest.putHeader("Content-Length", String.valueOf(binary.length));
		try {
			Response resp = rest.request("/event/events/"
									+eventId+"/binaries", "PUT",
									contentType, "", binary);
			if (resp.status == 404)
				throw new C8yNoSuchObjectException("添付対象イベント "
								+eventId+" がありません");
			if (resp.status != 201)
				throw new IOException("バイナリ更新失敗"
								+resp.status+resp.message);
		} catch (IOException e) {
			rest.removeHeader("Content-Length");
			throw e;
		}
	}
	
	/**
	 * Event ID を指定して添付されているバイナリを削除します。
	 * 204 No Content 以外のステータスコードが返却された場合、IOException
	 * がスローされます。
	 *
	 * @param	eventId		Event ID
	 * @throws	java.io.IOException REST異常
	 */
	public void deleteBinaryOfEvent(String eventId) throws IOException {
		Response resp = rest.delete("/event/events/"+eventId+"/binaries");
		if (resp.status != 204)
			throw new IOException("Event の Binary 削除失敗 : " + resp);
	}
	
	/**
	 * イベントコレクションAPIを用いて、Javaのforループで使える
	 * Event の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (Event e : api.events("source=41117&amp;pageSize=15")) {
	 * 		( e に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 * <br>利用可能な検索条件は以下の通りです。<br>
	 * source : 指定された source の event を取得<br>
	 * dateFrom, dateTo : 指定された期間の event を取得<br>
	 * type : 指定された type の event を取得<br>
	 * fragmentType : 指定された fragmentType を含む event を取得<br>
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&amp;dateTo={to}&amp;revert=true"
	 * @return	Event の iterable
	 */
	public Iterable<Event> events(final String queryString) {
		return new Iterable<Event>() {
			public Iterator<Event> iterator() {
				return new CollectionIterator<Event>(rest,
							"/event/events/?"+queryString,
							"events", Event.class);
			}
		};
	}
	
	/**
	 * 全 Event を取得する便利メソッドです。
	 *
	 * @return		全 Event を取得する iterable
	 */
	public Iterable<Event> events() {
		return events("");
	}
	
/*-----------
 * Alarm API
 */
	/**
	 * ID から Alarm を取得します。
	 *
	 * @param	id		取得対象の Alarm ID
	 * @return	Alarm
	 * @throws	java.io.IOException	REST異常
	 */
	public Alarm readAlarm(String id) throws IOException {
		Response resp = rest.get("/alarm/alarms/"+id, "alarm");
		return Jsonizer.fromJson(resp, Alarm.class);
	}
	
	/**
	 * アラームを送信します。
	 *
	 * @param		alarm	送信対象のアラーム
	 * @return	送信後、id などが付与された Alarm
	 * @throws	java.io.IOException	REST異常
	 */
	public Alarm createAlarm(Alarm alarm) throws IOException {
		Response resp = rest.post("/alarm/alarms/", "alarm", alarm);
		alarm.fill(resp);
		return alarm;
	}
	
	/**
	 * アラームを送信する便利メソッドです。
	 *
	 * @param	source		alarm を発生させた managed object の id
	 * @param	type		alarm の type
	 * @param	text		alarm の説明文
	 * @param	status		Alarm の status。
	 *						ACTIVE/ACKNOWLEDGED/CLEARED のいずれかである
	 *						必要があります
	 * @param	severity	Alarm の severity。
	 *						CRITICAL/MAJOR/MINOR/WARNING のいずれかである
	 *						必要があります
	 * @return	送信後、id などが付与された Alarm
	 * @throws	java.io.IOException	REST異常
	 */
	public Alarm createAlarm(String source, String type, String text,
					String status, String severity) throws IOException {
		return createAlarm(new Alarm(source, type, text, status, severity));
	}
	
	/**
	 * アラームを更新します
	 *
	 * @param	id		更新対象の Alarm ID
	 * @param	updater	更新内容を含む Alarm
	 * @return	更新された alarm で、新しいインスタンスが生成されます。
	 * @throws		java.io.IOException REST異常
	 */
	public Alarm updateAlarm(String id, Alarm updater) throws IOException {
		Response resp = rest.put("/alarm/alarms/"+id, "alarm", updater);
		return Jsonizer.fromJson(resp, Alarm.class);
	}
	
	/**
	 * アラームコレクションを取得します。
	 * Collection API では、結果のアトミック性が保証されていないことに注意して
	 * 下さい。
	 *
	 * @param	queryString	pageSize=5&amp;currentPage=1 など
	 * @return	取得された AlarmCollection
	 * @throws	java.io.IOException	REST異常
	 */
	public AlarmCollection readAlarmCollection(String queryString)
						throws IOException {
		Response resp = rest.get("/alarm/alarms/?"+queryString);
		return Jsonizer.fromJson(resp, AlarmCollection.class);
	}
	
	/**
	 * アラームコレクションAPIを用いて、Javaのforループで使える
	 * Alarm の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (Alarm a : api.alarms("source=41117&amp;pageSize=15")) {
	 * 		( a に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 * <br>利用可能な検索条件は以下の通りです。<br>
	 * source : 指定された source の alarm を取得<br>
	 * dateFrom, dateTo : 指定された期間の alarm を取得<br>
	 * status : 指定された status の alarm を取得<br>
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&amp;dateTo={to}&amp;revert=true"
	 * @return	Alarm の iterable
	 */
	public Iterable<Alarm> alarms(final String queryString) {
		return new Iterable<Alarm>() {
			public Iterator<Alarm> iterator() {
				return new CollectionIterator<Alarm>(rest,
								"/alarm/alarms/?"+queryString,
								"alarms", Alarm.class);
			}
		};
	}
	
	/**
	 * 全 Alarm を取得する便利メソッドです。
	 *
	 * @return		全 Alarm を取得する iterable
	 */
	public Iterable<Alarm> alarms() {
		return alarms("");
	}
	
/*---------------
 * Operation API
 */
	/**
	 * ID から Operation を取得します。
	 *
	 * @param	id		取得対象の Operation ID
	 * @return	Operation
	 * @throws	java.io.IOException	REST異常
	 */
	public Operation readOperation(String id) throws IOException {
		Response resp = rest.get("/devicecontrol/operations/"+id, "operation");
		return Jsonizer.fromJson(resp, Operation.class);
	}
	
	/**
	 * オペレーションを送信します。
	 *
	 * @param	operation		送信対象のオペレーション
	 * @return	送信後、id などが付与された Operation(渡された Operation が
	 *			変更されたもの)
	 * @throws	java.io.IOException	REST異常
	 */
	public Operation createOperation(Operation operation) throws IOException {
		Response resp = rest.post("/devicecontrol/operations/", "operation", operation);
		operation.fill(resp);
		return operation;
	}
	
	/**
	 * オペレーションを更新します。
	 * オペレーションでは、updater を再利用することが少ないと想定されるため、
	 * 結果は更新された updater インスタンスとして返却されます。
	 *
	 * @param	operationId	オペレーションID
	 * @param	updater		送信対象のオペレーション(更新されます)
	 * @return	送信後、結果更新された Operation
	 * @throws	java.io.IOException	REST異常
	 */
	public Operation updateOperation(String operationId, Operation updater)
						throws IOException {
		Response resp = rest.put("/devicecontrol/operations/"+operationId, "operation", updater);
		updater.fill(resp);
		return updater;
	}
	
	/**
	 * オペレーションステータスを更新する便利メソッドです。
	 *
	 * @param	operationId	オペレーションID
	 * @param	status		オペレーションステータス
	 *						(SUCCESSFUL/FAILED/PENDING/EXECUTING)
	 * @throws	java.io.IOException	REST異常
	 */
	public void updateOperationStatus(String operationId, String status)
						throws IOException {
		if (!"SUCCESSFUL".equals(status) &&
			!"FAILED".equals(status) &&
			!"PENDING".equals(status) &&
			!"EXECUTING".equals(status) )
				throw new IllegalArgumentException("オペレーションステータスは SUCCESSFUL/FAILED/PENDING/EXECUTING のいずれかを指定してください");
		Response resp = rest.put("/devicecontrol/operations/"+operationId,
									"operation", "{\"status\":\""+status+"\"}");
	}
	
	/**
	 * オペレーションコレクションを取得します。
	 * Collection API では、結果のアトミック性が保証されていないことに注意して
	 * 下さい。
	 *
	 * @param	queryString	pageSize=5&amp;currentPage=1 など
	 * @return	取得された OperationCollection
	 * @throws	java.io.IOException	REST異常
	 */
	public OperationCollection readOperationCollection(String queryString)
						throws IOException {
		Response resp = rest.get("/devicecontrol/operations/?"+queryString);
		return Jsonizer.fromJson(resp, OperationCollection.class);
	}
	
	/**
	 * オペレーションコレクションAPIを用いて、Javaのforループで使える
	 * Operation の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (Operation o : api.operations("deviceId=41117&amp;pageSize=15")) {
	 * 		( o に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 * <br>利用可能な検索条件は以下の通りです。<br>
	 * deviceId : 指定された deviceId に対する operation を取得<br>
	 * agentId : 指定された agentId に対する operation を取得<br>
	 * status : 指定された status の operation を取得<br>
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&amp;dateTo={to}&amp;revert=true"
	 * @return	Opearation の iterable
	 */
	public Iterable<Operation> operations(final String queryString) {
		return new Iterable<Operation>() {
			public Iterator<Operation> iterator() {
				return new CollectionIterator<Operation>(rest,
								"/devicecontrol/operations/?"+queryString,
								"operations", Operation.class);
			}
		};
	}
	
	/**
	 * 全 Operation を取得する便利メソッドです。
	 *
	 * @return		全 Operation を取得する iterable
	 */
	public Iterable<Operation> operations() {
		return operations("");
	}
	
/*------------
 * Tenant API
 */
	/**
	 * テナントコレクションを GET します。
	 *
	 * @param		queryString		クエリ文字列
	 * @return		取得された TenantCollection
	 * @throws	java.io.IOException	REST異常
	 */
	public TenantCollection readTenantCollection(String queryString)
								throws IOException {
		Response resp = rest.get("/tenant/tenants/?"+queryString, "tenantCollection");
		return Jsonizer.fromJson(resp, TenantCollection.class);
	}
	
	/**
	 * テナントコレクション API を用いて、Java の for ループで利用できる
	 * Iterable を取得します。
	 *
	 * @param	queryString	取得条件を指定します。
	 * @return	Tenant の iterable
	 */
	public Iterable<Tenant> tenants(String queryString) {
		return new Iterable<Tenant>() {
			public Iterator<Tenant> iterator() {
				return new CollectionIterator<Tenant>(rest,
								"/tenant/tenants/?"+queryString,
								"tenants", Tenant.class);
			}
		};
	}
	
	/**
	 * テナントコレクション API を用いて、Java の for ループで利用できる
	 * Iterable を取得します。
	 * @return	Tenant の iterable
	 */
	public Iterable<Tenant> tenants() {
		return tenants("");
	}
	
	/**
	 * テナントを新規作成します。
	 * パラメータ指定に不備があると、Json parse エラーが出ます(紛らわしい)。
	 * すでにある id を指定すると、C8yRestException e で
	 * e.getBody().get("error") の値が "tenants/Duplicate" になります。
	 *
	 * @param		tenant		新規作成するテナントの情報
	 *							(作成結果によって上書きされます)
	 * @return		登録結果によって更新された tenant
	 * @throws	java.io.IOException	REST異常
	 */
	public Tenant createTenant(Tenant tenant) throws IOException {
		Response resp = rest.post("/tenant/tenants", "tenant", tenant);
		tenant.fill(resp);
		return tenant;
	}
	
	/**
	 * 指定された id のテナント情報を取得します。
	 *
	 * @param		id		テナント id
	 * @return		テナント情報
	 * @throws	java.io.IOException	REST異常
	 */
	public Tenant readTenant(String id) throws IOException {
		Response resp = rest.get("/tenant/tenants/"+id, "tenant");
		if (resp.status == 404)
			throw new C8yNoSuchObjectException("tenant "+id+" is not found."+
						resp);
		return Jsonizer.fromJson(resp, Tenant.class);
	}
	
	/**
	 * 既存のテナントを更新します。
	 *
	 * @param		id		テナント id
	 * @param		updater	更新オブジェクト
	 * @return		更新後の Tenant 情報
	 * @throws	java.io.IOException	REST異常
	 */
	public Tenant updateTenant(String id, Tenant updater) throws IOException {
		Response resp = rest.put("/tenant/tenants/"+id, "tenant", updater);
		return Jsonizer.fromJson(resp, Tenant.class);
	}
	
	/**
	 * 指定された id のテナント情報を削除します。
	 *
	 * @param		id		テナント id
	 * @throws	java.io.IOException	REST異常
	 */
	public void deleteTenant(String id) throws IOException {
		Response resp = rest.delete("/tenant/tenants/"+id, "tenant");
	}
	
	/**
	 * 子テナントに登録されているアプリケーションを登録します。
	 * 指定された tenant や id のアプリケーションが存在しない場合、
	 * C8yNoSuchObjectException がスローされます。
	 *
	 * @param		tenant		子テナントの id
	 * @param		id			アプリケーション id
	 * @throws		C8yNoSuchObjectException 指定された tenant または id の
	 *				Application が存在しない
	 * @throws		java.io.IOException REST異常
	 */
	public void createApplication(String tenant, String id) throws IOException {
		JsonObject app = new JsonObject();
		app.put("application", new JsonObject());
		app.put("application.id", id);
		
		String ep = "/tenant/tenants/"+tenant+"/applications/";
		app.put("application.self", rest.getLocation()+ep+id);
		
		Response resp = rest.post(ep, "applicationReference", app);
		if (resp.status == 404) throw new C8yNoSuchObjectException(resp);
	}
	
	/**
	 * 子テナントに登録されているアプリケーションを削除します。
	 * 指定された tenant や id のアプリケーションが存在しない場合、
	 * C8yNoSuchObjectException がスローされます。
	 *
	 * @param		tenant		子テナントの id
	 * @param		id			アプリケーション id
	 * @throws		C8yNoSuchObjectException 指定された tenant または id の
	 *				Application が存在しない
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteApplication(String tenant, String id) throws IOException {
		Response resp = rest.delete("/tenant/tenants/" + tenant
							+ "/applications/"+id);
		if (resp.status == 404) throw new C8yNoSuchObjectException(resp);
		if (resp.status != 204) throw new IOException("application 削除失敗:"+resp.status+resp);
	}
	
	/**
	 * テナント使用状況統計コレクションを取得します。
	 * Collection API では、結果のアトミック性が保証されていないことに注意して
	 * 下さい。
	 *
	 * @param	queryString	pageSize=5&amp;currentPage=1 など
	 * @return	取得された TenantUsageStatisticsCollection
	 * @throws	java.io.IOException	REST異常
	 */
	public TenantUsageStatisticsCollection
			readTenantUsageStatisticsCollection(String queryString)
					throws IOException {
		Response resp = rest.get("/tenant/statistics?"+queryString);
		return Jsonizer.fromJson(resp, TenantUsageStatisticsCollection.class);
	}
	
	/**
	 * 自テナント使用状況統計コレクションAPIを用いて、Javaのforループで
	 * 使える UsageStatistics の iterator を取得します。
	 * この API では、自テナントの１日ごとの統計情報履歴を取得できます。
	 * 毎日 15.00.00Z の時刻で登録されています。
	 * <pre>
	 * 使用例：
	 * for (UsageStatistics u : api.usageStatistics("dateFrom=2017-08-01&amp;dateTill=2017-09-05&amp;pageSize=15")) {
	 * 		( u に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 *
	 * @param	queryString	取得条件を指定します。
	 * @return	UsageStatistics の iterable
	 */
	public Iterable<UsageStatistics> usageStatistics(final String queryString) {
		return new Iterable<UsageStatistics>() {
			public Iterator<UsageStatistics> iterator() {
				return new CollectionIterator<UsageStatistics>(
							rest, "/tenant/statistics/?"+queryString,
							"usageStatistics", UsageStatistics.class);
			}
		};
	}
	
	/**
	 * 全 UsageStatistics を取得する便利メソッドです。
	 *
	 * @return		全 UsageStatistics を取得する iterable
	 */
	public Iterable<UsageStatistics> usageStatistics() {
		return usageStatistics("");
	}
	
	/**
	 * 自テナントの使用状況サマリを取得します。
	 * 10分置き程度に更新される最新情報が取得可能です。
	 *
	 * @param	queryString	dateFrom, dateTill で期間を指定します。
	 *					省略した場合、月初から現在までとなります。
	 *					yyyy-MM-dd の形式で、dateTill を省略することもできます。
	 *					文書上、指定できるように書いてありますが、実際には
	 *					効かないようです。(2019/1/18)
	 * @return	取得された UsageStatistics
	 * @throws	java.io.IOException	REST異常
	 */
	public UsageStatistics readTenantStatisticsSummary(String queryString)
								throws IOException {
		Response resp = rest.get("/tenant/statistics/summary?"+queryString);
		return Jsonizer.fromJson(resp, UsageStatistics.class);
	}
	
	/**
	 * 自テナントの使用状況サマリを取得します。
	 * 10分置き程度に更新される最新情報が取得可能です。
	 *
	 * @return	取得された UsageStatistics
	 * @throws	java.io.IOException	REST異常
	 */
	public UsageStatistics readTenantStatisticsSummary()
								throws IOException {
		return readTenantStatisticsSummary("");
	}
	
	/**
	 * 全サブテナントの使用状況サマリを取得します。
	 * management テナントの場合、全テナントのサマリが取得できます。
	 *
	 * @param	queryString		クエリ文字列 dateFrom, dateTo のみ利用可能
	 * @return	テナントの使用状況(tenantId ごとにすべてのテナント分)
	 * @throws		java.io.IOException REST異常
	 */
	public UsageStatistics[] readAllTenantsSummary(String queryString)
								throws IOException {
		Response resp = rest.get("/tenant/statistics/allTenantsSummary?"+queryString);
		return Jsonizer.toArray(resp, new UsageStatistics[0]);
	}
	
	/**
	 * 全サブテナントの使用状況サマリを取得します。
	 * management テナントの場合、全テナントのサマリが取得できます。
	 *
	 * @return	テナントの使用状況(tenantId ごとにすべてのテナント分)
	 * @throws		java.io.IOException REST異常
	 */
	public UsageStatistics[] readAllTenantsSummary() throws IOException {
		return readAllTenantsSummary("");
	}
	
	/**
	 * このテナントのテナントオプションを登録します。
	 *
	 * @param		option		登録対象のテナントオプション
	 *							(成功時上書きされます)
	 * @return		登録結果によって更新された option
	 * @throws	java.io.IOException	REST異常
	 */
	public Option createOption(Option option) throws IOException {
		Response resp = rest.post("/tenant/options/", "option", option);
		option.fill(resp);
		return option;
	}
	
	/**
	 * テナントオプションAPIを用いて、Javaのforループで使える
	 * Option の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (Option o : api.tenantOptions("pageSize=15")) {
	 * 		( o に対する処理 )
	 * }
	 * </pre>
	 *
	 * @param	queryString	クエリ文字列
	 * @return	UsageStatistics の iterable
	 */
	public Iterable<Option> tenantOptions(final String queryString) {
		return new Iterable<Option>() {
			public Iterator<Option> iterator() {
				return new CollectionIterator<Option>(
							rest, "/tenant/options/?"+queryString,
							"options", Option.class);
			}
		};
	}
	
	/**
	 * 全 Option を取得する便利メソッドです。
	 *
	 * @return		全 Option を取得する iterable
	 */
	public Iterable<Option> tenantOptions() {
		return tenantOptions("");
	}
	
	/**
	 * 指定されたカテゴリ、キーのテナントオプションを取得します。
	 *
	 * @param	category	カテゴリ
	 * @param	key			キー
	 * @return	取得された Option
	 * @throws	java.io.IOException	REST異常
	 */
	public Option readOption(String category, String key) throws IOException {
		Response resp = rest.get("/tenant/options/"+category+"/"+key, "option");
		return Jsonizer.fromJson(resp, Option.class);
	}
	
	/**
	 * 指定されたカテゴリ、キーのテナントオプションを変更します。
	 *
	 * @param	category	カテゴリ
	 * @param	key			キー
	 * @param	updater		変更部分を表すオブジェクト
	 * @return	変更後の Option で、新しいインスタンスが返却されます。
	 * @throws	java.io.IOException	REST異常
	 */
	public Option updateOption(String category, String key, Option updater)
									throws IOException {
		Response resp = rest.put("/tenant/options/"+category+"/"+key, "option", updater);
		return Jsonizer.fromJson(resp, Option.class);
	}
	
	
/*----------
 * User API
 */
	/**
	 * UserCollection を get します。
	 *
	 * @param		queryString		queryとして指定する文字列
	 * @return		UserCollection
	 * @throws	java.io.IOException	REST異常
	 */
	public UserCollection readUserCollection(String queryString)
								throws IOException {
		Response resp = rest.get("/user/"+rest.getTenant()+"users", "userCollection");
		return Jsonizer.fromJson(resp, UserCollection.class);
	}
	
	/**
	 * 指定されたユーザーを生成します。
	 * ユーザーの登録先は、この API オブジェクトが保持する Rest オブジェクト
	 * のテナントです。
	 *
	 * @param	user	生成するユーザー
	 * @return	生成された結果を含むユーザー(引数オブジェクトが更新されます)
	 * @throws	java.io.IOException	REST異常
	 */
	public User createUser(User user) throws IOException {
		Response resp = rest.post("/user/"+rest.getTenant()+"users", "user", user);
		user.fill(resp);
		return user;
	}
	
	/**
	 * 指定された id を持つユーザーの情報を取得します。
	 *
	 * @param	id		ユーザーのid(ログイン時に使用する名前)
	 * @return	取得されたユーザーオブジェクト
	 * @throws	java.io.IOException	REST異常
	 */
	public User readUser(String id) throws IOException {
		Response resp = rest.get("/user/"+rest.getTenant()+"users/"+id, "user");
		return Jsonizer.fromJson(resp, User.class);
	}
	
	/**
	 * ユーザをユーザ名で取得します。
	 *
	 * @param		name	ユーザ名
	 * @return		User オブジェクト
	 * @throws	java.io.IOException	REST異常
	 */
	public User readUserByName(String name) throws IOException {
		Response resp = rest.get("/user/"+rest.getTenant()+"userByName/"+name, "user");
		return Jsonizer.fromJson(resp, User.class);
	}
	
	/**
	 * ユーザ情報を更新します。
	 *
	 * @param		id		ユーザID
	 * @param		updater	更新内容を示す User オブジェクト
	 * @return		更新後の新規 User オブジェクト
	 * @throws	java.io.IOException	REST異常
	 */
	public User updateUser(String id, User updater) throws IOException {
		Response resp = rest.put("/user/"+rest.getTenant()+"users/"+id, "user", updater);
		return Jsonizer.fromJson(resp, User.class);
	}
	
	/**
	 * ユーザ情報を削除します。
	 *
	 * @param		id		ユーザID
	 * @throws	java.io.IOException	REST異常
	 */
	public void deleteUser(String id) throws IOException {
		Response resp = rest.delete("/user/"+rest.getTenant()+"users/"+id, "user");
		if (resp.status != 204) // No content
			throw new C8yRestException("deleteUser failed: " + resp);
	}
	
	/**
	 * ユーザ情報にロールを追加します。
	 *
	 * @param		id		ユーザID(ログインID)
	 * @param		role	ロール文字列(Roleクラスの定数値参照)
	 * @see			com.ntt.tc.data.users.Role
	 * @throws		java.io.IOException REST異常
	 */
	public void createRoleToUser(String id, String role) throws IOException {
		if (!Role.ROLES.contains(role))
			throw new IllegalArgumentException("Role 値が不正です : "
						+ role + " /可能な値 : " + Role.ROLES);
		RoleReference rr = new RoleReference();
		rr.role = new Role();
		rr.role.putExtra("self", rest.getLocation() + "/user/roles/" + role); // self を指定する
		
		Response resp = rest.post("/user/"+rest.getTenant()+"users/"+id+"/roles", "roleReference", rr);
		
	}
	
	/**
	 * ユーザ情報のロールを削除します。
	 *
	 * @param		id		ユーザID(ログインID)
	 * @param		role	ロール文字列(Roleクラスの定数値参照)
	 * @see			com.ntt.tc.data.users.Role
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteRoleFromUser(String id, String role) throws IOException {
		if (!Role.ROLES.contains(role))
			throw new IllegalArgumentException("Role 値が不正です : "
						+ role + " /可能な値 : " + Role.ROLES);
		Response resp = rest.delete("/user/"+rest.getTenant()+"users/"+id+"/roles/"+role);
	}
	
	/**
	 * グループ(グローバルロール)にロールを追加します。
	 *
	 * @param		id		グループID(整数値)
	 * @param		role	ロール文字列(Roleクラスの定数値参照)
	 * @see			com.ntt.tc.data.users.Role
	 * @throws		java.io.IOException REST異常
	 */
	public void createRoleToGroup(int id, String role) throws IOException {
		if (!Role.ROLES.contains(role))
			throw new IllegalArgumentException("Role 値が不正です : "
						+ role + " /可能な値 : " + Role.ROLES);
		RoleReference rr = new RoleReference();
		rr.role = new Role();
		rr.role.putExtra("self", rest.getLocation() + "/user/roles/" + role); // self を指定する
		
		Response resp = rest.post("/user/"+rest.getTenant()+"groups/"+id+"/roles", "roleReference", rr);
		
	}
	
	/**
	 * グループ(グローバルロール)のロールを削除します。
	 *
	 * @param		id		グループID(整数値)
	 * @param		role	ロール文字列(Roleクラスの定数値参照)
	 * @see			com.ntt.tc.data.users.Role
	 * @throws		java.io.IOException REST異常
	 */
	public void deleteRoleFromUser(int id, String role) throws IOException {
		if (!Role.ROLES.contains(role))
			throw new IllegalArgumentException("Role 値が不正です : "
						+ role + " /可能な値 : " + Role.ROLES);
		Response resp = rest.delete("/user/"+rest.getTenant()+"groups/"+id+"/roles/"+role);
	}
	
	/**
	 * ユーザーコレクションAPIを用いて、Javaのforループで使える
	 * UserCollection の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (User u : api.users("pageSize=300")) {
	 * 		( u に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 *
	 * @param	queryString	取得条件を指定します。
	 * @return	User の iterable
	 */
	public Iterable<User> users(final String queryString) {
		return users(rest.getTenant(), queryString);
	}
	
	/**
	 * テナント名を指定してユーザーコレクションを取得します。
	 *
	 * @param		tenant	テナント名
	 * @param		queryString	pageSize 等の設定
	 * @return	User の iterable
	 */
	public Iterable<User> users(final String tenant, final String queryString) {
		return new Iterable<User>() {
			public Iterator<User> iterator() {
				return new CollectionIterator<User>(
							rest, "/user/"+tenant+"users/?"+queryString,
							"users", User.class);
			}
		};
	}
	
	/**
	 * 全 User を取得する便利メソッドです。
	 *
	 * @return		全 User を取得する iterable
	 */
	public Iterable<User> users() {
		return users("");
	}
	
/*------------
 * Binary API
 */
	/**
	 * バイナリデータを送信します。
	 *
	 * @param	filename	ファイル名
	 * @param	mimetype	MIME Type
	 * @param	binary		送信バイナリデータ
	 * @return	バイナリデータに対応する ManagedObject
	 * @throws	java.io.IOException	REST異常
	 */
	public ManagedObject createBinary(String filename,
							String mimetype,
							byte[] binary) throws IOException {
		Response resp = rest.postBinary(filename,mimetype, binary);
		return Jsonizer.fromJson(resp, ManagedObject.class);
	}

/*----------------------------
 * Real-time Notification API
 */
	// C8yEventDispatcher を利用のこと

/*-------------------------
 * Real-time statement API
 */
	/**
	 * 指定された id の Module を取得します。
	 * 存在しない場合、C8yNoSuchObjectException がスローされます。
	 * <pre>
	 * {"error":"cep-server/Not Found",
	 *  "info":"https://www.cumulocity.com/guides/reference-guide/#error_reporting",
	 *  "message":"Not found module file for id 6 : Could not find entity CepModule by ID 6!"
	 * }
	 * </pre>
	 *
	 * @param		id		モジュール id
	 * @return		Module
	 * @throws	java.io.IOException	REST異常
	 */
	public Module readModule(String id) throws IOException {
		// end point は文書に記載がなく、管理APの電文を見てわかった
		Response resp = rest.get("/cep/modules/"+id, "cepModule");
		
		if (resp.status == 404)
			throw new C8yNoSuchObjectException("指定された id("+
						id+")のモジュールは存在しません:"+resp);
		return Jsonizer.fromJson(resp, Module.class);
	}
	
	/**
	 * 指定された id の Module スクリプトを取得します。
	 * 存在しない場合、C8yNoSuchObjectException がスローされます。
	 * <pre>
	 * {"error":"cep-server/Not Found",
	 *  "info":"https://www.cumulocity.com/guides/reference-guide/#error_reporting",
	 *  "message":"Not found module file for id 6 : Could not find entity CepModule by ID 6!"
	 * }
	 * </pre>
	 *
	 * @param		id		モジュール id
	 * @return		スクリプト文字列
	 * @throws	java.io.IOException	REST異常
	 */
	public String readModuleText(String id) throws IOException {
		// end point は文書に記載がなく、管理APの電文を見てわかった
		Response resp = rest.request("/cep/modules/"+id+"?text", "GET", "cepModule", "text/plain", null); // text/plain 指定のため impl を使う
		if (resp.status == 404)
			throw new C8yNoSuchObjectException("指定された id("+
						id+")のモジュールは存在しません:"+resp);
		
		return new String(resp.body, "UTF-8");
	}
	
	/**
	 * ModuleCollection を取得します。
	 *
	 * @return		登録されている CEP Module の Collection
	 * @throws	java.io.IOException	REST異常
	 */
	public ModuleCollection readModuleCollection() throws IOException {
		Response resp = rest.get("/cep/modules", "cepModuleCollection");
		return Jsonizer.fromJson(resp, ModuleCollection.class);
	}
	
	/**
	 * 指定された CEP モジュールを登録します。
	 * 登録後、デプロイ済み状態になります。
	 * CEP コンパイルエラーとなる場合、 C8yRestException がスローされます。
	 * スクリプト内の改行は LF が標準と思われますが、CR+LF でも問題なく
	 * 動作しています。
	 *
	 * @param		moduleName		モジュール名
	 * @param		text			CEPステートメント
	 * @throws	java.io.IOException	REST異常
	 */
	public void createModule(String moduleName, String text)
					throws IOException {
		byte[] toPost = ("module "+moduleName+";\n"+text).getBytes("UTF-8");
		
		Response resp = rest.postMultipart("/cep/modules", "cepmodule", toPost);
	}
	
	public void createModule(String moduleName, List<String> lines)
					throws IOException {
		createModule(moduleName, lines.stream().collect(Collectors.joining("\n")));
	}
	
	/**
	 * 指定された path にあるファイルの内容で CEP モジュールを登録します。
	 *
	 * @param		moduleName	モジュール名
	 * @param		fileName	ファイルのpath
	 * @throws	java.io.IOException	REST異常
	 */
	public void createModuleTextByFile(String moduleName,
										String fileName) throws IOException {
		createModule(moduleName, Files.readAllLines(Paths.get(fileName), UTF8));
	}
	
	/**
	 * CEP モジュールのデプロイステータスを変更します。
	 *
	 * @param		id			CEP のモジュール id
	 * @param		deployed	デプロイ(true)、アンデプロイ(false)
	 * @throws	java.io.IOException	REST異常
	 */
	public void updateModule(String id, boolean deployed)
					throws IOException {
		String status = (deployed)?"DEPLOYED":"NOT_DEPLOYED";
		Module m = new Module();
		m.status = status;
		
		Response resp = rest.put("/cep/modules/"+id, "cepModule", m);
	}
	
	/**
	 * CEP モジュールのスクリプトを変更します。
	 * スクリプト内の改行は LF が標準と思われますが、CR+LF でも問題なく
	 * 動作しています。
	 *
	 * @param		id			CEP のモジュール id
	 * @param		moduleName	モジュール名
	 * @param		text		スクリプト
	 * @throws	java.io.IOException	REST異常
	 */
	public void updateModuleText(String id, String moduleName, String text)
					throws IOException {
		Response resp = rest.request("/cep/modules/"+id, "PUT", "text/plain", "", ("module "+moduleName+";\n"+text).getBytes("UTF-8")); // text/plain 指定のため impl を使う
	}
	
	public void updateModuleText(String id, String moduleName, List<String> lines)
					throws IOException {
		updateModuleText(id, moduleName, lines.stream().collect(Collectors.joining("\n")));
	}
	
	/**
	 * 指定された path にあるファイルの内容で CEP モジュールを更新します。
	 *
	 * @param		id			CEP のモジュール id
	 * @param		moduleName	モジュール名
	 * @param		fileName	ファイルのpath
	 * @throws	java.io.IOException	REST異常
	 */
	public void updateModuleTextByFile(String id,
										String moduleName,
										String fileName) throws IOException {
		updateModuleText(id, moduleName, Files.readAllLines(Paths.get(fileName), UTF8));
	}
	
	/**
	 * モジュールコレクションAPIを用いて、Javaのforループで使える
	 * Module の iterator を取得します。
	 *
	 * @param		queryString	pageSize 等の設定
	 * @return		登録されている module の itarable
	 */
	public Iterable<Module> modules(final String queryString) {
		return new Iterable<Module>() {
			public Iterator<Module> iterator() {
				return new CollectionIterator<Module>(
							rest, "/cep/modules?"+queryString,
							"modules", Module.class);
			}
		};
	}
	
	/**
	 * 全モジュールコレクションを取得する便利メソッドです。
	 *
	 * @return		登録されている module の itarable
	 */
	public Iterable<Module> modules() {
		return modules("");
	}
	
/*---------------
 * SmartRest API
 */
	/**
	 * SmartRest テンプレートを登録します.
	 * 本 API では header を操作するため、他の API と排他制御する必要が
	 * あることに注意して下さい。
	 *
	 * @param		xid		テンプレートの X-Id
	 * @param		csv		登録するテンプレート本体(一行で指定して下さい)
	 * @return		テンプレートの Managed Object ID
	 * @throws		java.io.IOException REST異常
	 */
	public String createSmartRest(String xid, String csv)
						throws IOException {
		if (!csv.startsWith("10,") && !csv.startsWith("11,"))
			throw new IllegalArgumentException("csv の最初の要素は 10(request) または 11(response) である必要があります");
		Response resp = null;
		// X-Id は header ではなく、body に設定することも可能。
		// 15,xid とすると、以降の csv 行の X-Id が xid となる
		//
		// header を追加
		csv = "15," + xid + "\r\n" + csv;	// body 側に設定
		
		resp = rest.post("/s/", csv);
		
		// header を削除
		String result = resp.toString();
		if (!result.startsWith("20"))
			throw new C8yRestException("SmartREST template registration failed: " + result);
		return result.substring(3);
	}
	
	
	
}

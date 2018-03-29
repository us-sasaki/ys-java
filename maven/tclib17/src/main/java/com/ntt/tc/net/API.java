package com.ntt.tc.net;

import java.io.IOException;
import java.util.Map;
import java.util.Iterator;

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
	
	protected Rest rest;
	protected Rest bootstrapRest;
	
/*-------------
 * constructor
 */
	public API(Rest rest) {
		this.rest = rest;
		bootstrapRest = new Rest(rest.getLocation(), "management", new String(Base64.decodeFromString(BUSR)), new String(Base64.decodeFromString(BPSS)));
	}
	
	public API(String location, String tenant, String user, String pass) {
		this(new Rest(location, tenant, user, pass));
	}
	
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
	 */
	public NewDeviceRequest readNewDeviceRequest(String id)
				throws IOException {
		// 存在しない場合、500 Server Error が出ることに対する対処
		try {
			Response resp = rest.get("/devicecontrol/newDeviceRequests/"+id,
									"newDeviceRequest");
			if (resp.code == 404) return null;
			return Jsonizer.fromJson(resp, NewDeviceRequest.class);
		} catch (C8yRestException c8ye) {
			Response resp = c8ye.getResponse();
			if (resp.code == 500 &&
				resp.body.get("message").getValue().
				startsWith("Could not find entity NewDeviceRequest")) {
				// ↑イマイチ
				//
				//Could not find entity NewDeviceRequest by ID {id}!
				return null;
			}
			System.out.println("readNewDeviceRequest() c8y error message : " +
								resp.body.get("message"));
			throw c8ye;
		}
	}
	
	/**
	 * 新規デバイスリクエストを登録します。
	 * 新規登録後、NewDeviceRequest の status は "WAITING_FOR_CONNECTION"
	 * になります。
	 *
	 * @param	req		新規デバイスリクエストのオブジェクト。id は必須です。
	 * @return	結果オブジェクトで、元のオブジェクトの参照が返却されます
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
	 */
	public void createNewDeviceRequest(String id) throws IOException {
		Response resp = rest.post("/devicecontrol/newDeviceRequests",
									"newDeviceRequest", "{\"id\":\""+id+"\"}");
	}
	
	/**
	 * デバイスクレデンシャルの承認ステータスを変更します。
	 *
	 * @param	updater		デバイスリクエストの更新オブジェクト
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
	 */
	public DeviceCredentials createDeviceCredentials(DeviceCredentials req)
				throws IOException {
		if (req.isValid()) return req;
		if (req.id == null || req.id.equals(""))
			throw new IllegalArgumentException("DeviceCredentials の id 値は必須です");
		Response resp = bootstrapRest.post("/devicecontrol/deviceCredentials", "deviceCredentials", req);
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
	
	/**
	 * 指定された type, 外部ID を削除します。 
	 *
	 * @param	type	external ID の type (c8y_Serial等)
	 * @param	externalId	external ID の値
	 */
	public void deleteExternalID(String type, String externalId)
				throws IOException {
		Response resp = rest.delete("/identity/externalIds/" + type +
										"/" + externalId);
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
	 */
	public ManagedObject updateManagedObject(String id, ManagedObject updater)
				throws IOException {
		Response resp = rest.put("/inventory/managedObjects/"+id, "managedObject", updater);
		return Jsonizer.fromJson(resp, ManagedObject.class);
	}
	
	/**
	 * Managed Object を削除します。
	 * 削除に失敗すると、IOException がスローされます。
	 *
	 * @param	id		削除対象の Managed Object ID
	 */
	public void deleteManagedObject(String id) throws IOException {
		Response resp = rest.delete("/inventory/managedObjects/" + id, "managedObject");
		if (resp.code != 204) throw new IOException("managed object 削除失敗" + id + ":" + resp);
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
		
		Response resp = rest.put("/inventory/managedObjects/" + id,
									"managedObject", jo);
	}
	
	/**
	 * ID から ManagedObject を取得します。
	 *
	 * @param	id		取得対象の Managed Object ID
	 * @return	ManagedObject
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
	 * @param	queryString	pageSize=5&currentPage=1 など
	 * @return	取得された ManagedObjectCollection
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
	 * for (ManagedObject m : api.managedObjects("source=41117&pageSize=15")) {
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
	 *						 "dateFrom={from}&dateTo={to}&revert=true"
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
	 */
	public Iterable<ManagedObject> managedObjects() throws IOException {
		return managedObjects("");
	}

/*-----------------
 * Measurement API
 */
	/**
	 * ID から Measurement を取得します。
	 *
	 * @param	id		取得対象の Measurement ID
	 * @return	Measurement
	 */
	public Measurement readMeasurement(String id) throws IOException {
		Response resp = rest.get("/measurement/measurements/"+id, "measurement");
		return Jsonizer.fromJson(resp, Measurement.class);
	}
	
	/**
	 * メジャーメントを送信します。
	 *
	 * @param	measurement		送信対象のメジャーメント
	 */
	public void createMeasurement(Measurement measurement) throws IOException {
		Response resp = rest.post("/measurement/measurements/", "measurement", measurement);
	}
	
	/**
	 * 複数メジャーメントの一括送信を行います。
	 *
	 * @param	collection	送信対象のメジャーメントコレクション。
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
	 *
	 * @param	queryString	pageSize=5&currentPage=1 など
	 * @return	取得された MeasurementCollection
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
	 * メジャーメントコレクションAPIを用いて、Javaのforループで使える
	 * Measurement の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (Measurement m : api.measurements("source=41117&pageSize=15")) {
	 * 		( m に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&dateTo={to}&revert=true"
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
	public Iterable<Measurement> measurements() throws IOException {
		return measurements("");
	}
	
/*-----------
 * Event API
 */
	/**
	 * ID から Event を取得します。
	 *
	 * @param	id		取得対象の Event ID
	 * @return	Event
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
	 *   id:<<指定された source>>,
	 *   text:"location changed event",
	 *   c8y_Position:{lat:<<指定された lat>>,
	 *                 lng:<<指定された lng>>,
	 *                 alt:<<指定された alt>>,
	 *                 trackingProtocol:<<指定されたもの>>,
	 *                 reportReason:<<指定されたもの>>
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
	 * @param	queryString	pageSize=5&currentPage=1 など
	 * @return	取得された EventCollection
	 */
	public EventCollection readEventCollection(String queryString)
						throws IOException {
		Response resp = rest.get("/event/events/?"+queryString);
		return Jsonizer.fromJson(resp, EventCollection.class);
	}
	
	/**
	 * イベントコレクションAPIを用いて、Javaのforループで使える
	 * Event の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (Event e : api.events("source=41117&pageSize=15")) {
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
	 *						 "dateFrom={from}&dateTo={to}&revert=true"
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
	public Iterable<Event> events() throws IOException {
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
	 */
	public Alarm createAlarm(Alarm alarm) throws IOException {
		Response resp = rest.post("/alarm/alarms/", "alarm", alarm);
		alarm.fill(resp);
		return alarm;
	}
	
	/**
	 * アラームを送信する便利メソッドです。
	 *
	 * @param	sourceId	alarm を発生させた managed object の id
	 * @param	type		alarm の type
	 * @param	text		alarm の説明文
	 * @param	status		Alarm の status。
	 *						ACTIVE/ACKNOWLEDGED/CLEARED のいずれかである
	 *						必要があります
	 * @param	severity	Alarm の severity。
	 *						CRITICAL/MAJOR/MINOR/WARNING のいずれかである
	 *						必要があります
	 * @return	送信後、id などが付与された Alarm
	 */
	public Alarm createAlarm(String source, String type, String text,
					String status, String severity) throws IOException {
		return createAlarm(new Alarm(source, type, text, status, severity));
	}
	
	/**
	 * アラームコレクションを取得します。
	 * Collection API では、結果のアトミック性が保証されていないことに注意して
	 * 下さい。
	 *
	 * @param	queryString	pageSize=5&currentPage=1 など
	 * @return	取得された AlarmCollection
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
	 * for (Alarm a : api.alarms("source=41117&pageSize=15")) {
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
	 *						 "dateFrom={from}&dateTo={to}&revert=true"
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
	public Iterable<Alarm> alarms() throws IOException {
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
	 * @param	updater		送信対象のオペレーション(更新されます)
	 * @return	送信後、結果更新された Operation
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
	 * @param	status		オペレーションステータス
	 *						(SUCCESSFUL/FAILED/PENDING/EXECUTING)
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
	 * @param	queryString	pageSize=5&currentPage=1 など
	 * @return	取得された OperationCollection
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
	 * for (Operation o : api.operations("deviceId=41117&pageSize=15")) {
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
	 *						 "dateFrom={from}&dateTo={to}&revert=true"
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
	public Iterable<Operation> operations() throws IOException {
		return operations("");
	}
	
/*------------
 * Tenant API
 */
	/**
	 * テナント使用状況統計コレクションを取得します。
	 * Collection API では、結果のアトミック性が保証されていないことに注意して
	 * 下さい。
	 *
	 * @param	queryString	pageSize=5&currentPage=1 など
	 * @return	取得された TenantUsageStatisticsCollection
	 */
	public TenantUsageStatisticsCollection
			readTenantUsageStatisticsCollection(String queryString)
					throws IOException {
		Response resp = rest.get("/tenant/statistics?"+queryString);
		return Jsonizer.fromJson(resp, TenantUsageStatisticsCollection.class);
	}
	
	/**
	 * テナント使用状況統計コレクションAPIを用いて、Javaのforループで使える
	 * UsageStatistics の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (UsageStatistics u : api.usageStatistics("dateFrom=2017-08-01&dateTill=2017-09-05&pageSize=15")) {
	 * 		( u に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 *
	 * @param	queryString	取得条件を指定します。
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
	 * テナントオプションAPIを用いて、Javaのforループで使える
	 * Option の iterator を取得します。
	 * <pre>
	 * 使用例：
	 * for (Option o : api.tenantOptions("pageSize=15")) {
	 * 		( o に対する処理 )
	 * }
	 * </pre>
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
	 * @return	変更後の Option で、新しいインスタンスが返却されます。
	 */
	public Option updateOption(String category, String key, Option updater) throws IOException {
		Response resp = rest.put("/tenant/options/"+category+"/"+key, "option", updater);
		return Jsonizer.fromJson(resp, Option.class);
	}
	
	
/*----------
 * User API
 */
	/**
	 * Rest オブジェクトからテナント文字列を取得します。<br>
	 * (1) rest.tenant があれば、それを返します<br>
	 * (2) なければ、url の "://" と次の "." の間の文字列を返します。<br>
	 *
	 * @return		テナント文字列
	 */
	private String getTenant() {
		String tenant = rest.tenant;
		if (tenant == null || tenant.equals("")) {
			tenant = rest.urlStr;
			int i = tenant.indexOf("://");
			int j = tenant.indexOf('.', i);
			tenant = tenant.substring(i+3, j) + "/";
		}
		return tenant;
	}
	
	/**
	 * 指定されたユーザーを生成します。
	 * ユーザーの登録先は、この API オブジェクトが保持する Rest オブジェクト
	 * のテナントです。
	 *
	 * @param	user	生成するユーザー
	 * @return	生成された結果を含むユーザー(引数オブジェクトが更新されます)
	 */
	public User createUser(User user) throws IOException {
		Response resp = rest.post("/user/"+getTenant()+"users", "user", user);
		user.fill(resp);
		return user;
	}
	
	/**
	 * 指定された id を持つユーザーの情報を取得します。
	 *
	 * @param	id		ユーザーのid(ログイン時に使用する名前)
	 * @return	取得されたユーザーオブジェクト
	 */
	public User readUser(String id) throws IOException {
		Response resp = rest.get("/user/"+getTenant()+"users/"+id, "user");
		return Jsonizer.fromJson(resp, User.class);
	}
	
	/**
	 * ユーザをユーザ名で取得します。
	 *
	 * @param		name	ユーザ名
	 * @return		User オブジェクト
	 */
	public User readUserByName(String name) throws IOException {
		Response resp = rest.get("/user/"+getTenant()+"userByName/"+name, "user");
		return Jsonizer.fromJson(resp, User.class);
	}
	
	/**
	 * ユーザ情報を更新します。
	 *
	 * @param		id		ユーザID
	 * @param		updater	更新内容を示す User オブジェクト
	 * @return		更新後の User オブジェクト
	 */
	public User updateUser(String id, User updater) throws IOException {
		Response resp = rest.put("/user/"+getTenant()+"users/"+id, "user", updater);
		return Jsonizer.fromJson(resp, User.class);
	}
	
	/**
	 * ユーザ情報を削除します。
	 *
	 * @param		id		ユーザID
	 */
	public void deleteUser(String id) throws IOException {
		Response resp = rest.delete("/user/"+getTenant()+"users/"+id, "user");
		if (resp.code != 204) // No content
			throw new C8yRestException("deleteUser failed: " + resp);
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
	 */
	public Iterable<User> users(final String queryString) {
		return users(rest.getTenant(), queryString);
	}
	
	/**
	 * テナント名を指定してユーザーコレクションを取得します。
	 *
	 * @param		tenant	テナント名
	 * @param		queryString	pageSize 等の設定
	 */
	public Iterable<User> users(final String tenant, final String queryString) {
		return new Iterable<User>() {
			public Iterator<User> iterator() {
				return new CollectionIterator<User>(
							rest, "/user/"+tenant+"/users?"+queryString,
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
}

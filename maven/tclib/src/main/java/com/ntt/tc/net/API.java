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
	
	public API(Map<String, String> account) {
		this(account.get("url"),
				account.get("tenant"),
				account.get("user"),
				account.get("password"));
	}
	
/*------------------
 * instance methods
 */
	public Rest getRest() {
		return rest;
	}

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
		Response resp = rest.post("/inventory/managedObjects", "managedObject", mo);
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
		//System.out.println(jo.toString("  "));
		
		Response resp = rest.put("/inventory/managedObjects/" + id, "managedObject", jo);
	}
	
	/**
	 * ID から ManagedObject を取得します。
	 *
	 * @param	id		取得対象の Managed Object ID
	 * @return	ManagedObject
	 */
	public ManagedObject readManagedObject(String id) throws IOException {
		Response resp = rest.get("/inventory/managedObjects/"+id, "managedObject");
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
	 * に変換され、元の例外は cause として設定されます。
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&dateTo={to}&revert=true"
	 */
	public Iterable<ManagedObject> managedObjects(final String queryString) {
		return new Iterable<ManagedObject>() {
			@Override
			public java.util.Iterator<ManagedObject> iterator() {
				return new CollectionIterator<ManagedObject>(rest, "/inventory/managedObjects/?"+queryString, ManagedObject.class);
			}
		};
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
	 *
	 * @param	queryString	pageSize=5&currentPage=1 など
	 * @return	取得された MeasurementCollection
	 */
	public MeasurementCollection readMeasurementCollection(String queryString)
						throws IOException {
		Response resp = rest.get("/measurement/measurements/?"+queryString);
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
			@Override
			public java.util.Iterator<Measurement> iterator() {
				return new CollectionIterator<Measurement>(rest, "/measurement/measurements/?"+queryString, Measurement.class);
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
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&dateTo={to}&revert=true"
	 */
	public Iterable<Event> events(final String queryString) {
		return new Iterable<Event>() {
			@Override
			public java.util.Iterator<Event> iterator() {
				return new CollectionIterator<Event>(rest, "/event/events/?"+queryString, Event.class);
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
	 * for (Alarm e : api.alarms("source=41117&pageSize=15")) {
	 * 		( e に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&dateTo={to}&revert=true"
	 */
	public Iterable<Alarm> alarms(final String queryString) {
		return new Iterable<Alarm>() {
			@Override
			public java.util.Iterator<Alarm> iterator() {
				return new CollectionIterator<Alarm>(rest, "/alarm/alarms/?"+queryString, Alarm.class);
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
	 * @return	送信後、id などが付与された Operation
	 */
	public Operation createOperation(Operation operation) throws IOException {
		Response resp = rest.post("/devicecontrol/operations/", "operation", operation);
		operation.fill(resp);
		return operation;
	}
	
	/**
	 * オペレーションを更新します。
	 *
	 * @param	updater		送信対象のオペレーション
	 * @return	送信後、id などが付与された Operation
	 */
	public Operation updateOperation(Operation updater) throws IOException {
		Response resp = rest.put("/devicecontrol/operations/", "operation", updater);
		updater.fill(resp);
		return updater;
	}
	
	/**
	 * オペレーションステータスを更新する便利メソッドです。
	 *
	 * @param	status		オペレーションステータス
	 *						(SUCCESSFUL/FAILED/PENDING/EXECUTING)
	 */
	public void updateOperationStatus(String status) throws IOException {
		if (!"SUCCESSFUL".equals(status) &&
			!"FAILED".equals(status) &&
			!"PENDING".equals(status) &&
			!"EXECUTING".equals(status) )
				throw new IllegalArgumentException("オペレーションステータスは SUCCESSFUL/FAILED/PENDING/EXECUTING のいずれかを指定してください");
		Response resp = rest.put("/devicecontrol/operations/", "operation", "{\"status\":\""+status+"\"}");
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
	 * for (Operation o : api.alarms("source=41117&pageSize=15")) {
	 * 		( o に対する処理 )
	 * }
	 * </pre>
	 *
	 * API 操作時に IOException が発生した場合、C8yRestRuntimeException
	 * に変換され、元の例外は cause として設定されます。
	 *
	 * @param	queryString	取得条件を指定します。例："source={id}",
	 *						 "dateFrom={from}&dateTo={to}&revert=true"
	 */
	public Iterable<Operation> operations(final String queryString) {
		return new Iterable<Operation>() {
			@Override
			public java.util.Iterator<Operation> iterator() {
				return new CollectionIterator<Operation>(rest, "/devicecontrol/operations/?"+queryString, Operation.class);
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
	/**
	 * ハンドシェークを行います。
	 * 結果は、配列で返却されます。
	 *
	 * @param	hr		要求メッセージの配列
	 * @return		応答メッセージの配列
	 */
	public HandshakeResponse[] createHandshake(HandshakeRequest[] hr)
									throws IOException {
		JsonArray ja = new JsonArray();
		for (HandshakeRequest r : hr)
			ja.push(r.toJson());
		Response resp = rest.post("/cep/realtime", ja);
		return Jsonizer.toArray(resp, new HandshakeResponse[0]);
	}
	
	/**
	 * サブスクライブを行います。
	 * 結果は、配列で返却されます。
	 */
	public SubscribeResponse[] createSubscribe(SubscribeRequest[] sr)
									throws IOException {
		JsonArray ja = new JsonArray();
		for (SubscribeRequest r : sr)
			ja.push(r.toJson());
		Response resp = rest.post("/cep/realtime", ja);
		return Jsonizer.toArray(resp, new SubscribeResponse[0]);
	}
	
	/**
	 * 接続をを行います。
	 * 結果は、配列で返却されます。
	 */
	public ConnectResponse[] createConnect(ConnectRequest[] cr)
									throws IOException {
		JsonArray ja = new JsonArray();
		for (ConnectRequest r : cr)
			ja.push(r.toJson());
		Response resp = rest.post("/cep/realtime", ja);
		return Jsonizer.toArray(resp, new ConnectResponse[0]);
	}
	
}

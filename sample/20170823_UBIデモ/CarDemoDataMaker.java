import java.io.IOException;
import java.io.FileReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import abdom.data.json.*;
import abdom.data.json.object.*;

import com.ntt.tc.data.*;
import com.ntt.tc.data.alarms.*;
import com.ntt.tc.data.events.*;
import com.ntt.tc.data.identity.*;
import com.ntt.tc.data.inventory.*;
import com.ntt.tc.data.measurements.*;
import com.ntt.tc.data.sensor.*;
import com.ntt.tc.net.Rest;

public class CarDemoDataMaker {

/*-----------
 * constants
 */
	private static final String URL		= "https://telema.iot-trialpack.com";
	private static final String TENANT	= "telema";
	private static final String USER	= "sasaki";
	private static final String PASS	= "appuri89";
	
//	private static final String URL		= "https://iottf_asset.je1.thingscloud.ntt.com";
//	private static final String TENANT	= "iottf_asset";
//	private static final String USER	= "admin";
//	private static final String PASS	= "IOTiot0033";
	
	/**
	 * CSV 形式の ManagedObject データを読み取ります。
	 */
	private static final JsonArray moData;
	static {
		Csv2Json c2j = new Csv2Json();
		moData = c2j.read("CarData.csv");
	}
	
/*
 * static variables
 */
	private static Rest rest;
	private static Rest transientRest;
	private static C8yConnector c8y = new C8yConnector(URL, TENANT, USER, PASS);
	
/*
 * instance methods
 */
	private Date date;
	private ManagedObject[] mos;

/*-------------
 * constructor
 */
	public CarDemoDataMaker() {
		date	= new Date();
		mos = new ManagedObject[moData.size()];
	}

/*------------------
 * instance methods
 */
	/**
	 * ManagedObject として、情報を登録します。
	 * すでに登録されていた場合、上書きします。
	 */
	public void makeMOs() throws IOException {
		Rest r = c8y.getRest();
		
		for (int i = 0; i < moData.size(); i++) {
			ManagedObject mo = getMOByIndex(i);
			
			mos[i] = createMO(i);
			Rest.Response resp = null;
			if (mo != null) {
				// 既存登録があったら、上書き
				mos[i].id = mo.id;
				c8y.updateManagedObject(mos[i]); // mos[i] は update される
				// 上書きしない場合
				// resp = r.get("/inventory/managedObjects/" + mo.id, "managedObject");
			} else {
				// 既存登録がなければ、新規登録
				c8y.createManagedObject(mos[i]); // mos[i] は update される
			}
			// extId(nttcom_NameStr) に登録
			registerNameStr(mos[i].id, i);
		}
	}
	
	/**
	 * 一般のアラーム情報を登録するメソッドです。
	 * 発生時間は、現在時間を基準とします。
	 *
	 * @param	index	managedObject のインデックス(csvでの行番号)
	 * @param	text	alarm に設定する text
	 * @param	type	alarm に設定する type
	 * @param	severity	alarm に設定する severity
	 * @param	past	発生時間のオフセット(現在時刻を基準としてどれだけ過去か)
	 */
	private void makeAlarm(int index, String text, String type, String severity, long past)
						throws IOException {
		c8y.createAlarm(mos[index], text, type, severity, past);
	}
	
	/**
	 * 一般のアラーム情報を登録するメソッドです。
	 */
	private void makeAlarm(int index, String text, String type, String severity, int dayoffst, int hour, int minute)
						throws IOException {
		c8y.createAlarm(mos[index], text, type, severity, dayoffst, hour, minute);
	}
	
/*---------------
 * class methods
 */
	/**
	 * ManagedObject 情報を作成
	 *
	 * @param	name	名前(name に設定)
	 */
	private static ManagedObject createMO(int index) {
		ManagedObject mo = new ManagedObject();
		
		// デフォルト値の設定
		mo.type = "c8y_CarDemo";
		mo.c8y_IsDevice = new JsonObject();
		mo.c8y_Hardware = new C8y_Hardware();
		mo.c8y_Hardware.model = "Dummy Tracker";
		mo.c8y_Hardware.serialNumber = "Demo data";
		mo.c8y_Hardware.revision = "000";
		mo.putExtra("demodata", new JsonObject());
		mo.putExtra("carType", new JsonValue("車種"));
		
		// ファイルの値で上書き
		mo.fill(moData.get(index));
		return mo;
	}
	
	/**
	 * 指定された nameStr から ManagedObject を検索し、返却します。
	 *
	 * @param	name	enternalId (名前情報)
	 * @return	ManagedObject。存在しない場合、 null
	 */
	private static HashMap<String, ManagedObject> cache = new HashMap<String, ManagedObject>();
	
	private static ManagedObject getMOByIndex(int index) throws IOException {
		String nameStr = moData.get(index).get("nttcom_NameStr").getValue();
		ManagedObject result = cache.get(nameStr);
		if (result != null) return result;
		
		Rest r = c8y.getRest();
		Rest.Response resp = r.get("/identity/externalIds/nttcom_NameStr/" + nameStr);
		if (resp.code >= 400) return null;
		ExternalID extId = Jsonizer.fromJson(resp, ExternalID.class);
		
		// extId.managedObject は id, self しかないため、ManagedObject 本体を
		// 取得
		
		resp = r.get("/inventory/managedObjects/" + extId.managedObject.id);
		if (resp.code != 200) {
			System.out.println("getMOByIndex で managementObject 取得失敗");
			System.out.println(resp.code);
			System.out.println(resp.message);
			System.exit(-1);
		} else {
			try {
				extId.managedObject.fill(resp);
			} catch (Exception e) {
				System.out.println(resp.toString("  "));
			}
		}
		cache.put(nameStr, extId.managedObject);
		return extId.managedObject;
	}
	
	/**
	 * 指定された managedObject.id に externalId(nttcom_NameStr) として
	 * nameStr を登録します。
	 *
	 * @param	moId	managedObject.id
	 * @param	nameStr	external id として登録する名称(ASCII文字限定)
	 */
	private static void registerNameStr(String moId, int index)
							throws IOException {
		String nameStr = moData.get(index).get("nttcom_NameStr").getValue();
		
		// 続けて externalId として nameStr を登録
		ExternalID extId = new ExternalID();
		extId.externalId	= nameStr;
		extId.type			= "nttcom_NameStr";
		Rest.Response r2 = c8y.getRest().post("/identity/globalIds/"+moId+"/externalIds", extId);
		if (r2.code != 201) {
			System.out.println("ExternalId 登録失敗");
			System.out.println(r2.code);
			System.out.println(r2.message);
		} else { // 405 collision は、同一 id/extId では発生しない
			extId.fill(r2);
		}
	}
	
	
	/**
	 * 指定された番号のMOに関し、現在位置、軌跡を送信します。
	 * date が null の場合、現在時刻が利用されます
	 */
	private void sendLocation(int index, double lat, double lng, String date)
									throws IOException {
		Rest r = c8y.getRest();
		
		// Managed Object のアップデートをする
		ManagedObject mo = getMOByIndex(index);
		if (mo == null) 
			throw new RuntimeException(moData.get(index).get("nttcom_NameStr").toString() + "の登録がありません。オブジェクトを登録して下さい。");
		ManagedObject updater = new ManagedObject();
		// 既存登録があったら、上書き
		updater.c8y_Position = new C8y_Position();
		updater.c8y_Position.lat = lat;
		updater.c8y_Position.lng = lng;
		updater.c8y_Position.alt = 0d;
		//updater.c8y_Position.reportReason = "Time Event";
		//updater.c8y_Position.trackingProtocol = "TELIC";
//		Rest.Response resp = null;
		Rest.Response resp = r.put("/inventory/managedObjects/" + mo.id, "managedObject", updater);
		if (resp.code != 200 && resp.code != 201) {
			// update 失敗
			System.out.println("setLocationEvent で MO 変更失敗");
			System.out.println(resp.code);
			System.out.println(resp.message);
			System.out.println(resp);
			System.exit(-1);
		}
		
		// Event としても POST する
		// TRANSIENT モードを利用(Mongoに書かない)
		Rest tr = c8y.getRest();
		Event event = new Event(mo, "c8y_LocationUpdate","位置情報更新");
		if (date == null || date.equals("")) event.time = new TC_Date();
		else event.time.set(date);
		event.putExtra("c8y_Position", updater.c8y_Position);
		resp = tr.post("/event/events", "event", event);
		if (resp.code != 201) { // not Created
			System.out.println("setLocationEvent で event 登録失敗");
			System.out.println(resp.code);
			System.out.println(resp.message);
			System.exit(-1);
		}
	}
	
	/**
	 * 指定されたMOの Measurement データを登録します。
	 * date が null の場合、現在時刻が利用されます
	 */
	private void sendMeasurement(int index, double speed, String date)
						throws IOException {
		// identity サービスで取得する
		ManagedObject mo = getMOByIndex(index);
		
		Measurement m = new Measurement(mo, "demoData");
		if (date == null || date.equals("")) m.time = new TC_Date();
		else m.time.set(date);
		
		// Accel
//		m.putExtra("c8y_AccelerationMeasurement", JsonType.o("acceleration", JsonType.o("value", accel).put("unit", "m/s2")));
		
		// Speed
		m.putExtra("c8y_SpeedMeasurement", JsonType.o("speed", JsonType.o("value", speed).put("unit", "Km/h")));
		
		Rest r = c8y.getRest();
		Rest.Response resp = r.post("/measurement/measurements", "measurement", m);
     	if (resp.code != 201) { // not Created
	     	throw new IOException("An error occurred while sending measurement."+resp.message);
		}
	}
	
	private void sendNineAxes(int index, JsonType pos, String date)
						throws IOException {
		// identity サービスで取得する
		ManagedObject mo = getMOByIndex(index);
		
		Measurement m = new Measurement(mo, "demoData");
		if (date == null || date.equals("")) m.time = new TC_Date();
		else m.time.set(date);
		
		// 9軸センサ情報を取得する
		double[] d = new double[9];
		d[0] = pos.get("accx").doubleValue();
		d[1] = pos.get("accy").doubleValue();
		d[2] = pos.get("accz").doubleValue();
		d[3] = pos.get("rx").doubleValue();
		d[4] = pos.get("ry").doubleValue();
		d[5] = pos.get("rz").doubleValue();
		d[6] = pos.get("magx").doubleValue();
		d[7] = pos.get("magy").doubleValue();
		d[8] = pos.get("magz").doubleValue();
		
		// ノイズ添付
		for (int i = 0; i < 9; i++) {
			d[i] = d[i] + Math.random() - 0.5d;
		}
		
		HKSMeasurement h = new HKSMeasurement(d[0], d[1], d[2],
												d[3], d[4], d[5],
												d[6], d[7], d[8]);
		m.putExtra("nine_axes", h.toJson());
//System.out.println(m.toString("  "));
		Rest r = c8y.getRest();
		Rest.Response resp = r.post("/measurement/measurements", "measurement", m);
     	if (resp.code != 201) { // not Created
	     	throw new IOException("An error occurred while sending measurement."+resp.message);
		}
	}
	
/**
 * 軌跡を順次送るスレッド(inner class)
 */
	private class OrbitSender extends Thread {
		int index;
		JsonType orbit;
		
		public OrbitSender(int index, String orbitJson) {
			this.index = index;
			try {
				orbit = JsonType.parse(orbitJson);
			} catch (JsonParseException e) {
				System.out.println(orbitJson);
				System.exit(-1);
			}
			if (orbit.getType() != JsonType.TYPE_ARRAY)
				throw new IllegalArgumentException("orbit must be JsonArray.");
		}
		
		//
		// 移動軌跡を表示するループ処理
		//
		public void run() {
			int offset = (int)(Math.random() * orbit.size());
			boolean first = true;
			try {
				while (true) {
					// 行き
					int i = (first)? offset : 0;
					first = false;
					for (; i < orbit.size(); i++) {
						JsonType pos = orbit.get(i);
						double lat = pos.get("lat").doubleValue();
						double lng = pos.get("lng").doubleValue();
						int speed = pos.get("speed").intValue();
						System.out.println("index = " + index + "  :sending (lat,lng)=("+lat+","+lng+")"+speed+"km/h");
						try {
							CarDemoDataMaker.this.sendLocation(index, lat, lng, null);
							CarDemoDataMaker.this.sendMeasurement(index, speed, null);
							CarDemoDataMaker.this.sendNineAxes(index, pos, null);
						} catch (IOException e) {
							e.printStackTrace();
						}
						Thread.sleep(rnd(1000L));
					}
					Thread.sleep(rnd(3000L));
					// 帰り
					for (i = orbit.size()-1; i >= 0; i--) {
						JsonType pos = orbit.get(i);
						double lat = pos.get("lat").doubleValue();
						double lng = pos.get("lng").doubleValue();
						int speed = pos.get("speed").intValue();
						System.out.println("index = " + index + "  :sending (lat,lng)=("+lat+","+lng+")"+speed+"km/h");
						try {
							CarDemoDataMaker.this.sendLocation(index, lat, lng, null);
							CarDemoDataMaker.this.sendMeasurement(index, speed, null);
						} catch (IOException e) {
							e.printStackTrace();
						}
						Thread.sleep(rnd(1000L));
					}
					Thread.sleep(rnd(3000L));
				}
			} catch (InterruptedException ie) {
			}
		}
		private long rnd(long base) {
			return (long)(base * (0.8 + Math.random() / 4));
		}
	}
	
	public void start(String[] orbits) {
		// 経路情報を順次送信
		for (int i = 0; i < orbits.length; i++) {
			if (orbits[i] == null || orbits[i].equals("")) continue;
			OrbitSender sender = new OrbitSender(i, orbits[i]);
			sender.start();
		}
	}
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		CarDemoDataMaker w = new CarDemoDataMaker();
		w.makeMOs();
		
		System.out.println("sending alarms");
		w.makeAlarm(0, "開始しました", "car_InfoLockOpen", "WARNING",0, 8, 50);
		w.makeAlarm(0, "終了しました", "car_InfoLockClose", "WARNING", 0, 10, 12);
		w.makeAlarm(1, "開始しました", "car_InfoLockOpen", "WARNING",-1, 12, 50);
		w.makeAlarm(1, "終了しました", "car_InfoLockClose", "WARNING", -1, 14, 12);
		w.makeAlarm(2, "開始しました", "car_InfoLockOpen", "WARNING",-1, 15, 50);
		w.makeAlarm(2, "終了しました", "car_InfoLockClose", "WARNING", -1, 13, 12);
		w.makeAlarm(3, "開始しました", "car_InfoLockOpen", "WARNING",-1, 16, 50);
		w.makeAlarm(3, "終了しました", "car_InfoLockClose", "WARNING", -1, 22, 12);
		w.makeAlarm(4, "開始しました", "car_InfoLockOpen", "WARNING",-1, 18, 50);
		w.makeAlarm(4, "終了しました", "car_InfoLockClose", "WARNING", -1, 20, 12);
		
		System.out.println("sending location data...");
		// 経路
		String[] orbit = new String[moData.size()];
		for (int i = 0; i < orbit.length; i++) {
			orbit[i] = readJsonFile(moData.get(i).get("orbitFileName").getValue()).toString();
		}
		
		w.start(orbit);
	}
	
	private static JsonType readJsonFile(String fname) throws IOException {
		FileReader fr = new FileReader(fname);
		JsonType jt = JsonType.parse(fr);
		fr.close();
		
		addSpeed(jt);
		addAcceleration(jt);
		return jt;
	}
	
	private static void addSpeed(JsonType jt) {
		JsonType prev = jt.get(0);
		prev.put("speed", new JsonValue(0));
		prev.put("speedx", new JsonValue(0));
		prev.put("speedy", new JsonValue(0));
		prev.put("speedz", new JsonValue(0));
		prev.put("magx", new JsonValue(0));
		prev.put("magy", new JsonValue(0));
		prev.put("magz", new JsonValue(0));
		for (int i = 1; i < jt.size(); i++) {
			JsonType next = jt.get(i);
			// 緯度が 1 違うと、大体 111.3km 離れている
			double lat = next.get("lat").doubleValue() - prev.get("lat").doubleValue();
			lat *= 111.3;
			// 経度が 1 違うと、大体90.5km離れている
			double lng = next.get("lng").doubleValue() - prev.get("lng").doubleValue();
			lng *= 90.5;
			double distance = Math.sqrt(lat*lat + lng*lng);
			
			// てきとうな正規化で km/h っぽくする
			double speed = distance * 150;
			if (speed > 80) speed = 130 * speed / (speed + 50);
			
			// distance(km) をそのままスピードにしている
			next.put("speed", new JsonValue((int)speed));
			// x, y, z 軸も入れる
			// 単位は m/s に変換
			next.put("speedx", new JsonValue( (lng * 150 * 1000 / 3600) ));
			next.put("speedy", new JsonValue( (lat * 150 * 1000 / 3600) ));
			next.put("speedz", new JsonValue(0));
			
			// magnet も入れる
			// lat, lng 方向のベクトルを正規化するだけ
			// 東京付近では 45uT(マイクロテスラ)くらいらしいのでこの値に正規化
			double magx = lat;
			double magy = lng;
			double lml = Math.sqrt(magx*magx + magy*magy);
			if (lml < 0.0001d) {
				magx = 0;
				magy = 0;
			} else {
				magx = magx / lml * 45.0;
				magy = magy / lml * 45.0;
			}
			
			next.put("magx", new JsonValue(magx));
			next.put("magy", new JsonValue(magy));
			next.put("magz", new JsonValue(0));
			
			prev = next;
		}
	}
	
	private static void addAcceleration(JsonType jt) {
		JsonType prev =jt.get(0);
		prev.put("acc", new JsonValue(0));
		prev.put("accx", new JsonValue(0));
		prev.put("accy", new JsonValue(0));
		prev.put("accz", new JsonValue(0));
		prev.put("rx", new JsonValue(0));
		prev.put("ry", new JsonValue(0));
		prev.put("rz", new JsonValue(0));
		
		for (int i = 1; i < jt.size(); i++) {
			JsonType next = jt.get(i);
			
			// 二点間の時間が 1 秒としている。
			double px = prev.get("speedx").doubleValue();
			double py = prev.get("speedy").doubleValue();
			double nx = next.get("speedx").doubleValue();
			double ny = next.get("speedy").doubleValue();
			
			
			double x = (nx - px)/1.0d;
			double y = (ny - py)/1.0d;
			double acc = Math.sqrt(x*x + y*y);
			
			next.put("acc", new JsonValue(acc));
			
			next.put("accx", new JsonValue(x));
			next.put("accy", new JsonValue(y));
			next.put("accz", new JsonValue(9.8d));
			
			// 角速度も計算
			// ベクトルの内積を利用し、cosθを求める
			double l = Math.sqrt(nx*nx+ny*ny) * Math.sqrt(px*px+py*py);
			double cos = (nx * px + ny * py);
			if (l < 0.0001d) cos = 0d;
			else cos = cos / l;
			if (cos > 1d) cos = 1d;
			if (cos < -1d) cos = -1d;
			//
			double r = Math.acos(cos); // 0 ～ π
			r = r / Math.PI * 180; // 0 ～ 180
			if (nx * py - ny * px < 0) r = -r; // 外積の公式より
			r = r / 1.0d; // 1秒ごととしている
			
			next.put("rx", new JsonValue(r));
			next.put("ry", new JsonValue(0));
			next.put("rz", new JsonValue(0));
			
//System.out.println("acc = " + acc + " / accx = " + x + " / accy = " + y + " / r = " + r);
			prev = next;
		}
	}
	
}

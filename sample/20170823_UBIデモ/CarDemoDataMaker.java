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
	 * CSV �`���� ManagedObject �f�[�^��ǂݎ��܂��B
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
	 * ManagedObject �Ƃ��āA����o�^���܂��B
	 * ���łɓo�^����Ă����ꍇ�A�㏑�����܂��B
	 */
	public void makeMOs() throws IOException {
		Rest r = c8y.getRest();
		
		for (int i = 0; i < moData.size(); i++) {
			ManagedObject mo = getMOByIndex(i);
			
			mos[i] = createMO(i);
			Rest.Response resp = null;
			if (mo != null) {
				// �����o�^����������A�㏑��
				mos[i].id = mo.id;
				c8y.updateManagedObject(mos[i]); // mos[i] �� update �����
				// �㏑�����Ȃ��ꍇ
				// resp = r.get("/inventory/managedObjects/" + mo.id, "managedObject");
			} else {
				// �����o�^���Ȃ���΁A�V�K�o�^
				c8y.createManagedObject(mos[i]); // mos[i] �� update �����
			}
			// extId(nttcom_NameStr) �ɓo�^
			registerNameStr(mos[i].id, i);
		}
	}
	
	/**
	 * ��ʂ̃A���[������o�^���郁�\�b�h�ł��B
	 * �������Ԃ́A���ݎ��Ԃ���Ƃ��܂��B
	 *
	 * @param	index	managedObject �̃C���f�b�N�X(csv�ł̍s�ԍ�)
	 * @param	text	alarm �ɐݒ肷�� text
	 * @param	type	alarm �ɐݒ肷�� type
	 * @param	severity	alarm �ɐݒ肷�� severity
	 * @param	past	�������Ԃ̃I�t�Z�b�g(���ݎ�������Ƃ��Ăǂꂾ���ߋ���)
	 */
	private void makeAlarm(int index, String text, String type, String severity, long past)
						throws IOException {
		c8y.createAlarm(mos[index], text, type, severity, past);
	}
	
	/**
	 * ��ʂ̃A���[������o�^���郁�\�b�h�ł��B
	 */
	private void makeAlarm(int index, String text, String type, String severity, int dayoffst, int hour, int minute)
						throws IOException {
		c8y.createAlarm(mos[index], text, type, severity, dayoffst, hour, minute);
	}
	
/*---------------
 * class methods
 */
	/**
	 * ManagedObject �����쐬
	 *
	 * @param	name	���O(name �ɐݒ�)
	 */
	private static ManagedObject createMO(int index) {
		ManagedObject mo = new ManagedObject();
		
		// �f�t�H���g�l�̐ݒ�
		mo.type = "c8y_CarDemo";
		mo.c8y_IsDevice = new JsonObject();
		mo.c8y_Hardware = new C8y_Hardware();
		mo.c8y_Hardware.model = "Dummy Tracker";
		mo.c8y_Hardware.serialNumber = "Demo data";
		mo.c8y_Hardware.revision = "000";
		mo.putExtra("demodata", new JsonObject());
		mo.putExtra("carType", new JsonValue("�Ԏ�"));
		
		// �t�@�C���̒l�ŏ㏑��
		mo.fill(moData.get(index));
		return mo;
	}
	
	/**
	 * �w�肳�ꂽ nameStr ���� ManagedObject ���������A�ԋp���܂��B
	 *
	 * @param	name	enternalId (���O���)
	 * @return	ManagedObject�B���݂��Ȃ��ꍇ�A null
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
		
		// extId.managedObject �� id, self �����Ȃ����߁AManagedObject �{�̂�
		// �擾
		
		resp = r.get("/inventory/managedObjects/" + extId.managedObject.id);
		if (resp.code != 200) {
			System.out.println("getMOByIndex �� managementObject �擾���s");
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
	 * �w�肳�ꂽ managedObject.id �� externalId(nttcom_NameStr) �Ƃ���
	 * nameStr ��o�^���܂��B
	 *
	 * @param	moId	managedObject.id
	 * @param	nameStr	external id �Ƃ��ēo�^���閼��(ASCII��������)
	 */
	private static void registerNameStr(String moId, int index)
							throws IOException {
		String nameStr = moData.get(index).get("nttcom_NameStr").getValue();
		
		// ������ externalId �Ƃ��� nameStr ��o�^
		ExternalID extId = new ExternalID();
		extId.externalId	= nameStr;
		extId.type			= "nttcom_NameStr";
		Rest.Response r2 = c8y.getRest().post("/identity/globalIds/"+moId+"/externalIds", extId);
		if (r2.code != 201) {
			System.out.println("ExternalId �o�^���s");
			System.out.println(r2.code);
			System.out.println(r2.message);
		} else { // 405 collision �́A���� id/extId �ł͔������Ȃ�
			extId.fill(r2);
		}
	}
	
	
	/**
	 * �w�肳�ꂽ�ԍ���MO�Ɋւ��A���݈ʒu�A�O�Ղ𑗐M���܂��B
	 * date �� null �̏ꍇ�A���ݎ��������p����܂�
	 */
	private void sendLocation(int index, double lat, double lng, String date)
									throws IOException {
		Rest r = c8y.getRest();
		
		// Managed Object �̃A�b�v�f�[�g������
		ManagedObject mo = getMOByIndex(index);
		if (mo == null) 
			throw new RuntimeException(moData.get(index).get("nttcom_NameStr").toString() + "�̓o�^������܂���B�I�u�W�F�N�g��o�^���ĉ������B");
		ManagedObject updater = new ManagedObject();
		// �����o�^����������A�㏑��
		updater.c8y_Position = new C8y_Position();
		updater.c8y_Position.lat = lat;
		updater.c8y_Position.lng = lng;
		updater.c8y_Position.alt = 0d;
		//updater.c8y_Position.reportReason = "Time Event";
		//updater.c8y_Position.trackingProtocol = "TELIC";
//		Rest.Response resp = null;
		Rest.Response resp = r.put("/inventory/managedObjects/" + mo.id, "managedObject", updater);
		if (resp.code != 200 && resp.code != 201) {
			// update ���s
			System.out.println("setLocationEvent �� MO �ύX���s");
			System.out.println(resp.code);
			System.out.println(resp.message);
			System.out.println(resp);
			System.exit(-1);
		}
		
		// Event �Ƃ��Ă� POST ����
		// TRANSIENT ���[�h�𗘗p(Mongo�ɏ����Ȃ�)
		Rest tr = c8y.getRest();
		Event event = new Event(mo, "c8y_LocationUpdate","�ʒu���X�V");
		if (date == null || date.equals("")) event.time = new TC_Date();
		else event.time.set(date);
		event.putExtra("c8y_Position", updater.c8y_Position);
		resp = tr.post("/event/events", "event", event);
		if (resp.code != 201) { // not Created
			System.out.println("setLocationEvent �� event �o�^���s");
			System.out.println(resp.code);
			System.out.println(resp.message);
			System.exit(-1);
		}
	}
	
	/**
	 * �w�肳�ꂽMO�� Measurement �f�[�^��o�^���܂��B
	 * date �� null �̏ꍇ�A���ݎ��������p����܂�
	 */
	private void sendMeasurement(int index, double speed, String date)
						throws IOException {
		// identity �T�[�r�X�Ŏ擾����
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
		// identity �T�[�r�X�Ŏ擾����
		ManagedObject mo = getMOByIndex(index);
		
		Measurement m = new Measurement(mo, "demoData");
		if (date == null || date.equals("")) m.time = new TC_Date();
		else m.time.set(date);
		
		// 9���Z���T�����擾����
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
		
		// �m�C�Y�Y�t
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
 * �O�Ղ���������X���b�h(inner class)
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
		// �ړ��O�Ղ�\�����郋�[�v����
		//
		public void run() {
			int offset = (int)(Math.random() * orbit.size());
			boolean first = true;
			try {
				while (true) {
					// �s��
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
					// �A��
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
		// �o�H�����������M
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
		w.makeAlarm(0, "�J�n���܂���", "car_InfoLockOpen", "WARNING",0, 8, 50);
		w.makeAlarm(0, "�I�����܂���", "car_InfoLockClose", "WARNING", 0, 10, 12);
		w.makeAlarm(1, "�J�n���܂���", "car_InfoLockOpen", "WARNING",-1, 12, 50);
		w.makeAlarm(1, "�I�����܂���", "car_InfoLockClose", "WARNING", -1, 14, 12);
		w.makeAlarm(2, "�J�n���܂���", "car_InfoLockOpen", "WARNING",-1, 15, 50);
		w.makeAlarm(2, "�I�����܂���", "car_InfoLockClose", "WARNING", -1, 13, 12);
		w.makeAlarm(3, "�J�n���܂���", "car_InfoLockOpen", "WARNING",-1, 16, 50);
		w.makeAlarm(3, "�I�����܂���", "car_InfoLockClose", "WARNING", -1, 22, 12);
		w.makeAlarm(4, "�J�n���܂���", "car_InfoLockOpen", "WARNING",-1, 18, 50);
		w.makeAlarm(4, "�I�����܂���", "car_InfoLockClose", "WARNING", -1, 20, 12);
		
		System.out.println("sending location data...");
		// �o�H
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
			// �ܓx�� 1 �Ⴄ�ƁA��� 111.3km ����Ă���
			double lat = next.get("lat").doubleValue() - prev.get("lat").doubleValue();
			lat *= 111.3;
			// �o�x�� 1 �Ⴄ�ƁA���90.5km����Ă���
			double lng = next.get("lng").doubleValue() - prev.get("lng").doubleValue();
			lng *= 90.5;
			double distance = Math.sqrt(lat*lat + lng*lng);
			
			// �Ă��Ƃ��Ȑ��K���� km/h ���ۂ�����
			double speed = distance * 150;
			if (speed > 80) speed = 130 * speed / (speed + 50);
			
			// distance(km) �����̂܂܃X�s�[�h�ɂ��Ă���
			next.put("speed", new JsonValue((int)speed));
			// x, y, z ���������
			// �P�ʂ� m/s �ɕϊ�
			next.put("speedx", new JsonValue( (lng * 150 * 1000 / 3600) ));
			next.put("speedy", new JsonValue( (lat * 150 * 1000 / 3600) ));
			next.put("speedz", new JsonValue(0));
			
			// magnet �������
			// lat, lng �����̃x�N�g���𐳋K�����邾��
			// �����t�߂ł� 45uT(�}�C�N���e�X��)���炢�炵���̂ł��̒l�ɐ��K��
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
			
			// ��_�Ԃ̎��Ԃ� 1 �b�Ƃ��Ă���B
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
			
			// �p���x���v�Z
			// �x�N�g���̓��ς𗘗p���Acos�Ƃ����߂�
			double l = Math.sqrt(nx*nx+ny*ny) * Math.sqrt(px*px+py*py);
			double cos = (nx * px + ny * py);
			if (l < 0.0001d) cos = 0d;
			else cos = cos / l;
			if (cos > 1d) cos = 1d;
			if (cos < -1d) cos = -1d;
			//
			double r = Math.acos(cos); // 0 �` ��
			r = r / Math.PI * 180; // 0 �` 180
			if (nx * py - ny * px < 0) r = -r; // �O�ς̌������
			r = r / 1.0d; // 1�b���ƂƂ��Ă���
			
			next.put("rx", new JsonValue(r));
			next.put("ry", new JsonValue(0));
			next.put("rz", new JsonValue(0));
			
//System.out.println("acc = " + acc + " / accx = " + x + " / accy = " + y + " / r = " + r);
			prev = next;
		}
	}
	
}

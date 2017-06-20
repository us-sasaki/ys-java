import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;

import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;

import com.ntt.tc.net.Rest;
import com.ntt.tc.data.*;
import com.ntt.tc.data.rest.*;
import com.ntt.tc.data.sensor.*;

/**
 * Cumulocity Device サンプル実装
 * JData 版
 *
 * @author	Yusuke Sasaki
 */
public class Device2 {
	public static final String CONFIG_FILE = "Device.conf";
	
	protected ExternalIds	hard;
	/** デバイスクレデンシャル要求用のデバイスID(正式名称？) */
	protected DeviceCredentialsResp credential;
	protected ManagedObject managedObject;
	
	/** このオブジェクトの ManagedObject */
	
	protected Rest rest;
	
/*-------------
 * Constructor
 */
	public Device2() {
		try {
			Reader r = new FileReader(CONFIG_FILE);
			JsonType conf		= JsonType.parse(r);
			r.close();
			
			credential = new DeviceCredentialsResp();
			if (conf.get("credential") != null) {
				credential.fill(conf.get("credential"));
			}
			hard = new ExternalIds();
			if (conf.get("hard") != null) {
				hard.fill(conf.get("hard"));
			} else {
				hard.externalId = "ext-"+credential.id;
				hard.type = "c8y_Serial";
			}
			managedObject = new ManagedObject();
			if (conf.get("managedObject") != null) {
				managedObject.fill(conf.get("managedObject"));
			} else {
				// デフォルトの ManagedObject を生成します。
				managedObject.name = "冷蔵庫管理" + credential.id;
				managedObject.type = "Raspberry Pi"; // c8y_Linux in doc's sample.
				managedObject.c8y_IsDevice = new JsonObject();
				managedObject.com_cumulocity_model_Agent = new JsonObject();
				managedObject.c8y_SupportedOperations = new String[] {"c8y_Restart","c8y_Configuration","c8y_SendConfiguration","testOperation"};
				managedObject.c8y_Hardware = new C8y_Hardware();
				managedObject.c8y_Hardware.revision	= "000";
				managedObject.c8y_Hardware.model		= "Refrigerator";
				managedObject.c8y_Hardware.serialNumber = credential.id;
				managedObject.c8y_Configuration = new C8y_Configuration();
				managedObject.c8y_Configuration.config	= "not defined :)";
			}
				
		} catch (IOException ioe) {
			System.err.println("An error occurred while config file reading. : " +ioe);
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * conf を CONFIG_FILE で示されるファイルとして出力
	 */
	private void writeConf() throws IOException {
		JsonObject conf = new JsonObject();
		conf.add("credential", credential.toJson());
		conf.add("hard", hard.toJson());
		conf.add("managedObject", managedObject.toJson());
	
		PrintWriter p = new PrintWriter(new FileWriter(CONFIG_FILE));
		
		p.println(conf.toString("    "));
		p.close();
	}
	
	/**
	 * Rest.Response を表示
	 */
	private void printResp(Rest.Response resp) {
		if (resp.toByteArray() == null)
			System.out.println(resp.code + ":" + resp.message);
		else System.out.println(resp.toJson());
	}
	
	/**
	 * このデバイスのデバイスクレデンシャルによる Rest クライアント
	 * オブジェクトを取得します。
	 */
	private Rest getRest() {
		if (rest == null) {
			rest = new Rest("https://management.iot-trialpack.com", credential.username, credential.password);
		}
		return rest;
	}
	
	/**
	 * Step 0.
	 * この Device がデバイスクレデンシャルを持っていない場合、サーバに要求します。
	 * 要求は５秒ごとに行い、正常返却があるまで続けます。
	 */
	private void getDeviceCredential() throws IOException {
		if (credential.username != null &&
			credential.password != null) return;
		if (credential.id == null || credential.id.equals(""))
			throw new IllegalStateException("id is null.");
		
		Rest r = new Rest("https://management.iot-trialpack.com", "management", "devicebootstrap", "Fhdt1bb1f");
		
		while (true) {
			System.out.println("Requesting credential of id " + credential.id);
			Rest.Response resp = r.post("/devicecontrol/deviceCredentials", "deviceCredentials", credential);
			if (resp.code == 404) {
				try {
					printResp(resp);
					Thread.sleep(5000L);
				} catch (InterruptedException ignored) {
				}
				continue;
			} else {
				credential.fill(resp.toJson());
				writeConf();
				break;
			}
		}
	}
	
	/**
	 * Step 1.
	 * externalId が登録されているか確認し、なければ登録します。
	 * ManagedObject 登録 → externalId 付与の順のため、externalId がない
	 * ことは ManagedObject もないことを示します。
	 * externalId は、config ファイルで指定がなかった場合、
	 * デフォルト値 "ext-"+id を c8y_Serial として確認します。
	 *
	 */
	private boolean existsExternalId() throws IOException {
		Rest r = getRest();
		Rest.Response resp = r.get("/identity/externalIds/c8y_Serial/" + hard.externalId, "externalId");
		if (resp.code == 200) return true;
		if (resp.code == 404) return false;
		
		printResp(resp);
		throw new IOException("An error occurred while externalId checking: "+resp.message);
	}
	
	/**
	 * Step 2.
	 * 管理オブジェクトを新規登録します。
	 */
	private void registerManagedObject() throws IOException {
		Rest r = getRest();
		Rest.Response resp = r.post("/inventory/managedObjects", "managedObject", managedObject.toJson() );
		
		if (resp.code == 201) {	// Created
		
			managedObject.fill(resp.toJson());
			writeConf();
		}
	}
	
	/**
	 * Step 3.
	 * ManagerObject に externalId を紐づけます
	 */
	private void registerExternalId() throws IOException {
		Rest r = getRest();
		Rest.Response resp = r.post("/identity/globalIds/"+managedObject.id+"/externalIds", "externalId", hard.toJson());
		
		if (resp.code == 201) { // Created
			hard.fill(resp.toJson());
			writeConf();
			return;
		}
		throw new IOException("An error occurred while registering externalId.");
	}
	
	/**
	 * Step 4.
	 * ManagedObject を最新化します
	 * 更新したい managedObject id に、更新部分のみを送信すればOK
	 */
	private void updateManagedObject() throws IOException {
		
		ManagedObject mo = new ManagedObject();
		mo.c8y_Position = new C8y_Position();
		mo.c8y_Position.alt = 0d;
		mo.c8y_Position.lat = 38d;
		mo.c8y_Position.lng = 136d;
		
		Rest r = getRest();
		Rest.Response resp = r.put("/inventory/managedObjects/"+managedObject.id, "managedObject", mo.toJson());
		if (resp.code == 200) { // OK
			return;
		}
		throw new IOException("An error occurred while updating mo."+resp.message);
	}
	
	
	/**
	 * Step 9 Send Measurements
	 */
	private void sendMeasurements() throws IOException {
		
		float temp = ((float)(int)((Math.random() * 20.0 + 15.0)*100))/100;
		System.out.println("sending Temperature meas." + temp);
		
		// c8y_TemperatureMeasurement
		Measurement m = new Measurement(managedObject, "c8y_PTCMeasurement");
		
		m.c8y_TemperatureMeasurement = new C8y_TemperatureMeasurement(temp);
		m.c8y_Battery = new C8y_Battery(23);
		
		Rest r = getRest();
		Rest.Response resp = r.post("/measurement/measurements", "measurement", m.toJson());
		if (resp.code == 201) { // Created
			return;
		}
		throw new IOException("An error occurred while sending measurement."+resp.message);
	}
	// 複数のメジャーメントを送信する場合、以下のようなものを POST
	/* POSTman サンプルより
{
    "measurements": [
        {
        	"c8y_SpeedMeasurement": {
            	"speed": { 
                	"value": 25,
                    "unit": "km/h" }
                },
            "time":"2017-05-22T17:03:14.000+02:00", 
            "source": {
            	"id":"10222" }, 
            "type": "c8y_SpeedMeasurement"
        },
        {
        	"c8y_SpeedMeasurement": {
            	"speed": { 
                	"value": 22,
                    "unit": "km/h" }
                },
            "time":"2017-05-22T17:05:14.000+02:00", 
            "source": {
            	"id":"10222" }, 
            "type": "c8y_SpeedMeasurement"
        }
    ]
}	
	*/
	
	
	/**
	 * Step 10 Send Events
	 */
	// 次は、event で Location update をしたい
	// あと、バイナリファイルを送受信したい
	private void sendEvents() throws IOException {
		Event e = new Event(managedObject, "c8y_LocationUpdate", "location Changed event.");
		e.c8y_Position = new C8y_Position();
		e.c8y_Position.alt = 0d;
		e.c8y_Position.lat = 38d;
		e.c8y_Position.lng = 136d;
		e.c8y_Position.trackingProtocol = "TELIC";
		e.c8y_Position.reportReason = "Time Event";
		
		Rest r = getRest();
		Rest.Response resp = r.post("/event/events", "event", e.toJson());
		if (resp.code == 201) { // Created
			e.fill(resp.toJson());
			System.out.println(resp.toJson().toString("  "));
			return;
		}
		throw new IOException("An error occurred while sending event."+resp.message);
	}
	
	/**
	 * Step 11 Send Alarms
	 */
	private void sendAlarms() throws IOException {
		Alarm a = new Alarm(managedObject.id, "Test alarm");
		
		System.out.println(a.toString("  "));
		Rest r = getRest();
		Rest.Response resp = r.post("/alarm/alarms", "alarm", a.toJson());
		if (resp.code == 201) { // Created
			return;
		}
		System.out.println("Response : " + resp.code);
		System.out.println("Message  : " + resp.message);
		
		throw new IOException("An error occurred while sending alarm."+resp.message);
	}
	
	/**
	 * measurement 送信
	 */
	private void cycle() throws IOException {
		while (true) {
			sendMeasurements();
			//sendEvents();
			//sendAlarms();
			try {
				Thread.sleep(2000L);
			} catch (InterruptedException ignored) {
			}
		}
	}
	
	/**
	 * Binary アップロード
	 */
	private void uploadBinary(String filename,
						String mimetype,
						byte[] binary) throws IOException {
		ManagedObject mo = new ManagedObject();
		mo.name = filename;
		mo.type = mimetype;
		
		Rest r = getRest();
		Rest.Response resp = r.postBinary(filename, mimetype, binary);
		System.out.println(resp.code);
		System.out.println(resp.message);
		System.out.println(resp.toJson());
	}
	
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		Device2 a = new Device2();
		a.getDeviceCredential();
		if (!a.existsExternalId()) {
			// 初回登録
			a.registerManagedObject();
			a.registerExternalId();
		}
		// アップデートがあれば、managedObject を更新
		// アップデートがあるかどうかの判定は行っておらず、必ず更新
		
		//a.updateManagedObject();
		// An error occurred while updating mo.Unprocessable Entity
		// が出る。id のような更新不能のものもまとめて送っているからと
		// 思われる。
		
		// location update
		a.updateManagedObject();
		
		a.cycle();
//		a.uploadBinary("binarySample.txt", "text/plain", "This is a sample binary.".getBytes());
		
	}
}
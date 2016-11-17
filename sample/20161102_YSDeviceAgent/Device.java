import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;

import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;

/**
 * Cumulocity Device サンプル実装
 *
 * @author	Yusuke Sasaki
 */
public class Device {
	public static final String CONFIG_FILE = "Device.conf";
	
	protected JsonType conf;
	
	/** デバイスクレデンシャル要求用のデバイスID(正式名称？) */
	protected String id;
	
	/** デバイスクレデンシャル用のユーザ名 */
	protected String username;
	/** デバイスクレデンシャル用のパスワード */
	protected String password;
	
	/** シリアル番号 */
	protected String externalId;
	
	protected Rest rest;
	
/*-------------
 * Constructor
 */
	public Device() {
		try {
			Reader r = new FileReader(CONFIG_FILE);
			this.conf		= JsonType.parse(r);
			JsonType cred	= conf.get("credential");
			this.id			= getStr(cred, "id");
			this.username	= getStr(cred, "username");
			this.password	= getStr(cred, "password");
			this.externalId	= getStr(cred, "externalId");
		} catch (IOException ioe) {
			System.err.println("An error occurred while config file reading. : " +ioe);
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * Jsonの値を取得します。キーがない場合、null が返ります。
	 */
	private String getStr(JsonType jt, String field) {
		JsonType j = jt.get(field);
		if (j == null) return null;
		String s = j.toString();
		if (s.startsWith("")) {
			s = s.substring(1, s.length()-1);
		}
		return s;
	}
	
	/**
	 * conf を CONFIG_FILE で示されるファイルとして出力
	 */
	private void writeConf() throws IOException {
		PrintWriter p = new PrintWriter(new FileWriter(CONFIG_FILE));
		p.println(conf.toString());
		p.close();
	}
	
	/**
	 * Rest.Response を表示
	 */
	private void printResp(Rest.Response resp) {
		if (resp.body == null) System.out.println(resp.code + ":" + resp.message);
		else System.out.println(resp.toJson());
	}
	
	private Rest getRest() {
		if (rest == null) {
			JsonType cred = conf.get("credential");
			rest = new Rest("https://nttcom.cumulocity.com", getStr(cred, "username"), getStr(cred, "password"));
		}
		return rest;
	}
	
	/**
	 * Step 0.
	 * この Device がデバイスクレデンシャルを持っていない場合、サーバに要求します。
	 * 要求は５秒ごとに行い、正常返却があるまで続けます。
	 */
	private void getDeviceCredential() throws IOException {
		if (username != null && password != null) return;
		if (id == null || id.equals(""))
			throw new IllegalStateException("id is null.");
		
		Rest r = new Rest("https://nttcom.cumulocity.com", "management", "devicebootstrap", "Fhdt1bb1f");
		JsonObject jo = new JsonObject().add("id", id);
//		jo.add("password", (String)null);
//		jo.add("tenantId", (String)null);
//		jo.add("username", (String)null);
		while (true) {
			System.out.println("Requesting credential of id " + id);
			Rest.Response resp = r.post("/devicecontrol/deviceCredentials", "deviceCredentials", jo);
			if (resp.code == 404) {
				try {
					printResp(resp);
					Thread.sleep(5000L);
				} catch (InterruptedException ignored) {
				}
				continue;
			} else {
				// JsonObject に置き換えのメソッドがない。。ので苦肉の策
				((JsonObject)conf).map.put("credential", resp.toJson());
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
		JsonType cred = conf.get("credential");
		JsonType hard = conf.get("hard");
		if (hard == null) hard = new JsonObject();
		if (hard.get("externalId") == null) {
			hard.add("externalId", "ext-"+cred.get("id"));
			hard.add("type", "c8y_Serial");
		}
		Rest r = getRest();
		Rest.Response resp = r.get("/identity/externalIds/c8y_Serial/" + hard.get("enternalId"), "externalId");
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
		JsonType cred = conf.get("credential");
		JsonType mo = conf.get("managedObject");
		if (mo == null) {
			// デフォルトの ManagedObject を生成します。
			mo = new JsonObject();
			
			mo.add("name", "YS Java device " + cred.get("id"));
			mo.add("type", "windows"); // c8y_Linux in doc's sample.
			mo.add("c8y_IsDevice", new JsonObject());
			mo.add("com_cumulocity_model_Agent", new JsonObject());
			mo.add("c8y_SupportedOperations", JsonType.parse("[\"c8y_Restart\",\"testOperation\"]"));
			mo.add("c8y_Hardware", new JsonObject().
					add("revision","000").
					add("model", "Java agent").
					add("serialNumber", cred.get("id")) );
			mo.add("c8y_Configuration", new JsonObject().
					add("config", "not defined :)"));
			// 以下略。。
		}
		Rest r = getRest();
		Rest.Response resp = r.post("/inventory/managedObjects", "managedObject", mo);
		if (resp.code == 201) {	// Created
			mo = resp.toJson();
			((JsonObject)conf).map.put("managedObject", mo);
			writeConf();
		}
	}
	
	/**
	 * Step 3.
	 * ManagerObject に externalId を紐づけます
	 */
	private void registerExternalId() throws IOException {
		JsonType cred = conf.get("credential");
		JsonType hard = conf.get("hard");
		if (hard == null) hard = new JsonObject();
		if (hard.get("externalId") == null) {
			hard.add("externalId", "ext-"+cred.get("id"));
			hard.add("type", "c8y_Serial");
		}
		JsonType mo = conf.get("managedObject");
		
		Rest r = getRest();
		Rest.Response resp = r.post("/identity/globalIds/"+getStr(mo, "id")+"/externalIds", "externalId", hard);
		
		if (resp.code == 201) { // Created
			((JsonObject)conf).map.put("hard", resp.toJson());
			writeConf();
			return;
		}
		throw new IOException("An error occurred while registering externalId.");
	}
	
	/**
	 * Step 4.
	 * ManagedObject を最新化します
	 */
	private void updateManagedObject() throws IOException {
		JsonType mo = conf.get("managedObject");
		
		Rest r = getRest();
		Rest.Response resp = r.put("/inventory/managedObjects/"+getStr(mo, "id"), "managedObject", mo.toString());
		if (resp.code == 200) { // OK
			return;
		}
		throw new IOException("An error occurred while updating mo."+resp.message);
	}
	
	
	/**
	 * Step 9 Send Measurements
	 */
	private void sendMeasurements() throws IOException {
		JsonType mo = conf.get("managedObject");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		float temp = ((float)(int)((Math.random() * 20.0 + 15.0)*100))/100;
		System.out.println("sending Temperature meas." + temp);
		
		// c8y_TemperatureMeasurement
		JsonType jt = JsonType.parse("{\"c8y_TemperatureMeasurement\":{\"T\":{\"value\":"+temp+",\"unit\":\"C\"}},\"time\":\""+sdf.format(new Date())+"\",\"source\":{\"id\":\""+getStr(mo, "id")+"\"},\"type\":\"c8y_PTCMeasurement\"}");
		// c8y_Battery
		jt.add("c8y_Battery", JsonType.parse("{\"level\":{\"value\":23,\"unit\":\"%\"}}"));
		
		Rest r = getRest();
		Rest.Response resp = r.post("/measurement/measurements", "measurement", jt);
		if (resp.code == 201) { // Created
			return;
		}
		throw new IOException("An error occurred while sending measurement."+resp.message);
	}
	
	// 次は、event で Location update をしたい
	// あと、バイナリファイルを送受信したい
	private void sendEvents() throws IOException {
		JsonType mo = conf.get("managedObject");
		
		//
		//
		Rest r = getRest();
		Rest.Response resp = r.post("/event/events", "event", jt);
		if (resp.code == 201) { // Created
			return;
		}
		throw new IOException("An error occurred while sending measurement."+resp.message);
	}
		
	}
	
	private void cycle() throws IOException {
		while (true) {
			sendMeasurements();
			try {
				Thread.sleep(100L);
			} catch (InterruptedException ignored) {
			}
		}
	}
	
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		Device a = new Device();
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
		
		a.cycle();
	}
	
}
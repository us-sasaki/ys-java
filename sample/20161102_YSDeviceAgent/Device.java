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
 * Cumulocity Device �T���v������
 *
 * @author	Yusuke Sasaki
 */
public class Device {
	public static final String CONFIG_FILE = "Device.conf";
	
	protected JsonType conf;
	
	/** �f�o�C�X�N���f���V�����v���p�̃f�o�C�XID(�������́H) */
	protected String id;
	
	/** �f�o�C�X�N���f���V�����p�̃��[�U�� */
	protected String username;
	/** �f�o�C�X�N���f���V�����p�̃p�X���[�h */
	protected String password;
	
	/** �V���A���ԍ� */
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
	 * Json�̒l���擾���܂��B�L�[���Ȃ��ꍇ�Anull ���Ԃ�܂��B
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
	 * conf �� CONFIG_FILE �Ŏ������t�@�C���Ƃ��ďo��
	 */
	private void writeConf() throws IOException {
		PrintWriter p = new PrintWriter(new FileWriter(CONFIG_FILE));
		p.println(conf.toString());
		p.close();
	}
	
	/**
	 * Rest.Response ��\��
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
	 * ���� Device ���f�o�C�X�N���f���V�����������Ă��Ȃ��ꍇ�A�T�[�o�ɗv�����܂��B
	 * �v���͂T�b���Ƃɍs���A����ԋp������܂ő����܂��B
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
				// JsonObject �ɒu�������̃��\�b�h���Ȃ��B�B�̂ŋ���̍�
				((JsonObject)conf).map.put("credential", resp.toJson());
				writeConf();
				break;
			}
		}
	}
	
	/**
	 * Step 1.
	 * externalId ���o�^����Ă��邩�m�F���A�Ȃ���Γo�^���܂��B
	 * ManagedObject �o�^ �� externalId �t�^�̏��̂��߁AexternalId ���Ȃ�
	 * ���Ƃ� ManagedObject ���Ȃ����Ƃ������܂��B
	 * externalId �́Aconfig �t�@�C���Ŏw�肪�Ȃ������ꍇ�A
	 * �f�t�H���g�l "ext-"+id �� c8y_Serial �Ƃ��Ċm�F���܂��B
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
	 * �Ǘ��I�u�W�F�N�g��V�K�o�^���܂��B
	 */
	private void registerManagedObject() throws IOException {
		JsonType cred = conf.get("credential");
		JsonType mo = conf.get("managedObject");
		if (mo == null) {
			// �f�t�H���g�� ManagedObject �𐶐����܂��B
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
			// �ȉ����B�B
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
	 * ManagerObject �� externalId ��R�Â��܂�
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
	 * ManagedObject ���ŐV�����܂�
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
	
	// ���́Aevent �� Location update ��������
	// ���ƁA�o�C�i���t�@�C���𑗎�M������
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
			// ����o�^
			a.registerManagedObject();
			a.registerExternalId();
		}
		// �A�b�v�f�[�g������΁AmanagedObject ���X�V
		// �A�b�v�f�[�g�����邩�ǂ����̔���͍s���Ă��炸�A�K���X�V
		
		//a.updateManagedObject();
		// An error occurred while updating mo.Unprocessable Entity
		// ���o��Bid �̂悤�ȍX�V�s�\�̂��̂��܂Ƃ߂đ����Ă��邩���
		// �v����B
		
		a.cycle();
	}
	
}
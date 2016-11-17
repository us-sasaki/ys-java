import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;

import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;

import com.ntt.tc.data.*;
import com.ntt.tc.data.rest.*;
import com.ntt.tc.data.sensor.*;

/**
 * Cumulocity Device �T���v������
 *
 * @author	Yusuke Sasaki
 */
public class Device2 {
	public static final String CONFIG_FILE = "Device.conf";
	
	protected ExternalIds	hard;
	/** �f�o�C�X�N���f���V�����v���p�̃f�o�C�XID(�������́H) */
	protected DeviceCredentialsResp credential;
	protected ManagedObject managedObject;
	
	/** ���̃I�u�W�F�N�g�� ManagedObject */
	
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
				// �f�t�H���g�� ManagedObject �𐶐����܂��B
				managedObject.name = "YS Java device " + credential.id;
				managedObject.type = "windows"; // c8y_Linux in doc's sample.
				managedObject.c8y_IsDevice = new JsonObject();
				managedObject.com_cumulocity_model_Agent = new JsonObject();
				managedObject.c8y_SupportedOperations = new String[] {"c8y_Restart","testOperation"};
				managedObject.c8y_Hardware = new C8y_Hardware();
				managedObject.c8y_Hardware.revision	= "000";
				managedObject.c8y_Hardware.model		= "Java agent";
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
	 * Json�̒l���擾���܂��B�L�[���Ȃ��ꍇ�Anull ���Ԃ�܂��B
	 * ���Ԃ�s�v
	 */
//	private String getStr(JsonType jt, String field) {
//		JsonType j = jt.get(field);
//		if (j == null) return null;
//		String s = j.toString();
//		if (s.startsWith("")) {
//			s = s.substring(1, s.length()-1);
//		}
//		return s;
//	}
	
	/**
	 * conf �� CONFIG_FILE �Ŏ������t�@�C���Ƃ��ďo��
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
	 * Rest.Response ��\��
	 */
	private void printResp(Rest.Response resp) {
		if (resp.body == null) System.out.println(resp.code + ":" + resp.message);
		else System.out.println(resp.toJson());
	}
	
	private Rest getRest() {
		if (rest == null) {
			rest = new Rest("https://nttcom.cumulocity.com", credential.username, credential.password);
		}
		return rest;
	}
	
	/**
	 * Step 0.
	 * ���� Device ���f�o�C�X�N���f���V�����������Ă��Ȃ��ꍇ�A�T�[�o�ɗv�����܂��B
	 * �v���͂T�b���Ƃɍs���A����ԋp������܂ő����܂��B
	 */
	private void getDeviceCredential() throws IOException {
		if (credential.username != null &&
			credential.password != null) return;
		if (credential.id == null || credential.id.equals(""))
			throw new IllegalStateException("id is null.");
		
		Rest r = new Rest("https://nttcom.cumulocity.com", "management", "devicebootstrap", "Fhdt1bb1f");
		
		while (true) {
			System.out.println("Requesting credential of id " + credential.id);
			Rest.Response resp = r.post("/devicecontrol/deviceCredentials", "deviceCredentials", credential.toJson());
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
	 * externalId ���o�^����Ă��邩�m�F���A�Ȃ���Γo�^���܂��B
	 * ManagedObject �o�^ �� externalId �t�^�̏��̂��߁AexternalId ���Ȃ�
	 * ���Ƃ� ManagedObject ���Ȃ����Ƃ������܂��B
	 * externalId �́Aconfig �t�@�C���Ŏw�肪�Ȃ������ꍇ�A
	 * �f�t�H���g�l "ext-"+id �� c8y_Serial �Ƃ��Ċm�F���܂��B
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
	 * �Ǘ��I�u�W�F�N�g��V�K�o�^���܂��B
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
	 * ManagerObject �� externalId ��R�Â��܂�
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
	 * ManagedObject ���ŐV�����܂�
	 */
	private void updateManagedObject() throws IOException {
		Rest r = getRest();
		Rest.Response resp = r.put("/inventory/managedObjects/"+managedObject.id, "managedObject", managedObject.toJson());
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
	
	// ���́Aevent �� Location update ��������
	// ���ƁA�o�C�i���t�@�C���𑗎�M������
	private void sendEvents() throws IOException {
	
		Rest r = getRest();
		Rest.Response resp = r.post("/event/events", "event", jt);
		if (resp.code == 201) { // Created
			return;
		}
		throw new IOException("An error occurred while sending measurement."+resp.message);
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
		Device2 a = new Device2();
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
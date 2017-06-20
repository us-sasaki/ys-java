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
 * Cumulocity Device �T���v������
 * JData ��
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
				managedObject.name = "�①�ɊǗ�" + credential.id;
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
		if (resp.toByteArray() == null)
			System.out.println(resp.code + ":" + resp.message);
		else System.out.println(resp.toJson());
	}
	
	/**
	 * ���̃f�o�C�X�̃f�o�C�X�N���f���V�����ɂ�� Rest �N���C�A���g
	 * �I�u�W�F�N�g���擾���܂��B
	 */
	private Rest getRest() {
		if (rest == null) {
			rest = new Rest("https://management.iot-trialpack.com", credential.username, credential.password);
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
	 * �X�V������ managedObject id �ɁA�X�V�����݂̂𑗐M�����OK
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
	// �����̃��W���[�����g�𑗐M����ꍇ�A�ȉ��̂悤�Ȃ��̂� POST
	/* POSTman �T���v�����
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
	// ���́Aevent �� Location update ��������
	// ���ƁA�o�C�i���t�@�C���𑗎�M������
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
	 * measurement ���M
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
	 * Binary �A�b�v���[�h
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
		
		// location update
		a.updateManagedObject();
		
		a.cycle();
//		a.uploadBinary("binarySample.txt", "text/plain", "This is a sample binary.".getBytes());
		
	}
}
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

/**
 * REST�ڑ��A�J�E���g����ێ����AREST-API �����̊e�탁�\�b�h��񋟂��܂��B
 *
 * @author	Yusuke Sasaki
 * @version	September 5, 2017
 */
public class C8yConnector {
	protected String url;
	protected String tenant;
	protected String user;
	protected String pass;
	
	protected Rest rest;
	protected Rest transientRest;
	protected Rest credentialRest; // unused
	
/*-------------
 * Constructor
 */
	/**
	 * �w�肳�ꂽ URL, TENANT, USER, PASSWORD �Őڑ�����C���X�^���X��
	 * �������܂��B
	 */
	public C8yConnector(String url, String tenant, String user, String pass) {
		this.url = url;
		this.tenant = tenant;
		this.user = user;
		this.pass = pass;
		
		rest = new Rest(url, tenant, user, pass);
		transientRest = new Rest(url, tenant, user, pass);
		transientRest.setProcessingMode(true);
		// credentialRest
	}
	
/*------------------
 * instance methods
 */
	/**
	 * REST �A�N�Z�X�I�u�W�F�N�g���擾���܂��B
	 */
	public Rest getRest() {
		return rest;
	}
	
	/**
	 * TRANSIENT ���[�h���ݒ肳�ꂽ REST �A�N�Z�X�I�u�W�F�N�g���擾���܂��B
	 */
	public Rest getTransientRest() {
		return transientRest;
	}
	
/*------------------------------------------
 * C8y �I�u�W�F�N�g�Ɋ֘A����֗����\�b�h�Q
 * http ���X�|���X�̃n���h�����s���܂�
 */
	/**
	 * ��ʂ̃A���[������o�^���郁�\�b�h�ł��B
	 * �������Ԃ́A���ݎ��ԂƂ��܂��B
	 *
	 * @param	s		alarm �ɐݒ肷�� managedObject
	 * @param	text	alarm �ɐݒ肷�� text
	 * @param	type	alarm �ɐݒ肷�� type
	 * @param	severity	alarm �ɐݒ肷�� severity
	 */
	public void createAlarm(ManagedObject s, String text, String type,
							String severity) throws IOException {
		createAlarm(s, text, type, severity, 0L);
	}
	
	/**
	 * ��ʂ̃A���[������o�^���郁�\�b�h�ł��B
	 * �������Ԃ́A���ݎ��Ԃ���Ƃ��A�ߋ����Ԃ�o�^�ł��܂��B
	 * ����́A
	 *
	 * @param	s		alarm �ɐݒ肷�� managedObject
	 * @param	text	alarm �ɐݒ肷�� text
	 * @param	type	alarm �ɐݒ肷�� type
	 * @param	severity	alarm �ɐݒ肷�� severity
	 * @param	past	�������Ԃ̃I�t�Z�b�g(���ݎ�������Ƃ��Ăǂꂾ���ߋ���)
	 */
	public void createAlarm(ManagedObject s, String text, String type,
							String severity, long past)
									throws IOException {
		long	time	= System.currentTimeMillis() - past;
		TC_Date	d		= new TC_Date(time);
		
		Alarm	alarm	= new Alarm();
		
		alarm.severity = severity;
		alarm.source = s;
		alarm.text = text;
		alarm.time = d;
		alarm.type = type;
		
		Rest.Response r = rest.post("/alarm/alarms", "alarm", alarm);
		if (r.code != 201) {
			System.out.println("makeAlarm failed");
			System.out.println(r.code);
			System.out.println(r.message);
			System.out.println(s.name);
		}
	}
	
	/**
	 * ��ʂ̃A���[������o�^���郁�\�b�h�ł��B
	 */
	public void createAlarm(ManagedObject s, String text, String type, String severity, int dayoffst, int hour, int minute)
						throws IOException {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + dayoffst);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		
		TC_Date	d		= new TC_Date(c.getTime());
		
		Alarm	alarm	= new Alarm();
		
		alarm.severity = severity;
		alarm.source = s;
		alarm.text = text;
		alarm.time = d;
		alarm.type = type;
		
		Rest.Response r = rest.post("/alarm/alarms", "alarm", alarm);
		if (r.code != 201) {
			System.out.println("makeAlarm failed");
			System.out.println(r.code);
			System.out.println(r.message);
			System.out.println(s.name);
		}
	}
	
	public void createManagedObject(ManagedObject mo) throws IOException {
		Rest.Response resp = rest.post("/inventory/managedObjects", "managedObject", mo);
		if (resp.code != 200 && resp.code != 201) { // Created �łȂ�
			throw new IOException("Managed Object �o�^���s�F" + resp.code + "/ " + resp.message + "/ " + resp);
		}
		mo.fill(resp);
	}
	
	public void updateManagedObject(ManagedObject mo) throws IOException {
		Rest.Response resp = rest.put("/inventory/managedObjects/" + mo.id, "managedObject", mo);
		if (resp.code != 200 && resp.code != 201) { // Created �łȂ�
			throw new IOException("Managed Object �X�V���s�F" + resp.code + "/ " + resp.message + "/ " + resp);
		}
		mo.fill(resp);
	}
	
	public ManagedObject readManagedObject(String id) throws IOException {
		Rest.Response resp = rest.get("/inventory/managedObjects/" + id);
		if (resp.code != 200) {
			throw new IOException("Managed Object �擾���s�F" + resp.code + "/ " + resp.message + "/ " + resp);
		}
		return Jsonizer.fromJson(resp, ManagedObject.class);
	}


}

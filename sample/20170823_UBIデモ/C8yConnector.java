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
 * REST接続アカウント情報を保持し、REST-API 相当の各種メソッドを提供します。
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
	 * 指定された URL, TENANT, USER, PASSWORD で接続するインスタンスを
	 * 生成します。
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
	 * REST アクセスオブジェクトを取得します。
	 */
	public Rest getRest() {
		return rest;
	}
	
	/**
	 * TRANSIENT モードが設定された REST アクセスオブジェクトを取得します。
	 */
	public Rest getTransientRest() {
		return transientRest;
	}
	
/*------------------------------------------
 * C8y オブジェクトに関連する便利メソッド群
 * http レスポンスのハンドルを行います
 */
	/**
	 * 一般のアラーム情報を登録するメソッドです。
	 * 発生時間は、現在時間とします。
	 *
	 * @param	s		alarm に設定する managedObject
	 * @param	text	alarm に設定する text
	 * @param	type	alarm に設定する type
	 * @param	severity	alarm に設定する severity
	 */
	public void createAlarm(ManagedObject s, String text, String type,
							String severity) throws IOException {
		createAlarm(s, text, type, severity, 0L);
	}
	
	/**
	 * 一般のアラーム情報を登録するメソッドです。
	 * 発生時間は、現在時間を基準とし、過去時間を登録できます。
	 * これは、
	 *
	 * @param	s		alarm に設定する managedObject
	 * @param	text	alarm に設定する text
	 * @param	type	alarm に設定する type
	 * @param	severity	alarm に設定する severity
	 * @param	past	発生時間のオフセット(現在時刻を基準としてどれだけ過去か)
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
	 * 一般のアラーム情報を登録するメソッドです。
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
		if (resp.code != 200 && resp.code != 201) { // Created でない
			throw new IOException("Managed Object 登録失敗：" + resp.code + "/ " + resp.message + "/ " + resp);
		}
		mo.fill(resp);
	}
	
	public void updateManagedObject(ManagedObject mo) throws IOException {
		Rest.Response resp = rest.put("/inventory/managedObjects/" + mo.id, "managedObject", mo);
		if (resp.code != 200 && resp.code != 201) { // Created でない
			throw new IOException("Managed Object 更新失敗：" + resp.code + "/ " + resp.message + "/ " + resp);
		}
		mo.fill(resp);
	}
	
	public ManagedObject readManagedObject(String id) throws IOException {
		Rest.Response resp = rest.get("/inventory/managedObjects/" + id);
		if (resp.code != 200) {
			throw new IOException("Managed Object 取得失敗：" + resp.code + "/ " + resp.message + "/ " + resp);
		}
		return Jsonizer.fromJson(resp, ManagedObject.class);
	}


}

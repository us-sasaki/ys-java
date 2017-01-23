package com.ntt.tc.data.alarms;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.inventory.ManagedObject;
import com.ntt.tc.data.auditing.AuditRecordCollection;
import abdom.data.json.JsonObject;

/**
 * Alarm class, 
 * post の時に使用できるコンストラクタを追加。
 */
public class Alarm extends C8yData {
	/**
	 * Uniquely identifies an alarm.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String self;
	
	/**
	 * Time when alarm was created in the database.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public TC_Date creationTime;
	
	/**
	 * Identifies the type of this alarm, e.g.,
	 * "com_cumulocity_events_TamperEvent".
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String type;
	
	/**
	 * Time of the alarm.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public TC_Date time;
	
	/**
	 * Text description of the alarm.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String text;
	
	/**
	 * The ManagedObject that the alarm originated from, as object containing
	 * the "id" property.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public ManagedObject source;
	
	/**
	 * The status of the alarm: ACTIVE, ACKNOWLEDGED or CLEARED. If status was
	 * not appeared, new alarm will have status ACTIVE. Must be upper-case.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : POST: Optional PUT: Optional
	 * </pre>
	 */
	public String status;
	
	/**
	 * The severity of the alarm: CRITICAL, MAJOR, MINOR or WARNING. Must be
	 * upper-case.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: Optional
	 * </pre>
	 */
	public String severity;
	
	/**
	 * The number of times this alarm has been sent.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public long count;
	
	/**
	 * The first time that this alarm occurred (i.e., where "count" was 1).
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public TC_Date firstOccurenceTime;
	
	/**
	 * History of modifications tracing property changes.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public AuditRecordCollection history;
	
	/**
	 * Additional properties of the event.
	 * <pre>
	 * Occurs : 0..n
	 * PUT/POST :  
	 * </pre>
	 */
	//This field has omitted because of type and field = "*"
	
/*-------------
 * constructor
 */
	/**
	 * 空のオブジェクトを生成します。
	 */
	public Alarm() {
	}
	
	/**
	 * 与えられた引数を保持するオブジェクトを生成します。
	 */
	@Deprecated
	public Alarm(String sourceId, String text) {
		this(sourceId, "c8y_PowerAlarm", text, "ACTIVE", "MINOR");
	}
	
	/**
	 * 与えられた引数を保持するオブジェクトを生成します。
	 */
	public Alarm(String sourceId, String type, String text, String status, String severity) {
		source = new ManagedObject();
		source.id = sourceId;
		time = new TC_Date();
		this.type = type;
		this.text = text;
		this.status = status;
		this.severity = severity;
	}
}

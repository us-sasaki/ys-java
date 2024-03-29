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
	/** severity に設定される、致命的であることを示す定数 */
	public static final String CRITICAL = "CRITICAL";
	
	/** severity に設定される、重大なエラーを示す定数 */
	public static final String MAJOR = "MAJOR";
	
	/** severity に設定される、エラーを示す定数 */
	public static final String MINOR = "MINOR";
	
	/** severity に設定される、警告を示す定数 */
	public static final String WARNING = "WARNING";
	
	/** status に設定される、アラーム通知中であることを示す定数 */
	public static final String ACTIVE = "ACTIVE";
	
	/** status に設定される、アラームが認識されたことを示す定数 */
	public static final String ACKNOWLEDGED = "ACKNOWLEDGED";
	
	/** status に設定される、アラームがクリアされたことを示す定数 */
	public static final String CLEARED = "CLEARED";
	
	
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
	 * "com_cumulocity_events_TamperEvent".<br>
	 * 指定された時間通信がなかった場合　"c8y_UnavailabilityAlarm"<br>
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
	 *
	 * @param	sourceId	alarm を発生させた managed object の id
	 * @param	type		alarm の type
	 * @param	text		alarm の説明文
	 * @param	status		Alarm の status。ACTIVE/ACKNOWLEDGED/CLEARED
	 * 						のいずれかである必要があります
	 * @param	severity	Alarm の severity。CRITICAL/MAJOR/MINOR/WARNING
	 * 						のいずれかである必要があります
	 */
	public Alarm(String sourceId, String type, String text, String status, String severity) {
		if (status != ACTIVE && status != ACKNOWLEDGED && status != CLEARED)
			throw new IllegalArgumentException("Alarm の status は ACTIVE/ACKNOWLEDGED/CLEARED のいずれかである必要があります");
		if (severity != CRITICAL && severity != MAJOR &&
			severity != MINOR && severity != WARNING)
				throw new IllegalArgumentException("Alarm の severity は CRITICAL/MAJOR/MINOR/WARNING のいずれかである必要があります");
		source = new ManagedObject();
		source.id = sourceId;
		time = new TC_Date();
		this.type = type;
		this.text = text;
		this.status = status;
		this.severity = severity;
	}
}

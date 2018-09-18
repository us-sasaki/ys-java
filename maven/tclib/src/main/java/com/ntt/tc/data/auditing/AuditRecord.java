package com.ntt.tc.data.auditing;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.inventory.ID;
import abdom.data.json.JsonObject;

/**
 * AuditRecord class
 * Set changes を JsonObject changes に変更。
 */
public class AuditRecord extends C8yData {
	/**
	 * Uniquely identifies this audit record.
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
	 * Time when audit record was created in the database.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public TC_Date creationTime;
	
	/**
	 * Identifies the type of this audit record.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String type;
	
	/**
	 * Time of the audit record.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public TC_Date time;
	
	/**
	 * Text description of the audit record.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String text;
	
	/**
	 * An optional ManagedObject that the audit record originated from, as
	 * object containing properties "id" and "self".
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public ID source;
	
	/**
	 * The user responsible for the audited action.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Optional
	 * </pre>
	 */
	public String user;
	
	/**
	 * The application used to carry out the audited action.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Optional
	 * </pre>
	 */
	public String application;
	
	/**
	 * The activity that was carried out.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: Optional
	 * </pre>
	 */
	public String activity;
	
	/**
	 * The severity of action: critical, major, minor, warning or information.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: Optional
	 * </pre>
	 */
	public String severity;
	
	/**
	 * An optional collection of objects describing the changes that were
	 * carried out.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public JsonObject changes; // 間違っているかも
	
	/**
	 * Additional properties of the audit record.
	 * <pre>
	 * Occurs : 0..n
	 * PUT/POST : Optional
	 * </pre>
	 */
	//This field has omitted because of type and field = "*"
	
}

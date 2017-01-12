package com.ntt.tc.data.auditing;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.auditing.AuditRecordCollection;

/**
 * AuditAPI class
 * This source is machine-generated.
 */
public class AuditAPI extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Collection of all audit records.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public AuditRecordCollection auditRecords;
	
	/**
	 * Read-only collection of all audit records of a particular type
	 * (placeholder {type}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String auditRecordsForType;
	
	/**
	 * Read-only collection of all audit records for a particular user
	 * (placeholder {user}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String auditRecordsForUser;
	
	/**
	 * Read-only collection of all audit records for a particular application
	 * (placeholder {application}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String auditRecordsForApplication;
	
	/**
	 * Read-only collection of all audit records of a particular user and type
	 * (placeholder {user} and {type}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String auditRecordsForUserAndType;
	
	/**
	 * Read-only collection of all audit records for a particular user and
	 * application (placeholder {user} and {application}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String auditRecordsForUserAndApplication;
	
	/**
	 * Read-only collection of all audit records of a particular type and
	 * application (placeholder {type} and {application}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String auditRecordsForTypeAndApplication;
	
	/**
	 * Read-only collection of all audit records of a particular type, user and
	 * application (placeholder {type}, {user} and {application}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String auditRecordsForTypeAndUserAndApplication;
	
}

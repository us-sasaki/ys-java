package auditing;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.auditing.AuditRecord;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * AuditRecordCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class AuditRecordCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of audit records, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public AuditRecord[] auditRecords;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of audit records.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of audit records.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

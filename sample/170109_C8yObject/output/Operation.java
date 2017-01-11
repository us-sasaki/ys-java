package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * Operation class
 * This source is machine-generated.
 */
public class Operation extends C8yData {
	/**
	 * Uniquely identifies an operation.
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
	 * Time when the operation was created in the database.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String creationTime;
	
	/**
	 * Identifies the target device on which this operation should be
	 * performed.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String deviceID;
	
	/**
	 * External IDs of the target device, see the
	 * [Identity](/guides/reference/identity) interface.
	 * <pre>
	 * Occurs : 0..n
	 * PUT/POST : No
	 * </pre>
	 */
	public ExternalIDCollection deviceExternalIDs;
	
	/**
	 * Reference to bulkOperationId, if this operation was scheduled from Bulk
	 * Operation
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String bulkOperationId;
	
	/**
	 * Operation status, can be one of SUCCESSFUL, FAILED, EXECUTING or
	 * PENDING.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: No PUT: Mandatory
	 * </pre>
	 */
	public String status;
	
	/**
	 * Reason for the failure.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public String failureReason;
	
	/**
	 * Additional properties describing the operation which will be performed
	 * on the device.
	 * <pre>
	 * Occurs : 1..n
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	//omitted since type, field equals "*"
	
}

package com.ntt.tc.data.device;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.identity.ExternalIDCollection;
import abdom.data.json.JsonObject;

/**
 * Operation class
 * This source is machine-generated from c8y-markdown docs.
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
	public TC_Date creationTime;
	
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
	public ExternalIDCollection[] deviceExternalIDs;
	
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
	//This field has omitted because of type and field = "*"
	
}

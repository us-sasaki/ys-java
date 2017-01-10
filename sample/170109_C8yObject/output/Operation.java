package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.ExternalIDCollection;

public class Operation extends C8yData {
	/**
	 * Uniquely identifies an operation.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String self;
	
	/**
	 * Time when the operation was created in the database.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String creationTime;
	
	/**
	 * Identifies the target device on which this operation should be
	 * performed.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public String deviceID;
	
	/**
	 * External IDs of the target device, see the Identity interface.
	 *
	 * Occurs : 0..n
	 * PUT/POST : No
	 */
	public ExternalIDCollection deviceExternalIDs;
	
	/**
	 * Reference to bulkOperationId, if this operation was scheduled from Bulk
	 * Operation
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String bulkOperationId;
	
	/**
	 * Operation status, can be one of SUCCESSFUL, FAILED, EXECUTING or
	 * PENDING.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: No PUT: Mandatory
	 */
	public String status;
	
	/**
	 * Reason for the failure.
	 *
	 * Occurs : 0..1
	 * PUT/POST : No
	 */
	public String failureReason;
	
	/**
	 * Additional properties describing the operation which will be performed
	 * on the device.
	 *
	 * Occurs : 1..n
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public Object *;
	
}

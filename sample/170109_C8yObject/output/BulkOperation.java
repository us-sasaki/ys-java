package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.OperationRepresentation;
import com.ntt.tc.data.BulkOperationProgressRepresentation;

public class BulkOperation extends C8yData {
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
	 * Identifies the target group on which this operation should be performed.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: No PUT: No
	 */
	public String groupId;
	
	/**
	 * Identifies the failed bulk operation from which failed operations should
	 * be rescheduled.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: No PUT: No
	 */
	public String failedBulkOperationId;
	
	/**
	 * Time when operations should be created.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public String startDate;
	
	/**
	 * Delay between every operation creation.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public Double creationRamp;
	
	/**
	 * Operation to be executed for every device in a group.
	 *
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 */
	public OperationRepresentation operationPrototype;
	
	/**
	 * Status of Bulk Operation. Possible values: ACTIVE, COMPLETED, DELETED
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public String status;
	
	/**
	 * Contains information about number of processed operations.
	 *
	 * Occurs : 1
	 * PUT/POST : No
	 */
	public BulkOperationProgressRepresentation progress;
	
}

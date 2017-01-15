package com.ntt.tc.data.device;

import com.ntt.tc.data.C8yData;

/**
 * BulkOperation class
 * This source is machine-generated from c8y-markdown docs.
 */
public class BulkOperation extends C8yData {
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
	 * Identifies the target group on which this operation should be performed.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: No PUT: No
	 * </pre>
	 */
	public String groupId;
	
	/**
	 * Identifies the failed bulk operation from which failed operations should
	 * be rescheduled.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: No PUT: No
	 * </pre>
	 */
	public String failedBulkOperationId;
	
	/**
	 * Time when operations should be created.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public String startDate;
	
	/**
	 * Delay between every operation creation.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public double creationRamp;
	
	/**
	 * Operation to be executed for every device in a group.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : POST: Mandatory PUT: No
	 * </pre>
	 */
	public OperationRepresentation operationPrototype;
	
	/**
	 * Status of Bulk Operation. Possible values: ACTIVE, COMPLETED, DELETED
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String status;
	
	/**
	 * Contains information about number of processed operations.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public BulkOperationProgressRepresentation progress;
	
}

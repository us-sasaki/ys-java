package com.ntt.tc.data.device;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.device.OperationCollection;

/**
 * DeviceControlAPI class
 * This source is machine-generated from c8y-markdown docs.
 */
public class DeviceControlAPI extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Collection of all operations.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public OperationCollection operations;
	
	/**
	 * Read-only collection of all operations in a particular status
	 * (placeholder {status}, see the operation media type below for permitted
	 * values).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String operationsByStatus;
	
	/**
	 * Read-only collection of all operations targeted to a particular agent
	 * (placeholder {agentId}, with the unique ID of the agent).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String operationsByAgentId;
	
	/**
	 * Read-only collection of all operations targeted to a particular agent
	 * (placeholder {agentId} and {status}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String operationsByAgentIdAndStatus;
	
	/**
	 * Read-only collection of all operations to be executed on a particular
	 * device (placeholder {deviceId} with the unique ID of the device).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String operationsByDeviceId;
	
	/**
	 * Read-only collection of all operations in particular state, that should
	 * be executed on a particular device (placeholder {deviceId} and
	 * {status}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String operationsByDeviceIdAndStatus;
	
}

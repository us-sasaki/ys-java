package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.OperationCollection;
import com.ntt.tc.data.String;

public class DeviceControlAPI extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * Collection of all operations.
	 *
	 * Occurs : 1
	 */
	public OperationCollection operations;
	
	/**
	 * Read-only collection of all operations in a particular status
	 * (placeholder {status}, see the operation media type below for permitted
	 * values).
	 *
	 * Occurs : 1
	 */
	public String operationsByStatus;
	
	/**
	 * Read-only collection of all operations targeted to a particular agent
	 * (placeholder {agentId}, with the unique ID of the agent).
	 *
	 * Occurs : 1
	 */
	public String operationsByAgentId;
	
	/**
	 * Read-only collection of all operations targeted to a particular agent
	 * (placeholder {agentId} and {status}).
	 *
	 * Occurs : 1
	 */
	public String operationsByAgentIdAndStatus;
	
	/**
	 * Read-only collection of all operations to be executed on a particular
	 * device (placeholder {deviceId} with the unique ID of the device).
	 *
	 * Occurs : 1
	 */
	public String operationsByDeviceId;
	
	/**
	 * Read-only collection of all operations in particular state, that should
	 * be executed on a particular device (placeholder {deviceId} and
	 * {status}).
	 *
	 * Occurs : 1
	 */
	public String operationsByDeviceIdAndStatus;
	
}

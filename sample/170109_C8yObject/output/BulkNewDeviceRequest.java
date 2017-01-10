package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.List;

public class BulkNewDeviceRequest extends C8yData {
	/**
	 * Number of lines processed from CSV file, without first line (column
	 * headers)
	 *
	 * Occurs : 1
	 */
	public Double numberOfAll;
	
	/**
	 * Number of created device credentials
	 *
	 * Occurs : 1
	 */
	public Double numberOfCreated;
	
	/**
	 * Number of failed creation of device credentials
	 *
	 * Occurs : 1
	 */
	public Double numberOfFailed;
	
	/**
	 * Number of successful creation of device credentials, contains create and
	 * update operations
	 *
	 * Occurs : 1
	 */
	public Double numberOfSuccessful;
	
	/**
	 * Array with updated device credentials
	 *
	 * Occurs : 0..n
	 */
	public List credentialUpdatedList;
	
	/**
	 * Device credentials creation status, possible values: CREATED, FAILED,
	 * CREDENTIAL_UPDATED
	 *
	 * Occurs : 1
	 */
	public String credentialUpdatedList.bulkNewDeviceStatus;
	
	/**
	 * Id of device
	 *
	 * Occurs : 1
	 */
	public String credentialUpdatedList.deviceId;
	
	/**
	 * Array with updated device credentials
	 *
	 * Occurs : 0..n
	 */
	public List failedCreationList;
	
	/**
	 * Device credentials creation status, possible values: CREATED, FAILED,
	 * CREDENTIAL_UPDATED
	 *
	 * Occurs : 1
	 */
	public String failedCreationList.bulkNewDeviceStatus;
	
	/**
	 * Id of device, appears if application can obtain it from file
	 *
	 * Occurs : 0..1
	 */
	public String failedCreationList.deviceId;
	
	/**
	 * Reason of error
	 *
	 * Occurs : 1
	 */
	public String failedCreationList.failureReason;
	
	/**
	 * Line with error
	 *
	 * Occurs : 1
	 */
	public String failedCreationList.line;
	
}

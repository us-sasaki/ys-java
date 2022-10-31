package com.ntt.tc.data.device;

import com.ntt.tc.data.C8yData;
import abdom.data.json.JsonObject;

/**
 * BulkNewDeviceRequest class
 * This source is machine-generated from c8y-markdown docs.
 */
public class BulkNewDeviceRequest extends C8yData {
	/**
	 * Number of lines processed from CSV file, without first line (column
	 * headers)
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public double numberOfAll;
	
	/**
	 * Number of created device credentials
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public double numberOfCreated;
	
	/**
	 * Number of failed creation of device credentials
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public double numberOfFailed;
	
	/**
	 * Number of successful creation of device credentials, contains create and
	 * update operations
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public double numberOfSuccessful;
	
	/**
	 * Array with updated device credentials
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public JsonObject[] credentialUpdatedList;
	
	/**
	 * Device credentials creation status, possible values: CREATED, FAILED,
	 * CREDENTIAL_UPDATED
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String credentialUpdatedListbulkNewDeviceStatus;
	
	/**
	 * Id of device
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String credentialUpdatedListdeviceId;
	
	/**
	 * Array with updated device credentials
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public JsonObject[] failedCreationList;
	
	/**
	 * Device credentials creation status, possible values: CREATED, FAILED,
	 * CREDENTIAL_UPDATED
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String failedCreationListbulkNewDeviceStatus;
	
	/**
	 * Id of device, appears if application can obtain it from file
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String failedCreationListdeviceId;
	
	/**
	 * Reason of error
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String failedCreationListfailureReason;
	
	/**
	 * Line with error
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String failedCreationListline;
	
}

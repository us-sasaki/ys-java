package device;

import com.ntt.tc.data.C8yData;

/**
 * DeviceCredentials class
 * This source is machine-generated from c8y-markdown docs.
 */
public class DeviceCredentials extends C8yData {
	/**
	 * Device identifier, e.g. IMEI
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String id;
	
	/**
	 * Tenant id for authentication
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String tenantId;
	
	/**
	 * New username
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String username;
	
	/**
	 * New password
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String password;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
}

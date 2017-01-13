package real;

import com.ntt.tc.data.C8yData;

/**
 * ConnectResponse class
 * This source is machine-generated from c8y-markdown docs.
 */
public class ConnectResponse extends C8yData {
	/**
	 * Id of message passed in request message
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String channel;
	
	/**
	 * Unique ID of client.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String clientId;
	
	/**
	 * Result of connect.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public boolean successful;
	
	/**
	 * List of notifications from channel.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String[] data;
	
	/**
	 * Connect failure reason.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String error;
	
}

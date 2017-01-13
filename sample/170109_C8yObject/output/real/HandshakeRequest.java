package real;

import com.ntt.tc.data.C8yData;

/**
 * HandshakeRequest class
 * This source is machine-generated from c8y-markdown docs.
 */
public class HandshakeRequest extends C8yData {
	/**
	 * Id of message, required to match response messageÂ?
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel, required value "/meta/handshake".
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String channel;
	
	/**
	 * Bayeux protocol version used by client.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String version;
	
	/**
	 * Minimum server-side Bayeux protocol version required by client.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String minimumVersion;
	
	/**
	 * List of connection types supported by client.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String[] supportedConnectionTypes;
	
	/**
	 * Session configuration parameters.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public JsonObject advice;
	
}

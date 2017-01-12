package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;

/**
 * Request class
 * This source is machine-generated.
 */
public class Request extends C8yData {
	/**
	 * Id of message, required to match response message
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public Int id;
	
	/**
	 * Name of channel, required value "/meta/disconnect".
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String channel;
	
	/**
	 * Unique ID of client received during handshake.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String clientId;
	
}

package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;

/**
 * DisconnectResponse class
 * This source is machine-generated from c8y-markdown docs.
 */
public class DisconnectResponse extends C8yData {
	/**
	 * Id of message passed in request message
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel, required value "/meta/disconnect".
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String channel;
	
	/**
	 * Result of disconnect operation.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public boolean successful;
	
	/**
	 * Unique ID of client received during handshake.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String clientId;
	
	/**
	 * Disconnect failure reason.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String error;
	
}

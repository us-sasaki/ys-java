package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;

/**
 * UnsubscribeRequest class
 * This source is machine-generated from c8y-markdown docs.
 */
public class UnsubscribeRequest extends C8yData {
	/**
	 * Id of message, required to match reponse message
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel, required value "/meta/unsubscribe".
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String channel;
	
	/**
	 * Unique client ID received during handshake.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String clientId;
	
	/**
	 * Name of channel.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String subscription;
	
}

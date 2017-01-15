package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;

/**
 * SubscribeRequest class
 * This source is machine-generated from c8y-markdown docs.
 */
public class SubscribeRequest extends C8yData {
	/**
	 * Id of message, required to match reponse message
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel, required value "/meta/subscribe"
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
	
	/**
	 * Name of channel to subscribe to.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String subscription;
	
}

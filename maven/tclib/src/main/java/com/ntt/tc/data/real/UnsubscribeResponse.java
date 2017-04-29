package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;

/**
 * UnsubscribeResponse class
 * This source is machine-generated from c8y-markdown docs.
 */
public class UnsubscribeResponse extends C8yData {
	/**
	 * Id of message passed in request message
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
	 * Unique ID of client.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String clientId;
	
	/**
	 * Name of subscribed channel.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String subscription;
	
	/**
	 * Result of unsubscription.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public boolean successful;
	
	/**
	 * Unsubscription failure reason.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String error;
	
}
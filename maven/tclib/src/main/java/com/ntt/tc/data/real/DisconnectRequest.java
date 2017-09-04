package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;

/**
 * DisconnectRequest class
 * This source is machine-generated from c8y-markdown docs.
 */
public class DisconnectRequest extends C8yData {
	/**
	 * Id of message, required to match response message
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel, required value "/meta/disconnect".
	 * 固定値 "/meta/disconnect" を設定
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String channel = "/meta/disconnect";
	
	/**
	 * Unique ID of client received during handshake.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String clientId;
	
}

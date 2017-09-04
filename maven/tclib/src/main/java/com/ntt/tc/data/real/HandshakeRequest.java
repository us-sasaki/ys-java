package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;
import abdom.data.json.JsonObject;

/**
 * HandshakeRequest class
 * advice が JsonObject となるため、Advice に変更
 */
public class HandshakeRequest extends C8yData {
	/**
	 * Id of message, required to match response messageﾂ?
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel, required value "/meta/handshake".
	 * 固定値 "/meta/handshake" を設定
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String channel = "/meta/handshake";
	
	/**
	 * Bayeux protocol version used by client.
	 * 固定値 "1.0" を設定
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String version = "1.0";
	
	/**
	 * Minimum server-side Bayeux protocol version required by client.
	 * 固定値 "1.0beta" を設定
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String minimumVersion = "1.0beta";
	
	/**
	 * List of connection types supported by client.
	 * [ "long-polling" ] を設定
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String[] supportedConnectionTypes = new String[] {"long-polling"};
	
	/**
	 * Session configuration parameters.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public Advice advice;
	
}

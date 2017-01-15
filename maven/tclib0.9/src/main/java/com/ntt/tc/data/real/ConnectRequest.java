package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;
import abdom.data.json.JsonObject;

/**
 * ConnectRequest class
 * advice が JsonObject となるため、Advice に修正。
 */
public class ConnectRequest extends C8yData {
	/**
	 * Id of message, required to match reponse message
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel, required value "/meta/connect".
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
	 * Selected connection type.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String connectionType;
	
	/**
	 * Configuration paramaters for current connect message.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public Advice advice;
	
}

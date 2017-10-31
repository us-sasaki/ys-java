package com.ntt.tc.data.real;

import abdom.data.json.JsonObject;
import com.ntt.tc.data.C8yData;

/**
 * Real-time Notification Response クラス
 * ハンドシェーク、サブスクライブ、コネクトがリクエスト配列中に混在すると
 * 思われるため、同一クラス化。
 * 別クラスとしてスーパークラスを定義した方がよいかも知れない。
 */
public class NotificationResponse extends C8yData {
	/**
	 * Id of message passed in request message
	 * <pre>
	 * handshake 時に利用
	 * subscribe 時に利用
	 * connect 時に利用
	 * Occurs : 1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel, required value "/meta/handshake".
	 * <pre>
	 * handshake 時に利用
	 * subscribe 時に利用
	 * unsubscribe 時に利用
	 * connect 時に利用
	 * disconnect 時に利用
	 * Occurs : 1
	 * </pre>
	 */
	public String channel;
	
	/**
	 * Bayeux protocol version used by server.
	 * <pre>
	 * handshake 時に利用
	 * Occurs : 0..1
	 * </pre>
	 */
	public String version;
	
	/**
	 * Minimum client-side Bayeux protocol version required by server.
	 * <pre>
	 * handshake 時に利用
	 * Occurs : 0..1
	 * </pre>
	 */
	public String minimumVersion;
	
	/**
	 * Connection types supported by both client and server (i.e., intersection
	 * between client and server options).
	 * <pre>
	 * handshake 時に利用
	 * Occurs : 0..1
	 * </pre>
	 */
	public String[] supportedConnectionTypes;
	
	/**
	 * Unique client ID generated by server.
	 * <pre>
	 * handshake 時に利用
	 * subscribe 時に利用
	 * unsubscribe 時に利用
	 * connect 時に利用
	 * disconnect 時に利用
	 * Occurs : 0..1
	 * </pre>
	 */
	public String clientId;
	
	/**
	 * Name of channel.
	 * <pre>
	 * subscribe 時に利用
	 * unsubscribe 時に利用
	 * Occurs : 1
	 * </pre>
	 */
	public String subscription;
	
	/**
	 * Result of handshake.
	 * <pre>
	 * handshake 時に利用
	 * subscribe 時に利用
	 * unsubscribe 時に利用
	 * connect 時に利用
	 * disconnect 時に利用
	 * Occurs : 1
	 * </pre>
	 */
	public boolean successful;
	
	/**
	 * List of notifications from channel.
	 * <pre>
	 * connect 時に利用
	 * Occurs : 1
	 * </pre>
	 */
	public JsonObject data;
	
	/**
	 * Handshake failure reason.
	 * <pre>
	 * handshake 時に利用
	 * subscribe 時に利用
	 * unsubscribe 時に利用
	 * connect 時に利用
	 * disconnect 時に利用
	 * Occurs : 0..1
	 * </pre>
	 */
	public String error;
	
}

package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;
import abdom.data.json.JsonObject;

/**
 * Real-time Notification Request クラス
 * ハンドシェーク、サブスクライブ、コネクトがリクエスト配列中に混在すると
 * 思われるため、同一クラス化。
 * 別クラスとしてスーパークラスを定義した方がよいかも知れない。
 */
public class NotificationRequest extends C8yData {
	/**
	 * Id of message, required to match response messageﾂ?
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public int id;
	
	/**
	 * Name of channel, required value "/meta/handshake".
	 * handshake 時 "/meta/handshake" を設定
	 * subscribe 時 "/meta/subscribe" を設定
	 * connect   時 "/meta/connect" を設定
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String channel;
	
	/**
	 * Bayeux protocol version used by client.
	 * handshake 時 "1.0" を設定
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String version;
	
	/**
	 * Minimum server-side Bayeux protocol version required by client.
	 * handshake 時 "1.0beta" を設定
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String minimumVersion;
	
	/**
	 * List of connection types supported by client.
	 * handshake 時 [ "long-polling" ] を設定
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String[] supportedConnectionTypes = new String[] {"long-polling"};
	
	/**
	 * Unique ID of client received during handshake.
	 * subscribe 時、設定要
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String clientId;
	
	/**
	 * Name of channel to subscribe to.
	 * subscribe 時、設定要
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String subscription;
	
	/**
	 * Selected connection type.
	 * connect 時 "long-polling" を設定
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String connectionType = "long-polling";
	
	/**
	 * Session configuration parameters.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public Advice advice;
	
}

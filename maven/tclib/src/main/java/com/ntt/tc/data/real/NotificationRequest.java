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
	 * メッセージの ID で、レスポンスメッセージのものと一致を確認する
	 * 必要があります。
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public int id;
	
	/**
	 * チャンネル名で、"/meta/handshake" など、フェーズにより規定値を利用します。
	 * <pre>
	 * handshake 時 "/meta/handshake" を設定
	 * subscribe 時 "/meta/subscribe" を設定
	 * unsubscribe 時 "/meta/unsubscribe" を設定
	 * connect   時 "/meta/connect" を設定
	 * disconnect時 "/meta/disconnect" を設定
	 * Occurs : 1
	 * </pre>
	 */
	public String channel;
	
	/**
	 * ユーザー側の Bayeux プロトコルバージョン
	 * <pre>
	 * handshake 時 "1.0" を設定
	 * Occurs : 1
	 * </pre>
	 */
	public String version;
	
	/**
	 * クライアントが指定する、サーバーの最低 Bayeux プロトコルバージョンです。
	 * Minimum server-side Bayeux protocol version required by client.
	 * <pre>
	 * handshake 時 "1.0beta" を設定
	 * Occurs : 0..1
	 * </pre>
	 */
	public String minimumVersion;
	
	/**
	 * クライアントでサポートされる、コネクションタイプのリストです。
	 * handshake 時 [ "long-polling" ] を設定
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String[] supportedConnectionTypes = new String[] {"long-polling"};
	
	/**
	 * ハンドシェーク時に受け取るクライアントの一意な ID です。
	 * <pre>
	 * subscribe 時、設定要
	 * unsubscribe 時、設定要
	 * connect 時、設定要
	 * disconnect 時、設定要
	 * Occurs : 1
	 * </pre>
	 */
	public String clientId;
	
	/**
	 * サブスクライブするチャンネルの名前です。
	 * "/operations/<managed object id>" のような文字列を設定します。
	 * <pre>
	 * subscribe 時、設定要
	 * unsubscribe 時、設定要
	 * Occurs : 1
	 * </pre>
	 */
	public String subscription;
	
	/**
	 * Selected connection type.
	 * <pre>
	 * connect 時 "long-polling" を設定
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

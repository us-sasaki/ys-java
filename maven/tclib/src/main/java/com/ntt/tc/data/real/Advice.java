package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;

/**
 * Advice class
 * 自動生成では ConnectAdvice となるため、Advice に変更。
 * timeout を TC_Date から int に変更
 * HandshakeAdvice は削除
 */
public class Advice extends C8yData {
	/**
	 * Interval between sending of connect message and response from server.
	 * Overrides server default settings for current request-response
	 * conversation.
	 * デフォルト値 1200000 を指定。
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public int timeout = 1200000;
	
	/**
	 * Period above which server will close session, if not received next
	 * connect message from client. Overrides server default settings for
	 * current request-response conversation.
	 * デフォルト値 30000 を指定。
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public int interval = 30000;
	
}

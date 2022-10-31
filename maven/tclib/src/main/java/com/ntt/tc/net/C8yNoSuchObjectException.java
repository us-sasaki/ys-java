package com.ntt.tc.net;

/**
 * Cumulocity REST で、指定されたオブジェクトが存在しない場合に
 * スローされる例外です。
 * Rest オブジェクトでは 404 Not Found は正常応答扱いのため、異常と見なす
 * べきときにスローされます。
 * 存在しないことが想定されず、通知する必要がある場合にスローされます。
 *
 * @version		May 9, 2018
 * @author		Yusuke Sasaki
 */
public class C8yNoSuchObjectException extends C8yRestException {
	
/*-------------
 * constructor
 */
	public C8yNoSuchObjectException() {
		super();
	}
	public C8yNoSuchObjectException(String msg) {
		super(msg);
	}
	public C8yNoSuchObjectException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public C8yNoSuchObjectException(Throwable cause) {
		super(cause);
	}
	public C8yNoSuchObjectException(Rest.Response resp) {
		super(resp.toJson().get("error").getValue() + " / "+resp.toJson().get("message").getValue() + " / " + resp.toJson().get("info").getValue());
	}
}

package com.ntt.tc.net;

/**
 * Cumulocity REST で、指定されたオブジェクトが存在しない場合に
 * スローされる例外です。
 *
 * @version		May 9, 2018
 * @author		Yusuke Sasaki
 */
public class C8yNoSuchObjectException extends C8yRestRuntimeException {
	
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
	
}

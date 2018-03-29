package com.ntt.tc.net;

import java.io.IOException;

/**
 * Cumulocity REST で、iterator などで用いる、catch 不要な例外への変換用
 * 例外です。
 *
 * @version		October 16, 2017
 * @author		Yusuke Sasaki
 */
public class C8yRestRuntimeException extends RuntimeException {
	
/*-------------
 * constructor
 */
	public C8yRestRuntimeException() {
		super();
	}
	public C8yRestRuntimeException(String msg) {
		super(msg);
	}
	public C8yRestRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public C8yRestRuntimeException(Throwable cause) {
		super(cause);
	}
	
	public C8yRestException getC8yCause() {
		return (C8yRestException)getCause();
	}
	
}

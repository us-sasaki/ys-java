package com.ntt.tc.net;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import abdom.data.json.JsonType;

/**
 * Cumulocity REST で、レスポンスが400以上のときに発生する例外です。
 * REST の要求、応答のオブジェクトを保持します。
 *
 * @version		September 25, 2017
 * @author		Yusuke Sasaki
 */
public class C8yRestException extends IOException {
	protected Rest.Response response;
	protected String location;
	protected String method;
	protected String contentType;
	protected String accept;
	protected JsonType body;
	
/*-------------
 * constructor
 */
	public C8yRestException(Rest.Response response, String location, String method, String contentType, String accept) {
		super(makeMessage(response, location, method, contentType, accept));
		
		this.response = response;
		this.location = location;
		this.method = method;
		this.contentType = contentType;
		this.accept = accept;
		if (response.body != null && response.body.length > 0)
			this.body = JsonType.parse(new String(response.body, StandardCharsets.UTF_8));
		else
			this.body = JsonType.NULL;
	}
	
	public C8yRestException() {
		super();
	}
	public C8yRestException(String msg) {
		super(msg);
	}
	public C8yRestException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public C8yRestException(Throwable cause) {
		super(cause);
	}
	
/*------------------
 * instance methods
 */
	private static String makeMessage(Rest.Response response, String location, String method, String contentType, String accept) {
		String msg = "ep=" + location + " method=" + method + " type=" + contentType +" accept=" + accept;
		if (response != null)
			msg = msg + " status=" + response.status + " msg="
						+ response.message + " body="
						+ new String(response.body, StandardCharsets.UTF_8);
		return msg;
	}
	
	public Rest.Response getResponse() {
		return response;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getAccept() {
		return accept;
	}
	
	public JsonType getBody() {
		return body;
	}
}

package com.ntt.net;

import java.io.IOException;

import abdom.data.json.JsonType;

/**
 * Cumulocity REST で、レスポンスが400以上のときに発生する例外です。
 * REST の要求、応答のオブジェクトを保持します。
 *
 * @version		December 17, 2017
 * @author		Yusuke Sasaki
 */
public class JsonRestException extends IOException {
	protected JsonRest.Response response;
	protected String location;
	protected String method;
	protected JsonType body;
	
/*-------------
 * constructor
 */
	public JsonRestException(JsonRest.Response response, String location, String method) {
		this("ep=" + location + " method=" + method +
			" code=" + response.code +
			" msg=" + response.message + " body=" + response.body);
		this.response = response;
		this.location = location;
		this.method = method;
		this.body = response.body;
	}
	
	public JsonRestException() {
		super();
	}
	public JsonRestException(String msg) {
		super(msg);
	}
	public JsonRestException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public JsonRestException(Throwable cause) {
		super(cause);
	}
	
/*------------------
 * instance methods
 */
	public JsonRest.Response getResponse() {
		return response;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getMethod() {
		return method;
	}
	
	public JsonType getBody() {
		return body;
	}
}

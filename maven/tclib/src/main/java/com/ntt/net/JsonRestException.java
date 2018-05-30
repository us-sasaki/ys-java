package com.ntt.net;

import java.io.IOException;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.JsonParseException;

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
		try {
			this.body = response.toJson();
		} catch (JsonParseException e) {
			this.body = new JsonValue("not json:" + new String(response.body, java.nio.charset.StandardCharsets.UTF_8));
		}
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

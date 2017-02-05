package com.ntt.tc.data;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;

/**
 * C8y で使用される、URI template です。
 *
 * @version		January 22nd, 2016
 * @author		Yusuke Sasaki
 */
public class URITemplate extends C8yValue {
	/**
	 * 内部的には String として保持します。
	 */
	protected String template;
	
/*-------------
 * constructor
 */
	public URITemplate() {
	}
	
	public URITemplate(String uri) {
		this.template = uri;
	}
	public URITemplate(JsonType uri) {
		if (uri.getType() != JsonType.TYPE_STRING)
			throw new IllegalArgumentException("URITemplate(JsonType) のコンストラクタでは JsonType は文字列型である必要があります。指定された値:" + uri);
		this.template = uri.getValue();
	}
	
/*-----------
 * overrides
 */
	@Override
	public void fill(JsonType jt) {
		if (jt.getType() != JsonType.TYPE_STRING)
			throw new C8yFormatException("URI を文字列以外で fill() しようとしています:"+jt);
		template = jt.getValue();
	}
	
	@Override
	public JsonType toJson() {
		return new JsonValue(template);
	}
	
/*------------------
 * instance methods
 */
//	public URI fill(JsonType container) {
//		return null;
//	}
}

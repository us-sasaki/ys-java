package com.ntt.tc.data;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;

/**
 * C8y で使用される、URIです。
 *
 * @version		January 22nd, 2016
 * @author		Yusuke Sasaki
 */
public class URI extends C8yValue {
	/**
	 * 内部的には JsonValue(string) として保持します。
	 */
	protected JsonValue uri;
	
/*-------------
 * constructor
 */
	public URI() {
	}
	
	public URI(String uri) {
		this.uri = new JsonValue(uri);
	}
	public URI(JsonType uri) {
		if (uri.getType() != JsonType.TYPE_STRING)
			throw new IllegalArgumentException("URI(JsonType) のコンストラクタでは JsonType は文字列型である必要があります。指定された値:" + uri);
		this.uri = (JsonValue)uri;
	}
	
/*-----------
 * overrides
 */
	@Override
	public void fill(JsonType jt) {
		if (jt.getType() != JsonType.TYPE_STRING)
			throw new C8yFormatException("URI を文字列以外で fill() しようとしています:"+jt);
		uri = (JsonValue)jt;
	}
	
	@Override
	public JsonType toJson() {
		return uri;
	}
	
}

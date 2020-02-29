package com.ntt.tc.data;

import abdom.data.json.JsonType;
import abdom.data.json.Jsonizable;
import abdom.data.json.object.JValue;

/**
 * Cumulocity のデータのうち、c8y_IsDevice のように object とも
 * string ともなりうるような値を表すクラスです。
 * 内部的に JsonType を保持しており、fill / toJson 処理をこの JsonType への
 * 設定 / 取得としています。
 *
 * @version		7 February, 2020
 * @author		Yusuke Sasaki
 */
public class C8yJsonData extends C8yValue {
	protected JsonType innerValue;
	
/*-------------
 * constructor
 */
	/**
	 * JsonType.NULL を示すオブジェクトを生成します。
	 */
	public C8yJsonData() {
		innerValue = JsonType.NULL;
	}
	
	/**
	 * Jsonizable を指定してオブジェクトを生成します。
	 *
	 * @param		v		Jsonizable オブジェクト
	 */
	public C8yJsonData(Jsonizable v) {
		innerValue = v.toJson();
	}
	
	/**
	 * JSON 文字列を指定してオブジェクトを生成します。
	 *
	 * @param		json		JSON 文字列
	 */
	public C8yJsonData(String json) {
		innerValue = JsonType.parse(json);
	}
	
/*------------------
 * instance methods
 */
	@Override
	public void fill(Jsonizable value) {
		innerValue = value.toJson();
	}
	
	@Override
	public JsonType toJson() {
		return innerValue;
	}
}

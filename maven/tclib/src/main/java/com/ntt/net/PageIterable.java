package com.ntt.net;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

import abdom.data.json.object.Jsonizer;
import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;

/**
 * PageIterable は複数ページで情報提供される Rest API を、
 * 単純な繰り返し処理で扱うための便利クラスです。
 *
 * @author		Yusuke Sasaki
 * @version		December 10, 2018
 */
public class PageIterable implements Iterable<JsonType> {
	
	protected JsonRest	rest;
	protected String	url;
	protected String	pagePropertyName;
	protected String	pageSizePropertyName;
	protected String	fieldName;
	
/*-------------
 * constructor
 */
	/**
	 * PageIterable を作成します。
	 * url に + (0x2B) が入っていた場合、自動的に %2B に変換されます。
	 *
	 * @param	rest	Rest オブジェクト
	 * @param	url		API の endpoint。例 /measurement/measurements/ 
	 * @param	fieldName	配列を格納しているフィールド名を指定します
	 */
	public PageIterable(JsonRest rest, String url, String pagePropertyName, String pageSizePropertyName, String fieldName) {
		// + -> %2B に変換
		this.url = url.replace("+", "%2B");
		this.rest = rest;
		this.pagePropertyName = pagePropertyName;
		this.pageSizePropertyName = pageSizePropertyName;
		this.fieldName = fieldName;
	}
	
/*------------------
 * instance methods
 */
 	@Override
	public Iterator<JsonType> iterator() {
		return new PagenationIterator(rest, url, pagePropertyName, pageSizePropertyName, fieldName);
	}
}

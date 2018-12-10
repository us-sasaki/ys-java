package com.ntt.net;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

import abdom.data.json.object.Jsonizer;
import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;

/**
 * PagenationIterator は複数ページで情報提供される Rest API を、
 * 単純な繰り返し処理で扱うための便利クラスです。
 *
 * @author		Yusuke Sasaki
 * @version		December 7, 2018
 */
public class PagenationIterator implements Iterator<JsonType> {
	
	protected static final int FETCH_SIZE = 100;
	
	protected JsonRest	rest;
	protected String	pagePropertyName;
	protected String	pageSizePropertyName;
	protected String	fieldName;
	
	protected JsonType	buffer; // JsonArray
	protected int	cursor;
	protected String	url;
	protected int	pageSize;
	protected int	currentPage;
	
/*-------------
 * constructor
 */
	/**
	 * PagenationIterator を作成します。
	 * フェッチサイズのデフォルト値は 100 ですが、url に pageSize= 指定を
	 * するとそれが利用されます。url に + (0x2B) が入っていた場合、自動的に
	 * %2B に変換されます。
	 *
	 * @param	rest	Rest オブジェクト
	 * @param	url		API の endpoint。例 /measurement/measurements/ 
	 * @param	fieldName	配列を格納しているフィールド名を指定します
	 */
	public PagenationIterator(JsonRest rest, String url, String pagePropertyName, String pageSizePropertyName, String fieldName) {
		// + -> %2B に変換
		url = url.replace("+", "%2B");
		this.rest = rest;
		this.pagePropertyName = pagePropertyName;
		this.pageSizePropertyName = pageSizePropertyName;
		
		// デフォルトページサイズの設定
		pageSize = FETCH_SIZE;
		
		// url を、endopoint や query に分割
		String[] parts = url.split("[\\?&]");
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : parts) {
			if (s.startsWith(pageSizePropertyName+"=")) {
				// pageSize 指定があれば、上書きする
				pageSize = Integer.parseInt(s.substring(9));
			} else if (s.startsWith(pagePropertyName+"=")) {
				// currentPage 指定があれば、上書きする
				currentPage = Integer.parseInt(s.substring(12)) - 1;
			} else {
				boolean http = (s.startsWith("/") && s.indexOf('=')==-1);
				if (http) {
				} else {
					// URL エンコーディング
					if (first) {
						sb.append('?');
						first = false;
					} else sb.append('&');
				}
				sb.append(s);
			}
		}
		this.url = sb.toString();
		this.fieldName = fieldName;
	}
	
/*------------------
 * instance methods
 */
	private void fetch() {
		// 読み込み
		currentPage++;
		
		try {
			String sep = "?";
			if (url.indexOf('?') > -1) sep = "&";
			String ep = url+sep+pageSizePropertyName+"="+pageSize+
							"&"+pagePropertyName+"="+currentPage;
			JsonRest.Response resp = rest.get(ep);
			JsonType jt = resp.toJson().get(fieldName); // JsonArray
			if (jt == null)
				throw new NoSuchElementException(
					"PagenationIterator に指定された配列フィールド " +
					fieldName + " の要素が返却されませんでした。" +
					"フィールド名があっているか確認して下さい。 結果" +
					resp.toJson().toString().substring(0, 20) );
			
			if (jt.size() == 0) buffer = null;
			else {
				buffer = jt;
			}
			cursor = 0;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	@Override
	public boolean hasNext() {
		if (buffer == null) {
			fetch();
			if (buffer == null) return false;
		}
		return (cursor < buffer.size());
	}
	
	@Override
	public JsonType next() {
		if (buffer == null) {
			fetch();
			if (buffer == null)
				throw new NoSuchElementException("PagenationIterator が終わりに達している状態で next() を呼び出しました。(url="+url+", field="+fieldName+")");
		}
		JsonType result = buffer.get(cursor++);
		if (cursor == buffer.size()) buffer = null;
		
		return result;
	}
}

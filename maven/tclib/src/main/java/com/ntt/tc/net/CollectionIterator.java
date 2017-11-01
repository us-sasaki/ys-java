package com.ntt.tc.net;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Iterator;

import abdom.data.json.object.Jsonizer;

/**
 * CollectionIterator は c8y の get collection API を用いて、
 * 繰り返し処理を行うための便利クラスです。
 * currentPage/pageSize 指定を隠蔽し、単純な繰り返し処理を行えます。
 *
 * @author		Yusuke Sasaki
 * @version		October 16, 2017
 */
class CollectionIterator<T> implements Iterator<T> {
	
	protected static final int FETCH_SIZE = 100;
	
	protected Rest	rest;
	protected String	fieldName;
	protected Class<T>	compType;
	
	protected T[]	buffer;
	protected int	cursor;
	protected String	url;
	protected int	pageSize;
	protected int	currentPage;
	
/*-------------
 * constructor
 */
	/**
	 * Collection Iterator を作成します。
	 * フェッチサイズのデフォルト値は 100 ですが、url に pageSize= 指定を
	 * するとそれが利用されます。
	 *
	 * @param	rest	Rest オブジェクト
	 * @param	url		API の endpoint。例 /measurement/measurements/ 
	 *					後ろの /measurements/ を collection の要素を格納する
	 *					属性とみなします
	 * @param	compType	Iterator で返す要素の型です(Measurement など)
	 */
	public CollectionIterator(Rest rest, String url, Class<T> compType) {
		this.rest = rest;
		this.compType = compType;
		
		// デフォルトページサイズの設定
		pageSize = FETCH_SIZE;
		
		// url を、endopoint や query に分割
		String[] parts = url.split("[\\?&]");
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : parts) {
			if (s.startsWith("pageSize=")) {
				// pageSize 指定があれば、上書きする
				pageSize = Integer.parseInt(s.substring(9));
			} else if (s.startsWith("currentPage=")) {
				// currentPage 指定があれば、上書きする
				currentPage = Integer.parseInt(s.substring(12)) - 1;
			} else {
				boolean http = (s.startsWith("/") && s.indexOf('=')==-1);
				if (http) {
					// end point の後ろの文字列を field 名として利用
					String[] s2 = s.split("\\/");
					fieldName = s2[s2.length-1];
				} else {
					// URL エンコーディング
					if (first) sb.append('?');
					else sb.append('&');
					first = false;
				}
				sb.append(s);
			}
		}
		this.url = sb.toString();
	}
	
/*------------------
 * instance methods
 */
	@SuppressWarnings("unchecked")
	private void fetch() {
		// 読み込み
		currentPage++;
		
		
		try {
			String sep = "?";
			if (url.indexOf('?') > -1) sep = "&";
			String ep = url+sep+"pageSize="+pageSize+
							"&currentPage="+currentPage;
			Rest.Response resp = rest.get(ep);
			
			buffer = (T[])Jsonizer.toArray(resp.toJson().get(fieldName), (T[])Array.newInstance(compType, 0));
			
			cursor = 0;
		} catch (IOException ioe) {
			throw new C8yRestRuntimeException(ioe);
		}
	}
	
	@Override
	public boolean hasNext() {
		if (buffer == null) fetch();
		return (cursor < buffer.length);
	}
	
	@Override
	public T next() {
		if (buffer == null) fetch();
		T result = buffer[cursor++];
		if (cursor == buffer.length) fetch();
		
		return result;
	}
}

package com.ntt.net;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;

import abdom.data.json.JsonArray;
import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.JsonObject;
import abdom.data.json.Jsonizable;
import abdom.data.json.JsonParseException;

import static java.net.HttpURLConnection.*;

/**
 * REST による要求を簡単に行うためのクラス。軽量化のため、外部ライブラリに
 * 非依存でつくる(java.net.HttpURLConnection ベース)
 * json-stream に対応するため、結果を JsonType.parse(Reader) で構築する。
 * レスポンスボディが JSON でない場合(cometd関連でhtmlを返す場合がある)、
 * エラー文字列を表す JsonValue レスポンスが返却されることに注意して下さい。
 *
 * @version	7, December 2017
 * @author	Yusuke Sasaki
 */
public class JsonRest {
	
	/** host を示す URL 文字列(http:// or https://) */
	protected String urlStr;
	
	/** HEADER に指定する項目 */
	protected Map<String, String> header;
	
	/** HttpURLConnection */
	protected HttpURLConnection con;
	protected InputStream in;
	protected OutputStream out;
	
	/**
	 * HTTP レスポンスを表す内部クラス
	 */
	public static class Response implements Jsonizable {
		/**
		 * HTTP レスポンスコードを返却します。
		 *
		 * @see		java.net.HttpURLConnection
		 */
		public int code;
		public String message;
		protected JsonType body;
		
		/**
		 * 結果の body を String で取得します
		 */
		@Override
		public String toString() {
			if (body == null) return "null";
			return body.toString();
		}
		
		@Override
		public String toString(String indent) {
			return toJson().toString(indent);
		}
		
		@Override
		public String toString(String indent, int maxwidth) {
			return toJson().toString(indent, maxwidth);
		}
		
		/**
		 * 結果の body を JsonType で取得します。
		 * エラーレスポンスに関する結果は不定で、通常 JsonParseExcception
		 * がスローされます。
		 */
		@Override
		public JsonType toJson() {
			if (body == null) return new JsonValue(null);
			return body;
		}
	}
	
/*-------------
 * Constructor
 */
	/**
	 * 指定された url を保持する Rest を作成します。
	 */
	public JsonRest(String urlStr) {
		if (urlStr == null)
			throw new IllegalArgumentException("url が指定されていません");
		this.urlStr = urlStr;
		header = new HashMap<String, String>();
		header.put("Content-Type", "application/json");
		header.put("Accept", "application/json");
	}
	
/*------------------
 * instance methods
 */
	/**
	 * ヘッダを指定します。
	 */
	public void putHeader(String key, String value) {
		header.put(key, value);
	}
	
	public void setHeader(Map<String, String> header) {
		this.header = header;
	}
	
	/**
	 * GET リクエストをします。
	 *
	 * @param	resource	GETするリソース
	 * @return	Rest.Response オブジェクト
	 */
	public Response get(String location) throws IOException {
		return requestImpl(location, "GET", null);
	}
	
	/**
	 * DELETE リクエストをします。
	 */
	public Response delete(String location) throws IOException {
		return requestImpl(location, "DELETE", null);
	}
	
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String resource, Jsonizable json)
							throws IOException {
		return put(resource, json.toString());
	}
	
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String resource, String body)
							throws IOException {
		return requestImpl(resource, "PUT", body.getBytes("UTF-8"));
	}
	
	/**
	 * POST リクエストをします。
	 */
	public Response post(String location, Jsonizable json)
							throws IOException {
		return post(location, json.toString());
	}
	
	/**
	 * Content-Type を指定して POST リクエストをします。
	 */
	public Response post(String location, String body)
							throws IOException {
		return requestImpl(location, "POST", body.getBytes("UTF-8"));
	}
	
	/**
	 * Httpリクエストの実処理を行います。
	 * Cumulocity 固有のヘッダを付加します。
	 *
	 * @param	location	リソースの場所 /platform 等
	 * @param	method		GET/POST/PUT/DELETE
	 * @param	contentType	アプリケーションタイプ(platformApi等, c8y 独自の
	 *						type, または multipart/form-data などフル指定)
	 *						'/' を含む場合、フル指定と見なされます。
	 *						空文字列では、application/json が設定されます。
	 *						method が GET / DELETE の場合設定されません。
	 * @param	accept		Accept タイプ(platformApi等, c8y 独自の type,
	 *						または application/json-stream 等フル指定)
	 *						'/' を含む場合、フル指定と見なされます。
	 *						空文字列では、application/json が設定されます。
	 * @param	body		body に設定するデータ
	 */
	protected synchronized Response requestImpl(String location, String method,
								byte[] body) throws IOException {
		URL url = null;
		if (location.startsWith("http://") ||
				location.startsWith("https://")) {
			url = new URL(location);
		} else {
			url = new URL(urlStr + location);
		}
		con = (HttpURLConnection)url.openConnection();
		
		// 出力設定
		boolean doOutput = (body != null && body.length > 0);
		if (doOutput) con.setDoOutput(true);
		
		// メソッド
		con.setRequestMethod(method);
		
		// header の設定
		if (header != null) {
			for (String key : header.keySet()) {
				if (key.equals("Content-Type") &&
							("GET".equals(method) || "DELETE".equals(method)))
								continue;
				
				con.setRequestProperty(key, header.get(key));
			}
		}
		
		// 出力
		if (doOutput) {
			out = con.getOutputStream();
			BufferedOutputStream bo = new BufferedOutputStream(out);
			bo.write(body);
			bo.flush();
		}
		
		// 結果オブジェクトの生成
		Response resp = new Response();
		resp.code = con.getResponseCode();
		
		ByteArrayOutputStream baos =  new ByteArrayOutputStream();
		try {
			in = null;
			if (resp.code < 400) {
				// 400以降のエラーが返されると InputStream が取得できない
				in = con.getInputStream(); // may throw IOException
			} else {
				// ErrorStream で取得する
				in = con.getErrorStream(); // may null
			}
			if (in != null) { // ErrorStream は null となることがある
				Reader r = new InputStreamReader(in, "UTF-8");
				try {
					JsonType result = JsonType.parse(r);
					resp.body = result;
				} catch (JsonParseException jpe) {
					resp.body = new JsonValue("Not JSON : " + jpe);
				}
				r.close(); //bis.close();
				in.close();
			}
		} catch (IOException ioe) {
			throw ioe;
		}
		resp.message = con.getResponseMessage();
		
		//
		// レスポンスコードの処理(404 Not Found は正常応答)
		//
		if (resp.code >= 400 && resp.code != 404)
			throw new JsonRestException(resp, location, method);
		
		return resp;
	}
	
	/**
	 * 保持している HttpURLConnection の disconnect を呼び、
	 * InputStream, OutputStream を close() します。
	 * ですが、読み込み中のブロックはキャンセルされません。
	 */
	public void disconnect() {
		try {
			if (in != null) {
				in.close();
				in = null;
			}
			System.out.println("Rest#in closed");
		} catch (IOException ioe) {
			System.err.println("Rest#InputStream#close() failed " + ioe);
		}
		try {
			if (out != null) {
				out.close();
				out = null;
			}
			System.out.println("Rest#out closed");
		} catch (IOException ioe) {
			System.err.println("Rest#InputStream#close() failed " + ioe);
		}
		if (con != null) {
			con.disconnect(); // 最後にしてみた
			con = null; // ガベージコレクト対象にする
		}
	}
}

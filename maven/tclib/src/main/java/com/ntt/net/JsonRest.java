package com.ntt.net;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;
import abdom.data.json.JsonParseException;

import static java.net.HttpURLConnection.*;

/**
 * REST による要求を簡単に行うためのクラス。軽量化のため、外部ライブラリに
 * 非依存でつくる(java.net.HttpURLConnection ベース)
 * レスポンスボディが JSON でない場合(cometd関連でhtmlを返す場合がある)、
 * toString() ではレスポンスがそのまま返されます。
 *
 * @version	7, December 2017
 * @author	Yusuke Sasaki
 */
public class JsonRest {
	
	/** host を示す URL 文字列(http:// or https://) スラッシュは含めない */
	protected String urlStr;
	
	/** HEADER に指定する項目 */
	protected Map<String, String> header;
	
	/** HttpURLConnection */
	protected HttpURLConnection con;
	protected InputStream in;
	protected OutputStream out;
	
	//private byte[] buffer = new byte[4096];
	
	/**
	 * HTTP レスポンスを表す内部クラス
	 */
	public static class Response implements Jsonizable {
		/**
		 * HTTP レスポンスコードを返却します。
		 *
		 * @see		java.net.HttpURLConnection
		 */
		public int status;
		public String message;
		public byte[] body;
		
		/**
		 * 結果の body を String で取得します
		 * JSON 解析を行わず、body の内容そのままを UTF-8 形式で文字列化します。
		 * このため、body が HTML などの JSON でない形式になる場合があります。
		 * body が存在しない場合、文字列 "null" が返却されます。
		 *
		 * @return		body 文字列 (通常 JSON 形式ですが、RESTサーバからの
		 *				HTML エラー文字列等になる場合があります)
		 */
		@Override
		public String toString() {
			if (body == null) return "null";
			return new String(body, StandardCharsets.UTF_8);
		}
		
		/**
		 * pretty JSON 形式の文字列を取得します。
		 * body が JSON 形式でない場合、abdom.data.json.JsonParseException
		 * がスローされます。
		 *
		 * @return		pretty JSON 形式
		 * @exception	abdom.data.json.JsonParseException	body の JSON parse
		 *				に失敗した
		 */
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
		 * レスポンスは JsonType.parse を行っているため、エラー文字列等で
		 * あった場合、abdom.data.json.JsonParseExcception がスローされます。
		 *
		 * @return		pretty JSON 形式
		 * @exception	abdom.data.json.JsonParseException	body の JSON parse
		 *				に失敗した
		 */
		@Override
		public JsonType toJson() {
			if (body == null) return JsonType.NULL;
			String jsonStr = new String(body, StandardCharsets.UTF_8);
			JsonType result = JsonType.parse(jsonStr);
			return result;
		}
	}
	
/*-------------
 * Constructor
 */
	/**
	 * 指定された url を保持する Rest を作成します。
	 * header には、Content-Type: application/json,
	 * Accept: application/json が自動的に付加されます。
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
	 * REST で使用する接続先 URL を取得します。
	 *
	 * @return		接続先 URL
	 */
	public String getLocation() {
		return urlStr;
	}
	
	/**
	 * ヘッダを指定します。
	 * デフォルトで、"Content-Type"="application/json"
	 * "Accept"="application/json" が指定されていますが上書き可能です。
	 * value が null となる Header は出力されません。
	 *
	 * @param	key		header の key
	 * @param	value	header の value
	 */
	public synchronized void putHeader(String key, String value) {
		header.put(key, value);
	}
	
	public synchronized void removeHeader(String key) {
		header.remove(key);
	}
	
	public synchronized String getHeader(String key) {
		return header.get(key);
	}
	
	public synchronized Map<String, String> getHeaders() {
		return header;
	}
	
	//public void setHeader(Map<String, String> header) {
	//	this.header = header;
	//}
	
	/**
	 * GET リクエストをします。
	 *
	 * @param	resource	GETするリソース
	 * @return	Rest.Response オブジェクト
	 * @exception	java.io.IOException 通信異常、REST 異常など
	 */
	public Response get(String location) throws IOException {
		return request(location, "GET", null);
	}
	
	/**
	 * DELETE リクエストをします。
	 *
	 * @param	location	URL
	 * @exception	java.io.IOException 通信異常、REST 異常など
	 */
	public Response delete(String location) throws IOException {
		return request(location, "DELETE", null);
	}
	
	/**
	 * PUT リクエストをします。
	 *
	 * @param	location	URL
	 * @param	json		body
	 * @exception	java.io.IOException 通信異常、REST 異常など
	 */
	public Response put(String location, Jsonizable json)
							throws IOException {
		return put(location, json.toString());
	}
	
	/**
	 * PUT リクエストをします。
	 *
	 * @param	location	URL
	 * @param	body		body
	 * @exception	java.io.IOException 通信異常、REST 異常など
	 */
	public Response put(String location, String body)
							throws IOException {
		return request(location, "PUT", body.getBytes("UTF-8"));
	}
	
	/**
	 * PUT リクエストをします。
	 *
	 * @param	location	URL
	 * @param	body		body
	 * @exception	java.io.IOException 通信異常、REST 異常など
	 */
	public Response put(String location, byte[] body)
							throws IOException {
		return request(location, "PUT", body);
	}
	
	/**
	 * POST リクエストをします。
	 *
	 * @param	location	URL
	 * @param	json		body
	 * @exception	java.io.IOException 通信異常、REST 異常など
	 */
	public Response post(String location, Jsonizable json)
							throws IOException {
		if (json == null)
			return request(location, "POST", null);
		return post(location, json.toString());
	}
	
	/**
	 * POST リクエストをします。
	 *
	 * @param	location	URL
	 * @param	body		body
	 * @exception	java.io.IOException 通信異常、REST 異常など
	 */
	public Response post(String location, String body)
							throws IOException {
		if (body == null)
			return request(location, "POST", null);
		return request(location, "POST", body.getBytes("UTF-8"));
	}
	
	/**
	 * POST リクエストをします。
	 *
	 * @param	location	URL
	 * @param	body		body
	 * @exception	java.io.IOException 通信異常、REST 異常など
	 */
	public Response post(String location, byte[] body)
							throws IOException {
		if (body == null)
			return request(location, "POST", null);
		return request(location, "POST", body);
	}
	
	/**
	 * Httpリクエストの実処理を行います。
	 * 400 以上のステータスコードであっても例外は発生しません。
	 * GET や DELETE メソッドでは、header に Content-Type 指定があっても
	 * 設定しません。
	 *
	 * @param	location	リソースの場所 /platform 等
	 *						http:// https:// ではじまる場合、そのURLを使用します
	 * @param	method		GET/POST/PUT/DELETE
	 * @param	body		body に設定するデータ
	 * @return	Response
	 * @exception	java.io.IOException 通信異常、REST 異常など
	 */
	public synchronized Response request(String location, String method,
								byte[] body) throws IOException {
		URL url = null;
		location = convLocation(location);
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
				String value = header.get(key);
				if (value != null) con.setRequestProperty(key, header.get(key));
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
		resp.status = con.getResponseCode();
		resp.message = con.getResponseMessage();
		
		ByteArrayOutputStream baos =  new ByteArrayOutputStream();
		try {
			in = null;
			if (resp.status < 400) {
				// 400以降のエラーが返されると InputStream が取得できない
				in = con.getInputStream(); // may throw IOException
			} else {
				// ErrorStream で取得する
				in = con.getErrorStream(); // may null
			}
			while (true) {
				if (in == null) break;
				//int c = in.read(buffer); // なんか遅い
				int c = in.read(); // byte[] buff を使った方が早い
				if (c == -1) break;
				//baos.write(buffer, 0, c);
				baos.write(c);
			}
			if (in != null) in.close();
			baos.close();
			
			resp.body = baos.toByteArray();
			
		} catch (IOException ioe) {
			throw ioe;
		}
		
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
	
	/**
	 * 指定された文字列に + が含まれる場合、%2B に置換します。
	 *
	 * @param	target	変換対象文字列
	 */
	protected static String convLocation(String target) {
/*		try {
			int i = target.indexOf('?');
			if (i == -1) return target;
			
			StringBuilder sb = new StringBuilder();
			sb.append(target.substring(0, i+1));
			String[] kv = target.substring(i+1).split("&");
			for (int j = 0; j < kv.length; j++) {
				if (j > 0) sb.append('&');
				String s = kv[j];
				int ind = s.indexOf('=');
				if (ind == -1) {
					sb.append(s);
					continue;
				}
				sb.append(s.substring(0, ind+1));
				sb.append(URLEncoder.encode(s.substring(ind+1), "UTF-8"));
			}
			return sb.toString();
		} catch (UnsupportedEncodingException uee) {
			throw new InternalError("UTF-8 が利用できません");
		}*/
		return target.replace("+", "%2B");
	}
	
}

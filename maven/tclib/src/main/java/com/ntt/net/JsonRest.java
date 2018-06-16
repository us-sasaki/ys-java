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
 * json-stream に対応するため、結果を JsonType.parse(Reader) で構築する。
 * レスポンスボディが JSON でない場合(cometd関連でhtmlを返す場合がある)、
 * エラー文字列を表す JsonValue レスポンスが返却されることに注意して下さい。
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
		protected byte[] body;
		
		/**
		 * 結果の body を String で取得します
		 * JSON 解析を行わず、body の内容そのままを UTF-8 形式で出力します。
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
		 * @throw		abdom.data.json.JsonParseException	body の JSON parse
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
		 * あった場合、JsonParseExcception がスローされます。
		 *
		 * @return		pretty JSON 形式
		 * @throw		abdom.data.json.JsonParseException	body の JSON parse
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
	 * デフォルトで、"Content-Type"="application/json"
	 * "Accept"="application/json" が指定されていますが上書き可能です。
	 * value が null となる Header は出力されません。
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
		if (json == null)
			return requestImpl(location, "POST", null);
		return post(location, json.toString());
	}
	
	/**
	 * Content-Type を指定して POST リクエストをします。
	 */
	public Response post(String location, String body)
							throws IOException {
		if (body == null)
			return requestImpl(location, "POST", null);
		return requestImpl(location, "POST", body.getBytes("UTF-8"));
	}
	
	/**
	 * Httpリクエストの実処理を行います。
	 * Cumulocity 固有のヘッダを付加します。
	 *
	 * @param	location	リソースの場所 /platform 等
	 *						http:// https:// ではじまる場合、そのURLを使用します
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
		resp.code = con.getResponseCode();
		resp.message = con.getResponseMessage();
		
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
			while (true) {
				int c = in.read(); // byte[] buff を使った方が早い
				if (c == -1) break;
				baos.write(c);
			}
			in.close();
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
}

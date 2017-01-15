package com.ntt.tc.net;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import abdom.data.json.JsonArray;
import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;

import static java.net.HttpURLConnection.*;

/**
 * REST による要求を簡単に行うためのクラス。軽量化のため、外部ライブラリに
 * 非依存でつくる(java.net.HttpURLConnection ベース)
 * 試験利用状態だが、各種 REST API はたたけている。
 *
 * @version	29, November 2016
 * @author	Yusuke Sasaki
 */
public class Rest {
	
	/** host を示す URL 文字列(http:// or https://) */
	protected String urlStr;
	
	/** Cumulocity におけるテナント名 */
	protected String tenant;
	
	/** Cumulocity ユーザアカウント */
	protected String user;
	
	/** Cumulocity ユーザパスワード */
	protected String password;
	
	/** Cumulocity App-key */
	protected String appKey;
	
	/** Processing Mode */
	protected boolean modeIsTransient = false;
	
	/**
	 * HTTP レスポンスを表す内部クラス
	 */
	public static class Response {
		/**
		 * HTTP レスポンスコードを返却します。
		 *
		 * @see		java.net.HttpURLConnection
		 */
		public int code;
		public String message;
		//public String contentType;
		protected byte[] body;
		
		/**
		 * 結果の body を byte[] で取得します。
		 */
		public byte[] toByteArray() {
			return body;
		}
		/**
		 * 結果の body を String で取得します
		 */
		public String toString() {
			if (body == null) return null;
			try {
				return new String(body, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
				throw new InternalError("UTF-8 is not supported in this environment.");
			}
		}
		
		/**
		 * 結果の body を JsonType で取得します。
		 * エラーレスポンスに関する結果は不定で、通常 JsonParseExcception
		 * がスローされます。
		 */
		public JsonType toJson() {
			if (body == null) return null;
			return JsonType.parse(toString());
		}
	}
	
/*-------------
 * Constructor
 */
	/**
	 * 指定された host, user, password を保持する Rest を作成します。
	 * tenant は host に含まれているものとします。
	 */
	public Rest(String urlStr, String user, String password) {
		this.urlStr = urlStr;
		this.tenant = "";
		this.user = user;
		this.password = password;
	}
	
	public Rest(String urlStr, String tenant, String user, String password) {
		this.urlStr = urlStr;
		this.tenant = tenant + "/";
		this.user = user;
		this.password = password;
	}
	
/*---------------
 * class methods
 */
	/**
	 * https://nttcom.cumuloity.com
	 * に接続する、demouserアカウントでログインする新インスタンスを返却します。
	 */
	public static Rest getDefaultC8YInstance() {
		return new Rest("https://nttcom.cumulocity.com", "demouser", "demouser");
	}
	
/*------------------
 * instance methods
 */
	/**
	 * アプリケーションキーを設定します。
	 */
	public void setApplicationKey(String key) {
		this.appKey = key;
	}
	
	/**
	 * プロセッシングモードを設定します。
	 *
	 * @param	isTransient	TRANSIENTモード(DBに書き込まない)を利用するか
	 */
	public void setProcessingMode(boolean isTransient) {
		modeIsTransient = isTransient;
	}
	
	/**
	 * GET リクエストをします。
	 *
	 * @param	resource	GETするリソース
	 * @return	Rest.Response オブジェクト
	 */
	public Response get(String resource) throws IOException {
		return get(resource, "");
	}
	
	/**
	 * GET リクエストをします。
	 */
	public Response get(String location, String type) throws IOException {
		return requestImpl(location, "GET", type, null);
	}
	
	/**
	 * DELETE リクエストをします。
	 */
	public Response delete(String location) throws IOException {
		return delete(location, "");
	}
	
	/**
	 * DELETE リクエストをします。
	 */
	public Response delete(String location, String type) throws IOException {
		return requestImpl(location, "DELETE", type, null);
	}
	
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String resource, JsonType json)
							throws IOException {
		return put(resource, "", json);
	}
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String resource, String body)
							throws IOException {
		return put(resource, "", body);
	}
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String resource, String type, JsonType json)
							throws IOException {
		return put(resource, type, json.toString());
	}
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String resource, String type, String body)
							throws IOException {
		return requestImpl(resource, "PUT", type, body);
	}
	
	/**
	 * POST リクエストをします。
	 */
	public Response post(String location, JsonType json)
							throws IOException {
		return post(location, "", json.toString());
	}
	
	/**
	 * POST リクエストをします。
	 */
	public Response post(String location, String body)
							throws IOException {
		return post(location, "", body);
	}
	
	/**
	 * POST リクエストをします。
	 */
	public Response post(String location, String type, JsonType json)
							throws IOException {
		return post(location, type, json.toString());
	}
	
	/**
	 * POST リクエストをします。
	 */
	public Response post(String location, String type, String body)
							throws IOException {
		return requestImpl(location, "POST", type, body);
	}
	
	/**
	 * Httpリクエストの実処理を行います。
	 */
	private Response requestImpl(String location, String method)
							throws IOException {
		return requestImpl(location, method, "", null);
	}
	
	/**
	 * Httpリクエストの実処理を行います。
	 */
	private Response requestImpl(String location, String method, String type)
							throws IOException {
		return requestImpl(location, method, type, null);
	}
	
	/**
	 * Httpリクエストの実処理を行います。
	 */
	private Response requestImpl(String location, String method,
								String type, String body)
							throws IOException {
		if (body == null || body.equals("")) {
			return requestImpl(location, method, type, type, null);
		}
		return requestImpl(location, method, type, type, body.getBytes("UTF-8"));
	}
	
	/**
	 * Httpリクエストの実処理を行います。
	 * Cumulocity 固有のヘッダを付加します。
	 *
	 * @param	location	リソースの場所 /platform 等
	 * @param	method		GET/POST/PUT/DELETE
	 * @param	type		アプリケーションタイプ(platformApi等)
	 * @param	body		body に設定するデータ
	 */
	private Response requestImpl(String location, String method,
								String contentType, String accept,
								byte[] body) throws IOException {
		URL url = new URL(urlStr + location);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		// 出力設定
		boolean doOutput = (body != null && body.length > 0);
		if (doOutput) con.setDoOutput(true);
		
		// メソッド
		con.setRequestMethod(method);
		
		// Content-Type
		if ("".equals(contentType)) {
			con.setRequestProperty("Content-Type", "application/json");
		} else {
			con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity."+contentType+"+json; charset=UTF-8; ver=0.9");
		}
		// Accept
		if ("".equals(accept)) {
			con.setRequestProperty("Accept", "application/json");
		} else {
			con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity."+accept+"+json; charset=UTF-8; ver=0.9");
		}
		// X-Cumulocity-Application-Key
		if (appKey != null && !appKey.equals("")) {
			con.setRequestProperty("X-Cumulocity-Application-Key", appKey);
		}
		// X-Cumulocity-Processing-Mode
		if (modeIsTransient) {
			con.setRequestProperty("X-Cumulocity-Processing-Mode", "TRANSIENT");
		}
		
		
		// 基本認証
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		
		// 出力
		if (doOutput) {
			BufferedOutputStream bo = new BufferedOutputStream(con.getOutputStream());
			bo.write(body);
			bo.flush();
		}
		
		// 結果オブジェクトの生成
		Response resp = new Response();
		resp.code = con.getResponseCode();
		if (resp.code < 400) {
			
			ByteArrayOutputStream baos =  new ByteArrayOutputStream();
			BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
			for (;;) {
				int c = bis.read();
					if (c == -1) break;
				baos.write(c);
			}
			baos.close();
			bis.close();
			resp.body = baos.toByteArray();
		} else {
			resp.message = con.getResponseMessage();
		}
		con.disconnect();
		
		return resp;
	}
	
	/**
	 * バイナリデータをファイルをPOSTします
	 */
	public Response postBinary(String filename, String mimetype, byte[] data)
							throws IOException {
		
		// body を生成する
		String bry = "----boundary----13243546"+(long)(Math.random() * 1000000000)+"5789554----";
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		
		OutputStream out = new BufferedOutputStream(out2);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
		
		// object part
		pw.println("--"+bry); // multipartの改行コード CR+LF
		pw.println("Content-Disposition: form-data; name=\"object\"");
		pw.println();
		JsonType mo = JsonType.o("name", filename)
						.put("type", mimetype);
		pw.println(mo.toString("  "));
		
		// filesize part
		pw.println("--"+bry);
		pw.println("Content-Disposition: form-data; name=\"filesize\"");
		pw.println();
		pw.println(data.length);
		
		// file part
		pw.println("--"+bry);
		pw.println("Content-Disposition: form-data; name=\"file\"; filename=\""+filename+"\"");
		pw.println("Content-Type: application/octet-stream");
		pw.println("Content-Transfer-Encoding: binary");
		pw.println();
		pw.flush();
		
		// ファイル実体
		out.write(data);
		out.write("\r\n".getBytes());
		out.flush();
		pw.println("--"+bry+"--");
		pw.println();
		pw.flush();
		
		return requestImpl("/inventory/binaries", "POST", "multipart/form-data; boundary="+bry, "managedObject", out2.toByteArray());
	}
}

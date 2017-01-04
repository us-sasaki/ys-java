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
	 * レスポンスコンテント(JSON)が含まれるかをレスポンスコードで判定
	 * 実際は、要求ー応答　の関係で決まると思われる。
	 */
//	private boolean hasContent(int responseCode) {
//		if (responseCode < 400) return true;
//		return false;
//	}
	
	public Response get(String resource) throws IOException {
		return get(resource, "");
	}
	
	/**
	 * GET リクエストをします。
	 * この実装は、resource = /platform, type = platformApi と設定することを
	 * 想定しています。
	 */
	public Response get(String location, String type) throws IOException {
		return requestImpl(location, "GET", type, null);
	}
	
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String resource, JsonType json)
							throws IOException {
		return put(resource, "", json);
	}
	public Response put(String resource, String body)
							throws IOException {
		return put(resource, "", body);
	}
	public Response put(String resource, String type, JsonType json)
							throws IOException {
		return put(resource, type, json.toString());
	}
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
	
	public Response post(String location, String body)
							throws IOException {
		return post(location, "", body);
	}
	public Response post(String location, String type, JsonType json)
							throws IOException {
		return post(location, type, json.toString());
	}
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
		URL url = new URL(urlStr + location);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		// 出力設定
		boolean doOutput = (body != null && !body.equals(""));
		if (doOutput) con.setDoOutput(true);
		
		// メソッド
		con.setRequestMethod(method);
		
		// Content-Type, Accept
		if ("".equals(type)) {
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
		} else {
			con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
			con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
		}
		// 基本認証
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		
		// 出力
		if (doOutput) {
			BufferedOutputStream bo = new BufferedOutputStream(con.getOutputStream());
			bo.write(body.getBytes("UTF-8"));
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
	 * location は /inventory/binaries を想定。
	 */
	public Response postBinary(String filename, String mimetype, byte[] data)
							throws IOException {
		String location = "/inventory/binaries";
		URL url = new URL(urlStr + location);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
//		con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity.managedObject+json; charset=UTF-8; ver=0.9");
		con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.managedObject+json; charset=UTF-8");
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		String bry = "----boundary----132435465789554----";
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary="+bry);
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		
		OutputStream out = new BufferedOutputStream(out2); //con.getOutputStream());
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
		
		//System.out.println(new String(out2.toByteArray()));
		con.getOutputStream().write(out2.toByteArray());
		con.getOutputStream().flush();
		
		// 結果オブジェクトの生成
		Response resp = new Response();
		resp.code = con.getResponseCode();
		resp.message = con.getResponseMessage();
		
		//
		ByteArrayOutputStream baos =  new ByteArrayOutputStream();
		BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
		while (true) {
			int c = bis.read();
				if (c == -1) break;
			baos.write(c);
		}
		baos.close();
		bis.close();
		con.disconnect();
		
		resp.body = baos.toByteArray();
		
		return resp;
	}
	
}

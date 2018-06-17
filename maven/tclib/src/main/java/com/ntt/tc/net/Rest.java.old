package com.ntt.tc.net;

import java.io.*;
import java.net.*;
//import java.util.Base64; // since JDK1.8
import java.util.Date;
import java.util.Map;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import abdom.data.json.JsonArray;
import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.JsonObject;
import abdom.data.json.Jsonizable;
import abdom.data.json.JsonParseException;

import com.ntt.tc.util.Base64; // till JDK1.7

import static java.net.HttpURLConnection.*;

/**
 * REST による要求を簡単に行うためのクラス。軽量化のため、外部ライブラリに
 * 非依存でつくる(java.net.HttpURLConnection ベース)
 * json-stream に対応するため、結果を JsonType.parse(Reader) で構築する。
 * レスポンスボディが JSON でない場合(cometd関連でhtmlを返す場合がある)、
 * エラー文字列を表す JsonValue レスポンスが返却されることに注意して下さい。
 *
 * @version	21, November 2017
 * @author	Yusuke Sasaki
 */
public class Rest {
	
	/** host を示す URL 文字列(http:// or https://) */
	protected String urlStr;
	
	/** Cumulocity におけるテナント名(内部的に末尾に/を付加します) */
	protected String tenant;
	
	/** Cumulocity ユーザアカウント */
	protected String user;
	
	/** Cumulocity ユーザパスワード */
	protected String password;
	
	/** Cumulocity App-key */
	protected String appKey;
	
	/** Processing Mode */
	protected boolean modeIsTransient = false;
	
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
		 * 結果が JSON 文字列でないことがわかっている場合、
		 * このメソッドで文字列を取得可能。
		 * ただし、たまたま JSON 文字列になる場合、取得できない。
		 * body の型を String とすべきだが、暫定的にこの実装とする。
		 */
		public String getBody() {
			if (body == null) return null;
			if (!(body instanceof JsonValue)) return toString();
			if (body.getType() != JsonType.TYPE_STRING) return toString();
			String str = body.getValue();
			
			if (str.charAt(0) == '[') {
				int index = str.indexOf(':');
				if (index == -1) return "Rest.Response body format error.";
				int len = Integer.parseInt(str.substring(1, index));
				return str.substring(index+1, index+1 + len);
			}
			return str;
		}
		
		/**
		 * 結果の body を JsonType で取得します。
		 * エラーレスポンスに関する結果は不定で、通常 JsonParseExcception
		 * がスローされます。
		 */
		@Override
		public JsonType toJson() {
			if (body == null) return JsonType.NULL;
			return body;
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
		if (urlStr == null)
			throw new IllegalArgumentException("url が指定されていません");
		if (user == null)
			throw new IllegalArgumentException("user が指定されていません");
		if (password == null)
			throw new IllegalArgumentException("passuword が指定されていません");		this.urlStr = urlStr;
		this.tenant = "";
		this.user = user;
		this.password = password;
	}
	
	public Rest(String urlStr, String tenant, String user, String password) {
		if (urlStr == null)
			throw new IllegalArgumentException("url が指定されていません");
		if (tenant == null)
			throw new IllegalArgumentException("tenant が指定されていません");
		if (user == null)
			throw new IllegalArgumentException("user が指定されていません");
		if (password == null)
			throw new IllegalArgumentException("passuword が指定されていません");
		this.urlStr = urlStr;
		this.tenant = tenant + "/";
		this.user = user;
		this.password = password;
	}
	
	public Rest(Map<String, String> account) {
		this(account.get("url"),
				account.get("tenant"),
				account.get("user"),
				account.get("password"));
	}
	
/*---------------
 * class methods
 */
	/**
	 * https://management.iot-trialpack.com
	 * に接続する、demouserアカウントでログインする新インスタンスを返却します。
	 */
	public static Rest getDefaultC8YInstance() {
		return new Rest("https://management.iot-trialpack.com", "demouser", "demouser");
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
	 * REST に含まれるテナントを取得します。
	 *
	 * @return		テナント名
	 */
	public String getTenant() {
		if ("".equals(tenant)) {
			int c = urlStr.indexOf("://");
			int i = urlStr.indexOf('.');
			if (i == -1) throw new IllegalStateException("location にテナント名が含まれません:"+urlStr);
			if (c == -1) return urlStr.substring(0, i);
			return urlStr.substring(c+3, i);
		}
		return tenant;
	}
	
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
	 * GET リクエストを json-stream 形式で行います
	 */
	public Response getByStream(String location) throws IOException {
		return requestImpl(location, "GET", "", "application/json-stream", null);
	}
	
	/**
	 * GET リクエストを json-stream 形式で行います
	 */
	public Response getByStream(String location, String type) throws IOException {
		return requestImpl(location, "GET", type, "application/json-stream", null);
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
	public Response put(String resource, Jsonizable json)
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
	 * Content-Type を指定して PUT リクエストをします。
	 */
	public Response put(String resource, String type, Jsonizable json)
							throws IOException {
		return put(resource, type, json.toString());
	}
	/**
	 * Content-Type を指定して PUT リクエストをします。
	 */
	public Response put(String resource, String type, String body)
							throws IOException {
		return requestImpl(resource, "PUT", type, body);
	}
	
	/**
	 * POST リクエストをします。
	 */
	public Response post(String location, Jsonizable json)
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
	 * Content-Type を指定して POST リクエストをします。
	 */
	public Response post(String location, String type, Jsonizable json)
							throws IOException {
		return post(location, type, json.toString());
	}
	
	/**
	 * Content-Type を指定して POST リクエストをします。
	 */
	public Response post(String location, String type, String body)
							throws IOException {
		return requestImpl(location, "POST", type, body);
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
	 * 指定された文字列に + が含まれる場合、%2B に置換します。
	 */
	private static String convLocation(String target) {
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
	
	/**
	 * Httpリクエストの実処理を行います。Cumulocity 固有のヘッダを付加します。
	 * レスポンスコードが400台、500台のものはエラーと見なし、
	 * com.ntt.tc.net.C8yRestException がスローされます。
	 * ただし、404 Not Found は例外的に正常応答と見なします。
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
								String contentType, String accept,
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
		
		// Content-Type
		if (!"GET".equals(method) && !"DELETE".equals(method) ) {
			if ("".equals(contentType)) {
				con.setRequestProperty("Content-Type", "application/json");
			} else if (contentType.indexOf('/') == -1) {
				con.setRequestProperty("Content-Type",
						"application/vnd.com.nsn.cumulocity."+
						contentType+"+json; charset=UTF-8; ver=0.9");
			} else {
				con.setRequestProperty("Content-Type", contentType);
			}
		}
		// Accept
		if ("".equals(accept)) {
			con.setRequestProperty("Accept", "application/json");
		} else if (accept.indexOf('/') == -1) {
			con.setRequestProperty("Accept",
						"application/vnd.com.nsn.cumulocity."+
						accept+"+json; charset=UTF-8; ver=0.9");
		} else {
			con.setRequestProperty("Accept", accept);
		}
		// X-Cumulocity-Application-Key
		if (appKey != null && !appKey.equals("")) {
			//System.out.println("App Key call : " + appKey);
			con.setRequestProperty("X-Cumulocity-Application-Key", appKey);
		}
		// X-Cumulocity-Processing-Mode
		if (modeIsTransient) {
			con.setRequestProperty("X-Cumulocity-Processing-Mode", "TRANSIENT");
		}
		
		
		// 基本認証
		if (user != null && password != null) {
			String authStr = Base64.encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
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
				StringBuilder sb = new StringBuilder();
				while (true) {
					int c = r.read();
					if (c == -1) break;
					sb.append( (char)c );
				}
				r.close(); //bis.close();
				try {
					JsonType result = JsonType.parse(sb.toString());
					resp.body = result;
				} catch (JsonParseException jpe) {
					// このエラーメッセージの先頭部分[]は、getBody() でJSON
					// でない電文を取得するのに利用する場合があるため、
					// フォーマット変更時は両方変更すること
					//
					// 暫定実装であり、Response.body を String または byte[]
					// とするべき
					try {
						resp.body = new JsonValue("["+sb.length() + ":" +
											sb+"] is not JSON: " + jpe);
					} catch (Exception e) {
						resp.body = new JsonValue("not JSON: " + jpe + e);
					}
				}
				in.close();
			}
		} catch (IOException ioe) {
			throw ioe;
		}
		resp.message = con.getResponseMessage();
		//con.disconnect();		// 高速化に寄与する可能性があるためコメントアウト
		
		//
		// レスポンスコードの処理(404 Not Found は正常応答)
		//
		if (resp.code >= 400 && resp.code != 404)
			throw new C8yRestException(resp, location, method, contentType, accept);
		
		return resp;
	}
	
	/**
	 * バイナリデータのファイルをPOSTします
	 *
	 * @param	filename	object パートの name に指定されるファイル名
	 * @param	mimetype	object パートの type に指定されるMIME-Type
	 * @param	data		upload する binary データ
	 * @return	http レスポンス
	 */
	public synchronized Response postBinary(String filename, String mimetype, byte[] data)
							throws IOException {
		
		// body を生成する
		String bry = "----boundary----13243546"+(long)(Math.random() * 1000000000)+"5789554----";
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		
		OutputStream out = new BufferedOutputStream(out2);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
		
		final String CRLF = "\r\n"; // まだ使ってない
		
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
	
	/**
	 * multipart/form-data を利用してファイルを送信します。
	 * /cep/modules では 415 Unsupported Media Type が返却されます
	 * 修正し、/bulkNewDeviceRequests では upload が成功しました。
	 */
	public synchronized Response postMultipart(String endPoint, String filename, byte[] data)
									throws IOException {
		// body を生成する
		String bry = "----boundary----13243546"+(long)(Math.random() * 1000000000)+"5789554----";
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		
		OutputStream out = new BufferedOutputStream(out2);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
		
		final String CRLF = "\r\n";
		
		// file part
		pw.print("--"+bry+CRLF);
		pw.print("Content-Disposition: form-data; name=\"file\"; filename=\""+filename+"\""+CRLF);
		pw.print("Content-Type: text/plain"+CRLF);
//		pw.print("Content-Transfer-Encoding: binary"+CRLF);
		pw.print(CRLF);
		pw.flush();
		
		// ファイル実体
		out.write(data);
		out.write(CRLF.getBytes());
		out.flush();
		pw.print("--"+bry+"--"+CRLF);
		pw.print(CRLF);
		pw.flush();
		
		return requestImpl(endPoint, "POST", "multipart/form-data; boundary="+bry, "", out2.toByteArray());
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

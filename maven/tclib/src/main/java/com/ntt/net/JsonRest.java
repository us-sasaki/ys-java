package com.ntt.net;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import com.ntt.tc.util.Base64; // till JDK1.7

import abdom.data.json.JsonType;
import abdom.data.json.Jsonizable;


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
	
	protected boolean debugMode = false;
	
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
	 *
	 * @param	urlStr		接続先の URL https://host.domain.com/api など
	 */
	public JsonRest(String urlStr) {
		if (urlStr == null)
			throw new IllegalArgumentException("url が指定されていません");
		this.urlStr = urlStr;
		header = new HashMap<String, String>();
		header.put("Content-Type", "application/json");
		header.put("Accept", "application/json");
	}
	
	public JsonRest(Map<String, String> map) {
		String urlStr = map.get("host");
		if (urlStr == null)
			throw new IllegalArgumentException("host が設定されていません");
		String user = map.get("user");
		if (user == null)
			throw new IllegalArgumentException("user が設定されていません");
		String pass = map.get("pass");
		if (pass == null)
			throw new IllegalArgumentException("pass が設定されていません");
		
		this.urlStr = urlStr;
		header = new HashMap<String, String>();
		header.put("Content-Type", "application/json");
		header.put("Accept", "application/json");
		
		String authStr = Base64.encodeToString((user + ":" + pass).getBytes());
		header.put("Authorization", "Basic " + authStr);
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
	
	/**
	 * ヘッダを削除します。
	 *
	 * @param		key		削除するヘッダのキー
	 */
	public synchronized void removeHeader(String key) {
		header.remove(key);
	}
	
	/**
	 * ヘッダの値を取得します。
	 *
	 * @param		key		取得するヘッダのキー
	 * @return		ヘッダの値
	 */
	public synchronized String getHeader(String key) {
		return header.get(key);
	}
	
	/**
	 * ヘッダを格納する Map を取得します。
	 *
	 * @return		ヘッダを格納する Map
	 */
	public synchronized Map<String, String> getHeaders() {
		return header;
	}
	
	/**
	 * デバッグモード(証明書チェックを行わない)設定
	 *
	 * @param	debugMode		チェックしないモードにする
	 */
	public synchronized void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	
	/**
	 * GET リクエストをします。
	 *
	 * @param	location	GETするリソース
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
	 * @return	Rest.Response オブジェクト
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
	 * @return	Rest.Response オブジェクト
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
	 * @return	Rest.Response オブジェクト
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
	 * @return	Rest.Response オブジェクト
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
	 * @return	Rest.Response オブジェクト
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
	 * @return	Rest.Response オブジェクト
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
	 * @return	Rest.Response オブジェクト
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
		if (debugMode) setLooseCheck(con);
		
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
				if (value != null) {
					con.setRequestProperty(key, header.get(key));
//System.out.println("header "+key + "="+header.get(key));
				}
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
			if (in != null)
				try { in.close(); } catch (IOException ignored) { }
			if (out != null)
				try { out.close(); } catch (IOException ignored) { }
			baos.close();
			
			resp.body = baos.toByteArray();
			
		} catch (IOException ioe) {
			throw ioe;
		}
		
		return resp;
	}
	
	/**
	 * multipart/form-data を利用してファイルを送信します。
	 *
	 * @param		endPoint	POST を行う end point
	 * @param		filename	ファイル名
	 * @param		data		出力するバイナリ情報
	 * @return		Rest.Response オブジェクト
	 * @throws		java.io.IOException REST異常
	 */
	public synchronized Response postMultipart(
									String endPoint,
									String filename,
									byte[] data)
										throws IOException {
		return postMultipart(endPoint, filename, "text/plain", data);
	}
	
	/**
	 * multipart/form-data を利用してファイルを送信します。
	 *
	 * @param		endPoint	POST を行う end point
	 * @param		filename	ファイル名
	 * @param		contentType	Content-Type に指定する値(text/plainなど)
	 * @param		data		出力するバイナリ情報
	 * @return		Rest.Response オブジェクト
	 * @throws		java.io.IOException REST異常
	 */
	public synchronized Response postMultipart(
									String endPoint,
									String filename,
									String contentType,
									byte[] data)
										throws IOException {
		return requestMultipart(endPoint, "POST", filename, contentType, data);
	}
	
	/**
	 * multipart/form-data を利用してファイル(1つ)を送信します。
	 *
	 * @param		endPoint	request を行う end point
	 * @param		method		httpメソッド(POSTなど)
	 * @param		filename	ファイル名
	 * @param		contentType	Content-Type に指定する値(text/plainなど)
	 * @param		data		出力するバイナリ情報
	 * @return		Rest.Response オブジェクト
	 * @throws		java.io.IOException REST異常
	 */
	public synchronized Response requestMultipart(
									String endPoint,
									String method,
									String filename,
									String contentType,
									byte[] data)
										throws IOException {
		// body を生成する
		String bry = "----boundary----13243546"+(long)(Math.random() * 1000000000)+"5789554----";
		final String CRLF = "\r\n";
		
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(
								new OutputStreamWriter(out, "UTF-8"))) {
			
			// file part
			pw.print("--"+bry+CRLF);
			pw.print("Content-Disposition: form-data; name=\"file\"; filename=\""
						+filename+"\""+CRLF);
			pw.print("Content-Type: "+contentType+CRLF);
			pw.print(CRLF);
			pw.flush();
			
			// ファイル実体
			out.write(data);
			out.write(CRLF.getBytes());
			out.flush();
			pw.print("--"+bry+"--"+CRLF);
			pw.print(CRLF);
			pw.flush();
			
			String ct = getHeader("Content-Type");
			String ac = getHeader("Accept");
			putHeader("Content-Type","multipart/form-data; boundary="+bry);
			putHeader("Accept", "application/json");
			Response r = request(endPoint, method, out.toByteArray());
			if (ct == null) removeHeader("Content-Type");
			else putHeader("Content-Type", ct);
			if (ac == null) removeHeader("Accept");
			else putHeader("Accept", ac);
			
			return r;
		}
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
	 * @return	%2B に置換された文字列
	 */
	protected static String convLocation(String target) {
		return target.replace("+", "%2B");
		// return URLEncoder.encode(target, "UTF-8");
	}
	
	private void setLooseCheck(HttpURLConnection con) throws IOException {
		try {
			if (!(con instanceof HttpsURLConnection)) return;
			HttpsURLConnection c = (HttpsURLConnection)con;
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null,
							new X509TrustManager[] { new LooseTrustManager() },
							new SecureRandom());
			c.setSSLSocketFactory(sslContext.getSocketFactory());
			
			c.setHostnameVerifier(new LooseHostnameVerifier());
		} catch (java.security.GeneralSecurityException gse) {
			throw new IOException(gse);
		}
	}
	
	private static class LooseTrustManager implements X509TrustManager {
		@Override
    	public void checkClientTrusted(X509Certificate[] chain, String authType)
    					throws CertificateException { }
		
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
						throws CertificateException { }
		
		@Override
		public X509Certificate[] getAcceptedIssuers() { return null; }
	}
	
	private static class LooseHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	/**
	 * クライアント証明書、秘密鍵を keyStore ファイルにより設定します。
	 * 事前に keytool コマンドで keyStore ファイル、パスワードを設定する必要があります。
	 * 処理内容は System プロパティの javax.net.ssl.keyStore,
	 * javax.net.ssl.keyStorePassword への設定のみです。
	 * 動作未確認。(2024/7/5)
	 * @param keyStoreFile myClientCert.p12 のような keyStore ファイル名
	 * @param keyStorePass keyStore のパスワード
	 */
	static void setClientAuth(String keyStoreFile, String keyStorePass) {
		System.setProperty("javax.net.ssl.keyStore", keyStoreFile);
		System.setProperty("javax.net.ssl.keyStorePassword", keyStorePass);
	}
}

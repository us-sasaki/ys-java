package com.ntt.tc.net;

import java.io.*;
import java.net.*;
//import java.util.Base64; // from JDK1.8
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

import com.ntt.net.JsonRest;
import com.ntt.tc.util.Base64; // till JDK1.7

import static java.net.HttpURLConnection.*;

/**
 * REST による要求を簡単に行うためのクラス。軽量化のため、外部ライブラリに
 * 非依存でつくる(java.net.HttpURLConnection ベース)
 * json-stream に対応するため、結果を JsonType.parse(Reader) で構築する。
 * レスポンスボディが JSON でない場合(cometd関連でhtmlを返す場合がある)、
 * エラー文字列を表すレスポンスが返却されることに注意して下さい。
 * このオブジェクトはスレッドセーフではありません。
 * ヘッダ設定処理部分がマルチスレッド対応になっていません。
 *
 * @version	16, June 2018
 * @author	Yusuke Sasaki
 */
public class Rest {
	
	/** JsonRest の一般的な処理を行うオブジェクト(委譲先) */
	protected JsonRest r;
	
	/** Cumulocity におけるテナント名(内部的に末尾に/を付加します) */
	protected String tenant;
	
	/** Cumulocity ユーザアカウント */
	protected String user;
	
	/** Cumulocity ユーザパスワード */
	protected String password;
	
	/**
	 * HTTP レスポンスを表す内部クラス
	 */
	public static class Response extends JsonRest.Response {
		public Response(JsonRest.Response src) {
			this.status = src.status;
			this.message = src.message;
			this.body = src.body;
		}
	}
	
/*-------------
 * Constructor
 */
	/**
	 * 指定された host, user, password を保持する Rest を作成します。
	 * tenant は host に含まれているものとします。
	 *
	 * @param		urlStr		"https://tenant.domain.com" の形式
	 * @param		user		ユーザ名
	 * @param		password	パスワード
	 */
	public Rest(String urlStr, String user, String password) {
		r = new JsonRest(urlStr);
		if (user == null)
			throw new IllegalArgumentException("user が指定されていません");
		if (password == null)
			throw new IllegalArgumentException("passuword が指定されていません");
		this.tenant = "";
		this.user = user;
		this.password = password;
		setAuthentication();
	}
	
	/**
	 * 指定された host, user, password を保持する Rest を作成します。
	 * tenant は host に含まれているものとします。
	 *
	 * @param		urlStr		"https://tenant.domain.com" の形式
	 * @param		tenant		テナント名
	 * @param		user		ユーザ名
	 * @param		password	パスワード
	 */
	public Rest(String urlStr, String tenant, String user, String password) {
		r = new JsonRest(urlStr);
		if (tenant == null)
			throw new IllegalArgumentException("tenant が指定されていません");
		if (user == null)
			throw new IllegalArgumentException("user が指定されていません");
		if (password == null)
			throw new IllegalArgumentException("passuword が指定されていません");
		if (tenant.endsWith("/")) // スラッシュ除去
			tenant = tenant.substring(0, tenant.length()-1);
		if (!tenant.equals("")) tenant = tenant + "/";
		this.tenant = tenant;
		this.user = user;
		this.password = password;
		setAuthentication();
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
	 * アクセス先の URL 文字列を取得します。
	 *
	 * @return		"https://tenant.domain.com" の形式の文字列
	 */
	public String getLocation() {
		return r.getLocation();
	}
	
	/**
	 * Authentication ヘッダを指定します。
	 */
	private void setAuthentication() {
		// 基本認証
		if (user != null && password != null) {
			String authStr = Base64.encodeToString((tenant + user + ":" + password).getBytes());
			r.putHeader("Authorization", "Basic " + authStr);
		}
	}
	
	/**
	 * header に指定された contentType を設定します。
	 * / が入っていない場合、application/vnd.com.nsn.cumulocity.{}+json;
	 * charset=UTF-8; ver=0.9 を付加します。
	 * / がある場合(application/json 等)、そのまま指定します。
	 * 空文字列の場合、application/json を指定します。
	 *
	 * @param		contentType		Content-Type に指定する文字列
	 */
	private void setContentType(String contentType) {
		// Content-Type
		if ("".equals(contentType)) {
			r.putHeader("Content-Type", "application/json");
		} else if (contentType.indexOf('/') == -1) {
			r.putHeader("Content-Type",
					"application/vnd.com.nsn.cumulocity."+
					contentType+"+json; charset=UTF-8; ver=0.9");
		} else {
			r.putHeader("Content-Type", contentType);
		}
	}
	
	/**
	 * header に指定された accept を設定します。
	 * / が入っていない場合、application/vnd.com.nsn.cumulocity.{}+json;
	 * charset=UTF-8; ver=0.9 を付加します。
	 * / がある場合(application/json 等)、そのまま指定します。
	 * 空文字列の場合、application/json を指定します。
	 *
	 * @param		accept		Accept に指定する文字列
	 */
	private void setAccept(String accept) {
		// Accept
		if ("".equals(accept)) {
			r.putHeader("Accept", "application/json");
		} else if (accept.indexOf('/') == -1) {
			r.putHeader("Accept",
						"application/vnd.com.nsn.cumulocity."+
						accept+"+json; charset=UTF-8; ver=0.9");
		} else {
			r.putHeader("Accept", accept);
		}
	}
	
	/**
	 * REST に含まれるテナントを取得します。
	 * 設定された tenant 名、または URL のホスト名から取得されます。
	 *
	 * @return		テナント名(末尾に "/" がつきます)
	 */
	public String getTenant() {
		if ("".equals(tenant)) {
			String t = r.getLocation();
			int c = t.indexOf("://");
			int i = t.indexOf('.');
			if (i == -1) throw new IllegalStateException("location にテナント名が含まれません:"+t);
			if (c == -1) return t.substring(0, i)+"/";
			return t.substring(c+3, i)+"/";
		}
		return tenant;
	}
	
	/**
	 * アプリケーションキーを設定します。
	 */
	public void setApplicationKey(String key) {
		if (key == null) {
			r.removeHeader("X-Cumulocity-Application-Key");
		} else {
			r.putHeader("X-Cumulocity-Application-Key", key);
		}
	}
	
	/**
	 * プロセッシングモードを設定します。
	 *
	 * @param	isTransient	TRANSIENTモード(DBに書き込まない)を利用するか
	 */
	public void setProcessingMode(boolean isTransient) {
		if (isTransient) {
			r.putHeader("X-Cumulocity-Processing-Mode", "TRANSIENT");
		} else {
			r.removeHeader("X-Cumulocity-Processing-Mode");
		}
	}
	
	/**
	 * ヘッダを設定します。
	 *
	 * @param		key		ヘッダの key
	 * @param		value	ヘッダの value
	 */
	public void putHeader(String key, String value) {
		r.putHeader(key, value);
	}
	
	/**
	 * ヘッダを削除します。
	 *
	 * @param		key		ヘッダの key
	 */
	public void removeHeader(String key) {
		r.removeHeader(key);
	}
	
	/**
	 * ヘッダ設定を示す Map オブジェクトを取得します。
	 * このオブジェクトは内部的に利用されているヘッダ情報のため、
	 * 値を変更した場合、本オブジェクトで設定するヘッダ情報が変更されます。
	 *
	 * @return		header の一覧(java.util.Map)
	 */
	public Map<String, String> getHeaders() {
		return r.getHeaders();
	}
	
	/**
	 * GET リクエストをします。
	 *
	 * @param	location	GETするリソース
	 * @return	Rest.Response オブジェクト
	 */
	public Response get(String location) throws IOException {
		setAccept("");
		return requestImpl(location, "GET");
		//return requestImpl(location, "GET", "", null);
	}
	
	/**
	 * GET リクエストをします。
	 */
	public Response get(String location, String type)
													throws IOException {
		setAccept(type);
		return requestImpl(location, "GET");
		//return requestImpl(location, "GET", type, null);
	}
	
	/**
	 * GET リクエストを json-stream 形式で行います
	 */
	public Response getByStream(String location)
													throws IOException {
		setAccept("application/json-stream");
		return requestImpl(location, "GET");
		//return requestImpl(location, "GET", "", "application/json-stream", null);
	}
	
	/**
	 * DELETE リクエストをします。
	 */
	public Response delete(String location) throws IOException {
		setAccept("");
		return requestImpl(location, "DELETE");
		//return requestImpl(location, "DELETE", "", null);
	}
	
	/**
	 * DELETE リクエストをします。
	 */
	public Response delete(String location, String type)
													throws IOException {
		setAccept(type);
		return requestImpl(location, "DELETE");
		//return requestImpl(location, "DELETE", type, null);
	}
	
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String location, Jsonizable json)
													throws IOException {
		setContentType("");
		setAccept("");
		return requestImpl(location, "PUT", json);
		//return requestImpl(location, "PUT", "", json);
	}
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String location, String body)
							throws IOException {
		setContentType("");
		setAccept("");
		return requestImpl(location, "PUT", body);
		//return requestImpl(location, "PUT", "", body);
	}
	/**
	 * Content-Type を指定して PUT リクエストをします。
	 */
	public Response put(String location, String type, Jsonizable json)
							throws IOException {
		setContentType(type);
		setAccept(type);
		return requestImpl(location, "PUT", json);
		//return requestImpl(location, "PUT", type, json.toString());
	}
	/**
	 * Content-Type を指定して PUT リクエストをします。
	 */
	public Response put(String location, String type, String body)
							throws IOException {
		setContentType(type);
		setAccept(type);
		return requestImpl(location, "PUT", body);
		//return requestImpl(location, "PUT", type, body);
	}
	
	/**
	 * POST リクエストをします。
	 */
	public Response post(String location, Jsonizable json)
							throws IOException {
		setContentType("");
		setAccept("");
		return requestImpl(location, "POST", json);
		//return requestImpl(location, "POST", "", json.toString());
	}
	
	/**
	 * POST リクエストをします。
	 */
	public Response post(String location, String body)
							throws IOException {
		setContentType("");
		setAccept("");
		return requestImpl(location, "POST", body);
		//return requestImpl(location, "POST", "", body);
	}
	
	/**
	 * Content-Type を指定して POST リクエストをします。
	 */
	public Response post(String location, String type, Jsonizable json)
							throws IOException {
		setContentType(type);
		setAccept(type);
		return requestImpl(location, "POST", json);
		//return requestImpl(location, "POST", type, json.toString());
	}
	
	/**
	 * Content-Type を指定して POST リクエストをします。
	 */
	public Response post(String location, String type, String body)
							throws IOException {
		setContentType(type);
		setAccept(type);
		return requestImpl(location, "POST", body);
		//return requestImpl(location, "POST", type, body);
	}
	
	/**
	 * Httpリクエストの実処理を行います。
	 */
	private Response request(String location, String method, String body)
							throws IOException {
		if (body == null || body.equals("")) {
			return requestImpl(location, method);
		}
		return requestImpl(location, method, body.getBytes("UTF-8"));
	}
	
	/**
	 * API#readModuleText 向けの API
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
	public Response request(String location, String method,
								String contentType, String accept,
								byte[] body) throws IOException {
		setContentType(contentType);
		setAccept(accept);
		return requestImpl(location, method, body);
	}
	
	protected Response requestImpl(String location, String method)
												throws IOException {
		return requestImpl(location, method, (byte[])null);
	}
	
	protected Response requestImpl(String location, String method,
									Jsonizable json) throws IOException {
		return requestImpl(location, method, json.toString());
	}
	
	protected Response requestImpl(String location, String method,
									String body) throws IOException {
		return requestImpl(location, method, body.getBytes("UTF-8"));
	}
	
	/**
	 * Httpリクエストの実処理を行います。Cumulocity 固有のヘッダを付加します。
	 * レスポンスコードが400台、500台のものはエラーと見なし、
	 * com.ntt.tc.net.C8yRestException がスローされます。
	 * ただし、404 Not Found は例外的に正常応答と見なします。
	 *
	 * @param	location	リソースの場所 /platform 等
	 * @param	method		GET/POST/PUT/DELETE
	 * @param	body		body に設定するデータ
	 */
	protected Response requestImpl(String location, String method, byte[] body)
											throws IOException {
		JsonRest.Response resp = r.request(location, method, body);
		Response result = new Response(resp);
		//
		// レスポンスコードの処理(404 Not Found は正常応答)
		//
		if (resp.status >= 400 && resp.status != 404)
			throw new C8yRestException(result, location, method, r.getHeader("Content-Type"), r.getHeader("Accept"));
		
		return result;
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
		
		setContentType("multipart/form-data; boundary="+bry);
		setAccept("managedObject");
		
		return requestImpl("/inventory/binaries", "POST", out2.toByteArray());
	}
	
	/**
	 * multipart/form-data を利用してファイルを送信します。
	 * /cep/modules では 415 Unsupported Media Type が返却されます
	 * 修正し、/bulkNewDeviceRequests では upload が成功しました。
	 *
	 * @param		endPoint	POST を行う end point
	 * @param		filename	ファイル名
	 * @param		data		出力するバイナリ情報
	 * @return		response
	 */
	public synchronized Response postMultipart(
									String endPoint,
									String filename,
									byte[] data)
										throws IOException {
		return postMultipart(endPoint, filename, "text/plain", data);
	}
	
	/**
	 * multipart/form-data を利用してファイル(1つ)を送信します。
	 *
	 * @param		endPoint	POST を行う end point
	 * @param		filename	ファイル名
	 * @param		contentType	Content-Type に指定する値(text/plainなど)
	 * @param		data		出力するバイナリ情報
	 * @return		response
	 */
	public synchronized Response postMultipart(
									String endPoint,
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
			
			setContentType("multipart/form-data; boundary="+bry);
			setAccept("");
			return requestImpl(endPoint, "POST", out.toByteArray());
		}
	}
	
	public void disconnect() {
		r.disconnect();
	}
}

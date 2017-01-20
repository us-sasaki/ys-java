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
 * REST による要求を簡単に行うためのクラス
 * GET /platform は成功
 * POST は未検証
 *
 * @version	20, October 2016
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
			} catch (NullPointerException npe) {
				return code + message;
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
	 * に接続する、佐々木アカウントでログインする新インスタンスを返却します。
	 */
	public static Rest getDefaultC8YInstance() {
		return new Rest("https://nttcom.cumulocity.com", "us.sasaki@ntt.com", "nttcomsasaki3");
	}
	
/*------------------
 * instance methods
 */
	/**
	 *
	 */
	private boolean hasContent(int responseCode) {
		switch (responseCode) {
		case HTTP_OK:
		case HTTP_CREATED:
			return true;
		}
		return false;
	}
	
	public Response get(String resource) throws IOException {
		return get(resource, "");
	}
	/**
	 * GET リクエストをします。
	 * この実装は、resource = /platform, type = platformApi と設定することを
	 * 想定しています。
	 */
	public Response get(String resource, String type) throws IOException {
		URL url = null;
		if (resource.startsWith("http://") ||
					resource.startsWith("https://")) {
			url = new URL(resource);
		} else {
			url = new URL(urlStr + resource);
		}
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		if ("".equals(type)) {
			con.setRequestProperty("Accept", "application/json");
		} else {
			con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity." + type + "+json;charset=UTF-8;ver=0.9");
		}
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		con.connect();
		
		// 結果オブジェクトの生成
		Response resp = new Response();
		resp.code = con.getResponseCode();
		
		if (hasContent(resp.code)) {
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
		} else {
			resp.message = con.getResponseMessage();
		}
		
		return resp;
		
	}
	
	public Response post(String resource, JsonType json) throws IOException {
		return post(resource, "", json);
	}
	public Response post(String resource, String body) throws IOException {
		return post(resource, "", body);
	}
	/**
	 * POST リクエストをします。
	 */
	public Response post(String resource, String type, JsonType json)
							throws IOException {
		return post(resource, type, json.toString());
	}
	public Response post(String resource, String type, String body)
							throws IOException {
		URL url = null;
		if (resource.startsWith("http://") ||
					resource.startsWith("https://")) {
			url = new URL(resource);
		} else {
			url = new URL(urlStr + resource);
		}
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		if ("".equals(type)) {
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8; ver=0.9");
			con.setRequestProperty("Accept", "application/json; charset=UTF-8; ver=0.9");
		} else {
			con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
			con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
		}
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
		bw.write(body);
		bw.flush();
		
		// 結果オブジェクトの生成
		Response resp = new Response();
		resp.code = con.getResponseCode();
		
		if (hasContent(resp.code)) {
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
		} else {
			resp.message = con.getResponseMessage();
		}
		
		return resp;
	}
	
	/**
	 * PUT リクエストをします。
	 */
	public Response put(String resource, String type, JsonType json)
							throws IOException {
		return put(resource, type, json.toString());
	}
	public Response put(String resource, String type, String body)
							throws IOException {
		URL url = null;
		if (resource.startsWith("http://") ||
					resource.startsWith("https://")) {
			url = new URL(resource);
		} else {
			url = new URL(urlStr + resource);
		}
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("PUT");
		
		con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
		con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity." + type + "+json;charset=UTF-8;ver=0.9");
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		con.connect();
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
		bw.write(body);
		bw.close();
		
		// 結果オブジェクトの生成
		Response resp = new Response();
		resp.code = con.getResponseCode();
		
		if (hasContent(resp.code)) {
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
		} else {
			resp.message = con.getResponseMessage();
		}
		
		return resp;
	}
	
/*--------------
 * main(sample)
 */
	private static void printResp(Response resp) {
		if (resp.body == null) System.out.println(resp.code + ":" + resp.message);
		else System.out.println(resp.toJson());
	}
	public static void main(String[] args) throws Exception {
		// Step 0 デバイスクレデンシャル要求
		System.out.println("------ Step 0 ------");
		
		Rest r = new Rest("https://nttcom.cumulocity.com", "management", "devicebootstrap", "Fhdt1bb1f"); //"us.sasaki@ntt.com", "nttcomsasaki3");
		JsonObject jo = new JsonObject().add("id", "ysdev000001");
		Response resp = r.post("/devicecontrol/deviceCredentials", "deviceCredentials", jo);
		printResp(resp);
		
		// デバイスクレデンシャルは無視して、defaultInstance で各ステップを処理
		// Step 1
		System.out.println("------ Step 1 ------");
		r = Rest.getDefaultC8YInstance();
/*		resp = r.get("/identity/externalIds/c8y_Serial/VAIO-5102173", "externalId");
		printResp(resp);
		
		// Step 2
		System.out.println("------ Step 2 ------");
		jo = new JsonObject();
		jo.add("name", "VAIO YS's 5102173")
			.add("type", "YSAP")
			.add("c8y_IsDevice", new JsonObject())
			.add("c8y_Hardware",
						new JsonObject().add("serialNumber", "5102173")
										.add("CPU", "Core i5") )
			.add("c8y_Software",
						new JsonObject().add("virtual-driver", "vd-0.9") )
			.add("c8y_Configuration",
						new JsonObject().add("config", "on the YS Desk") );
			
		resp = r.post("/inventory/managedObjects", "managedObject", jo);
		printResp(resp);
		
		// Step 3
		System.out.println("------ Step 3 ------");
		jo = new JsonObject();
		jo.add("type", "c8y_Serial")
			.add("externalId","VAIO-Serial-5102173");
			
		resp = r.post("/identity/globalIds/12244450/externalIds", "externalId", jo);
		printResp(resp);
		
		// Step 4
		System.out.println("------ Step 4 ------");
		jo = new JsonObject();
		jo.add("c8y_Software", new JsonObject().add("virtual-driver", "vd-1.0"));
		resp = r.put("/inventory/managedObjects/12244450", "managedObject", jo.toString());
		
		printResp(resp);
		
		// Step 5
		System.out.println("------ Step 5 ------");
		
		jo = new JsonObject();
		jo.add("managedObject", new JsonObject().add("self", "https://nttcom.cumulocity.com/inventory/managedObjects/9941768"));
		resp = r.post("/inventory/managedObjects/12244450/childDevices", "managedObjectReference", jo);
		
		printResp(resp);
*/		
		// Step 6
		System.out.println("------ Step 6 ------");
		
		resp = r.get("/devicecontrol/operations?agentId=12244450&status=EXECUTING", "operationCollection");
		
		printResp(resp);
		
		// Bayeux (exception occurs because this device has no operation allowed)
		JsonType j = resp.toJson();
		if (j.get("operations").size() == 0) {
			System.out.println("no operations left");
		}
		
		jo = new JsonObject();
		jo.add("id", "1");
		jo.add("supportedConnectionTypes", new JsonArray(new String[] {"long-polling"}));
		jo.add("channel", "/meta/handshake");
		jo.add("version", "1.0");
		
		System.out.println(new JsonArray(new JsonType[] {jo}));
		resp = r.post("/devicecontrol/notifications", "", new JsonArray(new JsonType[] {jo}));
		printResp(resp);
		
		
		
		// Step 7
		
//		resp = r.get("/platform", "platformApi");
//		System.out.println(resp.toJson());
	}
}


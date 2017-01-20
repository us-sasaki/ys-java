import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import abdom.data.json.*;

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
	
/*-------------
 * Constructor
 */
	/**
	 * 指定された host, tenant, user, password を保持する Rest を作成します。
	 */
	public Rest(String urlStr, String tenant, String user, String password) {
		this.urlStr = urlStr;
		this.tenant = tenant;
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
		return new Rest("https://nttcom.cumulocity.com", "nttcom", "us.sasaki@ntt.com", "nttcomsasaki3");
	}
	
/*------------------
 * instance methods
 */
	/**
	 * GET リクエストをします。
	 * この実装は、resource = /platform, type = platformApi と設定することを
	 * 想定しています。
	 */
	public void get(String resource, String type) throws IOException {
		URL url = new URL(urlStr + resource);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		
		String authStr = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
System.out.println("Auth Str " + authStr);
		con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity." + type + "+json;charset=UTF-8;ver=0.9");
		con.setRequestProperty("Authorization", "Basic " + authStr);
		con.connect();
		
		int res = con.getResponseCode();
		switch (res) {
		case HTTP_OK: // static imported (HttpURLConnection.HTTP_OK)
			InputStreamReader r = new InputStreamReader(con.getInputStream(), "UTF-8");
			BufferedReader br = new BufferedReader(r);
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			break;
			
		default:
			System.out.println("Response : " + res);
			System.out.println("Message  : " + con.getResponseMessage());
		}
		con.disconnect();
	}
	
	/**
	 * POST リクエストをします。
	 */
	public void post(String location, String type, String body)
							throws IOException {
		URL url = new URL(urlStr + location);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		String authStr = Base64.getEncoder().encodeToString((tenant + "/" + user + ":" + password).getBytes());
		con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
		con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
		con.setRequestProperty("Authorization", "Basic " + authStr);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
		bw.write(body);
		bw.flush();
		
		int res = con.getResponseCode();
		switch (res) {
		case HTTP_OK:
		case HTTP_CREATED:
			InputStreamReader r = new InputStreamReader(con.getInputStream(), "UTF-8");
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(r);
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				sb.append(line);
			}
			System.out.println("--- formatted JSON ---");
			try {
				JsonType.setIndent(true);
				System.out.println(JsonType.parse(sb.toString()));
			} catch (Exception e) {
				System.out.println(e);
			}
			break;
		default:
			System.out.println("Response : " + res);
			System.out.println("Message  : " + con.getResponseMessage());
		}
		
		con.disconnect();
	}
	
/*
 * main
 */
	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		float temp = ((float)(int)((Math.random() * 20.0 + 15.0)*100))/100;
		JsonType.setIndent(false);
		JsonType jt = JsonType.parse("{\"c8y_TemperatureMeasurement\":{\"T\":{\"value\":"+temp+",\"unit\":\"C\"}},\"time\":\""+sdf.format(new Date())+"\",\"source\":{\"id\":\"10899431\"},\"type\":\"c8y_PTCMeasurement\"}");
/*		jt.add("c8y_Position", new JsonObject()
								.add("alt",30)
								.add("lat",35.671251)
								.add("lng", 139.757461)
								.add("trackingProtocol","TELIC")
								.add("reportReason","Time Event"));
*/
		Rest r = Rest.getDefaultC8YInstance();
//		Rest r = new Rest("http://localhost:8080", "tenant", "us.sasaki@ntt.com", "nttcomsasaki3");
		r.get("/platform", "platformApi");
//		r.post("/measurement/measurements", jt.toString());
		
	}
}


import java.io.*;
import java.net.*;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import static java.net.HttpURLConnection.*;

/**
 * REST による要求を簡単に行うためのクラス
 * GET /platform は成功
 * POST は未検証
 *
 * @version	19, October 2016
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
	 * GET を使用したリクエストをします。
	 * この実装は、/platformAPI 決め打ちになっています。
	 */
	public void get(String resource) throws IOException {
		URL url = new URL(urlStr + resource);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		
		String authStr = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
System.out.println("Auth Str " + authStr);
		con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.platformApi+json;charset=UTF-8;ver=0.9");
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
	
	public void post(String location, String body) throws IOException {
		URL url = new URL(urlStr + location);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		String authStr = Base64.getEncoder().encodeToString((tenant + "/" + user + ":" + password).getBytes());
		con.setRequestProperty("Authorization", "Basic " + authStr);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
		bw.write(body);
		bw.flush();
		
		int res = con.getResponseCode();
		switch (res) {
		case HTTP_OK:
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
	
/*
 * main
 */
	public static void main(String[] args) throws Exception {
//		Rest r = new Rest("http://localhost:8080", "tenant", "us.sasaki@ntt.com", "nttcomsasaki3");
		Rest r = Rest.getDefaultC8YInstance();
		r.get("/platform");
	}
}


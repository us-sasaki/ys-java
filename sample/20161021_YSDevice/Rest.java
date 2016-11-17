import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import abdom.data.json.*;

import static java.net.HttpURLConnection.*;

/**
 * REST �ɂ��v�����ȒP�ɍs�����߂̃N���X
 * GET /platform �͐���
 * POST �͖�����
 *
 * @version	20, October 2016
 * @author	Yusuke Sasaki
 */
public class Rest {
	
	/** host ������ URL ������(http:// or https://) */
	protected String urlStr;
	
	/** Cumulocity �ɂ�����e�i���g�� */
	protected String tenant;
	
	/** Cumulocity ���[�U�A�J�E���g */
	protected String user;
	
	/** Cumulocity ���[�U�p�X���[�h */
	protected String password;
	
	/**
	 * HTTP ���X�|���X��\�������N���X
	 */
	public static class Response {
		public int responseCode;
		public String contentType;
		protected byte[] responseBody;
		
		public byte[] toByteArray() {
			return responseBody;
		}
//		public String toString() {
//			return new String(responseBody, "UTF-8");
//		}
		public JsonType toJson() {
			return JsonType.parse(toString());
		}
	}
	
/*-------------
 * Constructor
 */
	/**
	 * �w�肳�ꂽ host, user, password ��ێ����� Rest ���쐬���܂��B
	 * tenant �� host �Ɋ܂܂�Ă�����̂Ƃ��܂��B
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
	 * �ɐڑ�����A���X�؃A�J�E���g�Ń��O�C������V�C���X�^���X��ԋp���܂��B
	 */
	public static Rest getDefaultC8YInstance() {
		return new Rest("https://nttcom.cumulocity.com", "us.sasaki@ntt.com", "nttcomsasaki3");
	}
	
/*------------------
 * instance methods
 */
	/**
	 * GET ���N�G�X�g�����܂��B
	 * ���̎����́Aresource = /platform, type = platformApi �Ɛݒ肷�邱�Ƃ�
	 * �z�肵�Ă��܂��B
	 */
	public void get(String resource, String type) throws IOException {
		URL url = new URL(urlStr + resource);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		
		con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity." + type + "+json;charset=UTF-8;ver=0.9");
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
System.out.println("Auth Str " + authStr);
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
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
	 * POST ���N�G�X�g�����܂��B
	 */
	public void post(String location, String type, String body)
							throws IOException {
		URL url = new URL(urlStr + location);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
		con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		
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
	
	/**
	 * PUT ���N�G�X�g�����܂��B
	 */
	public void put(String resource, String type, String body)
							throws IOException {
		URL url = new URL(urlStr + resource);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("PUT");
		
		con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity."+type+"+json; charset=UTF-8; ver=0.9");
		con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity." + type + "+json;charset=UTF-8;ver=0.9");
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
System.out.println("Auth Str " + authStr);
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		con.connect();
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
		bw.write(body);
		bw.close();
		
		int res = con.getResponseCode();
		switch (res) {
		case HTTP_OK: // static-imported (HttpURLConnection.HTTP_OK)
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
 * main(sample)
 */
	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	}
}


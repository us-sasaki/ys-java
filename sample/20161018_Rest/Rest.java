import java.io.*;
import java.net.*;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import static java.net.HttpURLConnection.*;

/**
 * REST �ɂ��v�����ȒP�ɍs�����߂̃N���X
 * GET /platform �͐���
 * POST �͖�����
 *
 * @version	19, October 2016
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
	
/*-------------
 * Constructor
 */
	/**
	 * �w�肳�ꂽ host, tenant, user, password ��ێ����� Rest ���쐬���܂��B
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
	 * �ɐڑ�����A���X�؃A�J�E���g�Ń��O�C������V�C���X�^���X��ԋp���܂��B
	 */
	public static Rest getDefaultC8YInstance() {
		return new Rest("https://nttcom.cumulocity.com", "nttcom", "us.sasaki@ntt.com", "nttcomsasaki3");
	}
	
/*------------------
 * instance methods
 */
	/**
	 * GET ���g�p�������N�G�X�g�����܂��B
	 * ���̎����́A/platformAPI ���ߑł��ɂȂ��Ă��܂��B
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

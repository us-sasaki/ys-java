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
	 * GET ���N�G�X�g�����܂��B
	 * ���̎����́Aresource = /platform, type = platformApi �Ɛݒ肷�邱�Ƃ�
	 * �z�肵�Ă��܂��B
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
	 * POST ���N�G�X�g�����܂��B
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

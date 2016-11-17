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
		/**
		 * HTTP ���X�|���X�R�[�h��ԋp���܂��B
		 *
		 * @see		java.net.HttpURLConnection
		 */
		public int code;
		public String message;
		//public String contentType;
		protected byte[] body;
		
		/**
		 * ���ʂ� body �� byte[] �Ŏ擾���܂��B
		 */
		public byte[] toByteArray() {
			return body;
		}
		/**
		 * ���ʂ� body �� String �Ŏ擾���܂�
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
		 * ���ʂ� body �� JsonType �Ŏ擾���܂��B
		 * �G���[���X�|���X�Ɋւ��錋�ʂ͕s��ŁA�ʏ� JsonParseExcception
		 * ���X���[����܂��B
		 */
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
	 * GET ���N�G�X�g�����܂��B
	 * ���̎����́Aresource = /platform, type = platformApi �Ɛݒ肷�邱�Ƃ�
	 * �z�肵�Ă��܂��B
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
		
		// ���ʃI�u�W�F�N�g�̐���
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
	 * POST ���N�G�X�g�����܂��B
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
		
		// ���ʃI�u�W�F�N�g�̐���
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
	 * PUT ���N�G�X�g�����܂��B
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
		
		// ���ʃI�u�W�F�N�g�̐���
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
		// Step 0 �f�o�C�X�N���f���V�����v��
		System.out.println("------ Step 0 ------");
		
		Rest r = new Rest("https://nttcom.cumulocity.com", "management", "devicebootstrap", "Fhdt1bb1f"); //"us.sasaki@ntt.com", "nttcomsasaki3");
		JsonObject jo = new JsonObject().add("id", "ysdev000001");
		Response resp = r.post("/devicecontrol/deviceCredentials", "deviceCredentials", jo);
		printResp(resp);
		
		// �f�o�C�X�N���f���V�����͖������āAdefaultInstance �Ŋe�X�e�b�v������
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


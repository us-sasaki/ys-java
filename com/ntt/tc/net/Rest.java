package com.ntt.tc.net;

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
 * REST �ɂ��v�����ȒP�ɍs�����߂̃N���X�B�y�ʉ��̂��߁A�O�����C�u������
 * ��ˑ��ł���(java.net.HttpURLConnection �x�[�X)
 * �������p��Ԃ����A�e�� REST API �͂������Ă���B
 *
 * @version	29, November 2016
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
	 * �ɐڑ�����Ademouser�A�J�E���g�Ń��O�C������V�C���X�^���X��ԋp���܂��B
	 */
	public static Rest getDefaultC8YInstance() {
		return new Rest("https://nttcom.cumulocity.com", "demouser", "demouser");
	}
	
/*------------------
 * instance methods
 */
	/**
	 * ���X�|���X�R���e���g(JSON)���܂܂�邩�����X�|���X�R�[�h�Ŕ���
	 * ���ۂ́A�v���[�����@�̊֌W�Ō��܂�Ǝv����B
	 */
	private boolean hasContent(int responseCode) {
		if (responseCode < 400) return true;
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
		URL url = new URL(urlStr + resource);
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
	
	/**
	 * POST ���N�G�X�g�����܂��B
	 */
	public Response post(String location, JsonType json)
							throws IOException {
		return post(location, "", json.toString());
	}
	
	public Response post(String location, String body)
							throws IOException {
		return post(location, "", body);
	}
	public Response post(String location, String type, JsonType json)
							throws IOException {
		return post(location, type, json.toString());
	}
	public Response post(String location, String type, String body)
							throws IOException {
		URL url = new URL(urlStr + location);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		if ("".equals(type)) {
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
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
	 * �o�C�i���f�[�^���t�@�C����POST���܂�
	 * location �� /inventory/binaries ��z��B
	 */
	public Response postBinary(String filename, String mimetype, byte[] data)
							throws IOException {
		String location = "/inventory/binaries";
		URL url = new URL(urlStr + location);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
//		con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity.managedObject+json; charset=UTF-8; ver=0.9");
		con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.managedObject+json; charset=UTF-8");
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		String bry = "----boundary----132435465789554----";
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary="+bry);
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		
		OutputStream out = new BufferedOutputStream(out2); //con.getOutputStream());
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
		
		// object part
		pw.println("--"+bry); // multipart�̉��s�R�[�h CR+LF
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
		
		// �t�@�C������
		out.write(data);
		out.write("\r\n".getBytes());
		out.flush();
		pw.println("--"+bry+"--");
		pw.println();
		pw.flush();
		
		//System.out.println(new String(out2.toByteArray()));
		con.getOutputStream().write(out2.toByteArray());
		con.getOutputStream().flush();
		
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
		URL url = new URL(urlStr + resource);
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
}

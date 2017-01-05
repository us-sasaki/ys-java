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
	
	/** Cumulocity App-key */
	protected String appKey;
	
	/** Processing Mode */
	protected boolean modeIsTransient = false;
	
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
	 * �A�v���P�[�V�����L�[��ݒ肵�܂��B
	 */
	public void setApplicationKey(String key) {
		this.appKey = key;
	}
	
	/**
	 * �v���Z�b�V���O���[�h��ݒ肵�܂��B
	 *
	 * @param	isTransient	TRANSIENT���[�h(DB�ɏ������܂Ȃ�)�𗘗p���邩
	 */
	public void setProcessingMode(boolean isTransient) {
		modeIsTransient = isTransient;
	}
	
	/**
	 * GET ���N�G�X�g�����܂��B
	 *
	 * @param	resource	GET���郊�\�[�X
	 * @return	Rest.Response �I�u�W�F�N�g
	 */
	public Response get(String resource) throws IOException {
		return get(resource, "");
	}
	
	/**
	 * GET ���N�G�X�g�����܂��B
	 */
	public Response get(String location, String type) throws IOException {
		return requestImpl(location, "GET", type, null);
	}
	
	/**
	 * DELETE ���N�G�X�g�����܂��B
	 */
	public Response delete(String location) throws IOException {
		return delete(location, "");
	}
	
	/**
	 * DELETE ���N�G�X�g�����܂��B
	 */
	public Response delete(String location, String type) throws IOException {
		return requestImpl(location, "DELETE", type, null);
	}
	
	/**
	 * PUT ���N�G�X�g�����܂��B
	 */
	public Response put(String resource, JsonType json)
							throws IOException {
		return put(resource, "", json);
	}
	/**
	 * PUT ���N�G�X�g�����܂��B
	 */
	public Response put(String resource, String body)
							throws IOException {
		return put(resource, "", body);
	}
	/**
	 * PUT ���N�G�X�g�����܂��B
	 */
	public Response put(String resource, String type, JsonType json)
							throws IOException {
		return put(resource, type, json.toString());
	}
	/**
	 * PUT ���N�G�X�g�����܂��B
	 */
	public Response put(String resource, String type, String body)
							throws IOException {
		return requestImpl(resource, "PUT", type, body);
	}
	
	/**
	 * POST ���N�G�X�g�����܂��B
	 */
	public Response post(String location, JsonType json)
							throws IOException {
		return post(location, "", json.toString());
	}
	
	/**
	 * POST ���N�G�X�g�����܂��B
	 */
	public Response post(String location, String body)
							throws IOException {
		return post(location, "", body);
	}
	
	/**
	 * POST ���N�G�X�g�����܂��B
	 */
	public Response post(String location, String type, JsonType json)
							throws IOException {
		return post(location, type, json.toString());
	}
	
	/**
	 * POST ���N�G�X�g�����܂��B
	 */
	public Response post(String location, String type, String body)
							throws IOException {
		return requestImpl(location, "POST", type, body);
	}
	
	/**
	 * Http���N�G�X�g�̎��������s���܂��B
	 */
	private Response requestImpl(String location, String method)
							throws IOException {
		return requestImpl(location, method, "", null);
	}
	
	/**
	 * Http���N�G�X�g�̎��������s���܂��B
	 */
	private Response requestImpl(String location, String method, String type)
							throws IOException {
		return requestImpl(location, method, type, null);
	}
	
	/**
	 * Http���N�G�X�g�̎��������s���܂��B
	 */
	private Response requestImpl(String location, String method,
								String type, String body)
							throws IOException {
		if (body == null || body.equals("")) {
			return requestImpl(location, method, type, type, null);
		}
		return requestImpl(location, method, type, type, body.getBytes("UTF-8"));
	}
	
	/**
	 * Http���N�G�X�g�̎��������s���܂��B
	 * Cumulocity �ŗL�̃w�b�_��t�����܂��B
	 *
	 * @param	location	���\�[�X�̏ꏊ /platform ��
	 * @param	method		GET/POST/PUT/DELETE
	 * @param	type		�A�v���P�[�V�����^�C�v(platformApi��)
	 * @param	body		body �ɐݒ肷��f�[�^
	 */
	private Response requestImpl(String location, String method,
								String contentType, String accept,
								byte[] body) throws IOException {
		URL url = new URL(urlStr + location);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		// �o�͐ݒ�
		boolean doOutput = (body != null && body.length > 0);
		if (doOutput) con.setDoOutput(true);
		
		// ���\�b�h
		con.setRequestMethod(method);
		
		// Content-Type
		if ("".equals(contentType)) {
			con.setRequestProperty("Content-Type", "application/json");
		} else {
			con.setRequestProperty("Content-Type", "application/vnd.com.nsn.cumulocity."+contentType+"+json; charset=UTF-8; ver=0.9");
		}
		// Accept
		if ("".equals(accept)) {
			con.setRequestProperty("Accept", "application/json");
		} else {
			con.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity."+accept+"+json; charset=UTF-8; ver=0.9");
		}
		// X-Cumulocity-Application-Key
		if (appKey != null && !appKey.equals("")) {
			con.setRequestProperty("X-Cumulocity-Application-Key", appKey);
		}
		// X-Cumulocity-Processing-Mode
		if (modeIsTransient) {
			con.setRequestProperty("X-Cumulocity-Processing-Mode", "TRANSIENT");
		}
		
		
		// ��{�F��
		if (user != null && password != null) {
			String authStr = Base64.getEncoder().encodeToString((tenant + user + ":" + password).getBytes());
			con.setRequestProperty("Authorization", "Basic " + authStr);
		}
		
		// �o��
		if (doOutput) {
			BufferedOutputStream bo = new BufferedOutputStream(con.getOutputStream());
			bo.write(body);
			bo.flush();
		}
		
		// ���ʃI�u�W�F�N�g�̐���
		Response resp = new Response();
		resp.code = con.getResponseCode();
		if (resp.code < 400) {
			
			ByteArrayOutputStream baos =  new ByteArrayOutputStream();
			BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
			for (;;) {
				int c = bis.read();
					if (c == -1) break;
				baos.write(c);
			}
			baos.close();
			bis.close();
			resp.body = baos.toByteArray();
		} else {
			resp.message = con.getResponseMessage();
		}
		con.disconnect();
		
		return resp;
	}
	
	/**
	 * �o�C�i���f�[�^���t�@�C����POST���܂�
	 */
	public Response postBinary(String filename, String mimetype, byte[] data)
							throws IOException {
		
		// body �𐶐�����
		String bry = "----boundary----13243546"+(long)(Math.random() * 1000000000)+"5789554----";
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		
		OutputStream out = new BufferedOutputStream(out2);
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
		
		return requestImpl("/inventory/binaries", "POST", "multipart/form-data; boundary="+bry, "managedObject", out2.toByteArray());
	}
}

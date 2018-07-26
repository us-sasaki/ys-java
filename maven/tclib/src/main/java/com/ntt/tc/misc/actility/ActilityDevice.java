package com.ntt.tc.misc.actility;

import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

import com.ntt.tc.data.TC_Date;

/**
 * ダミー Actility として、HTTPS で XML を POST します。
 * 試験で利用できるよう、HTTPヘッダや XML を変更できます。
 * makeXML は https://support.cumulocity.com/hc/en-us/requests/16770
 * での Ozge の例に準拠しています。
 *
 * @version		May 29, 2018
 * @author		Yusuke Sasaki
 */
public class ActilityDevice {
	private String serverUrl;
	
	private String devEui;
	private String devAddr;
	private int fport;
	private int fcntup;
	private int fcntdn;
	
/*--------------------
 * static inner class
 */
	/**
	 * Actility Device 内で利用される、HTTP Response を表す構造体クラス
	 */
	public static class HttpResp {
		/** HTTP ステータスコード(3桁の数値) */
		public int status;
		
		/** HTTP ステータスメッセージ */
		public String message;
		
		/** HTTP レスポンスボディ */
		public byte[] body;
		
	/*-------------
	 * constructor
	 */
		/**
		 * 空のオブジェクトを生成します。
		 */
		HttpResp() {
		}
		
		/**
		 * 指定された値を持つオブジェクトを生成します。
		 *
		 * @param		status		ステータスコード
		 * @param		message		ステータスメッセージ
		 * @param		body		レスポンスボディ
		 */
		HttpResp(int status, String message, byte[] body) {
			this.status		= status;
			this.message	= message;
			this.body		= body;
		}
		
	}
	
/*-------------
 * constructor
 */
	/**
	 * 指定されたパラメータでインスタンスを作成します。
	 *
	 * @param		serverUrl		接続先(SSA)の URL "https://....:8021" 等
	 * @param		devEui			DevEUI(16文字ヘキサ文字列)
	 * @param		devAddr			DevAddr(8文字ヘキサ文字列)
	 * @param		fport			fport値(1 等)
	 */
	public ActilityDevice(String serverUrl, 
							String devEui,
							String devAddr,
							int fport) {
		fcntup = 0;
		fcntdn = 0;
		
		if (!serverUrl.endsWith("/")) serverUrl = serverUrl + "/";
		this.serverUrl = serverUrl;
		this.devEui = devEui;
		this.devAddr = devAddr;
		this.fport = fport;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * XML を POST します。
	 *
	 * @param		payloadHex		payloadHex 値
	 * @return		結果を byte 列にしたもの
	 * @throws		java.io.IOException	通信異常
	 */
	public HttpResp post(String payloadHex) throws IOException {
		byte[] body = makeXML(payloadHex).getBytes();
		Map<String, String> header = new HashMap<String, String>();
		header.put("Content-Type", "application/xml");
		return post(serverUrl+"?" + makeURLParam(), header, body);
	}
	
	/**
	 * XML を POST します。
	 *
	 * @param		xml		xml 値
	 * @return		結果を byte 列にしたもの
	 * @throws		java.io.IOException	通信異常
	 */
	public HttpResp postXML(String xml) throws IOException {
		byte[] body = xml.getBytes();
		Map<String, String> header = new HashMap<String, String>();
		header.put("Content-Type", "application/xml");
		return post(serverUrl+"?" + makeURLParam(), header, body);
	}
	
	/**
	 * POST の実処理を行います。(java.net.* を利用した実装)
	 * 指定された任意の body を post します。
	 * post(payload_hex) では、
	 * <pre>
	 * endpoint = "?LrnDevEui="+devEui
	 * header = Map { "Content-Type": "application/xml" }
	 * body = makeXML(payload_hex).getBytes()
	 * </pre>
	 * を指定しています。
	 *
	 * @param		urlStr		URL(http ではじまらない場合、serverUrl が
	 *							自動的に付加されます。
	 * @param		header		HTTP Header
	 * @param		body		HTTP Body
	 * @return		結果
	 * @throws		java.io.IOException	通信異常
	 */
	public HttpResp post(String urlStr, Map<String, String> header, byte[] body)
							throws IOException {
		// URL に接続
		if (!urlStr.startsWith("http")) urlStr = serverUrl + urlStr;
		URL url = new URL(urlStr);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		// output する
		con.setDoOutput(true);
		
		// method
		con.setRequestMethod("POST");
		
		// ヘッダ
		for (String key : header.keySet()) {
			String value = header.get(key);
			con.setRequestProperty(key, value);
		}
		
		// output
		OutputStream out = con.getOutputStream();
		out.write(body);
		out.flush();
		
		// input
		int code = con.getResponseCode();
		String msg = con.getResponseMessage();
		
		// Response
		HttpResp resp = new HttpResp();
		resp.status		= code;
		resp.message	= msg;
		
		InputStream in = null;
		if (code < 400) {
			in = con.getInputStream();
		} else {
			in = con.getErrorStream();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		while (true) {
			int c = in.read();
			if (c == -1) break;
			baos.write(c);
		}
		baos.close();
		in.close();
		
		resp.body = baos.toByteArray();
		
		return resp;
	}
	
	/**
	 * XML を構成します。
	 * この XML は NTTネオメイトより GlobalSAT メッセージとして2017/4に
	 * 受領した形式です。(Ozge の例にも準拠)
	 *
	 * @param		payload_hex		payloadHex 値です(ヘキサ文字列)
	 * @return		構成された XML
	 */
	public String makeXML(String payload_hex) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<DevEUI_uplink xmlns=\"http://uri.actility.com/lora\">");
		sb.append("<Time>");
		sb.append(new TC_Date().getValue()); //2017-04-10T09:41:34.922+02:00
		sb.append("</Time><DevEUI>");
		sb.append(devEui); //000DB53114873546 case sensitive
		sb.append("</DevEUI><FPort>");
		sb.append(fport); //2
		sb.append("</FPort><FCntUp>");
		sb.append(fcntup); //3
		sb.append("</FCntUp><MType>4</MType><FCntDn>");
		sb.append(fcntdn); //4
		sb.append("</FCntDn><payload_hex>");
		sb.append(payload_hex); //00025f0211705b08139d7b
		sb.append("</payload_hex><mic_hex>aad53643</mic_hex>");
		sb.append("<Lrcid>00000201</Lrcid><LrrRSSI>-57.000000</LrrRSSI>");
		sb.append("<LrrSNR>9.500000</LrrSNR><SpFact>12</SpFact>");
		sb.append("<SubBand>G0</SubBand><Channel>LC5</Channel>");
		sb.append("<DevLrrCnt>1</DevLrrCnt><Lrrid>080E004B</Lrrid>");
		sb.append("<Late>0</Late><Lrrs><Lrr><Lrrid>080E004B</Lrrid>");
		sb.append("<Chain>0</Chain><LrrRSSI>-57.000000</LrrRSSI>");
		sb.append("<LrrSNR>9.500000</LrrSNR><LrrESP>-57.461838</LrrESP>");
		sb.append("</Lrr></Lrrs><CustomerID>100002293</CustomerID>");
		sb.append("<CustomerData>{\"alr\":{\"pro\":\"LORA/Generic\",\"ver\":\"1\"}}</CustomerData>");
		sb.append("<ModelCfg>0</ModelCfg><DevAddr>");
		sb.append(devAddr); //14873546
		sb.append("</DevAddr><AckRequested>1</AckRequested></DevEUI_uplink>");
		return sb.toString();
	}
	
	public String makeURLParam() {
		return "LrnDevEui="+devEui+"&LrnFPort="+fport
				+"&LrnInfos=TWA_100002293.3460.AS-1-18037232&AS_ID=neo-test&"
				+ "Time=2017-04-10T09:41:35.683%2B02:00&"
				+ "Token=096c26ef649bf653afefc6702f90b4641f78aeac1f6cab78d2872e2f57f287ae";
	}
}

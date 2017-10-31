package com.ntt.tc.net;

import java.io.IOException;
import java.util.List;

import abdom.data.json.JsonType;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.real.*;
import com.ntt.tc.data.device.Operation;

public class C8yEventHandler extends Thread {
	static final int TIMEOUT = 10; // 10 sec
	
	OperationListener operationListener;
	String sourceId;
	String clientId;
	//List<C8yEventListener> listeners;
	//List<String> sourceId;
	//List<String> clientId;
	API api;
	boolean started = false;
	
/*-------------
 * constructor
 */
	public C8yEventHandler(API api) {
		this.api = api;
	}
	
/*------------------
 * instance methods
 */
	public void addOperationListener(OperationListener listener, String sourceId) {
		this.sourceId = sourceId;
		operationListener = listener;
		if (!started) {
			started = true;
			start();
		}
	}
	
	@Override
	public void run() {
		boolean connected = false;
		while (true) {
			try {
				NotificationRequest nr;
				Rest.Response resp;
				JsonType jt;
				NotificationResponse nrp;
				if (!connected) {
					//
					// 接続処理(clientId 取得)
					//   handshake, subscribe を実行
					//   "/operation/{id}" を subscribe
					//
					
					// handshake
					nr = new NotificationRequest();
					nr.channel = "/meta/handshake";
					nr.version = "1.0";
					nr.minimumVersion = "1.0beta";
					nr.supportedConnectionTypes = new String[] { "long-polling" };
					
					nr.advice = new Advice();
					nr.advice.interval	= 1 * 1000; // 1 sec
					nr.advice.timeout	= TIMEOUT * 1000; // 5 sec
					
					resp = api.getRest().post("/cep/realtime", nr);
					
					// 配列要素が１の場合のみ実装
					jt = resp.toJson();
					if (jt.size() > 1)
						throw new RuntimeException("handshake のレスポンスとして複数項目が含まれます:"+jt.size());
					
					nrp = Jsonizer.fromJson(jt.get(0),
													NotificationResponse.class);
					if (!nrp.successful)
						throw new RuntimeException("handshake が失敗"+nrp.toString("  "));
					this.clientId = nrp.clientId;
					
					// subscribe( /operation/{op-id} )
					nr = new NotificationRequest();
					nr.channel = "/meta/subscribe";
					nr.id = 12345; // 固定値！！　要修正
					nr.subscription = "/operations/"+sourceId;
					nr.clientId = clientId;
					
					resp = api.getRest().post("/cep/realtime", nr);
					
					// 配列要素が1の場合のみ実装
					jt = resp.toJson();
					if (jt.size() > 1)
						throw new RuntimeException("subscribe のレスポンスとして複数項目が含まれます:" + jt.size());
					
					nrp = Jsonizer.fromJson(jt.get(0), NotificationResponse.class);
					if (!nrp.successful)
						throw new RuntimeException("subscribe が失敗"+nrp.toString("  "));
					connected = true;
					System.out.println("listening to operation of " + sourceId + " at " + clientId);
				}
				// connect
				nr = new NotificationRequest();
				nr.channel = "/meta/connect";
				nr.clientId = clientId;
				nr.connectionType = "long-polling";
				nr.id = 12345; // 固定値！！
				
				nr.advice = new Advice();
				nr.advice.interval	= 1 * 1000; // 1 sec
				nr.advice.timeout	= TIMEOUT * 1000; // 5 sec
				
				resp = api.getRest().post("/cep/realtime", nr);
				
				// 配列要素が1の場合のみ実装
				jt = resp.toJson();
				if (jt.size() > 1)
					System.out.println("connect のレスポンスとして複数項目が含まれます:" + jt.size());
				
				nrp = Jsonizer.fromJson(jt.get(0), NotificationResponse.class);
				if (false) {
					// "Unknown Client" がエラーらしい
					connected = false;
					System.out.println("connect が失敗"+nrp.toString("  "));
				} else {
					System.out.println("operation 検知");
					System.out.println(nrp.toString("  "));
					if (nrp.data != null) {
						Operation ope = new Operation();
						ope.fill(nrp.data.get("data").toString());
						
						// イベントハンドラのメインスレッドからたたいているが、、
						operationListener.operationPerformed(ope);
					} else {
						System.out.println("reconnect");
					}
				}
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
	}
	
}

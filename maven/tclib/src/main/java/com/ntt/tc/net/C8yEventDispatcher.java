package com.ntt.tc.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import abdom.data.json.JsonArray;
import abdom.data.json.JsonType;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.real.*;
import com.ntt.tc.data.device.Operation;
import com.ntt.tc.data.alarms.Alarm;
import com.ntt.tc.data.inventory.ManagedObject;
import com.ntt.tc.data.events.Event;

/**
 * 処理内容
 * <pre>
 * 1. 登録されている各 listener に session を対応させる。
 * 2. 無効な( = clientId がない) session に対し、clientId 取得(handshake)する
 * 3. clientId が取得できたら、listener の種類に応じて決まる subscription を登録
 * 4. clientId 取得、subscription 登録はすべて完了するまで続ける
 * 5. コネクトし、レスポンス内容を listener に通知
 * 6. 5.に戻る。コネクト失敗したセッションがあれば、clientId = null とし2.に戻る。
 * 4-6 で、listener を削除する場合、disconnect, unsubscribe 処理を行う
 *
 * add/remove の場合、すでにコネクトされている通信があれば強制的に切る。
 * add 対象のみ handshake/subscribe するようにしたい
 * サーバからのハートビートを検知して通信を張りなおしたい。
 * disconnect を実装する。(すべて remove)
 * </pre>
 *
 * @author	Yusuke Sasaki
 */
public class C8yEventDispatcher extends Thread {
	static final int HANDSHAKE_RETRY = 2;
	static final long HANDSHAKE_RETRY_WAIT = 10 * 1000L; // 10 sec
	
	static final int SUBSCRIBE_RETRY = 2;
	static final long SUBSCRIBE_RETRY_WAIT = 10 * 1000L; // 10 sec
	
	// Advice の値
	static final int TIMEOUT = 10; // 10 sec
	
	API api;
	boolean started = false; // 通信スレッドが開始したか
	boolean isStopping = false; // 強制終了中か
	
	int[] id;
	private List<Session> sessions; // 利用時 synchronized 要
	
	
	/**
	 * 個別のセッション情報の構造体
	 */
	private static class Session {
		private String	clientId;
		private int		id;
		/** deviceId, managedObjectId, agentId, * などの通知対象 id まで */
		private String	subscription;
		private boolean subscribed = false;
		private Consumer<JsonType> listener;
		private boolean isRemoved = false;
	}
	
/*-------------
 * constructor
 */
	public C8yEventDispatcher(API api) {
		this.api = api;
		sessions = new ArrayList<Session>();
		
//		Session s = new Session();
//		Consumer<Operation> a = (c8y -> System.out.println(c8y));
//		s.listener = a;
//		s.listener.accept((Operation)null);
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 指定されたデバイス向けの Operation の通知を受ける listener を登録
	 * します。 
	 *
	 * @param	listener	通知を受ける listener
	 * @param	deviceId	Operation を発行するデバイスの managedObject ID
	 */
	public void addOperationListener(OperationListener listener,
										String deviceId) {
		addListener( jt -> listener.operationPerformed(
			Jsonizer.fromJson(jt, Operation.class)), "/operations/"+deviceId );
	}
	
	public void addInventoryListener(InventoryListener listener,
										String managedObjectId) {
		addListener( jt -> listener.inventoryUpdated(
			Jsonizer.fromJson(jt, ManagedObject.class)),
			"/managedobjects/" + managedObjectId);
	}
	
	public void addEventListener(EventListener listener,
										String deviceId) {
		addListener( jt -> listener.eventReceived(
			Jsonizer.fromJson(jt, Event.class)), "/events/" + deviceId);
	}
	
	public void addAlarmListener(AlarmListener listener,
										String deviceId) {
		addListener( jt -> listener.alarmRaised(
			Jsonizer.fromJson(jt, Alarm.class)), "/alarms/" + deviceId);
	}
	
//　/<<moduleName>>/<<statementName>>
//　/devicecontrol/notifications
//　/<<agentId>>

	
	public void addListener(Consumer<JsonType> listener, String target) {
		Session s = new Session();
		s.subscription = target;
		s.listener = listener;
		synchronized (this) {
			sessions.add(s);
		}
		if (!started) {
			started = true;
			start();
		}
	}
	
	public void removeListener(Consumer<JsonType> listener) {
		synchronized(this) {
			for (Session s : sessions) {
				if (s.listener == listener) {
					s.isRemoved = true;
					// 未実装：
					// disconnect, unsubscribe
					// sessions から remove
				}
			}
		}
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(100L);
		} catch (InterruptedException ie) {
		}
		while (!isStopping) {
			handshake();
			subscribe();
			connect();
		}
		started = false;
	}
	
	public void stopThread() {
		api.getRest().disconnect();
		//this.interrupt();		// disconnect のみで切れる
		isStopping = true;
	}
	
	
	/**
	 * handShake 処理。複数セッションを同時に処理する。
	 * 失敗したらリトライを行う(未実装)
	 * 通信中、ずっと synchronized のほげ実装
	 */
	private synchronized void handshake() {
		try {
			JsonType reqs = new JsonArray();
			int idcount = 7654321; // 初期値として
			// request
			for (Session s : sessions) {
				if (s.clientId != null) continue;
				
				s.id = idcount++;
				
				// Request の JSON 構築。Array とすべき
				JsonType jo = JsonType.o("channel", "/meta/handshake")
						.put("version", "1.0")
						.put("minimumVersion", "1.0beta")
						.put("supportedConnectionTypes",
									JsonType.a("long-polling"))
						.put("id", s.id)
						.put("advice.interval", 1 * 1000) // 1sec
						.put("advice.timeout", TIMEOUT * 1000); // 5 sec
				
				reqs.push(jo);
			}
			if (reqs.size() == 0) return;
			
			// send, and receive response
			Rest.Response r = api.getRest().post("/cep/realtime", reqs);
			JsonType resps = r.toJson();
			
System.out.println("resps " + resps.toString("  "));
			// dispatch responses to corresponding session.
			for (JsonType resp : resps) {
				int id = resp.get("id").intValue();
				
				// id を持つセッションを全件検索し、session に格納
				Session session = null;
				for (Session s : sessions) {
					if (s.clientId != null) continue;
					if (s.id == id) {
						session = s;
						break;
					}
				}
				if (session == null) { // エラー
					throw new InternalError("handshake リクエストのレスポンスで合致しない id が検出されました:"+resps);
				}
				session.clientId = resp.get("clientId").getValue();
System.out.println("clientId " + session.clientId);
				session.subscribed = false;
				if (!resp.get("successful").booleanValue())
					System.out.println("handshake が失敗"+r.toString("  "));
			}
		} catch (IOException ioe) {
			System.out.println("error !! " + ioe);
		}
	}
	
	/**
	 * subscribe 処理。複数セッションを同時に処理する。
	 */
	private synchronized void subscribe() {
		try {
			JsonType reqs = new JsonArray();
			int idcount = 654321;
			
			for (Session s : sessions) {
				if (s.clientId == null || s.subscribed) continue;
				
				s.id = idcount++;
				JsonType jo = JsonType.o("channel", "/meta/subscribe")
						.put("id", s.id)
						.put("subscription", s.subscription)
						.put("clientId", s.clientId);
				reqs.push(jo);
			}
			System.out.println(reqs.toString("  "));
			if (reqs.size() == 0) return;
			
			Rest.Response r = api.getRest().post("/cep/realtime", reqs);
			System.out.println("subscribe response");
			System.out.println(r);
			JsonType resps = r.toJson();
			
			// dispatch resps to corresponding session.
			for (JsonType resp : resps) {
				int id = resp.get("id").intValue();
System.out.println("subscribe : id " + id);
				
				// id を持つセッションを全件検索し、session に格納
				Session session = null;
				for (Session s : sessions) {
					if (s.clientId == null || s.subscribed) continue;
					if (s.id == id) {
						session = s;
						break;
					}
				}
				if (session == null) { // エラー
					throw new InternalError("subscribe リクエストのレスポンスで合致しない id が検出されました:"+resps);
				}
				
				session.subscribed = true;
				if (!resp.get("successful").booleanValue())
					System.out.println("subscribe が失敗"+r.toString("  "));
			}
		} catch (IOException ioe) {
			System.out.println("error !! " + ioe);
		}
	}
	
	/**
	 * connect 処理。複数セッションを同時に処理する。
	 * この処理は、複数接続をループする。
	 */
	private synchronized void connect() {
		try {
			// connect
			JsonType reqs = new JsonArray();
			int idcount = 54321;
			
			for (Session s : sessions) {
				if (s.clientId == null || !s.subscribed) continue;
				
				s.id = idcount++;
				JsonType jo = JsonType.o("channel", "/meta/connect")
								.put("clientId", s.clientId)
								.put("connectionType", "long-polling")
								.put("id", s.id);
				jo.put("advice.interval", 1 * 1000); // 1 sec
				jo.put("advice.timeout", TIMEOUT * 1000); // 5 sec
				reqs.push(jo);
			}
			
			Rest.Response r = api.getRest().post("/cep/realtime", reqs);
			//
			System.out.println(r.status);
			System.out.println(r.message);
			System.out.println(r.toString());
			
			JsonType resps = r.toJson();
			
			// dispatch resps to corresponding session.
			for (JsonType resp : resps) {
				String channel = resp.get("channel").getValue();
System.out.println("connect : channel " + channel);
				
				// channel を subscribe するセッションを全件検索し、処理
				// 複数の id がある場合どうなるのか？
				for (Session s : sessions) {
					if (s.clientId == null || !s.subscribed) continue;
					if (!s.subscription.equals(channel)) {
						// この辺の処理(条件)はおかしい
						JsonType successful = resp.get("successful");
						if (successful == null || !successful.booleanValue())
							System.out.println("connect が失敗"+r.toString("  "));
						continue;
					}
					// connect の場合、id に対応するレスポンスには
					// data が含まれず、独自の id を含む要素が追加される。
					// subscribe 対象の channel でマッチングする
					if (false) {
						// "Unknown Client" がclientId expired エラーらしい
						s.clientId = null;
						s.subscribed = false;
						System.out.println("connect expired");
					} else {
//System.out.println("operation 検知");
//System.out.println(resp.toString("  "));
						
						JsonType data = resp.get("data");
						if (data == null) {
							System.out.println("data is null.");
						} else if ("CREATE".equals(data.get("realtimeAction").getValue())) {
							JsonType body = data.get("data");
							if (body == null) {
								System.out.println("body is null");
							} else {
								// 別スレッドで実行(しっぱなし)
								new Thread( () -> s.listener.accept(body)).start();
							}
						} else {
							System.out.println("realtimeAction:"+data.get("realtimeAction"));
						}
					}
				}
			}
		} catch (IOException ioe) {
			System.out.println("error !! " + ioe);
			// stopThread() した場合、java.net.SocketException: Socket closed
			
		}
	}
}

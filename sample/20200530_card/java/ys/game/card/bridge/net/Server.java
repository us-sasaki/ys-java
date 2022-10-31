import java.io.*;
import java.net.*;

/**
 * このオブジェクトは、ネットワーク上のサーバとして動作します。
 * ユーザの一覧を保持しており、get(String) メソッドでリモートユーザ
 * に接続されている UserStub を取得することができます。
 * 
 * @author		Yusuke Sasaki
 * @version		making		12, December 2000
 */
public class Server extends Thread {
	public static final int MAX_USERS = 100;
	
	protected UserStub[]	user;
	protected int			port;
	protected Multicaster	router;
	protected BoardManager	boardManager;
	
/*-------------
 * Constructor
 */
	/**
	 * 指定したポートで待機するサーバを作成します。この後、start()メソッドを
	 * コールすることで、サーバが起動します。
	 *
	 * @param		port		ポート番号
	 */
	public Server(int port) {
		super();
		
		this.port = port;
		user = new UserStub[MAX_USERS];
		router = new Multicaster(this);
//		boardManager = new BoardManager();
	}
	
/*-----------
 * overrides
 */
	/**
	 * java.lang.Thread.run() のオーバーライドで、サーバ処理本体です。
	 * ポートが使用できないなどの致命的エラーが発生するまで実行しつづけます。
	 */
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(port);
			
			while (true) {
				try {
					Socket sock = ss.accept();
					assignSocket(sock);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException fatal) {
			fatal.printStackTrace();
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 登録されているすべてのメンバーに対して同一のメッセージを送信します。
	 *
	 * @param		message		送信するメッセージ
	 */
	protected void broadcast(String message) {
		for (int i = 0; i < MAX_USERS; i++) {
			try {
				user[i].send(message);
			} catch (NullPointerException ignored) {
			}
		}
	}
	
	protected synchronized void assignSocket(Socket sock) throws IOException {
		int i;
		for (i = 0; i < MAX_USERS; i++) {
			if (user[i] == null) break;
		}
		if (i == MAX_USERS) return;
		
		user[i] = new UserStub(sock, this, router);
		user[i].start();
	}
	
	/**
	 * このオブジェクトのもつ一覧から指定した UserStub を削除します。
	 * UserStub に対する終了通知などは行われません。
	 *
	 * @param		target		削除対象の UserStub
	 */
	protected synchronized void removeUser(UserStub target) {
		for (int i = 0; i < MAX_USERS; i++) {
			if (user[i] == target) user[i] = null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Server(7878).start();
	}
	
}

/**
 * ブリッジサーバにログインしているユーザスタブ
 *
 * 
 */
public class UserStub implements Runnable {
	/**
	 * このユーザのハンドルネーム。本システムでは、ユーザIDでもある。
	 */
	protected String		name;
	
	/**
	 * Join しているテーブルオブジェクト。BroadCastなどを行う。
	 */
	protected Table			table;
	
	/**
	 * リモートユーザからのコマンドを受け取る入力ストリーム
	 */
	protected InputStream	in;
	protected BufferedReader	br;
	
	/**
	 * リモートユーザに通知する出力ストリーム
	 */
	protected OutputStream	out;
	
	/**
	 * ストリームから各種コマンドを受け取り、ディスパッチするスレッド
	 */
	protected Thread		streamObserver;
	
/*-------------
 * Constructor
 */
	public UserStub(InputStream in, OutputStream out, String name) {
		this.in		= in;
		this.out	= out;
		this.name	= name;
		
		br = new BufferedReader(new InputStreamReader(in));
		
		streamObserver = new Thread(this);
		streamObserver.start();
	}
	
/*-----------------------
 * implements (Runnable)
 */
	/**
	 * 入力ストリームを見張り、各種コマンドを受信した際に適切なメソッドを呼び出す。
	 */
	public void run() {
		while (true) {
			String line = br.readLine();
		}
		
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 各種コマンドメッセージを送信する。
	 * 高速化のため送信の別スレッド化を行う場合、BroadCastを行うオブジェクトで実施すること。
	 */
	public void send(String message) {
	}
	
	
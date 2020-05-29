import java.io.*;
import java.net.*;

/**
 * サーバ側で動作するユーザスタブクラス。
 * 基本的にはソケットを保持し、入力ストリームを見張ってイベントを発生します。
 *
 * コマンドの形式
 *
 * @version		making		21, November 2000
 */
public class UserStub extends Thread {
	protected Socket			sock;
	protected PrintWriter		p;
	protected BufferedReader	br;
	protected Multicaster		handler;
	protected Server			parent;
	
/*-------------
 * Constructor
 */
	/**
	 * 
	 */
	public UserStub(Socket sock, Server parent, Multicaster handler)
						throws IOException {
		super();
		
		this.sock = sock;
		p = new PrintWriter(
				new OutputStreamWriter(
					sock.getOutputStream()));
		br = new BufferedReader(
				new InputStreamReader(
					sock.getInputStream()));
		
		this.parent		= parent;
		this.handler	= handler;
	}
	
	public void run() {
		try {
			while (true) {
				String input = br.readLine();
				if (input == null)	// End Of File
					disconnect();
				else
					dispatchEvent(input);
			}
		} catch (IOException e) {
			disconnect();
		}
	}
	
	protected void dispatchEvent(String input) {
		handler.chat(input);
	}
	
	/**
	 * disconnect後にコールされることもある.
	 */
	public synchronized void send(String msg) {
		p.println(msg);
		p.flush();
	}
	
	public synchronized void disconnect() {
		parent.removeUser(this);
		try {
			sock.close();
		} catch (IOException ignored) {
		}
	}
}
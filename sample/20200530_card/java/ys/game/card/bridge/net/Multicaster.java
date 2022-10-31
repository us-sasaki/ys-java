/**
 * ユーザから送られた情報を、適切な相手にブロードキャストします。
 * このオブジェクトはサーバ側で実行されます。
 * 
 * @author		Yusuke Sasaki
 * @version		making		10, December 2000
 */
public class Multicaster {
	protected Server server;
	
	public Multicaster(Server server) {
		this.server = server;
	}
	
	public void chat(String input) {
		server.broadcast(input);
	}
}

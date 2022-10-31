/**
 * ユーザから送られた情報を、適切な相手にブロードキャストします。
 * このオブジェクトはサーバ側で実行されます。
 * 
 * @author		Yusuke Sasaki
 * @version		making		10, December 2000
 */
public class UserEventRouter {
	protected Server server;
	
	public UserEventRouter(Server server) {
		this.server = server;
	}
	
	public void chat(String input) {
		server.broadcast(input);
	}
}

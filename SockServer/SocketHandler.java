import java.io.*;
import java.net.*;
import java.util.logging.*;

/**
 * Socket のクローズを管理するオブジェクト
 */
public class SocketHandler extends Thread {
	/** クライアントからの Socket */
	protected Socket	sock;
	
	/** クライアント → サーバ のパイプ */
	protected StreamLogger	client2server;
	
	protected int	id;
	protected Logger logger;
	
/*-------------
 * constructor
 */
	public SocketHandler(Socket sock, int id, Logger logger) {
		this.sock		= sock;
		this.id			= id;
		this.logger		= logger;
	}
	
/*-----------
 * overrides
 */
	public void run() {
		logger.fine("SocketHandler," + id + ",,開始");
		
		try {
			logger.finer("SocketHandler," + id + ",,ClientSocket Prop"
				+ ",(TCP_NODELAY="	+ sock.getTcpNoDelay() // Nagleアルゴリズムを無効にして小さいパケットでも即送信
				+ "),(SO_KEEPALIVE=" + sock.getKeepAlive() // 2時間無通信の場合にダミーパケットを送る
				+ "),(SO_TIMEOUT="	+ sock.getSoTimeout()
				+ "),(SO_RCVBUF="	+ sock.getReceiveBufferSize()
				+ "),(SO_SNDBUF="	+ sock.getSendBufferSize()
				+ "),(SO_REUSEADDR="	+ sock.getReuseAddress()
				+ "),(SO_LINGER="	+ sock.getSoLinger() // TIME_WAITにならず即CLOSE
				+ "),(TraficClass="	+ sock.getTrafficClass()
				+ ")");
		
			// クライアント Socket から Stream を取得
			InputStream		in		= sock.getInputStream();
			OutputStream	out		= sock.getOutputStream(); // 使われない
			
			client2server = new StreamLogger("StreamLogger,"+String.valueOf(id)+",c2s,", in, logger);
			
			// thread start
			client2server.start();
			
			try {
				client2server.join(1000*60*3);
			} catch (InterruptedException e) {
				logger.severe(e.toString() + "join() Interrupted id=" + id);
			}
			sock.close();
		} catch (IOException e) {
			logger.severe(e.toString() + "SocketHandler IOException id=" + id);
		}

		logger.fine("SocketHandler," + id + ",,終了");
		
	}
}

import java.io.*;
import java.net.*;

/**
 * 超簡易なサーバ。起動後、１回のSocket接続を受け、入力内容を
 * System.out にダンプする。ダンプは入力コードをそのまま出力する。
 * (ASCII文字列で入力されることを想定している)
 *
 *
 * Cumulocity の REST Request 電文取得用を目的とする。
 */
public class Logger {
	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(8080, 10);
		System.out.println("Server Started");
		Socket sock = ss.accept();
		System.out.println("Socket Accepted");
		InputStream in = sock.getInputStream();
		
		while (true) {
			int c = in.read();
			if (c == -1) break;
			System.out.write(c);
			System.out.flush();
		}
		
		sock.close();
	}
}

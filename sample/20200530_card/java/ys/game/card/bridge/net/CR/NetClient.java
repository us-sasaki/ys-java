import java.io.*;
import java.net.*;

public class NetClient extends Thread {
	
	protected Socket			sock;
	protected PrintWriter		p;
	protected BufferedReader	br;
	
	protected NetEventListener	listener;
	
/*-------------
 * Constructor
 */
	public NetClient(String serverAddress, int port) throws IOException {
		super();
		
		//
		// connect
		//
		sock = new Socket(serverAddress, port);
		
		p = new PrintWriter(
				new OutputStreamWriter(
					sock.getOutputStream()));
		
		br = new BufferedReader(
				new InputStreamReader(
					sock.getInputStream()));
		
		//
		// ‹N“®
		//
		start();
	}
	
/*------------------
 * instance methods
 */
	public void setNetEventListener(NetEventListener listener) {
		if (this.listener == null) this.listener = listener;
	}
	
	public void run() {
		try {
			while (true) {
				String line = br.readLine();
				if (line == null) break;
				
				if (listener != null) listener.commandInvoked(line);
			}
		} catch (IOException e) {
		}
	}
	
	public PrintWriter getPrintWriter() {
		return p;
	}
}


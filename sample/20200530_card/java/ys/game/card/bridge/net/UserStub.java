import java.io.*;
import java.net.*;

/**
 * �T�[�o���œ��삷�郆�[�U�X�^�u�N���X�B
 * ��{�I�ɂ̓\�P�b�g��ێ����A���̓X�g���[�����������ăC�x���g�𔭐����܂��B
 *
 * �R�}���h�̌`��
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
	 * disconnect��ɃR�[������邱�Ƃ�����.
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
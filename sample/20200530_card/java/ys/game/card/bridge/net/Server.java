import java.io.*;
import java.net.*;

/**
 * ���̃I�u�W�F�N�g�́A�l�b�g���[�N��̃T�[�o�Ƃ��ē��삵�܂��B
 * ���[�U�̈ꗗ��ێ����Ă���Aget(String) ���\�b�h�Ń����[�g���[�U
 * �ɐڑ�����Ă��� UserStub ���擾���邱�Ƃ��ł��܂��B
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
	 * �w�肵���|�[�g�őҋ@����T�[�o���쐬���܂��B���̌�Astart()���\�b�h��
	 * �R�[�����邱�ƂŁA�T�[�o���N�����܂��B
	 *
	 * @param		port		�|�[�g�ԍ�
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
	 * java.lang.Thread.run() �̃I�[�o�[���C�h�ŁA�T�[�o�����{�̂ł��B
	 * �|�[�g���g�p�ł��Ȃ��Ȃǂ̒v���I�G���[����������܂Ŏ��s���Â��܂��B
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
	 * �o�^����Ă��邷�ׂẴ����o�[�ɑ΂��ē���̃��b�Z�[�W�𑗐M���܂��B
	 *
	 * @param		message		���M���郁�b�Z�[�W
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
	 * ���̃I�u�W�F�N�g�̂��ꗗ����w�肵�� UserStub ���폜���܂��B
	 * UserStub �ɑ΂���I���ʒm�Ȃǂ͍s���܂���B
	 *
	 * @param		target		�폜�Ώۂ� UserStub
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

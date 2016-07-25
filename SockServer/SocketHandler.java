import java.io.*;
import java.net.*;
import java.util.logging.*;

/**
 * Socket �̃N���[�Y���Ǘ�����I�u�W�F�N�g
 */
public class SocketHandler extends Thread {
	/** �N���C�A���g����� Socket */
	protected Socket	sock;
	
	/** �N���C�A���g �� �T�[�o �̃p�C�v */
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
		logger.fine("SocketHandler," + id + ",,�J�n");
		
		try {
			logger.finer("SocketHandler," + id + ",,ClientSocket Prop"
				+ ",(TCP_NODELAY="	+ sock.getTcpNoDelay() // Nagle�A���S���Y���𖳌��ɂ��ď������p�P�b�g�ł������M
				+ "),(SO_KEEPALIVE=" + sock.getKeepAlive() // 2���Ԗ��ʐM�̏ꍇ�Ƀ_�~�[�p�P�b�g�𑗂�
				+ "),(SO_TIMEOUT="	+ sock.getSoTimeout()
				+ "),(SO_RCVBUF="	+ sock.getReceiveBufferSize()
				+ "),(SO_SNDBUF="	+ sock.getSendBufferSize()
				+ "),(SO_REUSEADDR="	+ sock.getReuseAddress()
				+ "),(SO_LINGER="	+ sock.getSoLinger() // TIME_WAIT�ɂȂ炸��CLOSE
				+ "),(TraficClass="	+ sock.getTrafficClass()
				+ ")");
		
			// �N���C�A���g Socket ���� Stream ���擾
			InputStream		in		= sock.getInputStream();
			OutputStream	out		= sock.getOutputStream(); // �g���Ȃ�
			
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

		logger.fine("SocketHandler," + id + ",,�I��");
		
	}
}

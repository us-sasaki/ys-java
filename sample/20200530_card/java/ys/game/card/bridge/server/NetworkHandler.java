package ys.game.card.bridge.net;

import java.io.*;
import java.net.*;

/**
 * �l�b�g���[�N�ڑ��ɂ�����T�[�o�[�f�[�������p�b�N����N���X�ł��B
 * �T�|�[�g����Ă���l�b�g���[�N�R�}���h�̌`���͎��̒ʂ�ł��B<BR>
 *<PRE>
 * message := # | Command | <space> | Args | <LF>
 *
 * Command |                 ���e
 *---------+--------------------------------------------
 * PING    | �l�b�g���[�N���ڑ�����Ă��邩�m�F
 * PONG    | PING�ɑ΂��鉞��
 * BOARD   | Board���e��ʒm����
 * HAND    | Hand���e��ʒm����
 * BID     | Bid ��ʒm����
 * PLAY    | Play ��ʒm����
 * CHAT    | �`���b�g���e��ʒm����
 *---------+--------------------------------------------
 *
 * BOARD�R�}���h�`��
 *   BOARD <BoardNo><Seed><N-HandStr><E-HandStr><S-HandStr><W-HandStr><BidSeqStr><Contract>
 *         <Trick-1><Trick-2><....><Trick-current>
 *
 * HAND�R�}���h�`��
 *   HAND <Seat><HandStr>
 *     <Seat>   = "N", "E", "S", "W"
 *     <HandStr>= "SAKQJTHAKQDAKC432", "SAKQHAK????????" �Ȃ�
 *
 * BID�R�}���h�`��
 *   BID <Seat><BidStr>
 *     <Seat>   = "N", "E", "S", "W"
 *     <BidStr> = "P", "1NT", "X", "6C" �Ȃ�
 *
 * PLAY�R�}���h�`��
 *   PLAY <Seat><CardStr>
 *     <Seat>   = "N", "E", "S", "W"
 *     <CardStr>= "DA", "CT", "S3" �Ȃ�
 *
 * CHAT�R�}���h�`��
 *   CHAT <Message>
 *     <Message>= "�ق����" �Ȃ�
 *</PRE>
 *
 *
 * @version		making		19, August 2000
 * @author		Yusuke Sasaki
 */
public class NetworkHandler extends Thread {
	protected Socket			socket;
	protected PrintWriter		p;
	protected BufferedReader	br;
	protected volatile boolean	killed;
	
	protected PingListener		pingObserver;
	protected PongListener		pongObserver;
	protected BoardListener		boardObserver;
	protected HandListener		handObserver;
	protected BidListener		bidObserver;
	protected PlayListener		playObserver;
	protected ChatListener		chatObserver;
	
/*-------------
 * Constructor
 */
	public NetworkHandler(Socket sock) {
		socket	= sock;
		p		= new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "SJIS"));
		br		= new BufferedReader(new InputStreamReader(sock.getInputStream(), "SJIS"));
		killed	= false;
	}
	
/*-----------------------
 * implements (Runnable)
 */
	public void run() {
		
		while (true) {
			try {
				// �܂��A��s�ǂݍ���
				String input = br.readLine();
				if (!input.startsWith("#"))
					throw new IOException("���b�Z�[�W�� # �ŊJ�n����Ă��܂���B");
				int index = input.indexOf(' ');
				if (index == -1) index = input.length();
				
				// �R�}���h�AARG ���擾����
				String command	= input.substring(1, index);
				String args		= input.substring(index + 1);
				
				// �R�}���h�̎�ނɂ�镪��
				if (command.equals("PING")) pingImpl(args);
				else if (command.equals("PONG")) pongImpl(args);
				else if (command.equals("BOARD")) boardImpl(args);
				else if (command.equals("HAND")) handImpl(args);
				else if (command.equals("BID")) bidImpl(args);
				else if (command.equals("PLAY")) playImpl(args);
				else if (command.equals("CHAT")) chatImpl(args);
			} catch (IOException e) {
				// �ǂ�Ȉُ킪���蓾��̂�?
			}
			if (killed) break;
		}
		try {
			socket.close();
		} catch (IOException ignored) {
		}
		try {
			p.close();
		} catch (IOException ignored) {
		}
		try {
			br.close();
		} catch (IOException ignored) {
		}
	}
	
/*------------------
 * instance methods
 */
	public void addPingListener(PingListener listener) {
		pingObserver = listener;
	}
	
	public void addPongListener(PongListener listener) {
		pongObserver = listener;
	}
	
	public void addBoardListener(BoardListener listener) {
		boardObserver = listener;
	}
	
	public void addHandListener(HandListener listener) {
		handObserver = listener;
	}
	
	public void addBidListener(BidListener listener) {
		bidObserver = listener;
	}
	
	public void addPlayListener(PlayListener listener) {
		playObserver = listener;
	}
	
	public void addChatListener(ChatListener listener) {
		chatObserver = listener;
	}
	
	/**
	 * PING �R�}���h��M�C�x���g�𔭐����܂��B
	 */
	public void pingImpl(String args) {
		if (pingObserver != null)
			pingObserver.pingNotified(new PingEvent(args));
	}
	
	/**
	 * PONG �R�}���h��M�C�x���g�𔭐����܂��B
	 */
	public void pongImpl(String args) {
		if (pongObserver != null)
			pongObserver.pongNotified(new PongEvent(args));
	}
	
	/**
	 * BOARD �R�}���h��M�C�x���g�𔭐����܂��B
	 */
	public void boardImpl(String args) {
		if (boardObserver != null)
			boardObserver.boardNotified(new BoardEvent(args));
	}
	
	/**
	 * HAND �R�}���h��M�C�x���g�𔭐����܂��B
	 */
	public void handImpl(String args) {
		if (handObserver != null)
			handObserver.handNotified(new HandEvent(args));
	}
	
	/**
	 * BID �R�}���h��M�C�x���g�𔭐����܂��B
	 */
	public void bidImpl(String args) {
		if (bidObserver != null)
			bidObserver.bidNotified(new BidEvent(args));
	}
	
	/**
	 * PLAY �R�}���h��M�C�x���g�𔭐����܂��B
	 */
	public void playImpl(String args) {
		if (playObserver != null)
			playObserver.playNotified(new PlayEvent(args));
	}
	
	/**
	 * CHAT �R�}���h��M�C�x���g�𔭐����܂��B
	 */
	public void chatImpl(String args) {
		if (chatObserver != null)
			chatObserver.chatNotified(new ChatEvent(args));
	}
	
	/**
	 * ���̃��\�b�h���ĂԂ��Ƃɂ���āA���̃X���b�h����~���܂��B
	 * �l�b�g���[�N�ڑ��͐ؒf����܂��B
	 */
	public void disconnect() {
		killed = true;
		sock.close(); // ����ɂ���� readLine() ���u���b�N���������
//		this.interrupt();
	}
}

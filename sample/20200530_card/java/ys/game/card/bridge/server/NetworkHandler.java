package ys.game.card.bridge.net;

import java.io.*;
import java.net.*;

/**
 * ネットワーク接続におけるサーバーデーモンをパックするクラスです。
 * サポートされているネットワークコマンドの形式は次の通りです。<BR>
 *<PRE>
 * message := # | Command | <space> | Args | <LF>
 *
 * Command |                 内容
 *---------+--------------------------------------------
 * PING    | ネットワークが接続されているか確認
 * PONG    | PINGに対する応答
 * BOARD   | Board内容を通知する
 * HAND    | Hand内容を通知する
 * BID     | Bid を通知する
 * PLAY    | Play を通知する
 * CHAT    | チャット内容を通知する
 *---------+--------------------------------------------
 *
 * BOARDコマンド形式
 *   BOARD <BoardNo><Seed><N-HandStr><E-HandStr><S-HandStr><W-HandStr><BidSeqStr><Contract>
 *         <Trick-1><Trick-2><....><Trick-current>
 *
 * HANDコマンド形式
 *   HAND <Seat><HandStr>
 *     <Seat>   = "N", "E", "S", "W"
 *     <HandStr>= "SAKQJTHAKQDAKC432", "SAKQHAK????????" など
 *
 * BIDコマンド形式
 *   BID <Seat><BidStr>
 *     <Seat>   = "N", "E", "S", "W"
 *     <BidStr> = "P", "1NT", "X", "6C" など
 *
 * PLAYコマンド形式
 *   PLAY <Seat><CardStr>
 *     <Seat>   = "N", "E", "S", "W"
 *     <CardStr>= "DA", "CT", "S3" など
 *
 * CHATコマンド形式
 *   CHAT <Message>
 *     <Message>= "ほげらり" など
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
				// まず、一行読み込む
				String input = br.readLine();
				if (!input.startsWith("#"))
					throw new IOException("メッセージが # で開始されていません。");
				int index = input.indexOf(' ');
				if (index == -1) index = input.length();
				
				// コマンド、ARG を取得する
				String command	= input.substring(1, index);
				String args		= input.substring(index + 1);
				
				// コマンドの種類による分岐
				if (command.equals("PING")) pingImpl(args);
				else if (command.equals("PONG")) pongImpl(args);
				else if (command.equals("BOARD")) boardImpl(args);
				else if (command.equals("HAND")) handImpl(args);
				else if (command.equals("BID")) bidImpl(args);
				else if (command.equals("PLAY")) playImpl(args);
				else if (command.equals("CHAT")) chatImpl(args);
			} catch (IOException e) {
				// どんな異常があり得るのか?
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
	 * PING コマンド受信イベントを発生します。
	 */
	public void pingImpl(String args) {
		if (pingObserver != null)
			pingObserver.pingNotified(new PingEvent(args));
	}
	
	/**
	 * PONG コマンド受信イベントを発生します。
	 */
	public void pongImpl(String args) {
		if (pongObserver != null)
			pongObserver.pongNotified(new PongEvent(args));
	}
	
	/**
	 * BOARD コマンド受信イベントを発生します。
	 */
	public void boardImpl(String args) {
		if (boardObserver != null)
			boardObserver.boardNotified(new BoardEvent(args));
	}
	
	/**
	 * HAND コマンド受信イベントを発生します。
	 */
	public void handImpl(String args) {
		if (handObserver != null)
			handObserver.handNotified(new HandEvent(args));
	}
	
	/**
	 * BID コマンド受信イベントを発生します。
	 */
	public void bidImpl(String args) {
		if (bidObserver != null)
			bidObserver.bidNotified(new BidEvent(args));
	}
	
	/**
	 * PLAY コマンド受信イベントを発生します。
	 */
	public void playImpl(String args) {
		if (playObserver != null)
			playObserver.playNotified(new PlayEvent(args));
	}
	
	/**
	 * CHAT コマンド受信イベントを発生します。
	 */
	public void chatImpl(String args) {
		if (chatObserver != null)
			chatObserver.chatNotified(new ChatEvent(args));
	}
	
	/**
	 * このメソッドを呼ぶことによって、このスレッドが停止します。
	 * ネットワーク接続は切断されます。
	 */
	public void disconnect() {
		killed = true;
		sock.close(); // これによって readLine() がブロック解除される
//		this.interrupt();
	}
}

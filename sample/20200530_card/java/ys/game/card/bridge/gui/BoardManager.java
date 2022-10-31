package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;

/**
 * PlayMain を整理して Board を進めるオブジェクトを作成することとする。
 * このオブジェクトはアプレットで使用されることを前提とする。
 * 将来的な GUI オプション、プレイ内容制御機能を提供させたい。
 * とりあえず、基本機能のみを実装する。
 *
 * 将来的に、
 * GUI オプション、プレイ内容制御機能としては、次を含められるようにしたい
 * ・プレイナビゲーション(解説。あるイベントですみれが説明する。)
 * ●中断
 * ●ダブルダミーモード
 * ・視点の設定
 * ・部分的オープンモード
 * ・カードの裏の模様設定
 * ・点滅時間など、wait の設定
 * ・Undo
 * ・これらの機能の有効化、無効化
 */
public class BoardManager implements MouseListener, ActionListener, Runnable {

/*-----------
 * Constants
 */
	/** interrupt() の理由を示す定数で、中断を表します */
	static final int		QUIT			= 1;
	
	static final int		PLAYER_CHANGE	= 2;
	
/*--------------------
 * instance variables
 */
	/**
	 * 親 Container で、表示される場所である。
	 */
	protected Container		display;
	
	/**
	 * 表示エリアに貼り付けられる BridgeField である
	 */
	protected BridgeField	field;
	
	/**
	 * 進行対象となるボード
	 */
	protected GuiedBoard	board;
	
	/**
	 * プレイヤーアルゴリズム
	 */
	protected Player[]		player;
	
	protected Button		quit;
	protected Thread		runner;
	protected int			interruptReason;
	
	protected YesNoDialog	confirmDialog;
	
/*-------------
 * Constructor
 */
	public BoardManager(Container display) {
		player = new Player[4];
		
		this.display	= display;
		
		// 画面の大枠である BridgeField を設定する。
		field	= new BridgeField(display);
		
		board = new GuiedBoard(new BoardImpl(1));
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// レイアウト設定
		//
//		display.setLayout(null);
		
		// BridgeField を親 Container に追加する
		display.add(field.getCanvas());
		field.getCanvas().requestFocus();
		
		// スレッドを開始する
		runner = new Thread(this);
		runner.start();
	}
	
/*----------------------------
 * implements (MouseListener)
 */
	/**
	 * マウスがクリックされた場合、このオブジェクトの wait() を開放します。
	 */
	public void mouseClicked(MouseEvent me) {
		synchronized (this) {
			notifyAll();
		}
	}
	
	public void mousePressed(MouseEvent me) { }
	public void mouseReleased(MouseEvent me) { }
	public void mouseEntered(MouseEvent me) { }
	public void mouseExited(MouseEvent me) { }
	
/*-----------------------------
 * implements (ActionListener)
 */
	public void actionPerformed(ActionEvent e) {
	}
	
/*--------------
 * 設定メソッド
 */
	/**
	 * 初期化を行います。
	 */
	public void init() {
	}
	
	/**
	 * init()と対になるメソッド
	 */
	public void destroy() {
	}
	
	/**
	 * new と対になるメソッド
	 */
	public void dispose() {
		if (field != null)
			field.removeEntity(board);
		if (confirmDialog != null) confirmDialog.disposeDialog();
		if (player != null) {
			for (int i = 0; i < player.length; i++) {
				if ((player[i] != null)&&(player[i] instanceof HumanPlayer)) {
					((HumanPlayer)player[i]).dispose();
				}
			}
		}
		
		if (runner != null) runner = null;
	}
	
	/**
	 * プレイを中断します
	 */
	public void quit() {
		if (runner != null) {
			interruptReason |= QUIT;
			runner.interrupt();
		}
	}
	
	/**
	 * この BoardManager GUI を利用する HumanPlayer のインスタンスを作成します。
	 *
	 * HumanPlayerの破棄のタイミングは誰がしっているのか？？
	 * finalize() のみと思われる。
	 */
	public HumanPlayer getHumanPlayerInstance() {
		if (field == null) return null;
		return new HumanPlayer(board, field, Board.NORTH);
	}
	
	/**
	 * プレイヤーを設定します。
	 * 設定されたプレイヤーインスタンスの setBoard(), setMySeat() が自動的に呼ばれます。
	 */
	public void setPlayer(Player p, int seat) {
		p.setMySeat(seat);
		p.setBoard(board);
		
		player[seat] = p;
		
		interruptReason |= PLAYER_CHANGE;
		runner.interrupt();
	}
	
	public GuiedBoard getBoard() {
		return board;
	}
	
/*------------
 * ボード進行
 */
	/**
	 * board を進行させます。
	 */
	public void run() {
		while (true) {
			Thread.interrupted(); // interruptステータスクリア
			
			try {
				deal();
				bid();
				play();
				scoring();
			} catch (InterruptedBridgeException e) {
				// 中断ボタンが押され、終了が選択された場合
				field.removeSpot();
				field.repaint();
				break;
			}
		}
	}
	
	protected void deal() {
		board.reset(1);
		board.deal();
	}
	
	protected void bid() {
		// 適当に１Ｓとなったことにしておく。
		board.setContract(new Bid(Bid.BID, 1, Bid.SPADE), Board.EAST);
	}
	
	/**
	 * メインループ
	 */
	protected void play() throws InterruptedBridgeException {
		while (true) {
			//
			// Spot を指定する
			//
			field.setSpot(board.getTurn());
			field.repaint();
			
			Object c = null;
			while (c == null) {
				try {
					Player p = null;
					// player が null の間、ブロックする
					synchronized (player) {
						while ( (p = player[board.getPlayer()]) == null) {
							player.wait(); // may throw InterruptedException();
						}
					}
					c = p.play(); // ブロックする
				} catch (InterruptedException e) {
					if ((interruptReason & QUIT) != 0) {
						interruptReason ^= QUIT;
						if (confirmQuit()) throw new InterruptedBridgeException();
					}
					// PLAYER_CHANGE の場合、もう一度
					if ((interruptReason & PLAYER_CHANGE) != 0)
						interruptReason ^= PLAYER_CHANGE;
				}
			}
			board.play(c);
			field.repaint();
			
			if (board.getStatus() == Board.SCORING) break;
		}
	}
	
	/**
	 *
	 */
	protected void scoring() {
	}
	
	/**
	 * 再描画を行わせます。
	 */
	public void repaint() {
		if (field != null) field.repaint();
	}
	
	/**
	 * 中断ボタンが押されたときに表示する、中断確認ダイアログ表示処理
	 */
	protected boolean confirmQuit() {
		try {
			confirmDialog = new YesNoDialog(
								field.getCanvas(),
								"このボードを破棄して中断しますか？");
			confirmDialog.show();
			boolean yes = confirmDialog.isYes();
			confirmDialog.disposeDialog();
			confirmDialog = null;
			return yes;
		} catch (Exception e) {
			// ブラウザの強制終了の場合
			return true;
		}
	}
	
	/**
	 * クリックされるまで待ちます。
	 * このメソッドは、サブクラスでナビゲーションなどに使用することを期待しています。
	 * 本クラス内では使用しません。
	 * interrupt() メソッドがコールされた場合、InterruptedException がスローされます。
	 */
	protected void waitClick() throws InterruptedException {
		synchronized (this) {
			Canvas canvas = field.getCanvas();
			canvas.addMouseListener(this);
			try {
				wait();
				canvas.removeMouseListener(this);
			} catch (InterruptedException e) {
				canvas.removeMouseListener(this);
				throw new InterruptedException();
			}
		}
	}
	
}

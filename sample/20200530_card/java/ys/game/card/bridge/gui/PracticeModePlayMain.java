package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.ta.ReadAheadPlayer;

/**
 * 練習モードのメインプログラムです。
 * PlayMain に対して継承を使っています。
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public class PracticeModePlayMain extends PlayMain {
	protected static final int[] VUL = { Board.VUL_NONE, Board.VUL_NS, Board.VUL_EW, Board.VUL_BOTH, Board.VUL_NS, Board.VUL_EW, Board.VUL_BOTH, Board.VUL_NONE };
	
	protected static final int BOARDS = 4;	// ８以下に指定する必要がある
	
	protected GuiedBoard[]	boards	= new GuiedBoard[BOARDS];
	protected int			boardNo;
	protected Problem		prob	= new RandomProblem4();
	protected int			totalScore;
	protected boolean		finished = false;
	protected String		thinker;
	
	// 2002/10/5 記録モードのための追加
	// ダブルダミーにすると無効になる
	// 途中状態でも無効
//	protected boolean		doubledummied = false;
//	protected boolean		recordIsValid = false;
	
//	protected YesNoDialog	retry;
	
/*-------------
 * Constructor
 */
	public PracticeModePlayMain(Container display) {
		super(display);
		this.thinker = "SimplePlayer2";
	}
	
	public PracticeModePlayMain(Container display, String thinker) {
		super(display);
		if (thinker == null) this.thinker = "SimplePlayer2";
		else this.thinker = thinker;
	}
	
	public boolean isFinished() { return finished; }
	
/*-----------
 * overrides
 */
	public void addProblem(Problem p) {
		throw new IllegalArgumentException("addProblem() は PracticeModePlayMain ではサポートしていません");
	}
	
	/**
	 * Board の初期化を行います。
	 */
	public void start() {
		mainThread = Thread.currentThread();
		totalScore = 0;
		Score score = new Score();
		
		for (boardNo = 0; boardNo < BOARDS; boardNo++) {
			if (display instanceof AppletCardImageHolder) {
				((AppletCardImageHolder)display).setBackImage(boardNo%4);
			}
			makeNewhand();
			main();
			super.stop();
			
			if (boards[boardNo] != null) {
				if (boards[boardNo].getStatus() != Board.SCORING) break;
			}
		}
		
		// 2002/6/29 終わった後、画面再描画を行っていなかったため追加
		// add start
		field.repaint();
		// add end
		
		if ( (boardNo == BOARDS)&&(boards[boardNo-1].getStatus() == Board.SCORING) ) {
			finished = false;
		} else {
			confirmDialog = new YesNoDialog(
								field.getCanvas(),
								"練習用アプレットを終了しますか？");
			confirmDialog.show();
			finished = confirmDialog.isYes();
			confirmDialog.dispose();
		}
	}
	
	public void stop() {
		super.stop();
//		if (retry != null) retry.disposeDialog();
		if (field != null) {
			for (int i = 0; i < boards.length; i++) {
				field.removeEntity(boards[i]);
			}
//			field.dispose();
		}
		for (int i = 0; i < boards.length; i++) {
			boards[i] = null;
		}
	}
	
	public void dispose() {
//System.out.println("practiceModePlayMain dispose() called field = "+field);
		if (field != null) field.dispose();
	}
	
	/**
	 * ダイアログで新しいハンドを選択したときの処理です。
	 */
	protected void makeNewhand() {
		prob.start();
		
		board = new GuiedBoard(new BoardImpl(Board.NORTH, VUL[boardNo]));
		boards[boardNo] = board;
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player 設定
		//
		player = new Player[4];
		player[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
		if (thinker.equals("DoubleDummyPlayer"))
			player[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
		else
			player[Board.EAST ] = new SimplePlayer2(board, Board.EAST);
		player[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
		if (thinker.equals("DoubleDummyPlayer"))
			player[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.getOpeningLead());
		else
			player[Board.WEST ] = new SimplePlayer2(board, Board.WEST, prob.getOpeningLead());
		
		//
		// ディール
		//
		Packet[] hand = prob.getHand();
		
		board.deal(hand);
		field.repaint();
		
		//
		// ビッドを行う
		//
		board.setContract(prob.getContract(), Board.SOUTH);
		
		//
		// タイトルを設定する
		//
		board.setName("Board " + (boardNo+1));
	}
	
	/**
	 * 始めのすみれによる説明を表示する
	 */
	protected void explain() throws InterruptedBridgeException {
		
		sumire = new Explanation(field, prob.getDescription());
		contractString = prob.getContractString();
		
		field.addEntity(sumire);
		field.repaint();
		try {
			waitClick(); // クリックを待つ。 InterruptedException をスローするかも
		} catch (InterruptedException e) {
			if (confirmQuit()) {
				field.removeEntity(sumire);
				throw new InterruptedBridgeException();
			}
		}
		field.removeEntity(sumire);
		field.repaint();
	}
	
	/**
	 * 元のハンドの表示、スコアの表示を行う
	 */
	protected void displayScore() throws InterruptedBridgeException {
		field.removeSpot();
		field.repaint();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			if (confirmQuit()) throw new InterruptedBridgeException();
		}
		
		//
		// カードをもう一度表示する
		//
		Packet[] original = calculateOriginalHand(board);
		for (int i = 0; i < 4; i++) {
			GuiedPacket hand = (GuiedPacket)(board.getHand(i));
			for (int j = 0; j < original[i].size(); j++) {
				Card c = original[i].peek(j);
				c.turn(true);
				hand.add(c);
			}
		}
		board.layout();
		for (int i = 0; i < 4; i++) {
			GuiedPacket hand = (GuiedPacket)(board.getHand(i));
			hand.arrange();
			hand.layout();
		}
		
		//
		// スコア表示
		//
//		Score score = new Score();
//System.out.println(Converter.serialize(board));
		String msg = "結果：" + contractString + "  ";
		String msg2;
		// メイク数
		int win		= countWinners(board);
		int up		= win - board.getContract().getLevel() - 6;
		int make	= win - 6;
		
		if (up >= 0) {
			// メイク
			msg += String.valueOf(make) + "メイク";
			msg2 = "おめでとう！！";
		} else {
			// ダウン
			msg += String.valueOf(-up) + "ダウン";
			msg2 = "残念。もう一度がんばって！";
		}
		
		msg += "("+win+"トリック)\nN-S側のスコア："+Score.calculate(board, Board.SOUTH);
		
		totalScore += Score.calculate(board, Board.SOUTH); // override 追加分
		msg += "\nスコア累積："+totalScore;
		
		msg += "\n" + msg2;
		
		sumire = new Explanation(field, msg);
		if (up >= 0) 
			sumire.animate(Explanation.DELIGHTED);
		else
			sumire.animate(Explanation.SAD);
		
		field.addEntity(sumire);
		field.repaint();
		
		try {
			waitClick();
		} catch (InterruptedException e) {
			if (confirmQuit()) {
				field.removeEntity(sumire);
				clearHands();
				throw new InterruptedBridgeException();
			}
		}
		field.removeEntity(sumire);
		field.repaint();
		clearHands();
	}
	
	protected boolean confirmQuit() {
		try {
			confirmDialog = new YesNoDialog(
								field.getCanvas(),
								"これまでのプレイ結果を破棄して中断しますか？");
			confirmDialog.show();
			boolean yes = confirmDialog.isYes();
			confirmDialog.dispose();
			return yes;
		} catch (Exception e) {
//			e.printStackTrace();
			return true;
		}
	}
	
	// IEのJavaVM 4.0 対応
	private static int countWinners(Board board) {
		return BridgeUtils.countDeclarerSideWinners(board);
	}
	
	public Board getBoard(int boardNo) {
		return boards[boardNo];
	}

}

package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;

/**
 * 指定されたボード(ビッド、プレイ履歴)にしたがってビデオ再生する
 * コンピュータプレイヤーです。
 *
 * @version		making		6, May 2000
 * @author		Yusuke Sasaki
 */
public class VideoPlayer extends Player {
	protected Board scenario;
	
/*-------------
 * Constructor
 */
	public VideoPlayer(Board board, Board scenario, int seat) {
		setBoard(board);
		setMySeat(seat);
		
		this.scenario = scenario;
	}
	
/*------------
 * implements
 */
	/**
	 * ビッド履歴から現在の状態でのビッドを取得し、返却します。(未実装)
	 *
	 * @return		パス
	 */
	public Bid bid() throws InterruptedException {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * プレイ履歴から現在の状態でのプレイを取得し、返却します。
	 *
	 * @return		ランダムなプレイ
	 */
	public Card draw() throws InterruptedException {
		Thread.sleep(400); // 考えた振り
		
		int trickCount = getBoard().getTricks();
		int num = getBoard().getTrick().size();
		
		Trick tr = scenario.getPlayHistory().getTrick(trickCount);
		
		return tr.peek(num);
	}
	
}

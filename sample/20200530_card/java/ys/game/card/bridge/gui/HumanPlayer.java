package ys.game.card.bridge.gui;

import ys.game.card.Card;
import ys.game.card.Packet;

import ys.game.card.bridge.*;

/**
 * ＧＵＩ操作でプレイを行うヒューマンプレイヤーです。
 *
 * @version		making		7, May 2000
 * @author		Yusuke Sasaki
 */
public class HumanPlayer extends Player {
	
	protected BridgeField	field;
	protected MessageDialog	messageDialog;
/*
 * 
 */
	public HumanPlayer(Board board, BridgeField field, int seat) {
		setBoard(board);
		setMySeat(seat);
		this.field = field;
	}
	
/*------------
 * implements
 */
	/**
	 * ビッドはまだ実装されていません。自動的に必ずパスします。
	 *
	 * @return		パス
	 */
	public Bid bid() throws InterruptedException {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * ダブルクリックされたカードがルール上可能なプレイであったら、そのカードを返却します。
	 *
	 * @return		ランダムなプレイ
	 */
	public Card draw() throws InterruptedException {
		Board board = getBoard();
		Card clicked = null;
		while (true) {
			clicked = field.waitCardSelect();
			if (clicked != null) {
				if (board.allows(clicked)) break;
				if (getHand().contains(clicked)) {
					messageDialog = new MessageDialog(field.getCanvas(), "リードと同じスートを選択してください");
					messageDialog.show();
					messageDialog.dispose();
				} else {
					int turn = getBoard().getTurn();
					String seat;
					switch (turn) {
					case Board.NORTH:
						seat = "North"; break;
					case Board.EAST:
						seat = "East"; break;
					case Board.SOUTH:
						seat = "South"; break;
					case Board.WEST:
						seat = "West"; break;
					default:
						seat = "??";
					}
					
					messageDialog = new MessageDialog(field.getCanvas(), seat + "のハンドからカードを選択してください");
					messageDialog.show();
					messageDialog.dispose();
				}
			}
		}
		
		return clicked;
	}
	
	public void dispose() {
		if (messageDialog != null) messageDialog.dispose();
	}
}

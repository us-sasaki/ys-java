package ys.game.card.bridge.gui;

import java.awt.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * GUIed の PlayHistory で、Trick のインスタンスとして GuiedTrick を常に生成します。
 * PlayHistory そのものは、GUI表現を持っていません。
 *
 * @version		a-release		4, January 2001
 * @author		Yusuke Sasaki
 */
public class GuiedPlayHistory extends PlayHistoryImpl {
	
	public GuiedPlayHistory() {
		hand		= new Packet[4];
		trick		= new Trick[13];
		trickCount	= 0;
		trump		= -1;
	}
	
	/**
	 * 指定された PlayHistory と同一内容の GuiedPlayHistory のインスタンスを
	 * 新規に生成します。
	 * Trick, Hand の内容については、Guied 化されます。
	 *
	 * @param		src		コピー元の PlayHistory
	 */
	public GuiedPlayHistory(PlayHistory src) {
		this();
		
		//
		// Hand のコピー
		//
		Packet[] srcHand = src.getHand();
		int n = srcHand.length; // srcHand != null
		
		for (int i = 0; i < n; i++) {
			if (srcHand[i] instanceof GuiedPacket)
				hand[i] = (GuiedPacket)srcHand[i];
			else
				hand[i] = new GuiedPacket(srcHand[i]);
		}
		
		//
		// Trick のコピー
		//
		Trick[] srcTrick = src.getAllTricks();
		if (srcTrick != null) {
			for (int i = 0; i < srcTrick.length; i++) {
				Trick tr = srcTrick[i];
				if (tr instanceof GuiedTrick)
					trick[i] = (GuiedTrick)tr;
				else
					trick[i] = new GuiedTrick(tr);
			}
			trickCount = srcTrick.length - 1;
			if ( src.isFinished() ) trickCount = 13;
		}
		
		//
		// trump のコピー
		//
		trump = src.getTrump();
	}
	
/*----------------------------
 * Overrides(PlayHistoryImpl)
 */
	public void setHand(Packet[] hand) {
		if ( (trick[0] != null)&&(trick[0].size() > 0) )
			throw new IllegalStatusException("すでにプレイが開始されているため" +
											" setHand は行えません。");
		if (hand.length != 4)
			throw new IllegalArgumentException("４人分のハンドが指定されていません。");
		
		for (int i = 0; i < 4; i++) {
			if (hand[i].size() != 13)
				throw new IllegalArgumentException("ハンド"+i+"のカード枚数が異常です。13枚指定して下さい。");
		}
		
		int n = hand.length; // srcHand != null
		
		for (int i = 0; i < n; i++) {
			if (hand[i] instanceof GuiedPacket)
				this.hand[i] = (GuiedPacket)hand[i];
			else
				this.hand[i] = new GuiedPacket(hand[i]);
		}
	}
	
	public void play(Card p) {
		if (!(p instanceof GuiedCard))
			p = new GuiedCard(p);
			
		super.play(p);
	}
	
	protected Trick createTrick(int leader, int trump) {
		return new GuiedTrick(leader, trump);
	}

}

package ys.game.card.bridge.gui;

import java.awt.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * Trick の GUI 表現です。
 * 抽象的な Trick に追加される概念として、基準となる席(視点, 方向)があります。
 *
 * @version		a-release		5, May 2000
 * @author		Yusuke Sasaki
 */
public class GuiedTrick extends GuiedPacket implements Trick {
	
	public static final int WIDTH	= 300;
	public static final int HEIGHT	= 200;
	
/*-------------
 * Constructor
 */
	public GuiedTrick(int leader, int trump) {
		impl = new TrickImpl(leader, trump);
		
		direction = Entity.UPRIGHT; // NORTH が上
		setSize(WIDTH, HEIGHT);
		setLayout(new TrickLayout());
	}
	
	public GuiedTrick(Trick src) {
		super();
		if (src instanceof GuiedTrick) {
			impl = ((GuiedTrick)src).impl;
System.out.println("Trick is already an instance of GuiedTrick.");
		} else {
			if (src instanceof TrickImpl) impl = (TrickImpl)src;
			else impl = new TrickImpl(src);
			
			setTrickGui();
			
			direction = Entity.UPRIGHT; // NORTH が上
			setSize(WIDTH, HEIGHT);
			setLayout(new TrickLayout());
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * このオブジェクトが持っている PacketImpl のインスタンスが保持する
	 * Card の内容を Entities としても保持します。同時に、Cardのインスタンスを
	 * すべて GuiedCard に変更します。
	 */
	private void setTrickGui() {
		int size = impl.size();
		entity = new Entity[impl.size()];
		entities = impl.size();
		
		for (int i = 0; i < size; i++) {
			Card c = impl.draw(i);
			GuiedCard card;
			if (c instanceof GuiedCard) card = (GuiedCard)c;
			else card = new GuiedCard(c);
			impl.insertAt(card, i);
			entity[i] = card; //insertEntityAt(card, i);
		}
		layout();
	}
	
/*-------------------
 * overrides(Entity)
 */
	/**
	 * GuiedTrick では、方向によって縦横の大きさが変化しないため、
	 * オーバーライドしています。
	 *
	 * @param		direction		設定する方向
	 *								(0..NORTHが上, 1..EASTが上, 
	 *									2..SOUTHが上、3..WESTが上)
	 */
	public void setDirection(int direction) {
		if ( (direction < 0)||(direction > 3) )
			throw new IllegalArgumentException("setDirection で指定された方向の値" +
											direction + "は無効です。");
		this.direction = direction;
		
		layout();
	}
	
/*---------------------
 * Overrides(Entities)
 */

/*-------------------
 * Implements(Trick)
 *
 * すべて impl に対して委譲を行います。
 */
	public int getLeader() { return ((TrickImpl)impl).getLeader(); }
	public int getTrump() { return ((TrickImpl)impl).getTrump(); }
	public int getTurn() { return ((TrickImpl)impl).getTurn(); }
	public boolean isFinished() { return ((TrickImpl)impl).isFinished(); }
	public Card getLead() { return ((TrickImpl)impl).getLead(); }
	public int getWinner() { return ((TrickImpl)impl).getWinner(); }
	public Card getWinnerCard() { return ((TrickImpl)impl).getWinnerCard(); }
}

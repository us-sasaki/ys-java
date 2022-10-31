package ys.game.card.bridge.gui;

import java.awt.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * Trick �� GUI �\���ł��B
 * ���ۓI�� Trick �ɒǉ������T�O�Ƃ��āA��ƂȂ��(���_, ����)������܂��B
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
		
		direction = Entity.UPRIGHT; // NORTH ����
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
			
			direction = Entity.UPRIGHT; // NORTH ����
			setSize(WIDTH, HEIGHT);
			setLayout(new TrickLayout());
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * ���̃I�u�W�F�N�g�������Ă��� PacketImpl �̃C���X�^���X���ێ�����
	 * Card �̓��e�� Entities �Ƃ��Ă��ێ����܂��B�����ɁACard�̃C���X�^���X��
	 * ���ׂ� GuiedCard �ɕύX���܂��B
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
	 * GuiedTrick �ł́A�����ɂ���ďc���̑傫�����ω����Ȃ����߁A
	 * �I�[�o�[���C�h���Ă��܂��B
	 *
	 * @param		direction		�ݒ肷�����
	 *								(0..NORTH����, 1..EAST����, 
	 *									2..SOUTH����A3..WEST����)
	 */
	public void setDirection(int direction) {
		if ( (direction < 0)||(direction > 3) )
			throw new IllegalArgumentException("setDirection �Ŏw�肳�ꂽ�����̒l" +
											direction + "�͖����ł��B");
		this.direction = direction;
		
		layout();
	}
	
/*---------------------
 * Overrides(Entities)
 */

/*-------------------
 * Implements(Trick)
 *
 * ���ׂ� impl �ɑ΂��ĈϏ����s���܂��B
 */
	public int getLeader() { return ((TrickImpl)impl).getLeader(); }
	public int getTrump() { return ((TrickImpl)impl).getTrump(); }
	public int getTurn() { return ((TrickImpl)impl).getTurn(); }
	public boolean isFinished() { return ((TrickImpl)impl).isFinished(); }
	public Card getLead() { return ((TrickImpl)impl).getLead(); }
	public int getWinner() { return ((TrickImpl)impl).getWinner(); }
	public Card getWinnerCard() { return ((TrickImpl)impl).getWinnerCard(); }
}

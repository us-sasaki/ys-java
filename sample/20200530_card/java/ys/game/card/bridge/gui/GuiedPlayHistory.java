package ys.game.card.bridge.gui;

import java.awt.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * GUIed �� PlayHistory �ŁATrick �̃C���X�^���X�Ƃ��� GuiedTrick ����ɐ������܂��B
 * PlayHistory ���̂��̂́AGUI�\���������Ă��܂���B
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
	 * �w�肳�ꂽ PlayHistory �Ɠ�����e�� GuiedPlayHistory �̃C���X�^���X��
	 * �V�K�ɐ������܂��B
	 * Trick, Hand �̓��e�ɂ��ẮAGuied ������܂��B
	 *
	 * @param		src		�R�s�[���� PlayHistory
	 */
	public GuiedPlayHistory(PlayHistory src) {
		this();
		
		//
		// Hand �̃R�s�[
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
		// Trick �̃R�s�[
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
		// trump �̃R�s�[
		//
		trump = src.getTrump();
	}
	
/*----------------------------
 * Overrides(PlayHistoryImpl)
 */
	public void setHand(Packet[] hand) {
		if ( (trick[0] != null)&&(trick[0].size() > 0) )
			throw new IllegalStatusException("���łɃv���C���J�n����Ă��邽��" +
											" setHand �͍s���܂���B");
		if (hand.length != 4)
			throw new IllegalArgumentException("�S�l���̃n���h���w�肳��Ă��܂���B");
		
		for (int i = 0; i < 4; i++) {
			if (hand[i].size() != 13)
				throw new IllegalArgumentException("�n���h"+i+"�̃J�[�h�������ُ�ł��B13���w�肵�ĉ������B");
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

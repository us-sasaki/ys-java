package ys.game.card;

import java.util.Vector;

/**
 * ���̃N���X�́A�P�g�̃J�[�h��p�ӂ��郁�\�b�h������.
 * ����A�J�[�h�]���Ȃǂ֗̕��ȃN���X���\�b�h���C�u�����I�ȃN���X�ɂ�����.
 *
 * @version		a-release	23, April 2000
 * @author		Yusuke Sasaki
 */
public class PacketFactory {

	/** provideDeck() ���\�b�h�� Joker ���܂ރf�b�L�𐶐����邽�߂̒萔 */
	public static final boolean WITH_JOKER		= true;
	
	/** provideDeck() ���\�b�h�� Joker ���܂܂Ȃ��f�b�L�𐶐����邽�߂̒萔 */
	public static final boolean WITHOUT_JOKER	= false;
	
/*---------------
 * class methods
 */
	/**
	 * �P�g�̋�� Packet �𐶐����܂��B
	 * �{���\�b�h�́A�{�N���X�Ŏ�舵���f�t�H���g�� Packet �C���X�^���X���`���܂��B
	 */
	public static Packet newPacket() {
		return new PacketImpl();
	}
	
	/**
	 * �P����UnspecifiedCard�𐶐����܂��B
	 * �{���\�b�h�́A�{�N���X�Ŏ�舵���f�t�H���g�� Card �C���X�^���X���`���܂��B
	 */
	static CardImpl newCard() {
		return new CardImpl();
	}
	
	/**
	 * �w�肳�ꂽ�X�[�g�A�o�����[�����P���̃J�[�h�𐶐����܂��B
	 * �{���\�b�h�́A�{�N���X�Ŏ�舵���f�t�H���g�� Card �C���X�^���X���`���܂��B
	 */
	static CardImpl newCard(int suit, int value) {
		return new CardImpl(suit, value);
	}
	
	/**
	 * �P�g�� Unspecified Card ����Ȃ� Packet ��p�ӂ��܂��B
	 */
	public static Packet provideUnspecifiedDeck(boolean withJoker) {
		Packet set = newPacket();
		Packet ret = newPacket();
		for (int suit = 4; suit > 0; suit--)
			for (int value = 13; value > 0; value--) {
				CardImpl c = newCard(suit, value);
				c.turn(false);
				set.add(c);
				
				c = newCard();
				c.setHolder(set);
				ret.add(c);
			}
		
		if (withJoker) {
			CardImpl c = newCard(Card.JOKER, Card.JOKER);
			c.turn(false);
			set.add(c);
			
			c = newCard();
			c.setHolder(set);
			ret.add(c);
		}
		return ret;
	}
	
	/**
	 * �P�g�̃J�[�h��p�ӂ���. �W���[�J�[���܂ނ��ǂ����w��ł���.
	 */
	public static Packet provideDeck(boolean withJoker) {
		Packet set = newPacket();
		Packet ret = newPacket();
		for (int suit = 4; suit > 0; suit--)
			for (int value = 13; value > 0; value--) {
				CardImpl c = newCard(suit, value);
				c.setHolder(set);
				set.add(c);
				
				c = newCard(suit, value);
				c.setHolder(set);
				ret.add(c);
			}
		if (withJoker) {
			CardImpl c = newCard(Card.JOKER, Card.JOKER);
			c.setHolder(set);
			set.add(c);
			
			c = newCard(Card.JOKER, Card.JOKER);
			c.setHolder(set);
			ret.add(c);
		}
		return ret;
	}
	
	/**
	 * �w�肳�ꂽ pile �̃J�[�h�𓙂��� n �l�ɔz��܂��B
	 * �z����́Apile �̏ォ�珇�Ԃɔz��܂��B
	 * ���ʂ̃C���X�^���X�͎w�肳�ꂽ pile �̂��̂ƂȂ�܂��B
	 */
	public static Packet[] deal(Packet pile, int n) {
		if (n <= 0) return null; // error !!
		Packet[] result = new Packet[n];
		for (int i = 0; i < n; i++) result[i] = newPacket();
		int c = pile.size();
		for (int i = 0; i < c; i++) {
			Card card = pile.draw();
			result[i % n].add(card);
		}
		return result;
	}
	
	/**
	 * �p�C������A�J�[�h�𓯖������A�w�肵�� Packet �ɔz��܂��B
	 * �z��ꂽ�J�[�h�͒ǉ�����܂��B
	 *
	 * @param	pile	�J�[�h�̎R
	 * @param	hand	�z���. �Y�����̏��ɔz����.
	 * @param	begin	�z��͂��߂�Y����.
	 */
	public static void deal(Packet pile, Packet[] hand, int begin) {
		int n = hand.length;
		int c = pile.size();
		for (int i = 0; i < c; i++) {
			Card card = pile.draw();
			hand[(i + begin) % n].add(card);
		}
	}
	
	/**
	 * �J�[�h�𓙂����w�肵�� Packet �ɔz��. �z��ꂽ�J�[�h�͒ǉ������.
	 *
	 * @param	pile	�z��J�[�h�̎R
	 * @param	hand	�z���. �Y���� 0 ���珇�ɔz����.
	 */
	public static void deal(Packet pile, Packet[] hand) {
		deal(pile, hand, 0);
	}
	
	/**
	 * Packet ������w�肵���X�[�c�̃J�[�h�𒊏o����. �����Ȃ�.
	 */
	public static Packet getSubpacket(Packet hand, int suit) {
		Packet result = newPacket();
		int n = hand.size();
		for (int i = 0; i < n; i++) {
			Card c = hand.peek(i);
			if (c.getSuit() == suit) result.add(c);
		}
		return result;
	}
	
	/**
	 * Packet ���Ɏw�肵���X�[�c�̃J�[�h���������邩�J�E���g����.
	 * ����.
	 */
	public static int countCard(Packet hand, int suit) {
		int c = 0;
		int n = hand.size();
		for (int i = 0; i < n; i++)
			if (hand.peek(i).getSuit() == suit) c++;
		return c;
	}
	
}


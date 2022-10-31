package ys.game.card.bridge;

import ys.game.card.Card;

/**
 * �r�b�h�A�܂��̓R���g���N�g��\������N���X�ł��B
 *
 * @version		unit tested		17, April 2000
 * @author		Yusuke Sasaki
 */
public class Bid {
	
	/** kind (Bid) ��\���萔. */
	public static final int BID			= 0;
	
	/** kind (Pass) ��\���萔. */
	public static final int PASS		= 1;
	
	/** kind (Double) ��\���萔. */
	public static final int DOUBLE		= 2;
	
	/** kind (Redouble) ��\���萔. */
	public static final int REDOUBLE	= 3;
	
	/** bid suit (club) ��\���萔(=1). */
	public static final int CLUB		= Card.CLUB;	// = 1;
	
	/** bid suit (diamond) ��\���萔(=2). */
	public static final int DIAMOND		= Card.DIAMOND;	// = 2;
	
	/** bid suit (heart) ��\���萔(=3). */
	public static final int HEART		= Card.HEART;	// = 3;
	
	/** bid suit (spade) ��\���萔(=4). */
	public static final int SPADE		= Card.SPADE;	// = 4;
	
	/** bid suit (no trump) ��\���萔(=5). */
	public static final int NO_TRUMP	= 5;
	
/*--------------------
 * instance variables
 */
	/** Bid �̎��(Pass, Double �Ȃ�) */
	private int kind;
	
	/** Bid �̃��x��. kind == BID, DOUBLE, REDOUBLE �ŗL�� */
	private int level;
	
	/** Bid �� suit. kind == BID, DOUBLE, REDOUBLE �ŗL�� */
	private int suit;
	
/*-------------
 * Constructor
 */
	/**
	 * �w�肳�ꂽ�r�b�h�𐶐�����. �r�b�h�̓R���g���N�g���������߁A
	 * Double, Redouble �̂Ƃ��� level, suit �͎w�肵�Ȃ���΂Ȃ�Ȃ�.
	 *
	 */
	public Bid(int kind, int level, int suit) {
		if ((kind < BID)||(kind > REDOUBLE))
			throw new IllegalArgumentException("Illegal Bid kind : " + kind);
		if ((level < 0)||(level > 7))
			throw new IllegalArgumentException("Illegal level : " + level);
		if ((suit < 0)||(suit > NO_TRUMP))
			throw new IllegalArgumentException("Illegal suit : " + suit);
		this.kind  = kind;
		this.level = level;
		this.suit  = suit;
	}
	
	public Bid(int pass) {
		this(Bid.PASS, 0, 0);
		if (pass != Bid.PASS) throw new IllegalArgumentException();
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �r�b�h�̎�ނ𓾂�. �l�́ABid.BID, Bid.PASS, Bid.DOUBLE, Bid.REDOUBLE
	 * �̂����ꂩ�ł���. Bid.PASS �ȊO�ł� Level, Suit �̒l���擾�ł���.
	 *
	 * @return		�r�b�h�̎��(Bid.BID, Bid.PASS, Bid.DOUBLE, Bid.REDOUBLE)
	 */
	public int getKind() { return kind; }
	
	/**
	 * �r�b�h�̃��x��(1-7)�𓾂܂��B
	 *
	 * @return		�r�b�h�̃��x��(1-7)
	 */
	public int getLevel() { return level; }
	
	/**
	 * �g�����v�X�[�g�𓾂܂��B
	 *
	 * @return		�r�b�h���ꂽ�g�����v(Bid.NO_TRUMP, Bid.SPADE, �c�c)
	 */
	public int getSuit() { return suit; }
	
	/**
	 * �w�肳�ꂽ�R���g���N�g�̂��Ƃɐ錾�\�����肵�܂��B<BR>
	 * ��) [1C] < [1D] < [2C] < [2CX] < [2CXX] �ƂȂ�܂��B
	 * [pass] �͏�ɉ\(true)�ł��B
	 */
	public boolean isBiddableOver(Bid contract) {
		switch (kind) {
		
		case PASS:
			return true;
		
		case BID:
			if (contract == null) return true;
			if (level > contract.level) return true;
			if (level < contract.level) return false;
			if (suit > contract.suit) return true;
			return false;
			
		case DOUBLE:
			if (contract == null) return false;
			if (contract.kind != BID) return false;
			if (contract.suit != suit) return false;
			if (contract.level != level) return false;
			return true;
			
		case REDOUBLE:
			if (contract == null) return false;
			if (contract.kind != DOUBLE) return false;
			if (contract.suit != suit) return false;
			if (contract.level != level) return false;
			return true;
		
		default:
			throw new InternalError(
						"kind ���s���Ȓl" + kind + "�ɂȂ��Ă��܂�");
		}
		
	}
	
/*-----------
 * Overrides
 */
	public String toString() {
		switch (kind) {
			case BID:
				return "[" + level + " " +
							" C D H SNT".substring(suit*2-2, suit*2) + "  ]";
			case PASS:
				return "[pass  ]";
			
			case DOUBLE:
				return "[" + level + " " +
							" C D H SNT".substring(suit*2-2, suit*2) + "X ]";
				
			case REDOUBLE:
				return "[" + level + " " +
							" C D H SNT".substring(suit*2-2, suit*2) + "XX]";
				
			default:
				return "[? bid ]";
		}
	}
	
}

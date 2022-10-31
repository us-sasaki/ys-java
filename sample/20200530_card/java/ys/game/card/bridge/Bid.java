package ys.game.card.bridge;

import ys.game.card.Card;

/**
 * ビッド、またはコントラクトを表現するクラスです。
 *
 * @version		unit tested		17, April 2000
 * @author		Yusuke Sasaki
 */
public class Bid {
	
	/** kind (Bid) を表す定数. */
	public static final int BID			= 0;
	
	/** kind (Pass) を表す定数. */
	public static final int PASS		= 1;
	
	/** kind (Double) を表す定数. */
	public static final int DOUBLE		= 2;
	
	/** kind (Redouble) を表す定数. */
	public static final int REDOUBLE	= 3;
	
	/** bid suit (club) を表す定数(=1). */
	public static final int CLUB		= Card.CLUB;	// = 1;
	
	/** bid suit (diamond) を表す定数(=2). */
	public static final int DIAMOND		= Card.DIAMOND;	// = 2;
	
	/** bid suit (heart) を表す定数(=3). */
	public static final int HEART		= Card.HEART;	// = 3;
	
	/** bid suit (spade) を表す定数(=4). */
	public static final int SPADE		= Card.SPADE;	// = 4;
	
	/** bid suit (no trump) を表す定数(=5). */
	public static final int NO_TRUMP	= 5;
	
/*--------------------
 * instance variables
 */
	/** Bid の種類(Pass, Double など) */
	private int kind;
	
	/** Bid のレベル. kind == BID, DOUBLE, REDOUBLE で有効 */
	private int level;
	
	/** Bid の suit. kind == BID, DOUBLE, REDOUBLE で有効 */
	private int suit;
	
/*-------------
 * Constructor
 */
	/**
	 * 指定されたビッドを生成する. ビッドはコントラクトも示すため、
	 * Double, Redouble のときも level, suit は指定しなければならない.
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
	 * ビッドの種類を得る. 値は、Bid.BID, Bid.PASS, Bid.DOUBLE, Bid.REDOUBLE
	 * のいずれかである. Bid.PASS 以外では Level, Suit の値を取得できる.
	 *
	 * @return		ビッドの種類(Bid.BID, Bid.PASS, Bid.DOUBLE, Bid.REDOUBLE)
	 */
	public int getKind() { return kind; }
	
	/**
	 * ビッドのレベル(1-7)を得ます。
	 *
	 * @return		ビッドのレベル(1-7)
	 */
	public int getLevel() { return level; }
	
	/**
	 * トランプスートを得ます。
	 *
	 * @return		ビッドされたトランプ(Bid.NO_TRUMP, Bid.SPADE, ……)
	 */
	public int getSuit() { return suit; }
	
	/**
	 * 指定されたコントラクトのあとに宣言可能か判定します。<BR>
	 * 例) [1C] < [1D] < [2C] < [2CX] < [2CXX] となります。
	 * [pass] は常に可能(true)です。
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
						"kind が不正な値" + kind + "になっています");
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

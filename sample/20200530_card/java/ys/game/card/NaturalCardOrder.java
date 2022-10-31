package ys.game.card;
/*
 * NaturalCardOrder.java
 *
 * history
 *  00/1/27 ver 1.1 DummyHandLayout の実装のため、スートの優先順位を変更でき
 *                  るようにした
 */

/**
 * 通常のカードの並べ方
 *
 * @version		a-release	5, May 2000
 * @author		Yusuke Sasaki
 */
public class NaturalCardOrder implements CardOrder {
	
	/** Spade, Heart, Club, Diamondの順 */
	public static final int[] SUIT_ORDER_SPADE =
//										{ 0, 3, 4, 2, 1};
										{ 0, 2, 1, 3, 4 };
	//                                   Jo  C  D  H  S
	
	/** Heart, Club, Diamond, Spadeの順 */
	public static final int[] SUIT_ORDER_HEART =
										{ 0, 3, 2, 4, 1};
	
	/** Diamond, Spade, Heart, Clubの順 */
	public static final int[] SUIT_ORDER_DIAMOND =
										{ 0, 1, 4, 2, 3};
	
	/** Club, Diamond, Spade, Heartの順 */
	public static final int[] SUIT_ORDER_CLUB =
										{ 0, 4, 3, 1, 2};
	
	public static final int[] VALUE_ORDER
//		= { 1,   2,15,14,13,12,11,10, 9, 8, 7 ,  6,  5,  4};
		= { 15, 14, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
	//      Jo   A  2  3  4  5  6  7  8  9  10   J   Q   K
	
	public static final int[] REVERSE_VALUE_ORDER
		= { 1,   2,15,14,13,12,11,10, 9, 8, 7 ,  6,  5,  4};
	
	protected int[] suitOrder;
	protected int[] valueOrder;

/*-------------
 * Constructor
 */
	public NaturalCardOrder() {
		suitOrder = SUIT_ORDER_SPADE;
		valueOrder = VALUE_ORDER;
	}
	
	public NaturalCardOrder(int[] suitOrder) {
		this.suitOrder = suitOrder;
		valueOrder = VALUE_ORDER;
	}
	
	public NaturalCardOrder(int[] suitOrder, int[] valueOrder) {
		this.suitOrder = suitOrder;
		this.valueOrder = valueOrder;
	}
	
/*------------------------
 * implements (CardOrder)
 */
	public int compare(Card a, Card b) {
		if ( a.equals(b) ) return 0;
		
		if (a.isUnspecified()) {
			if (b.isUnspecified()) return 0;
			else return -1;
		}
		if (b.isUnspecified()) return 1;

		int suitA = suitOrder[a.getSuit()];
		int suitB = suitOrder[b.getSuit()];
		if (suitA > suitB) return 1;
		if (suitA < suitB) return -1;

		int valueA = valueOrder[a.getValue()];
		int valueB = valueOrder[b.getValue()];
		if (valueA > valueB) return 1;
		return -1;
	}
	
/*------------------
 * instance methods
 */
	/*
	 * DummyHandLayout で、あるスートのカードがなくなったとき
	 * 左詰で表示されることを防ぐために追加。2010/7/15
	 */
	public int[] getSuitOrder() {
		return suitOrder;
	}
	
	public int[] getValueOrder() {
		return valueOrder;
	}
}

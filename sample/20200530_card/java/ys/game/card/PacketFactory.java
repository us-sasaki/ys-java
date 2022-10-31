package ys.game.card;

import java.util.Vector;

/**
 * このクラスは、１組のカードを用意するメソッドをもつ.
 * 今後、カード評価などの便利なクラスメソッドライブラリ的なクラスにしたい.
 *
 * @version		a-release	23, April 2000
 * @author		Yusuke Sasaki
 */
public class PacketFactory {

	/** provideDeck() メソッドで Joker を含むデッキを生成するための定数 */
	public static final boolean WITH_JOKER		= true;
	
	/** provideDeck() メソッドで Joker を含まないデッキを生成するための定数 */
	public static final boolean WITHOUT_JOKER	= false;
	
/*---------------
 * class methods
 */
	/**
	 * １組の空の Packet を生成します。
	 * 本メソッドは、本クラスで取り扱うデフォルトの Packet インスタンスを定義します。
	 */
	public static Packet newPacket() {
		return new PacketImpl();
	}
	
	/**
	 * １枚のUnspecifiedCardを生成します。
	 * 本メソッドは、本クラスで取り扱うデフォルトの Card インスタンスを定義します。
	 */
	static CardImpl newCard() {
		return new CardImpl();
	}
	
	/**
	 * 指定されたスート、バリューをもつ１枚のカードを生成します。
	 * 本メソッドは、本クラスで取り扱うデフォルトの Card インスタンスを定義します。
	 */
	static CardImpl newCard(int suit, int value) {
		return new CardImpl(suit, value);
	}
	
	/**
	 * １組の Unspecified Card からなる Packet を用意します。
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
	 * １組のカードを用意する. ジョーカーを含むかどうか指定できる.
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
	 * 指定された pile のカードを等しく n 人に配ります。
	 * 配り方は、pile の上から順番に配ります。
	 * 結果のインスタンスは指定された pile のものとなります。
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
	 * パイルから、カードを同枚数ずつ、指定した Packet に配ります。
	 * 配られたカードは追加されます。
	 *
	 * @param	pile	カードの山
	 * @param	hand	配り先. 添え字の順に配られる.
	 * @param	begin	配りはじめる添え字.
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
	 * カードを等しく指定した Packet に配る. 配られたカードは追加される.
	 *
	 * @param	pile	配るカードの山
	 * @param	hand	配り先. 添え字 0 から順に配られる.
	 */
	public static void deal(Packet pile, Packet[] hand) {
		deal(pile, hand, 0);
	}
	
	/**
	 * Packet 中から指定したスーツのカードを抽出する. 速くない.
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
	 * Packet 中に指定したスーツのカードが何枚あるかカウントする.
	 * 速い.
	 */
	public static int countCard(Packet hand, int suit) {
		int c = 0;
		int n = hand.size();
		for (int i = 0; i < n; i++)
			if (hand.peek(i).getSuit() == suit) c++;
		return c;
	}
	
}


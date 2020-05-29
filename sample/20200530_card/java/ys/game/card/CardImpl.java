package ys.game.card;

/**
 * CardImpl はインターフェース Card のデフォルトの実装を提供します。
 *
 * @version		a-release	29, April 2000
 * @author		Yusuke Sasaki
 */
public class CardImpl implements Card {
	
	/** このカードのバリュー */
	private int		value;
	
	/** このカードのスート */
	private int		suit;
	
	/**
	 * 表向きかどうかを示します。ただし、裏向きでも値の取得などは可能なため、
	 * 意味付けは特にありません。
	 * 値の取得自体を不可能にしたい場合、Unspecified の枠組みを使用して下さい。
	 */
	private boolean	isHead = true;
	
	/**
	 * この Card の所属する holder です。
	 * holder は、すべての Card インスタンスが保持している Card の集まり
	 * (通常は 1 deck 52 枚)を保持しています。
	 * holder は、PacketFactory クラスの provideDeck 関連メソッド内部で
	 * 生成され、あとで変更することはできません。
	 */
	private Packet holder;
	
/*-------------
 * Constructor
 */
	/**
	 * Unspecified Card を作成します。
	 * Unspecified Card は初期状態として、裏向きに設定されます。
	 */
	public CardImpl() {
		value	= UNSPECIFIED;
		suit	= UNSPECIFIED;
		isHead	= false;
	}
	
	/**
	 * 指定されたスート、バリューの Card を作成します。
	 * 初期状態は表向きです。
	 *
	 * @param		suit		Card.JOKER, Card.SPADE, .., Card.CLUB
	 * @param		value		Card.JOKER, 1, .., 13(== Card.KING)
	 *
	 * @see			ys.game.card.Card#SPADE
	 * @see			ys.game.card.Card#HEART
	 * @see			ys.game.card.Card#DIAMOND
	 * @see			ys.game.card.Card#CLUB
	 * @see			ys.game.card.Card#JOKER
	 */
	public CardImpl(int suit, int value) {
		if ((suit < 0)||(suit > 4))
			throw new IllegalArgumentException(
						"スートの値に " + suit + "は指定できません。");
		if ((value < 0)||(value > 13))
			throw new IllegalArgumentException(
						"バリューの値に " + value + "は指定できません。");
		
		if (suit  == JOKER) value = JOKER;
		if (value == JOKER) suit  = JOKER;
		
		this.suit	= suit;
		this.value	= value;
		isHead		= true;
	}
	
	/**
	 * 指定されたカードと同一の内容の Card を新規に作成します。
	 * holder, 表裏の情報もコピーされます。
	 * 
	 * @param		card		作成したいカード
	 */
	public CardImpl(Card card) {
		try {
			int suit	= card.getSuit();
			int value	= card.getValue();
			
			this.suit	= suit;
			this.value	= value;
		}
		catch (UnspecifiedException e) {
			value	= UNSPECIFIED;
			suit	= UNSPECIFIED;
		}
		isHead = card.isHead();
		setHolder(card.getHolder()); // Card において、equals は内容の一致なので、
		// このオブジェクト自身が holder に登録されているインスタンスでなくてもよい。
	}
/*-------------------
 * implements (Card)
 */
	/**
	 * バリューの内容を返す。
	 * 未知のカードであった場合、UnspecifiedException がスローされる。
	 *
	 * @return		Card.ACE(=1), Card.TWO(=2), ……, Card.KING(=13),
	 *				Card.JOKER(=0)
	 */
	public int getValue() {
		if (value == UNSPECIFIED)
			throw new UnspecifiedException("カード内容が未知のため、getValue()は行えません");
		return value;
	}
	
	/**
	 * スートの内容を返す。
	 * 未知のカードであった場合、UnspecifiedException がスローされる。
	 *
	 * @return		Card.SPADE(=1), Card.HEART(=2), Card.DIAMOND(=3),
	 *              Card.CLUB(=4), Card.JOKER(=0)
	 *
	 * @see			ys.game.card.Card#SPADE
	 * @see			ys.game.card.Card#HEART
	 * @see			ys.game.card.Card#DIAMOND
	 * @see			ys.game.card.Card#CLUB
	 * @see			ys.game.card.Card#JOKER
	 */
	public int getSuit() {
		if (suit == UNSPECIFIED)
			throw new UnspecifiedException("カード内容が未知のため、getSuit()は行えません");
		return suit;
	}
	
	/**
	 * Unspecifiedのカードに対してスートとバリューを設定します。
	 * すでに同一の holder に指定されたスート、バリューのカードが含まれて
	 * いる場合、AlreadySpecifiedException がスローされます。
	 *
	 * @param		suit		スートの指定(Card.SPADE など)
	 * @param		value		バリューの指定
	 * 
	 * @see			ys.game.card.Card#SPADE
	 * @see			ys.game.card.Card#HEART
	 * @see			ys.game.card.Card#DIAMOND
	 * @see			ys.game.card.Card#CLUB
	 * @see			ys.game.card.Card#JOKER
	 * @see			ys.game.card.Card#JACK
	 * @see			ys.game.card.Card#QUEEN
	 * @see			ys.game.card.Card#KING
	 */
	public void specify(int suit, int value) {
		if (this.value != UNSPECIFIED)
			throw new AlreadySpecifiedException(
						"このカードはSpecifiedです。");
		
		int index = 0;
		while (index < holder.size()) {
			index = holder.indexOf(suit, value, index);
			if (index == -1)
				throw new AlreadySpecifiedException(
						"すでにこのカード内容は使用されているか、holder に含まれない内容です。");
			if (!holder.peek(index).isHead()) break;
			index++;
		}
		
		this.suit	= suit;
		this.value	= value;
		
		holder.peek(index).turn(true);
	}
	
	/**
	 * Unspecifiedのカードに対してスートとバリューを設定します。
	 * すでに同一の holder に指定されたスート、バリューのカードが含まれて
	 * いる場合、AlreadySpecifiedException がスローされます。
	 * 
	 * @param		card		設定するカードの内容
	 */
	public void specify(Card card) {
		specify(card.getSuit(), card.getValue());
	}
	
	/**
	 * このカードが指定されたスート、バリューに specify 可能であるか
	 * テストします。
	 */
	public boolean isSpecifiableAs(int suit, int value) {
		if (this.value != UNSPECIFIED) return false;
		
		int index = 0;
		while (index < holder.size()) {
			index = holder.indexOf(suit, value, index);
			if (index == -1) return false;
			if (!holder.peek(index).isHead()) break;
			index++;
		}
		return true;
	}
	
	/**
	 * このカードが指定されたカードの内容に specify 可能であるか
	 * テストします。
	 */
	public boolean isSpecifiableAs(Card card) {
		return isSpecifiableAs(card.getSuit(), card.getValue());
	}
	
	/**
	 * このカードを Unspecified にします。
	 */
	public void invalidate() {
		int index = 0;
		while (index < holder.size()) {
			index = holder.indexOf(suit, value, index);
			if (index == -1)
			throw new InternalError(
					"holder の状態不正です。holderにこのカードが含まれていません。");
			if (holder.peek(index).isHead()) break;
			index++;
		}
		
		suit  = UNSPECIFIED;
		value = UNSPECIFIED;
		
		holder.peek(index).turn(false);
	}
	
	/**
	 * 裏表をひっくり返す
	 */
	public void turn() {
		isHead = !isHead;
	}
	
	/**
	 * 裏表を指定します。
	 */
	public void turn(boolean head) {
		isHead = head;
	}
	
	/**
	 * 表であるかテストします。
	 */
	public boolean isHead() {
		return isHead;
	}
	
	/**
	 * Unspecified カードであるかテストします。
	 */
	public boolean isUnspecified() {
		return (suit == UNSPECIFIED);
	}
	
	/**
	 * このカードの所属するデッキを返します。
	 *
	 * @return		この Card の所属する holder
	 */
	public Packet getHolder() {
		return holder;
	}
	
	/**
	 * このカードの所属するデッキを指定します。
	 * カードは生成後、一度だけ本メソッドを呼ばれ、所属デッキが指定されます。
	 */
	public void setHolder(Packet holder) {
		if (this.holder == null) this.holder = holder;
	}
	
/*-----------
 * overrides
 */
	/**
	 * 同じ種類のカードであるかテストします。
	 * Unspecified Card に対しては、specifiable であるとき UnspecifiedException
	 * がスローされます。
	 */
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof Card)) return false;
		
		Card c = (Card)o;
		
		if (this.isUnspecified()) {
			if (c.isUnspecified())
				throw new UnspecifiedException();
			
			if (this.isSpecifiableAs(c))
				throw new UnspecifiedException();
			
			return false;
		}
		if (c.isUnspecified()) {
			if (c.isSpecifiableAs(this))
				throw new UnspecifiedException();
			
			return false;
		}
		
		return ( (getValue() == c.getValue())&&(getSuit() == c.getSuit() ) );
	}
	
	/**
	 * 文字列表現を取得します。
	 */
	public String toString() {
		String s;
		if (this.isHead()) s = "/"; else s = "_";
		if (this.isUnspecified()) return s+"??";
		
		switch (getSuit()) {
			case UNSPECIFIED:
				s += "*";
				break;
			case SPADE:
				s += "S";
				break;
			case HEART:
				s += "H";
				break;
			case DIAMOND:
				s += "D";
				break;
			case CLUB:
				s += "C";
				break;
			default:
				s += "Jo";
		}
		switch (getValue()) {
			case UNSPECIFIED:
			case JOKER:
				break;
			case ACE:
				s = s + "A";
				break;
			case 10:
				s = s + "T";
				break;
			case JACK:
				s = s + "J";
				break;
			case QUEEN:
				s = s + "Q";
				break;
			case KING:
				s = s + "K";
				break;
			default:
				s = s + getValue();
		}
		return s; // + "]";
	}
}

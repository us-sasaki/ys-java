package ys.game.card;
/*
 * 2001/ 7/22  shuffle で使用する乱数をクラス変数に変更
 */

import java.util.Random;

/**
 * デッキ、ハンドといった一般のカードの集まりをあらわします。
 * PacketImpl は、スタックとなっており、FILO型(First In Last Out)です。
 *
 * @version		a-release	22, July 2001
 * @author		Yusuke Sasaki
 */
public class PacketImpl implements Packet {
	protected static Random random;
	
	/** 最後の index が一番上 */
	protected Card[]	content;
	protected int		size;
	protected CardOrder	cardOrder;
	protected Packet	holder;
	
/*-------------
 * Constructor
 */
	public PacketImpl() {
		content		= new Card[0];
		size		= 0;
		cardOrder	= new NaturalCardOrder();
		holder		= null;
	}
	
	/**
	 * 指定された Packet と同一の内容のカードを保持する PacketImpl の
	 * インスタンスを生成します。保持するカードのインスタンスはコピー元の
	 * Packet のものが使用されます。(Impl カテゴリーのインスタンスに変換しない)
	 * したがって、holder もコピー元のものと同一となります。
	 * CardOrder についてもコピーが行われます。
	 *
	 * @param		packet		コピー元の Packet
	 */
	public PacketImpl(Packet packet) {
		this();
		int size = packet.size();
		for (int i = 0; i < size; i++) {
			Card card = packet.peek(i);
			add(card);
		}
		cardOrder = packet.getCardOrder();
	}
	
/*---------------
 * class methods
 */
	/**
	 * shuffle() で使用する乱数列を設定します。
	 * 定まった系列の Random オブジェクトを設定することによって、shuffle() の結果に
	 * 再現性を持たせることができます。
	 *
	 * @param		r		今後使用する Random オブジェクト
	 */
	public static void setRandom(Random r) {
		random = r;
	}
	
/*------------------
 * instance methods
 */
	
	/**
	 * 何枚のカードが含まれているかカウントします。
	 *
	 * @return		含んでいるカードの枚数
	 */
	public int size() {
		return size;
	}
	
	/**
	 * 何枚のUnspecifiedCard が含まれているかカウントします。
	 *
	 * @return		含んでいる UnspecifiedCard の枚数
	 */
	public int countUnspecified() {
		int cnt = 0;
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) cnt++;
		}
		return cnt;
	}
	
	/**
	 * 指定されたスートが何枚あるかカウントします。
	 * UnspecifiedCard はカウントしません。
	 
	 * @return		指定されたスートの枚数
	 */
	public int countSuit(int suit) {
		int cnt = 0;
		for (int i = 0; i < size; i++) {
			try {
				if (content[i].getSuit() == suit) cnt++;
			} catch (UnspecifiedException ignored) { }
		}
		return cnt;
	}
	
	/**
	 * 各スーツが何枚あるかカウントし、配列の形式で返します。
	 * 添字が、Card.SPADE などの要素に枚数が格納されます。
	 * Joker を含め、要素数 5 の配列が返ります。
	 * UnspecifiedCard はカウントしません。
	 *
	 * @return		各スートの枚数
	 */
	public int[] countSuits() {
		int[] result = new int[5];
		for (int i = 0; i < size; i++) {
			try {
				result[content[i].getSuit()]++;
			} catch (UnspecifiedException ignored) { }
		}
		return result;
	}
	
	/**
	 * 指定されたカードのインデックスを取得します。
	 * 指定されたカードを含まない場合、-1 を返します。
	 * カードの一致判定は、Card クラスの equals() を使用して行っています。
	 *
	 * @param		c		インデックスを取得したいカード(not Unspecified)
	 */
	public int indexOf(Card c) {
		Card unspecified = null;
		
		for (int i = 0; i < size; i++) {
			try {
				if (content[i].equals(c)) return i;
			}
			catch (UnspecifiedException e) {
				unspecified = content[i];
			}
		}
		if (unspecified != null)
			if (unspecified.isSpecifiableAs(c))
				throw new UnspecifiedException();
		
		return -1;
	}
	
	/**
	 * 指定されたスート、バリューのカードのインデックスを取得します。
	 * 該当するものが存在しない場合、-1 を返します。
	 */
	public int indexOf(int suit, int value) {
		Card unspecified = null;
		
		for (int i = 0; i < size; i++) {
			try {
				if ( (content[i].getValue() == value)
						&& (content[i].getSuit() == suit) ) return i;
			}
			catch (UnspecifiedException e) {
				unspecified = content[i];
			}
		}
		if (unspecified != null)
			if (unspecified.isSpecifiableAs(suit, value))
				throw new UnspecifiedException();
		
		return -1;
	}
	
	/**
	 * 指定されたスート、バリューをもつカードのインデックスを取得します。
	 * 検索を startIndex から開始します。
	 * 該当するものが存在しない場合、-1 を返します。
	 */
	public int indexOf(int suit, int value, int startIndex) {
		if (startIndex < 0)
			throw new IndexOutOfBoundsException("startIndex " + startIndex + " < 0");
		
		Card unspecified = null;
		
		for (int i = startIndex; i < size; i++) {
			try {
				if ( (content[i].getValue() == value)
						&& (content[i].getSuit() == suit) ) return i;
			}
			catch (UnspecifiedException e) {
				unspecified = content[i];
			}
		}
		if (unspecified != null)
			if (unspecified.isSpecifiableAs(suit, value))
				throw new UnspecifiedException();
		
		return -1;
	}
	
	/**
	 * 指定されたカードが含まれているかテストします。
	 */
	public boolean contains(Card card) {
		return (indexOf(card) > -1);
	}
	
	/**
	 * 指定されたカードが含まれているかテストします。
	 */
	public boolean contains(int suit, int value) {
		return (indexOf(suit, value) > -1);
	}
	
	/**
	 * UnspecifiedCard が含まれているかテストします。
	 */
	public boolean containsUnspecified() {
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) return true;
		}
		return false;
	}
	
	/**
	 * 指定されたスートのカードが含まれているかテストします。
	 */
	public boolean containsSuit(int suit) {
		boolean containsUnspecified = false;
		if ( (suit < 0)||(suit > 4) ) return false;
		
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) containsUnspecified = true;
			else if (content[i].getSuit() == suit) return true;
		}
		
		if (containsUnspecified) {
			if (holder == null) return false;
			//
			// holder に指定されたスーツのもので使用されてないものが
			// ある場合，UnspecifiedException
			//
			for (int i = 0; i < holder.size(); i++) {
				Card c = holder.peek(i);
				if ( (c.getSuit() == suit)
						&& (!c.isHead()) ) throw new UnspecifiedException();
			}
		}
		
		return false;
	}
	
	/**
	 * 指定されたバリューのカードが含まれているかテストする。
	 */
	public boolean containsValue(int value) {
		boolean containsUnspecified = false;
		if ( (value < 0)||(value > 13) ) return false;
		
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) containsUnspecified = true;
			else if (content[i].getValue() == value) return true;
		}
		
		if (containsUnspecified) {
			if (holder == null) return false;
			//
			// holder に指定されたスーツのもので使用されてないものが
			// ある場合，UnspecifiedException
			//
			for (int i = 0; i < holder.size(); i++) {
				Card c = holder.peek(i);
				if ( (c.getValue() == value)
						&& (!c.isHead()) ) throw new UnspecifiedException();
			}
		}
		
		return false;
	}
	
	/**
	 * 指定されたカードの集まりのいずれかのカードが含まれているかテストする。
	 */
	public boolean intersects(Packet packet) {
		boolean mayContainUnspecified = false;
		for (int i = 0; i < packet.size(); i++) { // packet を synchronized した方が？
			Card card = packet.peek(i);
			try {
				if (contains(card)) return true;
			}
			catch (UnspecifiedException e) {
				mayContainUnspecified = true;
			}
		}
		if (mayContainUnspecified)
			throw new UnspecifiedException();
		
		return false;
	}
	
	/**
	 * 指定されたカードの集まりを含んでいるかテストする。
	 */
	public boolean contains(Packet packet) {
		int containsUnspecified = 0;
		
		if (size < packet.size()) return false;
		
		for (int i = 0; i < packet.size(); i++) {
			Card card = packet.peek(i);
			try {
				if (!contains(card)) return false;
			}
			catch (UnspecifiedException e) {
				containsUnspecified++;
			}
		}
		if ( (containsUnspecified > 0)
				&&(countUnspecified() >= containsUnspecified) )
			throw new UnspecifiedException();
		
		return true;
	}
	
	/**
	 * カードをこのPacketの一番上に追加します。
	 * 同一 holder のカードのみが add 可能です。同一の holder に属していれば
	 * 同種類のカードも追加できます。
	 *
	 * @param		c		追加するカード
	 */
	public void add(Card c) {
		if ( (!c.isUnspecified())&&(contains(c)) ) return;
		
		if (holder != null) {
			if ( holder != c.getHolder() ) {
				throw new CardRuntimeException(
					"holder の異なるカードは追加できません。");
			}
		}
		else {
			holder = c.getHolder();
		}
		
		if (content.length == size) {		// < のときはありえない
			int newSize = content.length * 2 + 1;
			Card[] tmp = new Card[newSize];
			System.arraycopy(content, 0, tmp, 0, content.length);
			content = tmp;
		}
		content[size] = c;
		size++;
		
	}
	
	/**
	 * 指定された Packet をこの Packet の一番上に追加します。
	 *
	 * @param		packet	追加する Packet
	 */
	public void add(Packet packet) {
		for (int i = 0; i < packet.size(); i++) {
			add(packet.peek(i)); // 遅い実装
		}
	}
	
	/**
	 * カードをこのPacketの指定された位置に挿入します。
	 * insertAt(c, size()) は add(c) と同等の動作を行います。
	 */
	public void insertAt(Card c, int index) {
		if (index < 0) throw new IndexOutOfBoundsException("値範囲外です:"+index);
		if (index > size) index = size;
		
		Card[] src = content;
		Card[] dst;
		
		if (holder != null) {
			if ( holder != c.getHolder() ) {
				throw new CardRuntimeException(
					"holder の異なるカードは追加できません。");
			}
		}
		else {
			holder = c.getHolder();
		}
		
		if (content.length == size) {
			int newSize = content.length * 2;
			dst = new Card[newSize];
			if (index > 0)
				System.arraycopy(content, 0, dst, 0, index);
		}
		else {
			dst = content;
		}
		if (size > index)
			System.arraycopy(src, index, dst, index + 1, size - index);
		dst[index] = c;
		
		content = dst;
		size++;
	}
	
	/**
	 * 一番上のカードを引きます。引かれたカードは本Packetから削除されます。
	 * この操作は、draw(size() - 1) と(カードを含まない場合を除いて)同等です。
	 * カードを含まない Packet に対して本メソッドをコールすると、IllegalStateException
	 * がスローされます。
	 *
	 * @return		一番上のカード
	 */
	public Card draw() {
		if (size == 0)
			throw new IllegalStateException("Empty Packet に対して draw を実行しました。");
		
		Card drawn = content[size - 1];
		content[size - 1] = null;
		size--;
		
		return drawn;
	}
	
	/**
	 * 指定された番号のカードを引く。引かれたカードは本 Packet から削除されます。
	 * 引数として無効な値を指定した場合、IndexOutOfBoundsException がスローされます。
	 *
	 * @param		n		上から何番目のカードを引くか
	 * @return		引いたカード
	 */
	public Card draw(int n) {
		if ( (n < 0)||(n >= size) )
			throw new IndexOutOfBoundsException("draw の引数は無効です。");
		
		Card drawn = content[n];
		
		System.arraycopy(content, n+1, content, n, size - n - 1);
		
		content[size - 1] = null;
		size--;
		
		return drawn;
	}
	
	/**
	 * 指定されたカード(と同種類のカード)を引きます。
	 * 引かれたカードは本 Packet から削除されます。
	 * 同種類のカードがない場合、null が返されます。
	 * 返却される Card のインスタンスは引数に指定した Card のインスタンスと一般
	 * に異なります。
	 * 返却される Card はつねにこの Packet に含まれるインスタンスであるのに対し、
	 * 引数に指定する Card はその限りではないからです。
	 *
	 * @param		c		引きたいカード
	 * @return		この Packet に含まれる指定されたカードと同種のカードへの参照
	 */
	public Card draw(Card c) {
		int index = indexOf(c);
		if (index == -1) return null;
		return draw(index);
	}
	
	/**
	 * 指定されたカードを引きます。引かれたカードは本 Packet から削除されます。
	 */
	public Card draw(int suit, int value) {
		int index = indexOf(suit, value);
		if (index == -1) return null;
		return draw(index);
	}
	
	/**
	 * UnspecifiedCard を引きます。
	 * この Packet が UnspecifiedCard を含まない場合、null が返ります。
	 *
	 * @return		この Packet に含まれる Unspecified Card (のひとつ)
	 */
	public Card drawUnspecified() {
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) return draw(i);
		}
		
		return null;
	}
	
	/**
	 * 一番上のカードを覗きます。覗かれたカードは本 Packet から削除されません。
	 * この操作は、カードが含まれない場合を除いて peek(size() - 1)と同等です。
	 * この Packet が空の場合、null が返ります。
	 *
	 * @return		一番上のカードへの参照
	 */
	public Card peek() {
		if (size == 0) return null;
		return content[size - 1];
	}
	
	/**
	 * 指定された番号のカードを覗きます。覗かれたカードは本 Packet から削除されません。
	 *
	 * @param		n		上から何番目のカードを覗くか
	 * @return		除いたカードへの参照
	 */
	public Card peek(int n) {
		if ( (n < 0) || (n >= size) )
			throw new IndexOutOfBoundsException(
						"この Packet には" + size + "枚のカードが含まれ、"
						+ n + "番目は指定できません。");
		
		return content[n];
	}
	
	/**
	 * 指定されたスート、バリューをもつカードを取得します。
	 * 該当するものがない場合、null が返ります。
	 */
	public Card peek(int suit, int value) {
		int index = indexOf(suit, value);
		if (index == -1) return null;
		return content[index];
	}
	
	/**
	 * UnspecifiedCard への参照を取得します。
	 * この Packet が UnspecifiedCard を含まない場合、null が返ります。
	 *
	 * @return		この Packet に含まれる Unspecified Card (のひとつ)
	 */
	public Card peekUnspecified() {
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) return peek(i);
		}
		
		return null;
	}
	
	/**
	 * 本 Packet の指定されたスーツを抜き出します。
	 * 抜き出されたカードは本パケットから削除されません。
	 * Unspecified Card は抜き出し対象から除外されます。
	 *
	 * @param		suit		取り出したいスーツ
	 * @return		取出された Packet
	 *
	 */
	public Packet subpacket(int suit) {
		Packet result = PacketFactory.newPacket();
		for (int i = 0; i < size; i++) {
			try {
				if (content[i].getSuit() == suit)
					result.add(content[i]);
			}
			catch (UnspecifiedException ignored) {
			}
		}
		
		return result;
	}
	
	/**
	 * arrange()によって並び替える際のカード順序規則を設定します。
	 *
	 * @param		order		並び順の規則
	 */
	public void setCardOrder(CardOrder order) {
		cardOrder = order;
	}
	
	/**
	 * この Packet で使用しているカード順序規則を取得します。
	 *
	 * @return		並び順の規則
	 */
	public CardOrder getCardOrder() {
		return cardOrder;
	}
	
	/**
	 * カードの並び替えを行います。
	 * setCardOrder(CardOrder) で指定されたカード順序規則に従って、順序大のものが
	 * 上に来るように並び替えられます。
	 *
	 * @see			ys.game.card.Packet#setCardOrder
	 * @see			ys.game.card.CardOrder
	 */
	public void arrange() {
		for (int i = 0; i < size - 1; i++) {
			for (int j = i + 1; j < size; j++) {
				if (cardOrder.compare(content[i], content[j]) < 0) {
					Card tmp = content[i];
					content[i] = content[j];
					content[j] = tmp;
				}
			}
		}
	}

	/**
	 * カードをランダムにシャッフルする。
	 * PacketImpl では、java.util.Random によるコンピュータシャッフルを行う。
	 */
	public void shuffle() {
		if (size == 0) return;
		
		if (random == null) random = new Random();
		Random r = random;
		
		Card[] tmp = new Card[size];
		
		for (int i = 0; i < size; i++) {
			int index;
			do {
				index = (r.nextInt() & 0x7fffffff) % size;
			} while ( tmp[index] != null );
			tmp[index] = content[i];
		}
		content = tmp;
	}
	
	/**
	 * 本 Packet に含まれていないカードの集合(残りカード)を取得する。
	 */
	public Packet complement() {
		Packet result	= new PacketImpl();
		Packet copy		= (Packet)clone();
		
		for (int i = 0; i < holder.size(); i++) {
			Card c = holder.peek(i);
			if (copy.contains(c)) copy.draw(c); // holder には同種のカードが２枚以上あることがある
			else result.add(c);
		}
		return result;
	}
	
	public void turn() {
		for (int i = 0; i < size; i++)
			content[i].turn();
	}
	
	public void turn(boolean head) {
		for (int i = 0; i < size; i++)
			content[i].turn(head);
	}
	
	/**
	 * 指定された Pakcet と本 Packet の共通部分を取得します。
	 * 結果は PacketImpl のインスタンスであり、それを構成する Card インスタンスは
	 * 本 Packet のものが使用されます。
	 * 
	 * @param		target		共通部分を取る対象
	 * @return		この Packet と target の共通部分
	 */
	public Packet intersection(Packet target) {
		PacketImpl result = new PacketImpl();
		
		for (int i = 0; i < size; i++) {
			Card c = content[i];
			if (target.contains(c)) result.add(c);
		}
		
		return result;
	}
	
	public Packet sub(Packet target) {
		PacketImpl result = new PacketImpl();
		
		for (int i = 0; i < size; i++) {
			Card c = content[i];
			if (!target.contains(c)) result.add(c);
		}
		return result;
	}
	
/*-----------
 * overrides
 */
	/**
	 * この PacketImpl のコピーを作成します。
	 * 配列については新たに作成されますが、保持しているCardのインスタンス
	 * のコピーは行いません。
	 *
	 * @return		このPacketのコピーオブジェクト
	 */
	public Object clone() {
		PacketImpl copy = new PacketImpl();
		copy.content	= new Card[size];
		
		System.arraycopy(content, 0, copy.content, 0, size);
		
		copy.size		= size;
		copy.cardOrder	= cardOrder;
		copy.holder		= holder;
		
		return copy;
	}
		
	/**
	 * この PacketImpl の文字列表現を返します。
	 *
	 * @return		この Packet の文字列表現
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < size; i++) {
			s.append(content[i].toString());
		}
		return "{" + s.toString() + "}";
	}

}

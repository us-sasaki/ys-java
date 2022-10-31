package ys.game.card;

/**
 * デッキ、ハンドといった一般のカードの集まりをあらわすクラスです。
 * Packetは、スタックとなっておりFILO(First in Last out)型です。
 *
 * @version		a-release	3, December 2000
 * @author		Yusuke Sasaki
 */
public interface Packet extends Cloneable { // extends java.util.SortedSet ( after JDK 1.2 )
	
	/**
	 * 何枚のカードが含まれているかカウントします。
	 *
	 * @return		含んでいるカードの枚数
	 */
	int size();
	
	/**
	 * 何枚のUnspecifiedCard が含まれているかカウントします。
	 *
	 * @return		含んでいる UnspecifiedCard の枚数
	 */
	int countUnspecified();
	
	/**
	 * 指定されたスートが何枚あるかカウントします。
	 * UnspecifiedCard はカウントしません。
	 *
	 * @return		指定されたスートの枚数
	 */
	int countSuit(int suit);
	
	/**
	 * 各スーツが何枚あるかカウントし、配列の形式で返します。
	 * 添字が、Card.SPADE などの要素に枚数が格納されます。
	 * Joker を含め、要素数 5 の配列が返ります。
	 * UnspecifiedCard はカウントしません。
	 *
	 * @return		各スートの枚数
	 */
	int[] countSuits();
	
	/**
	 * 指定したカードのインデックスを取得します。
	 * 取得されたインデックスは、draw(int)などのメソッドで使用します。
	 * 指定されたカードを含まない場合、-1 を返します。
	 * カードの一致判定は、Card クラスの equals() を使用して行っています。
	 */
	int indexOf(Card c);
	
	/**
	 * 指定されたスート、バリューをもつカードのインデックスを取得します。
	 * 該当するものが存在しない場合、-1 を返します。
	 */
	int indexOf(int suit, int value);
	
	/**
	 * 指定されたスート、バリューをもつカードのインデックスを取得します。
	 * 検索を startIndex から開始します。
	 * 該当するものが存在しない場合、-1 を返します。
	 */
	int indexOf(int suit, int value, int startIndex);
	
	/**
	 * 指定されたカードが含まれているかテストします。
	 */
	boolean contains(Card card);
	
	/**
	 * 指定されたカードが含まれているかテストします。
	 */
	public boolean contains(int suit, int value);
	
	/**
	 * UnspecifiedCard が含まれているかテストします。
	 */
	boolean containsUnspecified();
	
	/**
	 * 指定されたスートのカードが含まれているかテストします。
	 */
	boolean containsSuit(int suit);
	
	/**
	 * 指定されたバリューのカードが含まれているかテストします。
	 */
	boolean containsValue(int value);
	
	/**
	 * 指定されたカードの集まりのいずれかのカードが含まれているかテストします。
	 */
	boolean intersects(Packet packet);
	
	/**
	 * 指定されたカードの集まりを含んでいるかテストします。
	 */
	boolean contains(Packet packet);
	
	/**
	 * カードをこのPacketの一番上に追加します。
	 * 同一 holder のカードのみが add 可能です。同一の holder に属していれば
	 * 同種類のカードも追加できます。
	 * null オブジェクトを挿入しようとすると何も行いません。
	 *
	 * @param		c		追加するカード
	 */
	void add(Card c);
	
	/**
	 * 指定された Packet をこの Packet の一番上に追加します。
	 *
	 * @param		packet	追加する Packet
	 */
	void add(Packet packet);
	
	/**
	 * カードをこのPacketの指定された位置に挿入します。
	 * insertAt(c, size()) は add(c) と同等の動作を行います。
	 * null オブジェクトを挿入しようとすると何も行いません。
	 */
	void insertAt(Card c, int index);
	
	/**
	 * 一番上のカードを引きます。引かれたカードは本Packetから削除されます。
	 * この操作は、draw(size() - 1) と(カードを含まない場合を除いて)同等です。
	 * カードを含まない Packet に対して本メソッドをコールすると、IllegalStateException
	 * がスローされます。
	 *
	 * @return		一番上のカード
	 */
	Card draw();
	
	/**
	 * 指定された番号のカードを引く。引かれたカードは本 Packet から削除されます。
	 * 引数として無効な値を指定した場合、IndexOutOfBoundsException がスローされます。
	 *
	 * @param		n		上から何番目のカードを引くか
	 * @return		引いたカード
	 */
	Card draw(int n);
	
	/**
	 * 指定されたカードを引きます。(引かれたカード).equals(指定カード) となります。
	 * インスタンスのカテゴリーが異なっている場合などで、「同種の」カードを引きたい
	 * 場合、draw(int, int) インターフェースを使用してください。
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
	Card draw(Card c);
	
	/**
	 * 指定されたカードを引きます。引かれたカードは本 Packet から削除されます。
	 */
	Card draw(int suit, int value);
	
	/**
	 * UnspecifiedCard を引きます。引かれたカードは本 Packet から削除されます。
	 */
	Card drawUnspecified();
	
	/**
	 * 一番上のカードを覗きます。覗かれたカードは本 Packet から削除されません。
	 * この操作は、カードが含まれない場合を除いて peek(size() - 1)と同等です。
	 * この Packet が空の場合、null が返ります。
	 *
	 * @return		一番上のカードへの参照
	 */
	Card peek();
	
	/**
	 * 指定された番号のカードを覗きます。覗かれたカードは本 Packet から削除されません。
	 *
	 * @param		n		上から何番目のカードを覗くか
	 * @return		除いたカードへの参照
	 */
	Card peek(int n);
	
	/**
	 * 指定されたカードへの参照を取得します。
	 */
	Card peek(int suit, int value);
	
	/**
	 * Unspecified Card (の１つ)への参照を取得します。
	 */
	Card peekUnspecified();
	
	/**
	 * 本 Packet の指定されたスーツを抜き出します。
	 * 抜き出されたカードは本パケットから削除されません。
	 *
	 * @param		suit		取り出したいスーツ
	 * @return		取出された Packet
	 *
	 */
	Packet subpacket(int suit);
	
	/**
	 * arrange()によって並び替える際のカード順序規則を設定します。
	 *
	 * @param		order		並び順の規則
	 */
	void setCardOrder(CardOrder order);
	
	/**
	 * この Packet で使用しているカード順序規則を取得します。
	 *
	 * @return		並び順の規則
	 */
	CardOrder getCardOrder();
	
	/**
	 * カードの並び替えを行います。
	 * setCardOrder(CardOrder) で指定されたカード順序規則に従って、順序大のものが
	 * 上に来るように並び替えます。
	 *
	 * @see			ys.game.card.Packet#setCardOrder
	 * @see			ys.game.card.CardOrder
	 */
	void arrange();
	
	/**
	 * カードをランダムにシャッフルします。
	 * シャッフルの仕方は実装クラスで規定されます。
	 */
	void shuffle();
	
	/**
	 * 本 Packet に含まれていないカードの集合(残りカード)を取得します。
	 */
	Packet complement();
	
	/**
	 * 指定された Pakcet と本 Packet の共通部分を取得します。
	 * 結果は PacketImpl のインスタンスであり、それを構成する Card インスタンスは
	 * 本 Packet のものが使用されます。
	 * 
	 * @param		target		共通部分を取る対象
	 * @return		この Packet と target の共通部分
	 */
	Packet intersection(Packet target);

	/**
	 * 一枚無作為に抽出します。
	 */
//	Card drawAtRandom();
	
	/**
	 * 一枚無作為に閲覧します。
	 */
//	Card peekAtRandom();
	
	/**
	 * 合併を求めます。これは add(Packet) で実装されました。
	 */
//	Packet union(Packet target);
	
	/**
	 * 差を求めます。
	 * this に含まれている Packet から指定 Packet に含まれるものを除いたものが返却されます。
	 */
	Packet sub(Packet target);
	
	/**
	 * 含まれているすべてのカードの表裏を逆にします。
	 */
	void turn();
	
	/**
	 * 含まれているすべてのカードの表裏を指定します。
	 */
	void turn(boolean head);
}

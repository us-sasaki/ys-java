package ys.game.card;

/**
 * 抽象的なカードをあらわすインターフェースです。<BR>
 * <BR><B>Versionについて</B></BR>
 * Version は、以下のように遷移します。<BR>
 * <TABLE BORDER=1>
 * <TR><TD>0.making</TD>
 * <TD>ソースコード書き途中、コンパイルはまだ</TD>
 * <TD>コンパイル成功→1</TD></TR>
 * <TR><TD>1.draft</TD>
 * <TD>ソースコードを書き、コンパイルが通った</TD>
 * <TD>単体試験完了→2</TD></TR>
 * <TR><TD>2.unit tested</TD>
 * <TD>単体、もしくは基本的な結合試験が通った</TD>
 * <TD>結合試験完了→3</TD></TR>
 * <TR><TD>3.a-release</TD>
 * <TD>結合試験が通った</TD>
 * <TD>簡単な修正→3　大規模な修正→4</TD></TR>
 * <TR><TD>4.remaking</TD>
 * <TD>ソースコード修正中</TD>
 * <TD>修正、コンパイル完了→1 or 2</TD></TR>
 * <TR><TD>5.release</TD>
 * <TD>3の状態で、長期に渡って変更がなかった</TD>
 * <TD>簡単な修正→3　大規模な修正→4</TD></TR>
 * </TABLE>
 *
 * @version		a-release		17, April 2000
 * @author		Yusuke Sasaki
 */
public interface Card {

	// スーツの種類
	
	/**
	 * このカードが Unspecified であることを示す定数です。
	 * この値が取得されることはありません。
	 */
	int UNSPECIFIED	= -1;
	
	/**
	 * スート、バリューに適用される、このカードが
	 * ジョーカーであることを示す定数です。
	 */
	int JOKER	= 0;
	
	/** スートに適用される、スペードであることを示す定数(=4)です。 */
	int SPADE	= 4;
	
	/** スートに適用される、ハートであることを示す定数(=3)です。 */
	int HEART	= 3;
	
	/** スートに適用される、ダイアモンドであることを示す定数(=2)です。 */
	int DIAMOND = 2;
	
	/** スートに適用される、クラブであることを示す定数(=1)です。 */
	int CLUB	= 1;
	
	// 数
	/** バリューに適用される、エースであることを示す定数(=1)です。 */
	int ACE		= 1;
	
	/** バリューに適用される、ジャックであることを示す定数(=11)です。 */
	int JACK	= 11;
	
	/** バリューに適用される、クイーンであることを示す定数(=12)です。 */
	int QUEEN	= 12;
	
	/** バリューに適用される、キングであることを示す定数(=13)です。 */
	int KING	= 13;
	
	/**
	 * バリューの内容を返します。
	 * 未知のカードであった場合、UnspecifiedException がスローされます。
	 *
	 * @return		Card.ACE(=1), Card.TWO(=2), ……, Card.KING(=13),
	 *				Card.JOKER(=0)
	 * @see			#ACE
	 * @see			#KING
	 * @see			#QUEEN
	 * @see			#JACK
	 */
	int getValue();
	
	/**
	 * スートの内容を返します。
	 * 未知のカードであった場合、UnspecifiedException がスローされます。
	 *
	 * @return		Card.SPADE(=1), Card.HEART(=2), Card.DIAMOND(=3),
	 *              Card.CLUB(=4), Card.JOKER(=0)
	 * @see			#SPADE
	 * @see			#HEART
	 * @see			#DIAMOND
	 * @see			#CLUB
	 */
	int getSuit();
	
	/**
	 * Unspecifiedのカードに対してスートとバリューを設定します。
	 * この結果、カードは自動的に表向きになります。
	 * すでに同一の holder に指定されたスート、バリューのカードが含まれて
	 * いる場合、AlreadySpecifiedException がスローされます。
	 *
	 * @param		suit		設定するスート
	 * @param		value		設定するバリュー
	 * 
	 * @see			#SPADE
	 * @see			#HEART
	 * @see			#DIAMOND
	 * @see			#CLUB
	 * @see			#JOKER
	 * @see			#JACK
	 * @see			#QUEEN
	 * @see			#KING
	 * @see			#invalidate()
	 */
	void specify(int suit, int value);
	
	/**
	 * Unspecifiedのカードに対してスートとバリューを設定します。
	 * この結果、カードは自動的に表向きになります。
	 * すでに同一の holder に指定されたスート、バリューのカードが含まれて
	 * いる場合、AlreadySpecifiedException がスローされます。
	 *
	 * @param		card		設定するカードの内容
	 * @see			#invalidate()
	 */
	void specify(Card card);
	
	/**
	 * 本オブジェクトを Unspecified カードに変更します。
	 *
	 * @see			#specify(int,int)
	 * @see			#specify(ys.game.card.Card)
	 */
	void invalidate();
	
	/**
	 * このカードが指定されたスート、バリューに specify 可能であるか
	 * テストします。
	 *
	 * @param		suit		テストしたいスートの指定
	 * @param		value		テストしたい値の指定
	 * @return		specifiable なら true
	 */
	boolean isSpecifiableAs(int suit, int value);
	
	/**
	 * このカードが指定されたカードの内容に specify 可能であるか
	 * テストします。
	 *
	 * @param		card		テストしたいカード(の種類)の指定
	 * @return		specifiable なら true
	 */
	boolean isSpecifiableAs(Card card);
	
	/**
	 * 裏表をひっくり返します。
	 *
	 * @see		#isHead()
	 */
	void turn();
	
	/**
	 * 裏表を指定します。
	 *
	 * @param		head		表にする場合 true, 裏にする場合 false
	 *							を指定します。
	 * @see		#isHead()
	 */
	void turn(boolean head);
	
	/**
	 * 表であるかテストします。
	 *
	 * @return		表の場合 true, 裏の場合 false が返ります。
	 * @see			#turn()
	 * @see			#turn(boolean)
	 */
	boolean isHead();
	
	/**
	 * スート、バリューが設定されているかテストする。
	 *
	 * @return		スート、バリューが指定されていない場合 true が返ります。
	 */
	boolean isUnspecified();
	
	/**
	 * このカードの所属するデッキを返します。
	 *
	 * @return		この Card の所属する holder
	 */
	Packet getHolder();
	
	/**
	 * このカードの所属するデッキを指定します。
	 * カードは生成後、一度だけ本メソッドを呼ばれ、所属デッキが指定されます。
	 * 
	 * @param		holder		所属するデッキ
	 */
	void setHolder(Packet holder);

}

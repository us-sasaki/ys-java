package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;
/**
 * Player は Bid, または Playを行う主体です。
 * GUIに連動した人、コンピュータアルゴリズムなどが当てはまります。
 * Player は、各々１つのBoardと連動している。BoardManagerのもつmaster board
 * との整合性は、今のところPlayerのありえないプレイを契機として修復される。
 * 将来的には Board の状態変更メソッド内で hashCode を用いて随時検知したい。
 *
 * @version		a-release		11, May 2000
 * @author		Yusuke Sasaki
 */
public abstract class Player {
	/** リードの順番を示す定数で、リーダー（１番目）を示します。 */
	protected static final int LEAD		= Board.LEAD;
	
	/** リードの順番を示す定数で、セカンドハンド（２番目）を示します。 */
	protected static final int SECOND	= Board.SECOND;
	
	/** リードの順番を示す定数で、サードハンド（３番目）を示します。 */
	protected static final int THIRD	= Board.THIRD;
	
	/** リードの順番を示す定数で、フォースハンド（４番目）を示します。 */
	protected static final int FORTH	= Board.FORTH;
	
	/** プレイヤーの相対位置を示す定数(=0)で、自分の位置を示します。 */
	protected static final int ME		= 0;
	
	/** プレイヤーの相対位置を示す定数(=1)で、自分の左の席(left hand)を示します。 */
	protected static final int LEFT		= 1;
	
	/** プレイヤーの相対位置を示す定数(=2)で、パートナーの位置を示します。 */
	protected static final int PARTNER	= 2;
	
	/** プレイヤーの相対位置を示す定数(=3)で、自分の右の席(right hand)を示します。 */
	protected static final int RIGHT	= 3;
	
	private Board	myBoard;
	private int		mySeat;
	
/*------------------
 * instance methods
 */
	/**
	 * このプレイヤーが参照するボードを設定します。
	 * 継承クラスのコンストラクタなどで使用します。
	 * 上書き可能です。
	 */
	public void setBoard(Board board) {
		myBoard = board;
	}
	
	/**
	 * このプレイヤーの座っている場所(Board.NORTHなど)を指定します。
	 */
	public void setMySeat(int seat) {
		mySeat = seat;
	}
	
	/**
	 * 上位プログラムからコールされるメソッドで、
	 * このプレイヤーのビッド、プレイを返却します。
	 */
	public Object play() throws InterruptedException {
		switch (myBoard.getStatus()) {
		
		case Board.BIDDING:
			while (true) {
				Bid b = bid();
				if (myBoard.allows(b)) return b;
			}
			
		case Board.OPENING:
		case Board.PLAYING:
			while (true) {
				Card c = draw();
				if (myBoard.allows(c)) return c;
			}
			
		case Board.DEALING:
		case Board.SCORING:
			throw new IllegalStatusException();
			
		default:
			throw new InternalError();
		}
	}
	
/*
 * サブクラスに提供する便利関数
 */
	/**
	 * このプレイヤーの参照するボードを返却します。
	 */
	public Board getBoard() {
		return myBoard;
	}
	
	/**
	 * このプレイヤーの座っている座席を返却します。
	 */
	public int getMySeat() {
		return mySeat;
	}
	
	/**
	 * パートナーの座っている場所(Board.NORTHなどの座席定数)を返却します。
	 *
	 * @since		2002/5
	 */
	public int getPartnerSeat() {
		return (mySeat + 2) % 4;
	}
	
	/**
	 * このプレイヤーのもつハンドを返却します。
	 */
	public Packet getMyHand() {
		return myBoard.getHand(mySeat);
	}
	
	/**
	 * 現在プレイ順番となっているハンドを取得します。
	 * このプレイヤーがディクレアラーの場合、このメソッドを使用することで
	 * プレイ対象となっている自分、またはダミーのいずれかのハンドが取得できます。
	 */
	public Packet getHand() {
		return myBoard.getHand(myBoard.getTurn());
	}
	
	public Packet getDummyHand() {
		return myBoard.getHand(myBoard.getDummy());
	}
	
	/**
	 * 現在場に出ているカードを取得します。
	 */
	public Trick getTrick() {
		return myBoard.getTrick();
	}
	
	/**
	 * リードされたカードを取得します。
	 * 自分がリードを行う番であった場合や、Board の状態が Board.PLAYING
	 * でなかった場合、null が返却されます。
	 */
	public Card getLead() {
		int o = getPlayOrder();
		if ((o == Board.LEAD)||(o == -1)) return null;
		return getTrick().getLead();
	}
	
	/**
	 * 現在のプレイ順(lead, 2nd, 3rd, 4th)を返します。
	 * Board の状態が Board.OPENING, Board.PLAYING 以外の場合、 -1 が返却されます。
	 *
	 * @return		プレイ順を示す定数(LEAD, SECOND, THIRD, FORTH)
	 */
	public int getPlayOrder() {
		return myBoard.getPlayOrder();
	}
	
	/**
	 * ダミーの自分からの相対位置を返します。
	 * ダミーがまだ決定していない場合、IllegalStatusException がスローされます。
	 *
	 * @return		ダミーの相対位置
	 * @see			ME
	 * @see			LEFT
	 * @see			PARTNER
	 * @see			RIGHT
	 */
	public int getDummyPosition() {
		int dummySeat = myBoard.getDummy();
		if (dummySeat == -1)
			throw new IllegalStatusException("まだコントラクトが決定していません");
		
		return (dummySeat - mySeat + 4) % 4;
	}
	
	/**
	 * Leader の自分からの相対位置を返します。
	 * プレイの状態でないとき、IllegalStatusException がスローされます。
	 *
	 * @return		Leaderの相対位置
	 * @see			ME
	 * @see			LEFT
	 * @see			PARTNER
	 * @see			RIGHT
	 */
	public int getLeaderPosition() {
		if ( (myBoard.getStatus() != Board.OPENING) && (myBoard.getStatus() != Board.PLAYING) )
			throw new IllegalStatusException("プレイが開始されていません");
		if (myBoard.getStatus() == Board.SCORING)
			throw new IllegalStatusException("ボードはすでに終了しています");
		
		Trick t = myBoard.getTrick();
		return (t.getLeader() - mySeat + 4) % 4;
	}
	
/*------------------
 * abstract methods
 */
	public abstract Bid bid() throws InterruptedException;
	public abstract Card draw() throws InterruptedException;
	
}

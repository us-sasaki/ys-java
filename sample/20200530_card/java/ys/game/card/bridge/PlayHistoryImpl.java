package ys.game.card.bridge;

import ys.game.card.Packet;
import ys.game.card.Card;
import ys.game.card.UnspecifiedException;

/**
 * PlayHistory クラスは、コントラクトブリッジにおけるプレイ部分
 * の状態、ルールをパックします。
 * 本クラスは Board オブジェクトに保持され、プレイ部分の実処理を
 * 行います。
 *
 * @version		a-release		17, April 2000
 * @author		Yusuke Sasaki
 */
public class PlayHistoryImpl implements PlayHistory {
	
	/** それぞれの席のハンド情報 */
	protected Packet[]	hand;
	
	/** これまでプレイされたトリックです。 */
	protected Trick[]		trick;
	
	/** 現在プレイ中のトリック数です。 */
	protected int 		trickCount;
	
	/** トランプを指定します。 */
	protected int			trump;
	
/*-------------
 * constructor
 */
	public PlayHistoryImpl() {
		hand		= new Packet[4];
		trick		= new Trick[13];
		trickCount	= 0;
		trump		= -1;
	}
	
	/**
	 * 指定された PlayHistory と同一内容の PlayHistoryImpl のインスタンスを
	 * 新規に生成します。
	 * Trick, Hand の内容については、コピー元のインスタンスが使用されます。
	 *
	 * @param		src		コピー元の PlayHistory
	 */
	public PlayHistoryImpl(PlayHistory src) {
		this();
		
		//
		// Hand のコピー
		//
		hand = src.getHand();
		
		//
		// Trick のコピー
		//
		Trick[] srcTrick = src.getAllTricks();
		if (srcTrick != null) {
			System.arraycopy(srcTrick, 0, trick, 0, srcTrick.length);
			trickCount = srcTrick.length - 1;
			if ( src.isFinished() ) trickCount = 13;
		}
		
		//
		// trump のコピー
		//
		trump = src.getTrump();
	}
	
/*------------------
 * instance methods
 */

	public void setHand(Packet[] hand) {
		if ( (trick[0] != null)&&(trick[0].size() > 0) )
			throw new IllegalStatusException("すでにプレイが開始されているため" +
											" setHand は行えません。");
		if (hand.length != 4)
			throw new IllegalArgumentException("４人分のハンドが指定されていません。");
		
		for (int i = 0; i < 4; i++) {
			if (hand[i].size() != 13)
				throw new IllegalArgumentException("ハンド"+i+"のカード枚数が異常です。13枚指定して下さい。");
		}
		
		this.hand = hand;
	}
	
	public void setContract(int leader, int trump) {
		if (this.trump != -1)
			throw new IllegalStatusException("一度指定されたコントラクトを変更することはできません。");
		this.trump = trump;
		trick[0] = createTrick(leader, trump);
	}
	
	/**
	 * 指定されたプレイが可能であるか判定します。
	 * プレイヤーとして、この PlayHistory の getTurn() のプレイヤーが
	 * プレイしていると仮定しています。
	 * 具体的には、現在順番
	 * 
	 * @param		p		プレイ可能か判定したいカード
	 * @return		プレイできるかどうか
	 */
	public boolean allows(Card p) {
		int turn = trick[trickCount].getTurn();
		
		//
		// hand[turn] が指定されたカード持っていない場合 false
		//
		try {
			if (!hand[turn].contains(p)) return false; // 例外の方がいいかも
		}
		catch (UnspecifiedException ignored) {
			// 持っていてもおかしくない
		}
		
		//
		// スートフォローに従っているか
		//
		Card lead = trick[trickCount].getLead();
		if (lead == null) return true;
		int suit = lead.getSuit();
		if (suit == p.getSuit()) return true;
		try {
			if (!hand[turn].containsSuit(suit)) return true;
			return false;
		}
		catch (UnspecifiedException e) {
			// 含んでいるか判断が付かない場合、ＯＫとする。
			return true;
		}
	}
	
	/**
	 * 指定されたカードをプレイして状態を更新します。
	 * プレイできないカードをプレイしようとすると IllegalPlayException
	 * がスローされます。
	 *
	 * @param		p		プレイするカード
	 */
	public void play(Card p) {
		if (!this.allows(p))
			throw new IllegalPlayException(
					p.toString() + "は現在プレイできません。");
		
		int turn = trick[trickCount].getTurn();
		
		Card drawn;
		try {
			drawn = hand[turn].draw(p);
		}
		catch (UnspecifiedException e) {
			drawn = hand[turn].drawUnspecified();
			drawn.specify(p);
		}
		drawn.turn(true);
		Trick tr = trick[trickCount];
		tr.add(drawn);
		
		if (!tr.isFinished()) return;
		
		trickCount++;
		if (trickCount < 13) {
			trick[trickCount] = createTrick(tr.getWinner(), trump);
		}
	}
	
	public int getTurn() {
		return trick[trickCount].getTurn();
	}
	
	public Packet getHand(int seat) {
		return hand[seat];
	}
	
	public Packet[] getHand() {
		return hand;
	}
	
	public int getTrump() {
		return trump;
	}
	
	/**
	 * 現在まででプレイされているトリック数を取得する。
	 * プレイ中のトリックについてはカウントされない。
	 */
	public int getTricks() {
		return trickCount;
	}
	
	/**
	 * 現在プレイ中のトリックを返す。
	 */
	public Trick getTrick() {
		if (trickCount == 13) return trick[12];
		return trick[trickCount];
	}
	
	/**
	 * 指定されたラウンドのトリックを返却します。
	 * ラウンドは 0 から 12 までの整数値です。
	 */
	public Trick getTrick(int index) {
		return trick[index];
	}
	
	public Trick[] getAllTricks() {
		if (trick[0] == null) return null;
		if (hand[0] == null) return null;
		
		int n = trickCount + 1;
		if (trickCount == 13) n--;
		Trick[] result = new Trick[n];
		System.arraycopy(trick, 0, result, 0, n);
		
		return result;
	}
	
	public boolean isFinished() {
		return ( (trickCount == 13) && (trick[12].size() == 4) );
	}
	
	protected Trick createTrick(int lead, int trump) {
		return new TrickImpl(lead, trump);
	}
	
	public void reset() {
		hand		= new Packet[4];
		trick		= new Trick[13];
		trickCount	= 0;
		trump		= -1;
	}
	
	/**
	 * プレイにおける undo() を行います。最後にプレイされたカードを返却します。
	 *
	 * 初期状態では、IllegalStatusException をスローします。
	 */
	public Card undo() {
		if (trick[0] == null)
			throw new IllegalStatusException("初期状態のため、undo() できません");
		if ((trickCount == 0)&&(trick[0].size() == 0))
			throw new IllegalStatusException("初期状態にあるため、undo() できません");
		
		if (trickCount == 13) {
			trickCount--;
		} else if (trick[trickCount].size() == 0) {
			// 現在リードの状態
			trick[trickCount] = null;
			trickCount--;
		}
		
		// だれのハンドに戻すか
		int seatToBePushbacked = (trick[trickCount].getTurn() + 3) % 4;
		
		// 最後のプレイを取得する
		Card lastPlay = trick[trickCount].draw();
//		lastPlay.turn(false); // このオブジェクトは Dummy が誰かを知らない
		hand[seatToBePushbacked].add(lastPlay);
		hand[seatToBePushbacked].arrange();
		
		// 一番はじめにもどすための特殊処理( reset() 相当の処理 )
		if ((trickCount == 0)&&(trick[0].size() == 0)) {
			// setContract 以前の状態まで戻す
			hand[0] = hand[1] = hand[2] = hand[3] = null;
			trick[0]	= null;
			trump		= -1;
		}
		return lastPlay;
	}
	
	public String toString() {
		String result = "";
		
		result += "N : " + hand[Board.NORTH]	+ "\n";
		result += "E : " + hand[Board.EAST]		+ "\n";
		result += "S : " + hand[Board.SOUTH]	+ "\n";
		result += "W : " + hand[Board.WEST]		+ "\n";
		
		result += "\n";
		
		if (trickCount < 13) result += trick[trickCount];
		
		result += "\n\n";
		for (int i = trickCount-1; i >= 0; i--) {
			if (i < 13) {
				result +="[";
				if (i < 10) result += " ";
				result += String.valueOf(i);
				result += "]"+trick[i];
				result += "  win="+trick[i].getWinnerCard();
				result += "  " + Board.SEAT_STRING[trick[i].getWinner()]+"\n";
			}
		}
		
		return result;
	}
}

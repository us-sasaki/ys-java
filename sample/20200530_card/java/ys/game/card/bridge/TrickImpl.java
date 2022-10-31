package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

/**
 * Trick は、場に出ているトリック、プレイされたトリックをパックする。
 * Trick では、１トリックを構成するカードの保持の他、トリックのウィナー
 * の判定を行う。
 * hand に関する情報は保持しない。
 *
 * @version		draft		29, August 2002
 * @author		Yusuke Sasaki
 */
public class TrickImpl extends PacketImpl implements Trick {
	/** leader の席番号 */
	private int		leader;
	
	/** １トリックが終了した場合、winner 席番号が設定される。 */
	private int		winner;
	
	/** １トリックが終了した場合、winner となったカードが設定される。 */
	private Card	winnerCard;
	
	/** トランプ */
	private int		trump;
	
/*-------------
 * Constructor
 */
	/**
	 * leader と trump を指定して TrickImpl を作成します。
	 */
	public TrickImpl(int leader, int trump) {
		super();
		this.leader		= leader;
		this.winner		= -1;
		this.winnerCard	= null;
		this.trump		= trump;
	}
	
	/**
	 * コピーコンストラクタ
	 */
	public TrickImpl(Trick src) {
		this(src.getLeader(), src.getTrump());
		
		int size = src.size();
		for (int i = 0; i < size; i++) {
			Card card = src.peek(i);
			add(card);
		}
		cardOrder = src.getCardOrder();
	}
	
/*-----------
 * Overrides
 */
	/**
	 * Trick では、任意の位置へのカード挿入は許可されません。
	 * RuntimeException がスローされます。
	 */
	public void insertAt(Card card, int index) {
		throw new RuntimeException(
					"Trick では、insertAt(Card, int)は使用できません。");
	}
	
	/**
	 * Trick は４枚までしか保持せず、４枚そろった時には Winner がきまる.
	 */
	public void add(Card card) {
		if (isFinished())
			throw new IllegalStateException(
					"すでに終了した Trick に対して add(Card) は実行できません。");
		
		super.add(card);
		
		if (size() == 4) setWinner();
	}
	
	/**
	 * Winner を lead, trump などから決定します。
	 */
	private void setWinner() {
		if (size() == 0) {
			winnerCard = null;
			return;
		}
		
		// winner をセットする
		winnerCard = peek(0);
		winner = 0;
		int starter = winnerCard.getSuit();
		for (int i = 1; i < size(); i++) {
			Card c = peek(i);
			if (winnerCard.getSuit() == trump) { // NO_TRUMP のときはここにこない
				if ((c.getSuit() == trump)&&(winnerCard.getValue() != Card.ACE)) {
					if ((c.getValue() > winnerCard.getValue())||
						(c.getValue() == Card.ACE)) {
						winnerCard = c;
						winner = i;
					}
				}
			} else { // winner のスーツは場のスーツ
				if (c.getSuit() == trump) {
					winnerCard = c;
					winner = i;
				}
				else if ((c.getSuit() == starter)&&(winnerCard.getValue() != Card.ACE)) {
					if ((c.getValue() > winnerCard.getValue())||(c.getValue() == Card.ACE)) {
						winnerCard = c;
						winner = i;
					}
				}
			}
		}
	}

	/**
	 * Trick では、カード順序の変更は許可されません。
	 * RuntimeException がスローされます。
	 */
	public void arrange() {
		throw new RuntimeException(
					"Trick では、arrange()は使用できません。");
	}
	
	/**
	 * Trick では、カード順序の変更は許可されません。
	 * RuntimeException がスローされます。
	 */
	public void shuffle() {
		throw new RuntimeException(
					"Trick では、shuffle()は使用できません。");
	}
	
/*-----------------------------------
 * instance methods(Trick固有の処理)
 */
	/**
	 * はじめに台札を出す座席の番号を取得します。
	 *
	 * @return		leader の座席番号
	 */
	public int getLeader() {
		return leader;
	}
	
	/**
	 * 設定されている Trump を取得します。
	 *
	 * @return		トランプスート
	 */
	public int getTrump() {
		return trump;
	}
	
	/**
	 * 次は誰の番かを返す.
	 * NESW の順である. Dummy が返ることもある.
	 */
 	public int getTurn() {
 		return ((size() + leader) % 4);
 	}
 	
	/**
	 * このトリックが終っているかテストする。
	 * 終っている場合、getWinner(), getWinnerCard() の値が有効となる。
	 */
	public boolean isFinished() {
		return (size() == 4);
	}
	
	/**
	 * 台札を取得する。
	 * 台札が出ていない場合、null が返る。
	 */
	public Card getLead() {
		if (size() == 0) return null;
		return peek(0);
	}
	
	public int getWinner() {
		if (winnerCard == null) setWinner();
		return (leader + winner) % 4;
	}
	
	/**
	 * Winner カードを得る.
	 * まだプレイ中であった場合、null が返る仕様であったが、途中での winner も
	 * 返却するように変更された。(2002/8/29)
	 *
	 * @return		winnerカード
	 */
	public Card getWinnerCard() {
		if (winnerCard == null) setWinner();
		return winnerCard;
	}
	
	public String toString() {
		String result = "Leader:" + Board.SEAT_STRING[leader];
		return result + super.toString();
	}
}

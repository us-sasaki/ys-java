package ys.game.card.bridge;
/*
 * 2001/ 7/23  setName(), getName() 追加
 */

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.PacketFactory;
import ys.game.card.CardOrder;
import ys.game.card.NaturalCardOrder;

/**
 * ブリッジにおける１ボードをパックする。
 * BoardManagerに対する受動的なオブジェクトで、状態変化を起こす
 * メソッドを提供する。
 * Playerに対しては状態参照のみを許す。
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public class BoardImpl implements Board {
	
/*---------------
 * Vulnerability
 */
	// O | - +   | - + O   - + O |   + O | -
	private static final int[] VUL = { 0, 1, 2, 3,  1, 2, 3, 0,
											2, 3, 0, 1, 3, 0, 1, 2 };
	
	/** この Board の名前 */
	private String			name;
	
	/** ビッド履歴などのオークション部分の機能を提供する。 */
	private BiddingHistory	bidding;
	
	/** プレイ部分の機能を提供する。 */
	private PlayHistory		play;
	
	/** 現在のステータス */
	private int				status;
	
	/** このボードの vulnerability */
	private int				vul;
	
	/** 既知のカード */
	private Packet			openCards;
	
/*-------------
 * Constructor
 */
	/**
	 * 与えられた ボードナンバーの初期ボードを作成する。
	 *
	 * @param		num		ボード番号(1以上)
	 */
	public BoardImpl(int num) {
		this((num - 1)%4, VUL[(num - 1)%16]);
	}
	
	/**
	 * 与えられたディーラー、バルの初期ボードを作成します。
	 */
	public BoardImpl(int dealer, int vul) {
		// 実処理クラスの設定
		bidding	= new BiddingHistoryImpl(dealer);
		play	= new PlayHistoryImpl();
		
		this.vul	= vul;
		status		= DEALING;
	}
	
	/**
	 * 指定された Board と同一の状態の BoardImpl のインスタンスを新規に生成します。
	 * BiddingHistory, PlayHistory については、コピー元のもののインスタンスが使用
	 * されます。
	 */
	public BoardImpl(Board src) {
		name	= src.getName();
		bidding	= src.getBiddingHistory();
		play	= src.getPlayHistory();
		status	= src.getStatus();
		vul		= src.getVulnerability();
		openCards = src.getOpenCards();
	}
	
/*----------------------------------------------------------
 * 状態変化メソッド(公開されないパッケージレベルのメソッド)
 */
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		if (name == null) return "";
		return name;
	}
	
	public void deal() {
		if (status != DEALING)
			throw new IllegalStatusException("deal() は DEALING 状態のみで実行可能です。");
		
		Packet[] hand = new Packet[4];
		for (int i = 0; i < 4; i++) hand[i] = new PacketImpl();
		
		Packet pile = PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER);
		openCards = new PacketImpl();
		
		// holder を設定する
		openCards.add(pile.peek());
		openCards.draw(pile.peek());
		
		// カードを裏向きにして配る
		for (int i = 0; i < pile.size(); i++) {
			Card card = pile.peek(i);
			card.turn(false);
		}
		pile.shuffle();
		PacketFactory.deal(pile, hand);
		for (int i = 0; i < 4; i++) hand[i].arrange();
		
		play.setHand(hand);
		
		status = BIDDING;
	}
	
	public void deal(Packet[] hand) {
		if (status != DEALING)
			throw new IllegalStatusException("deal() は DEALING 状態のみで実行可能です。");
		
		// カードを裏向きにしておく
		for (int i = 0; i < hand.length; i++) {
			for (int j = 0; j < hand[i].size(); j++) {
				hand[i].peek(j).turn(false);
			}
		}
		
		openCards = new PacketImpl();
		// holder を設定する
		openCards.add(hand[0].peek());
		openCards.draw(hand[0].peek());
		
		play.setHand(hand);
		status = BIDDING;
	}
	
	/**
	 * この Board で使用している BiddingHistory を取得します。
	 *
	 * @return		BiddingHistory
	 */
	public BiddingHistory getBiddingHistory() {
		return bidding;
	}
	
	/**
	 * この Board で使用している PlayHistory を取得します。
	 *
	 * @return		PlayHistory
	 */
	public PlayHistory getPlayHistory() {
		return play;
	}
	
	/**
	 * この Board で使用する PlayHistory を指定します。
	 *
	 * @param		設定したい playHistory
	 */
	public void setPlayHistory(PlayHistory playHistory) {
		this.play = playHistory;
	}
	
	public void setContract(Bid contract, int declarer) {
		bidding.setContract(contract, declarer); // may throw IllegalStatusException
		status = OPENING;
		this.play.setContract((getDeclarer() + 1)%4, getTrump() );
		
		reorderHand();
	}
	
	/**
	 * ビッド、プレイを行う。状態が変化する。
	 */
	public void play(Object play) {
		if (!this.allows(play))
			throw new IllegalPlayException(play.toString() + "は行えません。");
		switch (status) {
		
		case BIDDING:
			if (!(play instanceof Bid))
				throw new IllegalPlayException("ビッドしなければなりません。" + play);
			bidding.bid((Bid)play);
			if (bidding.isFinished()) {
				if (getContract().getKind() == Bid.PASS) {
					//
					// Pass out
					//
					status = SCORING;
					break;
				}
				status = OPENING;
				this.play.setContract((getDeclarer() + 1)%4, getTrump() );
				
				reorderHand();
			}
			break;
			
		case OPENING:
		case PLAYING:
			if (!(play instanceof Card))
				throw new IllegalPlayException("プレイしなければなりません。" + play);
			this.play.play((Card)play);
			openCards.add((Card)play);
			
			if (status == OPENING) dummyOpen();
			// 将来ネットワークに対応する場合、マスター以外のボードでは
			// dummy は Unspecified となっている。opening lead と同時に
			// dummy hand を通知し、dummyOpen() 以前にボードのハンドを
			// specified にするべき。
			//
			
			if (this.play.isFinished()) status = SCORING;
			else status = PLAYING;
			break;
			
		case DEALING:
		case SCORING:
			throw new IllegalPlayException(play.toString() + "は行えません。");
		
		default:
			throw new InternalError("Board.status が不正な値"
							+ status + "になっています。");
		}
	}
	
	/**
	 * １つ前の状態に戻します。
	 * 状態遷移を考える
	 *
	 * DEALING
	 *    |                           pass out
	 *    +----->BIDDING---------------------------------+
	 *    |              ＼                              |
	 *    +----------------->OPENING                     |
	 *                          |                        |
	 *                          +-------->PLAYING        |
	 *                          O.L.         |           V
	 *                                       +------->SCORING
	 *                                       last play
	 */
	public void undo() {
		switch (status) {
		case DEALING:
			throw new IllegalStatusException("DEALING 状態で undo() はできません");
		
		case BIDDING:
			if (bidding.countBid() == 0) {
				status = DEALING;
				break;
			}
			bidding.undo();
			break;
		
		case OPENING:
			if (bidding.countBid() == 0) {
				// setContract されている
				status = DEALING;
				bidding.reset(bidding.getDealer());
				break;
			}
			status = BIDDING;
			bidding.undo();
			break;
		
		case SCORING:
			status = PLAYING;
			// fall through
			
		case PLAYING:
			//
			// まず、PlayHistory を undo() する
			//
			Card lastPlay = play.undo(); // PLAYING なので、Exception はでないはず
			int turn = play.getTurn();
			if (turn != getDummy()) lastPlay.turn(false);
			openCards.draw(lastPlay);
			
			// undo() の結果、OPENING になる場合
			if ((getTricks() == 0)&&(play.getTrick() == null)) {
				// O.L.にもどる
				dummyClose();
				status = OPENING;
			}
			break;
			
		default:
			throw new InternalError("status が異常値 " + status + " になっています");
		}
	}
	
	/**
	 * ビッド終了後に、トランプスートが左に来るように並び替えます。
	 */
	private void reorderHand() {
		CardOrder order; // stateless object
		
		switch (getTrump()) {
		case Bid.HEART:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_HEART);
			break;
		case Bid.DIAMOND:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_DIAMOND);
			break;
		case Bid.CLUB:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_CLUB);
			break;
		default:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_SPADE);
			break;
		}
		
		getHand(getDummy()).setCardOrder(order);
		getHand(getDummy()).arrange();
		getHand(getDeclarer()).setCardOrder(order);
		getHand(getDeclarer()).arrange();
	}
	
	/**
	 * オープニングリードの後に呼ばれ、ダミーの手を表向きに変更します。
	 * また、場に出ているカードにダミーハンドを追加します。
	 */
	protected void dummyOpen() {
		Packet dummy = getHand(getDummy());
		dummy.turn(true);
		openCards.add(dummy);
	}
	
	/**
	 * undo() 時に Opening Lead 状態にもどすための処理を行います。
	 * ダミーの手を裏向きに変更します。
	 */
	protected void dummyClose() {
		Packet dummy = getHand(getDummy());
		dummy.turn(false);
		openCards.sub(dummy);
		
		if (openCards.size() != 0) throw new InternalError("openCards 枚数に矛盾があります");
	}
	
/*------------------------------
 * 状態参照メソッド(公開される)
 */
	/**
	 * プレイ可能であるかテストする。
	 *
	 * @return		true：可能    false:不可能
	 */
	public boolean allows(Object play) {
		switch (status) {
		
		case BIDDING:
			if (play instanceof Bid) return bidding.allows((Bid)play);
			return false;
			
		case OPENING:
		case PLAYING:
			if (play instanceof Card) return this.play.allows((Card)play);
			return false;
			
		case DEALING:
		case SCORING:
			return false;
			
		default:
			throw new InternalError("Board.status が不正な値"
							+ status + "になっています。");
		}
	}
	
	public int getStatus() {
		return status;
	}
	
	/**
	 * ステータスが Board.OPENING, Board.PLAYING の場合にプレイ順を
	 * 示す定数を返却します。
	 *
	 * @return		プレイ順を示す定数
	 */
	public int getPlayOrder() {
		switch (status) {
		
		case OPENING:
		case PLAYING:
			return getTrick().size();
		
		case DEALING:
		case BIDDING:
		case SCORING:
			return -1;
		
		default:
			throw new InternalError("Board.status が不正な値"
							+ status + "になっています。");
		}
	}
	
	/**
	 * Vulnerability を取得します。
	 *
	 * @return		このボードの vulnerability。
	 *				(VUL_NONE, VUL_NS, VUL_EW, VUL_BOTH)
	 */
	public int getVulnerability() {
		return vul;
	}
	
	/**
	 * だれの番であるかを席番号で返却する。
	 * DEALING, SCORING のステータスでは -1 が返却される。
	 */
	public int getTurn() {
		switch (status) {
		
		case BIDDING:
			return bidding.getTurn();
			
		case OPENING:
		case PLAYING:
			return play.getTurn();
			
		case DEALING:
		case SCORING:
			return -1;
			
		default:
			throw new InternalError("Board.status が不正な値"
							+ status + "になっています。");
		}
	}
	
	/**
	 * だれがプレイする番か取得します。
	 * getTurn() との違いは、プレイのとき、ダミーの番でディクレアラーの
	 * 席番号が返される点です。
	 */
	public int getPlayer() {
		int seat = getTurn();
		
		switch (status) {
		
		case OPENING:
		case PLAYING:
			if (seat == getDummy()) return getDeclarer();
			return seat;
			
		case BIDDING:
			return seat;
			
		case DEALING:
		case SCORING:
		default:
			return -1;
		}
	}
	
	/**
	 * (現在までの)最終コントラクトを取得する。
	 * ビッドがまだ行われていない場合、null が返る。
	 */
	public Bid getContract() {
		return bidding.getContract();
	}
	
	/**
	 * (現在までの)最終トランプを取得します。
	 * ビッドがまだ行われていない場合、-1 が返ります。
	 */
	public int getTrump() {
		Bid contract = getContract();
		if (contract == null) return -1;
		return contract.getSuit();
	}
	
	/**
	 * (現在までで決定している)ディクレアラーの席番号を取得する。
	 * ビッドがまだ行われていない場合、-1 が返る。
	 */
	public int getDeclarer() {
		return bidding.getDeclarer();
	}
	
	/**
	 * (現在までで決定している)ダミーの席番号を取得する。
	 * ビッドがまだ行われていない場合、-1 が返る。
	 */
	public int getDummy() {
		int dec = bidding.getDeclarer();
		if (dec == -1) return -1;
		return (dec + 2) % 4;
	}
	
	/**
	 * (ビッドを開始する)ディーラーの席番号を取得する。
	 * ビッドがまだ行われていない場合、-1 が返る。
	 */
	public int getDealer() {
		return bidding.getDealer();
	}
	
	/**
	 * ハンドの情報を取得する。UnspecifiedCardが含まれることもある。
	 */
	public Packet getHand(int seat) {
		return play.getHand(seat);
	}
	
	/**
	 * すべてのハンドの状態を取得する。
	 */
	public Packet[] getHand() {
		return play.getHand();
	}
	
	/**
	 * 現在までにプレイされたトリック数を取得する。
	 */
	public int getTricks() {
		return play.getTricks();
	}
	
	/**
	 * 現在場に出ているトリックを取得する。
	 */
	public Trick getTrick() {
		return play.getTrick();
	}
	
	/**
	 * プレイされた過去のトリックすべてを取得します。
	 * 本処理は、PlayHistory.getAllTricks() へ委譲しています。
	 *
	 * @see		ys.game.card.bridge.PlayHistory#getAllTricks()
	 */
	public Trick[] getAllTricks() {
		return play.getAllTricks();
	}
	
	/**
	 * 指定された座席がバルであるか判定します。
	 */
	public boolean isVul(int seat) {
		int mask = 0;
		if ( (seat == NORTH)||(seat == SOUTH) ) mask = 1;
		else mask = 2;
		
		if ((vul & mask) > 0) return true;
		return false;
	}
	
	/**
	 * プレイされたカード、ダミーハンドといった既知のカードを返します。
	 * 思考ルーチンで使用されることを期待しているメソッドです。
	 * openCards として認識されるものは、すでに場に出たカードとダミーのハンドです。
	 *
	 * @return		既知のカードからなる Packet
	 */
	public Packet getOpenCards() {
		return openCards;
	}
	
	/**
	 * 文字列表現を得る。
	 */
	public String toString() {
		String result = "---------- Board Information ----------";
		result += "\n  [    Status     ]  : " + STATUS_STRING[status];
		result += "\n  [ Vulnerability ]  : " + VUL_STRING[vul];
		result += "\n  [    Dealer     ]  : " + SEAT_STRING[getDealer()];
		result += "\n  [   Declarer    ]  : ";
		if (getDeclarer() == -1) result += "none";
		else result += SEAT_STRING[getDeclarer()];
		result += "\n\n  [Bidding History]\n";
		result += bidding.toString();
		result += "\n  [  Table Info   ]\n";
		result += play.toString() + "\n";
		
		return result;
	}
	
	public void reset(int num) {
		reset((num - 1)%4, VUL[(num - 1)%16]);
	}
	
	public void reset(int dealer, int vul) {
		bidding.reset(dealer);
		play.reset();
		
		this.vul = vul;
		status = DEALING;
	}
	
	public String toText() {
		StringBuffer s = new StringBuffer();
		String nl = "\n";
		
		s.append(name);
		s.append(nl);
		s.append("----- コントラクト -----");
		s.append(nl);
		s.append("contract：");	s.append(getContract().toString());
		s.append(" by " + SEAT_STRING[getDeclarer()]);	s.append(nl);
		s.append("vul     ：");	s.append(VUL_STRING[getVulnerability()]);	s.append(nl);
		
		s.append(nl);
		s.append("----- オリジナルハンド -----");
		s.append(nl);
		
		Packet[] hands = BridgeUtils.calculateOriginalHand(this);
		
		// NORTH
		for (int suit = 4; suit >= 1; suit--) {
			s.append("               ");
			s.append(getHandString(hands, 0, suit));
			s.append(nl);
		}
		
		// WEST, EAST
		for (int suit = 4; suit >= 1; suit--) {
			String wstr = getHandString(hands, 3, suit) + "               ";
			wstr = wstr.substring(0, 15);
			s.append(wstr);
			switch (suit) {
			case 4:
			s.append("    N          "); break;
			case 3:
			s.append("W       E      "); break;
			case 2:
			s.append("               "); break;
			case 1:
			s.append("    S          "); break;
			default:
			}
			
			s.append(getHandString(hands, 1, suit));
			s.append(nl);
		}
		// SOUTH
		for (int suit = 4; suit >= 1; suit--) {
			s.append("               ");
			s.append(getHandString(hands, 2, suit));
			s.append(nl);
		}
		
		// ビッド経過
//		s.append(bidding.toString());
//		s.append(nl);
		
		// プレイライン
		s.append(nl);
		s.append("----- プレイ -----");
		s.append(nl);
		
		s.append("    1  2  3  4  5  6  7  8  9 10 11 12 13");
		s.append(nl);
		
		Trick[] trick = getAllTricks();
		StringBuffer[] nesw = new StringBuffer[4];
		for (int i = 0; i < 4; i++) {
			nesw[i] = new StringBuffer();
			nesw[i].append("NESW".substring(i,i+1));
			nesw[i].append(' ');
			if (getTricks() > 0) {
				int leaderSeat = trick[0].getLeader();
				if (i == leaderSeat) nesw[i].append('-');
				else nesw[i].append(' ');
			}
		}
		
		for (int i = 0; i < getTricks(); i++) {
			int leaderSeat = trick[i].getLeader();
			int winnerSeat = trick[i].getWinner();
			for (int j = 0; j < 4; j++) {
				int seat = (j + leaderSeat) % 4;
				nesw[seat].append(trick[i].peek(j).toString().substring(1));
				if (seat == winnerSeat) nesw[seat].append('+');
				else nesw[seat].append(' ');
			}
		}
		for (int i = 0; i < 4; i++) {
			s.append(nesw[i]);
			s.append(nl);
		}
//				
//		for (int i = 0; i < getTricks(); i++) {
//			if (i < 13) {
//				s.append("[");
//				if (i < 9) s.append(" ");
//				s.append(String.valueOf(i+1));
//				s.append("]"+getAllTricks()[i]);
//				s.append("  win:"+getAllTricks()[i].getWinnerCard());
//				s.append(" " + Board.SEAT_STRING[getAllTricks()[i].getWinner()]+"\n");
//			}
//		}
//		s.append(nl);
//		
		if (getStatus() == SCORING) {
			s.append(nl);
			s.append("----- 結果 -----");
			s.append(nl);
			// メイク数
			int win		= countWinners();
			int up		= win - getContract().getLevel() - 6;
			int make	= win - 6;
			
			if (up >= 0) {
				// メイク
				s.append(String.valueOf(make) + "メイク  ");
			} else {
				// ダウン
				s.append(String.valueOf(-up) + "ダウン  ");
			}
			
			s.append("("+win+"トリック)\nN-S側のスコア："+Score.calculate(this, SOUTH));
			s.append(nl);
		}
		return s.toString();
	}
	
	private String getHandString(Packet[] hand, int seat, int suit) {
		StringBuffer s = new StringBuffer();
		s.append("CDHS".substring(suit-1, suit));
		s.append(':');
		Packet oneSuit = hand[seat].subpacket(suit);
		oneSuit.arrange();
		for (int i = 0; i < oneSuit.size(); i++) {
			Card c = oneSuit.peek(i);
			if ((c.isHead())||(getOpenCards().contains(c))) {
				s.append(c.toString().substring(2));
			}
		}
		return s.toString();
	}
	
	private int countWinners() {
		Trick[]	tr			= getAllTricks();
		if (tr == null) return 0;
		
		int		win			= 0;
		int		declarer	= getDeclarer();
		
		// winner を数える(Board にあったほうが便利)
		for (int i = 0; i < tr.length; i++) {
			int winner = tr[i].getWinner();
			if ( ((winner ^ declarer) & 1) == 0 ) win++;
		}
		
		return win;
	}
}


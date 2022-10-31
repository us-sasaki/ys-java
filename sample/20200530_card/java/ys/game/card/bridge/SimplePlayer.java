package ys.game.card.bridge;

/*
 * 22, July 2001  countHonerPoint 関連を static 関数として BridgeUtils に切り出した
 */
import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

/**
 * 和美の考えた基本的なディフェンダープレイを行うクラスです。
 * ビッドはつねにパスします。
 *
 * @version		a-release		22, July 2001
 * @author		Yusuke Sasaki
 */
public class SimplePlayer extends Player {
	protected Card		lead;
	protected Board		board;
	protected Packet	hand;
	protected Packet	dummyHand;
	
	protected String	openingLead;
	
/*-------------
 * constructor
 */
	public SimplePlayer(Board board, int seat) {
		setBoard(board);
		setMySeat(seat);
	}
	
	public SimplePlayer(Board board, int seat, String ol) {
		this(board, seat);
		openingLead = ol;
	}
	
/*------------
 * implements
 */
	/**
	 * パスします。
	 *
	 * @return		パス
	 */
	public Bid bid() throws InterruptedException {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * 和美の考えた基本的なディフェンダープレイを行います。
	 *
	 * @return		和美の考えた基本的なプレイ
	 */
	public Card draw() throws InterruptedException {
		Thread.sleep(400); // 考えた振り
		
		board		= getBoard();
		hand		= getHand(); // = getMyHand()
		dummyHand	= getDummyHand();
		
		// リードの時
		if (getPlayOrder() == LEAD) return playInLeading();
		
		//
		// ラフできるときは（誰もまだラフしていなければ）ローラフし
		//（追加：すでに誰かがラフしているとき、
		//             オポーネントがラフしているとき、
		//                     オポーネントより強いトランプがあればチーペストにオーバーラフ、
		//                     オポーネントより強いトランプがなければディスカードする。
		//             パートナーだけがラフしているときはディスカードする。）
		//				パートナーが勝っているときはラフせず、ディスカードする。
		//            
		// ディスカードする優先順位は Winner, Kx, Qxx は低い。(未実装)
		//
		lead = getLead(); // lead でないので not null
		
		if (!hand.containsSuit(lead.getSuit())) { // スートフォローできない
			int trump = board.getContract().getSuit();
			Packet pack = hand.subpacket(trump);
			if (pack.size() > 0) {
				pack.arrange();
				return pack.peek(); // ローエストラフ
			} else {
				hand.shuffle();
				int suit = hand.peek().getSuit();
				hand.arrange();
				
				Packet pack2 = hand.subpacket(suit);
				pack2.arrange();
				return pack2.peek(); // ローエストディスカード
			}
		}
		
		if (getPlayOrder() == SECOND)	return playIn2nd();
		if (getPlayOrder() == THIRD)	return playIn3rd();
		if (getPlayOrder() == FORTH)	return playIn4th();
		
		throw new InternalError("playOrder が異常な値です。:"+getPlayOrder());
	}
	
	/**
	 * リードの時は、
	 *   Ａ）ウィナーがあればキャッシュ
	 *   Ｂ）ウィナーがない時
	 *      ＬＨＯはー＞ダミーのスートのうち、トランプ以外でアナー点が多く、８点以下の
	 *                  ところをうつ。多いところ優先。短いところ優先。
	 *      ＲＨＯは－＞現在パートナーとディクレアラーの持っている点の多いスーツを打つ。
	 */
	private Card playInLeading() {
		if ((board.getStatus() == Board.OPENING)&&(openingLead != null)&&(openingLead.length()==2)) {
			int suit = Card.SPADE;
			int value = 2;
			
			switch (openingLead.charAt(0)) {
			case 'S': suit = Card.SPADE; break;
			case 'H': suit = Card.HEART; break;
			case 'D': suit = Card.DIAMOND; break;
			case 'C': suit = Card.CLUB; break;
			}
			switch (openingLead.charAt(1)) {
			case 'A': value = Card.ACE; break;
			case 'K': value = Card.KING; break;
			case 'Q': value = Card.QUEEN; break;
			case 'J': value = Card.JACK; break;
			case 'T': value = 10; break;
			default: value = openingLead.charAt(1) - '0';
			}
			Card ol = getMyHand().peek(suit, value);
			if (ol != null) return ol;
		}
		
		if ((board.getStatus() != Board.OPENING)||(openingLead == null)||(openingLead.length()!=1)) {
//System.out.println("playInLeading()");
			Packet winner = getWinners(false); // before dummy
//System.out.println("playInLeading() winner = " + winner);
			if (winner.size() > 0) {	// ウィナーがあれば
				winner.shuffle(); // そのうちの一つをランダムに選択して返す
				return winner.peek(0);
			}
		}
		// ウィナーがない
//System.out.println("playInLeading(): no winner");
		if (getDummyPosition() == LEFT) {
//System.out.println("playInLeading(): no winner LHO");
			//-----
			// LHO
			//
			int determinedSuit = 0;
			if ((board.getStatus() == Board.OPENING)&&(openingLead != null)&&(openingLead.length()==1)) {
				switch (openingLead.charAt(0)) {
				case 'S': determinedSuit = Card.SPADE; break;
				case 'H': determinedSuit = Card.HEART; break;
				case 'D': determinedSuit = Card.DIAMOND; break;
				case 'C': determinedSuit = Card.CLUB; break;
				}
				// 持っていない場合、通常のスートの選び方にしたがう。
				if (!hand.containsSuit(determinedSuit)) determinedSuit = 0;
			}
			if (determinedSuit == 0) {
				int[] honerPts = BridgeUtils.countHonerPoint(dummyHand);
				
				int[] preference = new int[4]; // どのスーツを打ちたいかの評価点
				for (int i = 0; i < 4; i++) {
					preference[i] = 0;
					if (!hand.containsSuit(i + 1)) { // 持ってないスーツは問題外
						preference[i] = Integer.MIN_VALUE;
						continue;
					}
					// トランプの優先順位は低くする
					if (board.getContract().getSuit() == i + 1) continue;
					
					if (honerPts[i + 1] <= 8) preference[i] = honerPts[i + 1] * 1000;
					
					preference[i] -= dummyHand.countSuit(i + 1) * 100;
				}
				
				// スートを決定する。(細かい前提：CLUBが優先)
				int pref = Integer.MIN_VALUE;
				for (int i = 0; i < 4; i++) {
					if (preference[i] > pref) {
						pref = preference[i];
						determinedSuit = i + 1;
					}
				}
			}
			
			// スート決定→リードの規則にしたがってカードを選ぶ
			// リードの規則 
			// アナー（ＡＫＱＪ１０）がないときは、トップオブナッシング
			// アナーを含み、枚数が
			// １枚→それ
			// ２枚→上
			// ３枚以上→アナーシークエンス（ＡＫから１０９まで）があるか見る
			//                                 （インテリアシークエンスも含む）
			//           シークエンスあり→シークエンスの一番上
			//           シークエンスなし→４枚以上→４枚目
			//                             ３枚    →アナーが１枚なら３枚目
			//                                     →アナー２枚以上なら２枚目
			Packet pack = hand.subpacket(determinedSuit);
			pack.arrange();
			if (pack.size() == 1) return pack.peek(); // １枚のとき、それ
			if (pack.size() == 2) return pack.peek(0); // ２枚のとき、上
			if (pack.peek(0).getValue() < 10) return pack.peek(0); // トップオブナッシング
			
			// ３枚以上
			// シーケンスを探す
			Card top = null;
			int v1 = pack.peek(0).getValue();
			for (int i = 1; i < pack.size(); i++) {
				int v2 = pack.peek(i).getValue();
				if ((v1 - v2 == 1)&&(v2 > 8)) {
					top = pack.peek(i-1);
					break;
				}
				v1 = v2;
				if (v1 < 10) break;
			}
			if (top != null) return top; // トップオブシーケンス
			
			if (pack.size() >= 4) return pack.peek(3); // ４枚以上
			// ３枚のとき
			int honers = 0;
			for (int i = 0; i < pack.size(); i++) {
				if (pack.peek(i).getValue() > 9) honers++;
			}
			if (honers == 1) return pack.peek(2);
			return pack.peek(1);
			
		} else {
//System.out.println("playInLeading(): no winner RHO");
			//------------
			// RHO のはず
			//
			Packet knownCards = board.getOpenCards(); // dummy + played
			knownCards.add(hand);
			
			Packet unknownCards = knownCards.complement(); // declarer + pard
			
			int[] honerPts = BridgeUtils.countHonerPoint(unknownCards);
//System.out.println("playInLeading(): honerPts = " + honerPts[0] + "," + honerPts[1] + "," + honerPts[2] + "," + honerPts[3] + "," + honerPts[4]);
			
			int[] preference = new int[4]; // どのスーツを打ちたいかの評価点
			for (int i = 0; i < 4; i++) {
				preference[i] = 0;
				if (!hand.containsSuit(i + 1)) { // 持ってないスーツは問題外
					preference[i] = Integer.MIN_VALUE;
					continue;
				}
				// トランプの優先順位を低く
				if (board.getContract().getSuit() == i + 1) continue;
				preference[i] = honerPts[i + 1] * 1000;
				
				preference[i] += dummyHand.countSuit(i + 1) * 100;
			}
			
			// スートを決定する。(細かい前提：CLUBが優先)
			int determinedSuit = 0;
			int pref = Integer.MIN_VALUE;
			for (int i = 0; i < 4; i++) {
				if (preference[i] > pref) {
					pref = preference[i];
					determinedSuit = i + 1;
				}
			}
			
			// ローエストをうつ
			Packet pack = getMyHand().subpacket(determinedSuit);
			pack.arrange();
			
			return pack.peek();
		}
	}
	
	/**
	 * ２番手では、
	 * ・ウィナーがあれば出す（複数あれば下から）
	 * ・なければローエスト
	 */
	private Card playIn2nd() {
//System.out.println("playIn2nd()");
		int suit = lead.getSuit();
		
		boolean afterDummy = (getDummyPosition() == RIGHT);
		
		Packet winner = getWinners(afterDummy);
		
		Packet pack = hand.subpacket(suit);
		// win = pack.intersection(winner); のような実装が簡潔。
		
		winner.arrange();
		for (int i = winner.size() - 1; i >= 0; i--) {
			Card c = winner.peek(i);
			if ( (pack.contains(c))&&(c.getSuit() == suit) ) return c;
		}
		
		// ない
		pack.arrange();
		
		return pack.peek(); // ローエスト
	}
	
	/**
	 * ３番手では、
	 * ・ＬＨＯの場合、ダミーとプレイされたカードを見て、勝てるか考える。
	 *   勝てるならチーペストに勝つ。だめならローエスト。
	 * ・ＲＨＯの場合、ハイエストを出す（ただしダミーと自分を合わせたカードで
	 *   シークエンスとなる時はその内で最下位を出す）
	 */
	private Card playIn3rd() {
//System.out.println("playIn3rd()");
		if (getDummyPosition() == LEFT) {
//System.out.println("playIn3rd() : LHO");

			//
			// LHO
			//
			
			// 勝てるカードを持っているか？
			// ダミーのもっとも強いカードを選ぶ
			Card dummyStrongest;
			Packet dFollow = dummyHand.subpacket(lead.getSuit());
			
			if (dFollow.size() == 0) {
				dummyStrongest = dummyHand.peek(); // 適当なもの(trumpかもしれないが)
			}
			else {
				dFollow.arrange();
				dummyStrongest = dFollow.peek(0);
			}
			
			//
			// 将来をシミュレートする
			//
			Packet pack = hand.subpacket(lead.getSuit());
			pack.arrange();
			
			Card play = null;
			for (int i = 0; i < pack.size(); i++) {
				Trick virtual = new TrickImpl(getTrick());
				virtual.add(pack.peek(i));
				virtual.add(dummyStrongest);
				if (((virtual.getWinner() ^ getMySeat()) & 1) == 0) play = pack.peek(i);
			}
			if (play == null) play = pack.peek();
			
			return play;
		} else {
//System.out.println("playIn3rd() : RHO");
			//
			// RHO(単にハイエストを選ぶ、負けるカードしかない場合、ローエスト) 
			//                   ↑うまく機能してないかも
			//
			Packet pack = hand.subpacket(lead.getSuit());
			pack.arrange();
			
			Card c = pack.peek(0); // ハイエスト
			
			if (pack.size() == 1) return c;
			
			Trick virtual = new TrickImpl(getTrick());
			virtual.add(c);
			virtual.add(pack.peek(1));
			if (virtual.getWinner() == getMySeat()) return c;
			return pack.peek();
		}
	}
	
	/**
	 * ４番手では、一番安く勝つかローエスト
	 */
	private Card playIn4th() {
//System.out.println("playIn4th()");
		Packet pack = hand.subpacket(lead.getSuit());
		pack.arrange();
		Card play = null;
		
		for (int i = 0; i < pack.size(); i++) {
			Trick virtual = new TrickImpl(getTrick());
			virtual.add(pack.peek(i));
			if (((virtual.getWinner() ^ getMySeat()) & 1) == 0) play = pack.peek(i);
		}
		if (play == null) play = pack.peek();
		return play;
	}
	
	/**
	 * 自分のハンドの中でウィナーとなっているカードを抽出した Packet を返却します。
	 * ウィナーであることは、各スートにおいて今プレイされていないカードのうちもっとも
	 * 高いカードであることで判断します。
	 * 不確定な情報は使用しません。
	 * 
	 * @return		winner
	 */
	private Packet getWinners(boolean afterDummy) {
		Packet result = new PacketImpl();
		
		//
		// 残りのカードを抽出する
		//
		Packet rest = board.getOpenCards().complement();
		rest.add(getTrick());
		
		if (!afterDummy) rest.add(getDummyHand());
//System.out.println("getWinners() : rest = " + rest);
		
		// 残りのカードを各スートに分ける
		Packet[] suits = new Packet[4];
		
		for (int i = 0; i < 4; i++) {
			suits[i] = rest.subpacket(i+1);
			suits[i].arrange();	// 強いカードを小さいインデックスに
		}
		
		// 各スーツのウィナーを抽出する
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < suits[i].size(); j++) {
				Card winner = suits[i].peek(j);
//System.out.println("getWinners() : winner = " + winner);
				if (hand.contains(winner)) {
					result.add(winner); // 最高位を持っている場合、次の位も調べる
				} else {
					break;	// シークエンスが切れた場合、終了
				}
			}
		}
		
		result.arrange();
		return result;
	}
	
}

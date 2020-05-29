package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

/**
 * 和美の考えたディフェンダープレイを行うクラスです。
 * ビッドはつねにパスします。
 *
 * @version		making		27, July 2002
 * @author		Yusuke Sasaki
 */
public class SimplePlayer2 extends Player {
	protected Card		lead;
	protected Board		board;
	protected Packet	hand;
	protected Packet	dummyHand;
	
	protected String	openingLead;
	
/*-------------
 * constructor
 */
	public SimplePlayer2(Board board, int seat) {
		setBoard(board);
		setMySeat(seat);
	}
	
	public SimplePlayer2(Board board, int seat, String ol) {
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
	 * 和美の考えたディフェンダープレイを行います。
	 *
	 * @return		和美の考えたプレイ
	 */
	public Card draw() throws InterruptedException {
		Thread.sleep(400); // 考えた振り
		return draw2();
	}
	
	public Card draw2() {
		board		= getBoard();
		hand		= getHand(); // = getMyHand()
		dummyHand	= getDummyHand();
		lead		= getLead();
		
		int order = getPlayOrder();
		if (order == LEAD)		return playIn1st();
		//
		// ラフできるときは（誰もまだラフしていなければ）ローラフし
		//（追加：すでに誰かがラフしているとき、
		//             オポーネントがラフしているとき、
		//                     オポーネントより強いトランプがあればチーペストにオーバーラフ、
		//                     オポーネントより強いトランプがなければディスカードする。
		//             パートナーだけがラフしているときはディスカードする。）
		//				パートナーが勝っているときはラフせず、ディスカードする。
		//
		if (!hand.containsSuit(lead.getSuit())) { // スートフォローできない
			int trump = board.getContract().getSuit();
			Packet pack = hand.subpacket(trump); // NTのときは、空になる
			if (pack.size() > 0) {
				// ラフすることができる
				pack.arrange();
				if ((order == THIRD)&&(getDummyPosition()==LEFT)) {
					//--------------------------------
					// 三番手でかつ最後がダミーの場合
					//--------------------------------
					// ラフで勝てる場合、チーペストラフで勝つ
					// 勝てない、またはディスカードで勝てる(パートナーが勝っている)
					// 場合、ディスカード(discard()が選んだカード)する。
					//
					
					// 勝てるカードを持っているか？
					// ダミーのもっとも強いカードを選ぶ
					Card dummyStrongest;
					Packet dFollow = dummyHand.subpacket(lead.getSuit());
					
					if (dFollow.size() == 0) {
						dFollow = dummyHand.subpacket(trump);
					}
					if (dFollow.size() == 0) {
						dummyStrongest = dummyHand.peek(); // 適当なもの
					}
					else {
						// フォローできるときはそのスートの最大のもの
						// できないときはトランプの最大なもの
						dFollow.arrange();
						dummyStrongest = dFollow.peek(0);
					}
					
					//
					// 将来をシミュレートする(ダミーハンドからのプレイを行う)
					//
					
					// discard でも勝てる場合、discardするため、候補に追加
					
					Packet pack2 = hand.subpacket(board.getContract().getSuit());
					pack2.arrange();
					pack2.add(discard());
					
					Card play = null;
					for (int i = 0; i < pack2.size(); i++) {
						Trick virtual = new TrickImpl(getTrick());
						virtual.add(pack2.peek(i));
						virtual.add(dummyStrongest);
						if (isItOurSide(virtual.getWinner())) play = pack2.peek(i);
					}
					// だめなのでディスカード
					if (play == null) return discard();
					
					return play;
				}
				if (order == THIRD) {
					//--------------------------------------
					// 三番手で最後の一人はダミーでない場合
					//--------------------------------------
					Packet winner = getWinners2();
					if ( (winner.contains(lead))
							&&(isItOurSide(board.getTrick().getWinner())) ) {
						// パートナーはウィナーをプレイし、それが勝っているときにディスカード
						return discard();
					}
					// ローラフ
					pack.arrange();
					return pack.peek();
				}
				if (order == FORTH) {
					//--------------
					// 四番手の場合
					//--------------
					// パートナーがすでにプレイしている
					if (isItOurSide(board.getTrick().getWinner())) {
						// 自分たち（この場合パートナー）の勝ち
						
						
						// ディスカードする
						return discard();
					}
				}
				//--------------------------------------------
				// 二番手の場合、四番手でまだ勝っていない場合
				//--------------------------------------------
				// パートナーがプレイしていないか、勝っていない
				// (チーペストにオーバー)ラフを試みる
				for (int i = pack.size()-1; i >= 0; i--) {
					Card c = pack.peek(i);
					Trick virtual = new TrickImpl(board.getTrick());
					virtual.add(c);
					if (isItOurSide(virtual.getWinner()))	return c;
				}
				return discard();
			} else {
				// ディスカードする
				return discard();
			}
		}
		
		//--------------------------
		// スートフォローできる場合
		//--------------------------
		
		if (order == SECOND)	return playIn2nd();
		if (order == THIRD)		return playIn3rd();
		if (order == FORTH)		return playIn4th();
		
		throw new InternalError("Play Order が異常値("+order+")になっています");
	}
	
	/**
	 * ディスカードします。
	 */
	private Card discard() {
		Packet winners = getWinners(); // 自分のハンドのウィナー
		boolean winnerIsInOnlyOneSuit = false;
		for (int i = 0; i < 4; i++) {
			int s = winners.size();
			if ((s > 0)&&(s == winners.countSuit(i+1))) winnerIsInOnlyOneSuit = true;
		}
		
		// スートを選ぶ
		int trump = board.getContract().getSuit();
		
		// トランプしか持っていないとき
		// 仕方なくローエストトランプをディスカード
		if (hand.size() == hand.subpacket(trump).size()) return hand.peek();
		
		// 候補
		Packet p = new PacketImpl(); // winner 抜き候補
		Packet w = new PacketImpl(); // winner 入り候補
		for (int i = 0; i < hand.size(); i++) {
			Card c = hand.peek(i);
			if (c.getSuit() == trump) continue;	// トランプは候補としない
			w.add(c);
			if ((!winnerIsInOnlyOneSuit)&&winners.contains(c))
				continue;	// ウィナーを持っているスートが２以上のとき、ウィナーも候補としない
			p.add(c);
		}
		int suit;
		if (p.size() == 0) {
			// ウィナーしか持っていない
			w.shuffle();
			suit = w.peek().getSuit();
		} else {
			p.shuffle();
			suit = p.peek().getSuit();
		}
		Packet p2 = hand.subpacket(suit);
		p2.arrange();
		return p2.peek(); // ローエスト
	}
	
	//*********************************************************************//
	//  １番手のプレイ（共通部分）                                         //
	//*********************************************************************//
	
	/**
	 * リードの位置にいるときの手を考えます。
	 * オープニングリードかどうか、コントラクトがＮＴかどうかで４通りの関数に分岐します。
	 */
	private Card playIn1st() {
		if (board.getStatus() == Board.OPENING) {
			// オープニングリード
			
			//
			// 指定がある場合はそのカード
			//
			if (openingLead != null) {
				Card play = null;
				int suit	= -1;
				int value	= -1;
				
				try {
					switch (openingLead.charAt(0)) {
					case 'S': suit = Card.SPADE;	break;
					case 'H': suit = Card.HEART;	break;
					case 'D': suit = Card.DIAMOND;	break;
					case 'C': suit = Card.CLUB;		break;
					}
					switch (openingLead.charAt(1)) {
					case 'A': value = Card.ACE;		break;
					case 'K': value = Card.KING;	break;
					case 'Q': value = Card.QUEEN;	break;
					case 'J': value = Card.JACK;	break;
					case 'T': value = 10;			break;
					default: value = openingLead.charAt(1) - '0';
					}
				} catch (Exception e) {
				}
				if ( (suit != -1)&&(value != -1) ) {
					// 指定されたスートとバリューがともに有効
					Card ol = getMyHand().peek(suit, value);
					if (ol != null) return ol;
				}
				if ((suit != -1)&&(hand.containsSuit(suit))) {
					// スートのみが有効
					if (board.getContract().getSuit() == Bid.NO_TRUMP) return ntOpening(suit);
					return suitOpening(suit);
				}
			}
			
			if (board.getContract().getSuit() == Bid.NO_TRUMP) return ntOpening();
			return suitOpening();
		}
		if (board.getContract().getSuit() == Bid.NO_TRUMP) return ntLead();
		return suitLead();
	}
	
	//*********************************************************************//
	//  ＮＴオープニングリードのプレイ                                     //
	//*********************************************************************//
	
	/**
	 * ＮＴコントラクトの場合のオープニングリードを考えます
	 */
	private Card ntOpening() {
		//
		// 一番長いスートを選ぶ(同一枚数のときはランクの高いスート)
		//
		int suit = -1;
		int max  = -1;
		
		for (int i = 0; i < 4; i++) {
			int c = hand.countSuit(i+1);
			if (c > max) {
				max = c;
				suit = i+1;
			}
		}
		
		return ntOpening(suit);
	}
	
	/**
	 * ＮＴコントラクトのオープニングリードでスートまできまっている場合
	 */
	private Card ntOpening(int suit) {
		String suitPat = BridgeUtils.valuePattern(hand, suit);
		int value = -1;
		
		//
		// 所定のハンドパターンに合致するか
		//
		if (suitPat.startsWith("AKQ"))	{
			if (hand.countSuit(suit) >= 5) value = Card.ACE;
			else value = Card.KING;
		}
		if (suitPat.startsWith("KQJ"))	value = Card.KING;
		if (suitPat.startsWith("KQT"))	value = Card.KING;
		if ( (suitPat.startsWith("KQ"))&&(hand.countSuit(suit) == 3) ) value = Card.KING; // 2015/8/15 added
		if (suitPat.startsWith("AQJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("AQJ9"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJ9"))	value = Card.QUEEN;
		if ( (suitPat.startsWith("QJ"))&&(hand.countSuit(suit) == 3) ) value = Card.QUEEN; // 2015/8/15 added
		if (suitPat.startsWith("AKJT"))	{
			if (hand.countSuit(suit) >= 5) value = Card.ACE;
			else value = Card.KING;
		}
		if (suitPat.startsWith("AJT"))	value = Card.JACK;
		if (suitPat.startsWith("KJT"))	value = Card.JACK;
		if (suitPat.startsWith("JT"))	value = Card.JACK;
		if (suitPat.startsWith("AKT9"))	value = 10;
		if (suitPat.startsWith("AT9"))	value = 10;
		if (suitPat.startsWith("KT9"))	value = 10;
		if (suitPat.startsWith("QT9"))	value = 10;
		if (suitPat.startsWith("AQT9"))	value = 10;
		if (suitPat.startsWith("T9"))	value = 10;
		
		if (value > -1) return hand.peek(suit, value);
		
		Packet p = hand.subpacket(suit);
		p.arrange();
		if ( bridgeValue(p.peek(0)) < 10 ) {
			return p.peek(0); // トップオブナッシング
		}
		
		//
		// ４ｔｈベストが出せるか
		//
		int size = p.size();
		
		if (size >= 4) return p.peek(3);
		
		//
		// ４ｔｈベストが出せない
		//
		if (size == 3) return p.peek(2);
		return p.peek(0);
	}
	
	//*********************************************************************//
	//  スートコントラクトでのオープニングリードのプレイ                   //
	//*********************************************************************//
	
	/**
	 * スーツコントラクトのオープニングリードを考える。
	 */
	private Card suitOpening() {
		int max  = -1;
		Card play = null;
		
		for (int i = 0; i < 4; i++) {
			if ( (i+1) == board.getContract().getSuit() ) continue; // トランプは除外
			
			String suitPat = BridgeUtils.valuePattern(hand, i+1);
			
			// AK のあるスート (10 点)
			if ( (suitPat.startsWith("AK"))&&(max < 10) ) {
				max = 10;
				play = hand.peek(i+1, Card.KING);
			}
			// KQ のあるスート ( 9 点)
			if ( (suitPat.startsWith("KQ"))&&(max < 9) ) {
				max = 9;
				play = hand.peek(i+1, Card.KING);
			}
			// シングルトン ( 8 点)
			if ( (suitPat.length() == 1)&&(max < 8) ) {
				max = 8;
				play = hand.subpacket(i+1).peek();
			}
			// QJ のあるスート (7 点)
			if ( (suitPat.startsWith("QJ"))&&(max < 7) ) {
				max = 7;
				play = hand.peek(i+1, Card.QUEEN);
			}
			// ダブルトン (6 点)
			if ( (suitPat.length() == 2)&&(max < 6) ) {
				max = 6;
				Packet p = hand.subpacket(i+1);
				p.arrange();
				play = p.peek(0);
			}
		}
		if (play != null) return play;
		
		//
		// 決まらなかった(適当なスートを乱数で選ぶ)
		//
		for (int i = 0; i < 20; i++) {
			int suit = (int)(Math.random() * 4) + 1;
			if (suit == board.getContract().getSuit()) continue;
			
			if (hand.containsSuit(suit)) return suitOpening(suit);
		}
		//
		// ハンドがトランプスートのみからなっているなど稀な場合
		//
		return hand.peek();
	}
	
	/**
	 * スーツコントラクトでオープニングリードのスートが決まったとき
	 */
	private Card suitOpening(int suit) {
//		if (suit == Board.getContract().getSuit()) return 0;
		
		String suitPat = BridgeUtils.valuePattern(hand, suit);
//System.out.println("suitOpening(suit) . suitPat = " + suitPat);
		if (suitPat.startsWith("AK")) return hand.peek(suit, Card.KING);
		if (suitPat.startsWith("A")) return hand.peek(suit, Card.ACE);
		if (suitPat.startsWith("KQ")) return hand.peek(suit, Card.KING);
		if (suitPat.startsWith("QJ")) return hand.peek(suit, Card.QUEEN);
		if (suitPat.startsWith("KJT")) return hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("JT")) return hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("KT9")) return hand.peek(suit, 10);
		if (suitPat.startsWith("QT9")) return hand.peek(suit, 10);
		if (suitPat.startsWith("T9")) return hand.peek(suit, 10);
		if (suitPat.charAt(0) <= '9') return hand.peek(suit, suitPat.charAt(0) - '0');
		
		Packet p = hand.subpacket(suit);
		p.arrange();
		if ( bridgeValue(p.peek(0)) < 10 ) {
			return p.peek(0); // トップオブナッシング
		}
		if (p.size() >= 4) return p.peek(3);
		if (p.size() == 3) return p.peek(2);
		return p.peek(0);
	}
	
	//*********************************************************************//
	//  ＮＴでの１番手のプレイ                                             //
	//*********************************************************************//
	
	/**
	 * ＮＴコントラクトでのリード
	 *
	 *
	 */
	private Card ntLead() {
// ３．ディフェンダーが勝ったときのリード
// ・ＮＴの場合
// 　スートの決め方次の順位：
// 　（０）自分の手のウィナーの数＋パートナーのウィナーの数が
// 　　　　コントラクトを落とすのに十分なとき、自分のウィナーをキャッシュ
// 　（１）パートナーにウィナーのあるスート
// 　（２）Ｏ．Ｌ．と同じスート
// 　（３）今までディフェンダーが勝ったときにリードしたスート（最近から順に）
// 　（４）０～３のスートがないとき
// 　　　　ＬＨＯ（ディクレアラーの左手）：
//                ダミーのアナー（ＨＣＰで判断）の多いスート
// 　　　　ＲＨＯ（ディクレアラーの右手）：
// 　　　　　　　　ダミーのアナー（ＨＣＰで判断）の少ないスート　　
// 
// 　スート内でのカードの決め方を次の順位で決める　　　　　　　　
// 　　（１）そのスートの中で、トップがウィナーならキャッシュ
//　　　　　ＡＫからはＫ、他は上
// 　　（２）ＫＱからＫ
// 　　（３）ＱＪからＱ
// 　　（４）ＫＪＴ，ＪＴからＪ
// 　　（５）ＫＴ９，ＱＴ９、Ｔ９からＴ
// 　　（６）その他：現在２枚：上
// 　　　　　　　　　現在３枚：３枚目
// 　　　　　　　　　現在４枚以上→４枚目　
		//
		// (0) 自分とパートナーのウィナーの数がコントラクトを落とすのに十分なとき、
		//     自分のウィナーをキャッシュ
		Packet winners = getWinnersInNTLead();
		
		// ディフェンダー側のとったトリック
		int win = board.getTricks() - BridgeUtils.countDeclarerSideWinners(board); 
		if ( (winners.size() + win) > 7 - board.getContract().getLevel() ) {
			// コントラクトを落とせる
			for (int i = 0; i < winners.size(); i++) {
				if (hand.contains(winners.peek(i))) return winners.peek(i);
			}
		}

		//
		// (1) パートナーにウィナーのあるスート
		//		（自分の持っているスート）
		//
//		for (int i = 0; i < winners.size(); i++) {
//			Card c = winners.peek(0);
//			if (!hand.contains(c)) {
//				int suit = c.getSuit();
//				Packet p = hand.subpacket(suit);
//				p.arrange();
//				return p.peek(); // ローエスト
//			}
//		}
		
		int suit = chooseSuitInNTLead();
		return choosePlayInNTLead(suit);
		
	}
	
	/**
	 * NTコントラクトのリードスートを選びます。
	 */
// 　スートの決め方次の順位：
// 　（１）パートナーにウィナーのあるスート
// 　（２）Ｏ．Ｌ．と同じスート
// 　（３）今までディフェンダーが勝ったときにリードしたスート（最近から順に）
// 　（４）０～３のスートがないとき
// 　　　　ＬＨＯ（ディクレアラーの左手）：
//                ダミーのアナー（ＨＣＰで判断）の多いスート
// 　　　　ＲＨＯ（ディクレアラーの右手）：
// 　　　　　　　　ダミーのアナー（ＨＣＰで判断）の少ないスート　　
	private int chooseSuitInNTLead() {
		//
		// (1) パートナーにウィナーのあるスート
		//     ※ これは(2)と同値であるが一応実装してある
		//
		Packet winners = getWinnersInNTLead();
		for (int i = 0; i < winners.size(); i++) {
			Card c = winners.peek(i);
			if (!hand.contains(c)) { // パートナーが持っている
				if (hand.containsSuit(c.getSuit())) return c.getSuit();
			}
		}
		
		//
		// (2) O.L. と同じスート
		//
		if (board.getTricks() >= 1) {
			Card c = board.getAllTricks()[0].peek(0);
			if (hand.containsSuit(c.getSuit())) return c.getSuit();
		}
		
		//
		// (3) 今までディフェンダーが勝ったトリックのリード(最近から順に)
		//
		Trick[] trick = board.getAllTricks();
		for (int i = board.getTricks()-2; i >= 0; i--) {
			if (isItOurSide(trick[i].getWinner())) { // 自分たちの勝ち
				int suit = trick[i+1].getLead().getSuit();
				if (hand.containsSuit(suit)) return suit;
			}
		}
		
		//
		// (4) ０～３のスートがないとき
		//　　　　ＬＨＯ（ディクレアラーの左手）：
		//            ダミーのアナー（ＨＣＰで判断）の多いスート
		//　　　　ＲＨＯ（ディクレアラーの右手）：
		//    　　　　ダミーのアナー（ＨＣＰで判断）の少ないスート
		//
		int[] dummyHonerPoint = BridgeUtils.countHonerPoint(dummyHand);
		
		if (getDummyPosition() == LEFT) { // 自分はＬＨＯ
			int maxHcpSuit = 0;
			int maxHcpVal  = -1;
			for (int i = 1; i < 5; i++) {
				if (!dummyHand.containsSuit(i)) continue; // 持ってないスートは除外
				if (!hand.containsSuit(i)) continue;
				if (dummyHonerPoint[i] >= maxHcpVal) { // 同じ HCP では Major を優先させる
					maxHcpVal  = dummyHonerPoint[i];
					maxHcpSuit = i;
				}
			}
			if (maxHcpVal > -1) return maxHcpSuit;
		} else { // 自分はＲＨＯ
			int minHcpSuit = 0;
			int minHcpVal  = 100;
			for (int i = 1; i < 5; i++) {
				if (!dummyHand.containsSuit(i)) continue;
				if (!hand.containsSuit(i)) continue;
				if (dummyHonerPoint[i] <= minHcpVal) { // 同じ HCP では Major を優先させる
					minHcpVal  = dummyHonerPoint[i];
					minHcpSuit = i;
				}
			}
			if (minHcpVal < 100) return minHcpSuit;
		}
		// 持ってないスートが該当スートだった場合、適当に
		hand.shuffle();
		int suit = hand.peek(0).getSuit();
		hand.arrange();
		return suit;
	}
	
	/**
	 * NT コントラクトの場合で、リードするスートが決まった場合のプレイを行います。
	 *　スート内でのカードの決め方を次の順位で決める　　　　　　　　
	 *　　（１）そのスートの中で、トップがウィナーならキャッシュ
	 *　　（２）ＫＱからＫ
	 *　　（３）ＱＪからＱ
	 *　　（４）ＫＪＴ，ＪＴからＪ
	 *　　（５）ＫＴ９，ＱＴ９、Ｔ９からＴ
	 *　　（６）その他：現在２枚：上
	 *　　　　　　　　　現在３枚：３枚目
	 *　　　　　　　　　現在４枚以上→４枚目　
	 */
	public Card choosePlayInNTLead(int suit) {
		Packet candidacy = hand.subpacket(suit);
		if (candidacy.size() == 0)
			throw new InternalError("choosePlayInNTLead で指定されたスート("+suit+")を持っていません");
		candidacy.arrange();
		
		// （１）そのスートの中で、トップがウィナーならキャッシュ
		Packet winner = getWinners(); //getWinnersInNTLead();
		Card top = candidacy.peek(0);
		if (winner.contains(top.getSuit(), top.getValue())) return top;
		
		// （２）ＫＱからＫ
		if (BridgeUtils.patternMatch(hand, "KQ*", suit)) {
			return hand.peek(suit, Card.KING);
		}
		
		// （３）ＱＪからＱ
		if (BridgeUtils.patternMatch(hand, "QJ*", suit)) {
			return hand.peek(suit, Card.QUEEN);
		}
		
		// （４）ＫＪＴ，ＪＴからＪ
		if (BridgeUtils.patternMatch(hand, "KJT*", suit)) {
			return hand.peek(suit, Card.JACK);
		}
		if (BridgeUtils.patternMatch(hand, "JT*", suit)) {
			return hand.peek(suit, Card.JACK);
		}
		
		// （５）ＫＴ９，ＱＴ９、Ｔ９からＴ
		if (BridgeUtils.patternMatch(hand, "KT9*", suit)) {
			return hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(hand, "QT9*", suit)) {
			return hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(hand, "T9*", suit)) {
			return hand.peek(suit, 10);
		}
		
		// （６）その他：現在２枚：上
		// 　　　　　　　現在３枚：３枚目
		// 　　　　　　　現在４枚以上→４枚目　
		switch (candidacy.size()) {
		case 1:
		case 2:
			return candidacy.peek(0);
		case 3:
			return candidacy.peek(2);
		default:
			return candidacy.peek(3);
		}
	}
	
	//*********************************************************************//
	//  スートコントラクトでの１番手のプレイ                               //
	//*********************************************************************//
	
	//・スーツの場合
	//　スートの決め方次の順位：
	//　（０）自分の手のウィナーの数＋パートナーのウィナーの数が
	//　　　　コントラクトを落とすのに十分なとき、すべてキャッシュ
	//
	//　（１）ラフリスのスートを除外（それしかなければしかたない）
	//　　　　ラフリスのスートとは：
	//　　　　ダミーにもディクレアラーにもトランプが残っている状況で
	//　　　　　　　　（現在０枚と判明していないこと）　　　　
	//　　　　ダミーもディクレアラーもが現在０枚と判明しているスート　　　　　　　
	//　　　　　　　　　
	//　（２）パートナーのトランプスートが現在０枚と確定しないとき、かつ
	//　　　　パートナーに現在０枚と確定しているサイドスートがあるとき、そのスート
	//　　　　（ラフさせる）
	//
	//　（３）パートナーにウィナーのあるスート
	//　（４）Ｏ．Ｌ．と同じスート
	//　（５）いままでディフェンダーが勝ったときにリードしたスート（最近から順に）
	//　（６）以上のスートがないとき
	//　　　　ＬＨＯ（ディクレアラーの左手）：
	//　　　　　　　ダミーのアナー（ＨＣＰで判断）の多いスート
	//　　　　ＲＨＯ（ディクレアラーの右手）：　　　　　　　
	//　　　　　　　ダミーのアナー（ＨＣＰで判断）の少ないスート　　　　　　
	//
	//　スート内でのカードの決め方
	//　次の順位で決める　　　　　　　　
	//　　（１）そのスートの中で、トップがウィナーならキャッシュ
	//　　（２）ＫＱからＫ
	//　　（３）ＱＪからＱ
	//　　（４）ＫＪＴ，ＪＴからＪ
	//　　（５）ＫＴ９，ＱＴ９、Ｔ９からＴ
	//　　（６）その他：現在２枚：上
	//　　　　　　　　　現在３枚：３枚目
	//　　　　　　　　　現在４枚以上→４枚目
	private Card suitLead() {
		//
		// (0) 自分とパートナーのウィナーの数がコントラクトを落とすのに十分なとき、
		//     自分のウィナーをキャッシュ
		Packet winners = getWinnersInSuitLead();
		
		// ディクレアラー側のとったトリック
		int win = board.getTricks() - BridgeUtils.countDeclarerSideWinners(board);
		if ( (winners.size() + win) > 7 - board.getContract().getLevel() ) {
			// コントラクトを落とせる
			// そのとき、リードされた回数の少ないスートをうつ（●未実装）
			for (int i = 0; i < winners.size(); i++) {
				if (hand.contains(winners.peek(i))) return winners.peek(i);
			}
		}
		
		//　（１）ラフリスのスートを除外（それしかなければしかたない）
		//　　　　ラフリスのスートとは：
		//　　　　ダミーにもディクレアラーにもトランプが残っている状況で
		//　　　　　　　　（現在０枚と判明していないこと）　　　　
		//　　　　ダミーもディクレアラーもが現在０枚と判明しているスート　　　　　　　
		
		//
		// （１）をどのように実装するか
		//
		//       それぞれの実装の中で、決めたスートがラフリスだった場合、次点のスートに変更
		//       することとする(あまりきれいではないが、(2)以降はそれぞれ個性的なやりかたな
		//       ので、得点つきでスートを登録する方式よりシンプルになりそうなので)
		//
		
		int suit = chooseSuitInSuitLead();
		
		return choosePlayInSuitLead(suit);
	}
	
	/**
	 * スーツコントラクトの場合のリードスートを選びます。
	 */
	private int chooseSuitInSuitLead() {
		// (int[4][4][2])で、[座席][スート][最大(1) or 最小(0)]
		int[][][] dist = ThinkingUtils.countDistribution(board, getMySeat());
		int trump = board.getContract().getSuit();
		
		//
		// (2) パートナーのトランプスートが現在０枚と確定しないとき、かつ
		//　　　パートナーに現在０枚と確定しているサイドスートがあるとき、そのスート
		//　　　（ラフさせる）
		//
		
		// トランプスートが現在０枚と確定しないとき、
		if (dist[ getPartnerSeat() ][ trump-1 ][ ThinkingUtils.MAX ] > 0) {
			int sideSuit;
			for (sideSuit = 1; sideSuit < 5; sideSuit++) {
				if (sideSuit == trump) continue;
				if (!hand.containsSuit(sideSuit)) continue;
				// (2) ではラフリスは単純に除外する
				if (isRuflis(dist, sideSuit)) continue;
				
				// パートナーに現在０枚と確定しているサイドスートがあるとき
				if (dist[ getPartnerSeat() ][ sideSuit-1 ][ ThinkingUtils.MAX ] == 0) break;
			}
			if (sideSuit < 5) return sideSuit;
		}
		
		//
		// (3)パートナーにウィナーのあるスート
		//    これは(4)と同義なので、実装しない  → 実装してください
		//    これは難しいので未実装。かわりにＬＨＯの場合はＯＬと同じスート
		if ((board.getTricks() >= 1)&&(getDummyPosition() == LEFT)) {
			Card c = board.getAllTricks()[0].peek(0);
			int suit = c.getSuit();
			if ( (hand.containsSuit(suit))
				&& (!isRuflis(dist, suit)) ) return suit;
		}
		
		
		// (4) O.L. と同じスート  →やめる
		//     ただし、これがラフリスの場合スキップする 
		//
//		if (board.getTricks() >= 1) {
//			Card c = board.getAllTricks()[0].peek(0);
//			int suit = c.getSuit();
//			if (suit == trump) 
//			if ( (hand.containsSuit(suit))
//				&& (!isRuflis(dist, suit)) ) return suit;
//		}
		
		//
		//　（５）いままでディフェンダーが勝ったときにリードしたスート（最近から順に）→やめる
		//
		//        ラフリスの場合、スキップする
		//
//		Trick[] trick = board.getAllTricks();
//		for (int i = board.getTricks()-2; i >= 0; i--) {
//			if (isItOurSide(trick[i].getWinner())) { // 自分たちの勝ち
//				int suit = trick[i+1].getLead().getSuit();
//				if (isRuflis(dist, suit)) continue;
//				if (!hand.containsSuit(suit)) continue;
//				return suit;
//			}
//		}
		
		//　（６）以上のスートがないとき
		//　　　　ＬＨＯ（ディクレアラーの左手）：
		//　　　　　　　ダミーのアナー（ＨＣＰで判断）の多いスート　　やめる
		//　　　　ＲＨＯ（ディクレアラーの右手）：　　　　　　　
		//　　　　　　　ダミーのアナー（ＨＣＰで判断）の少ないスート　やめる
		//　　　　ただし、ラフリスのスートとトランプは除外する　　　　やめない
		//
		//　　　　●ダミーのオリジナルハンドを対象 にするように変更してください
		//        ●ＬＨＯは、ダミーのオリジナルのアナーの枚数が１枚が最優先、
		//         ２枚が次優先、次が０枚となる。
		//        ●ＲＨＯは、アナーの枚数が０枚が最優先、１枚が次優先、となる
		int[] dummyHonerPoint = BridgeUtils.countHonerPoint(dummyHand);
		Packet dummyOriginal = BridgeUtils.calculateOriginalHand(board)[board.getDummy()];
//System.out.println("Dummy Original = " + dummyOriginal);
		
		if (getDummyPosition() == LEFT) { // 自分はＬＨＯ
			int honers = -1;
			int honerSuit = -1;
			for (int i = 1; i < 5; i++) {
				if (!dummyHand.containsSuit(i)) { // ダミーがラフできるスートは除外
					if (dummyHand.containsSuit(trump))
						continue;
				}
				if (!hand.containsSuit(i)) continue;	// 持ってないスートは除外
				if (i == trump) continue;				// トランプは除外
				int h = BridgeUtils.countHoners(dummyOriginal, i);
				if (h > 2) continue;
				if (honers == -1) {
					honers = h;
					honerSuit = i;
					continue;
				}
				if (honers == 0) {
					if (h > 0) {
						honers = h;
						honerSuit = i;
					}
					continue;
				}
				if (honers == 2) {
					if (h == 1) {
						honers = h;
						honerSuit = i;
					}
					continue;
				}
				if (h != 1) { // h = 0 or 2
					honers = h;
					honerSuit = i;
				}
			}
			if (honers > -1) return honerSuit;
			
//			int maxHcpSuit = 0;
//			int maxHcpVal  = -1;
//			for (int i = 1; i < 5; i++) {
//				if (!dummyHand.containsSuit(i)) {
//					if (dummyHand.containsSuit(trump))
//						continue; // Dummyにラフされるスートは除外
//				}
//				if (!hand.containsSuit(i)) continue;	// 自分が持ってないスートは除外
//				if (isRuflis(dist, i)) continue; // ラフリスのスートは除外(ここにはこない)
//				if (i == trump) continue;	// トランプは除外
//				if (dummyHonerPoint[i] >= maxHcpVal) { // 同じ HCP では Major を優先させる
//					maxHcpVal  = dummyHonerPoint[i];
//					maxHcpSuit = i;
//				}
//			}
//			if (maxHcpSuit > 0)	return maxHcpSuit;
			// ラフリスのスートしかもっていない場合またはトランプしかない
			// ここにきて、下に抜ける
			
		} else { // 自分はＲＨＯ
			int honers = -1;
			int honerSuit = -1;
			for (int i = 1; i < 5; i++) {
				if (!dummyHand.containsSuit(i)) { // ダミーがラフできるスートは除外
					if (dummyHand.containsSuit(trump))
						continue;
				}
				if (!hand.containsSuit(i)) continue;	// 持ってないスートは除外
				if (i == trump) continue;				// トランプは除外
				int h = BridgeUtils.countHoners(dummyOriginal, i);
				if (h > 1) continue;
				if (honers == -1) {
					honers = h;
					honerSuit = i;
					continue;
				}
				if (honers == 1) {
					if (h == 0) {
						honers = h;
						honerSuit = i;
						continue;
					}
				}
			}
			if (honers > -1) return honerSuit;
//			int minHcpSuit = 0;
//			int minHcpVal  = 100;
//			for (int i = 1; i < 5; i++) {
//				if (!dummyHand.containsSuit(i)) {
//					if (dummyHand.containsSuit(trump))
//						continue; // Dummyにラフされるスートは除外
//				}
//				if (!hand.containsSuit(i)) continue;	// 自分が持っていないスートは除外
//				if (isRuflis(dist, i)) continue; // ラフリスのスートは除外(ここにはこない)
//				if (i == trump) continue; // トランプは除外
//				if (dummyHonerPoint[i] <= minHcpVal) { // 同じ HCP では Major を優先させる
//					minHcpVal  = dummyHonerPoint[i];
//					minHcpSuit = i;
//				}
//			}
//			if (minHcpSuit > 0) return minHcpSuit;
			// ラフリスのスートしかもっていない場合またはトランプしかない
			//ここにきて、下に抜ける
		}
		//
		// (7)自分にウィナーの多いスート 追加(2002/09/21)
		//
		int maxWinner = -1;
		int maxWinnerSuit = -1;
		Packet winner = getWinners();
		
		for (int i = 1; i < 5; i++) {
			int winnerCount = winner.countSuit(i);
			if (winnerCount > maxWinner) {
				maxWinner		= winnerCount;
				maxWinnerSuit	= i;
			}
		}
		if (maxWinner > 0) return maxWinnerSuit;
		
		//
		// ラフリスのスートしかもっていない場合またはトランプしかない
		//
		Packet p = getMyHand();
		p.shuffle();
		int suit = p.peek(0).getSuit(); // 持っている任意のカードのスート
		p.arrange();
		
		return suit;
	}
	
	//
	// (1) ラフリスのスートを除外
	//　　　　ラフリスのスートとは：
	//　　　　ダミーにもディクレアラーにもトランプが残っている状況で
	//　　　　　　　　（現在０枚と判明していないこと）　　　　
	//　　　　ダミーもディクレアラーもが現在０枚と判明しているスート　　　　　　　
	//
	private boolean isRuflis(int[][][] dist, int suit) {
		int declarer	= board.getDeclarer();
		int dummy		= board.getDummy();
		
		int trump = board.getContract().getSuit();
		// ディクレアラーにトランプが確実に残っていない場合、ラフリスではない
		if (dist[ declarer ][ trump-1 ][ ThinkingUtils.MAX ] == 0) return false;
		
		// ダミーに確実にトランプが確実に残っていない場合、ラフリスではない
		if (dist[ dummy    ][ trump-1 ][ ThinkingUtils.MAX ] == 0) return false;
		
		// ディクレアラーが持っている可能性がある場合、ラフリスではない
		if (dist[ declarer ][ suit-1 ][ ThinkingUtils.MAX ] > 0) return false;
		
		// ダミーが持っている可能性がある場合、ラフリスではない
		if (dist[ dummy    ][ suit-1 ][ ThinkingUtils.MAX ] > 0) return false;
		
		return true;
	}
	
	/**
	 * スーツコントラクトの場合で、リードするスートが決まった場合のプレイを行います。
	 *　スート内でのカードの決め方を次の順位で決める　　　　　　　　
	 *　　（１）そのスートの中で、トップがウィナーならキャッシュ
	 *　　（２）ＫＱからＫ
	 *　　（３）ＱＪからＱ
	 *　　（４）ＫＪＴ，ＪＴからＪ
	 *　　（５）ＫＴ９，ＱＴ９、Ｔ９からＴ
	 *　　（６）その他：トップオブナッシング
	 *                  現在２枚：上
	 *　　　　　　　　　現在３枚：３枚目
	 *　　　　　　　　　現在４枚以上→４枚目　
	 */
	public Card choosePlayInSuitLead(int suit) {
		Packet candidacy = hand.subpacket(suit);
		if (candidacy.size() == 0)
			throw new InternalError("choosePlayInSuitLead で指定されたスート("+suit+")を持っていません");
		candidacy.arrange();
		
		// （１）そのスートの中で、トップがウィナーならキャッシュ
		Packet winner = getWinners(); //getWinnersInSuitLead();
//System.out.println("choosePlayInSuitLead(suit). winner = " + winner);
		Card top = candidacy.peek(0);
		if (winner.contains(top.getSuit(), top.getValue())) return top;
		
		// （２）ＫＱからＫ
		if (BridgeUtils.patternMatch(hand, "KQ*", suit)) {
			return hand.peek(suit, Card.KING);
		}
		
		// （３）ＱＪからＱ
		if (BridgeUtils.patternMatch(hand, "QJ*", suit)) {
			return hand.peek(suit, Card.QUEEN);
		}
		
		// （４）ＫＪＴ，ＪＴからＪ
		if (BridgeUtils.patternMatch(hand, "KJT*", suit)) {
			return hand.peek(suit, Card.JACK);
		}
		if (BridgeUtils.patternMatch(hand, "JT*", suit)) {
			return hand.peek(suit, Card.JACK);
		}
		
		// （５）ＫＴ９，ＱＴ９、Ｔ９からＴ
		if (BridgeUtils.patternMatch(hand, "KT9*", suit)) {
			return hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(hand, "QT9*", suit)) {
			return hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(hand, "T9*", suit)) {
			return hand.peek(suit, 10);
		}
		
		// （６）その他：現在２枚：上
		// 　　　　　　　現在３枚：３枚目
		// 　　　　　　　現在４枚以上→４枚目
		// そのスートの第一回目のリードのときは
		// アナーのないときはトップオブナッシング
		// トップがアナーのときはこのままでよい。
		
		if (suitIsFirstTime(suit)) {
			if (bridgeValue(candidacy.peek(0)) < 10)
				return candidacy.peek(0);
		}
		
		switch (candidacy.size()) {
		case 1:
		case 2:
			return candidacy.peek(0);
		case 3:
			return candidacy.peek(2);
		default:
			return candidacy.peek(3);
		}
	}
	
	private boolean suitIsFirstTime(int suit) {
		for (int i = 0; i < board.getTricks(); i++) {
			Trick t = board.getAllTricks()[i];
			if (t.size() == 0) continue;
			if (t.peek(0).getSuit() == suit) return false;
		}
		return true;
	}
	
	//*********************************************************************//
	//  ２番手のプレイ(スートフォローできる場合)                           //
	//*********************************************************************//
	
	
	/**
	 * ２番手では、
	 * ・ウィナーがあれば出す（複数あれば下から）
	 * ・なければローエスト
	 */
	private Card playIn2nd() {
		int suit = lead.getSuit();
		if (getDummyPosition() == LEFT) {
			// LHO
			Packet follow = hand.subpacket(suit);
			follow.arrange();
			if (follow.size() == 0)
				throw new InternalError("playIn2nd() で、LHO はスートフォローできなくなっています");
			int trump = board.getContract().getSuit();
			
			//
			// 和美アルゴリズム
			//
			Packet dummyFollow = dummyHand.subpacket(suit);
			if ( dummyFollow.size() == 0 ) {
				// ダミーがフォローできない
				if ( (trump != Bid.NO_TRUMP)&& // スーツコントラクト
						(dummyHand.subpacket(trump).size() > 0)&& // ダミーにトランプがある
						(bridgeValue(lead) <= 10))
					return follow.peek(); // ローエスト
				else
					return getCheepestWinner(follow, lead);
			}
			dummyFollow.arrange();
			if ( bridgeValue(dummyFollow.peek()) > bridgeValue(follow.peek(0)) )
				// ダミーのローエスト ＞ 自分のハイエスト --> ローエスト
				return follow.peek();
			else if (bridgeValue(lead) > bridgeValue(dummyFollow.peek(0)))
				// リード ＞ ダミーのハイエスト --> リードにチーペストに勝つ
				return getCheepestWinner(follow, lead);
			else if (dummyFollow.size() == 1)
				// ダミーのカードが１枚
				return getCheepestWinner(follow, dummyFollow.peek());
			else if (bridgeValue(follow.peek(0)) > bridgeValue(dummyFollow.peek(0)))
				// 自分のハイエスト＞ダミーのハイエスト
				return getCheepestWinner(follow, dummyFollow.peek(0));
			else if ((bridgeValue(lead) > bridgeValue(dummyFollow.peek()))&&
						(bridgeValue(lead) >= 10))
				// リード＞＞ダミーのローエスト）＆（リードが１０以上）
				return getCheepestWinner(follow, lead);
			else
				return follow.peek(); // ローエスト
		} else {
			// RHO
			Packet winner = getWinners();
			
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
	}
	
	//*********************************************************************//
	//  ３番手のプレイ(スートフォローできる場合)                           //
	//*********************************************************************//
	
	/**
	 * ３番手では、
	 * ・ＲＨＯの場合、ハイエストを出す（ただしダミーと自分を合わせたカードで
	 *   シークエンスとなる時はその内で最下位を出す）
	 */
	private Card playIn3rd() {
		if (getDummyPosition() == LEFT) {

			//
			// LHO
			//
			
			Packet follow = hand.subpacket(lead.getSuit());
			// follow できない場合はすでにラフ or ディスカードしているので、
			// 下の if では 0 になることはない。
			if (follow.size() <= 1) return follow.peek();
			follow.arrange();
			
			int trump = board.getContract().getSuit(); // NT(==5)のこともある
			
			Card declarerPlay = board.getTrick().peek(1);
//System.out.println("declarerPlay = " + declarerPlay);
			if ( (lead.getSuit() != trump)&&(declarerPlay.getSuit() == trump) )
				// ディクレアラーがラフした
				return getSignal();
			
			Packet dummyFollow = dummyHand.subpacket(lead.getSuit());
			dummyFollow.arrange();
			
			if ( compare(declarerPlay, lead) > 0 ) {
				// ディクレアラーがプレイして、それが勝っている
				if (dummyFollow.size() == 0) {
					// ダミーはフォローできない
					if ( compare(follow.peek(0), declarerPlay) > 0 )
						// 自分のハイエスト ＞ ディクレアラーのプレイ
						return getCheepestWinner(follow, declarerPlay);
					else
						return getSignal();
				} else {
					// ダミーはフォローできる
					if ( (compare(dummyFollow.peek(), follow.peek(0)) > 0)
							||(compare(declarerPlay, follow.peek(0)) > 0) ) {
						// ダミーのローエスト＞自分のハイエスト
						//  or ディクレアラープレイ＞自分のハイエスト
						return getSignal();
					} else if ( compare(follow.peek(0), dummyFollow.peek(0)) > 0){
						return getCheepestWinner(follow,
									getStronger(declarerPlay, dummyFollow.peek(0)) );
					} else {
						//ダミーのハイエスト＞自分のハイエスト＞ダミーのローエスト
						//＆自分のハイエスト＞ディクレアラー
						//ダミーが１枚のときはありえない
						//  → getcheepestwinner（自分の手,（ダミーのgetcheepestwinner（ダミー
						// の手、自分のハイエスト）の次に低いカード）とディクレアラーの大きい方）
//System.out.println("●●●●●●●●一般の場合になった●●●●●●●●");
//System.out.println("follow = " + follow);
//System.out.println("dummyFollow = " + dummyFollow);
//System.out.println("declarerPlay = " + declarerPlay);
//System.out.println("getCheepestWinner(dummyFollow, follow.peek(0)) = " + getCheepestWinner(dummyFollow, follow.peek(0)));
//System.out.println("getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))) = " + getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))));
//System.out.println("getStronger(getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))),declarerPlay) = "+getStronger(getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))),declarerPlay));
//System.out.println(getCheepestWinner(follow, getStronger(getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))), declarerPlay)));
						return 
						getCheepestWinner(follow, 
						getStronger(
							getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))),
							 declarerPlay
						)
						);
					}
				}
			} else {
				// リード＞ディクレアラー
				if (dummyFollow.size() == 0) {
					return getSignal();
				} else if (dummyFollow.size() == 1) {
					if ( compare(lead, dummyFollow.peek()) > 0) return getSignal();
					else return getCheepestWinner(follow, dummyFollow.peek());
				} else {
					//if   リード＞ダミーのハイエスト OR
					// ダミーのハイエスト＞リード＞自分のハイエスト
					//         →getsignal
					if (compare(lead, dummyFollow.peek(0)) > 0) return getSignal();
					if ((compare(dummyFollow.peek(0), lead) > 0)
							&&(compare(lead, follow.peek(0)) >0 )) return getSignal();
					
					//ダミーのハイエスト＞リード＆ 自分のハイエスト＞リード
					if (compare(follow.peek(0), dummyFollow.peek(0)) > 0 )
						return getCheepestWinner(follow, dummyFollow.peek(0));
					//ダミーのハイエスト＞自分のハイエスト
					if (compare(	getCheepestWinner(dummyFollow, lead),
									getCheepestWinner(dummyFollow, follow.peek(0) )) >= 0)
						return getSignal();
					
					return getCheepestWinner(follow, getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))));
				}
			}
		} else {
			//
			// RHO(RHO３番手フォローの戦略)
			//
			Packet pack = hand.subpacket(lead.getSuit());
			pack.arrange();
			
			// １枚しかない場合はそのカードを出す
			if (pack.size() == 1) return pack.peek();
			
			// 勝っている人、カードを選んでおく
			Card wc = board.getTrick().getWinnerCard();
			int wcs = board.getTrick().getWinner();
			
			//
			Card max = lead;
			Card dummyPlay = board.getTrick().peek(1);
			if (compare(lead, dummyPlay) < 0) max = dummyPlay;
			
			Card highest = pack.peek(0);
			if (compare(highest, max) < 0) {
				// 自分のハイエスト＜Max(リード、ダミーのプレイしたカード)
				return getSignal();
			} else {
				// 自分のハイエスト≧Max(リード、ダミーのプレイしたカード)
				Packet o = new PacketImpl(board.getOpenCards());
				o.add(hand);	// o = ダミーのカード、プレイされたカード、自分のハンド
				Card cardA = getBottomOfSequence(o, highest);
				
				if (compare(dummyPlay, lead) < 0) {
					// ダミーのプレイしたカード ＜ リード (＜ 自分のハイエスト)
					if (compare(cardA, lead) <= 0) return getSignal();
					return getCheepestWinner(hand, cardA);	// 3rd hand high
				}
				if (compare(cardA, dummyPlay) <= 0)
					return getCheepestWinner(hand, dummyPlay);
				return getCheepestWinner(hand, cardA);
			}
		}
	}
	
	//*********************************************************************//
	//  ４番手のプレイ(スートフォローできる場合)                           //
	//*********************************************************************//
	/**
	 * ４番手では、一番安く勝つかローエスト
	 */
	private Card playIn4th() {
		Packet pack = hand.subpacket(lead.getSuit());
		pack.arrange();
		Card play = null;
		
		for (int i = 0; i < pack.size(); i++) {
			Trick virtual = new TrickImpl(getTrick());
			virtual.add(pack.peek(i));
			if (isItOurSide(virtual.getWinner())) play = pack.peek(i);
		}
		if (play == null) play = pack.peek();
		return play;
	}
	
	/**
	 * 指定したシート番号が自分たちサイドの場合、true
	 */
	private boolean isItOurSide(int seat) {
		return (((seat ^ getMySeat()) & 1) == 0);
	}
	
/*==========================================================
 *                  便  利  関  数  群
 *==========================================================
 */
	/**
	 * 自分のハンドの中でウィナーとなっているカードを抽出した Packet を返却します。
	 * ウィナーであることは、各スートにおいて今プレイされていないカードのうちもっとも
	 * 高いカードであることで判断します。
	 * 不確定な情報は使用しません。
	 * 
	 * @return		winner
	 */
	private Packet getWinners() {
		boolean afterDummy = (getDummyPosition() == RIGHT);
		
		Packet result = new PacketImpl();
		
		//
		// 現在残っているカード(winnerの候補)を抽出する
		// winner の候補は、現在プレイされていないカードと今場に出ているカードである
		// ただし、このオブジェクトがＲＨＯでしかもダミーがプレイし、このオブジェクトが
		// プレイしていないとき、winner の候補からダミーのハンドを除く。
		//
		Packet rest = board.getOpenCards().complement();
		rest.add(getTrick());
		
		if ( (afterDummy)&&(board.getTrick().size() > 0) ) {
			// RHO で、リードでない場合(ダミーのハンドを除く)
		} else {
			// LHO であるか、RHO だが自分からリードする場合
			rest.add(getDummyHand());
		}
		
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
	
	/**
	 * ラフできるとき、ダミーのあとの３番手のときには、パートナーがウィナーをだしていたら
	 * ラフしない、を実現するためのウィナー抽出関数。
	 */
	private Packet getWinners2() {
		boolean afterDummy = (getDummyPosition() == RIGHT);
		Packet result = new PacketImpl();
		
		//
		// 残りのカード(Winner の候補)を抽出する
		//
		Packet rest = board.getOpenCards().complement();
		rest.add(getTrick());
		
		if (!afterDummy) rest.add(getDummyHand());
		
		// 残りのカードを各スートに分ける
		Packet[] suits = new Packet[4];
		
		for (int i = 0; i < 4; i++) {
			suits[i] = rest.subpacket(i+1);
			suits[i].arrange();	// 強いカードを小さいインデックスに
		}
		
		Packet hand2 = new PacketImpl(hand);
		hand2.add(lead); // パートナーのリードを追加しておく
		
		// 各スーツのウィナーを抽出する
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < suits[i].size(); j++) {
				Card winner = suits[i].peek(j);
				if (hand2.contains(winner)) {
					result.add(winner); // 最高位を持っている場合、次の位も調べる
				} else {
					break;	// シークエンスが切れた場合、終了
				}
			}
		}
		
		result.arrange();
		return result;
	}
	
	/**
	 * ＮＴコントラクトの場合の自分たちの持っている絶対的なウィナーを考えます。
	 * 自分の手はすべて評価対象となりますが、パートナーの手はオープニングリードの
	 * 決まりから推定されるもののみが対象となります。
	 */
	private Packet getWinnersInNTLead() {
		Packet result = new PacketImpl();
		
		// opened = すでに見えているカード
		//        = (ダミーハンド) ∪ (これまでプレイされたトリック)
		// 次の rest のために取得しています。
		Packet opened = board.getOpenCards();
		
		// rest = まだプレイされていないカード(ダミーハンドを含む)
		Packet rest = opened.complement();
		rest.add(getDummyHand());
		
		// ours = {自分たちのウィナーになる可能性があるカード}
		//      := (自分のハンド) ∪ (O.L.から期待されるパートナーの現在のハンド)
		Packet ours = new PacketImpl();
		ours.add(hand); // 自分のハンド
		ours.add(getExpectedCardsInNT()); // パートナーが持っていると期待されるカード
		
		// rest のカード全体で、各スートについて上から順に ours に入っているものが
		// NT におけるウィナーとなります。
		for (int suit = 1; suit < 5; suit++) {
			Packet restOfSuit = rest.subpacket(suit);
			restOfSuit.arrange(); // 上から順番に
			for (int i = 0; i < restOfSuit.size(); i++) {
				Card c = restOfSuit.peek(i);
				if (ours.contains(c))
					result.add(c);
				else break; // ours にあるシークェンスが途切れた
			}
		}
		
		//
		// 今後の課題として、
		// ＮＴコントラクトではローカードのウィナーが重要で、これをカウントしたい。
		//
		
		//
		// 上記で言っていることは、ディクレアラーとダミーでショウアウトしたスートを
		// ウィナーとしてカウントしたい、パートナーの４ｔｈベストリードなどのシグナル
		// によって分かる枚数情報を使ってロングスートのローカードのウィナーをカウント
		// したい、という内容か？
		//
		
		return result;
	}
	
	private Packet getWinnersInSuitLead() {
		Packet result = new PacketImpl();
		
		// opened = すでに見えているカード
		//        = (ダミーハンド) ∪ (これまでプレイされたトリック)
		// 次の rest のために取得しています。
		Packet opened = board.getOpenCards();
//System.out.println("getWinnersInSuitLead . opened = " + opened);
		
		// rest = まだプレイされていないカード(ダミーハンドを含む)
		Packet rest = opened.complement();
		rest.add(getDummyHand());
		
		// ours = {自分たちのウィナーになる可能性があるカード}
		//      := (自分のハンド) ∪ (O.L.から期待されるパートナーの現在のハンド)
		Packet ours = new PacketImpl();
		ours.add(hand); // 自分のハンド
		ours.add(getExpectedCardsInTrump()); // パートナーが持っていると期待されるカード
		
		// sideSuits
		int[][][] sideSuits = ThinkingUtils.countDistribution(board, getMySeat());
		
		// rest のカード全体で、各スートについて上から順に ours に入っているものが
		// Suit Contract におけるウィナーとなります。
		// ただし、Suit Contract では、ダミーとディクレアラーについてトランプが残って
		// いる可能性がある状態では、サイドスートの(最大)数までしかウィナーを認めません。
		
		// トランプが残っていない場合、制限をはずす
		int trump = board.getTrump();
		// ダミー
		if (sideSuits[board.getDummy()][trump-1][ThinkingUtils.MAX] == 0) {
//System.out.println("getWinnersInSuitLead . dummy Trump is empty.");
			for (int suit = 1; suit < 5; suit++)
				if (suit != trump)
					sideSuits[board.getDummy()][suit-1][ThinkingUtils.MAX] = 13;
		}
		// ディクレアラー
		if (sideSuits[board.getDeclarer()][trump-1][ThinkingUtils.MAX] == 0) {
			for (int suit = 1; suit < 5; suit++)
				if (suit != trump)
					sideSuits[board.getDeclarer()][suit-1][ThinkingUtils.MAX] = 13;
		}
//System.out.println("getWinnersInSuitLead . rest = " + rest);
//System.out.println("getWinnersInSuitLead . ours = " + ours);

		for (int suit = 1; suit < 5; suit++) {
			Packet restOfSuit = rest.subpacket(suit);
			restOfSuit.arrange(); // 上から順番に
			
			// ウィナーとカウントできる最大数を計算します
			int maxWinnersOfSuit = restOfSuit.size();
			if (suit != trump) { // suit はサイドスート
				int tmp = sideSuits[board.getDummy()][suit-1][ThinkingUtils.MAX];
				if (maxWinnersOfSuit > tmp) maxWinnersOfSuit = tmp;
				tmp = sideSuits[board.getDeclarer()][suit-1][ThinkingUtils.MAX];
				if (maxWinnersOfSuit > tmp) maxWinnersOfSuit = tmp;
			}
//System.out.println("getWinnersInSuitLead.suit="+suit+"  maxWinnersOfSuit="+maxWinnersOfSuit);
			for (int i = 0; i < maxWinnersOfSuit; i++) {
				Card c = restOfSuit.peek(i);
				if (ours.contains(c))
					result.add(c);
				else break; // ours にあるシークェンスが途切れた
			}
		}
//System.out.println("getWinnersInsuitLead.result="+result);
		return result;
	}
	
	private static final String[][] NT_EXPECTED_PATTERN = {
		{ "T9*" }, // T lead
		{ "JT*" },	// J lead
		{ "QJ9*", "QJT*" },	// Q lead
		{ "KQT*", "KQJ*", "AKJT*", "AKQ*" }, // K lead
		{ "AKJTx*", "AKQxx*" } }; // A lead
	/**
	 * ＮＴコントラクトの場合のオープニングリードから推定されるパートナーの手を取り出します。
	 * パートナーがオープニングリーダーであった場合に、そのスートは一定のルール
	 * によってある優先順位に基づいてハンドパターンが推定されます。
	 * リードされたカードのバリューと推定されるハンドパターンは次の通りです。
	 * 先頭のものがより優先順位が高く設定されています。
	 *
	 *	{ "T9*" }, // T lead
	 *	{ "JT*" },	// J lead
	 *	{ "QJ9*", "QJT*" },	// Q lead
	 *	{ "KQT*", "KQJ*", "AKJT*", "AKQ*" }, // K lead
	 *	{ "AKJTx*", "AKQxx*" } }; // A lead
	 *
	 * @param		パートナーが持っていると推定されるカード
	 */
	private Packet getExpectedCardsInNT() {
		return getExpectedCardsImpl(NT_EXPECTED_PATTERN);
	}
	
	/**
	 * パートナーの行ったオープニングリードから推定される
	 * 現在のパートナーハンドを返却します。現在の、とはオープニングリードから
	 * 推定されるハンドですでにプレイされたものは除外する、という意味です。
	 * オリジナルハンドで考えて、引数で示されるパターン文字列にあてはまるもの
	 * が抽出されます。
	 */
	private Packet getExpectedCardsImpl(String[][] pattern) {
		Packet result = new PacketImpl();
		
		Trick opening = board.getPlayHistory().getTrick(0); // null はありえない
		
		// 自分がオープニングリーダーの場合、情報はないため、
		// 空の Packet を返却する。
		if (opening.getLeader() == getMySeat()) return result;
		
		// パートナーがオープニングリーダーであり、自分の番になっているため、
		// すでにオープニングリードは行われているはず
		Card openingLead = opening.getLead();
		
		int value = openingLead.getValue();
		if ((value <= 9)&&(value >= 2)) return result; // ローカードのリードは何も期待できない
		
		int suit = openingLead.getSuit();
		
		int index = value - 10; // T=0, J=1, Q=2, K=3, A=4
		if (index < 0) index = 4; // ACE は value == 1 となっているため
		String [] handPattern = pattern[index];
		
		// 優先順位の高いものから順に推定
		int handPatternIndex = 0;
		
		//
		// パートナーとディクレアラーの手の Union を求める。
		// このアルゴリズムではこれをパートナーの持ちうる手とみなす。
		//
		// open = {場に出たカード(含ダミー)} ∪ (現在の自分のハンド)
		//  i.e. 自分が認識できているすべてのカード
		Packet open = new PacketImpl(board.getOpenCards());
		open.add(getMyHand());
		
		// これにこれまでプレイしたパートナーの手を合わせたものが Union
		//
		// rest = ￢open
		// i.e. 自分から見て未知のすべてのカード ( = パートナー ∪ ディクレアラー )
		Packet rest = open.complement();
		
		// これまでのトリックの中で、パートナーが出したものすべてを rest に加える
		// i.e. rest ＝ パートナーの初期ハンド ∪ 現在のディクレアラーハンド
		//           ⊃ パートナーの初期ハンド
		Trick[] trick = board.getAllTricks();
		for (int i = 0; i < board.getTricks(); i++) {
			for (int j = 0; j < trick[i].size(); j++) {
				int seat = (trick[i].getLeader() + j)%4;
				if (( (seat - getMySeat() + 6)%4 ) == 0) rest.add(trick[i].peek(j));
			}
		}
//System.out.println("expected card (NT/Suit) rest : " + rest);
		
		// パートナーの初期ハンドとしてありうるものを handPattern から探す
		for (handPatternIndex = 0; handPatternIndex < handPattern.length; handPatternIndex++) {
			if (BridgeUtils.patternMatch(rest, handPattern[handPatternIndex], suit)) break;
		}
		
		if (handPatternIndex == handPattern.length) return result; // 該当なし。空パケット返却
		
		// 該当ありのため、パターン文字列を result に加える(High Card のみ)
		String toAdd = handPattern[handPatternIndex];
		for (int i = 0; i < toAdd.length(); i++) {
			char c = toAdd.charAt(i);
			
			// open に含まれているものは add しない (すでにパートナーがプレイしたもの)
			switch (c) {
			case 'A':
				if (!open.contains(suit, Card.ACE))
					result.add(rest.peek(suit, Card.ACE));
				break;
			case 'K':
				if (!open.contains(suit, Card.KING))
					result.add(rest.peek(suit, Card.KING));
				break;
			case 'Q':
				if (!open.contains(suit, Card.QUEEN))
					result.add(rest.peek(suit, Card.QUEEN));
				break;
			case 'J':
				if (!open.contains(suit, Card.JACK))
					result.add(rest.peek(suit, Card.JACK));
				break;
			case 'T':
				if (!open.contains(suit, 10))
					result.add(rest.peek(suit, 10));
				break;
			default:
			}
		}
		return result;
	}
	
	private static final String[][] SUIT_EXPECTED_PATTERN = {
		{ "T9*", "KT9*", "QT9*" }, // T lead
		{ "JT*", "KJT*" },	// J lead
		{ "QJ*" },	// Q lead
		{ "KQ*" }, // K lead
		{ "A*" } }; // A lead
	
	/**
	 * スーツコントラクトの場合のオープニングリードから推定されるパートナーの手を取り出します。
	 * パートナーがオープニングリーダーであった場合に、そのスートは一定のルール
	 * によってある優先順位に基づいてハンドパターンが推定されます。
	 * リードされたカードのバリューと推定されるハンドパターンは次の通りです。
	 * 先頭のものがより優先順位が高く設定されています。
	 *
		{ "T9*", "KT9*", "QT9" }, // T lead
		{ "JT*", "KJT*" },	// J lead
		{ "QJ*" },	// Q lead
		{ "KQ*" }, // K lead
		{ "A*" } }; // A lead
	 *
	 * @param		パートナーが持っていると推定されるカード
	 */
	private Packet getExpectedCardsInTrump() {
		return getExpectedCardsImpl(SUIT_EXPECTED_PATTERN);
	}
	
	/**
	 * 指定された候補カードの集まりの中から、指定されたカードに勝てる値がチーペストな
	 * カードを取得する。どうしても勝てない場合、ローエストを返す。
	 * スートフォローについては考慮しておらず、値による評価しかしていない。
	 */
	private Card getCheepestWinner(Packet candidacy, Card target) {
		Packet p = candidacy.subpacket(target.getSuit());
		if (target == null) return p.peek();
		p.arrange();
		if (p.contains(target)) return target;
		
		Card stronger = null;
		for (int i = 0; i < p.size(); i++) {
			Card c = p.peek(i);
			if (bridgeValue(c) > bridgeValue(target)) stronger = c;
		}
		if (stronger == null) return p.peek(); // ローエスト
		return stronger;
	}
	
	/**
	 * 指定された Packet の中の、指定されたカードと同等の最低のカードを出す
	 */
	private Card getBottomOfSequence(Packet candidacy, Card base) {
		Packet p = candidacy.subpacket(base.getSuit());
		p.arrange();
		Card c = base;
		for (int i = 1; i < p.size(); i++) {
			Card c2 = p.peek(i);
			if (bridgeValue(c) - bridgeValue(c2) == 1)	c = c2;
		}
		return c;
	}
	
	private Card getBottomOfSequence(Packet candidacy) {
		if (candidacy.size() == 0) return null;
		Packet p = new PacketImpl(candidacy);
		p.arrange();
		Card c = p.peek(0);
		for (int i = 1; i < p.size(); i++) {
			Card c2 = p.peek(i);
			if (bridgeValue(c) - bridgeValue(c2) == 1)	c = c2;
		}
		return c;	
	}
	
	private Card getBottomOfSequence(Packet candidacy, int suit) {
		return getBottomOfSequence(candidacy.subpacket(suit));
	}
	
	private Card getNextLowerCard(Card c) {
		Packet p = hand.subpacket(c.getSuit());
		p.arrange();
		int index = p.indexOf(c);
		if ((!(index == -1))||(index == p.size()-1))
			throw new IllegalStateException("ハンドに " + c + "が含まれていません");
		return p.peek(index+1);
	}
	
	private Card getNextLowerCard(Packet h, Card c) {
		Packet p = h.subpacket(c.getSuit());
		p.arrange();
//System.out.println("getNext..p = " + p);
//System.out.println("c = " + c);
		int index = p.indexOf(c);
		if (index == -1)
			throw new IllegalStateException("対象 Packet に " + c + "が含まれていません");
		if (index == p.size()-1) return null;
		return p.peek(index+1);
	}
	
	/**
	 * どの位置でも呼ばれる
	 */
	private Card getSignal() {
		// 自分のハンドから、リードと同じスートのカードを抽出する
		Packet follow = hand.subpacket(lead.getSuit());
		
		if (follow.size() == 0) return null;
		if (follow.size() == 1) return follow.peek();
		
		follow.arrange();
		Card card1 = board.getTrick().peek(0); // == lead
		Card card2 = board.getTrick().peek(1);
		int trump = board.getContract().getSuit();
		
//オーバーテイクの場合を選出		
		if ((card1.getSuit() == trump)||(card2.getSuit() != trump)) {
			// ディクレアラーはラフしていない
			if ((compare(follow.peek(0), card1) > 0)&&(compare(card1, card2) > 0)) {
				// フォローのハイエスト＞card1＞card2
				Packet p = board.getOpenCards().complement();
				p.add(dummyHand);
				p = p.complement().subpacket(lead.getSuit()); // p = 今までプレイされたカード
				// 下の２行はすでにプレイされているので、pに含まれているはず
				//p.add(card1);
				//if (card2.getSuit() == card1.getSuit()) p.add(card2);
				
				Card high = follow.peek(0);
				Card low;
				if (follow.size() == 2) low = follow.peek(1);
				else low = follow.peek(2);
				
				int i;
				for (i = bridgeValue(low); i <= bridgeValue(high); i++) {
					int j = i;
					if (j == 14) j = 1;
					if (!p.containsValue(j)) break;
				}
				if (i > bridgeValue(high)) {
					Card c = follow.peek(0);
					for (int j = 1; j < follow.size(); j++) {
						Card c2 = follow.peek(j);
						if (bridgeValue(c) - bridgeValue(c2) == 1)	c = c2;
					}
					//オーバーテイクしても損のない状況ではオーバーテイクする
					return c;
				}
			}
		}
		return follow.peek(); // ローエスト 
/*
//・カモンシグナルの場合
if  いままでそのスートがリードされたことはない//初めてそのスートがリードされ
た
    ＆（（packetにＡまたはＫを含む）  ＯＲ  （card1=K & packetにＱを含む））
        if packetに９以下のカードがある
             →９以下のカードの中で一番高いもの
         else //10以上しか持っていない
             →ローエストを返す

//・アンブロック（Ａ７からはＡをだすなど）LHOはしない（将来ＲＨＯにはさせた
い）
//・カウントシグナル （奇数枚ならロー、偶数枚ならハイ）今はしない
//・スーツプリファランスシグナル（リードのときに出す事が多いので、なし）
else
→  ローエストを返す
*/
	}
	
	/**
	 * Aceを14に変換します。
	 */
	private int bridgeValue(int value) {
		if (value == 1) return 14;
		else return value;
	}
	
	/**
	 * Aceを14として、指定カードの値を読み取ります
	 */
	private int bridgeValue(Card target) {
		return bridgeValue(target.getValue());
	}
	
	/**
	 * lead に対するスートフォロー、トランプスートを考慮して２枚のカードの強さを比較します
	 * ただし、２枚とも同じスートのディスカードの場合、値が大きい方を強いとみなし、
	 * 違うスートのディスカードの場合は 0 を返却しています
	 *
	 * @param		a		比較対象のカード
	 * @param		b		比較対象のカード
	 * @return		結果( (1) a > b  (-1) a < b  (0) a = b )
	 */
	private int compare(Card a, Card b) {
		if ((a == null)&&(b == null)) return 0;
		if (b == null) return 1;
		if (a == null) return -1;
		
		// 同じスートの場合
		// (２枚とも同じスートのディスカードの場合、値の大きい方が強い事となっている）
		if (a.getSuit() == b.getSuit()) {
			int av = bridgeValue(a);
			int bv = bridgeValue(b);
			if (av > bv) return 1;
			if (av == bv) return 0;
			return -1;
		}
		int trump = board.getContract().getSuit();
		
		// ラフの場合
		if (a.getSuit() == trump) return 1;
		if (b.getSuit() == trump) return -1;
		
		// スートフォローを見る
		if (a.getSuit() == lead.getSuit()) return 1;
		if (b.getSuit() == lead.getSuit()) return -1;
		
		//
		return 0;
	}
	
	/**
	 * 指定された２カードのうち、強い方を返却します。
	 * 強さの判定には compare(a,b) を使用します。
	 *
	 * @param		a		候補Ａ
	 * @param		b		候補Ｂ
	 * @return		２候補のうち、強いカード
	 */
	private Card getStronger(Card a, Card b) {
		if (compare(a, b) > 0) return a;
		else return b;
	}
}

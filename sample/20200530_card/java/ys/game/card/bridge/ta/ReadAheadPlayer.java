package ys.game.card.bridge.ta;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.bridge.Player;
import ys.game.card.bridge.Board;
import ys.game.card.bridge.Bid;
import ys.game.card.bridge.BridgeUtils;
import ys.game.card.bridge.IllegalStatusException;
import ys.game.card.bridge.SimplePlayer2;

/**
 * ダブルダミー状態で、最後まで読みきって最善手を打つプレイヤー。
 *
 * 2015/8/12 コンピュータの性能アップに伴い、先読み深化
 *
 * @version		making		20 October, 2002
 * @author		Yusuke Sasaki
 */
public class ReadAheadPlayer extends Player {
	protected SimplePlayer2	base;
	protected boolean		openingLeadSpecified;
	
	private byte[][] paths = new byte[5000][13];
	
/*
 * 
 */
	public ReadAheadPlayer(Board board, int seat) {
		setBoard(board);
		setMySeat(seat);
		
		base = new SimplePlayer2(board, seat);
	}
	
	public ReadAheadPlayer(Board board, int seat, String ol) {
		setBoard(board);
		setMySeat(seat);
		
		base = new SimplePlayer2(board, seat, ol);
		if ((ol != null)&&(!ol.equals(""))) openingLeadSpecified = true;
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
	 * OptimizedBoard の最善手探索アルゴリズムを使用したプレイを行います。
	 * オープニングリードについては指定がある場合、指定に従います。
	 *
	 * @return		最善手
	 */
	public Card draw() throws InterruptedException {
		// プレイの間隔をなるべく一定にするため
		long t0 = System.currentTimeMillis();
		
		Board board = getBoard();
		
		if (board.getStatus() == Board.OPENING) {
			// オープニングリードの場合は SimplePlayer2 のアルゴリズムを使う
			if (openingLeadSpecified) return base.draw();
		}
		
		// 最善手プレイの集合を取得します。
		Packet playOptions = getPlayOptions();
		
		Card play = choosePlay(playOptions);
		
		// プレイ間隔一定のため
		long t = System.currentTimeMillis();
		try { if ((t - t0) < 700) Thread.sleep(700 - (t - t0)); // 700msec になるまで考えるふり
		} catch (InterruptedException ignored) { }
		
		return play;
	}
	
	/**
	 * OptimizedBoard を使用して、最善手の候補を見つけます。
	 * 本クラス内の Optimized 関連部分となります。
	 *
	 * @return		最善手の候補
	 */
	protected Packet getPlayOptions() {
		OptimizedBoard b = new OptimizedBoard(getBoard());
		//
		// トリック数による先読みの深さ変更
		//
		
		// depthBorder 値を指定
		// depthBorder は、最低先読みプレイ数で、実際にはこれ以降の最初のリード状態
		// まで先読みが行われます。例えば０を指定し、すでにリード状態にあった場合、
		// 先読みは行われません。
		//                        
		int[] depth = new int[] {   8,   8,   8,   8,
								    8,   8,   9,  10,
								  100, 100, 100, 100, 100, 100 };


// これだと時間がかかりすぎということで、もう少し減らす
// 2016/3/27
//
//		int[] depth = new int[] {   9,   9,   9,   9,
//								    9,   9,  10,  100,
//								  100, 100, 100, 100, 100, 100 };

// Pentium 3(700MHz)時代(2015までこの値を採用していた)
//		int[] depth = new int[] {   5,   5,   5,   5,
//								    5,   5,   6,   6,
//								  100, 100, 100, 100, 100, 100 };
		b.setDepthBorder(depth[getBoard().getTricks()]);
		
		int[] bps = b.getBestPlay();
		
		//
		// デバッグ用出力
		//
/*
for (int i = 0; i < bps.length; i++) {
	if (bps[i] == -1) break;
	System.out.print(" " + i + ":" + OptimizedBoard.getCardString(bps[i]));
}
System.out.println();
*/
		
		//------------------
		// 同格カードの抽出
		//------------------
		// プレイされていないカード = 0 // または今場に出ているカード
		// 持っているカード         = 1
		// 指定されたカード			= 2
		// プレイされたカード       = 3
		// 0 を delimiter として、token を区切り、2 が含まれている token の
		// 1 を 2 に変更する。2 となっているカードを返却する
		
		int[] tmp = new int[56];
		
		// プレイされたカード(3)の設定
		// まだ山に戻っていないカード(disposed)
		//   = { open cards } - { 今出ているカード }
		Board board = getBoard();
		Packet disposed = board.getOpenCards().sub(board.getTrick()).sub(getDummyHand());
		
		for (int i = 0; i < disposed.size(); i++) {
			Card c = disposed.peek(i);
			tmp[ OptimizedBoard.getCardNumber(c) ] = 3;
		}
		
		// 持っているカード(1)の設定
		Packet h = getHand();
		
		for (int i = 0; i < h.size(); i++) {
			Card c = h.peek(i);
			tmp[ OptimizedBoard.getCardNumber(c) ] = 1;
		}
		
		// 指定されたカード(2)の設定
		for (int i = 0; i < bps.length; i++) {
			if (bps[i] == -1) break;
//if (tmp[ bps[i] ] != 1) System.out.println("asserted in tmp != 1");
			tmp[ bps[i] ] = 2;
		}
		
		// token ごとの処理
		int tokenStartIndex = 0;
		int resultCount = 0;
		
		while (true) {
			// delimiter でないインデックスを探す --> tokenStartIndex
			for (; tokenStartIndex < 56; tokenStartIndex++) {
				if (tmp[tokenStartIndex] != 0) break;
			}
			if (tokenStartIndex == 56) break;
			
			int tokenEndIndex;
			boolean containsTargetCard = false;
			for (tokenEndIndex = tokenStartIndex; tokenEndIndex < 56; tokenEndIndex++) {
				if (tmp[tokenEndIndex] == 2)
					containsTargetCard = true;
				else if (tmp[tokenEndIndex] == 0) break;
			}
			
			if (containsTargetCard) {
				for (int i = tokenStartIndex; i < tokenEndIndex; i++) {
					if (tmp[i] != 3) {
						tmp[i] = 2;
						resultCount++;
					}
				}
			}
			tokenStartIndex = tokenEndIndex + 1;
			if (tokenStartIndex >= 56) break;
		}
		
		//
		// 結果生成
		//
		Packet result = new PacketImpl();
		
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i] != 2) continue;
			
			int value	= (i % 14) + 2;
			if (value == 14) value = Card.ACE;
			int suit	= (i / 14) + 1;
			
			result.add(getHand().peek(suit, value));
		}
System.out.println("同格カード含めた最善プレイ候補:" + result);
		return result;
	}
	
	/**
	 * はじめの方は SimplePlayer2 を優先させることができる
	 */
	static final boolean[] SPL_IS_SUPERIOR = new boolean[]
							 { true, true, true, false, false,
							 false, false, false, false, false,
							 false, false, false };
	
	/**
	 * 指定されたプレイ候補から、リード規則などに従うプレイを選びます。
	 * 各プレイ候補について、point 付けを行い、最大 point のプレイを返却します。
	 *
	 * @return		いいプレイ
	 */
	protected Card choosePlay(Packet option) throws InterruptedException {
		if (SPL_IS_SUPERIOR[getBoard().getTricks()]) {
			Card simplePlayer2Play = base.draw2(); // 考えた振りのwaitなし
System.out.println("SimplePlayer2 の意見を優先 : " + simplePlayer2Play);
			return simplePlayer2Play;
		}
		
		//
		// point付けをする
		//
		int[] point = new int[option.size()];
		
		//
		// オープニングリードの場合の規則
		//
		if (getBoard().getStatus() == Board.OPENING) {
			Packet p = leadSignal();
System.out.println("Lead Signal : " + p);
			for (int i = 0; i < option.size(); i++) {
				Card c = option.peek(i);
				if (p.contains(c)) point[i] += 100; //point[i] = 100;
			}
		}
		
		if (getBoard().getTurn() != getBoard().getDummy()) {
			// DummyではSimplePlayer2が機能しないため、スキップ
			
			//
			// SimplePlayer2 で選んだ手
			//
			Card simplePlayer2Play = base.draw2(); // 考えた振りのwaitなし
System.out.println("SimplePlayer2 の意見 : " + simplePlayer2Play);
			int index = option.indexOf(simplePlayer2Play);
			if (index >= 0) point[index] += 50;
			
			//
			// リードの場合、SimplePlayer2 のスートごとの選んだ手も評価
			//
			if (getPlayOrder() == LEAD) {
				for (int suit = 1; suit < 5; suit++) {
					if (getMyHand().countSuit(suit) == 0) continue;
					Card sp;
					if (getBoard().getContract().getSuit() == Bid.NO_TRUMP) {
						sp = base.choosePlayInNTLead(suit);
					} else {
						sp = base.choosePlayInSuitLead(suit);
					}
System.out.println("SimplePlayer2 のスートごとの意見：" + sp);
					int ind = option.indexOf(sp);
					if (ind >= 0) point[ind] += 10;
				}
			}
		}
		//
		// ディスカードの際、スクイズ耐性を増やす処理(2015/8/15追加)
		// 覗き見するため、SimplePlayer2 でなく ReadAheadPlayer に記述
		// 　相手のサイドスーツをエスタブリッシュさせないため、以下の
		// 　アンド条件で point[] を減らします
		// 　　　1) パートナーと枚数が同じか長い場合
		//   　　2) 相手の長いサイドスート
		//
		if  ( (getPlayOrder() != LEAD)&& // リードでなく
			  (!option.containsSuit(getLead().getSuit()))&& // リードスーツがなく
			  (!getMyHand().containsSuit(getBoard().getTrump())) ) { // トランプもない
			
System.out.println("ディスカード用処理開始");
			//
			// 相手の長いサイドスート、枚数、座席を検出する
			//
			int s1 = (getMySeat() + 1)%4;
			int s2 = (getMySeat() + 3)%4;
			Packet hand1 = getBoard().getHand()[s1];
			Packet hand2 = getBoard().getHand()[s2];
			
			int longSideSuitSeat	= -1;
			int longSideSuit		= -1;
			int longSideSuitCount	= -1; // 初期値
			
			for (int suit = 1; suit < 5; suit++) {
				if (suit == getBoard().getTrump()) continue;
				// トランプは除外, No Trump時は除外対象なしとなる
				if (hand1.countSuit(suit) > longSideSuitCount) {
					longSideSuitCount	= hand1.countSuit(suit);
					longSideSuit		= suit;
					longSideSuitSeat	= s1;
				}
				if (hand2.countSuit(suit) > longSideSuitCount) {
					longSideSuitCount	= hand2.countSuit(suit);
					longSideSuit		= suit;
					longSideSuitSeat	= s2;
				}
			}
			if (longSideSuitCount == -1)
				throw new InternalError("ReadAheadPlayer ディスカード処理で、想定外状態を検出しました");
			
System.out.println("相手の長いサイドスート : " + BridgeUtils.suitString(longSideSuit));
			//
			// 1) 2) のアンド条件となるスートを特定
			//
			int myCount  = getMyHand().countSuit(longSideSuit);
			int prdCount = getBoard().getHand(getPartnerSeat()).countSuit(longSideSuit);
			if ( (myCount >= prdCount)&&(myCount <= longSideSuitCount) ) {
				//	条件に合うので、point[] を減点
				for (int i = 0; i < option.size(); i++) {
					if (option.peek(i).getSuit() == longSideSuit)
						// SimplePlayer2 より優先
						point[i] -= 75;
				}
			}
		}
		
		//
		// 最大のものを選ぶ
		//
		int maxPoint = point[0];
		int maxIndex = 0;
		for (int i = 1; i < option.size(); i++) {
			if (point[i] > maxPoint) {
				maxPoint = point[i];
				maxIndex = i;
			}
		}
		
		return option.peek(maxIndex);
	}
	
	private Packet leadSignal() {
		if (getBoard().getContract().getSuit() == Bid.NO_TRUMP) {
			return leadSignalInNoTrump();
		} else {
			return leadSignalInTrump();
		}
	}
	
	private Packet leadSignalInNoTrump() {
		Packet result = new PacketImpl();
		
		for (int suit = Card.CLUB; suit <= Card.SPADE; suit++) {
			if (getHand().countSuit(suit) == 0) continue;
			result.add(ntOpening(suit));
		}
		return result;
	}
	
	private Card ntOpening(int suit) {
		Packet hand = getMyHand();
		
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
		if (suitPat.startsWith("AQJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("AQJ9"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJ9"))	value = Card.QUEEN;
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
	
	private Packet leadSignalInTrump() {
		Packet result = new PacketImpl();
		
		for (int suit = Card.CLUB; suit <= Card.SPADE; suit++) {
			if (getHand().countSuit(suit) == 0) continue;
			
			result.add(suitOpening(suit));
		}
		return result;
	}
	
	//
	// AK ダブルトンから K が出てくるけどＯＫ？
	//
	private Card suitOpening(int suit) {
		Packet hand = getMyHand();
//		if (suit == Board.getContract().getSuit()) return 0;
		
		String suitPat = BridgeUtils.valuePattern(hand, suit);
//System.out.println("suitOpening(suit) . suitPat = " + suitPat);
		if (suitPat.equals("AK")) return hand.peek(suit, Card.ACE);
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
	
}

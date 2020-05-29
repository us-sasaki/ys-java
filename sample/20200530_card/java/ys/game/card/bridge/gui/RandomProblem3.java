package ys.game.card.bridge.gui;

import ys.game.card.bridge.*;
import ys.game.card.*;

/**
 * ブリッジシミュレータのハンド、コントラクトを自動決定し、
 * 問題とするクラス。必ず South がディクレアラーになります。
 * RandomProblemでは、ダブルドコントラクトが起こりやすく、落ちやすいため、
 * 実際のコントラクトにより近いものに変更しています。
 *
 * @version		making		24, January 2004
 * @author		Yusuke Sasaki
 */
public class RandomProblem3 implements Problem {
	/** 問題のタイトル */
	protected String	title;
	
	// 以下の３変数はNS/EWの場合の想定コントラクトを決定する際の
	// ワークとしても利用されるが、makeProblem() の結果、最終コントラクトを
	// 示す値に設定される。
	protected int		kind;
	protected int		level;
	protected int		denomination;
	
	/** NSEW のハンド */
	protected String	description;
	
	protected Packet[]	hand;
	
	// 内部計算用
	/** 各ハンドのスートごとの枚数 */
	private int[][]		count; // [seat][suit-1]
	
	/** 各ハンドの High Card Point */
	private int[]		hcp;
	
	/** 各ハンドの point */
	private int[]		pts;
	
	/** NS/EW それぞれで見たときのビッド */
	private Bid[]		bid;	// NS(0) / EW(1)
	
	/** NS/EW それぞれで見たときのディクレアラー */
	private int[]		declarer;	// NS(0) / EW(1)
	
/*-------------
 * Constructor
 */
	public RandomProblem3() {
		count	= new int[4][4];
		hcp		= new int[4];
		pts		= new int[4];
		bid		= new Bid[2];
		declarer= new int[2];
	}
	
/*------------------
 * instance methods
 */
	public void start() {
		makeProblem();
	}
	
	/**
	 * スートごとの枚数、HCPを計算します
	 * hand 変数の値を使用します。
	 */
	private void calculateAttributes() {
		// スートごとの枚数をカウントする
		// 各スートについて、
		for (int suit = 1; suit < 5; suit++) {
			// それぞれの枚数を数える
			for (int i = 0; i < 4; i++) {
				count[i][suit-1] = hand[i].countSuit(suit);
			}
		}
		
		// HCPを計算する
		for (int i = 0; i < 4; i++) {
			hcp[i] = BridgeUtils.countHonerPoint(hand[i])[0];
		}
	}
	
	/**
	 * ビッド、ディクレアラーをNS, EWの指定された側が買い取ると仮定して決定します
	 * ビッドは可能な最高レベルのものを設定します
	 */
	private void assuming(int NSorEW) {
		// フィットの多いスートを探す
		
		// もっともフィットを持っている側を探し、その枚数も覚えておく
		// 同じフィットを持っている場合、レベルの高いスートを優先する
		int maxFit	= 0;
		int maxSuit = 0;
		int declarer = -1;
		for (int suit = 1; suit < 5; suit++) {
			int ns = count[NSorEW][suit-1] + count[NSorEW+2][suit-1];
			if ( (ns > maxFit)||( (ns == maxFit)&&(suit > maxSuit) ) ) {
				maxFit		= ns;
				maxSuit		= suit;
				declarer	= NSorEW; // とりあえず North or East としておく
			}
		}
System.out.println("NSorEW:"+NSorEW+"  maxFit:"+maxFit+"  maxSuit:"+maxSuit);
		
		//
		// コントラクトの denomination を決める
		//
		if (maxFit == 7) denomination = Bid.NO_TRUMP;
		else denomination = maxSuit;
		
		//
		// 4. declarer を決める
		//
		if (denomination == Bid.NO_TRUMP) {
			// NT コントラクトの時
			// Honer Point の高い方をディクレアラーとする
			if (hcp[declarer] < hcp[declarer+2]) declarer = declarer + 2;
		} else {
			// スートコントラクトの時
			int decCount = count[declarer  ][denomination-1];
			int dumCount = count[declarer+2][denomination-1];
			if ( decCount < dumCount ) {
				// パートナー(South / West) が declarer
				declarer = declarer + 2;
			} else if (decCount == dumCount) {
				// 枚数が同じ場合は Honer Point の高い方
				if (hcp[declarer] < hcp[declarer+2]) declarer = declarer + 2;
			}
		}
		
		//
		// レングスポイントなど計算する
		//
		
		// レングスポイントを加算する
		// レングスポイントは、NT/Suitコントラクトで (スートの枚数)-4 が正のとき
		// この数値を加算します(5枚スート..1pts  6枚スート..2pts ……)
		for (int i = NSorEW; i < 4; i+=2) {
			pts[i] = hcp[i];
			for (int suit = 1; suit < 5; suit++) {
				int cnt = count[i][suit-1];
				if (cnt > 4) pts[i] += (cnt - 4);
			}
		}
		
		int[] ptsBkup = new int[2];
		if (denomination != Bid.NO_TRUMP) {
			// ディストリビューションポイント
			// スートコントラクトの場合に、以下の点を加算する
			// ボイド..3pts  シングルトン..2pts  ダブルトン..1pts
			// また、特に、4-3-3-3 ブレイクのとき１点引く
			ptsBkup[0] = pts[0];
			ptsBkup[1] = pts[1]; // minorフィットをNTに戻すときにptsをこの値にする
			
			for (int i = NSorEW; i < 4; i+=2) {
				// トランプを持ってないときは加算しない
				if (count[i][denomination-1] == 0) continue;
				
				boolean f4333 = true;
				for (int suit = 1; suit < 5; suit++) {
					int cnt = count[i][suit-1];
					if ((cnt != 3)&&(cnt != 4)) f4333 = false;
					switch (cnt) {
					
					case 0: // void
						pts[i] += 0; //3;
						break;
					case 1: // singleton
						pts[i] += 0; //2;
						break;
					case 2: // doubleton
						pts[i] += 0; //1;
					default:	// fall through
					}
				}
				// 4-3-3-3 のときは１点ひく
				if (f4333) pts[i]--;
			}
			
			// ダミーポイント
			// スートコントラクトのダミーで、トランプサポートが４枚以上のとき、
			// 以下の補正を加える
			// ボイド..+2pts  シングルトン..+1pts
			int dummy = (declarer + 2) % 4;
			if (count[dummy][denomination-1] >= 4) {
				for (int suit = 1; suit < 5; suit++) {
					int cnt = count[dummy][suit-1];
					switch (cnt) {
					
					case 0:	// void
						pts[dummy] += 5;
						break;
					case 1: // singleton
						pts[dummy] += 3;
						break;
					default:	// fall through
					}
				}
			}
		}
		//
		// 5. コントラクトの level を決める
		//
		level = maxFit - 6; // とりあえず
		if (denomination == Bid.NO_TRUMP) level = 1;
		
		int totalPt = pts[NSorEW] + pts[NSorEW+2];
		
		if (totalPt > 36) {
			// グランドスラム
			if (has1stControl(NSorEW)) {
				level = 7;
			} else {
				level = 6;
			}
		}
		if ( (totalPt > 32)&&(totalPt < 37) ) {
			// スモールスラム
			level = 6;
		}
		if ( (totalPt > 29)&&(totalPt < 33) ) {
			if ( (denomination != Bid.NO_TRUMP)&&(level < 5) )
				level = 5;
			else if ( (denomination == Bid.NO_TRUMP) )
				 level = 5; // NT は無条件に５とする
		}
		if ( (totalPt > 24)&&(totalPt < 33) ) {
			// ゲーム
			if (denomination == Bid.NO_TRUMP) {
				if (level < 5) level = 3; // バランスハンドの場合なので、ストッパーは無視
			} else if ( (denomination == Bid.HEART)||(denomination == Bid.SPADE) ) {
				if (level < 4) level = 4;
			} else if ( (denomination == Bid.CLUB)||(denomination == Bid.DIAMOND) ) {
				// ストッパーがあるかどうか
				if (hasStopper(NSorEW)) {
					denomination = Bid.NO_TRUMP; // マイナーアンバランスハンドによるNTに変更
					level = 3;
					// ptsをNT用に直す
					pts[0] = ptsBkup[0];
					pts[1] = ptsBkup[1];
					
				} else {
					if (level < 4) level = 4; // 4 level minor
				}
			}
		} else if ( (totalPt > 21)&&(totalPt < 25) ) {
			if (denomination == Bid.NO_TRUMP) level = 2; // 2NT
		}
		
		// 結果の設定
		bid[NSorEW] = new Bid(Bid.BID, level, denomination);
		this.declarer[NSorEW] = declarer;
System.out.println("HCP[NSorEW] : " + hcp[NSorEW] + "  HCP[another] : " + hcp[NSorEW+2]);
System.out.println("pts[NSorEW] : " + pts[NSorEW] + "  pts[another] : " + pts[NSorEW+2]);
System.out.println("BID : " + bid[NSorEW] + "  declarer : " + declarer);
		
	}
	
	/**
	 * グランドスラムの場合に使用する、全スートにコントロールがあるかを
	 * チェックする関数です
	 */
	private boolean has1stControl(int NSorEW) {
		int pard = NSorEW + 2;
		for (int suit = 1; suit < 5; suit++) {
			// Aを持っているか、ボイドであればOK
			if ( (!BridgeUtils.patternMatch(hand[NSorEW], "A*", suit))
				&& (!BridgeUtils.patternMatch(hand[pard], "A*", suit))
				&& (count[NSorEW][suit-1] > 0)
				&& (count[pard]  [suit-1] > 0) )
				return false;
		}
		return true;
	}
	
	/**
	 * ノートランプコントラクトの場合に使用する、全スートにストッパーが
	 * あるかをチェックする関数です
	 */
	private boolean hasStopper(int NSorEW) {
		int pard = NSorEW + 2;
		for (int suit = 1; suit < 5; suit++) {
			if ( (!BridgeUtils.patternMatch(hand[NSorEW], "A?*", suit)) // A?*はストッパー
				&& (!BridgeUtils.patternMatch(hand[pard], "A?*", suit))
				&& (!BridgeUtils.patternMatch(hand[NSorEW], "K?*", suit)) // K?*はストッパー
				&& (!BridgeUtils.patternMatch(hand[pard],   "K?*", suit))
				&& (!BridgeUtils.patternMatch(hand[NSorEW], "Q??*", suit)) // Q??*はストッパー
				&& (!BridgeUtils.patternMatch(hand[pard],   "Q??*", suit))
				&& (!BridgeUtils.patternMatch(hand[NSorEW], "J???*", suit)) // J???*はストッパー
				&& (!BridgeUtils.patternMatch(hand[pard],   "J???*", suit))
				&& (!BridgeUtils.patternMatch(hand[NSorEW], "????*", suit)) // ????*はストッパー
				&& (!BridgeUtils.patternMatch(hand[pard],   "????*", suit)) )
					return false;
		}
		return true;
	}
	
	/**
	 * ランダムにハンドを配り、その内容からコントラクトなどを決定します。
	 */
	private void makeProblem() {
		//
		// 1. まず、ランダムにハンドを配る
		//
		Packet pile = PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER);
		pile.shuffle();
		hand = PacketFactory.deal(pile, 4);
		
		for (int i = 0; i < 4; i++) hand[i].arrange();
		
		//
		// 2. スートごとの枚数、HCPを基礎情報として計算する
		//
		calculateAttributes();
		
		//
		// 3. NS/EWそれぞれで買い取るとして最高ビッドを決める
		//
		assuming(Board.NORTH);
		assuming(Board.EAST);
		
		//
		// 4. 競り合い状況をチェックし、リーズナブルなコントラクトまで落とす
		//
		int declarer = -1;
		int NSorEW   = -1;
		int another  = -1;
		if (bid[Board.NORTH].isBiddableOver(bid[Board.EAST])) {
			declarer	= this.declarer[Board.NORTH];
			NSorEW		= Board.NORTH;
			another		= Board.EAST;
		} else {
			declarer	= this.declarer[Board.EAST];
			NSorEW		= Board.EAST;
			another		= Board.NORTH;
		}
		
		denomination	= bid[NSorEW].getSuit();
		level			= bid[NSorEW].getLevel();
		kind			= bid[NSorEW].getKind();
		
		switch (denomination) {
		
		case Bid.NO_TRUMP:
			// NTの場合は、4NT or 5NT の場合に3NTまで落とせれば落とす
			if ( (level > 3)&&(level < 6) ) {
				if (bid[another].getLevel() < 4) {
					level = 3;
				} else {
					level = bid[another].getLevel();
				}
			}
			break;
		
		case Bid.SPADE:
		case Bid.HEART:
			// Majorの場合は、5Mの場合は4Mに、3Mの場合は2以上の落とせるところまで落とす
			switch (level) {
			
			case 5:
				if (bid[another].getSuit() > denomination) { // 相手 S こっち H のとき
					if (bid[another].getLevel() < 4) { // 3S までなら4Hにできる
						level = 4; // 4H
					}
				} else {
					if (bid[another].getLevel() < 5) { // 4x までなら4Mにできる
						level = 4;
					}
				}
				break;
			
			case 3:
//			case 2:
				if (bid[another].getSuit() > denomination) {
					level = bid[another].getLevel() + 1;
				} else {
					level = bid[another].getLevel();
					if (level == 1) level++;
				}
				break;
			default:	// fall through
			}
			break;
		
		case Bid.DIAMOND:
		case Bid.CLUB:
			// Minorの場合は、4m, 3m の場合に2以上の落とせるところまで落とす
			switch (level) {
			case 4:
			case 3:
				if (bid[another].getSuit() > denomination) {
					level = bid[another].getLevel() + 1;
				} else {
					level = bid[another].getLevel();
					if (level == 1) level++;
				}
				break;
			default:	// fall through
			}
			break;
		
		default: // fall through
		}
		
		//
		// 6. ダブル状況をつくる
		//
		int totalPt = pts[NSorEW] + pts[NSorEW+2];
		kind = Bid.BID;
		if (totalPt < 13) {
			kind = Bid.DOUBLE;
		} else if (totalPt < 17) {
			if (level > 1) kind = Bid.DOUBLE;
		} else if (totalPt < 21) {
			if (level > 2) kind = Bid.DOUBLE;
		} else if (totalPt < 24) {
			if (level > 3) kind = Bid.DOUBLE;
		} else if (totalPt < 27) {
			if (level > 4) kind = Bid.DOUBLE;
		} else if (totalPt < 34) {
			if (level > 5) kind = Bid.DOUBLE;
		} else if (totalPt < 37) {
			if (level > 6) kind = Bid.DOUBLE;
		}
		
		//
		// 7. ディクレアラーを SOUTH になるようにハンドを回転させる
		//
		for (int i = 0; i < ((declarer - Board.SOUTH) + 4) % 4; i++) {
			Packet tmp = hand[0];
			for (int j = 0; j < 3; j++) {
				hand[j] = hand[j+1];
			}
			hand[3] = tmp;
		}
		
		//
		// 8. 説明文をつくる
		//
		description = "あなたの " + getContractString() + " よ。\n切り札は";
		switch (denomination) {
		case Bid.NO_TRUMP:	description += "ありません。";	break;
		case Bid.SPADE:		description += "スペード、";	break;
		case Bid.HEART:		description += "ハート、";		break;
		case Bid.DIAMOND:	description += "ダイアモンド、";break;
		case Bid.CLUB:		description += "クラブ、";		break;
		default:			description += "なんでしょう。";break;
		}
		
		description += "\n13トリックのうち、" + (level + 6) + "トリック以上とってね";
	}
	
	public String getTitle() {
		return "練習モード";
	}
	
	public Bid getContract() {
		return new Bid(kind, level, denomination);
	}
	
	/**
	 * ハンドを返します
	 */
	public Packet[] getHand() {
		return hand;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getContractString() {
		String result = String.valueOf(level);
		
		switch (denomination) {
		
		case Bid.NO_TRUMP:
			result += "NT";
			break;
		case Bid.SPADE:
			result += "S";
			break;
		case Bid.HEART:
			result += "H";
			break;
		case Bid.DIAMOND:
			result += "D";
			break;
		case Bid.CLUB:
			result += "C";
		}
		
		if (kind == Bid.DOUBLE) result += "ダブル";
		if (kind == Bid.REDOUBLE) result += "リダブル";
		
		return result;
	}
	
	public String getOpeningLead() {
		return null;
	}
	
	public String getThinker() {
		return null;
	}
	
	public boolean isValid() {
		return true; // ぜったい valid
	}
}

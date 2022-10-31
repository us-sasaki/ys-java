package ys.game.card.bridge.gui;

import ys.game.card.bridge.*;
import ys.game.card.*;
import ys.game.card.bridge.ta.*;

/**
 * ブリッジシミュレータのハンド、コントラクトを自動決定し、
 * 問題とするクラス。必ず South がディクレアラーになります。
 * RandomProblemでは、ダブルドコントラクトが起こりやすく、落ちやすいため、
 * 実際のコントラクトにより近いものに変更しています。
 * 盤面評価関数を使い、デノミネーションを決定し、ポイントによってコントラクトの
 * レベルを決定します。ダブルドコントラクトは起こりません。
 *
 * @version		making		31, January 2004
 * @author		Yusuke Sasaki
 */
public class RandomProblem4 implements Problem {
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
	
	/** 盤面評価関数の結果(NS側のトリック数) */
	private int[]		trick;	// [denomination-1]
	
	private int			declarer;
	
	/**
	 * 乱数の種を指定します。
	 */
	private long		randomSeed;
	
/*-------------
 * Constructor
 */
	public RandomProblem4() {
		this(System.currentTimeMillis());
	}
	
	public RandomProblem4(long rseed) {
		count	= new int[4][4];
		hcp		= new int[4];
		pts		= new int[4];
		trick	= new int[6];
		
		randomSeed	= rseed;
	}
	
/*------------------
 * instance methods
 */
	public void start() {
		makeProblem();
	}
	
	public void setSeed(long rseed) {
		randomSeed = rseed;
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
		
		// デノミネーションごとのトリック数を計算する。
		for (int denomination = 1; denomination < 6; denomination++) {
			Board b = new BoardImpl(1);
			b.deal(hand);
			b.setContract(new Bid(Bid.BID, 1, denomination), Board.SOUTH);
			
			OptimizedBoard ob = new OptimizedBoard(b);
			trick[denomination-1] = 1300 - ob.calcApproximateTricks();
			// NS側のトリック * 100 とする
System.out.println(" denom : " + denomination + "  Tricks : " + trick[denomination-1]);
		}
	}
	
	/**
	 * デノミネーション、ディクレアラーが決まった後で
	 * レングスポイント、ダミーポイントを計算する。
	 */
	private void calcPoints(int NSorEW) {
		//
		// レングスポイントなど計算する
		//
		for (int i = NSorEW; i < 4; i+=2) {
			pts[i] = hcp[i];
		}
		
		if (denomination != Bid.NO_TRUMP) {
			// レングスポイントを加算する
			// レングスポイントは、NT/Suitコントラクトで (スートの枚数)-4 が正のとき
			// この数値を加算します(5枚スート..1pts  6枚スート..2pts ……)
			// トランプスートは、次の評価式とする
			// FP（フィットポイント・・和美の造語＝（フィット枚数－8）＊1.5
			for (int i = NSorEW; i < 4; i+=2) {
				for (int suit = 1; suit < 5; suit++) {
					if (suit == denomination) continue;
					int cnt = count[i][suit-1];
					if (cnt > 4) pts[i] += (cnt - 4);
				}
			}
			int fit = count[NSorEW][denomination-1] + count[NSorEW+2][denomination-1];
			if (fit > 8) pts[NSorEW] += ((fit - 8) * 3 / 2);
			// NorEのみに加点しているが、トランプのときはtotalPointしか評価しない
			// ディクレアラーを決めるのに長さを使用しているのみ
		
			// ダミーポイント
			int dummy = (declarer + 2) % 4;
			int dummyTrumps = count[dummy][denomination-1];
			for (int suit = 1; suit < 5; suit++) {
				int cnt = count[dummy][suit-1];
				switch (cnt) {
				
				case 0:	// void
					pts[dummy] += min(5, dummyTrumps*3);
					break;
				case 1: // singleton
					pts[dummy] += min(3, (dummyTrumps-1)*3);
					break;
				case 2: // doubleton
					pts[dummy] += min(1, (dummyTrumps-2)*3);
				default:	// fall through
				}
			}
		}
	}
	
	private static int min(int a, int b) {
		if (a < b) return a;
		return b;
	}
	
	/**
	 * ランダムにハンドを配り、その内容からコントラクトなどを決定します。
	 */
	private void makeProblem() {
		//
		// 1. まず、ランダムにハンドを配る
		//
		Packet pile = PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER);
		PacketImpl.setRandom(new java.util.Random(randomSeed));
		
		pile.shuffle();
		hand = PacketFactory.deal(pile, 4);
		
		for (int i = 0; i < 4; i++) hand[i].arrange();
		
		//
		// 2. スートごとの枚数、HCP、NS側のトリック数を基礎情報として計算する
		//
		calculateAttributes();
		
		//
		// 3. デノミネーション、サイドをトリック数から決定する
		//
		
		// 最大と最小の幅を計算する
		int maxNSTricks = -1;
		int maxDenom 	= -1;
		int minNSTricks = 1400;
		int minDenom	= -1;
		for (int i = 0; i < 5; i++) {
			if (trick[i] > maxNSTricks) {
				maxNSTricks = trick[i];
				if (i < 4) maxDenom = i + 1;
			}
			if (trick[i] < minNSTricks) {
				minNSTricks = trick[i];
				if (i < 4) minDenom = i + 1;
			}
		}
		int NSorEW = -1;
System.out.println("min " + minNSTricks + "  max " + maxNSTricks);
		if (maxNSTricks - minNSTricks <= 100) {
			this.denomination = Bid.NO_TRUMP;
			if (trick[Bid.NO_TRUMP] > 650) NSorEW = 0;	// NS側コントラクト
			else NSorEW = 1;
		} else {
			if (maxNSTricks > 1300 - minNSTricks) {
				NSorEW = 0;
				this.denomination = maxDenom;
			} else {
				NSorEW = 1;
				this.denomination = minDenom;
			}
		}
		
		//
		// 4. ディクレアラーを決定する
		//
		if (this.denomination == Bid.NO_TRUMP) {
			// HCP の大きい方をディクレアラーとする
			if (hcp[NSorEW] > hcp[NSorEW + 2]) {
				this.declarer = NSorEW;
			} else {
				this.declarer = NSorEW + 2;
			}
		} else {
			// スートコントラクトのときは、トランプの長い方
			if (count[NSorEW][this.denomination-1] > count[NSorEW+2][this.denomination-1]) {
				this.declarer = NSorEW;
			} else {
				this.declarer = NSorEW + 2;
			}
		}
		
		//
		// 5. レングスポイント、ダミーポイントを計算する
		//
		calcPoints(NSorEW);
		
		//
		// 6. 点数レンジでレベルを決める
		//
		int totalPt = pts[NSorEW] + pts[NSorEW+2];
System.out.println("Total Point : " + totalPt);
		if (totalPt > 36) {
			// グランドスラム
			level = 7;
		} else
		if ( (totalPt > 32)&&(totalPt < 37) ) {
			// スモールスラム
			level = 6;
		} else
		if ( (totalPt > 29)&&(totalPt < 33 ) ) {
			// ５の代
			level = 5;
		} else
		if ( (totalPt > 26)&&(totalPt <30) ) {
			// ４の代
			level = 4;
		} else
		if ( (totalPt > 23)&&(totalPt < 27) ) {
			// ３の代
			level = 3;
		} else
		if ( (totalPt > 21)&&(totalPt < 24) ) {
			// ２の代
			level = 2;
		} else {
			// １の代
			level = 1;
		}
		kind = Bid.BID; // ダブルなし
		
		int tr = trick[denomination-1]/100;
		if (tr < 7) tr = 13 - tr;
		if (level + 5 > tr) kind = Bid.DOUBLE;
		
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

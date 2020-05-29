package ys.game.card.bridge.gui;

import ys.game.card.bridge.*;
import ys.game.card.*;

/**
 * ブリッジシミュレータのハンド、コントラクトを自動決定し、
 * 問題とするクラス。必ず South がディクレアラーになります。
 *
 * @version		making		22, July 2001
 * @author		Yusuke Sasaki
 */
public class RandomProblem implements Problem {
	protected String	title;
	protected int		kind;
	protected int		level;
	protected int		denomination;
	
	/** NSEW のハンド */
	protected String	description;
	
	protected Packet[]	hand;
	
/*-------------
 * Constructor
 */
	public RandomProblem() {
	}
	
/*------------------
 * instance methods
 */
	public void start() {
		makeProblem();
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
		// 2. フィットの多いスートを探す
		//
		int[][]	count = new int[4][4];
		
		// 各スートについて、
		for (int suit = 1; suit < 5; suit++) {
			// それぞれの枚数を数える
			for (int i = 0; i < 4; i++) {
				count[i][suit-1] = hand[i].countSuit(suit);
			}
		}
		// もっともフィットを持っている側を探し、その枚数も覚えておく
		// 同じフィットを持っている場合、レベルの高いスートを優先する
		int maxFit	= 0;
		int maxSuit = 0;
		int declarer = -1;
		for (int suit = 1; suit < 5; suit++) {
			int ns = count[Board.NORTH][suit-1] + count[Board.SOUTH][suit-1];
			if ( (ns > maxFit)||( (ns == maxFit)&&(suit > maxSuit) ) ) {
				maxFit		= ns;
				maxSuit		= suit;
				declarer	= Board.NORTH; // とりあえず North としておく
			}
			int ew = count[Board.EAST ][suit-1] + count[Board.WEST ][suit-1];
			if ( (ew > maxFit)||( (ew == maxFit)&&(suit > maxSuit) ) ) {
				maxFit		= ew;
				maxSuit		= suit;
				declarer	= Board.EAST; // とりあえず East としておく
			}
		}
		//
		// 3. コントラクトの denomination を決める
		//
		if (maxFit == 7) denomination = Bid.NO_TRUMP;
		else denomination = maxSuit;
		
		if (denomination == Bid.NO_TRUMP) {
			//
			// NTコントラクトの時、１番強い人のサイドのコントラクトとする。
			//
			int maxHcp = 0;
			for (int i = 0; i < 4; i++) {
				int hcp = BridgeUtils.countHonerPoint(hand[i])[0];
				if (hcp > maxHcp) {
					declarer = i % 2;
					maxHcp = hcp;
				}
			}
		}
		
		//
		// 4. declarer を決める
		//
		int hcp			= BridgeUtils.countHonerPoint(hand[declarer  ])[0];
		int anotherHcp	= BridgeUtils.countHonerPoint(hand[declarer+2])[0];
		
		if (denomination == Bid.NO_TRUMP) {
			
			// NT コントラクトの時
			// Honer Point の高い方をディクレアラーとする
			if (hcp < anotherHcp) declarer = declarer + 2;
		} else {
			// スートコントラクトの時
			int decCount = count[declarer  ][denomination-1];
			int dumCount = count[declarer+2][denomination-1];
			if ( decCount < dumCount ) {
				// パートナー(South / West) が declarer
				declarer = declarer + 2;
			} else if (decCount == dumCount) {
				// 枚数が同じ場合は Honer Point の高い方
				if (hcp < anotherHcp) declarer = declarer + 2;
			}
		}
		
		//
		// 5. コントラクトの level を決める
		//
		level = maxFit - 6; // とりあえず
		int totalPt = hcp + anotherHcp;
		if (denomination != Bid.NO_TRUMP) {
			totalPt += (maxFit - 8)*2;
		} else {
			level = 1;
		}
		
		if (totalPt > 36) {
			// グランドスラム
			level = 7;
		}
		if ( (totalPt > 32)&&(totalPt < 37) ) {
			// スモールスラム
			level = 6;
		}
		if ( (totalPt > 28)&&(totalPt < 33)&&( (denomination == Bid.CLUB)||(denomination == Bid.DIAMOND) ) ) {
			// マイナーゲーム
			level = 5;
		}
		else if ( (totalPt > 24)&&(totalPt < 33) ) {
			// ゲーム
			if (denomination == Bid.NO_TRUMP) {
				level = 3;
			} else if ( (denomination == Bid.HEART)||(denomination == Bid.SPADE) ) {
				level = 4;
			} else if ( (denomination == Bid.CLUB)||(denomination == Bid.DIAMOND) ) {
				denomination = Bid.NO_TRUMP;
				level = 3;
			}
		}
		
		//
		// 6. ダブル状況をつくる
		//
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

package ys.game.card.bridge;

import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.Card;
import ys.game.card.CardImpl;

/**
 * プレイされたカード、ディスカードなどの情報から、
 * ディストリビューションを計算するクラス。
 * ４人のスートの枚数に関する情報、間違いないウィナーを取得する static関数を提供します。
 * 将来的には、ハンドパターンについて、その内容（と確率）を提供したい。
 * 本クラスの countDistribution(Board, int) メソッドは、最小、最大について論理的な
 * 値より幅が多くなる可能性があります。これは、最小、最大相互の依存関係があり、逐次的に
 * 求める方式（方程式を求める際のニュートン法ライク）を採用していることに起因します。
 * 将来的に反復処理を行う、方程式で求めるなどで改善する余地がありますが、SimplePlayer2
 * での使用上問題ないレベルです。
 *
 * @author		Yusuke Sasaki
 * @version		a-release		20, January 2002
 */
public class ThinkingUtils {
	static final int MIN = 0;
	static final int MAX = 1;
	
	/**
	 * ４人のディストリビューションを(わかる範囲で)カウントします。
	 * 指定する Board では、ダミーと指定した席のハンド情報を持っている必要があります。
	 *
	 * @param		board		４人のハンドを保持する Board
	 * @param		seat		自分の席
	 * @return		int の３次元配列(int[4][4][2])で、[座席][スート][最大(1) or 最小(0)]
	 */
	public static int[][][] countDistribution(Board board, int seat) {
		if (seat == board.getDummy())
			throw new IllegalArgumentException("ダミーにおけるカウントはサポートしてません");
		if ( (seat < 0)||(seat > 3) )
			throw new IllegalArgumentException("指定された seat の値(="+seat+")が異常です");
		
		int[][][] c = new int[4][][];
		
		// 自分とダミーのハンドのディストリビューションはすでにわかっている
		int dummySeat = board.getDummy();
		
		Packet dummyHand	= board.getHand(board.getDummy());
		Packet myHand		= board.getHand(seat);
		
		c[dummySeat]	= countKnownDistribution(dummyHand);
		c[seat]			= countKnownDistribution(myHand);
		
		//
		// 他の２つのディストリビューションを計算する
		//
		int[] other = new int[2];
		int num = 0;
		for (int dir = 0; dir < 4; dir++) {
			if ( (dir == dummySeat)||(dir == seat) ) continue;
			other[num] = dir;
			num++;
		}
		
		// まず、残っているカード枚数から計算する。
		// (最小枚数)=0
		// (最大枚数)= min( そのスートの残枚数, その人のハンドの枚数)
		Packet played = getPlayedCards(board);
		
		int[][] playedDist = countKnownDistribution(played); // プレイされたカードのディストリビューション
		
		c[ other[0] ] = new int[4][2];
		c[ other[1] ] = new int[4][2];
		
		for (int suit = 0; suit < 4; suit++) {
			int restCards =			13
								- playedDist[suit][MIN]
								- c[dummySeat][suit][MIN]
								- c[seat][suit][MIN]; // 残り枚数
			c[ other[0] ][suit][MIN]
			= c[ other[1] ][suit][MIN]
			= 0;
			
			c[ other[0] ][suit][MAX] = Math.min(restCards, board.getHand( other[0] ).size());
			c[ other[1] ][suit][MAX] = Math.min(restCards, board.getHand( other[1] ).size());
		}
		
		// 次に、ショウアウトの情報を用いる
		// ショウアウト i.e. その人のそのスートのMAX=0
		for (int i = 0; i < board.getTricks(); i++) {
			Trick trick = board.getAllTricks()[i];
			int	leadSuit = trick.getLead().getSuit();
			for (int j = 1; j < trick.size(); j++) {
				int player = (j + trick.getLeader())%4;
				if ( ( player == dummySeat )||( player == seat ) ) continue;
				if (trick.peek(j).getSuit() != leadSuit) { // ショウアウト
					c[ player ][leadSuit-1][MAX] = 0;
					// もう一人はだれかを見つける。そのスートの枚数は確定する。
					int another = 0;
					if (other[0] == player) another = 1;
					c[ other[another] ][leadSuit-1][MAX] =
					c[ other[another] ][leadSuit-1][MIN] =
									13
								- playedDist[leadSuit-1][MAX]
								- c[ dummySeat  ][leadSuit-1][MAX]
								- c[   seat     ][leadSuit-1][MAX];
				}
			}
		}
		// 最後に、ショウアウトしたことから最小枚数が限定される分の修正
		for (int i = 0 ; i < 2; i++) {
			int cards = board.getHand( other[i] ).size()
						- c[other[i]][0][MAX]
						- c[other[i]][1][MAX]
						- c[other[i]][2][MAX]
						- c[other[i]][3][MAX];
			for (int suit = 0; suit < 4; suit++) {
				// 同じスートのカード枚数から出る条件
				int restMinCards =	13
								- playedDist[suit][MAX]
								- c[ dummySeat  ][suit][MAX]
								- c[   seat     ][suit][MAX]
								- c[ other[1-i] ][suit][MAX];
				
				// ハンドの総数から出る条件
				int cc = cards + c[other[i]][suit][MAX];
				restMinCards = Math.max(restMinCards, cc);
				
				// ショウアウトしたことによって、もう一方の other に与える影響があるのでは？
				// 上で考慮済みだった
				// では、MINが決まったことによってMAXにまた影響がでるのでは？
				// -> でる。最大枚数が変わる
				int newMin = Math.max(c[other[i]][suit][MIN], restMinCards);
//				if (newMin > 0) {
//					for (int otherSuit = 0; otherSuit < 4; otherSuit++) {
//						if (otherSuit == suit) continue;
						
				c[ other[i]][suit][MIN] = newMin;
			}
			
		}
		//
		// つねに成り立つ式によるMAXの補正(by 和美)
		//
		for (int i = 0; i < 2; i++) {
			for (int suit = 0; suit < 4; suit++) {
				c[ other[1-i]][suit][MAX] = 	13
								- playedDist[suit][MAX]
								- c[ dummySeat  ][suit][MAX]
								- c[   seat     ][suit][MAX]
								- c[  other[i]  ][suit][MIN];
			}
		}
		return c;
	}
	
	/**
	 * 与えられたハンドのディストリビューションをカウントします。
	 * UnspecifiedCard はカウントされませんが、本メソッドは Specified Card
	 * からなる Packet に対して使用することを想定しています。
	 *
	 * @param		hand		カウントしたいハンド
	 * @return		第一添数はスートをあらわし、第二添数は、最小値か最大値の選択をします。
	 */
	private static int[][] countKnownDistribution(Packet hand) {
		int[][] result = new int[4][2];
		
		for (int i = 0; i < 4; i++) {
			result[i][MIN] = result[i][MAX] = hand.countSuit(i+1); // 最小値 = 最大値 (枚数確定)
		}
		return result;
	}
	
	/**
	 * すでにプレイされたカードを取得します。
	 * 現在、Trick から１枚ずつ取ってくるアルゴリズムです。
	 */
	private static Packet getPlayedCards(Board board) {
		PacketImpl result = new PacketImpl();
		
		Trick[] tr = board.getAllTricks();
		if (tr == null) return result;
		
		int trickCount = board.getTricks();
		if (trickCount < 13) trickCount++;
		for (int i = 0; i < trickCount; i++) {
			for (int j = 0; j < tr[i].size(); j++) {
				result.add(tr[i].peek(j));
			}
		}
		return result;
	}
		
	
}

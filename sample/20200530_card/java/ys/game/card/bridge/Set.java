package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

public class Set {
	protected int	lastDeclarerSideScore;
	protected Card	bestPlay;
	
	public Set(int p, Card play) {
		lastDeclarerSideScore = p;
		bestPlay = play;
	}
	
	// 関数を作ってみる
	public static Set bestPlayOf(Board b) {
 		int declarer = b.getDeclarer();
		int seat = b.getTurn();
		
		Packet h = b.getHand(seat);
		
		// 最良の結果の保存場所
		// 最良の点数
		int maxScore		= Integer.MIN_VALUE;
		int decScore		= 0; // リターンするための declarer 側の点数
		// そのときのプレイ
		Card maxPlay		= null;
		
		// 可能なプレイについてループをまわす
		for (int i = 0; i < h.size(); i++) {
			// 同格のカードはスキップする
			Card trial = h.peek(i);
			try {
				// プレイしたとする
				// このプレイのもたらす結果が最良の結果となるものを保存しておく
				b.play(trial);
				
				// 最後のプレイだった場合は、当然結果は一意
				if (b.getStatus() == Board.SCORING) {
//					maxScore = Score.calculate(b, seat);	// 遅い
					maxScore = BridgeUtils.countDeclarerSideWinners(b);
					decScore = maxScore;
					if ( ((seat^declarer)&1) == 1) maxScore = 13-maxScore;
					maxPlay  = trial;
					b.undo();
					break;
				}
				
				// プレイしたとして、点数がどうなるかを見る。点数は一意に決まる。
				// 敵の番の best play を見ていることになる
				Set s = bestPlayOf(b);
				
				// 自分たちのスコアに直す
				int decsc = s.lastDeclarerSideScore;
				int score;
				if ( ((seat^declarer)&1) == 1) score = 13-decsc; // Defender は マイナスになる
				else score = decsc;
				
				if (maxScore < score) { // より良いプレイだった
					maxScore	= score;
					decScore	= decsc;
					maxPlay		= trial;
				}
				// 次のために b の状態を戻しておく
				b.undo();
			} catch (IllegalPlayException ignored) {
			}
		}
		return new Set(decScore, maxPlay);
	}
	
/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) throws Exception {
		Board board = new BoardImpl(1);
		board.deal();
		
		board.play(new Bid(Bid.BID, 1, Bid.CLUB));
		board.play(new Bid(Bid.PASS));
		board.play(new Bid(Bid.PASS));
		board.play(new Bid(Bid.PASS));
		
		// 前の手と同じ手を与える
//		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=SQJT6HKQT653CQ98&E=S742H84CAJ6D96543&S=SK98HJ7C432DAKT72&W=SA53HA92CKT75DQJ8&bid=1N-P-P-P&contract=1N&declarer=N", board);
//		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=S9432HQ73CAJDJ432&E=SK865HATC753DQT96&S=SQJ7HK865CT642DA5&W=SATHJ942CKQ98DK87&bid=1N-P-P-P&contract=1N&declarer=N", board);
		System.out.println("はじめの状態");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		// このアルゴリズムはどの席でも使いまわしできる
		Player player = new DebugPlayer(board, Board.NORTH);
		
		for (int i = 0; i < 52 - 17; i++) {
			board.play(player.draw());
		}
		
		System.out.println("最後の状態");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		//
		if (board.getStatus() == Board.SCORING) return;
		
		Set s = Set.bestPlayOf(board);
		
		System.out.println("最良の手");
		System.out.println("最高点：" + s.lastDeclarerSideScore);
		System.out.println("最善手：" + s.bestPlay);
	}
}


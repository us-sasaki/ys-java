package ys.game.card.bridge.thinking;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.bridge.*;

public class Conclusion {
	public int	lastNSSideTricks;
	public int	bestPlay;
	
	public Conclusion(int p, int play) {
		lastNSSideTricks = p;
		bestPlay = play;
	}
	
	/**
	 * 与えられた OptimizedBoard に対し、自分たちのトリック数を最大とするプレイ
	 * (最善手)と、そのときのトリック数を返します。
	 * かなりの計算量が必要で、最後の５トリック程度が限界かもしれない。
	 *
	 */
	public static Conclusion bestPlayOf(OptimizedBoard b) {
		int seat = b.getTurn();
		
		// プレイ候補を抽出する
		int[] h = ShortCut.listOptions(b);
		
		// 最良の結果の保存場所
		// 最良の点数
		int maxTricks		= Integer.MIN_VALUE;
		int decTricks		= 0; // リターンするための NS 側の点数
		// そのときのプレイ
		int maxPlay		= -1;
		
		// 可能なプレイについてループをまわす
		for (int i = 0; i < h.length; i++) {
			if (h[i] == -1) break;
			int trial = h[i];
			// プレイしたとする
			// このプレイのもたらす結果が最良の結果となるものを保存しておく
			int leftPlays = b.play(trial);
			
			// 最後のプレイだった場合は、当然結果は一意
			if (leftPlays == 0) {
				maxTricks = b.countNSWinners();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13-maxTricks;
				maxPlay  = trial;
				b.undo();
				break;
			} else if (leftPlays == 4) {
				maxTricks = b.countNSWinnersLeavingLastTrick();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13-maxTricks;
				maxPlay  = trial;
				b.undo();
				break;
			}
			
			// プレイしたとして、点数がどうなるかを見る。点数は一意に決まる。
			// 敵の番の best play を見ていることになる
			Conclusion s = bestPlayOf(b);
			
			// 自分たちのスコアに直す
			int decsc = s.lastNSSideTricks;
			int score;
			if ( (seat&1) == 1) score = 13-decsc; // Defender は マイナスになる
			else score = decsc;
			
			if (maxTricks < score) { // より良いプレイだった
				maxTricks	= score;
				decTricks	= decsc;
				maxPlay		= trial;
			}
			// 次のために b の状態を戻しておく
			b.undo();
		}
		return new Conclusion(decTricks, maxPlay);
	}
	
}


package ys.game.card.bridge.thinking;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.bridge.*;

/**
 * 先読みアルゴリズム本体
 *
 * @version		making
 * @author		Yusuke Sasaki
 */
public class Conclusion2 {
	/** bestPlays 用のカウンタ */
	public static int[][] bpbuf;
	static {
		bpbuf = new int[52][];
		for (int i = 0; i < 52; i++) {
			bpbuf[i] = new int[13-i/4];
		}
	}
	
	public float	lastNSSideTricks;
	public int		bestPlay;
	public int[]	bestPlays;
	public int		bestPlayCount;
	
	/**
	 * @param		p		NS側のポイント(ポイントはトリック数目安)
	 */
	public Conclusion2(float p, int play, int[] plays, int playCount) {
		lastNSSideTricks = p;
		bestPlay = play;
		bestPlays = plays;
		bestPlayCount = playCount;
	}
	
	/**
	 * 与えられた OptimizedBoard に対し、自分たちのトリック数を最大とするプレイ
	 * (最善手)と、そのときのトリック数を返します。
	 * かなりの計算量が必要で、最後の５トリック程度が限界かもしれない。
	 *
	 */
	public static Conclusion2 bestPlayOf(OptimizedBoard b) {
		int seat = b.getTurn();
		
		// プレイ候補を抽出する
		int[] h = ShortCut.listOptions(b);
		
		// どのバッファを使うか
		int[] bp = bpbuf[b.tricks * 4 + b.trickCount[b.tricks]];
		int   bpcnt = 0; // 最善手の候補数
		
		// 最良の結果の保存場所
		// 最良の点数
		float maxTricks		= Float.MIN_VALUE;
		float decTricks		= 0f; // リターンするための NS 側の点数
		int opponentOptions	= 14;
		// そのときのプレイ
		int maxPlay		= -1;
		
		//--------------------
		// １段目 Only の処理
		//--------------------
		// 可能なプレイが１種類しかないとき、そのプレイを行う(highest)
		if ((h.length == 1)||(h[1] == -1)) {
			bp[0] = h[0];
			bpcnt = 1;
			return new Conclusion2(-1f, h[0], bp, bpcnt);
		}
		
		// 可能なプレイについてループをまわす
		for (int i = 0; i < h.length; i++) {
			if (h[i] == -1) break;
			int trial = h[i];
			// プレイしたとする
			// このプレイのもたらす結果が最良の結果となるものを保存しておく
			int leftPlays = b.play(trial);
			
			// 最後のプレイだった場合は、当然結果は一意
			if (leftPlays == 0) {
				maxTricks = (float)b.countNSWinners();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13f-maxTricks;
				opponentOptions	= 0;
				maxPlay			= trial;
				bp[bpcnt++]		= trial;
				b.undo();
				break;
			} else if (leftPlays == 4) {
				maxTricks = (float)b.countNSWinnersLeavingLastTrick();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13f-maxTricks;
				opponentOptions	= 1;
				maxPlay			= trial;
				bp[bpcnt++]		= trial;
				b.undo();
				break;
			}
			
			// プレイしたとして、点数がどうなるかを見る。点数は一意に決まる。
			// 敵の番の best play を見ていることになる
			Conclusion2 s = bestPlayOfImpl(b, 2);
			
			// 自分たちのスコアに直す
			float decsc = s.lastNSSideTricks;
			float score;
			if ( (seat&1) == 1) score = 13f-decsc; // Defender は マイナスになる
			else score = decsc;
			
			if (maxTricks < score) { // より良いプレイだった
				maxTricks	= score;
				decTricks	= decsc;
				maxPlay		= trial;
				
				opponentOptions = s.bestPlayCount;
				
				bpcnt = 0; // リセット
				bp[bpcnt++] = trial;
			} else if (maxTricks == score) { // 同様に最善手だった
				// maxPlay を置きかえるか？
				// 敵の最善手選択肢を減らすようにする
				if (s.bestPlayCount < opponentOptions) {
					opponentOptions = s.bestPlayCount;
					maxPlay = trial;
				}
				bp[bpcnt++] = trial;
			}
			// 次のために b の状態を戻しておく
			b.undo();
		}
		if (bpcnt < bp.length) bp[bpcnt] = -1;
		
		// 同格のカードを含めないで返す
		return new Conclusion2(decTricks, maxPlay, bp, bpcnt);
	}
	
	/**
	 * ２段目以降の処理
	 */
	private static Conclusion2 bestPlayOfImpl(OptimizedBoard b, int depth) {
		int seat = b.getTurn();
		
		// プレイ候補を抽出する
		int[] h = ShortCut.listOptions(b);
		
		// どのバッファを使うか
		int[] bp = bpbuf[b.tricks * 4 + b.trickCount[b.tricks]];
		int   bpcnt = 0; // 最善手の候補数
		
		// 最良の結果の保存場所
		// 最良の点数
		float maxTricks		= Float.MIN_VALUE;
		float decTricks		= 0f; // リターンするための NS 側のトリック数
		int opponentOptions	= 14;
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
				// 最後のプレイだった場合
				// winner 数などは決定している
				maxTricks = (float)b.countNSWinners();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13-maxTricks;
				opponentOptions	= 0;
				maxPlay			= trial;
				bp[bpcnt++]		= trial;
				b.undo();
				break;
			} else if (leftPlays == 4) {
				// 残り１巡を残した場合
				// プレイの仕方は１通りなので、結果を求めることができる
				maxTricks = (float)b.countNSWinnersLeavingLastTrick();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13f-maxTricks;
				opponentOptions	= 1;
				maxPlay			= trial;
				bp[bpcnt++]		= trial;
				b.undo();
				break;
			//------------------------
			// ２段目以降 only の処理
			//------------------------
			} else if ((leftPlays > 16)&&(depth > 7)&&(leftPlays%4==0)) {
				// 先読みの depth が深い場合、maxTricks を概算で求める
				// 精度が落ちるが、スピードはかなり上がるはず
				maxTricks = ShortCut.countApproximateNSWinners(b);
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13f-maxTricks;
				opponentOptions = 1;
				maxPlay			= trial;
				bp[bpcnt++]		= trial;
				b.undo();
				break;
			}
			
			// プレイしたとして、点数がどうなるかを見る。点数は一意に決まる。
			// 敵の番の best play を見ていることになる
			Conclusion2 s = bestPlayOfImpl(b, depth + 1);
			
			// 自分たちのスコアに直す
			float decsc = s.lastNSSideTricks;
			float score;
			if ( (seat&1) == 1) score = 13f-decsc; // Defender は マイナスになる
			else score = decsc;
			
			if (maxTricks < score) { // より良いプレイだった
				maxTricks	= score;
				decTricks	= decsc;
				maxPlay		= trial;
				
				opponentOptions = s.bestPlayCount;
				
				bpcnt = 0; // リセット
				bp[bpcnt++] = trial;
				
				// ３段目以降では、
				// 残り全部とれる場合は、それ以上の最善手はないので、break する。
				int wins;
				if ((seat & 1)== 0) wins = b.nsWins;
				else wins = b.tricks - b.nsWins;
				if ( (depth > 2)&&(maxTricks >= (float)(wins + (leftPlays / 4))) ) {
//System.out.print(".");
					b.undo();
					break;
				}
			} else if (maxTricks == score) { // 同様に最善手だった
				// maxPlay を置きかえるか？
				// 敵の最善手選択肢を減らすようにする
				if (s.bestPlayCount < opponentOptions) {
					opponentOptions = s.bestPlayCount;
					maxPlay = trial;
				}
				bp[bpcnt++] = trial;
			}
			// 次のために b の状態を戻しておく
			b.undo();
			
		}
		if (bpcnt < bp.length) bp[bpcnt] = -1;
		return new Conclusion2(decTricks, maxPlay, bp, bpcnt);
	}
}

package ys.game.card.bridge.ta;

/**
 * 全数探索を行うことで得られる Board に関する情報
 */
public class BoardStatistics {
	public int	bestPlayCount;
	public int	bestPlayPaths;
	public int	totalPlayCount;
	public int	finalNSTricks;
//	public int[]	bestPlays; // いらんかも
	
	public String toString() {
		String result = 	"bestPlays=" + bestPlayCount;
		result = result +	", bestPaths=" + bestPlayPaths;
		result = result +	", totalPlayCount=" + totalPlayCount;
		result = result +	", finalNSTricks=" + finalNSTricks;
		
		return result;
	}
}

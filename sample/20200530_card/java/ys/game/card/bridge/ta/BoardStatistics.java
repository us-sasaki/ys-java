package ys.game.card.bridge.ta;

/**
 * �S���T�����s�����Ƃœ����� Board �Ɋւ�����
 */
public class BoardStatistics {
	public int	bestPlayCount;
	public int	bestPlayPaths;
	public int	totalPlayCount;
	public int	finalNSTricks;
//	public int[]	bestPlays; // ����񂩂�
	
	public String toString() {
		String result = 	"bestPlays=" + bestPlayCount;
		result = result +	", bestPaths=" + bestPlayPaths;
		result = result +	", totalPlayCount=" + totalPlayCount;
		result = result +	", finalNSTricks=" + finalNSTricks;
		
		return result;
	}
}

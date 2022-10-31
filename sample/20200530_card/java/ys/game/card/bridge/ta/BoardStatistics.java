package ys.game.card.bridge.ta;

/**
 * ‘S”’Tõ‚ğs‚¤‚±‚Æ‚Å“¾‚ç‚ê‚é Board ‚ÉŠÖ‚·‚éî•ñ
 */
public class BoardStatistics {
	public int	bestPlayCount;
	public int	bestPlayPaths;
	public int	totalPlayCount;
	public int	finalNSTricks;
//	public int[]	bestPlays; // ‚¢‚ç‚ñ‚©‚à
	
	public String toString() {
		String result = 	"bestPlays=" + bestPlayCount;
		result = result +	", bestPaths=" + bestPlayPaths;
		result = result +	", totalPlayCount=" + totalPlayCount;
		result = result +	", finalNSTricks=" + finalNSTricks;
		
		return result;
	}
}

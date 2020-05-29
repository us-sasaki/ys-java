package ys.game.card.bridge;

/*
 * 2001/ 8/ 7    各メソッドの static 化
 */

/**
 * このクラスはコントラクトブリッジにおいてスコアを算出します。
 * 当面、デュプリケート方式による算出をおこないますが、将来
 * ラバー、マッチポイントなどへの機能拡張を行いたいとおもっています。
 *
 * @version		a-release		21, May 2000
 * @author		Yusuke Sasaki
 */
public class Score {
	static final int[] VUL_BONUSES		= new int[] { 1250, 2000, 500, 50 };
	static final int[] NONVUL_BONUSES	= new int[] { 800, 1300, 300, 50 };
	
	/**
	 * このスコア計算オブジェクトを初期化します。
	 * ただし、現在は実装されていません。
	 */
	public void init() {
	}
	
	/**
	 * 与えられたボード、席における点数を計算します。
	 * 与えられたボードが終了していない場合、IllegalStatusException
	 * がスローされます。
	 *
	 * @param	board		計算対象のボード
	 * @param	seat		計算を行う座席(Board.NORTH など)
	 *
	 * @return	得点
	 */
	public static int calculate(Board board, int seat) {
		if (board.getStatus() != Board.SCORING)
			throw new IllegalStatusException("ボードはまだ終了していないため、点数の計算はできません。");
		
		if ( (seat < 0)||(seat > 3) )
			throw new IllegalArgumentException("指定された座席番号"+seat+"は無効です。");
		
		int		vul			= board.getVulnerability();
		Bid		contract	= board.getContract();
		if (contract.getKind() == Bid.PASS) return 0; // Passed-Out Board
		
		int		declarer	= board.getDeclarer();
		
		return calcImpl(contract, countWinners(board), declarer, seat, vul);
	}
	
	/**
	 * 与えられたボードにおいてディクレアラー側のとったトリック数をカウントします。
	 *
	 * @param		board		ウィナーを数える対象となるボード
	 */
	public static int countWinners(Board board) {
		return BridgeUtils.countDeclarerSideWinners(board);
	}
	
	public static int calculate(Bid contract, int win, int declarer, int seat, int vul) {
		return calcImpl(contract, win, declarer, seat, vul);
	}
	
	private static int calcImpl(Bid contract, int win, int declarer, int seat, int vul) {
		int make = win - 6;
		int up = win - contract.getLevel() - 6;
		
		int score = 0;
		if (up >= 0) {
			//
			// コントラクトに対する基本点計算
			//
			int trickScore = 0;
			switch (contract.getSuit()) {
			
			case Bid.NO_TRUMP:
				trickScore = 30;
				break;
			
			case Bid.SPADE:
			case Bid.HEART:
				trickScore = 30;
				break;
			
			case Bid.DIAMOND:
			case Bid.CLUB:
				trickScore = 20;
				break;
			
			default:
				throw new InternalError("コントラクトのスーツが不正です");
			}
			trickScore = trickScore * contract.getLevel();
			if (contract.getSuit() == Bid.NO_TRUMP) trickScore+=10;
			
			//
			// ダブルの時の修正
			//
			if (contract.getKind() == Bid.DOUBLE) trickScore *= 2;
			if (contract.getKind() == Bid.REDOUBLE) trickScore *= 4;
			
			//
			// アップトリック
			//
			int uptrickBonus = 0;
			switch (contract.getSuit()) {
			
			case Bid.NO_TRUMP:
				uptrickBonus = 30;
				break;
			
			case Bid.SPADE:
			case Bid.HEART:
				uptrickBonus = 30;
				break;
			
			case Bid.DIAMOND:
			case Bid.CLUB:
				uptrickBonus = 20;
				break;
			
			default:
				throw new InternalError("コントラクトのスーツが不正です");
			}
			
			//
			// ダブルの時の修正
			//
			if (contract.getKind() == Bid.DOUBLE) {
				if (isVul(vul, declarer)) score = trickScore + 200 * up;
				else score = trickScore + 100 * up;
			} else if (contract.getKind() == Bid.REDOUBLE) {
				if (isVul(vul, declarer)) score = trickScore + 400 * up;
				else score = trickScore + 200 * up;
			} else score = trickScore + uptrickBonus * up;
			
			//
			// ゲーム、スラムボーナス
			//
			int[] bonuses;
			
			if (isVul(vul, declarer)) {
				//
				// バルの場合
				//
				bonuses = VUL_BONUSES;
			}
			else {
				//
				// ノンバルの場合
				//
				bonuses = NONVUL_BONUSES;
			}
			//
			// ダブルメイクのボーナス
			//
			if (contract.getKind() == Bid.DOUBLE) score += 50;
			else if (contract.getKind() == Bid.REDOUBLE) score += 100;
			
			
			int level = contract.getLevel();
			if (level == 6) score += bonuses[0]; // Small Slum
			else if (level == 7) score += bonuses[1]; // Grand Slum
			else if (trickScore >= 100) score += bonuses[2]; // Game
			else score += bonuses[3];	// partial
			
			if ( ((declarer ^ seat) & 1) == 1 ) {
				score = -score;
			}
		}
		else {
			int down = -up;
			
			if (contract.getKind() == Bid.BID) {
				if (!isVul(vul, declarer)) score = -50 * down;
				else score = -100 * down;
			}
			else if (contract.getKind() == Bid.DOUBLE) {
				if (!isVul(vul, seat)) {
					for (int i = 0; down > 0; i++) {
						if (i == 0) score -= 100;
						else if ( (i > 0)&&(i < 3) ) score -= 200;
						else score -= 300;
						down--;
					}
				}
				else {
					for (int i = 0; down > 0; i++) {
						if (i == 0) score -= 200;
						else score -= 300;
						down--;
					}
				}
			}
			else if (contract.getKind() == Bid.REDOUBLE) {
				if (!isVul(vul, declarer)) {
					for (int i = 0; down > 0; i++) {
						if (i == 0) score -= 200;
						else if ( (i > 0)&&(i < 3) ) score -= 400;
						else score -= 600;
						down--;
					}
				}
				else {
					for (int i = 0; down > 0; i++) {
						if (i == 0) score -= 400;
						else score -= 600;
						down--;
					}
				}
			}
			
			if ( ((declarer ^ seat) & 1) == 1 ) {
				score = -score;
			}
		}
		
		return score;
	}
	
	private static boolean isVul(int vul, int seat) {
		int mask;
		if ( (seat == Board.NORTH)||(seat == Board.SOUTH) ) mask = 1;
		else mask = 2;
		
		if ((vul & mask) > 0) return true;
		return false;
	}
	
/*
 * debug
 */
	public static void main(String[] args) throws Exception {
		Score s = new Score();
//				calcImpl(Bid contract, int win, int declarer, int seat, int vul) {

		int sc = s.calcImpl(new Bid(Bid.BID, 5, Bid.CLUB), 11, 0, 2, 3);
		System.out.println(sc);
		
	}
	
}

package ys.game.card.bridge.thinking;

import ys.game.card.bridge.Bid;

/**
 * Optimized Board に対する評価関数、高速化に関する枝刈りの関数群です
 */
public class ShortCut {
	/**
	 * n 番目のプレイのプレイ候補のリストを格納するための領域です。
	 * 添え字は、[n-1][0,1, ... , 14-n] となります
	 */
	public static int[][]		playOptions;
	static {
		playOptions	= new int[52][];
		for (int i = 0; i < 52; i++)
			playOptions[i] = new int[13-(i/4)];
	}
	
/*------------------
 * instance methods
 */
	/**
	 * プレイ候補となるカードの集合を抽出します。
	 * 高速化のため、同格カードを除く処理を行います。
	 * このメソッドは速度と精度を決めるため、上位で行ってもよいメソッドですが、
	 * 内部保持構造に立ち入るため、本クラスに含めています。
	 * 結果は、現在 n トリック目として、要素数が 14-n の配列となります。
	 * 候補数は少ない場合があり、そのときは候補が終わっていることを示すため、-1 が
	 * 挿入されます。-1 の後の内容は不定です。
	 *
	 * @return		プレイ候補
	 */
	public static int[] listOptions(OptimizedBoard b) {
		int[] result = listPlayableCards(b);
		
		// 枝刈り(1)
		// 同格のカードを除外する
		int lastEntried = result[0];
		for (int i = 1; i < result.length; i++) {
			int next = result[i];
			if (next == -1) break;
			// lastEntried と同格かどうか
			// 「同格」とは、２枚のカードのいずれを出しても同一の結果となること
			// lastEntried, next の間のカードがすべてプレイ済みである場合、「同格」
			// ★★ただし、今場に出ているカードは「プレイ済みでない」と考える必要がある！
			//
			// (1) lastEntried, next ともに同じハンドに入っている
			// (2) result には降順にカードが含まれている
			// 
			int j;
		loop: // おそい処理のループがあるので。。。
			for (j = lastEntried - 1; j > next; j--) {
				if (!b.isPlayed[j]) break;
				// ★★の処理。遅い！！ プレイ済みカードが増えないとここにこないので
				// 影響は少ないとは思うが……
				// もし、影響があった場合、isPlayed を、トリックが終わってから true
				// とすることでこの処理を割愛できる。(isPlayed を isRemoved にする)
				// その場合、OptimizedBoard の draw, undo のみを変更すればよい
				// ShortCut では、isRemoved の使い方しかしていない
				for (int k = 0; k < b.trickCount[b.tricks]; k++) {
					if (j == b.trick[b.tricks][k]) break loop;
				}
			}
			lastEntried = next;
			if (j == next) {
				// 同格。つめる。ループがきちんと回るようにする。
				System.arraycopy(result, i+1, result, i, result.length-i-1);
				result[result.length - 1] = -1; // すでに前に -1 が入っているかもしれないが
				i--;
			}
		}
		
		return result;
	}
	
	/**
	 * 現在プレイするハンドの中で、指定カードと同格のカードを抽出します。
	 * ベストプレイのリストが出たときに、同格のカードも含めた実際のリストに
	 * 戻すときに使用するための関数です。
	 * 本メソッドは特に高速になるようにはつくられていないため、ループの外側で
	 * 呼ぶ必要があります。
	 *
	 * ★★現在場に出ているトリックは、「まだプレイされていない」と認識しないと、
	 *     場に出ているものを含めてシークエンスになっている場合、ローカードを
	 *     フォローしてしまうことがある。
	 *	 
	 * @param		card	同格のカードを探したいカード
	 * @return		同格のカード
	 */
	public static int[] getEqualCards(OptimizedBoard b, int[] cards, int cardCount) {
		int		seat	= (b.leader[b.tricks]+b.trickCount[b.tricks])%4;
		
		// プレイされていないカード = 0 // または今場に出ているカード
		// 持っているカード         = 1
		// 指定されたカード			= 2
		// プレイされたカード       = 3
		// 0 を delimiter として、token を区切り、2 が含まれている token の
		// 1 を 2 に変更する。2 となっているカードを返却する
		int[] tmp = new int[56];
		
		// プレイされたかどうかでフラグを設定
		for (int i = 0; i < 56; i++) {
			if (b.isPlayed[i]) tmp[i] = 3;
			else tmp[i] = 0;
		}
		// 今場に出ているカード
		for (int i = 0; i < b.trickCount[b.tricks]; i++) {
			tmp[b.trick[b.tricks][i]] = 0; // 3 になっていたはず
		}
		
		// 持っているカードの設定
		for (int i = 0; i < b.handCount[seat]; i++) {
			tmp[b.hand[seat][i]] = 1;
		}
		// 指定されたカードの設定
		for (int i = 0; i < cardCount; i++) {
			if (tmp[cards[i]] != 1)
				throw new IllegalArgumentException("カード " + cards[i] + " は持っていません");
			tmp[cards[i]] = 2;
		}
		
		// token ごとの処理
		int tokenStartIndex = 0;
		int resultCount = 0;
		
		while (true) {
			// delimiter でないインデックスを探す --> tokenStartIndex
			for (; tokenStartIndex < 56; tokenStartIndex++) {
				if (tmp[tokenStartIndex] != 0) break;
			}
			if (tokenStartIndex == 56) break;
			
			int tokenEndIndex;
			boolean containsTargetCard = false;
			for (tokenEndIndex = tokenStartIndex; tokenEndIndex < 56; tokenEndIndex++) {
				if (tmp[tokenEndIndex] == 2)
					containsTargetCard = true;
				else if (tmp[tokenEndIndex] == 0) break;
			}
			
			if (containsTargetCard) {
				for (int i = tokenStartIndex; i < tokenEndIndex; i++) {
					if (tmp[i] != 3) {
						tmp[i] = 2;
						resultCount++;
					}
				}
			}
			tokenStartIndex = tokenEndIndex + 1;
			if (tokenStartIndex >= 56) break;
		}
		
		// 結果の作成
		// ここを変えることで弱い順に並べることもできる
		int[] result = new int[resultCount];
		resultCount = 0;
		for (int i = 55; i >= 0; i--) {
			if (tmp[i] == 2)
				result[resultCount++] = i;
		}
		return result;
	}
	
	/**
	 * 本クラスで play() メソッドの引数として設定する、ブリッジルールにのっとった
	 * カード全体の集合を返却します。
	 *
	 * @return		ルール上プレイ可能なカードの集合(カード定数)
	 */
	public static int[] listPlayableCards(OptimizedBoard b) {
		int seat = (b.leader[b.tricks]+b.trickCount[b.tricks])%4;
		int playOptionsIndex = 4*b.tricks+b.trickCount[b.tricks];
		
		if (b.trickCount[b.tricks] != 0) {
			int leadSuit = b.trick[b.tricks][0]/14;
			int options = 0;
			
			// スートフォローを探す
			for (int i = 0; i < b.handCount[seat]; i++) {
				if ((b.hand[seat][i]/14)==leadSuit) {
					// あった
					playOptions[playOptionsIndex][options++] = b.hand[seat][i];
				}
			}
			if (options > 0) {
				if (options < 13-b.tricks) playOptions[playOptionsIndex][options] = -1;
				return playOptions[playOptionsIndex];
			}
		}
		// リードの場合、またはスートフォローできない ... すべてのカードが出せる
		System.arraycopy(b.hand[seat], 0, playOptions[playOptionsIndex], 0, b.handCount[seat]);
		// -1 を挿入する必要はない
		
		return playOptions[playOptionsIndex];
	}
	
	/**
	 * NS 側の手をボードの盤面評価によって近似値を算出します。
	 * 先読みの深さが深いときにある程度以上先の手は本関数で評価することによって
	 * 高速化を行います。
	 * 本関数ではリード状態となっているボード以外を指定することはできません。
	 *
	 * @param		b		評価対象の board
	 * @return		盤面評価によって概算された NS 側のトリック数
	 * @exception	IllegalStateException		リード状態以外のボードが指定された場合
	 */
	public static float countApproximateNSWinners(OptimizedBoard b) {
		if (b.trickCount[b.tricks] != 0)
			throw new IllegalStateException("リード状態でないと使えません");
		
		clearCountCache();
		int leader = b.leader[b.tricks];
		
		float leaderQuickTricks = countApproximateWinners(b, leader);
		float oppQT1 = countApproximateWinners(b, (leader+1)%4 );
		float oppQT2 = countApproximateWinners(b, (leader+3)%4 );
		float oppQuickTricks = ( oppQT1 + oppQT2 ) / 2f;
		
		float leftTricks = 13f - (float)b.tricks;
		float leaderTricks;
		if (leaderQuickTricks + oppQuickTricks > leftTricks) {
			leaderTricks = leaderQuickTricks;
			if (leaderTricks > leftTricks) leaderTricks = leftTricks;
		} else {
			leaderTricks = (leaderQuickTricks + (leftTricks - oppQuickTricks) ) / 2;
		}
		
		if ((leader % 2) == 0) return leaderTricks + (float)b.nsWins;
		return leftTricks - leaderTricks + (float)b.nsWins;
	}
	
	public static float countApproximateWinners(OptimizedBoard b, int seat) {
		float result = 0f;
		for (int suit = 0; suit < 4; suit++) {
			result += (float)countQuickWinnersInSuit(b, suit, seat);
		}
		return result;
	}
	
	/**
	 * あるスートでのＮＳ側の quick winner 数の概算を行います。
	 *
	 * @param		b		評価対象の board
	 * @param		suit	スート(0 がクラブである Optimized 付与による)
	 * @param		seat	中心として考える座席(通常 leader を想定)
	 */
	private static int countQuickWinnersInSuit(OptimizedBoard b, int suit, int seat) {
		
		int lead = seat; //b.leader[b.tricks];	// leader の座席定数
		int pard = (lead + 2) % 4;	// leader のパートナーの座席定数
		
		// まずはスートの長さを調べ、leaderLengthに格納する。
		// ついでにそのスートのスタートインデックスを leaderStartIndex に覚える
		int leaderLength = 0;		// リーダーのそのスートの長さ
		int lowestCardOfLeader	= -1;
		int highestCardOfLeader	= -1;
		int leaderStartIndex = -1;
		for (int i = 0; i < b.handCount[lead]; i++) {
			if ( (b.hand[lead][i] / 14) != suit ) continue;	// 関係ないスートは除外
			lowestCardOfLeader = b.hand[lead][i];
			if (highestCardOfLeader == -1) {
				highestCardOfLeader = b.hand[lead][i];
				leaderStartIndex = i;
			}
			leaderLength++;
		}
		if (leaderLength == 0) return 0;
		
		// パートナーについてもスートの長さを調べる
		int partnerLength = 0;
		int lowestCardOfPard	= -1;
		int highestCardOfPard	= -1;
		int partnerStartIndex	= -1;
		for (int i = 0; i < b.handCount[pard]; i++) {
			if ( (b.hand[pard][i] / 14) != suit ) continue;	// 関係ないスートは除外
			lowestCardOfPard = b.hand[pard][i];
			if (highestCardOfPard == -1) {
				highestCardOfPard = b.hand[pard][i];
				partnerStartIndex = i;
			}
			partnerLength++;
		}
		
		// longerLength, shorterLength, lowestCardOfShorterSuit, highestCardOfLongerSuit
		// に格納する
		int longerLength, shorterLength, lowestCardOfShorterSuit, highestCardOfLongerSuit;
		if (leaderLength >= partnerLength) {
			longerLength	= leaderLength;
			shorterLength	= partnerLength;
			lowestCardOfShorterSuit = lowestCardOfPard;
			highestCardOfLongerSuit = highestCardOfLeader;
		} else {
			longerLength	= partnerLength;
			shorterLength	= leaderLength;
			lowestCardOfShorterSuit = lowestCardOfLeader;
			highestCardOfLongerSuit = highestCardOfPard;
		}
		
		// ウィナーの数をカウントする(totalWinners への格納)
		int cheapestWinner	= -1;
		int totalWinners	= 0;		// ウィナーの数
		if (leaderStartIndex == -1) leaderStartIndex = b.handCount[lead];
		if (partnerStartIndex == -1) partnerStartIndex = b.handCount[pard];
		
		for (int i = suit * 14 + 12; i >= suit * 14; i--) {
			if (b.isPlayed[i]) continue;
			// i には winner のカード定数が入っている
			if ( (leaderStartIndex < b.handCount[lead])
					&&(b.hand[lead][leaderStartIndex] == i) ) { // リーダーが winner を持っている
				totalWinners++;
				leaderStartIndex++;
				cheapestWinner = i;
			} else if ( (partnerStartIndex < b.handCount[pard])
					&&(b.hand[pard][partnerStartIndex] == i) ) { // パートナーが winner を持っている
				totalWinners++;
				partnerStartIndex++;
				cheapestWinner = i;
			} else {	// どっちも持っていない
				break;
			}
		}
		
		boolean lowestCardOfShorterSuitIsWinner = (cheapestWinner == lowestCardOfShorterSuit);
		
		//
		// ①完全にブロックしている場合
		//
		if ( (shorterLength > 0)
				&&(lowestCardOfShorterSuitIsWinner)
				&&( (lowestCardOfShorterSuit%14)>(highestCardOfLongerSuit%14) ) ) {
			if (suit+1 != b.trump) {
				return adjustForRuff(b, suit, seat, shorterLength);
			} else {
				// suit は切り札スートであるため、ラフによる修正は行わない
				// return min(longerLength, totalWinners);
				if (totalWinners > longerLength) return adjustForLongTrump(b, seat, longerLength, longerLength);
				else return adjustForLongTrump(b, seat, totalWinners, longerLength);
			}
		}
		
		//
		// ②完全にはブロックしていない場合
		//   エスタブリッシュでウィナーが増える可能性がある
		//
		if (shorterLength == 0)
			return adjustForEstablishment(b, suit, seat, totalWinners, longerLength);
		int tempxs;
		if (lowestCardOfShorterSuitIsWinner) {
			if (totalWinners-1 > longerLength) tempxs = longerLength;
			else tempxs = totalWinners - 1;
		} else {
			if (totalWinners > longerLength) tempxs = longerLength;
			else tempxs = totalWinners;
		}
		if (suit+1 != b.trump)
			return adjustForEstablishment(b, suit, seat, tempxs, longerLength);
		else
			return adjustForLongTrump(b, seat, tempxs, longerLength);
	}
	
	/**
	 * エスタブリッシュによって増えたトリックを返す。
	 * (xs) ＞ (各オポーネントのそのスートの枚数)
	 * であった場合、エスタブリッシュし、longerLength を返却する。そうでない場合、
	 * xs を返却する。
	 * さらに、ラフによる補正(adjustForRuff)も行う。
	 */
	private static int adjustForEstablishment(
							OptimizedBoard b,
							int suit,
							int seat,
							int xs,
							int longerLength) {
		int opp1 = (seat + 1) % 4;
		int opp2 = (seat + 3) % 4;
		
		// 判定
		if ( (xs >= countSuit(b, suit, opp1))&&(xs >= countSuit(b, suit, opp2)) ) {
				return adjustForRuff(b, suit, seat, longerLength);
		}
		return adjustForRuff(b, suit, seat, xs);
	}
	
	/**
	 * オポーネントのラフによってトリック数を減らす修正を行う。
	 * 
	 */
	private static int adjustForRuff(
							OptimizedBoard b,
							int suit,
							int seat,
							int xs) {
		if (b.trump == Bid.NO_TRUMP) return xs;
		if (b.trump == (suit + 1)) return xs;
		
		// ラフによる補正を加える
		int opp1 = (seat + 1)%4;
		
		if (countSuit(b, b.trump - 1, opp1) > 0) { // トランプを持っている
			int cnt = countSuit(b, suit, opp1);
			if (xs > cnt) xs = cnt;
		}
		
		int opp2 = (seat + 3)%4;
		
		if (countSuit(b, b.trump - 1, opp2) > 0) {
			int cnt = countSuit(b, suit, opp2);
			if (xs > cnt) xs = cnt;
		}
		return xs;
	}
	
	/**
	 * 長いトランプスートによるトリックを加算します。
	 */
	private static int adjustForLongTrump(
							OptimizedBoard b,
							int seat,
							int xs,
							int longerLength) {
		int opp1Len = countSuit(b, b.trump - 1, (seat + 1)%4);
		if (xs <= opp1Len) longerLength -= (opp1Len - xs);
		int opp2Len = countSuit(b, b.trump - 1, (seat + 3)%4);
		if (xs <= opp2Len) longerLength -= (opp2Len - xs);
		
		if (longerLength > xs) return longerLength; // 増える場合のみ適用
		return xs;
	}
	
	/**
	 * 特定のハンドの指定スートの枚数をカウントします。
	 * 最初と OptimizedBoard の状態が変わったときに、clearCountCache() をする必要があります。
	 *
	 * @param		suit		Optimized Board 付与の値を指定します
	 */
	// countSuit() で使用するカウント値のキャッシュです
	// ある程度効果が見こまれ、対応が楽なため実装しています
	private static int[][]			valueCache	= new int[4][4];
	
	private static int countSuit(OptimizedBoard b, int suit, int seat) {
		// キャッシュに入っていればそれを返す
		if (valueCache[suit][seat] >= 0) {
			return valueCache[suit][seat];
		}
		
		// カウント処理
		int count = 0;
		for (int i = 0; i < b.handCount[seat]; i++) {
			if ( (b.hand[seat][i] / 14) == suit ) count++;
		}
		
		valueCache[suit][seat] = count;
		
		return count;
	}
	
	private static void clearCountCache() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				valueCache[i][j] = -1;
			}
		}
	}
}

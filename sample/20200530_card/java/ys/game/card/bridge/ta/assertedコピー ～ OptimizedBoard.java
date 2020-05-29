package ys.game.card.bridge.ta;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.bridge.Board;
import ys.game.card.bridge.Trick;
import ys.game.card.bridge.IllegalStatusException;

/**
 * 先読みに特化した Board 的なオブジェクト
 * 内部的にプレイの状態は、カードの状態が変化することで表現する。
 * int card[56] がその状態を示す変数である。
 *
 */
public class OptimizedBoard {
	static final String[] SUIT_STR = new String[] { "C", "D", "H", "S", "*" };
	static final String[] VALUE_STR =
			new String[] { "2","3","4","5","6","7","8","9","T","J","Q","K","A", "_"};
	static final String[] SEAT_STR = new String[] { "N", "E", "S", "W", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
	
	static final int TRICK_MULTIPLICITY = 100;
	
	/**
	 * カードの状態を示す。
	 * 下位４ビットは、所有者(North=0, East=1, South=2, West=3)をあらわし、
	 * それを除いた上位ビットはプレイ番号(1-52, 0は未プレイ)を示す。
	 * なお、実際にあらわすカードのない添え字(14, 28, 42, 56)のカードには 15 が格納される。
	 * 添え字はカード定数であり、バリューとスートをあらわす１つの値である。
	 * バリューは、2を0に、Aを12に対応させ、クラブを0, スペードを3に対応させる。<br>
	 * (カード定数)＝(バリュー)＋(スート)×14 で求める。
	 */
	int[]	card;
	
	/**
	 * プレイ番号(1..52)からカード番号を得る、逆引き用インデックス。
	 */
	int[]	play;
	
	/**
	 * winner(次のleader)の座席を格納する。添え字は、0-13 となる。
	 */
	int[]	leader;
	
	/**
	 * これまでのプレイカウント
	 * play() を呼ぶとカウントアップする。
	 * Opening Lead 状態では 0 で、Opening Lead をプレイすると 1 になる。
	 * Scoring 状態では 52 となっている。
	 */
	int		count;
	
	/**
	 * トランプスート(クラブ=0、スペード=3)
	 */
	int		trump;
	
	/**
	 * NS側のとったトリック数
	 */
	int		nsWins;
	
	/**
	 * new させないために depth ごとに使用する BoardStatistics のバッファ
	 */
	BoardStatistics[]	statBuffer;
	
	/**
	 * 最後の１トリック算出用の高速化バッファです
	 */
	int[]	lastPlayBuffer;
	
	/**
	 * 概算モードに移る depth の指定です。
	 * この depth を超えた最初のリード状態で概算を行います。
	 */
	int		depthBorder;
	
	int[]	bestPlay;
	//
	// 以降、概算アルゴリズム用
	//
	private static final int SEAT = 4;
	private static final int SUIT = 4;
	private static final int NS_OR_EW = 2;
	private static final int CARDS = 56;
	
	public	int[][] suitCount		= new int[SEAT][SUIT]; // 枚数が格納される
	public	int[][] totalWinners	= new int[NS_OR_EW][SUIT]; // ns, ew の Winnerの数
	public	int[][] longerLength	= new int[NS_OR_EW][SUIT];
	public	int[][] shorterLength	= new int[NS_OR_EW][SUIT];
	public	int[][]	lowestCard		= new int[SEAT][SUIT];
	public	int[][]	highestCard		= new int[SEAT][SUIT];
	public	int[][] lowestCardOfShorterSuit	= new int[NS_OR_EW][SUIT];
	public	int[][] highestCardOfLongerSuit	= new int[NS_OR_EW][SUIT];
	
	public boolean[] isWinner = new boolean[CARDS];
	public int		limitTricks; // calcPropData

/*-------------
 * Constructor
 */
	/**
	 * 
	 */
	public OptimizedBoard(Board board) {
		int s = board.getStatus();
		if ( (s != Board.PLAYING)&&(s != Board.OPENING) )
			throw new IllegalStatusException("指定された Board は、OPENING または PLAYING ステータスでなければなりません");
		card	= new int[56];
		for (int i = 13; i < 56; i+=14) {
			card[i] = 15;
		}
		play	= new int[53];
		leader	= new int[14];
		count	= 0;
		nsWins	= 0;
		statBuffer	= new BoardStatistics[52];
		for (int i = 0; i < 52; i++) {
			statBuffer[i] = new BoardStatistics();
		}
		lastPlayBuffer = new int[4];
		
		depthBorder		= 4;		// 概算は使用しない
		
		trump = board.getTrump() - 1;
		
		// ハンド状態のコピー
		for (int seat = 0; seat < 4; seat++) {
			Packet h = board.getHand(seat);
			for (int n = 0; n < h.size(); n++) {
				Card c = h.peek(n);
				int value = c.getValue();
				if (value == Card.ACE) value = 14;
				card[(c.getSuit() - 1)*14+(value-2)] = seat;
			}
		}
		
		// トリック状態のコピー
		Trick[] tr = board.getAllTricks();
		nsWins = 0;
		for (int i = 0; i < tr.length; i++) {
			if (tr[i] == null) break;
			for (int j = 0; j < tr[i].size(); j++) {
				Card c = tr[i].peek(j);
				int value = c.getValue();
				if (value == Card.ACE) value = 14;
				int index = (c.getSuit() - 1)*14+(value-2);
				card[index] = (i * 4 + j + 1) << 4;
				card[index] += (tr[i].getLeader() + j) % 4;
				play[i * 4 + j + 1] = index;
				count++;
			}
			leader[i] = tr[i].getLeader();
			if (!tr[i].isFinished()) break;
			leader[i+1] = tr[i].getWinner();
			if ((leader[i + 1] & 1) == 0) nsWins++;
		}
		
		bestPlay = new int[14];
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 実際には先読みルーチンの中でインラインに書くことになるだろうメソッド
	 *
	 * @param		playedCard		プレイされたカード定数
	 * @return		今のプレイのプレイカウント(1..52) 52が最後のプレイとなる
	 */
	public final int play(int playedCard) {
		count++;
		card[playedCard] += (count << 4);
		play[count] = playedCard;
		
		if ( (count%4) != 0 ) return count;
		
		// １トリック完了
		
		// ウィナーの決定、leader に設定する
		int		winner	= leader[count/4 - 1];
		int		winCard = play[count - 3];
		
		for (int i = -2; i <= 0; i++) {
			int card = play[count + i];
			if ( (winCard/14) == trump ) {
				if ( (card/14) == trump ) {
					if (card > winCard) {
						// winCard が trump の場合は、大きいトランプを出さなければ勝たない
						winCard	= card;
						winner	= (leader[count/4 - 1] + i + 3) % 4;
					}
				}
			} else if ( (card/14) == trump ) {
				// はじめて出た trump は必ず勝つ
				winCard	= card;
				winner	= (leader[count/4 - 1] + i + 3) % 4;
			} else if ( (winCard / 14) == (card / 14) ) {
				if (card > winCard) {
					// スートフォローの場合、大きいバリューなら勝つ
					winCard	= card;
					winner	= (leader[count/4 - 1] + i + 3) % 4;
				}
			}
		}
		
		// 次の leader を設定する。
		leader[count/4]	= winner;
		
		if ((winner & 1) == 0) nsWins++;
		
		return count;
	}
	
	/**
	 * play() を呼ぶ前の状態に戻します。
	 * leader[] の値のリセットはとりあえず行っていません。
	 * つまり、現在以降のleader[] の値は不定です。
	 * また、play[] についてもリセットしていません。
	 */
	public final void undo() {
		if ( (count%4) == 0 ) {
			if ((leader[count/4] & 1) == 0) nsWins--;
		}
		card[play[count]]	&= 0x0F;
		count--;
		
		// leader のリセットは行わない。(高速化)
	}
	
	/**
	 * 何手先読みを行うかを設定する。動的に変更可能。
	 * ０を設定すると、はじめのリード状態まで先読みを行う。
	 * １を設定すると、１手読み、つづくはじめのリード状態まで先読みを行う。
	 * 48以上を設定すると、最後まで読みきる。
	 */
	public void setDepthBorder(int depthBorder) {
		this.depthBorder = depthBorder;
	}
	
/*------------
 * 先読み本体
 */
	public int[] getBestPlay() {
		// プレイ候補を探すループ
		int turn = (leader[count/4] + count) % 4;
		boolean nsside = ( (turn & 1) == 0 );
		
		//
		// 再帰的処理
		//
		int countAtLead		= (count/4)*4+1;
		
		// スートフォローできるかどうかの判定
		// 現在、スートフォローできるかどうかの検索と実際にプレイするループの
		// ２つをまわしているが、インライン展開することで１つにできる
		int	startIndex	= 0;
		int	endIndex	= 55;
		if ( (count % 4) != 0 ) {
			int suit	= play[countAtLead] / 14;
			int suit2	= suit * 14;
			for (int c = suit2; c < suit2 + 13; c++) {
				if (card[c] == turn) {
					// スートフォローできる
					startIndex	= c;
					endIndex	= suit2 + 13;
					break;
				}
			}
		}
		
		boolean lastEntried	= false; // 同格カードを除くための変数
		int bestPlayCount	=  0;
		int bestTricks		= -1;
		
		int countAtLead2	= countAtLead << 4;
		
		// リードの場合、またはスートフォローできない場合(なんでも出せる)
		// ボードが終わりに近づくにつれて無駄が多くなる。....低速化
		for (int c = startIndex; c < endIndex; c++) {
			int tmp = card[c];
			if ((tmp > 15)&&(tmp < countAtLead2)) continue; // プレイされたカードは無視
			
			if (tmp == turn) { // 今場に出ているカードはまだプレイされていないと考える
				if (lastEntried) continue;
				lastEntried = true;
				// 持っていて、プレイされていない ... c を出せる
				play(c);
				
				BoardStatistics stats = calculateImpl(0);
System.out.println(getCardString(c) + " のボード統計情報");
System.out.println(stats);
				
				// best play かどうかの判定、bestPlayCount, bestTricks の更新
				int finalTricks;
				if (nsside) finalTricks = stats.finalNSTricks;
				else finalTricks = 13 * TRICK_MULTIPLICITY - stats.finalNSTricks;
				
				if (bestTricks < finalTricks) {
					bestTricks		= finalTricks;
					bestPlayCount	= 1;
					bestPlay[0]		= c;
				} else if (bestTricks == finalTricks) {
					bestPlay[bestPlayCount++] = c;
				}
				
				undo();
			} else {
				lastEntried = false; // 他の人が持っている or デリミタ... シーケンスが切れた
			}
			// 抜けるのは、今プレイ中のカードのみ
		}
		bestPlay[bestPlayCount] = -1;
		return bestPlay;
	}
	
	public final BoardStatistics calculate() {
		return calculateImpl(0);
	}
	
	private final BoardStatistics calculateImpl(int depth) {
		// 結果オブジェクト。使いまわすことで、高速化。
		BoardStatistics result = statBuffer[depth];
		result.totalPlayCount = 0;
		
		// プレイ候補を探すループ
		int turn = (leader[count/4] + count) % 4;
		
		//
		boolean nsside = ( (turn & 1) == 0 );
		
		//
		// 帰納法のはじめ
		//
		if (count == 52) {
			// 最終トリックだった場合の返却
			result.totalPlayCount	= 1;
			result.bestPlayCount	= 1;
			result.bestPlayPaths	= 1;
			result.finalNSTricks	= nsWins * TRICK_MULTIPLICITY;
if ((result.finalNSTricks % 10) != 0) System.out.println("asserted in lastTrick");
			
			return result;
		} else if (count == 48) {
			// 残り１トリックだった場合の返却
			result.totalPlayCount	= 1;
			result.bestPlayCount	= 1;
			result.bestPlayPaths	= 1;
			result.finalNSTricks	= nsWins * TRICK_MULTIPLICITY;
			
			// nsWins を求める
			
			// 全員分最後の１枚が何か調べ、lastPlayBuffer に格納する
			for (int i = 0; i < 55; i++) {
				if (card[i] < 4) {
					lastPlayBuffer[card[i]] = i;
				}
			}
			// ウィナーの決定、leader に設定する
			int		winner	= leader[count/4];
			int		leaderSeat	= winner;
			int		winCard = lastPlayBuffer[leaderSeat];
			
			for (int i = 1; i < 4; i++) {
				int card = lastPlayBuffer[(leaderSeat + i) % 4];
				if ( (winCard/14) == trump ) {
					if ( (card/14) == trump ) {
						if (card > winCard) {
							// winCard が trump の場合は、大きいトランプを出さなければ勝たない
							winCard	= card;
							winner	= (leaderSeat + i) % 4;
						}
					}
				} else if ( (card/14) == trump ) {
					// はじめて出た trump は必ず勝つ
					winCard	= card;
					winner	= (leaderSeat + i) % 4;
				} else if ( (winCard / 14) == (card / 14) ) {
					if (card > winCard) {
						// スートフォローの場合、大きいバリューなら勝つ
						winCard	= card;
						winner	= (leaderSeat + i) % 4;
					}
				}
			}
			
			if ((winner & 1) == 0) result.finalNSTricks+=TRICK_MULTIPLICITY;
if ((result.finalNSTricks % 10) != 0) System.out.println("asserted in remaining1Trick");
			
			return result;
		}
		
		//
		// depthBorder を超えているか？
		//
		if ((depth >= depthBorder)&&( (count%4) == 0 )) {
			// ここで概算を行う
			int tricks = calcApproximateTricks(); // 現在のリーダーがとれるトリック数
if (tricks > limitTricks * TRICK_MULTIPLICITY) {
System.out.println("トリック数１４以上:"+tricks);
}
if (tricks < 0) {
System.out.println("トリック数マイナス："+tricks);
}
			if ( (leader[count/4] % 2) == 0 ) {
				// リーダーは NS
				tricks += nsWins * TRICK_MULTIPLICITY;
			} else {
				// リーダーは EW
				tricks = (nsWins + 13 - (count/4)) * TRICK_MULTIPLICITY - tricks;
			}
			
			// 最終トリックだった場合の返却
			result.totalPlayCount	= 10;
			result.bestPlayCount	= 1;
			result.bestPlayPaths	= 1;
			result.finalNSTricks	= tricks;
if ((result.finalNSTricks % 10) != 0) System.out.println("asserted in depthBorder超えているか？" +  tricks);

			
			return result;
		}
		
		//
		// 再帰的処理
		//
		int countAtLead		= (count/4)*4+1;
		
		// スートフォローできるかどうかの判定
		// 現在、スートフォローできるかどうかの検索と実際にプレイするループの
		// ２つをまわしているが、インライン展開することで１つにできる
		int	startIndex	= 0;
		int	endIndex	= 55;
		if ( (count % 4) != 0 ) {
			int suit	= play[countAtLead] / 14;
			int suit2	= suit * 14;
			for (int c = suit2; c < suit2 + 13; c++) {
				if (card[c] == turn) {
					// スートフォローできる
					startIndex	= c;
					endIndex	= suit2 + 13;
					break;
				}
			}
		}
		
		boolean lastEntried	= false; // 同格カードを除くための変数
		int bestPlayCount	=  0;
		int bestPlayPath	=  0;
		int bestTricks		= -1;
		
		int countAtLead2	= countAtLead << 4;
		
		// リードの場合、またはスートフォローできない場合(なんでも出せる)
		// ボードが終わりに近づくにつれて無駄が多くなる。....低速化
		for (int c = startIndex; c < endIndex; c++) {
			int tmp = card[c];
			if ((tmp > 15)&&(tmp < countAtLead2)) continue; // プレイされたカードは無視
			
			if (tmp == turn) { // 今場に出ているカードはまだプレイされていないと考える
				if (lastEntried) continue;
				lastEntried = true;
				// 持っていて、プレイされていない ... c を出せる
				play(c);
				
				BoardStatistics stats = calculateImpl(depth+1);
				
				// best play かどうかの判定、bestPlayCount, bestTricks の更新
				int finalTricks;
				if (nsside) finalTricks = stats.finalNSTricks;
				else finalTricks = 13 * TRICK_MULTIPLICITY - stats.finalNSTricks;
if (finalTricks < 0) System.out.println("finalTricks minuses : " + finalTricks);
				if (bestTricks < finalTricks) {
					bestTricks		= finalTricks;
					bestPlayCount	= 1;
					bestPlayPath	= stats.bestPlayPaths;
				} else if (bestTricks == finalTricks) {
					bestPlayCount++;
					bestPlayPath	+= stats.bestPlayPaths;
				}
				
				// result の更新
				result.totalPlayCount += stats.totalPlayCount;				
				
				undo();
			} else {
				lastEntried = false; // 他の人が持っている or デリミタ... シーケンスが切れた
			}
			// 抜けるのは、今プレイ中のカードのみ
		}
		
		//
		// 結果生成
		//
		result.bestPlayCount	= bestPlayCount;
		result.bestPlayPaths	= bestPlayPath;
		
		if (nsside) result.finalNSTricks	= bestTricks;
		else	result.finalNSTricks	= 13 * TRICK_MULTIPLICITY - bestTricks;
if ((bestTricks % 10) != 0) System.out.println("asserted in 結果生成");
		
		return result;
	}
	
/*-----------------------------------
 * 和美による Board 概算アルゴリズム
 */
	public final int calcApproximateTricks() {
		int seat = (leader[count/4] + count) % 4;
		calcPropData();
		
		int leaderTricks	= calcX(seat);
		
		// オポーネントから見た leader のトリック数(残りトリック数 - oppのクイックトリック)
		int opponentTricks	= limitTricks - calcMaxX(1 - (seat & 1));
		
		if (leaderTricks > opponentTricks)
			return leaderTricks * TRICK_MULTIPLICITY;
		
		return (leaderTricks + opponentTricks) * TRICK_MULTIPLICITY / 2;
	}
	
	/**
	 * 和美アルゴリズムで規定されている以下の値を計算する。
	 * longerLength
	 * shorterLength
	 * totalWinners
	 * lowestCardOfShorterSuit
	 * highestCardOfLongerSuit
	 * isWinner
	 */
	public final void calcPropData() {
		//
		// suitCount を求める
		// highest card, lowest card は何か？
		//
		for (int i = 0; i < SEAT; i++) {
			for (int j = 0; j < SUIT; j++) {
				suitCount[i][j]		= 0;
				lowestCard[i][j]	= 0;
			}
		}
		
		for (int i = 0; i < 55; i++) {
			if (card[i] < 4) {
				int tmp = i / 14;
				int tmp2 = card[i];
				if (lowestCard[tmp2][tmp] == 0)	lowestCard[tmp2][tmp] = i;
				suitCount[tmp2][tmp]++;
				highestCard[tmp2][tmp] = i;
			}
		}
		
		//
		// winnerの数を数える
		// この処理は、毎回やるよりも、play で更新した方が速いのでは？
		//
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				totalWinners[i][j] = 0;
			}
		}
		
		int NSorEW = -1;
		
		for (int i = 54; i >= 0; i--) {
			isWinner[i] = false;
			if (card[i] > 15) continue;
			if (card[i] == 15) {
				NSorEW = -1;
				continue;
			}
			if (NSorEW == -1) NSorEW = (card[i] & 1);
			if ( (card[i] & 1) == NSorEW ) {
				totalWinners[NSorEW][i/14]++;
				isWinner[i] = true;
			} else {
				// winner シーケンスが切れた
				// skip する
				i = (i/14)*14; // このあと、-1 される
				NSorEW = -1;
			}
		}
		
		//
		// longer, shorter を考える
		//
		
		for (int suit = 0; suit < 4; suit++) {
			// NS で考える
			if (suitCount[0][suit] > suitCount[2][suit]) {
				highestCardOfLongerSuit[0][suit] = highestCard[0][suit];
				longerLength[0][suit]	= suitCount[0][suit];
				shorterLength[0][suit]	= suitCount[2][suit];
				lowestCardOfShorterSuit[0][suit] = lowestCard[2][suit];
			} else {
				highestCardOfLongerSuit[0][suit] = highestCard[2][suit];
				longerLength[0][suit]	= suitCount[2][suit];
				shorterLength[0][suit]	= suitCount[0][suit];
				lowestCardOfShorterSuit[0][suit] = lowestCard[0][suit];
			}
			
			// EW で考える
			if (suitCount[1][suit] > suitCount[3][suit]) {
				highestCardOfLongerSuit[1][suit] = highestCard[1][suit];
				longerLength[1][suit]	= suitCount[1][suit];
				shorterLength[1][suit]	= suitCount[3][suit];
				lowestCardOfShorterSuit[1][suit] = lowestCard[3][suit];
			} else {
				highestCardOfLongerSuit[1][suit] = highestCard[3][suit];
				longerLength[1][suit]	= suitCount[3][suit];
				shorterLength[1][suit]	= suitCount[1][suit];
				lowestCardOfShorterSuit[1][suit] = lowestCard[1][suit];
			}
		}
		
		limitTricks = 13 - (count/4);
	}
	
	/**
	 * 指定された座席での(準)クイックトリック数Ｘを求めます。
	 */
	public final int calcX(int seat) {
		int NSorEW = (seat & 1);
		int result = 0;
		
		for (int suit = 0; suit < 4; suit++) {
			if (suitCount[ seat ][suit] > 0) result += calcXs(NSorEW, suit);
		}
		if (result > limitTricks) return limitTricks;
		return result;
	}
	
	/**
	 * Max(Te, Tw)
	 */
	public final int calcMaxX(int NSorEW) {
		int opp2 = NSorEW + 2; // opp1 = NSorEW
		int result1 = 0;
		int result2 = 0;
		
		for (int suit = 0; suit < 4; suit++) {
			int r = calcXs(NSorEW, suit);
			if (suitCount[NSorEW][suit] > 0) result1 += r;
			if (suitCount[ opp2 ][suit] > 0) result2 += r;
		}
		
		if (result1 > result2) {
			if (result1 > limitTricks) return limitTricks;
			return result1;
		}
		if (result2 > limitTricks) return limitTricks;
		return result2;
	}
	
	/**
	 * 指定された NS/EW とスートに関する(準)クイックトリック数 Xs を求めます。
	 */
	public final int calcXs(int NSorEW, int suit) {
		int xs;
		// (A) ①完全にブロックしている場合
		if ( (shorterLength[NSorEW][suit] > 0)
				&&(isWinner[lowestCardOfShorterSuit[NSorEW][suit]])
				&&( (lowestCardOfShorterSuit[NSorEW][suit] % 14)
						> (highestCardOfLongerSuit[NSorEW][suit] % 14) ) ) {
			if (suit == trump) {
				if (totalWinners[NSorEW][suit] > longerLength[NSorEW][suit]) {
					xs = longerLength[NSorEW][suit];
				} else {
					xs = totalWinners[NSorEW][suit];
				}
			} else {
				xs = shorterLength[NSorEW][suit];
			}
		} else {
			// (A) ②完全にはブロックしていない場合
			if (shorterLength[NSorEW][suit] == 0) {
				xs = totalWinners[NSorEW][suit];
			} else {
				if (isWinner[lowestCardOfShorterSuit[NSorEW][suit]]) {
					// lowestCardOfShorterSuit が winner
					// オーバーテイクする
					if (totalWinners[NSorEW][suit]-1 > longerLength[NSorEW][suit]) {
						xs = longerLength[NSorEW][suit];
					} else {
						xs = totalWinners[NSorEW][suit]-1;
						if (xs < 0) xs = 0;
					}
				} else {
					// lowestCardOfShorterSuit が winner でない
					if (totalWinners[NSorEW][suit] > longerLength[NSorEW][suit]) {
						xs = longerLength[NSorEW][suit];
					} else {
						xs = totalWinners[NSorEW][suit];
					}
				}
			}
			// (B) エスタブリッシュによる昇格分の修正
			//   (A) ②の３つの場合について...
			if (suit != trump) {
				int opp1 = NSorEW + 1;
				int opp2 = (NSorEW + 3)%4;
				if ( (xs >= suitCount[opp1][suit]) && (xs >= suitCount[opp2][suit]) ) {
					xs = longerLength[NSorEW][suit];
				}
			}
		}
		
		// (C) さらにこの後で、オポーネントにラフされる分を考慮からはずす
		if (suit != trump) {
			if (trump < 4) {
				int opp1 = (NSorEW + 1) % 4;
				int opp2 = (NSorEW + 3) % 4;
				
				if ( (suitCount[opp1][trump] > 0)&&(xs > suitCount[opp1][suit])) {
					xs = suitCount[opp1][suit];
				}
				if ( (suitCount[opp2][trump] > 0)&&(xs > suitCount[opp2][suit])) {
					xs = suitCount[opp2][suit];
				}
			}
		} else {
			// (D) s が切り札スートのとき、スモールカードによるトリックを考慮し、修正
			int opp1 = (NSorEW + 1) % 4;
			int opp2 = (NSorEW + 3) % 4;
			int adj1 = suitCount[opp1][suit] - xs;
			if (adj1 < 0) adj1 = 0;
			int adj2 = suitCount[opp2][suit] - xs;
			if (adj2 < 0) adj2 = 0;
			
			int tmp = longerLength[NSorEW][suit] - adj1 - adj2;
			if (tmp > xs) xs = tmp;
			// トランプの短い側から計算すると、クイックトリックも消されるため、
			// この計算結果がクイックトリック Xs より悪い場合、Xs を採用する
		}
		return xs;
	}

/*------------
 * toString()
 */
	/**
	 * カード定数から C5 などのカードを示す文字列を得ます。
	 */
	public static String getCardString(int card) {
		return SUIT_STR[card/14]+VALUE_STR[card%14];
	}
	
	/**
	 * デバッグ用の文字列に変換します。
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		String nl = System.getProperty("line.separator");
		
		s.append("●変数内容表示●");
		s.append(nl);
		s.append("count     ：");	s.append(count);	s.append(nl);
		s.append("nsWins    ：");	s.append(nsWins);	s.append(nl);
		s.append("trump     ：");	s.append(trump);	s.append(nl);
		s.append("カード状態：");
		for (int i = 0; i < card.length; i++) {
			s.append(getCardString(i));
			s.append(':');
			s.append(card[i]/16);
			s.append('/');
			s.append(SEAT_STR[card[i]%16]);
			s.append(' ');
		}
		s.append(nl);
		s.append("プレイされたカード：");
		for (int i = 1; i <= count; i++) {
			s.append(i);
			s.append(':');
			s.append(getCardString(play[i]));
			if ( (card[play[i]]/16) != i ) s.append("論理矛盾");
			s.append(' ');
		}
		s.append(nl);
		s.append("leader：");
		for (int i = 0; i <= (count/4); i++) {
			s.append(i);
			s.append(SEAT_STR[leader[i]]);
			s.append(' ');
		}
		s.append(nl);
		s.append("●ボード状態表示●");
		s.append(nl);
		s.append("ハンド情報：");
		s.append(nl);
		
		// NORTH
		for (int suit = 3; suit >= 0; suit--) {
			s.append("               ");
			s.append(getHandString(0, suit));
			s.append(nl);
		}
		
		// WEST, EAST
		for (int suit = 3; suit >= 0; suit--) {
			String wstr = getHandString(3, suit) + "               ";
			wstr = wstr.substring(0, 15);
			s.append(wstr);
			s.append("               ");
			s.append(getHandString(1, suit));
			s.append(nl);
		}
		// SOUTH
		for (int suit = 3; suit >= 0; suit--) {
			s.append("               ");
			s.append(getHandString(2, suit));
			s.append(nl);
		}
		
		return s.toString();
	}
	
	private String getHandString(int seat, int suit) {
		StringBuffer s = new StringBuffer();
		s.append("CDHS".substring(suit, suit+1));
		s.append(':');
		for (int i = card.length-1; i >= 0; i--) {
			if (card[i] != seat) continue;
			if ((i/14) != suit) continue;
			s.append(getCardString(i).substring(1));
		}
		return s.toString();
	}
}

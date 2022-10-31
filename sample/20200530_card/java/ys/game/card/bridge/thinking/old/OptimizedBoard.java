package ys.game.card.bridge.thinking;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.bridge.Board;
import ys.game.card.bridge.Trick;
import ys.game.card.bridge.IllegalStatusException;

/**
 * 先読みルーチン用の Board(PlayHistory) の役割をするオブジェクトで、
 * play, undo が高速に実行されます。
 * 本クラスの内部では、カードを整数型で保持します。実際の Card との対応は、
 * Card.SPADE(=4), Card.HEART(=3), ..
 * フェイスの Ace(=1), 2, 3, 4, ... ,T(=10), J(=11), .. , K(=13) としたときに、
 * まず、フェイスの Ace を 14 に対応させます。そして、カード定数を次のように
 * 算出します。<BR><BR>
 * (カード定数) ＝ ( suit - 1 )×１４＋( value - 2 )    <BR><BR>
 * 14 を乗じているため、C2(=0), ... ,CA(=12), D2(=14),...
 * のようにスート間では、整数が１抜けます。これは、連続するカードの判定を
 * 高速に行うためにしています。
 *
 * @version		a-release		20 October, 2002
 * @author		Yusuke Sasaki
 */
public class OptimizedBoard {
	static final String[] SUIT = new String[] { "C", "D", "H", "S", "*" };
	static final String[] VALUE =
			new String[] { "2","3","4","5","6","7","8","9","T","J","Q","K","A"};

	// 座席定数
	public static final int NORTH		= 0;
	public static final int EAST		= 1;
	public static final int SOUTH		= 2;
	public static final int WEST		= 3;
	// スート定数
	public static final int NO_TRUMP	= 5;
	public static final int SPADE		= 4;
	public static final int HEART		= 3;
	public static final int DIAMOND		= 2;
	public static final int CLUB		= 1;
	
	// ボードの情報(最低限)
	
	/**
	 * それぞれの座席のハンド枚数を保持します。
	 */
	public int[]		handCount;
	/**
	 * ハンドの内容を保持する。添え字は [seat][0-12]となる。
	 * 0-12は、前詰めで格納する。
	 * value については、2,3,4, ... ,11(=J),12(=Q),13(=K),14(=A) が格納される
	 * カードをあらわす定数は、次の式で計算する<BR><BR>
	 * (suit-1)*14 + (value-2)<BR><BR>
	 * 14を乗じているのは、連続していることの判定を高速に行うためである
	 * 格納は、高速化のため大きい順(降順)に格納する
	 */
	public int[][]		hand;
	
	/** 完了トリック数(=現在プレイ中の添え字) */
	public int			tricks;	// 完了したトリック数(=現在進行中の添え字)
	
	/**
	 * プレイ履歴を保持する。添え字は [0-12][0-3] となる。
	 */
	public int[]		trickCount;
	
	/** 各トリックの内容を格納します。添え字は、[tricks][0-3] となります。 */
	public int[][]		trick;
	
	/** 各トリックの leader を座席定数で格納します。 */
	public int[]		leader;
	
	/** 各トリックの winner を格納します。 */
	public int[]		winner;
	
	/**
	 * トランプスート(Card.SPADE など)を格納します。
	 */
	public int		trump;
	
	/**
	 * すでにプレイされたかどうかのフラグ
	 * 添え字は (suit-1)*14+(value-2) で計算される。
	 */
	public boolean[] isPlayed;
	
	/**
	 * n 番目のプレイのプレイ候補のリストを格納するための領域です。
	 * 添え字は、[n-1][0,1, ... , 14-n] となります
	 */
	public int[][]		playOptions;
	
/*-------------
 * Constructor
 */
	/**
	 * 指定された Board に対応する OptimizedBoard を生成します。
	 * 指定 Board は、PLAYING / OPENING のステータスでなければなりません。
	 *
	 * @exception	IllegalStatusException 指定 Board のステータス異常
	 */
	public OptimizedBoard(Board board) {
		int s = board.getStatus();
		if ( (s != Board.PLAYING)&&(s != Board.OPENING) )
			throw new IllegalStatusException("指定された Board は、OPENING または PLAYING ステータスでなければなりません");
		handCount	= new int[4];
		hand		= new int[4][13];
		
		tricks		= 0;
		trickCount	= new int[13];
		trick		= new int[13][4];
		leader		= new int[13];
		winner		= new int[13];
		
		isPlayed	= new boolean[56];
		for (int i = 0; i < 56; i++)	isPlayed[i] = false;
		
		playOptions	= new int[52][];
		for (int i = 0; i < 52; i++)
			playOptions[i] = new int[13-(i/4)];
		
		trump = board.getTrump();
		
		// ハンド状態のコピー
		for (int seat = 0; seat < 4; seat++) {
			Packet h = board.getHand(seat);
			handCount[seat] = h.size();
			for (int n = 0; n < h.size(); n++) {
				Card c = h.peek(n);
				int value = c.getValue();
				if (value == Card.ACE) value = 14;
				hand[seat][n] = (c.getSuit() - 1)*14+(value-2);
			}
			// ハンド内容のソート
			// １回しか来ないので、手抜き(bubble sort)
			for (int i = 0; i < handCount[seat] - 1; i++) {
				for (int j = i + 1; j < handCount[seat]; j++) {
					if (hand[seat][i] < hand[seat][j]) {
						int tmp = hand[seat][i];
						hand[seat][i] = hand[seat][j];
						hand[seat][j] = tmp;
					}
				}
			}
		}
		
		// トリック状態のコピー
		Trick[] tr = board.getAllTricks();
		for (int i = 0; i < tr.length; i++) {
			if (tr[i] == null) break;
			if (tr[i].size() == 4) tricks++;
			trickCount[i] = tr[i].size();
			for (int j = 0; j < tr[i].size(); j++) {
				Card c = tr[i].peek(j);
				int value = c.getValue();
				if (value == Card.ACE) value = 14;
				int index = (c.getSuit() - 1)*14+(value-2);
				trick[i][j] = index;
				isPlayed[index] = true;
			}
			leader[i] = tr[i].getLeader();
			winner[i] = tr[i].getWinner();
		}
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
	public int[] listOptions() {
		int[] result = listPlayableCards();
		
		// 枝刈り(1)
		// 同格のカードを除外する
		int lastEntried = result[0];
		for (int i = 1; i < result.length; i++) {
			int next = result[i];
			if (next == -1) break;
			// lastEntried と同格かどうか
			// 「同格」とは、２枚のカードのいずれを出しても同一の結果となること
			// lastEntried, next の間のカードがすべてプレイ済みである場合、「同格」
			//
			// (1) lastEntried, next ともに同じハンドに入っている
			// (2) result には降順にカードが含まれている
			// 
			int j;
			for (j = lastEntried - 1; j > next; j--) {
				if (!isPlayed[j]) break;
			}
			lastEntried = next;
			if (j == next) {
				// 同格。つめる。ループがきちんと回るようにする。
				System.arraycopy(result, i+1, result, i, result.length-i-1);
				result[result.length - 1] = -1; // すでに前に -1 が入っているかもしれないが
				i--;
			}
		}
		
		// 枝刈(2)
		// 各スートごとに８以下のカードを同一視する
/*		int lastSuitv = -1;
		boolean eightAppeared = false;
		for (int i = 0; i < result.length; i++) {
			if (result[i] == -1) break;
			if (lastSuitv != (result[i]/14)) {
				lastSuitv = (result[i]/14);
				eightAppeared = false;
			}
			if ((result[i]%14) < 9) {
				if (eightAppeared) {
					// つめる
					System.arraycopy(result, i+1, result, i, result.length-i-1);
					result[result.length - 1] = -1; // すでに前に -1 が入っているかもしれないが
					i--;
				} else {
					eightAppeared = true;
				}
			}
		}
*/		
		return result;
	}
	
	/**
	 * 現在プレイするハンドの中で、指定カードと同格のカードを抽出します。
	 * ベストプレイのリストが出たときに、同格のカードも含めた実際のリストに
	 * 戻すときに使用するための関数です。
	 * 本メソッドは特に高速になるようにはつくられていないため、ループの外側で
	 * 呼ぶ必要があります。
	 *
	 * @param		card	同格のカードを探したいカード
	 * @return		同格のカード
	 */
	public int[] getEqualCards(int[] cards, int cardCount) {
		int		seat	= (leader[tricks]+trickCount[tricks])%4;
		
		// プレイされていないカード = 0
		// 持っているカード         = 1
		// 指定されたカード			= 2
		// プレイされたカード       = 3
		// 0 を delimiter として、token を区切り、2 が含まれている token の
		// 1 を 2 に変更する。2 となっているカードを返却する
		int[] tmp = new int[56];
		
		// プレイされたかどうかでフラグを設定
		for (int i = 0; i < 56; i++) {
			if (isPlayed[i]) tmp[i] = 3;
			else tmp[i] = 0;
		}
		// 持っているカードの設定
		for (int i = 0; i < handCount[seat]; i++) {
			tmp[hand[seat][i]] = 1;
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
	public int[] listPlayableCards() {
		int seat = (leader[tricks]+trickCount[tricks])%4;
		int playOptionsIndex = 4*tricks+trickCount[tricks];
		
		if (trickCount[tricks] != 0) {
			int leadSuit = trick[tricks][0]/14;
			int options = 0;
			
			// スートフォローを探す
			for (int i = 0; i < handCount[seat]; i++) {
				if ((hand[seat][i]/14)==leadSuit) {
					// あった
					playOptions[playOptionsIndex][options++] = hand[seat][i];
				}
			}
			if (options > 0) {
				if (options < 13-tricks) playOptions[playOptionsIndex][options] = -1;
				return playOptions[playOptionsIndex];
			}
		}
		// リードの場合、またはスートフォローできない ... すべてのカードが出せる
		System.arraycopy(hand[seat], 0, playOptions[playOptionsIndex], 0, handCount[seat]);
		// -1 を挿入する必要はない
		
		return playOptions[playOptionsIndex];
	}
	
	/**
	 * １手進めます。スートフォローのチェックは行いません。
	 * 残りプレイ数を返却します。
	 *
	 * @param		c	プレイするカード
	 * @return		残りプレイ数
	 */
	public int play(Card c) {
		int suit	= c.getSuit();
		int value	= c.getValue();
		if (value == Card.ACE) value = 14;
		
		return play( (suit-1)*14+(value-2) );
	}
	
	/**
	 * 指定したカード定数(本クラスで独自定義)をプレイしたとして、
	 * ボードの状態を更新します。トリックが終了したら、ウィナーの設定、次のリーダー
	 * の設定などの内部状態を更新します。残りプレイ数を結果として返却します。
	 * スートフォローチェックは行いません。
	 * 
	 * @param		c	プレイするカード
	 * @exception	ArrayIndexOutOfBoundsException
	 *				プレイ順の人が持っていないカードをプレイしようとした
	 *              もう終了している状態でプレイしようとした
	 */
	public int play(int c) {
		//
		// 1. ハンドから指定カードをdraw
		//    draw せずに、フラグで draw したことを示す方法もあるが、フラグチェックが
		//    増える悪影響があるので、arraycopy による詰めを行うこととしている
		//
		int seat = (leader[tricks] + trickCount[tricks])%4;
		int n;
		
		// draw するカードを検索する
		for (n = 0; n < handCount[seat]; n++) {
			if (c == hand[seat][n]) break;
		}
		
		if (n == handCount[seat])
			throw new ArrayIndexOutOfBoundsException("プレイするカード " + toString(c) + "を含んでいません");
		
		// arraycopy を使ってdraw
		handCount[seat]--;
		System.arraycopy(hand[seat], n+1, hand[seat], n, handCount[seat]-n);
		// 末尾への pad は入れないで高速化する(そのままの値が入ることになる)
		
		//
		// 2. トリックに指定カードを add
		//
		trick[tricks][trickCount[tricks]++] = c;
		
		//
		// 3. isPlayed フラグの更新
		//
		isPlayed[c] = true;
		
		//
		// 4. トリックが終了した場合の処理
		//    winner 次の leader の設定
		//
		if (trickCount[tricks] == 4) {
			// リードが勝っているとする
			int win		= leader[tricks];
			int winCard	= trick[tricks][0];
			int winCardSuitv = (winCard/14)+1;
			
			for (int i = 1; i < 4; i++) {
				int card = trick[tricks][i];
				if (winCardSuitv == trump) {
					// トランプの場合、スートフォローで大きい場合のみ勝てる
					if (trump == (card/14+1) ) {
						// スートフォローだったとき、大小比較で決定する
						if (winCard < card) {
							win = (leader[tricks]+i)%4;
							winCard = card;
							// winCardSuitv は変更なし
						}
					}
				} else {
					// トランプでない場合、スートフォローで大きいか、トランプの場合に勝つ
					if (trump == (card/14+1)) {
						// トランプの場合、無条件に勝つ
						win = (leader[tricks]+i)%4;
						winCard = card;
						winCardSuitv = trump;
					} else if (winCardSuitv == (card/14+1)) {
						// スートフォローの場合
						if (winCard < card) {
							win = (leader[tricks]+i)%4;
							winCard = card;
							// winCardSuitv は変更なし
						}
					}
				}
			}
			winner[tricks] = win;
			tricks++;
			if (tricks == 13) return 0;
			leader[tricks] = win;
		}
		return 52-tricks*4-trickCount[tricks];
	}
	
	/**
	 * １手戻します。
	 */
	public void undo() {
		//
		// 1. 対象トリックを見つける
		//
		if ((tricks == 13)||(trickCount[tricks] == 0)) {
			tricks--;
		}
		
		//
		// 2. トリックから１枚カードを減らす
		//    trickCount を減らすのみ
		//
		int card = trick[tricks][--trickCount[tricks]];
		
		//
		// 3. ハンドにカードを戻す
		//    降順になっているので、適当な位置に挿入する
		//
		int seat = (leader[tricks]+trickCount[tricks])%4;
		int n;
		for (n = 0; n < handCount[seat]; n++) {
			if (card > hand[seat][n]) break;
		}
		System.arraycopy(hand[seat], n, hand[seat], n+1, handCount[seat]-n);
		hand[seat][n] = card;
		handCount[seat]++;
		
		//
		// 4. プレイしたフラグリセット
		//
		isPlayed[card] = false;
		
		//
		// 5. leader, winner などのクリアは行わない
		//    tricks で判断できるため
		//
	}
	
	/**
	 * 現在まで終了しているトリックについて、ＮＳ側の取ったトリック数をカウントします。
	 *
	 * @return		ＮＳ側のトリック数
	 */
	public int countNSWinners() {
		int result = 0;
		for (int i = 0; i < tricks; i++) {
			if ((winner[i]%2) == 0) result++;
		}
		return result;
	}
	
	/**
	 * のこり４枚になったときにプレイさせずに NS の winner 数をカウントします。
	 * 先読みルーチンを高速化する場合に使用できます。
	 * 残り４枚でなくても本メソッドはコールできますが、結果は不定です。
	 *
	 * @return		プレイを完了させたときのＮＳ側のトリック数
	 */
	public int countNSWinnersLeavingLastTrick() {
		int result = 0;
		for (int i = 0; i < tricks; i++) {
			if ((winner[i]%2) == 0) result++;
		}
		
		// 最後のwinnerをそれぞれのハンドを見ることで見る
		// リードが勝っているとする
		int win		= leader[tricks];
		int winCard	= hand[win][0];
		int winCardSuitv = (winCard/14)+1;
		
		for (int i = 1; i < 4; i++) {
			int c = hand[(leader[tricks]+i)%4][0];
			if (winCardSuitv == trump) {
				// トランプの場合、スートフォローで大きい場合のみ勝てる
				if (trump == (c/14+1) ) {
					// スートフォローだったとき、大小比較で決定する
					if (winCard < c) {
						win = (leader[tricks]+i)%4;
						winCard = c;
						// winCardSuitv は変更なし
					}
				}
			} else {
				// トランプでない場合、スートフォローで大きいか、トランプの場合に勝つ
				if (trump == (c/14+1)) {
					// トランプの場合、無条件に勝つ
					win = (leader[tricks]+i)%4;
					winCard = c;
					winCardSuitv = trump;
				} else if (winCardSuitv == (c/14+1)) {
					// スートフォローの場合
					if (winCard < c) {
						win = (leader[tricks]+i)%4;
						winCard = c;
						// winCardSuitv は変更なし
					}
				}
			}
		}
		if ((win%2) == 0) result++;
		
		return result;
	}
	
	/**
	 * 現在のトリックのリーダーを座席定数で取得します。
	 * 終わっている Board に対して本メソッドをコールすると、
	 * ArrayIndexOutOfBoundsException がスローされます。
	 *
	 * @return		現在のトリックのリーダー
	 */
	public int getLeader() {
		return leader[tricks];
	}
	
	/**
	 * 現在までで完了しているトリック数を取得します。
	 *
	 * @return		トリック数
	 */
	public int getTricks() {
		return tricks;
	}
	
	
	/**
	 * 現在誰の番になっているかを座席定数で取得します。
	 */
	public int getTurn() {
		return (leader[tricks]+trickCount[tricks])%4;
	}
	
	public static String toString(int card) {
		return SUIT[card/14]+VALUE[card%14];
	}
}

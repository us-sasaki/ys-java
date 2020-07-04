/**
 * 全数探索を行うことで得られる Board に関する情報
 */
class BoardStatistics {
	/** @type {number} */ bestPlayCount;
	/** @type {number} */ bestPlayPaths;
	/** @type {number} */ totalPlayCount;
	/** @type {number} */ finalNSTricks;
    
    /**
     * 文字列表現を得ます。
     * @returns {string} 文字列表現
     */
	toString() {
		let result = 	"bestPlays=" + this.bestPlayCount;
		result = result +	", bestPaths=" + this.bestPlayPaths;
		result = result +	", totalPlayCount=" + this.totalPlayCount;
		result = result +	", finalNSTricks=" + this.finalNSTricks;
		
		return result;
	}
}

/**
 * 先読みに特化した Board 的なオブジェクト
 * 内部的にプレイの状態は、カードの状態が変化することで表現する。
 * int card[56] がその状態を示す変数である。
 */
class OptimizedBoard {
	static SUIT_STR = "CDHS*"; // Array
	static VALUE_STR = "23456789TJQKA"; // Array
	static SEAT_STR = "NESW456789abcdef"; // Array
	
	static TRICK_MULTIPLICITY = 100;
	
	/**
	 * カードの状態を示す。
	 * 下位４ビットは、所有者(North=0, East=1, South=2, West=3)をあらわし、
	 * それを除いた上位ビットはプレイ番号(1-52, 0は未プレイ)を示す。
	 * なお、実際にあらわすカードのない添え字(14, 28, 42, 56)のカードには 15 が格納される。
	 * 添え字はカード定数であり、バリューとスートをあらわす１つの値である。
	 * バリューは、2を0に、Aを12に対応させ、クラブを0, スペードを3に対応させる。<br>
	 * (カード定数)＝(バリュー)＋(スート)×14 で求める。
     * @type {number[]}
	 */
	card;
	
	/** @type {number[]} プレイ番号(1..52)からカード番号を得る、逆引き用インデックス。 */
	play;
	
	/** @type {number[]} winner(次のleader)の座席を格納する。添え字は、0-13 となる。 */
	leader;
	
	/**
	 * これまでのプレイカウント
	 * play() を呼ぶとカウントアップする。
	 * Opening Lead 状態では 0 で、Opening Lead をプレイすると 1 になる。
	 * Scoring 状態では 52 となっている。
     * @type {number}
	 */
	count;
	
	/** @type {number} トランプスート(クラブ=0、スペード=3) */
	trump;
	
	/** @type {number} NS側のとったトリック数 */
	nsWins;
	
	/**
	 * new させないために depth ごとに使用する BoardStatistics のバッファ
     * @type {BoardStatistics[]}
	 */
	statBuffer;
	
	/** @type {number[]} 最後の１トリック算出用の高速化バッファです */
	lastPlayBuffer;
	
	/**
	 * 概算モードに移る depth の指定です。
	 * この depth を超えた最初のリード状態で概算を行います。
     * @type {number}
	 */
	depthBorder;
    
    /** @type {number[]} */
    bestPlay;
    
	//
	// 以降、概算アルゴリズム用
	//
	static SEAT = 4;
	static SUIT = 4;
	static NS_OR_EW = 2;
	static CARDS = 56;
	
	/** @type {number[][]} [SEAT][SUIT] 枚数が格納される */
	suitCount;
	/** @type {number[][]} [NS_OR_EW][SUIT] ns, ew の Winner の数 */
	totalWinners;
	/** @type {number[][]} [NS_OR_EW][SUIT] */
	longerLength;
	/** @type {number[][]} [NS_OR_EW][SUIT] */
	shorterLength;
	/** @type {number[][]} [SEAT][SUIT] */
	lowestCard;
	/** @type {number[][]} [SEAT][SUIT] */
	highestCard;
	/** @type {number[][]} [NS_OR_EW][SUIT] */
	lowestCardOfShorterSuit;
	/** @type {number[][]} [NS_OR_EW][SUIT] */
	highestCardOfLongerSuit;
	
	/** @type {boolean[]} [CARDS] */
	isWinner;
	/** @type {number} calcPropData	 */
	limitTricks;

/*-------------
 * Constructor
 */
	/**
	 * @param {Board} board コピー元の Board
	 */
	constructor(board) {
		const s = board.status;
		if ( (s != Board.PLAYING)&&(s != Board.OPENING) )
			throw new Error("指定された Board は、OPENING または PLAYING ステータスでなければなりません");
		this.card	= new Array(56);
		for (let i = 13; i < 56; i+=14) this.card[i] = 15;

		this.play = new Array(53);
		this.leader = new Array(14);
		this.count = 0;
		this.nsWins	= 0;
		this.statBuffer	= new Array(52); //BoardStatistics[52];
		for (let i = 0; i < 52; i++) this.statBuffer[i] = new BoardStatistics();
		this.lastPlayBuffer = new Array(4);
		
		this.depthBorder = 4;		// 概算は使用しない
		
		this.trump = board.getTrump() - 1; // (==4 when NoTrump)
		this.suitCount = OptimizedBoard.newArray(OptimizedBoard.SEAT, OptimizedBoard.SUIT);
		this.totalWinners = OptimizedBoard.newArray(OptimizedBoard.NS_OR_EW, OptimizedBoard.SUIT);
		this.longerLength = OptimizedBoard.newArray(OptimizedBoard.NS_OR_EW, OptimizedBoard.SUIT);
		this.shorterLength = OptimizedBoard.newArray(OptimizedBoard.NS_OR_EW, OptimizedBoard.SUIT);
		this.lowestCard = OptimizedBoard.newArray(OptimizedBoard.SEAT, OptimizedBoard.SUIT);
		this.highestCard = OptimizedBoard.newArray(OptimizedBoard.SEAT, OptimizedBoard.SUIT);
		this.lowestCardOfShorterSuit = OptimizedBoard.newArray(OptimizedBoard.NS_OR_EW, OptimizedBoard.SUIT);
		this.highestCardOfLongerSuit = OptimizedBoard.newArray(OptimizedBoard.NS_OR_EW, OptimizedBoard.SUIT);
		this.isWinner = new Array(OptimizedBoard.CARDS).fill(false);
		
		// ハンド状態のコピー
		for (let seat = 0; seat < 4; seat++) {
			const h = board.getHand(seat);
			for (let n = 0; n < h.children.length; n++) {
				this.card[OptimizedBoard.getCardNumber(h.children[n])] = seat;
			}
		}
		
		// トリック状態のコピー
		const tr = board.getAllTricks();
		this.nsWins = 0;
		for (let i = 0; i < tr.length; i++) {
			if (tr[i] == null) break;
			for (let j = 0; j < tr[i].children.length; j++) {
				const c = tr[i].children[j];
				let value = c.value;
				if (value === Card.ACE) value = 14;
				const index = (c.suit - 1)*14+(value-2);
				this.card[index] = (i*4 + j + 1) << 4;
				this.card[index] += (tr[i].leader + j) % 4;
				this.play[i * 4 + j + 1] = index;
				this.count++;
			}
			this.leader[i] = tr[i].leader;
			if (!tr[i].isFinished()) break;
			this.leader[i+1] = tr[i].getWinner();
			if ((this.leader[i + 1] & 1) === 0) this.nsWins++;
		}
		
		this.bestPlay = new Array(14);
	}

	static newArray(x, y) {
		const a = [];
		for (let i = 0; i < x; i++) a[i] = new Array(y).fill(0);
		return a;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 実際には先読みルーチンの中でインラインに書くことになるだろうメソッド
	 *
	 * @param {number} playedCard プレイされたカード定数
	 * @return {number} 今のプレイのプレイカウント(1..52) 52が最後のプレイとなる
	 */
	draw(playedCard) {
		this.count++;
		this.card[playedCard] += (this.count << 4);
		this.play[this.count] = playedCard;
		
		if ( (this.count%4) != 0 ) return this.count;
		
		// １トリック完了
		
		// ウィナーの決定、leader に設定する
		const winner = this.leader[(this.count>>2) - 1];
		let winCard = this.play[this.count - 3];
		
		for (let i = -2; i <= 0; i++) {
			const card = this.play[this.count + i];
			if ( (winCard/14|0) === this.trump ) {
				if ( (card/14|0) === this.trump ) {
					if (card > winCard) {
						// winCard が trump の場合は、大きいトランプを出さなければ勝たない
						winCard	= card;
						winner	= (this.leader[(this.count>>2) - 1] + i + 3) % 4;
					}
				}
			} else if ( (card/14|0) === this.trump ) {
				// はじめて出た trump は必ず勝つ
				winCard	= card;
				winner	= (this.leader[this.count/4 - 1] + i + 3) % 4;
			} else if ( (winCard/14|0) === (card/14|0) ) {
				if (card > winCard) {
					// スートフォローの場合、大きいバリューなら勝つ
					winCard	= card;
					winner	= (this.leader[(this.count>>2) - 1] + i + 3) % 4;
				}
			}
		}
		
		// 次の leader を設定する。
		this.leader[this.count/4]	= winner;
		
		if ((winner & 1) == 0) this.nsWins++;
		
		return this.count;
	}
	
	/**
	 * play() を呼ぶ前の状態に戻します。
	 * leader[] の値のリセットはとりあえず行っていません。
	 * つまり、現在以降のleader[] の値は不定です。
	 * また、play[] についてもリセットしていません。
	 */
	undo() {
		if ( (this.count%4) === 0 ) {
			if ((this.leader[this.count>>2] & 1) == 0) this.nsWins--;
		}
		this.card[this.play[this.count]] &= 0x0F;
		this.count--;
		// leader のリセットは行わない。(高速化)
	}
	
	/**
	 * 何手先読みを行うかを設定する。動的に変更可能。
	 * ０を設定すると、はじめのリード状態まで先読みを行う。
	 * １を設定すると、１手読み、つづくはじめのリード状態まで先読みを行う。
	 * 48以上を設定すると、最後まで読みきる。
	 * @param {number} depthBorder 先読みの手数
	 */
	setDepthBorder(depthBorder) {
		this.depthBorder = depthBorder;
	}
	
/*------------
 * 先読み本体
 */
	/**
	 * 次の一手。先読みルーチンとほぼ同じだが、最善手の候補を記録する点が異なる
	 * @returns {number[]} ベストプレイの配列
	 */
	getBestPlay() {
		// プレイ候補を探すループ
		const turn = (this.leader[this.count>>2] + this.count) % 4;
		const nsside = ( (turn & 1) === 0 );
		
		//
		// 再帰的処理
		//
		let countAtLead		= (count>>2)*4+1;
		
		// スートフォローできるかどうかの判定
		// 現在、スートフォローできるかどうかの検索と実際にプレイするループの
		// ２つをまわしているが、インライン展開することで１つにできる
		let	startIndex	= 0;
		let	endIndex	= 55;
		if ( (this.count % 4) !== 0 ) {
			const suit	= this.play[countAtLead] / 14 |0;
			const suit2	= suit * 14;
			for (let c = suit2; c < suit2 + 13; c++) {
				if (this.card[c] === turn) {
					// スートフォローできる
					startIndex = c;
					endIndex = suit2 + 13;
					break;
				}
			}
		}
		
		let lastEntried	= false; // 同格カードを除くための変数
		let bestPlayCount =  0;
		let bestTricks = - OptimizedBoard.TRICK_MULTIPLICITY;
		
		let countAtLead2	= countAtLead << 4;
		
		// リードの場合、またはスートフォローできない場合(なんでも出せる)
		// ボードが終わりに近づくにつれて無駄が多くなる。....低速化
		for (let c = startIndex; c < endIndex; c++) {
			const tmp = this.card[c];
			if ((tmp > 15)&&(tmp < countAtLead2)) continue; // プレイされたカードは無視
			
			if (tmp === turn) { // 今場に出ているカードはまだプレイされていないと考える
				if (lastEntried) continue;
				lastEntried = true;
				// 持っていて、プレイされていない ... c を出せる
				this.draw(c);
				
				const stats = calculateImpl(0, 14 * OptimizedBoard.TRICK_MULTIPLICITY, true);
console.log(this.getCardString(c) + " のボード統計情報");
console.log(stats.toString());
				
				// best play かどうかの判定、bestPlayCount, bestTricks の更新
				let finalTricks;
				if (nsside) finalTricks = stats.finalNSTricks;
				else finalTricks = 13 * OptimizedBoard.TRICK_MULTIPLICITY - stats.finalNSTricks;
				
				if (bestTricks < finalTricks) {
					bestTricks		= finalTricks;
					bestPlayCount	= 1;
					this.bestPlay[0] = c;
				} else if (bestTricks === finalTricks) {
					this.bestPlay[bestPlayCount++] = c;
				}
				
				this.undo();
			} else {
				lastEntried = false; // 他の人が持っている or デリミタ... シーケンスが切れた
			}
			// 抜けるのは、今プレイ中のカードのみ
		}
		this.bestPlay[bestPlayCount] = -1;
		return this.bestPlay;
	}
	
/*
 * 先読み(二手目以降)
 */
	/**
	 * @returns	{BoardStatistics} 
	 */
	calculate() {
		// 第二パラメータが 14 のとき、第三パラメータは不問
		return this.calculateImpl(0, 14 * OptimizedBoard.TRICK_MULTIPLICITY, true);
	}
	
	/**
	 * @param {number} depth 探索の現在の深さ
	 * @param {number} border α-β枝刈のための閾値
	 * @param {number} borderNsside NS側の番か
	 * @returns {BoardStatistics}
	 */
	calculateImpl(depth, border, borderNsside) {
		// 結果オブジェクト。使いまわすことで、高速化。
		const result = this.statBuffer[depth];
		result.totalPlayCount = 0;
		
		// プレイ候補を探すループ
		const turn = (this.leader[this.count>>2] + this.count) % 4;
		
		//
		const nsside = ( (turn & 1) === 0 );
		
		//
		// 帰納法のはじめ
		//
		if (this.count === 52) {
			// 最終トリックだった場合の返却
			result.totalPlayCount = 1;
			result.bestPlayCount = 1;
			result.bestPlayPaths = 1;
			result.finalNSTricks = nsWins * OptimizedBoard.TRICK_MULTIPLICITY;
			
			return result;
		} else if (count === 48) {
			// 残り１トリックだった場合の返却
			result.totalPlayCount	= 1;
			result.bestPlayCount	= 1;
			result.bestPlayPaths	= 1;
			result.finalNSTricks	= nsWins * OptimizedBoard.TRICK_MULTIPLICITY;
			
			// nsWins を求める
			
			// 全員分最後の１枚が何か調べ、lastPlayBuffer に格納する
			for (let i = 0; i < 55; i++) {
				if (this.card[i] < 4) {
					this.lastPlayBuffer[this.card[i]] = i;
				}
			}
			// ウィナーの決定、leader に設定する
			let winner = this.leader[count>>2];
			const leaderSeat = winner;
			let winCard = this.lastPlayBuffer[leaderSeat];
			
			for (let i = 1; i < 4; i++) {
				const card = this.lastPlayBuffer[(leaderSeat + i) % 4];
				if ( (winCard/14 |0) === this.trump ) {
					if ( (card/14 |0) === this.trump ) {
						if (card > winCard) {
							// winCard が trump の場合は、大きいトランプを出さなければ勝たない
							winCard	= card;
							winner	= (leaderSeat + i) % 4;
						}
					}
				} else if ( (card/14 |0) === trump ) {
					// はじめて出た trump は必ず勝つ
					winCard	= card;
					winner	= (leaderSeat + i) % 4;
				} else if ( (winCard / 14 |0) === (card / 14 |0) ) {
					if (card > winCard) {
						// スートフォローの場合、大きいバリューなら勝つ
						winCard	= card;
						winner	= (leaderSeat + i) % 4;
					}
				}
			}
			
			if ((winner & 1) === 0) result.finalNSTricks += OptimizedBoard.TRICK_MULTIPLICITY;
			
			return result;
		}
		
		//
		// depthBorder を超えているか？
		//
		if ((depth >= this.depthBorder)&&( (count%4) === 0 )) {
			// ここで概算を行う
			let tricks = this.calcApproximateTricks(); // 現在のリーダーがとれるトリック数
			if ( (this.leader[this.count>>2] % 2) == 0 ) {
				// リーダーは NS
				tricks += this.nsWins * OptimizedBoard.TRICK_MULTIPLICITY;
			} else {
				// リーダーは EW
				tricks = (this.nsWins + 13 - (this.count>>2)) * OptimizedBoard.TRICK_MULTIPLICITY - tricks;
			}
			
			// 最終トリックだった場合の返却
			result.totalPlayCount = 10;
			result.bestPlayCount = 1;
			result.bestPlayPaths = 1;
			result.finalNSTricks = tricks;
			
			return result;
		}
		
		//
		// 再帰的処理
		//
		const countAtLead		= (count>>2)*4+1;
		
		// スートフォローできるかどうかの判定
		// 現在、スートフォローできるかどうかの検索と実際にプレイするループの
		// ２つをまわしているが、インライン展開することで１つにできる
		let	startIndex = 0;
		let	endIndex = 55;
		if ( (count % 4) != 0 ) {
			const suit	= this.play[countAtLead]/14 |0;
			const suit2	= suit * 14;
			for (let c = suit2; c < suit2 + 13; c++) {
				if (this.card[c] === turn) {
					// スートフォローできる
					startIndex	= c;
					endIndex	= suit2 + 13;
					break;
				}
			}
		}
		
		let lastEntried	= false; // 同格カードを除くための変数
		let bestPlayCount	=  0;
		let bestPlayPath	=  0;
		let bestTricks		= -OptimizedBoard.TRICK_MULTIPLICITY;
		
		const countAtLead2	= countAtLead << 4;
		
		// リードの場合、またはスートフォローできない場合(なんでも出せる)
		// ボードが終わりに近づくにつれて無駄が多くなる。....低速化
		for (let c = startIndex; c < endIndex; c++) {
			const tmp = this.card[c];
			if ((tmp > 15)&&(tmp < countAtLead2)) continue; // プレイされたカードは無視
			
			if (tmp === turn) { // 今場に出ているカードはまだプレイされていないと考える
				if (lastEntried) continue;
				lastEntried = true;
				// 持っていて、プレイされていない ... c を出せる
				this.draw(c);
				
				const stats = this.calculateImpl(depth+1, 13 * OptimizedBoard.TRICK_MULTIPLICITY - bestTricks, nsside);
				
				// best play かどうかの判定、bestPlayCount, bestTricks の更新
				let finalTricks;
				if (nsside) finalTricks = stats.finalNSTricks;
				else finalTricks = 13 * OptimizedBoard.TRICK_MULTIPLICITY - stats.finalNSTricks;
				if (bestTricks < finalTricks) {
					bestTricks		= finalTricks;
					bestPlayCount	= 1;
					bestPlayPath	= stats.bestPlayPaths;
					
					// α-β枝刈
					// これは、前回の nsside(borderNsside)と今回の nsside が違っている
					// 場合にしか適用できない。トリックの切れ目などで side が同じ場合
					// α-βは適用不可、ということ
					if ((nsside !== borderNsside)&&(bestTricks > border)) {
						result.totalPlayCount += stats.totalPlayCount;
						undo();
						break;
					}
				} else if (bestTricks === finalTricks) {
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
		result.bestPlayCount = bestPlayCount;
		result.bestPlayPaths = bestPlayPath;
		
		if (nsside) result.finalNSTricks	= bestTricks;
		else result.finalNSTricks	= 13 * OptimizedBoard.TRICK_MULTIPLICITY - bestTricks;
		
		return result;
	}
	
	/**
	 * 和美による Board 概算アルゴリズム
	 * @returns {number} 概算トリック数
	 */
	calcApproximateTricks() {
		const seat = (this.leader[this.count>>2] + this.count) % 4;
		calcPropData();
		
		const leaderTricks	= this.calcX(seat);
		
		// オポーネントから見た leader のトリック数(残りトリック数 - oppのクイックトリック)
		const opponentTricks = this.limitTricks - this.calcMaxX(1 - (seat & 1));
		
		if (leaderTricks > opponentTricks)
			return leaderTricks * OptimizedBoard.TRICK_MULTIPLICITY;
		
		return (leaderTricks + opponentTricks) * OptimizedBoard.TRICK_MULTIPLICITY >> 1;
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
	calcPropData() {
		//
		// suitCount を求める
		// highest card, lowest card は何か？
		//
		for (let i = 0; i < OptimizedBoard.SEAT; i++) {
			for (let j = 0; j < OptimizedBoard.SUIT; j++) {
				this.suitCount[i][j] = 0;
				this.lowestCard[i][j] = 0;
			}
		}
		
		for (let i = 0; i < 55; i++) {
			if (this.card[i] < 4) {
				const tmp = i / 14 |0;
				const tmp2 = this.card[i];
				if (this.lowestCard[tmp2][tmp] === 0) this.lowestCard[tmp2][tmp] = i;
				this.suitCount[tmp2][tmp]++;
				this.highestCard[tmp2][tmp] = i;
			}
		}
		
		//
		// winnerの数を数える
		// この処理は、毎回やるよりも、play で更新した方が速いのでは？
		//
		for (let i = 0; i < 2; i++) {
			for (let j = 0; j < 4; j++) {
				this.totalWinners[i][j] = 0;
			}
		}
		
		let NSorEW = -1;
		
		for (let i = 54; i >= 0; i--) {
			this.isWinner[i] = false;
			// リードされたカードは除外（現在リード状態のため、場にあるカードはない）
			if (this.card[i] > 15) continue;
			if (this.card[i] == 15) { // デリミタ
				NSorEW = -1;
				continue;
			}
			if (NSorEW === -1) NSorEW = (this.card[i] & 1);
			if ( (this.card[i] & 1) === NSorEW ) {
				this.totalWinners[NSorEW][i/14 |0]++;
				this.isWinner[i] = true;
			} else {
				// winner シーケンスが切れた
				// skip する isWinner[] を false にはしないでよい
				// それは、１ボードで、あるカードについて winner だったものが
				// winner でなくなることはプレイされたとき以外にはない
				const tmp = (i/14 |0)*14;
				for (i = i - 1; i > tmp; i--) this.isWinner[i] = false;
				NSorEW = -1;
			}
		}
		
		//
		// assert
		//
//		for (let suit = 0; suit < 4; suit++) {
//			let totalWinner = 0;
//			for (let i = suit*14; i < suit*14+14; i++) {
//				if (this.isWinner[i]) totalWinner++;
//			}
//			if (totalWinner != (this.totalWinners[0][suit]+this.totalWinners[1][suit]))
//				console.log("asserted winner Count " + suit + "totalWinner " + totalWinner + " totalWinners[][] " + (this.totalWinners[0][suit]+this.totalWinners[1][suit]));
//		}
		
		//
		// longer, shorter を考える
		//
		for (let suit = 0; suit < 4; suit++) {
			// NS で考える
			if (this.suitCount[0][suit] > this.suitCount[2][suit]) {
				this.highestCardOfLongerSuit[0][suit] = this.highestCard[0][suit];
				this.longerLength[0][suit]	= this.suitCount[0][suit];
				this.shorterLength[0][suit]	= this.suitCount[2][suit];
				this.lowestCardOfShorterSuit[0][suit] = this.lowestCard[2][suit];
			} else {
				this.highestCardOfLongerSuit[0][suit] = this.highestCard[2][suit];
				this.longerLength[0][suit] = this.suitCount[2][suit];
				this.shorterLength[0][suit]	= this.suitCount[0][suit];
				this.lowestCardOfShorterSuit[0][suit] = this.lowestCard[0][suit];
			}
			
			// EW で考える
			if (this.suitCount[1][suit] > this.suitCount[3][suit]) {
				this.highestCardOfLongerSuit[1][suit] = this.highestCard[1][suit];
				this.longerLength[1][suit] = this.suitCount[1][suit];
				this.shorterLength[1][suit]	= this.suitCount[3][suit];
				this.lowestCardOfShorterSuit[1][suit] = this.lowestCard[3][suit];
			} else {
				this.highestCardOfLongerSuit[1][suit] = this.highestCard[3][suit];
				this.longerLength[1][suit] = this.suitCount[3][suit];
				this.shorterLength[1][suit]	= this.suitCount[1][suit];
				this.lowestCardOfShorterSuit[1][suit] = this.lowestCard[1][suit];
			}
		}
		
		this.limitTricks = 13 - (this.count>>2);
	}
	
	/**
	 * 指定された座席での(準)クイックトリック数Ｘを求めます。
	 * @param {number} seat 座席定数
	 * @returns {number} 概算クイックトリック数
	 */
	calcX(seat) {
		const NSorEW = (seat & 1);
		let result = 0;
		
		for (let suit = 0; suit < 4; suit++) {
			if (this.suitCount[seat][suit] > 0) result += this.calcXs(NSorEW, suit);
			else {
				if (suit === this.trump) {
					// 2003/5/31 追加
					// トランプスートだけは、自分がボイドでもパートナートリックは確実
					result += this.totalWinners[NSorEW][suit];
				}
			}
		}
		if (result > this.limitTricks) return this.limitTricks;
		return result;
	}
	
	/**
	 * Max(Te, Tw)
	 * @param {number} NSorEW 0 or 1
	 * @returns {number} 結果
	 */
	calcMaxX(NSorEW) {
		const opp2 = NSorEW + 2; // opp1 = NSorEW
		let result1 = 0;
		let result2 = 0;
		
		for (let suit = 0; suit < 4; suit++) {
			const r = this.calcXs(NSorEW, suit);
			if (this.suitCount[NSorEW][suit] > 0) result1 += r;
			if (this.suitCount[ opp2 ][suit] > 0) result2 += r;
		}
		
		if (result1 > result2) {
			if (result1 > this.limitTricks) return this.limitTricks;
			return result1;
		}
		if (result2 > this.limitTricks) return this.limitTricks;
		return result2;
	}
	
	/**
	 * 指定された NS/EW とスートに関する(準)クイックトリック数 Xs を求めます。
	 * @param {number} NSorEW 0 or 1
	 * @param {number} suit スーツ
	 * @returns {number} 結果
	 */
	calcXs(NSorEW, suit) {
		let xs;
		// (A) ①完全にブロックしている場合
		if ( (this.shorterLength[NSorEW][suit] > 0)
				&&(this.isWinner[lowestCardOfShorterSuit[NSorEW][suit]])
				&&( (this.lowestCardOfShorterSuit[NSorEW][suit] % 14)
						> (this.highestCardOfLongerSuit[NSorEW][suit] % 14) ) ) {
			if (suit === this.trump) {
				// トランプスートの場合、
				// xs = min(totalWinners, longerLength)
				if (this.totalWinners[NSorEW][suit] > this.longerLength[NSorEW][suit]) {
					xs = this.longerLength[NSorEW][suit];
				} else {
					xs = this.totalWinners[NSorEW][suit];
				}
			} else {
				// サイドスートの場合
				xs = this.shorterLength[NSorEW][suit];
			}
		} else {
			// (A) ②完全にはブロックしていない場合
			if (this.shorterLength[NSorEW][suit] === 0) {
				xs = this.totalWinners[NSorEW][suit];
			} else {
				if (this.isWinner[this.lowestCardOfShorterSuit[NSorEW][suit]]) {
					// lowestCardOfShorterSuit が winner
					// オーバーテイクする
					if (this.totalWinners[NSorEW][suit]-1 > this.longerLength[NSorEW][suit]) {
						xs = this.longerLength[NSorEW][suit];
					} else {
						xs = this.totalWinners[NSorEW][suit]-1;
					}
				} else {
					// lowestCardOfShorterSuit が winner でない
					if (this.totalWinners[NSorEW][suit] > this.longerLength[NSorEW][suit]) {
						xs = this.longerLength[NSorEW][suit];
					} else {
						xs = this.totalWinners[NSorEW][suit];
					}
				}
			}
			// (B) エスタブリッシュによる昇格分の修正
			//   (A) ②の３つの場合について...
			if (suit !== this.trump) {
				const opp1 = NSorEW + 1;
				const opp2 = (NSorEW + 3)%4;
				if ( (xs >= this.suitCount[opp1][suit]) && (xs >= this.suitCount[opp2][suit]) ) {
					xs = this.longerLength[NSorEW][suit];
				}
			}
		}
		
		// (C) さらにこの後で、オポーネントにラフされる分を考慮からはずす
		if (suit !== this.trump) {
			if (this.trump < 4) {
				const opp1 = (NSorEW + 1) % 4;
				const opp2 = (NSorEW + 3) % 4;
				
				if ( (this.suitCount[opp1][this.trump] > 0)&&(xs > this.suitCount[opp1][suit])) {
					xs = this.suitCount[opp1][suit];
				}
				if ( (this.suitCount[opp2][this.trump] > 0)&&(xs > this.suitCount[opp2][suit])) {
					xs = this.suitCount[opp2][suit];
				}
			}
		} else {
			// (D) s が切り札スートのとき、スモールカードによるトリックを考慮し、修正
			const opp1 = (NSorEW + 1) % 4;
			const opp2 = (NSorEW + 3) % 4;
			const adj1 = Math.max(0, this.suitCount[opp1][suit] - xs);
			const adj2 = Math.max(0, this.suitCount[opp2][suit] - xs);
			
			xs = Math.max(xs, this.longerLength[NSorEW][suit] - adj1 - adj2);
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
	 * @param {number} card カード定数
	 * @returns {string} C5 のようなカード文字列
	 */
	static getCardString(card) {
		return OptimizedBoard.SUIT_STR[card/14 |0]+OptimizedBoard.VALUE_STR[card%14];
	}
	
	/**
	 * カードオブジェクトからカード定数を求めます。
	 * @param {Card} card Card
	 * @returns {number} カード定数
	 */
	static getCardNumber(card) {
		let value = card.value;
		if (value === Card.ACE) value = 14;
		return (card.suit - 1)*14+(value-2);
	}
	
	/**
	 * デバッグ用の文字列に変換します。
	 * @returns {string} 文字列表現
	 */
	toString() {
		let s = "●変数内容表示●\n";
		s += "count     ："+this.count+'\n';
		s += "nsWins    ："+this.nsWins+'\n';
		s += "trump     ："+this.trump+'\n';
		s += "カード状態：";
		for (let i = 0; i < this.card.length; i++) {
			s += (OptimizedBoard.getCardString(i));
			s += ':'+(this.card[i]>>4)+'/';
			s += OptimizedBoard.SEAT_STR[this.card[i]%16]+' ';
		}
		s += '\n';
		s += "プレイされたカード：";
		for (let i = 1; i <= this.count; i++) {
			s += i +':'+ OptimizedBoard.getCardString(this.play[i]);
			if ( (this.card[this.play[i]]>>4) !== i ) s += "論理矛盾";
			s += ' ';
		}
		s += '\n';
		s += "leader：";
		for (let i = 0; i <= (this.count>>2); i++) {
			s += i;
			s += OptimizedBoard.SEAT_STR[this.leader[i]];
			s += ' ';
		}
		s += '\n';
		s += "●ボード状態表示●\n";
		s += "ハンド情報：\n";
		
		// NORTH
		for (let suit = 3; suit >= 0; suit--) {
			s += "               ";
			s += this.getHandString(0, suit);
			s += '\n';
		}
		
		// WEST, EAST
		for (let suit = 3; suit >= 0; suit--) {
			let wstr = this.getHandString(3, suit) + "               ";
			wstr = wstr.substring(0, 15);
			s += wstr;
			s += "               ";
			s += this.getHandString(1, suit);
			s += '\n';
		}
		// SOUTH
		for (let suit = 3; suit >= 0; suit--) {
			s += "               ";
			s += this.getHandString(2, suit);
			s += '\n';
		}
		
		return s;
	}
	
	/**
	 * 指定された座席、スーツのハンド文字列を返却します。
	 * @param {number} seat 座席番号
	 * @param {number} suit スーツ
	 */
	getHandString(seat, suit) {
		let s = "CDHS".substring(suit, suit+1);
		s += ':';
		for (let i = this.card.length-1; i >= 0; i--) {
			if (this.card[i] !== seat) continue;
			if ((i/14 |0) != suit) continue;
			s += OptimizedBoard.getCardString(i).substring(1);
		}
		return s;
	}
}

/**
 * ダブルダミー状態で、最後まで読みきって最善手を打つプレイヤー。
 * 2015/8/12 コンピュータの性能アップに伴い、先読み深化
 */
class ReadAheadPlayer extends Player {
	/** @type {SimplePlayer2} 思考ルーチン */
	base;
	/** @type {boolean} オープニングリード指定があるか */
	openingLeadSpecified;

	/** @type {number[][]} もともとbyte[5000][3] だが利用されていないようだ */
	paths;
	
	/**
	 * 
	 * @param {Board} board プレイ対象の Board
	 * @param {number} seat 座席番号
	 * @param {?string} ol オープニングリード指定(あれば)
	 */
	constructor(board, seat, ol) {
		super();
		this.setBoard(board);
		this.setMySeat(seat);
		
		base = new SimplePlayer2(board, seat, ol);
		if (ol) this.openingLeadSpecified = true;
	}
	
/*------------
 * implements
 */
	/**
	 * パスします。
	 * @async
	 * @return {Promise<Bid>} パス
	 */
	async bid() {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * OptimizedBoard の最善手探索アルゴリズムを使用したプレイを行います。
	 * オープニングリードについては指定がある場合、指定に従います。
	 * @async
	 * @returns {Promise<Card>} 最善手
	 */
	async draw() {
		// プレイの間隔をなるべく一定にするため
		long t0 = System.currentTimeMillis();
		
		Board board = getBoard();
		
		if (board.getStatus() == Board.OPENING) {
			// オープニングリードの場合は SimplePlayer2 のアルゴリズムを使う
			if (openingLeadSpecified) return base.draw();
		}
		
		// 最善手プレイの集合を取得します。
		Packet playOptions = getPlayOptions();
		
		Card play = choosePlay(playOptions);
		
		// プレイ間隔一定のため
		long t = System.currentTimeMillis();
		try { if ((t - t0) < 700) Thread.sleep(700 - (t - t0)); // 700msec になるまで考えるふり
		} catch (InterruptedException ignored) { }
		
		return play;
	}
	
	/**
	 * OptimizedBoard を使用して、最善手の候補を見つけます。
	 * 本クラス内の Optimized 関連部分となります。
	 *
	 * @return		最善手の候補
	 */
	protected Packet getPlayOptions() {
		OptimizedBoard b = new OptimizedBoard(getBoard());
		//
		// トリック数による先読みの深さ変更
		//
		
		// depthBorder 値を指定
		// depthBorder は、最低先読みプレイ数で、実際にはこれ以降の最初のリード状態
		// まで先読みが行われます。例えば０を指定し、すでにリード状態にあった場合、
		// 先読みは行われません。
		//                        
		int[] depth = new int[] {   8,   8,   8,   8,
								    8,   8,   9,  10,
								  100, 100, 100, 100, 100, 100 };


// これだと時間がかかりすぎということで、もう少し減らす
// 2016/3/27
//
//		int[] depth = new int[] {   9,   9,   9,   9,
//								    9,   9,  10,  100,
//								  100, 100, 100, 100, 100, 100 };

// Pentium 3(700MHz)時代(2015までこの値を採用していた)
//		int[] depth = new int[] {   5,   5,   5,   5,
//								    5,   5,   6,   6,
//								  100, 100, 100, 100, 100, 100 };
		b.setDepthBorder(depth[getBoard().getTricks()]);
		
		int[] bps = b.getBestPlay();
		
		//
		// デバッグ用出力
		//
/*
for (int i = 0; i < bps.length; i++) {
	if (bps[i] == -1) break;
	System.out.print(" " + i + ":" + OptimizedBoard.getCardString(bps[i]));
}
System.out.println();
*/
		
		//------------------
		// 同格カードの抽出
		//------------------
		// プレイされていないカード = 0 // または今場に出ているカード
		// 持っているカード         = 1
		// 指定されたカード			= 2
		// プレイされたカード       = 3
		// 0 を delimiter として、token を区切り、2 が含まれている token の
		// 1 を 2 に変更する。2 となっているカードを返却する
		
		int[] tmp = new int[56];
		
		// プレイされたカード(3)の設定
		// まだ山に戻っていないカード(disposed)
		//   = { open cards } - { 今出ているカード }
		Board board = getBoard();
		Packet disposed = board.getOpenCards().sub(board.getTrick()).sub(getDummyHand());
		
		for (int i = 0; i < disposed.size(); i++) {
			Card c = disposed.peek(i);
			tmp[ OptimizedBoard.getCardNumber(c) ] = 3;
		}
		
		// 持っているカード(1)の設定
		Packet h = getHand();
		
		for (int i = 0; i < h.size(); i++) {
			Card c = h.peek(i);
			tmp[ OptimizedBoard.getCardNumber(c) ] = 1;
		}
		
		// 指定されたカード(2)の設定
		for (int i = 0; i < bps.length; i++) {
			if (bps[i] == -1) break;
//if (tmp[ bps[i] ] != 1) System.out.println("asserted in tmp != 1");
			tmp[ bps[i] ] = 2;
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
		
		//
		// 結果生成
		//
		Packet result = new PacketImpl();
		
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i] != 2) continue;
			
			int value	= (i % 14) + 2;
			if (value == 14) value = Card.ACE;
			int suit	= (i / 14) + 1;
			
			result.add(getHand().peek(suit, value));
		}
System.out.println("同格カード含めた最善プレイ候補:" + result);
		return result;
	}
	
	/**
	 * はじめの方は SimplePlayer2 を優先させることができる
	 */
	static final boolean[] SPL_IS_SUPERIOR = new boolean[]
							 { true, true, true, false, false,
							 false, false, false, false, false,
							 false, false, false };
	
	/**
	 * 指定されたプレイ候補から、リード規則などに従うプレイを選びます。
	 * 各プレイ候補について、point 付けを行い、最大 point のプレイを返却します。
	 *
	 * @return		いいプレイ
	 */
	protected Card choosePlay(Packet option) throws InterruptedException {
		if (SPL_IS_SUPERIOR[getBoard().getTricks()]) {
			Card simplePlayer2Play = base.draw2(); // 考えた振りのwaitなし
System.out.println("SimplePlayer2 の意見を優先 : " + simplePlayer2Play);
			return simplePlayer2Play;
		}
		
		//
		// point付けをする
		//
		int[] point = new int[option.size()];
		
		//
		// オープニングリードの場合の規則
		//
		if (getBoard().getStatus() == Board.OPENING) {
			Packet p = leadSignal();
System.out.println("Lead Signal : " + p);
			for (int i = 0; i < option.size(); i++) {
				Card c = option.peek(i);
				if (p.contains(c)) point[i] += 100; //point[i] = 100;
			}
		}
		
		if (getBoard().getTurn() != getBoard().getDummy()) {
			// DummyではSimplePlayer2が機能しないため、スキップ
			
			//
			// SimplePlayer2 で選んだ手
			//
			Card simplePlayer2Play = base.draw2(); // 考えた振りのwaitなし
System.out.println("SimplePlayer2 の意見 : " + simplePlayer2Play);
			int index = option.indexOf(simplePlayer2Play);
			if (index >= 0) point[index] += 50;
			
			//
			// リードの場合、SimplePlayer2 のスートごとの選んだ手も評価
			//
			if (getPlayOrder() == LEAD) {
				for (int suit = 1; suit < 5; suit++) {
					if (getMyHand().countSuit(suit) == 0) continue;
					Card sp;
					if (getBoard().getContract().getSuit() == Bid.NO_TRUMP) {
						sp = base.choosePlayInNTLead(suit);
					} else {
						sp = base.choosePlayInSuitLead(suit);
					}
System.out.println("SimplePlayer2 のスートごとの意見：" + sp);
					int ind = option.indexOf(sp);
					if (ind >= 0) point[ind] += 10;
				}
			}
		}
		//
		// ディスカードの際、スクイズ耐性を増やす処理(2015/8/15追加)
		// 覗き見するため、SimplePlayer2 でなく ReadAheadPlayer に記述
		// 　相手のサイドスーツをエスタブリッシュさせないため、以下の
		// 　アンド条件で point[] を減らします
		// 　　　1) パートナーと枚数が同じか長い場合
		//   　　2) 相手の長いサイドスート
		//
		if  ( (getPlayOrder() != LEAD)&& // リードでなく
			  (!option.containsSuit(getLead().getSuit()))&& // リードスーツがなく
			  (!getMyHand().containsSuit(getBoard().getTrump())) ) { // トランプもない
			
System.out.println("ディスカード用処理開始");
			//
			// 相手の長いサイドスート、枚数、座席を検出する
			//
			int s1 = (getMySeat() + 1)%4;
			int s2 = (getMySeat() + 3)%4;
			Packet hand1 = getBoard().getHand()[s1];
			Packet hand2 = getBoard().getHand()[s2];
			
			int longSideSuitSeat	= -1;
			int longSideSuit		= -1;
			int longSideSuitCount	= -1; // 初期値
			
			for (int suit = 1; suit < 5; suit++) {
				if (suit == getBoard().getTrump()) continue;
				// トランプは除外, No Trump時は除外対象なしとなる
				if (hand1.countSuit(suit) > longSideSuitCount) {
					longSideSuitCount	= hand1.countSuit(suit);
					longSideSuit		= suit;
					longSideSuitSeat	= s1;
				}
				if (hand2.countSuit(suit) > longSideSuitCount) {
					longSideSuitCount	= hand2.countSuit(suit);
					longSideSuit		= suit;
					longSideSuitSeat	= s2;
				}
			}
			if (longSideSuitCount == -1)
				throw new InternalError("ReadAheadPlayer ディスカード処理で、想定外状態を検出しました");
			
System.out.println("相手の長いサイドスート : " + BridgeUtils.suitString(longSideSuit));
			//
			// 1) 2) のアンド条件となるスートを特定
			//
			int myCount  = getMyHand().countSuit(longSideSuit);
			int prdCount = getBoard().getHand(getPartnerSeat()).countSuit(longSideSuit);
			if ( (myCount >= prdCount)&&(myCount <= longSideSuitCount) ) {
				//	条件に合うので、point[] を減点
				for (int i = 0; i < option.size(); i++) {
					if (option.peek(i).getSuit() == longSideSuit)
						// SimplePlayer2 より優先
						point[i] -= 75;
				}
			}
		}
		
		//
		// 最大のものを選ぶ
		//
		int maxPoint = point[0];
		int maxIndex = 0;
		for (int i = 1; i < option.size(); i++) {
			if (point[i] > maxPoint) {
				maxPoint = point[i];
				maxIndex = i;
			}
		}
		
		return option.peek(maxIndex);
	}
	
	private Packet leadSignal() {
		if (getBoard().getContract().getSuit() == Bid.NO_TRUMP) {
			return leadSignalInNoTrump();
		} else {
			return leadSignalInTrump();
		}
	}
	
	private Packet leadSignalInNoTrump() {
		Packet result = new PacketImpl();
		
		for (int suit = Card.CLUB; suit <= Card.SPADE; suit++) {
			if (getHand().countSuit(suit) == 0) continue;
			result.add(ntOpening(suit));
		}
		return result;
	}
	
	private Card ntOpening(int suit) {
		Packet hand = getMyHand();
		
		String suitPat = BridgeUtils.valuePattern(hand, suit);
		int value = -1;
		
		//
		// 所定のハンドパターンに合致するか
		//
		if (suitPat.startsWith("AKQ"))	{
			if (hand.countSuit(suit) >= 5) value = Card.ACE;
			else value = Card.KING;
		}
		if (suitPat.startsWith("KQJ"))	value = Card.KING;
		if (suitPat.startsWith("KQT"))	value = Card.KING;
		if (suitPat.startsWith("AQJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("AQJ9"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJ9"))	value = Card.QUEEN;
		if (suitPat.startsWith("AKJT"))	{
			if (hand.countSuit(suit) >= 5) value = Card.ACE;
			else value = Card.KING;
		}
		if (suitPat.startsWith("AJT"))	value = Card.JACK;
		if (suitPat.startsWith("KJT"))	value = Card.JACK;
		if (suitPat.startsWith("JT"))	value = Card.JACK;
		if (suitPat.startsWith("AKT9"))	value = 10;
		if (suitPat.startsWith("AT9"))	value = 10;
		if (suitPat.startsWith("KT9"))	value = 10;
		if (suitPat.startsWith("QT9"))	value = 10;
		if (suitPat.startsWith("AQT9"))	value = 10;
		if (suitPat.startsWith("T9"))	value = 10;
		
		if (value > -1) return hand.peek(suit, value);
		
		Packet p = hand.subpacket(suit);
		p.arrange();
		if ( bridgeValue(p.peek(0)) < 10 ) {
			return p.peek(0); // トップオブナッシング
		}
		
		//
		// ４ｔｈベストが出せるか
		//
		int size = p.size();
		
		if (size >= 4) return p.peek(3);
		
		//
		// ４ｔｈベストが出せない
		//
		if (size == 3) return p.peek(2);
		return p.peek(0);
	}
	
	private Packet leadSignalInTrump() {
		Packet result = new PacketImpl();
		
		for (int suit = Card.CLUB; suit <= Card.SPADE; suit++) {
			if (getHand().countSuit(suit) == 0) continue;
			
			result.add(suitOpening(suit));
		}
		return result;
	}
	
	//
	// AK ダブルトンから K が出てくるけどＯＫ？
	//
	private Card suitOpening(int suit) {
		Packet hand = getMyHand();
//		if (suit == Board.getContract().getSuit()) return 0;
		
		String suitPat = BridgeUtils.valuePattern(hand, suit);
//System.out.println("suitOpening(suit) . suitPat = " + suitPat);
		if (suitPat.equals("AK")) return hand.peek(suit, Card.ACE);
		if (suitPat.startsWith("AK")) return hand.peek(suit, Card.KING);
		if (suitPat.startsWith("A")) return hand.peek(suit, Card.ACE);
		if (suitPat.startsWith("KQ")) return hand.peek(suit, Card.KING);
		if (suitPat.startsWith("QJ")) return hand.peek(suit, Card.QUEEN);
		if (suitPat.startsWith("KJT")) return hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("JT")) return hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("KT9")) return hand.peek(suit, 10);
		if (suitPat.startsWith("QT9")) return hand.peek(suit, 10);
		if (suitPat.startsWith("T9")) return hand.peek(suit, 10);
		if (suitPat.charAt(0) <= '9') return hand.peek(suit, suitPat.charAt(0) - '0');
		
		Packet p = hand.subpacket(suit);
		p.arrange();
		if ( bridgeValue(p.peek(0)) < 10 ) {
			return p.peek(0); // トップオブナッシング
		}
		if (p.size() >= 4) return p.peek(3);
		if (p.size() == 3) return p.peek(2);
		return p.peek(0);
	}
	
	/**
	 * Aceを14に変換します。
	 */
	private int bridgeValue(int value) {
		if (value == 1) return 14;
		else return value;
	}
	
	/**
	 * Aceを14として、指定カードの値を読み取ります
	 */
	private int bridgeValue(Card target) {
		return bridgeValue(target.getValue());
	}
	
}

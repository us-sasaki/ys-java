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
		let result = `bestPlays=${this.bestPlayCount}, bestPaths=${this.bestPlayPaths}, totalPlayCount=${this.totalPlayCount}, finalNSTricks=${this.finalNSTricks}`;
		
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
	 * なお、実際にあらわすカードのない添え字(13, 27, 41, 55)のカードには 15 が格納される。
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
		let winner = this.leader[(this.count>>2) - 1];
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
		let countAtLead		= (this.count>>2)*4+1;
		
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
				
				const stats = this.calculateImpl(0, 14 * OptimizedBoard.TRICK_MULTIPLICITY, true);
//console.log(this.getCardString(c) + " のボード統計情報");
//console.log(stats.toString());
				
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
			result.finalNSTricks = this.nsWins * OptimizedBoard.TRICK_MULTIPLICITY;
			
			return result;
		} else if (this.count === 48) {
			// 残り１トリックだった場合の返却
			result.totalPlayCount	= 1;
			result.bestPlayCount	= 1;
			result.bestPlayPaths	= 1;
			result.finalNSTricks	= this.nsWins * OptimizedBoard.TRICK_MULTIPLICITY;
			
			// nsWins を求める
			
			// 全員分最後の１枚が何か調べ、lastPlayBuffer に格納する
			for (let i = 0; i < 55; i++) {
				if (this.card[i] < 4) {
					this.lastPlayBuffer[this.card[i]] = i;
				}
			}
			// ウィナーの決定、leader に設定する
			let winner = this.leader[this.count>>2];
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
				} else if ( (card/14 |0) === this.trump ) {
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
		if ((depth >= this.depthBorder)&&( (this.count%4) === 0 )) {
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
		const countAtLead		= (this.count>>2)*4+1;
		
		// スートフォローできるかどうかの判定
		// 現在、スートフォローできるかどうかの検索と実際にプレイするループの
		// ２つをまわしているが、インライン展開することで１つにできる
		let	startIndex = 0;
		let	endIndex = 55;
		if ( (this.count % 4) != 0 ) {
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
						this.undo();
						break;
					}
				} else if (bestTricks === finalTricks) {
					bestPlayCount++;
					bestPlayPath	+= stats.bestPlayPaths;
				}
				
				// result の更新
				result.totalPlayCount += stats.totalPlayCount;
				
				this.undo();
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
		this.calcPropData();
		
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
				&&(this.isWinner[this.lowestCardOfShorterSuit[NSorEW][suit]])
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
	 * @static
	 * @param {number} card カード定数
	 * @returns {string} C5 のようなカード文字列
	 */
	static getCardString(card) {
		return OptimizedBoard.SUIT_STR[card/14 |0]+OptimizedBoard.VALUE_STR[card%14];
	}
	
	/**
	 * カードオブジェクトからカード定数を求めます。
	 * @static
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
	static DEPTH = [8,8,8,8,8,8,9,10,100, 100, 100, 100, 100, 100 ];

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
		
		this.base = new SimplePlayer2(board, seat, ol);
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
		const t0 = new Date().getTime();
		
		const board = this.myBoard;
		
		if (board.status === Board.OPENING) {
			// オープニングリードの場合は SimplePlayer2 のアルゴリズムを使う
			if (this.openingLeadSpecified) return await this.base.draw();
		}
		
		// 最善手プレイの集合を取得します。
		const playOptions = this.getPlayOptions();
		
		const play = this.choosePlay(playOptions);
		
		// プレイ間隔一定のため
		const t = new Date().getTime();
		if ((t - t0) < 700 && board.getField() )
			await board.getField().sleep(700 - (t - t0)); // 700msec になるまで考えるふり
		
		return play;
	}
	
	/**
	 * OptimizedBoard を使用して、最善手の候補を見つけます。
	 * 本クラス内の Optimized 関連部分となります。
	 *
	 * @returns {Packet} 最善手の候補
	 */
	getPlayOptions() {
		const b = new OptimizedBoard(this.myBoard);
		//
		// トリック数による先読みの深さ変更
		//
		
		// depthBorder 値を指定
		// depthBorder は、最低先読みプレイ数で、実際にはこれ以降の最初のリード状態
		// まで先読みが行われます。例えば０を指定し、すでにリード状態にあった場合、
		// 先読みは行われません。
		//                        
		const depth = ReadAheadPlayer.DEPTH;
		//const depth = [5,5,5,5,5,5,5,5,5,5,5,5,5,5]; // for test


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
		b.setDepthBorder(depth[this.myBoard.getTricks()]);
		
		const bps = b.getBestPlay();
		
		//------------------
		// 同格カードの抽出
		//------------------
		// プレイされていないカード = 0 // または今場に出ているカード
		// 持っているカード         = 1
		// 指定されたカード			= 2
		// プレイされたカード       = 3
		// 0 を delimiter として、token を区切り、2 が含まれている token の
		// 1 を 2 に変更する。2 となっているカードを返却する
		
		const tmp = new Array(56).fill(0);
		
		// プレイされたカード(3)の設定
		// まだ山に戻っていないカード(disposed)
		//   = { open cards } - { 今出ているカード }
		const board = this.myBoard;
		const disposed = board.openCards.sub(board.getTrick()).sub(this.getDummyHand());
		
		for (let i = 0; i < disposed.children.length; i++) {
			const c = disposed.children[i];
			tmp[ OptimizedBoard.getCardNumber(c) ] = 3;
		}
		
		// 持っているカード(1)の設定
		const h = this.getHand();
		
		for (let i = 0; i < h.children.length; i++) {
			const c = h.children[i];
			tmp[ OptimizedBoard.getCardNumber(c) ] = 1;
		}
		
		// 指定されたカード(2)の設定
		for (let i = 0; i < bps.length; i++) {
			if (bps[i] === -1) break;
//if (tmp[ bps[i] ] != 1) console.log("asserted in tmp != 1");
			tmp[ bps[i] ] = 2;
		}
		
		// token ごとの処理
		let tokenStartIndex = 0;
		//let resultCount = 0;
		
		while (true) {
			// delimiter でないインデックスを探す --> tokenStartIndex
			for (; tokenStartIndex < 56; tokenStartIndex++) {
				if (tmp[tokenStartIndex] !== 0) break;
			}
			if (tokenStartIndex === 56) break;
			
			let tokenEndIndex;
			let containsTargetCard = false;
			for (tokenEndIndex = tokenStartIndex; tokenEndIndex < 56; tokenEndIndex++) {
				if (tmp[tokenEndIndex] === 2)
					containsTargetCard = true;
				else if (tmp[tokenEndIndex] === 0) break;
			}
			
			if (containsTargetCard) {
				for (let i = tokenStartIndex; i < tokenEndIndex; i++) {
					if (tmp[i] !== 3) {
						tmp[i] = 2;
						//resultCount++;
					}
				}
			}
			tokenStartIndex = tokenEndIndex + 1;
			if (tokenStartIndex >= 56) break;
		}
		
		//
		// 結果生成
		//
		const result = new Packet();
		
		for (let i = 0; i < tmp.length; i++) {
			if (tmp[i] !== 2) continue;
			
			let value	= (i % 14) + 2;
			if (value === 14) value = Card.ACE;
			const suit	= (i / 14 |0) + 1;
			
			result.add(this.getHand().peek(suit, value));
		}
console.log("同格カード含めた最善プレイ候補:" + result);
		return result;
	}
	
	/**
	 * 指定されたプレイ候補から、リード規則などに従うプレイを選びます。
	 * 各プレイ候補について、point 付けを行い、最大 point のプレイを返却します。
	 * @param {Packet} option プレイ候補
	 * @return		{Card} なるべく規則に従うプレイ
	 */
	choosePlay(option) {
		// SimplePlayer2 を優先させるか
		const SPL_IS_SUPERIOR = [ true, true, true, false, false,
								false, false, false, false, false,
								false, false, false ];
		if (SPL_IS_SUPERIOR[this.myBoard.getTricks()]) {
			const simplePlayer2Play = this.base.draw2(); // 考えた振りのwaitなし
console.log("SimplePlayer2 の意見を優先 : " + simplePlayer2Play);
			return simplePlayer2Play;
		}
		
		//
		// point付けをする
		//
		const point = new Array(option.children.length).fill(0);
		
		//
		// オープニングリードの場合の規則
		//
		if (this.myBoard.status === Board.OPENING) {
			const p = this.leadSignal();
console.log("Lead Signal : " + p);
			for (let i = 0; i < option.children.length; i++) {
				const c = option.children[i];
				if (p.indexOf(c) > -1) point[i] += 100; //point[i] = 100;
			}
		}
		
		if (this.myBoard.getTurn() != this.myBoard.getDummy()) {
			// DummyではSimplePlayer2が機能しないため、スキップ
			
			//
			// SimplePlayer2 で選んだ手
			//
			const simplePlayer2Play = this.base.draw2(); // 考えた振りのwaitなし
console.log("SimplePlayer2 の意見 : " + simplePlayer2Play);
			const index = option.indexOf(simplePlayer2Play);
			if (index >= 0) point[index] += 50;
			
			//
			// リードの場合、SimplePlayer2 のスートごとの選んだ手も評価
			//
			if (this.getPlayOrder() === Player.LEAD) {
				for (let suit = 1; suit < 5; suit++) {
					if (this.getMyHand().countSuit(suit) === 0) continue;
					let sp;
					if (this.myBoard.getContract().suit === Bid.NO_TRUMP) {
						sp = this.base.choosePlayInNTLead(suit);
					} else {
						sp = this.base.choosePlayInSuitLead(suit);
					}
console.log("SimplePlayer2 のスートごとの意見：" + sp);
					const ind = option.indexOf(sp);
					if (ind >= 0) point[ind] += 10;
				}
			}
		}
		//
		// ディスカードの際、スクイズ耐性を増やす処理(2015/8/15追加)
		// 覗き見するため、SimplePlayer2 でなく ReadAheadPlayer に記述
		//   相手のサイドスーツをエスタブリッシュさせないため、以下の
		//   アンド条件で point[] を減らします
		//       1) パートナーと枚数が同じか長い場合
		//       2) 相手の長いサイドスート
		//
		if  ( (this.getPlayOrder() !== Player.LEAD)&& // リードでなく
			(option.countSuit(this.getLead().suit) === 0)&& // リードスーツがなく
			(this.getMyHand().countSuit(this.myBoard.getTrump()) === 0) ) { // トランプもない
			
console.log("ディスカード用処理開始");
			//
			// 相手の長いサイドスート、枚数、座席を検出する
			//
			const s1 = (this.mySeat + 1)%4;
			const s2 = (this.mySeat + 3)%4;
			const hand1 = this.myBoard.getHand()[s1];
			const hand2 = this.myBoard.getHand()[s2];
			
			//let longSideSuitSeat	= -1;
			let longSideSuit		= -1;
			let longSideSuitCount	= -1; // 初期値
			
			for (let suit = 1; suit < 5; suit++) {
				if (suit === this.myBoard.getTrump()) continue;
				// トランプは除外, No Trump時は除外対象なしとなる
				if (hand1.countSuit(suit) > longSideSuitCount) {
					longSideSuitCount	= hand1.countSuit(suit);
					longSideSuit		= suit;
					//longSideSuitSeat	= s1;
				}
				if (hand2.countSuit(suit) > longSideSuitCount) {
					longSideSuitCount	= hand2.countSuit(suit);
					longSideSuit		= suit;
					//longSideSuitSeat	= s2;
				}
			}
			if (longSideSuitCount === -1)
				throw new Error("ReadAheadPlayer ディスカード処理で、想定外状態を検出しました");
			
console.log("相手の長いサイドスート : " + BridgeUtils.suitString(longSideSuit));
			//
			// 1) 2) のアンド条件となるスートを特定
			//
			const myCount  = this.getMyHand().countSuit(longSideSuit);
			const prdCount = this.myBoard.getHand(this.getPartnerSeat()).countSuit(longSideSuit);
			if ( (myCount >= prdCount)&&(myCount <= longSideSuitCount) ) {
				//	条件に合うので、point[] を減点
				for (let i = 0; i < option.children.length; i++) {
					if (option.children[i].suit === longSideSuit)
						// SimplePlayer2 より優先
						point[i] -= 75;
				}
			}
		}
		
		//
		// 最大のものを選ぶ
		//
		let maxPoint = point[0];
		let maxIndex = 0;
		for (let i = 1; i < option.children.length; i++) {
			if (point[i] > maxPoint) {
				maxPoint = point[i];
				maxIndex = i;
			}
		}
		
		return option.children[maxIndex];
	}
	
	/**
	 * @returns {Packet}
	 */
	leadSignal() {
		if (this.myBoard.getContract().suit === Bid.NO_TRUMP) {
			return this.leadSignalInNoTrump();
		} else {
			return this.leadSignalInTrump();
		}
	}
	
	/**
	 * @returns {Packet}
	 */
	leadSignalInNoTrump() {
		const result = new Packet();
		
		for (let suit = Card.CLUB; suit <= Card.SPADE; suit++) {
			if (this.getHand().countSuit(suit) === 0) continue;
			result.add(this.ntOpening(suit));
		}
		return result;
	}
	
	/**
	 * @param {number} suit
	 * @returns {Card}
	 */
	ntOpening(suit) {
		const hand = this.getMyHand();
		
		const suitPat = BridgeUtils.valuePattern(hand, suit);
		let value = -1;
		
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
		
		const p = hand.subpacket(suit);
		p.arrange();
		if ( this.bridgeValue(p.children[0]) < 10 ) {
			return p.children[0]; // トップオブナッシング
		}
		
		//
		// ４ｔｈベストが出せるか
		//
		const size = p.children.length;
		
		if (size >= 4) return p.children[3];
		
		//
		// ４ｔｈベストが出せない
		//
		if (size === 3) return p.children[2];
		return p.children[0];
	}
	
	/**
	 * @returns {Packet}
	 */
	leadSignalInTrump() {
		const result = new Packet();
		
		for (let suit = Card.CLUB; suit <= Card.SPADE; suit++) {
			if (this.getHand().countSuit(suit) === 0) continue;
			
			result.add(this.suitOpening(suit));
		}
		return result;
	}
	
	//
	// AK ダブルトンから K が出てくるけどＯＫ？
	//
	/**
	 * @param {number} suit
	 * @returns {Card}
	 */
	suitOpening(suit) {
		const hand = this.getMyHand();
		
		const suitPat = BridgeUtils.valuePattern(hand, suit);
		if (suitPat == "AK") return hand.peek(suit, Card.ACE);
		if (suitPat.startsWith("AK")) return hand.peek(suit, Card.KING);
		if (suitPat.startsWith("A")) return hand.peek(suit, Card.ACE);
		if (suitPat.startsWith("KQ")) return hand.peek(suit, Card.KING);
		if (suitPat.startsWith("QJ")) return hand.peek(suit, Card.QUEEN);
		if (suitPat.startsWith("KJT")) return hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("JT")) return hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("KT9")) return hand.peek(suit, 10);
		if (suitPat.startsWith("QT9")) return hand.peek(suit, 10);
		if (suitPat.startsWith("T9")) return hand.peek(suit, 10);
		if (suitPat[0] <= '9') return hand.peek(suit, parseInt(suitPat[0]));
		
		const p = hand.subpacket(suit);
		p.arrange();
		if ( this.bridgeValue(p.children[0]) < 10 ) {
			return p.children[0]; // トップオブナッシング
		}
		if (p.children.length >= 4) return p.children[3];
		if (p.children.length === 3) return p.children[2];
		return p.children[0];
	}
	
}

/**
 * プレイされたカード、ディスカードなどの情報から、
 * ディストリビューションを計算するクラス。
 * ４人のスートの枚数に関する情報、間違いないウィナーを取得する static関数を提供します。
 * 将来的には、ハンドパターンについて、その内容（と確率）を提供したい。
 * 本クラスの countDistribution(Board, int) メソッドは、最小、最大について論理的な
 * 値より幅が多くなる可能性があります。これは、最小、最大相互の依存関係があり、逐次的に
 * 求める方式（方程式を求める際のニュートン法ライク）を採用していることに起因します。
 * 将来的に反復処理を行う、方程式で求めるなどで改善する余地がありますが、SimplePlayer2
 * での使用上問題ないレベルです。
 */
class Utils {
	/** @type {number} 3番目の添え字に適用する値(0)です */
	static MIN = 0;
	/** @type {number} 3番目の添え字に適用する値(1)です */
	static MAX = 1;
	
	/**
	 * ４人のディストリビューションを(わかる範囲で)カウントします。
	 * 指定する Board では、ダミーと指定した席のハンド情報を持っている必要があります。
	 *
	 * @param	{Board} board ４人のハンドを保持する Board
	 * @param	{number} seat 自分の席
	 * @return	{number[][][]} int の３次元配列(int[4][4][2])で、[座席][スート][最大(1) or 最小(0)]
	 */
	static countDistribution(board, seat) {
		if (seat === board.getDummy()) seat ^= 2;
		//	throw new Error("ダミーにおけるカウントはサポートしてません");
		if ( (seat < 0)||(seat > 3) )
			throw new Error("指定された seat の値(="+seat+")が異常です");
		
		//int[][][] c = new int[4][][];
		const c = new Array(4);
		
		// 自分とダミーのハンドのディストリビューションはすでにわかっている
		const dummySeat = board.getDummy();
		
		const dummyHand	= board.getHand(board.getDummy());
		const myHand = board.getHand(seat);
		
		c[dummySeat]	= Utils.countKnownDistribution(dummyHand);
		c[seat]			= Utils.countKnownDistribution(myHand);
		
		//
		// 他の２つのディストリビューションを計算する
		//
		/** @type {number[]} [2] */ const other = new Array(2).fill(0);
		let num = 0;
		for (let dir = 0; dir < 4; dir++) {
			if ( (dir === dummySeat)||(dir === seat) ) continue;
			other[num] = dir;
			num++;
		}
		
		// まず、残っているカード枚数から計算する。
		// (最小枚数)=0
		// (最大枚数)= min( そのスートの残枚数, その人のハンドの枚数)
		const played = Utils.getPlayedCards(board);
		
		const playedDist = Utils.countKnownDistribution(played); // プレイされたカードのディストリビューション
		
		c[ other[0] ] = new Array(4); //new int[4][2];
		c[ other[1] ] = new Array(4); //new int[4][2];
		
		for (let suit = 0; suit < 4; suit++) {
			let restCards =			13
								- playedDist[suit][Utils.MIN]
								- c[dummySeat][suit][Utils.MIN]
								- c[seat][suit][Utils.MIN]; // 残り枚数
			c[ other[0] ][suit] = [];
			c[ other[1] ][suit] = [];
			c[ other[0] ][suit][Utils.MIN] = 0;
			c[ other[1] ][suit][Utils.MIN] = 0;
			
			c[ other[0] ][suit][Utils.MAX] = Math.min(restCards, board.getHand( other[0] ).children.length);
			c[ other[1] ][suit][Utils.MAX] = Math.min(restCards, board.getHand( other[1] ).children.length);
		}
		
		// 次に、ショウアウトの情報を用いる
		// ショウアウト i.e. その人のそのスートのMAX=0
		for (let i = 0; i < board.getTricks(); i++) {
			const trick = board.getAllTricks()[i];
			const leadSuit = trick.children[0].suit; // lead suit
			for (let j = 1; j < trick.children.length; j++) {
				const player = (j + trick.leader)%4;
				if ( ( player === dummySeat )||( player === seat ) ) continue;
				if (trick.children[j].suit !== leadSuit) { // ショウアウト
					c[ player ][leadSuit-1][Utils.MAX] = 0;
					// もう一人はだれかを見つける。そのスートの枚数は確定する。
					const another = (other[0] === player)?1:0;
					c[ other[another] ][leadSuit-1][Utils.MAX] =
					c[ other[another] ][leadSuit-1][Utils.MIN] =
									13
								- playedDist[leadSuit-1][Utils.MAX]
								- c[ dummySeat  ][leadSuit-1][Utils.MAX]
								- c[   seat     ][leadSuit-1][Utils.MAX];
				}
			}
		}
		// 最後に、ショウアウトしたことから最小枚数が限定される分の修正
		for (let i = 0 ; i < 2; i++) {
			const cards = board.getHand( other[i] ).children.length
						- c[other[i]][0][Utils.MAX]
						- c[other[i]][1][Utils.MAX]
						- c[other[i]][2][Utils.MAX]
						- c[other[i]][3][Utils.MAX];
			for (let suit = 0; suit < 4; suit++) {
				// 同じスートのカード枚数から出る条件
				let restMinCards =	13
								- playedDist[suit][Utils.MAX]
								- c[ dummySeat  ][suit][Utils.MAX]
								- c[   seat     ][suit][Utils.MAX]
								- c[ other[1-i] ][suit][Utils.MAX];
				
				// ハンドの総数から出る条件
				const cc = cards + c[other[i]][suit][Utils.MAX];
				restMinCards = Math.max(restMinCards, cc);
				
				// ショウアウトしたことによって、もう一方の other に与える影響があるのでは？
				// 上で考慮済みだった
				// では、MINが決まったことによってMAXにまた影響がでるのでは？
				// -> でる。最大枚数が変わる
				const newMin = Math.max(c[other[i]][suit][Utils.MIN], restMinCards);
						
				c[ other[i]][suit][Utils.MIN] = newMin;
			}
			
		}
		//
		// つねに成り立つ式によるMAXの補正(by 和美)
		//
		for (let i = 0; i < 2; i++) {
			for (let suit = 0; suit < 4; suit++) {
				c[ other[1-i]][suit][Utils.MAX] = 13
								- playedDist[suit][Utils.MAX]
								- c[ dummySeat  ][suit][Utils.MAX]
								- c[   seat     ][suit][Utils.MAX]
								- c[  other[i]  ][suit][Utils.MIN];
			}
		}
		return c;
	}
	
	/**
	 * 与えられたハンドのディストリビューションをカウントします。
	 * UnspecifiedCard はカウントされませんが、本メソッドは Specified Card
	 * からなる Packet に対して使用することを想定しています。
	 *
	 * @param {Packet} hand カウントしたいハンド
	 * @return {number[][]} 第一添数はスートをあらわし、第二添数は、最小値か最大値の選択をします。
	 */
	static countKnownDistribution(hand) {
		const result = new Array(4);
		
		for (let i = 0; i < 4; i++) {
			result[i] = [];
			result[i][Utils.MIN] = result[i][Utils.MAX] = hand.countSuit(i+1); // 最小値 = 最大値 (枚数確定)
		}
		return result;
	}
	
	/**
	 * すでにプレイされたカードを取得します。
	 * 現在、Trick から１枚ずつ取ってくるアルゴリズムです。
	 * @private
	 * @param {Board} board
	 * @returns {Packet}
	 */
	static getPlayedCards(board) {
		const result = new Packet();
		
		const tr = board.getAllTricks();
		if (tr === null) return result;
		
		let trickCount = board.getTricks();
		if (trickCount < 13) trickCount++;
		for (let i = 0; i < trickCount; i++) {
			for (let j = 0; j < tr[i].children.length; j++) {
				result.add(tr[i].children[j]);
			}
		}
		return result;
	}
}


/**
 * 和美の考えたディフェンダープレイを行うクラスです。
 * ビッドはつねにパスします。
 */
class SimplePlayer2 extends Player {
	/** @type {Card} */
	lead;
	/** @type {Board} */
	board;
	/** @type {Packet} */
	hand;
	/** @type {Packet} */
	dummyHand;
	
	/** @type {string?} */
	openingLead;
	
/*-------------
 * constructor
 */
	/**
	 * 
	 * @param {Board} board 所属する Board
	 * @param {number} seat 座っている座席番号
	 * @param {string?} ol オープニングリード指定
	 */
	constructor(board, seat, ol) {
		super();
		this.setBoard(board);
		this.setMySeat(seat);
		if (ol) this.openingLead = ol;
	}
	
/*------------
 * implements
 */
	/**
	 * パスします。
	 * @async
	 * @return	{Promise<Bid>}	パス
	 */
	async bid() {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * 和美の考えたディフェンダープレイを行います。
	 * @async
	 * @return {Promise<Card>}		和美の考えたプレイ
	 */
	async draw() {
		const field = this.myBoard.getField();
		if (field) await field.sleep(400); // 考えた振り
		return this.draw2();
	}
	
	/**
	 * @returns {Card} 和美の考えたプレイ
	 */
	draw2() {
		/** @type {Board} */ this.board	= this.myBoard;
		/** @type {Packet} */ this.hand = this.getHand(); // = getMyHand()
		this.dummyHand = this.getDummyHand();
		this.lead = this.getLead();
		
		const order = this.getPlayOrder();
		if (order === Player.LEAD)		return this.playIn1st();
		//
		// ラフできるときは（誰もまだラフしていなければ）ローラフし
		//（追加：すでに誰かがラフしているとき、
		//             オポーネントがラフしているとき、
		//                     オポーネントより強いトランプがあればチーペストにオーバーラフ、
		//                     オポーネントより強いトランプがなければディスカードする。
		//             パートナーだけがラフしているときはディスカードする。）
		//				パートナーが勝っているときはラフせず、ディスカードする。
		//
		if (this.hand.countSuit(this.lead.suit) === 0) { // スートフォローできない
			const trump = this.board.getContract().suit;
			const pack = this.hand.subpacket(trump); // NTのときは、空になる
			if (pack.children.length > 0) {
				// ラフすることができる
				pack.arrange();
				if ((order === Player.THIRD)&&(this.getDummyPosition()===Player.LEFT)) {
					//--------------------------------
					// 三番手でかつ最後がダミーの場合
					//--------------------------------
					// ラフで勝てる場合、チーペストラフで勝つ
					// 勝てない、またはディスカードで勝てる(パートナーが勝っている)
					// 場合、ディスカード(discard()が選んだカード)する。
					//
					
					// 勝てるカードを持っているか？
					// ダミーのもっとも強いカードを選ぶ
					/** @type {Card} */ let dummyStrongest;
					/** @type {Packet} */ let dFollow = this.dummyHand.subpacket(this.lead.suit);
					
					if (dFollow.children.length === 0) {
						dFollow = this.dummyHand.subpacket(trump);
					}
					if (dFollow.children.length === 0) {
						dummyStrongest = this.dummyHand.peek(); // 適当なもの
					}
					else {
						// フォローできるときはそのスートの最大のもの
						// できないときはトランプの最大なもの
						dFollow.arrange();
						dummyStrongest = dFollow.children[0];
					}
					
					//
					// 将来をシミュレートする(ダミーハンドからのプレイを行う)
					//
					
					// discard でも勝てる場合、discardするため、候補に追加
					
					const pack2 = this.hand.subpacket(this.board.getContract().suit);
					pack2.arrange();
					pack2.add(this.discard());
					
					/** @type {Card} */ let play = null;
					for (let i = 0; i < pack2.children.length; i++) {
						const virtual = new Trick(this.getTrick());
						virtual.add(pack2.children[i]);
						virtual.add(dummyStrongest);
						if (this.isItOurSide(virtual.getWinner())) play = pack2.children[i];
					}
					// だめなのでディスカード
					if (play === null) return this.discard();
					
					return play;
				}
				if (order === Player.THIRD) {
					//--------------------------------------
					// 三番手で最後の一人はダミーでない場合
					//--------------------------------------
					/** @type {Packet} */ const winner = this.getWinners2();
					if ( (winner.indexOf(this.lead) > -1)
							&&(this.isItOurSide(this.board.getTrick().getWinner())) ) {
						// パートナーはウィナーをプレイし、それが勝っているときにディスカード
						return this.discard();
					}
					// ローラフ
					pack.arrange();
					return pack.peek();
				}
				if (order === Player.FORTH) {
					//--------------
					// 四番手の場合
					//--------------
					// パートナーがすでにプレイしている
					if (this.isItOurSide(this.board.getTrick().getWinner())) {
						// 自分たち（この場合パートナー）の勝ち
						
						// ディスカードする
						return this.discard();
					}
				}
				//--------------------------------------------
				// 二番手の場合、四番手でまだ勝っていない場合
				//--------------------------------------------
				// パートナーがプレイしていないか、勝っていない
				// (チーペストにオーバー)ラフを試みる
				for (let i = pack.children.length-1; i >= 0; i--) {
					const c = pack.children[i];
					const virtual = new Trick(this.board.getTrick());
					virtual.add(c);
					if (this.isItOurSide(virtual.getWinner()))	return c;
				}
				return this.discard();
			} else {
				// ディスカードする
				return this.discard();
			}
		}
		
		//--------------------------
		// スートフォローできる場合
		//--------------------------
		
		if (order === Player.SECOND) return this.playIn2nd();
		if (order === Player.THIRD)	return this.playIn3rd();
		if (order === Player.FORTH)	return this.playIn4th();
		
		throw new Error("Play Order が異常値("+order+")になっています");
	}
	
	/**
	 * ディスカードします。
	 * @private
	 * @returns {Card} discard
	 */
	discard() {
		const winners = this.getWinners(); // 自分のハンドのウィナー
		let winnerIsInOnlyOneSuit = false;
		for (let i = 0; i < 4; i++) {
			const s = winners.children.length;
			if ((s > 0)&&(s === winners.countSuit(i+1))) winnerIsInOnlyOneSuit = true;
		}
		
		// スートを選ぶ
		const trump = this.board.getContract().suit;
		
		// トランプしか持っていないとき
		// 仕方なくローエストトランプをディスカード
		if (this.hand.children.length === this.hand.subpacket(trump).children.length)
			return this.hand.children[this.hand.children.length - 1];
		
		// 候補
		const p = new Packet(); // winner 抜き候補
		const w = new Packet(); // winner 入り候補
		for (let i = 0; i < this.hand.children.length; i++) {
			const c = this.hand.children[i];
			if (c.suit === trump) continue;	// トランプは候補としない
			w.add(c);
			if ((!winnerIsInOnlyOneSuit)&&winners.indexOf(c)>-1)
				continue;	// ウィナーを持っているスートが２以上のとき、ウィナーも候補としない
			p.add(c);
		}
		let suit;
		if (p.children.length === 0) {
			// ウィナーしか持っていない
			w.shuffle();
			suit = w.peek().suit;
		} else {
			p.shuffle();
			suit = p.peek().suit;
		}
		const p2 = this.hand.subpacket(suit);
		p2.arrange();
		return p2.peek(); // ローエスト
	}
	
	//*********************************************************************//
	//  １番手のプレイ（共通部分）                                         //
	//*********************************************************************//
	
	/**
	 * リードの位置にいるときの手を考えます。
	 * オープニングリードかどうか、コントラクトがＮＴかどうかで４通りの関数に分岐します。
	 * @private
	 * @returns {Card}
	 */
	playIn1st() {
		if (this.board.status === Board.OPENING) {
			// オープニングリード
			//
			// 指定がある場合はそのカード
			//
			if (this.openingLead) {
				let suit	= -1;
				let value	= -1;
				
				try {
					switch (this.openingLead.charAt(0)) {
					case 'S': suit = Card.SPADE;	break;
					case 'H': suit = Card.HEART;	break;
					case 'D': suit = Card.DIAMOND;	break;
					case 'C': suit = Card.CLUB;		break;
					}
					switch (openingLead.charAt(1)) {
					case 'A': value = Card.ACE;		break;
					case 'K': value = Card.KING;	break;
					case 'Q': value = Card.QUEEN;	break;
					case 'J': value = Card.JACK;	break;
					case 'T': value = 10;			break;
					default: value = parseInt(openingLead.charAt(1));
					}
				} catch (e) {
					// エラー無視
				}
				if ( (suit != -1)&&(value != -1) ) {
					// 指定されたスートとバリューがともに有効
					const ol = this.getMyHand().peek(suit, value);
					if (ol) return ol;
				}
				if ((suit != -1)&&(this.hand.countSuit(suit)>0)) {
					// スートのみが有効
					if (this.board.getContract().suit === Bid.NO_TRUMP)
						return this.ntOpening(suit);
					return this.suitOpening(suit);
				}
			}
			
			if (this.board.getContract().suit === Bid.NO_TRUMP) return this.ntOpening();
//console.log("playIn1st() suitOpening call");
			return this.suitOpening();
		}
		if (this.board.getContract().suit === Bid.NO_TRUMP) return this.ntLead();
		return this.suitLead();
	}
	
	//*********************************************************************//
	//  ＮＴオープニングリードのプレイ                                     //
	//*********************************************************************//
	
	/**
	 * ＮＴコントラクトの場合のオープニングリードを考えます
	 * @private
	 * @returns {Card}
	 */
	ntOpening_null() {
		//
		// 一番長いスートを選ぶ(同一枚数のときはランクの高いスート)
		//
		let suit = -1;
		let max  = -1;
		
		for (let i = 0; i < 4; i++) {
			const c = this.hand.countSuit(i+1);
			if (c > max) {
				max = c;
				suit = i+1;
			}
		}
		
		return this.ntOpening(suit);
	}
	
	/**
	 * ＮＴコントラクトのオープニングリードでスートまできまっている場合
	 * @private
	 * @param {number?} スーツ指定
	 * @returns {Card}
	 */
	ntOpening(suit) {
		if (!suit) return this.ntOpening_null();
		const suitPat = BridgeUtils.valuePattern(this.hand, suit);
		let value = -1;
		
		//
		// 所定のハンドパターンに合致するか
		//
		if (suitPat.startsWith("AKQ"))	{
			if (this.hand.countSuit(suit) >= 5) value = Card.ACE;
			else value = Card.KING;
		}
		if (suitPat.startsWith("KQJ"))	value = Card.KING;
		if (suitPat.startsWith("KQT"))	value = Card.KING;
		if ( (suitPat.startsWith("KQ"))&&(this.hand.countSuit(suit) === 3) )
			value = Card.KING; // 2015/8/15 added
		if (suitPat.startsWith("AQJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("AQJ9"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJ9"))	value = Card.QUEEN;
		if ( (suitPat.startsWith("QJ"))&&(this.hand.countSuit(suit) === 3) )
			value = Card.QUEEN; // 2015/8/15 added
		if (suitPat.startsWith("AKJT"))	{
			if (this.hand.countSuit(suit) >= 5) value = Card.ACE;
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
		
		if (value > -1) return this.hand.peek(suit, value);
		
		const p = this.hand.subpacket(suit);
		p.arrange();
		if ( this.bridgeValue(p.children[0]) < 10 ) {
			return p.children[0]; // トップオブナッシング
		}
		
		//
		// ４ｔｈベストが出せるか
		//
		const size = p.children.length;
		
		if (size >= 4) return p.children[3];
		
		//
		// ４ｔｈベストが出せない
		//
		if (size == 3) return p.children[2];
		return p.children[0];
	}
	
	//*********************************************************************//
	//  スートコントラクトでのオープニングリードのプレイ                   //
	//*********************************************************************//
	
	/**
	 * スーツコントラクトのオープニングリードを考える。
	 * @private
	 * @returns {Card}
	 */
	suitOpening_null() {
//console.log("suitOpening()");
		let max  = -1;
		let play = null;
		
		for (let i = 0; i < 4; i++) {
			if ( (i+1) === this.board.getContract().suit ) continue; // トランプは除外
			
			const suitPat = BridgeUtils.valuePattern(this.hand, i+1);
			
			// AK のあるスート (10 点)
			if ( (suitPat.startsWith("AK"))&&(max < 10) ) {
				max = 10;
				play = this.hand.peek(i+1, Card.KING);
			}
			// KQ のあるスート ( 9 点)
			if ( (suitPat.startsWith("KQ"))&&(max < 9) ) {
				max = 9;
				play = this.hand.peek(i+1, Card.KING);
			}
			// シングルトン ( 8 点)
			if ( (suitPat.length === 1)&&(max < 8) ) {
				max = 8;
				play = this.hand.subpacket(i+1).children[0];
			}
			// QJ のあるスート (7 点)
			if ( (suitPat.startsWith("QJ"))&&(max < 7) ) {
				max = 7;
				play = this.hand.peek(i+1, Card.QUEEN);
			}
			// ダブルトン (6 点)
			if ( (suitPat.length === 2)&&(max < 6) ) {
				max = 6;
				const p = this.hand.subpacket(i+1);
				p.arrange();
				play = p.children[0];
			}
		}
//console.log("suitOpening() play="+(play?play.toString():"null"));
		if (play !== null) return play;
		
		//
		// 決まらなかった(適当なスートを乱数で選ぶ)
		//
//console.log("suitOpening(): 決まらなかった");
		for (let i = 0; i < 20; i++) {
			const suit = ReproducibleRandom.nextInt(4)+1;
			if (suit === this.board.getContract().suit) continue;
			
			if (this.hand.countSuit(suit) > 0) return this.suitOpening(suit);
		}
		//
		// ハンドがトランプスートのみからなっているなど稀な場合
		//
		return this.hand.peek();
	}
	
	/**
	 * スーツコントラクトでオープニングリードのスートが決まったとき
	 * @private
	 * @param {number?} suit
	 * @returns {Card}
	 */
	suitOpening(suit) {
		if (suit === void 0) return this.suitOpening_null();
		const suitPat = BridgeUtils.valuePattern(this.hand, suit);
		if (suitPat.startsWith("AK")) return this.hand.peek(suit, Card.KING);
		if (suitPat.startsWith("A")) return this.hand.peek(suit, Card.ACE);
		if (suitPat.startsWith("KQ")) return this.hand.peek(suit, Card.KING);
		if (suitPat.startsWith("QJ")) return this.hand.peek(suit, Card.QUEEN);
		if (suitPat.startsWith("KJT")) return this.hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("JT")) return this.hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("KT9")) return this.hand.peek(suit, 10);
		if (suitPat.startsWith("QT9")) return this.hand.peek(suit, 10);
		if (suitPat.startsWith("T9")) return this.hand.peek(suit, 10);
		if (suitPat[0] <= '9') return this.hand.peek(suit, parseInt(suitPat[0]));
		
		const p = this.hand.subpacket(suit);
		p.arrange();
		if ( this.bridgeValue(p.children[0]) < 10 ) {
			return p.children[0]; // トップオブナッシング
		}
		if (p.children.length >= 4) return p.children[3];
		if (p.children.length === 3) return p.children[2];
		return p.children[0];
	}
	
	//*********************************************************************//
	//  ＮＴでの１番手のプレイ                                             //
	//*********************************************************************//
	
	/**
	 * ＮＴコントラクトでのリード
	 *
	 * @private
	 * @returns {Card}
	 */
	ntLead() {
// ３．ディフェンダーが勝ったときのリード
// ・ＮＴの場合
//   スートの決め方次の順位：
//   （０）自分の手のウィナーの数＋パートナーのウィナーの数が
//         コントラクトを落とすのに十分なとき、自分のウィナーをキャッシュ
//   （１）パートナーにウィナーのあるスート
//   （２）Ｏ．Ｌ．と同じスート
//   （３）今までディフェンダーが勝ったときにリードしたスート（最近から順に）
//   （４）０～３のスートがないとき
//         ＬＨＯ（ディクレアラーの左手）：
//                ダミーのアナー（ＨＣＰで判断）の多いスート
//         ＲＨＯ（ディクレアラーの右手）：
//                 ダミーのアナー（ＨＣＰで判断）の少ないスート    
// 
//   スート内でのカードの決め方を次の順位で決める                
//     （１）そのスートの中で、トップがウィナーならキャッシュ
//          ＡＫからはＫ、他は上
//     （２）ＫＱからＫ
//     （３）ＱＪからＱ
//     （４）ＫＪＴ，ＪＴからＪ
//     （５）ＫＴ９，ＱＴ９、Ｔ９からＴ
//     （６）その他：現在２枚：上
//                   現在３枚：３枚目
//                   現在４枚以上→４枚目  
		//
		// (0) 自分とパートナーのウィナーの数がコントラクトを落とすのに十分なとき、
		//     自分のウィナーをキャッシュ
		const winners = this.getWinnersInNTLead();
		
		// ディフェンダー側のとったトリック
		const win = this.board.getTricks() - BridgeUtils.countDeclarerSideWinners(this.board); 
		if ( (winners.children.length + win) > 7 - this.board.getContract().level ) {
			// コントラクトを落とせる
			for (let i = 0; i < winners.children.length; i++) {
				if (this.hand.indexOf(winners.children[i]) > -1) return winners.children[i];
			}
		}

		//
		// (1) パートナーにウィナーのあるスート
		//		（自分の持っているスート）
		//
//		for (int i = 0; i < winners.size(); i++) {
//			Card c = winners.peek(0);
//			if (!hand.contains(c)) {
//				int suit = c.getSuit();
//				Packet p = hand.subpacket(suit);
//				p.arrange();
//				return p.peek(); // ローエスト
//			}
//		}
		
		const suit = this.chooseSuitInNTLead();
		return this.choosePlayInNTLead(suit);
	}
	
	/**
	 * NTコントラクトのリードスートを選びます。
	 * @private
	 * @returns {number} suit
	 */
//   スートの決め方次の順位：
//   （１）パートナーにウィナーのあるスート
//   （２）Ｏ．Ｌ．と同じスート
//   （３）今までディフェンダーが勝ったときにリードしたスート（最近から順に）
//   （４）０～３のスートがないとき
//         ＬＨＯ（ディクレアラーの左手）：
//                ダミーのアナー（ＨＣＰで判断）の多いスート
//         ＲＨＯ（ディクレアラーの右手）：
//                 ダミーのアナー（ＨＣＰで判断）の少ないスート    
	chooseSuitInNTLead() {
		//
		// (1) パートナーにウィナーのあるスート
		//     ※ これは(2)と同値であるが一応実装してある
		//
		const winners = this.getWinnersInNTLead();
		for (let i = 0; i < winners.children.length; i++) {
			const c = winners.children[i];
			if (this.hand.indexOf(c) === -1) { // パートナーが持っている
				if (this.hand.countSuit(c.suit) > 0) return c.suit;
			}
		}
		
		//
		// (2) O.L. と同じスート
		//
		if (this.board.getTricks() >= 1) {
			const c = this.board.getAllTricks()[0].children[0];
			if (this.hand.countSuit(c.suit) > 0) return c.suit;
		}
		
		//
		// (3) 今までディフェンダーが勝ったトリックのリード(最近から順に)
		//
		const trick = this.board.getAllTricks();
		for (let i = this.board.getTricks()-2; i >= 0; i--) {
			if (this.isItOurSide(trick[i].getWinner())) { // 自分たちの勝ち
				const suit = trick[i+1].getLead().suit;
				if (this.hand.countSuit(suit) > 0) return suit;
			}
		}
		
		//
		// (4) ０～３のスートがないとき
		//        ＬＨＯ（ディクレアラーの左手）：
		//            ダミーのアナー（ＨＣＰで判断）の多いスート
		//        ＲＨＯ（ディクレアラーの右手）：
		//            ダミーのアナー（ＨＣＰで判断）の少ないスート
		//
		const dummyHonerPoint = BridgeUtils.countHonerPoint(this.dummyHand);
		
		if (this.getDummyPosition() === Player.LEFT) { // 自分はＬＨＯ
			let maxHcpSuit = 0;
			let maxHcpVal  = -1;
			for (let i = 1; i < 5; i++) {
				if (this.dummyHand.countSuit(i) === 0) continue; // 持ってないスートは除外
				if (this.hand.countSuit(i) === 0) continue;
				if (dummyHonerPoint[i] >= maxHcpVal) { // 同じ HCP では Major を優先させる
					maxHcpVal  = dummyHonerPoint[i];
					maxHcpSuit = i;
				}
			}
			if (maxHcpVal > -1) return maxHcpSuit;
		} else { // 自分はＲＨＯ
			let minHcpSuit = 0;
			let minHcpVal  = 100;
			for (let i = 1; i < 5; i++) {
				if (this.dummyHand.countSuit(i) === 0) continue;
				if (this.hand.countSuit(i) === 0) continue;
				if (dummyHonerPoint[i] <= minHcpVal) { // 同じ HCP では Major を優先させる
					minHcpVal  = dummyHonerPoint[i];
					minHcpSuit = i;
				}
			}
			if (minHcpVal < 100) return minHcpSuit;
		}
		// 持ってないスートが該当スートだった場合、適当に
		this.hand.shuffle();
		const suit = this.hand.children[0].suit;
		this.hand.arrange();
		return suit;
	}
	
	/**
	 * NT コントラクトの場合で、リードするスートが決まった場合のプレイを行います。
	 *  スート内でのカードの決め方を次の順位で決める                
	 *    （１）そのスートの中で、トップがウィナーならキャッシュ
	 *    （２）ＫＱからＫ
	 *    （３）ＱＪからＱ
	 *    （４）ＫＪＴ，ＪＴからＪ
	 *    （５）ＫＴ９，ＱＴ９、Ｔ９からＴ
	 *    （６）その他：現在２枚：上
	 *                  現在３枚：３枚目
	 *                  現在４枚以上→４枚目
	 * @param {number} suit suit
	 * @returns {Card}
	 */
	choosePlayInNTLead(suit) {
		const candidacy = this.hand.subpacket(suit);
		if (candidacy.children.length === 0)
			throw new Error("choosePlayInNTLead で指定されたスート("+suit+")を持っていません");
		candidacy.arrange();
		
		// （１）そのスートの中で、トップがウィナーならキャッシュ
		const winner = this.getWinners(); //getWinnersInNTLead();
		const top = candidacy.children[0];
		if (winner.indexOf(top) > -1) return top;
		
		// （２）ＫＱからＫ
		if (BridgeUtils.patternMatch(this.hand, "KQ*", suit)) {
			return this.hand.peek(suit, Card.KING);
		}
		
		// （３）ＱＪからＱ
		if (BridgeUtils.patternMatch(this.hand, "QJ*", suit)) {
			return this.hand.peek(suit, Card.QUEEN);
		}
		
		// （４）ＫＪＴ，ＪＴからＪ
		if (BridgeUtils.patternMatch(this.hand, "KJT*", suit)) {
			return this.hand.peek(suit, Card.JACK);
		}
		if (BridgeUtils.patternMatch(this.hand, "JT*", suit)) {
			return this.hand.peek(suit, Card.JACK);
		}
		
		// （５）ＫＴ９，ＱＴ９、Ｔ９からＴ
		if (BridgeUtils.patternMatch(this.hand, "KT9*", suit)) {
			return this.hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(this.hand, "QT9*", suit)) {
			return this.hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(this.hand, "T9*", suit)) {
			return this.hand.peek(suit, 10);
		}
		
		// （６）その他：現在２枚：上
		//               現在３枚：３枚目
		//               現在４枚以上→４枚目  
		switch (candidacy.children.length) {
		case 1:
		case 2:
			return candidacy.children[0];
		case 3:
			return candidacy.children[2];
		default:
			return candidacy.children[3];
		}
	}
	
	//*********************************************************************//
	//  スートコントラクトでの１番手のプレイ                               //
	//*********************************************************************//
	
	//・スーツの場合
	//  スートの決め方次の順位：
	//  （０）自分の手のウィナーの数＋パートナーのウィナーの数が
	//        コントラクトを落とすのに十分なとき、すべてキャッシュ
	//
	//  （１）ラフリスのスートを除外（それしかなければしかたない）
	//        ラフリスのスートとは：
	//        ダミーにもディクレアラーにもトランプが残っている状況で
	//                （現在０枚と判明していないこと）        
	//        ダミーもディクレアラーもが現在０枚と判明しているスート              
	//                  
	//  （２）パートナーのトランプスートが現在０枚と確定しないとき、かつ
	//        パートナーに現在０枚と確定しているサイドスートがあるとき、そのスート
	//        （ラフさせる）
	//
	//  （３）パートナーにウィナーのあるスート
	//  （４）Ｏ．Ｌ．と同じスート
	//  （５）いままでディフェンダーが勝ったときにリードしたスート（最近から順に）
	//  （６）以上のスートがないとき
	//        ＬＨＯ（ディクレアラーの左手）：
	//              ダミーのアナー（ＨＣＰで判断）の多いスート
	//        ＲＨＯ（ディクレアラーの右手）：              
	//              ダミーのアナー（ＨＣＰで判断）の少ないスート            
	//
	//  スート内でのカードの決め方
	//  次の順位で決める                
	//    （１）そのスートの中で、トップがウィナーならキャッシュ
	//    （２）ＫＱからＫ
	//    （３）ＱＪからＱ
	//    （４）ＫＪＴ，ＪＴからＪ
	//    （５）ＫＴ９，ＱＴ９、Ｔ９からＴ
	//    （６）その他：現在２枚：上
	//                  現在３枚：３枚目
	//                  現在４枚以上→４枚目
	/**
	 * @private
	 * @returns {Card}
	 */
	suitLead() {
		//
		// (0) 自分とパートナーのウィナーの数がコントラクトを落とすのに十分なとき、
		//     自分のウィナーをキャッシュ
		const winners = this.getWinnersInSuitLead();
		
		// ディクレアラー側のとったトリック
		const win = this.board.getTricks() - BridgeUtils.countDeclarerSideWinners(this.board);
		if ( (winners.children.length + win) > 7 - this.board.getContract().level ) {
			// コントラクトを落とせる
			// そのとき、リードされた回数の少ないスートをうつ（●未実装）
			for (let i = 0; i < winners.children.length; i++) {
				if (this.hand.indexOf(winners.children[i]) > -1) return winners.children[i];
			}
		}
		
		//  （１）ラフリスのスートを除外（それしかなければしかたない）
		//        ラフリスのスートとは：
		//        ダミーにもディクレアラーにもトランプが残っている状況で
		//                （現在０枚と判明していないこと）        
		//        ダミーもディクレアラーもが現在０枚と判明しているスート              
		
		//
		// （１）をどのように実装するか
		//
		//       それぞれの実装の中で、決めたスートがラフリスだった場合、次点のスートに変更
		//       することとする(あまりきれいではないが、(2)以降はそれぞれ個性的なやりかたな
		//       ので、得点つきでスートを登録する方式よりシンプルになりそうなので)
		//
		
		const suit = this.chooseSuitInSuitLead();
		
		return this.choosePlayInSuitLead(suit);
	}
	
	/**
	 * スーツコントラクトの場合のリードスートを選びます。
	 * @private
	 * @returns {number}
	 */
	chooseSuitInSuitLead() {
		// (int[4][4][2])で、[座席][スート][最大(1) or 最小(0)]
		const dist = Utils.countDistribution(this.board, this.mySeat);
//console.log('seat='+Board.SEAT_STRING[this.mySeat]+' contract='+this.board.getContract().toString());
//console.log(this.board.toString());
//console.log('chooseSuitInSuitLead dist='+JSON.stringify(dist));
		const trump = this.board.getContract().suit;
		
		//
		// (2) パートナーのトランプスートが現在０枚と確定しないとき、かつ
		//      パートナーに現在０枚と確定しているサイドスートがあるとき、そのスート
		//      （ラフさせる）
		//
		
		// トランプスートが現在０枚と確定しないとき、
		if (dist[ this.getPartnerSeat() ][ trump-1 ][ Utils.MAX ] > 0) {
//console.log('chooseSuitInSuitLead() トランプスーツが0枚と確定しない'+ Board.SEAT_STRING[this.getPartnerSeat()]);
			let sideSuit;
			for (sideSuit = 1; sideSuit < 5; sideSuit++) {
				if (sideSuit === trump) continue;
				if (this.hand.countSuit(sideSuit) === 0) continue;
				// (2) ではラフリスは単純に除外する
				if (this.isRuflis(dist, sideSuit)) continue;
				
				// パートナーに現在０枚と確定しているサイドスートがあるとき
				if (dist[ this.getPartnerSeat() ][ sideSuit-1 ][ Utils.MAX ] === 0) break;
			}
			if (sideSuit < 5) return sideSuit;
		}
		
		//
		// (3)パートナーにウィナーのあるスート
		//    これは(4)と同義なので、実装しない  → 実装してください
		//    これは難しいので未実装。かわりにＬＨＯの場合はＯＬと同じスート
		if ((this.board.getTricks() >= 1)&&(this.getDummyPosition() === Player.LEFT)) {
			const c = this.board.getAllTricks()[0].children[0];
			const suit = c.suit;
			if ( (this.hand.countSuit(suit) > 0)
				&& (!this.isRuflis(dist, suit)) ) return suit;
		}
		
		
		// (4) O.L. と同じスート  →やめる
		//     ただし、これがラフリスの場合スキップする 
		//
//		if (board.getTricks() >= 1) {
//			Card c = board.getAllTricks()[0].peek(0);
//			int suit = c.getSuit();
//			if (suit == trump) 
//			if ( (hand.containsSuit(suit))
//				&& (!isRuflis(dist, suit)) ) return suit;
//		}
		
		//
		//  （５）いままでディフェンダーが勝ったときにリードしたスート（最近から順に）→やめる
		//
		//        ラフリスの場合、スキップする
		//
//		Trick[] trick = board.getAllTricks();
//		for (int i = board.getTricks()-2; i >= 0; i--) {
//			if (isItOurSide(trick[i].getWinner())) { // 自分たちの勝ち
//				int suit = trick[i+1].getLead().getSuit();
//				if (isRuflis(dist, suit)) continue;
//				if (!hand.containsSuit(suit)) continue;
//				return suit;
//			}
//		}
		
		//  （６）以上のスートがないとき
		//        ＬＨＯ（ディクレアラーの左手）：
		//              ダミーのアナー（ＨＣＰで判断）の多いスート    やめる
		//        ＲＨＯ（ディクレアラーの右手）：              
		//              ダミーのアナー（ＨＣＰで判断）の少ないスート  やめる
		//        ただし、ラフリスのスートとトランプは除外する        やめない
		//
		//        ●ダミーのオリジナルハンドを対象 にするように変更してください
		//        ●ＬＨＯは、ダミーのオリジナルのアナーの枚数が１枚が最優先、
		//         ２枚が次優先、次が０枚となる。
		//        ●ＲＨＯは、アナーの枚数が０枚が最優先、１枚が次優先、となる
		//const dummyHonerPoint = BridgeUtils.countHonerPoint(this.dummyHand);
		const dummyOriginal = BridgeUtils.calculateOriginalHand(this.board)[this.board.getDummy()];
		
		if (this.getDummyPosition() === Player.LEFT) { // 自分はＬＨＯ
			let honers = -1;
			let honerSuit = -1;
			for (let i = 1; i < 5; i++) {
				if (this.dummyHand.countSuit(i) === 0) { // ダミーがラフできるスートは除外
					if (this.dummyHand.countSuit(trump) > 0) continue;
				}
				if (this.hand.countSuit(i) === 0) continue;	// 持ってないスートは除外
				if (i === this.trump) continue;				// トランプは除外
				const h = BridgeUtils.countHoners(dummyOriginal, i);
				if (h > 2) continue;
				if (honers === -1) {
					honers = h;
					honerSuit = i;
					continue;
				}
				if (honers === 0) {
					if (h > 0) {
						honers = h;
						honerSuit = i;
					}
					continue;
				}
				if (honers === 2) {
					if (h === 1) {
						honers = h;
						honerSuit = i;
					}
					continue;
				}
				if (h !== 1) { // h = 0 or 2
					honers = h;
					honerSuit = i;
				}
			}
			if (honers > -1) return honerSuit;
			
//			int maxHcpSuit = 0;
//			int maxHcpVal  = -1;
//			for (int i = 1; i < 5; i++) {
//				if (!dummyHand.containsSuit(i)) {
//					if (dummyHand.containsSuit(trump))
//						continue; // Dummyにラフされるスートは除外
//				}
//				if (!hand.containsSuit(i)) continue;	// 自分が持ってないスートは除外
//				if (isRuflis(dist, i)) continue; // ラフリスのスートは除外(ここにはこない)
//				if (i == trump) continue;	// トランプは除外
//				if (dummyHonerPoint[i] >= maxHcpVal) { // 同じ HCP では Major を優先させる
//					maxHcpVal  = dummyHonerPoint[i];
//					maxHcpSuit = i;
//				}
//			}
//			if (maxHcpSuit > 0)	return maxHcpSuit;
			// ラフリスのスートしかもっていない場合またはトランプしかない
			// ここにきて、下に抜ける
			
		} else { // 自分はＲＨＯ
			let honers = -1;
			let honerSuit = -1;
			for (let i = 1; i < 5; i++) {
				if (this.dummyHand.countSuit(i) === 0) { // ダミーがラフできるスートは除外
					if (this.dummyHand.countSuit(this.trump) > 0) continue;
				}
				if (this.hand.countSuit(i) === 0) continue;	// 持ってないスートは除外
				if (i === this.trump) continue;				// トランプは除外
				const h = BridgeUtils.countHoners(dummyOriginal, i);
				if (h > 1) continue;
				if (honers == -1) {
					honers = h;
					honerSuit = i;
					continue;
				}
				if (honers == 1) {
					if (h == 0) {
						honers = h;
						honerSuit = i;
						continue;
					}
				}
			}
			if (honers > -1) return honerSuit;
//			int minHcpSuit = 0;
//			int minHcpVal  = 100;
//			for (int i = 1; i < 5; i++) {
//				if (!dummyHand.containsSuit(i)) {
//					if (dummyHand.containsSuit(trump))
//						continue; // Dummyにラフされるスートは除外
//				}
//				if (!hand.containsSuit(i)) continue;	// 自分が持っていないスートは除外
//				if (isRuflis(dist, i)) continue; // ラフリスのスートは除外(ここにはこない)
//				if (i == trump) continue; // トランプは除外
//				if (dummyHonerPoint[i] <= minHcpVal) { // 同じ HCP では Major を優先させる
//					minHcpVal  = dummyHonerPoint[i];
//					minHcpSuit = i;
//				}
//			}
//			if (minHcpSuit > 0) return minHcpSuit;
			// ラフリスのスートしかもっていない場合またはトランプしかない
			//ここにきて、下に抜ける
		}
		//
		// (7)自分にウィナーの多いスート 追加(2002/09/21)
		//
		let maxWinner = -1;
		let maxWinnerSuit = -1;
		const winner = this.getWinners();
		
		for (let i = 1; i < 5; i++) {
			const winnerCount = winner.countSuit(i);
			if (winnerCount > maxWinner) {
				maxWinner		= winnerCount;
				maxWinnerSuit	= i;
			}
		}
		if (maxWinner > 0) return maxWinnerSuit;
		
		//
		// ラフリスのスートしかもっていない場合またはトランプしかない
		//
		const p = this.getMyHand();
		p.shuffle();
		const suit = p.children[0].suit; // 持っている任意のカードのスート
		p.arrange();
		
		return suit;
	}
	
	//
	// (1) ラフリスのスートを除外
	//        ラフリスのスートとは：
	//        ダミーにもディクレアラーにもトランプが残っている状況で
	//                （現在０枚と判明していないこと）        
	//        ダミーもディクレアラーもが現在０枚と判明しているスート              
	//
	/**
	 * @private
	 * @param {number[][][]} dist ディストリビューションの可能性
	 * @param {number} suit スーツ
	 * @returns {boolean} ラフリスか否か
	 */
	isRuflis(dist, suit) {
		const declarer = this.board.getDeclarer();
		const dummy = this.board.getDummy();
		
		const trump = this.board.getContract().suit;
		// ディクレアラーにトランプが確実に残っていない場合、ラフリスではない
		if (dist[declarer][ trump-1 ][ Utils.MAX ] === 0) return false;
		
		// ダミーに確実にトランプが確実に残っていない場合、ラフリスではない
		if (dist[dummy   ][ trump-1 ][ Utils.MAX ] === 0) return false;
		
		// ディクレアラーが持っている可能性がある場合、ラフリスではない
		if (dist[declarer][ suit-1 ][ Utils.MAX ] > 0) return false;
		
		// ダミーが持っている可能性がある場合、ラフリスではない
		if (dist[dummy   ][ suit-1 ][ Utils.MAX ] > 0) return false;
		
		return true;
	}
	
	/**
	 * スーツコントラクトの場合で、リードするスートが決まった場合のプレイを行います。
	 *  スート内でのカードの決め方を次の順位で決める                
	 *    （１）そのスートの中で、トップがウィナーならキャッシュ
	 *    （２）ＫＱからＫ
	 *    （３）ＱＪからＱ
	 *    （４）ＫＪＴ，ＪＴからＪ
	 *    （５）ＫＴ９，ＱＴ９、Ｔ９からＴ
	 *    （６）その他：トップオブナッシング
	 *                  現在２枚：上
	 *                  現在３枚：３枚目
	 *                  現在４枚以上→４枚目
	 * @param {number} suit スーツ
	 * @returns	{Card}
	 */
	choosePlayInSuitLead(suit) {
//console.log('choosePlayInSuitLead() this.hand='+this.hand.toString());
		const candidacy = this.hand.subpacket(suit);
		if (candidacy.children.length === 0)
			throw new Error("choosePlayInSuitLead で指定されたスート("+suit+")を持っていません");
		candidacy.arrange();
		
		// （１）そのスートの中で、トップがウィナーならキャッシュ
		const winner = this.getWinners(); //getWinnersInSuitLead();
//System.out.println("choosePlayInSuitLead(suit). winner = " + winner);
		const top = candidacy.children[0];
		if (winner.indexOf(top) > -1) return top;
		
		// （２）ＫＱからＫ
		if (BridgeUtils.patternMatch(this.hand, "KQ*", suit)) {
			return this.hand.peek(suit, Card.KING);
		}
		
		// （３）ＱＪからＱ
		if (BridgeUtils.patternMatch(this.hand, "QJ*", suit)) {
			return this.hand.peek(suit, Card.QUEEN);
		}
		
		// （４）ＫＪＴ，ＪＴからＪ
		if (BridgeUtils.patternMatch(this.hand, "KJT*", suit)) {
			return this.hand.peek(suit, Card.JACK);
		}
		if (BridgeUtils.patternMatch(this.hand, "JT*", suit)) {
			return this.hand.peek(suit, Card.JACK);
		}
		
		// （５）ＫＴ９，ＱＴ９、Ｔ９からＴ
		if (BridgeUtils.patternMatch(this.hand, "KT9*", suit)) {
			return this.hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(this.hand, "QT9*", suit)) {
			return this.hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(this.hand, "T9*", suit)) {
			return this.hand.peek(suit, 10);
		}
		
		// （６）その他：現在２枚：上
		//               現在３枚：３枚目
		//               現在４枚以上→４枚目
		// そのスートの第一回目のリードのときは
		// アナーのないときはトップオブナッシング
		// トップがアナーのときはこのままでよい。
		
		if (this.suitIsFirstTime(suit)) {
			if (this.bridgeValue(candidacy.children[0]) < 10)
				return candidacy.children[0];
		}
		
		switch (candidacy.children.length) {
		case 1:
		case 2:
			return candidacy.children[0];
		case 3:
			return candidacy.children[2];
		default:
			return candidacy.children[3];
		}
	}
	
	/**
	 * @private
	 * @param {number} suit スーツ
	 * @returns {boolean}
	 */
	suitIsFirstTime(suit) {
		for (let i = 0; i < this.board.getTricks(); i++) {
			const t = this.board.getAllTricks()[i];
			if (t.children.length === 0) continue;
			if (t.children[0].suit === suit) return false;
		}
		return true;
	}
	
	//*********************************************************************//
	//  ２番手のプレイ(スートフォローできる場合)                           //
	//*********************************************************************//
	
	
	/**
	 * ２番手では、
	 * ・ウィナーがあれば出す（複数あれば下から）
	 * ・なければローエスト
	 * @private
	 * @returns {Card}
	 */
	playIn2nd() {
		const suit = this.lead.suit;
		if (this.getDummyPosition() === Player.LEFT) {
			// LHO
			const follow = this.hand.subpacket(suit);
			follow.arrange();
			if (follow.children.length === 0)
				throw new Error("playIn2nd() で、LHO はスートフォローできなくなっています");
			const trump = this.board.getContract().suit;
			
			//
			// 和美アルゴリズム
			//
			const dummyFollow = this.dummyHand.subpacket(suit);
			if ( dummyFollow.children.length === 0 ) {
				// ダミーがフォローできない
				if ( (trump != Bid.NO_TRUMP)&& // スーツコントラクト
						(this.dummyHand.subpacket(trump).children.length > 0)&& // ダミーにトランプがある
						(this.bridgeValue(this.lead) <= 10))
					return follow.peek(); // ローエスト
				else
					return this.getCheepestWinner(follow, this.lead);
			}
			dummyFollow.arrange();
			if ( this.bridgeValue(dummyFollow.peek()) > this.bridgeValue(follow.children[0]) )
				// ダミーのローエスト ＞ 自分のハイエスト --> ローエスト
				return follow.peek();
			else if (this.bridgeValue(this.lead) > this.bridgeValue(dummyFollow.children[0]))
				// リード ＞ ダミーのハイエスト --> リードにチーペストに勝つ
				return this.getCheepestWinner(follow, this.lead);
			else if (dummyFollow.children.length === 1)
				// ダミーのカードが１枚
				return this.getCheepestWinner(follow, dummyFollow.peek());
			else if (this.bridgeValue(follow.children[0]) > this.bridgeValue(dummyFollow.children[0]))
				// 自分のハイエスト＞ダミーのハイエスト
				return this.getCheepestWinner(follow, dummyFollow.children[0]);
			else if ((this.bridgeValue(this.lead) > this.bridgeValue(dummyFollow.peek()))&&
						(this.bridgeValue(this.lead) >= 10))
				// リード＞＞ダミーのローエスト）＆（リードが１０以上）
				return this.getCheepestWinner(follow, this.lead);
			else
				return follow.peek(); // ローエスト
		} else {
			// RHO
			const winner = this.getWinners();
			
			const pack = this.hand.subpacket(suit);
			// win = pack.intersection(winner); のような実装が簡潔。
				
			winner.arrange();
			for (let i = winner.children.length - 1; i >= 0; i--) {
				const c = winner.children[i];
				if ( (pack.indexOf(c) > -1)&&(c.suit === suit) ) return c;
			}
			
			// ない
			pack.arrange();
			
			return pack.peek(); // ローエスト
		}
	}
	
	//*********************************************************************//
	//  ３番手のプレイ(スートフォローできる場合)                           //
	//*********************************************************************//
	
	/**
	 * ３番手では、
	 * ・ＲＨＯの場合、ハイエストを出す（ただしダミーと自分を合わせたカードで
	 *   シークエンスとなる時はその内で最下位を出す）
	 * @private
	 * @returns {Card}
	 */
	playIn3rd() {
		if (this.getDummyPosition() === Player.LEFT) {

			//
			// LHO
			//
			
			const follow = this.hand.subpacket(this.lead.suit);
			// follow できない場合はすでにラフ or ディスカードしているので、
			// 下の if では 0 になることはない。
			if (follow.children.length <= 1) return follow.peek();
			follow.arrange();
			
			const trump = this.board.getContract().suit; // NT(==5)のこともある
			
			const declarerPlay = this.board.getTrick().children[1];
//System.out.println("declarerPlay = " + declarerPlay);
			if ( (this.lead.suit !== trump)&&(declarerPlay.suit === trump) )
				// ディクレアラーがラフした
				return this.getSignal();
			
			const dummyFollow = this.dummyHand.subpacket(this.lead.suit);
			dummyFollow.arrange();
			
			if ( this.compare(declarerPlay, this.lead) > 0 ) {
				// ディクレアラーがプレイして、それが勝っている
				if (dummyFollow.children.length === 0) {
					// ダミーはフォローできない
					if ( this.compare(follow.children[0], declarerPlay) > 0 )
						// 自分のハイエスト ＞ ディクレアラーのプレイ
						return this.getCheepestWinner(follow, declarerPlay);
					else
						return this.getSignal();
				} else {
					// ダミーはフォローできる
					if ( (this.compare(dummyFollow.peek(), follow.children[0]) > 0)
							||(this.compare(declarerPlay, follow.children[0]) > 0) ) {
						// ダミーのローエスト＞自分のハイエスト
						//  or ディクレアラープレイ＞自分のハイエスト
						return this.getSignal();
					} else if ( this.compare(follow.children[0], dummyFollow.children[0]) > 0){
						return this.getCheepestWinner(follow,
								this.getStronger(declarerPlay, dummyFollow.children[0]) );
					} else {
						//ダミーのハイエスト＞自分のハイエスト＞ダミーのローエスト
						//＆自分のハイエスト＞ディクレアラー
						//ダミーが１枚のときはありえない
						//  → getcheepestwinner（自分の手,（ダミーのgetcheepestwinner（ダミー
						// の手、自分のハイエスト）の次に低いカード）とディクレアラーの大きい方）
//System.out.println("●●●●●●●●一般の場合になった●●●●●●●●");
//System.out.println("follow = " + follow);
//System.out.println("dummyFollow = " + dummyFollow);
//System.out.println("declarerPlay = " + declarerPlay);
//System.out.println("getCheepestWinner(dummyFollow, follow.peek(0)) = " + getCheepestWinner(dummyFollow, follow.peek(0)));
//System.out.println("getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))) = " + getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))));
//System.out.println("getStronger(getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))),declarerPlay) = "+getStronger(getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))),declarerPlay));
//System.out.println(getCheepestWinner(follow, getStronger(getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))), declarerPlay)));
						return this.getCheepestWinner(follow, 
							this.getStronger(
							this.getNextLowerCard(dummyFollow, this.getCheepestWinner(dummyFollow, follow.children[0])),
							declarerPlay
							)
						);
					}
				}
			} else {
				// リード＞ディクレアラー
				if (dummyFollow.children.length === 0) {
					return this.getSignal();
				} else if (dummyFollow.children.length === 1) {
					if ( this.compare(this.lead, dummyFollow.peek()) > 0)
						return this.getSignal();
					else return this.getCheepestWinner(follow, dummyFollow.peek());
				} else {
					//if   リード＞ダミーのハイエスト OR
					// ダミーのハイエスト＞リード＞自分のハイエスト
					//         →getsignal
					if (this.compare(this.lead, dummyFollow.children[0]) > 0)
						return this.getSignal();
					if ((this.compare(dummyFollow.children[0], this.lead) > 0)
							&&(this.compare(this.lead, follow.children[0]) >0 ))
								return this.getSignal();
					
					//ダミーのハイエスト＞リード＆ 自分のハイエスト＞リード
					if (this.compare(follow.children[0], dummyFollow.children[0]) > 0 )
						return this.getCheepestWinner(follow, dummyFollow.children[0]);
					//ダミーのハイエスト＞自分のハイエスト
					if (this.compare(this.getCheepestWinner(dummyFollow, this.lead),
								this.getCheepestWinner(dummyFollow, follow.children[0] )) >= 0)
						return this.getSignal();
					
					return this.getCheepestWinner(follow, this.getNextLowerCard(dummyFollow, this.getCheepestWinner(dummyFollow, follow.children[0])));
				}
			}
		} else {
			//
			// RHO(RHO３番手フォローの戦略)
			//
			const pack = this.hand.subpacket(this.lead.suit);
			pack.arrange();
			
			// １枚しかない場合はそのカードを出す
			if (pack.children.length === 1) return pack.peek();
			
			//
			let max = this.lead;
			const dummyPlay = this.board.getTrick().children[1];
			if (this.compare(this.lead, dummyPlay) < 0) max = dummyPlay;
			
			const highest = pack.children[0];
			if (this.compare(highest, max) < 0) {
				// 自分のハイエスト＜Max(リード、ダミーのプレイしたカード)
				return this.getSignal();
			} else {
				// 自分のハイエスト≧Max(リード、ダミーのプレイしたカード)
				const o = new Packet(this.board.openCards);
				o.add(this.hand);	// o = ダミーのカード、プレイされたカード、自分のハンド
				const cardA = this.getBottomOfSequence(o, highest);
				
				if (this.compare(dummyPlay, this.lead) < 0) {
					// ダミーのプレイしたカード ＜ リード (＜ 自分のハイエスト)
					if (this.compare(cardA, this.lead) <= 0) return this.getSignal();
					return this.getCheepestWinner(this.hand, cardA);	// 3rd hand high
				}
				if (this.compare(cardA, dummyPlay) <= 0)
					return this.getCheepestWinner(this.hand, dummyPlay);
				return this.getCheepestWinner(this.hand, cardA);
			}
		}
	}
	
	//*********************************************************************//
	//  ４番手のプレイ(スートフォローできる場合)                           //
	//*********************************************************************//
	/**
	 * ４番手では、一番安く勝つかローエスト
	 * @private
	 * @returns {Card}
	 */
	playIn4th() {
		const pack = this.hand.subpacket(this.lead.suit);
		pack.arrange();
		let play = null;
		
		for (let i = 0; i < pack.children.length; i++) {
			const virtual = new Trick(this.getTrick());
			virtual.add(pack.children[i]);
			if (this.isItOurSide(virtual.getWinner())) play = pack.children[i];
		}
		if (play === null) play = pack.peek();
		return play;
	}
	
	/**
	 * 指定したシート番号が自分たちサイドの場合、true
	 * @private
	 * @param {number} seat
	 * @returns {boolean}
	 */
	isItOurSide(seat) {
		return (((seat ^ this.mySeat) & 1) === 0);
	}
	
/*==========================================================
 *                  便  利  関  数  群
 *==========================================================
 */
	/**
	 * 自分のハンドの中でウィナーとなっているカードを抽出した Packet を返却します。
	 * ウィナーであることは、各スートにおいて今プレイされていないカードのうちもっとも
	 * 高いカードであることで判断します。
	 * 不確定な情報は使用しません。
	 * @private
	 * @returns {Packet} winner
	 */
	getWinners() {
		const afterDummy = (this.getDummyPosition() === Player.RIGHT);
		
		const result = new Packet();
		
		//
		// 現在残っているカード(winnerの候補)を抽出する
		// winner の候補は、現在プレイされていないカードと今場に出ているカードである
		// ただし、このオブジェクトがＲＨＯでしかもダミーがプレイし、このオブジェクトが
		// プレイしていないとき、winner の候補からダミーのハンドを除く。
		//
		const rest = this.board.openCards.complement();
		rest.add(this.getTrick());
		
		if ( (afterDummy)&&(this.board.getTrick().children.length > 0) ) {
			// RHO で、リードでない場合(ダミーのハンドを除く)
		} else {
			// LHO であるか、RHO だが自分からリードする場合
			rest.add(this.getDummyHand());
		}
		
		// 残りのカードを各スートに分ける
		const suits = [];
		
		for (let i = 0; i < 4; i++) {
			suits[i] = rest.subpacket(i+1);
			suits[i].arrange();	// 強いカードを小さいインデックスに
		}
		
		// 各スーツのウィナーを抽出する
		for (let i = 0; i < 4; i++) {
			for (let j = 0; j < suits[i].children.length; j++) {
				const winner = suits[i].children[j];
				if (this.hand.indexOf(winner) > -1) {
					result.add(winner); // 最高位を持っている場合、次の位も調べる
				} else {
					break;	// シークエンスが切れた場合、終了
				}
			}
		}
		
		result.arrange();
		return result;
	}
	
	/**
	 * ラフできるとき、ダミーのあとの３番手のときには、パートナーがウィナーをだしていたら
	 * ラフしない、を実現するためのウィナー抽出関数。
	 * @private
	 * @returns {Packet}
	 */
	getWinners2() {
		const afterDummy = (this.getDummyPosition() === Player.RIGHT);
		const result = new Packet();
		
		//
		// 残りのカード(Winner の候補)を抽出する
		//
		const rest = this.board.openCards.complement();
		rest.add(this.getTrick());
		
		if (!afterDummy) rest.add(this.getDummyHand());
		
		// 残りのカードを各スートに分ける
		const suits = [];
		
		for (let i = 0; i < 4; i++) {
			suits[i] = rest.subpacket(i+1);
			suits[i].arrange();	// 強いカードを小さいインデックスに
		}
		
		const hand2 = new Packet(this.hand);
		hand2.add(this.lead); // パートナーのリードを追加しておく
		
		// 各スーツのウィナーを抽出する
		for (let i = 0; i < 4; i++) {
			for (let j = 0; j < suits[i].children.length; j++) {
				const winner = suits[i].children[j];
				if (hand2.indexOf(winner) > -1) {
					result.add(winner); // 最高位を持っている場合、次の位も調べる
				} else {
					break;	// シークエンスが切れた場合、終了
				}
			}
		}
		
		result.arrange();
		return result;
	}
	
	/**
	 * ＮＴコントラクトの場合の自分たちの持っている絶対的なウィナーを考えます。
	 * 自分の手はすべて評価対象となりますが、パートナーの手はオープニングリードの
	 * 決まりから推定されるもののみが対象となります。
	 * @private
	 * @returns {Packet}
	 */
	getWinnersInNTLead() {
		const result = new Packet();
		
		// opened = すでに見えているカード
		//        = (ダミーハンド) ∪ (これまでプレイされたトリック)
		// 次の rest のために取得しています。
		const opened = this.board.openCards;
		
		// rest = まだプレイされていないカード(ダミーハンドを含む)
		const rest = opened.complement();
		rest.add(this.getDummyHand());
		
		// ours = {自分たちのウィナーになる可能性があるカード}
		//      := (自分のハンド) ∪ (O.L.から期待されるパートナーの現在のハンド)
		const ours = new Packet();
		ours.add(this.hand); // 自分のハンド
		ours.add(this.getExpectedCardsInNT()); // パートナーが持っていると期待されるカード
		
		// rest のカード全体で、各スートについて上から順に ours に入っているものが
		// NT におけるウィナーとなります。
		for (let suit = 1; suit < 5; suit++) {
			const restOfSuit = rest.subpacket(suit);
			restOfSuit.arrange(); // 上から順番に
			for (let i = 0; i < restOfSuit.children.length; i++) {
				const c = restOfSuit.children[i];
				if (ours.indexOf(c) > -1)
					result.add(c);
				else break; // ours にあるシークェンスが途切れた
			}
		}
		
		//
		// 今後の課題として、
		// ＮＴコントラクトではローカードのウィナーが重要で、これをカウントしたい。
		//
		
		//
		// 上記で言っていることは、ディクレアラーとダミーでショウアウトしたスートを
		// ウィナーとしてカウントしたい、パートナーの４ｔｈベストリードなどのシグナル
		// によって分かる枚数情報を使ってロングスートのローカードのウィナーをカウント
		// したい、という内容か？
		//
		
		return result;
	}
	
	/**
	 * @private
	 * @returns {Packet}
	 */
	getWinnersInSuitLead() {
		const result = new Packet();
		
		// opened = すでに見えているカード
		//        = (ダミーハンド) ∪ (これまでプレイされたトリック)
		// 次の rest のために取得しています。
		const opened = this.board.openCards;
//System.out.println("getWinnersInSuitLead . opened = " + opened);
		
		// rest = まだプレイされていないカード(ダミーハンドを含む)
		const rest = opened.complement();
		rest.add(this.getDummyHand());
		
		// ours = {自分たちのウィナーになる可能性があるカード}
		//      := (自分のハンド) ∪ (O.L.から期待されるパートナーの現在のハンド)
		const ours = new Packet();
		ours.add(this.hand); // 自分のハンド
		ours.add(this.getExpectedCardsInTrump()); // パートナーが持っていると期待されるカード
		
		// sideSuits
		const sideSuits = Utils.countDistribution(this.board, this.mySeat);

		// rest のカード全体で、各スートについて上から順に ours に入っているものが
		// Suit Contract におけるウィナーとなります。
		// ただし、Suit Contract では、ダミーとディクレアラーについてトランプが残って
		// いる可能性がある状態では、サイドスートの(最大)数までしかウィナーを認めません。
		
		// トランプが残っていない場合、制限をはずす
		const trump = this.board.getTrump();
//console.log('Contract:'+this.board.getContract().toString());
//console.log(`getWinersInSuitLead() sideSuits[${this.board.getDummy()}][${trump-1}][${Utils.MAX}]`);
		// ダミー
		if (sideSuits[this.board.getDummy()][trump-1][Utils.MAX] === 0) {
//System.out.println("getWinnersInSuitLead . dummy Trump is empty.");
			for (let suit = 1; suit < 5; suit++)
				if (suit !== trump)
					sideSuits[this.board.getDummy()][suit-1][Utils.MAX] = 13;
		}
		// ディクレアラー
		if (sideSuits[this.board.getDeclarer()][trump-1][Utils.MAX] === 0) {
			for (let suit = 1; suit < 5; suit++)
				if (suit != trump)
					sideSuits[this.board.getDeclarer()][suit-1][Utils.MAX] = 13;
		}
//System.out.println("getWinnersInSuitLead . rest = " + rest);
//System.out.println("getWinnersInSuitLead . ours = " + ours);

		for (let suit = 1; suit < 5; suit++) {
			const restOfSuit = rest.subpacket(suit);
			restOfSuit.arrange(); // 上から順番に
			
			// ウィナーとカウントできる最大数を計算します
			let maxWinnersOfSuit = restOfSuit.children.length;
			if (suit != trump) { // suit はサイドスート
				let tmp = sideSuits[this.board.getDummy()][suit-1][Utils.MAX];
				if (maxWinnersOfSuit > tmp) maxWinnersOfSuit = tmp;
				tmp = sideSuits[this.board.getDeclarer()][suit-1][Utils.MAX];
				if (maxWinnersOfSuit > tmp) maxWinnersOfSuit = tmp;
			}
//System.out.println("getWinnersInSuitLead.suit="+suit+"  maxWinnersOfSuit="+maxWinnersOfSuit);
			for (let i = 0; i < maxWinnersOfSuit; i++) {
				const c = restOfSuit.children[i];
				if (ours.indexOf(c) > -1)
					result.add(c);
				else break; // ours にあるシークェンスが途切れた
			}
		}
//System.out.println("getWinnersInsuitLead.result="+result);
		return result;
	}
	
	static NT_EXPECTED_PATTERN = [
		[ "T9*" ], // T lead
		[ "JT*" ],	// J lead
		[ "QJ9*", "QJT*" ],	// Q lead
		[ "KQT*", "KQJ*", "AKJT*", "AKQ*" ], // K lead
		[ "AKJTx*", "AKQxx*" ] ]; // A lead
	/**
	 * ＮＴコントラクトの場合のオープニングリードから推定されるパートナーの手を取り出します。
	 * パートナーがオープニングリーダーであった場合に、そのスートは一定のルール
	 * によってある優先順位に基づいてハンドパターンが推定されます。
	 * リードされたカードのバリューと推定されるハンドパターンは次の通りです。
	 * 先頭のものがより優先順位が高く設定されています。
	 *
	 *	{ "T9*" }, // T lead
	 *	{ "JT*" },	// J lead
	 *	{ "QJ9*", "QJT*" },	// Q lead
	 *	{ "KQT*", "KQJ*", "AKJT*", "AKQ*" }, // K lead
	 *	{ "AKJTx*", "AKQxx*" } }; // A lead
	 * @private
	 * @returns {Packet}	パートナーが持っていると推定されるカードのPacket
	 */
	getExpectedCardsInNT() {
		return this.getExpectedCardsImpl(SimplePlayer2.NT_EXPECTED_PATTERN);
	}
	
	/**
	 * パートナーの行ったオープニングリードから推定される
	 * 現在のパートナーハンドを返却します。現在の、とはオープニングリードから
	 * 推定されるハンドですでにプレイされたものは除外する、という意味です。
	 * オリジナルハンドで考えて、引数で示されるパターン文字列にあてはまるもの
	 * が抽出されます。
	 * @private
	 * @param {string[][]} pattern
	 * @returns {Packet}
	 */
	getExpectedCardsImpl(pattern) {
		const result = new Packet();
		
		const opening = this.board.playHist.getTrick(0); // null はありえない
		
		// 自分がオープニングリーダーの場合、情報はないため、
		// 空の Packet を返却する。
		if (opening.leader === this.mySeat) return result;
		
		// パートナーがオープニングリーダーであり、自分の番になっているため、
		// すでにオープニングリードは行われているはず
		const openingLead = opening.getLead();
		
		const value = openingLead.value;
		if ((value <= 9)&&(value >= 2)) return result; // ローカードのリードは何も期待できない
		
		const suit = openingLead.suit;
		
		let index = value - 10; // T=0, J=1, Q=2, K=3, A=4
		if (index < 0) index = 4; // ACE は value == 1 となっているため
		const handPattern = pattern[index];
		
		// 優先順位の高いものから順に推定
		let handPatternIndex = 0;
		
		//
		// パートナーとディクレアラーの手の Union を求める。
		// このアルゴリズムではこれをパートナーの持ちうる手とみなす。
		//
		// open = {場に出たカード(含ダミー)} ∪ (現在の自分のハンド)
		//  i.e. 自分が認識できているすべてのカード
		const open = new Packet(this.board.openCards);
		open.add(this.getMyHand());
		
		// これにこれまでプレイしたパートナーの手を合わせたものが Union
		//
		// rest = ￢open
		// i.e. 自分から見て未知のすべてのカード ( = パートナー ∪ ディクレアラー )
		const rest = open.complement();
		
		// これまでのトリックの中で、パートナーが出したものすべてを rest に加える
		// i.e. rest ＝ パートナーの初期ハンド ∪ 現在のディクレアラーハンド
		//           ⊃ パートナーの初期ハンド
		const trick = this.board.getAllTricks();
		for (let i = 0; i < this.board.getTricks(); i++) {
			for (let j = 0; j < trick[i].children.length; j++) {
				const seat = (trick[i].leader + j)%4;
				if (( (seat - this.mySeat + 6)%4 ) === 0)
					rest.add(trick[i].children[j]);
			}
		}
//System.out.println("expected card (NT/Suit) rest : " + rest);
		
		// パートナーの初期ハンドとしてありうるものを handPattern から探す
		for (handPatternIndex = 0; handPatternIndex < handPattern.length; handPatternIndex++) {
			if (BridgeUtils.patternMatch(rest, handPattern[handPatternIndex], suit)) break;
		}
		
		if (handPatternIndex === handPattern.length) return result; // 該当なし。空パケット返却
		
		// 該当ありのため、パターン文字列を result に加える(High Card のみ)
		const toAdd = handPattern[handPatternIndex];
		for (let i = 0; i < toAdd.length; i++) {
			const c = toAdd.charAt(i);
			
			// open に含まれているものは add しない (すでにパートナーがプレイしたもの)
			switch (c) {
			case 'A':
				if (open.indexOf(suit, Card.ACE) === -1)
					result.add(rest.peek(suit, Card.ACE));
				break;
			case 'K':
				if (open.indexOf(suit, Card.KING) === -1)
					result.add(rest.peek(suit, Card.KING));
				break;
			case 'Q':
				if (open.indexOf(suit, Card.QUEEN) === -1)
					result.add(rest.peek(suit, Card.QUEEN));
				break;
			case 'J':
				if (open.indexOf(suit, Card.JACK) === -1)
					result.add(rest.peek(suit, Card.JACK));
				break;
			case 'T':
				if (open.indexOf(suit, 10) === -1)
					result.add(rest.peek(suit, 10));
				break;
			default:
			}
		}
		return result;
	}
	
	static SUIT_EXPECTED_PATTERN = [
		[ "T9*", "KT9*", "QT9*" ], // T lead
		[ "JT*", "KJT*" ],	// J lead
		[ "QJ*" ],	// Q lead
		[ "KQ*" ], // K lead
		[ "A*" ] ]; // A lead
	
	/**
	 * スーツコントラクトの場合のオープニングリードから推定されるパートナーの手を取り出します。
	 * パートナーがオープニングリーダーであった場合に、そのスートは一定のルール
	 * によってある優先順位に基づいてハンドパターンが推定されます。
	 * リードされたカードのバリューと推定されるハンドパターンは次の通りです。
	 * 先頭のものがより優先順位が高く設定されています。
	 *
		{ "T9*", "KT9*", "QT9" }, // T lead
		{ "JT*", "KJT*" },	// J lead
		{ "QJ*" },	// Q lead
		{ "KQ*" }, // K lead
		{ "A*" } }; // A lead
	 * @private
	 * @returns {Packet} パートナーが持っていると推定されるカードからなる Packet
	 */
	getExpectedCardsInTrump() {
		return this.getExpectedCardsImpl(SimplePlayer2.SUIT_EXPECTED_PATTERN);
	}
	
	/**
	 * 指定された候補カードの集まりの中から、指定されたカードに勝てる値がチーペストな
	 * カードを取得する。どうしても勝てない場合、ローエストを返す。
	 * スートフォローについては考慮しておらず、値による評価しかしていない。
	 * @private
	 * @param {Packet} candidacy
	 * @param {Card} target
	 * @returns {Card}
	 */
	getCheepestWinner(candidacy, target) {
		const p = candidacy.subpacket(target.suit);
		if (target === null) return p.peek();
		p.arrange();
		if (p.indexOf(target) > -1) return target;
		
		let stronger = null;
		for (let i = 0; i < p.children.length; i++) {
			const c = p.children[i];
			if (this.bridgeValue(c) > this.bridgeValue(target)) stronger = c;
		}
		if (stronger === null) return p.peek(); // ローエスト
		return stronger;
	}
	
	/**
	 * 指定された Packet の中の、指定されたカードと同等の最低のカードを出す
	 * @private
	 * @param {Packet} candidacy
	 * @param {Card|number?} base
	 * @returns {Card}
	 */
	getBottomOfSequence(candidacy, base) {
		if (!base)
			return this.getBottomOfSequence_baseNull(candidacy);
		if (typeof base == 'number')
			return this.getBottomOfSequence_withSuit(candidacy, base);

		const p = candidacy.subpacket(base.suit);
		p.arrange();
		let c = base;
		for (let i = 1; i < p.children.length; i++) {
			const c2 = p.children[i];
			if (this.bridgeValue(c) - this.bridgeValue(c2) === 1) c = c2;
		}
		return c;
	}
	
	/**
	 * @private
	 * @param {Packet} candidacy
	 * @returns {Card}
	 */
	getBottomOfSequence_baseNull(candidacy) {
		if (candidacy.children.length === 0) return null;
		const p = new Packet(candidacy);
		p.arrange();
		let c = p.children[0];
		for (let i = 1; i < p.children.length; i++) {
			const c2 = p.children[i];
			if (this.bridgeValue(c) - this.bridgeValue(c2) === 1) c = c2;
		}
		return c;	
	}
	
	/**
	 * @private
	 * @param {Packet} candidacy
	 * @param {number} suit
	 * @returns {Card}
	 */
	getBottomOfSequence_withSuit(candidacy, suit) {
		return this.getBottomOfSequence(candidacy.subpacket(suit));
	}
	
	/**
	 * @private
	 * @param {Packet?} h
	 * @param {Card} c
	 * @returns {Card}
	 */
	getNextLowerCard(h, c) {
		if (c === void 0 && h instanceof Card) {
			c = h;
			h = this.hand;
		}
		const p = this.hand.subpacket(c.suit);
		p.arrange();
		const index = p.indexOf(c);
		// 元のソースでは、h 指定の場合、
		// index === p.children.length-1 の場合 null が返る実装だった。
		if ( index !== -1 || index === p.children.length-1 )
			throw new Error("ハンドに " + c + "が含まれていません");
		return p.children[index+1];
	}
	
	/**
	 * どの位置でも呼ばれる
	 * @private
	 * @returns {Card?} シグナルを考慮したプレイ
	 */
	getSignal() {
		// 自分のハンドから、リードと同じスートのカードを抽出する
		const follow = this.hand.subpacket(this.lead.suit);
		
		if (follow.children.length === 0) return null;
		if (follow.children.length === 1) return follow.peek();
		
		follow.arrange();
		const card1 = this.board.getTrick().children[0]; // == lead
		const card2 = this.board.getTrick().children[1];
		const trump = this.board.getContract().suit;
		
//オーバーテイクの場合を選出		
		if ((card1.suit === trump)||(card2.suit !== trump)) {
			// ディクレアラーはラフしていない
			if ((this.compare(follow.children[0], card1) > 0)
				&&(this.compare(card1, card2) > 0)) {
				// フォローのハイエスト＞card1＞card2
				let p = this.board.openCards.complement();
				p.add(this.dummyHand);
				p = p.complement().subpacket(this.lead.suit); // p = 今までプレイされたカード
				// 下の２行はすでにプレイされているので、pに含まれているはず
				//p.add(card1);
				//if (card2.getSuit() == card1.getSuit()) p.add(card2);
				
				const high = follow.children[0];
				let low;
				if (follow.children.length === 2) low = follow.children[1];
				else low = follow.children[2];
				
				let i;
				for (i = this.bridgeValue(low); i <= this.bridgeValue(high); i++) {
					let j = i;
					if (j === 14) j = 1;
					if (p.countValue(j) === 0) break;
				}
				if (i > this.bridgeValue(high)) {
					let c = follow.children[0];
					for (let j = 1; j < follow.children.length; j++) {
						const c2 = follow.children[j];
						if (this.bridgeValue(c) - this.bridgeValue(c2) === 1) c = c2;
					}
					//オーバーテイクしても損のない状況ではオーバーテイクする
					return c;
				}
			}
		}
		return follow.peek(); // ローエスト 
/*
//・カモンシグナルの場合
if  いままでそのスートがリードされたことはない//初めてそのスートがリードされ
た
    ＆（（packetにＡまたはＫを含む）  ＯＲ  （card1=K & packetにＱを含む））
        if packetに９以下のカードがある
             →９以下のカードの中で一番高いもの
         else //10以上しか持っていない
             →ローエストを返す

//・アンブロック（Ａ７からはＡをだすなど）LHOはしない（将来ＲＨＯにはさせた
い）
//・カウントシグナル （奇数枚ならロー、偶数枚ならハイ）今はしない
//・スーツプリファランスシグナル（リードのときに出す事が多いので、なし）
else
→  ローエストを返す
*/
	}

	/**
	 * lead に対するスートフォロー、トランプスートを考慮して２枚のカードの強さを比較します
	 * ただし、２枚とも同じスートのディスカードの場合、値が大きい方を強いとみなし、
	 * 違うスートのディスカードの場合は 0 を返却しています
	 * @private
	 * @param		{Card} a		比較対象のカード
	 * @param		{Card} b		比較対象のカード
	 * @returns		{number} 結果( (1) a > b  (-1) a < b  (0) a = b )
	 */
	compare(a, b) {
		if ((a === null)&&(b === null)) return 0;
		if (b === null) return 1;
		if (a === null) return -1;
		
		// 同じスートの場合
		// (２枚とも同じスートのディスカードの場合、値の大きい方が強い事となっている）
		if (a.suit === b.suit) {
			const av = this.bridgeValue(a);
			const bv = this.bridgeValue(b);
			if (av > bv) return 1;
			if (av === bv) return 0;
			return -1;
		}
		const trump = this.board.getContract().suit;
		
		// ラフの場合
		if (a.suit === trump) return 1;
		if (b.suit === trump) return -1;
		
		// スートフォローを見る
		if (a.suit === this.lead.suit) return 1;
		if (b.suit === this.lead.suit) return -1;
		
		//
		return 0;
	}
	
	/**
	 * 指定された２カードのうち、強い方を返却します。
	 * 強さの判定には compare(a,b) を使用します。
	 * @private
	 * @param		{Card} a		候補Ａ
	 * @param		{Card} b		候補Ｂ
	 * @returns		{Card} ２候補のうち、強いカード
	 */
	getStronger(a, b) {
		if (this.compare(a, b) > 0) return a;
		else return b;
	}
}

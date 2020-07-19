/* 依存 Card.js Player.js */
const startBridge = function() {
	const sim = document.getElementById('bridge-simulator');
	sim.innerHTML = `
    <div id="modal-overlay"></div>
    <div id="modal-content">
        <table class="full">
            <tr><td style="font-weight:bold; vertical-align: middle;">　問題を選んでね！　<img src="images/sumire_icon.gif" height="100%" style="vertical-align: middle;"></img></td></tr>
            <tr><td>
                <table class="full"><tr><td>
                <select name="select"></select>
                </td>
                <td><input type="button" value="開始する" id="startButton"></td></tr>
                </table>
            </td></tr>
            <tr><td><input type="button" value="今のプレイを自動再生する" id="videoButton"></td></tr>
            <tr><td><input type="button" value="同じハンドをもう一度プレイする" id="replayButton"></td></tr>
        </table>
	</div>
	<canvas id="canvas" width="640" height="480"></canvas>
`;

	( async () => {
		const m = new PlayMain('canvas');
		
		while (true) {
			await m.start();
		}
	})();
};

/**
 * アドホックなメインプログラムです。だんだん本格的になってきました。
 * Java 版の AppTest, PracticeModePlayMain, PracticeApplet の機能を持っています。
 *
 * @version		a-release		19, June 2020
 * @author		Yusuke Sasaki
 */
class PlayMain {
	/** @constant {number[]} バルの出現順序(PracticeMode用) */
	static VUL = [ Board.VUL_NONE, Board.VUL_NS, Board.VUL_EW, Board.VUL_BOTH,
					Board.VUL_NS, Board.VUL_EW, Board.VUL_BOTH, Board.VUL_NONE ];
	/** @constant {number} 練習用ボード数(MAX 8) */
	static BOARDS = 4;

    /** @type {string} */ canvasId;
    /** @type {Field} */ field;
    /** @type {Board} */ board;
    /** @type {Player[]} */ players;
    /** @type {number} */ handno;
    /** @type {string} */ contractString;
	/** @type {Problem[]} problems.length == 0 のとき PracticeMode */ problems;
    /** @type {Sumire} */ sumire;
	/** @type {Button} */ quit;
    /** @type {Button} */ dd;
	/** @type {Button} */ textWindow;
	/** @type {SelectDialog} */ dialog;
    /** @type {YesNoDialog} */ confirmDialog;
	/** @type {boolean} */ exitSignal;
	/** @type {boolean} PracticeMode か */ isPractice;

	/** @type {number} practice mode の時の totalScore */ totalScore;
	/** @type {number} practice mode の時の board number */ boardNum;
	
	/**
	 * PlayMain オブジェクトを生成します。
	 */
	constructor(canvasId) {
		this.canvasId = canvasId;
		this.problems = [];

		this.dialog = new SelectDialog();
		this.field = new Field(this.canvasId);
        
		// ボタン
		this._placeQuitButton_();
		this._placeDDButton_();
		//this._placeTextButton_();

		// 問題を登録
		this.totalScore = 0;
		this.boardNum = 0;
		try {
			problem.forEach( p => this.addProblem(Problem.regular(p)));
		} catch (e) {
			this.isPractice = true;
			if (!e instanceof ReferenceError) console.log(e);
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * はじめに表示されるダイアログに問題を追加します。
	 * valid でない問題は追加されません。
     * @param   {Problem} p     追加する問題
	 */
	addProblem(p) {
		if (p.isValid()) this.problems.push(p);
	}
	
	/**
	 * 中断ボタン(this.quit)を生成、配置します。
	 * @private
	 */
	_placeQuitButton_() {
		this.quit = new Button(this.field, "中断");
		this.quit.setBounds(540, 30, 80, 24);
		this.field.add(this.quit);
		this.quit.setListener( () => {
			if (this.isPractice) {
				if (window.confirm('これまでのプレイ結果を破棄して中断しますか？')) {
					this.totalScore = 0;
					this.boardNum = -1;
					this.field.interrupt();
				}
			} else if (window.confirm('このボードを破棄して中断します')) {
				this.field.interrupt(); // OK 押下
			}
		});
		}

	/**
	 * ダブルダミーボタン(this.dd)を生成、配置します。
	 * this.dd には doubleDummy {boolean} プロパティがあります。
	 * @private
	 */
	_placeDDButton_() {
		const dd = new Button(this.field, "ダブルダミー"); // ダブルダミー
		dd.doubleDummy = false;
		this.field.add(dd);
		dd.setBounds(540, 58, 80, 24);
		dd.setListener( () => {
			if (this.board.status != Board.PLAYING) return;
			//
			dd.doubleDummy = !dd.doubleDummy;
			this._ddSet_(dd.doubleDummy);
		});
		this.dd = dd;
	}

	/**
	 * ダブルダミーボタンの状態を変更します。
	 * @private
	 * @param {boolean}} doubleDummy ダブルダミーボタンがダブルダミー状態か
	 */
	_ddSet_(doubleDummy) {
		this.dd.doubleDummy = doubleDummy;
		this.dd.caption = doubleDummy?'通常に戻す':'ダブルダミー';
		this.board.getHand(Board.EAST).turn(doubleDummy);
		this.board.getHand(Board.WEST).turn(doubleDummy);
		this.field.draw();
	}

	/**
	 * テキスト表示ボタン(this.textWindow)を生成、配置します。
	 * @private
	 */
	_placeTextButton_() {
		this.textWindow = new Button(this.field, "テキスト表示"); // テキスト表示
		this.field.add(this.textWindow);
		this.textWindow.setBounds(540, 86, 80, 24);
		this.textWindow.setListener( () => {
			window.confirm(this.board.toText()); // フォントがprop/下が切れる
		});
	}
	
	/**
	 * Board の初期化を行います。
	 * @async
	 */
	async start() {
		this.field.draw();
		// 問題が設定されていない場合、practice mode

		if (this.isPractice) {
			this.totalScore = 0;
			this._makeRandomHand_();
			this._ddSet_(false);
		} else {
			const titles = [];
			this.problems.forEach( prob => titles.push(prob.title));
			this.dialog.setPulldown(titles);
			// 前のボードがない場合、リプレイ/ビデオは無効化
			const disabled = (this.board)?false:true;
			this.dialog.replayButton.disabled = disabled;
			this.dialog.videoButton.disabled = disabled || (this.board.status !== Board.SCORING);

			const result = await this.dialog.show();
			
			if ("video" ==  result) {
				this._makeVideohand_();
				this._ddSet_(true);
				this.boardNum = (this.boardNum + PlayMain.BOARDS - 1) % PlayMain.BOARDS;
			} else if ("replay" == result) {
				this._makeLasthand_();
				this._ddSet_(false);
				this.boardNum = (this.boardNum + PlayMain.BOARDS - 1) % PlayMain.BOARDS;
			} else {
				this.handno = parseInt(result.substring(4));
				this._makeNewhand_();
				this._ddSet_(false);
			}
		}
		// field に board を追加
		CardImageHolder.setBackImage(this.boardNum%4);
		this.field.add(this.board);
		this.board.setPosition(0, 0);
		this.board.setDirection(0);
		this.field.draw();
		
		await this.main();

		// field から board を削除
		this.field.pull(this.board);

		// boardNum を増やす(カード裏面画像も変わる)
		this.boardNum++;
		if (this.boardNum === PlayMain.BOARDS) {
			this.totalScore = 0;
			this.boardNum = 0;
		}

	}
	
	/**
	 * ダイアログで新しいハンドを選択したときの処理です。
	 * @private
	 */
	_makeNewhand_() {
		const prob = this.problems[this.handno];
		
		this.board = new Board(1);
		this.board.name = prob.title;
		
		// Player 設定
		this._setPlayers_(prob);
		
		// ディール
		const hands = prob.createHands();
		
		this.board.deal(hands);
		
		// コントラクト設定を行う
		this.board.setContract(prob.contract, Board.SOUTH);
	}

	/**
	 * プレイにおけるプレイヤーを thinker に基づき設定します。
	 * @param	{Problem} prob 問題
	 */
	_setPlayers_(prob) {
		const thinker = (this.isPractice)?problem:prob.thinker;
		this.players = [];
		this.players[Board.NORTH] = new RandomPlayer(this.board, Board.NORTH);
		this.players[Board.SOUTH] = new HumanPlayer(this.board, this.field, Board.SOUTH);
		// Computer Player 設定
		if ( !thinker || thinker != "DoubleDummyPlayer") {
			this.players[Board.EAST ] = new SimplePlayer2(this.board, Board.EAST);
			this.players[Board.WEST ] = new SimplePlayer2(this.board, Board.WEST, prob.openingLead);
		} else if (thinker == "DoubleDummyPlayer") {
			this.players[Board.EAST ] = new ReadAheadPlayer(this.board, Board.EAST);
			this.players[Board.WEST ] = new ReadAheadPlayer(this.board, Board.WEST, prob.openingLead);
		} else {
			this.players[Board.EAST ] = new RandomPlayer(this.board, Board.EAST);
			this.players[Board.WEST ] = new RandomPlayer(this.board, Board.WEST);
		}
	}
	
	/**
	 * 前のプレイを自動再生するプレイヤーを設定します。
	 * @private
	 */
	_makeVideohand_() {
		const oldBoard = this.board;
		this.board = new Board(1);
		this.board.name = oldBoard.name;
		
		// Player 設定
		this.players = [];
		for (let i = 0; i < 4; i++)
			this.players.push(new VideoPlayer(this.board, oldBoard, i));
		
		// ディール
		const hands = BridgeUtils.calculateOriginalHand(oldBoard);
		
		this.board.deal(hands);
		
		// コントラクト設定を行う
		this.board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	/**
	 * 前回と同じハンドを設定します。
	 * @private
	 */
	_makeLasthand_() {
		const prob = this.problems[this.handno];
		const oldBoard = this.board;
		this.board = new Board(1);
		this.board.name = oldBoard.name;
		
		// Player 設定
		this._setPlayers_(prob);

		// ディール
		const hands = BridgeUtils.calculateOriginalHand(oldBoard);
		
		this.board.deal(hands);
		
		// コントラクト設定を行う
		this.board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	/**
	 * ダイアログで新しいハンドを選択したときの処理です。
	 * @private
	 */
	_makeRandomHand_() {
		const prob = Problem.random(this.boardNum);
		this.problems = [prob];
		this.handno = 0;
		
		this.board = new Board(1);
		this.board.name = prob.title;
		
		// Player 設定
		this._setPlayers_(prob);
		
		// ディール
		const hands = prob.createHands();
		
		this.board.deal(hands);
		
		// コントラクト設定を行う
		this.board.setContract(prob.contract, Board.SOUTH);
	}

	/**
	 * 始めのすみれによる説明を表示する
	 * @async
	 * @throws	QuitInterruptException 中断が選択された
	 */
	async explain() {
		const prob = this.problems[this.handno];
		this.sumire = new Sumire(this.field, prob.description);
		this.contractString = prob.getContractString();
		await this.sumire.animate(Sumire.NORMAL);
	}
	
	/**
	 * メインループ
	 * @async
	 * @throws	QuitInterruptException 中断が選択された
	 */
	async mainLoop() {
		if (this.dd.doubleDummy) {
			this.board.getHand(Board.EAST).turn(true);
			this.board.getHand(Board.WEST).turn(true);
		}
		
		while (true) {
			// Spot を指定する
			this.field.spot = this.board.getTurn();
			this.field.draw();
			
			let c = null;
			while (c === null) {
				c = await this.players[this.board.getPlayer()].play(); // ブロックする
			}
			await this.board.playWithGui(c);
			this.field.draw();
			
			if (this.board.status === Board.SCORING) break;
		}
	}
	
	/**
	 * 元のハンドの表示、スコアの表示を行う
	 * @async
	 * @throws	QuitInterruptException 中断が選択された
	 */
	async displayScore() {
		this.field.spot = -1; // spot を消す
		this.field.draw();
		
		await this.field.sleep(500);

		// カードをもう一度表示する
		const original = BridgeUtils.calculateOriginalHand(this.board);
		for (let i = 0; i < 4; i++) {
			const hand = this.board.getHand(i);
			for (let j = 0; j < original[i].children.length; j++) {
				const c = original[i].children[j];
				hand.add(c);
			}
			hand.turn(true);
		}

		this.board.selfLayout();
		this.board.getHand().forEach( hand => { hand.arrange(); hand.selfLayout(); });
	
		// スコア表示
		let msg = "結果：" + this.contractString + "  ";
		let msg2;
		// メイク数
		const win = BridgeUtils.countDeclarerSideWinners(this.board);
		const up = win - this.board.getContract().level - 6;
		const make = win - 6;
		
		if (up >= 0) {
			// メイク
			msg += make + "メイク";
			msg2 = "おめでとう！！";
		} else {
			// ダウン
			msg += (-up) + "ダウン";
			msg2 = (this.isPractice)?"残念。":"残念。もう一度がんばって！";
		}
		const score = Score.calculate(this.board, Board.SOUTH);
		msg += "("+win+"トリック)\nN-S側のスコア："+ score + "\n";
		if (this.isPractice) {
			this.totalScore += score;
			msg += "スコア累積:" + this.totalScore;
		}
		msg += "\n" + msg2;
		
		this.sumire = new Sumire(this.field, msg);
		if (up >= 0) 
			await this.sumire.animate(Sumire.DELIGHTED);
		else
			await this.sumire.animate(Sumire.SAD);
		this.field.draw();
		this.board.getHand().forEach( h => { while (h.children.length > 0) h.pull(); });
	}
	
	/**
	 * メインメソッドです。
	 * @async
	 */
	async main() {
		try {
			await this.explain();
			await this.mainLoop();
			await this.displayScore();
			this.field.pull(this.board);
		} catch (e) {
			if (e instanceof QuitInterruptException) {
console.log('quit inspected');
				this.field.spot = -1;
				this.field.draw();
			} else {
				throw e;
			}
		}
	}
}

class Problem {
	/** @type {string} 問題タイトル */ title;
	/** @type {Bid} コントラクト */ contract;
	/** @type {string[]} 4人のハンド文字列 */ hands;
	/** @type {string} 問題説明文 */ description;
	/** @type {string} O.L. */ openingLead;
	/** @type {string} 思考ルーチン */ thinker;

	/**
	 * 4  SXX などのコントラクト文字列を返却します。
	 * @returns		{string} コントラクト文字列
	 */
	getContractString() {
		if (!this.contract) return "null";
		return this.contract.toString().substring(1, 7);
	}

	isValid() {
		// ★★　未実装　★★
		return true;
	}

	/**
	 * 指定されたパラメータで問題を作成します。
	 * 
	 * @param {object} problem {string} title 問題の表題,
	 * {string} contractStr "4SXX" のようなコントラクトを示す文字列,
	 * {string} n, e, s, w hands ハンド文字列,
	 * {string} description 問題の説明,
	 * {string} openingLead オープニングリード指定(指定なしは undefined or null),
	 * {string} thinker 思考アルゴリズム名(指定なしは undefined or null)
	 * @returns	{Problem} 問題
	 */
	static regular(problem) {
		let p = new Problem();
		p.title = problem.title;
		p.description = problem.description;
		p.openingLead = problem.openingLead;
		p.thinker = problem.thinker;
		// コントラクト文字列の解釈
		const level = parseInt(problem.contract[0]);
		let kind = Bid.BID;
		if (problem.contract.endsWith("XX")) kind = Bid.REDOUBLE;
		else if (problem.contract.endsWith("X")) kind = Bid.DOUBLE;
		const s = problem.contract.substring(1);
		let suit = Bid.CLUB;
		if (s.startsWith('D')) suit = Bid.DIAMOND;
		if (s.startsWith('H')) suit = Bid.HEART;
		if (s.startsWith('S')) suit = Bid.SPADE;
		if (s.startsWith('NT')) suit = Bid.NO_TRUMP;

		p.contract = new Bid(kind, level, suit);

		p.hands = [problem.n, problem.e, problem.s, problem.w];
		return p;
	}

	/**
	 * この問題のハンドを生成します。
	 * @returns	{Packet[]} この問題のハンド
	 */
	createHands() {
		const pile = Packet.provideDeck();
		const hs = [];
		for (let i = 0; i < 4; i++) hs.push(new Packet());
		
		for (let i = 0; i < 4; i++) Problem.draw(pile, hs[i], this.hands[i]);
		
		// 残りはランダムに配る
		if (pile.children.length > 0) {
			pile.shuffle();
			for (let i = 0; i < 4; i++) {
				for (let j = hs[i].children.length; j < 13; j++) {
					hs[i].add(pile.pull());
					// 途中で pile が尽きることもある
					// その場合は、isValid() で false を返すことになる
				}
			}
		}
		hs.forEach( h => h.arrange() );
		return hs;
	}

	/**
	 * 文字列で与えられたハンド情報を解釈して hand に設定します。
	 * @param	{Packet} pile カードの山
	 * @param	{Packet} hand 設定先のハンド
	 * @param	{string?} str ハンド文字列
	 */
	static draw(pile, hand, str) {
		if (!str) return;
		let suit = Card.SPADE;
		
		for (let i = 0; i < str.length; i++) {
			const c = str.charAt(i);
			
			if (c == 'S') {
				suit = Card.SPADE;
			} else if (c == 'H') {
				suit = Card.HEART;
			} else if (c == 'D') {
				suit = Card.DIAMOND;
			} else if (c == 'C') {
				suit = Card.CLUB;
			} else if (c == 'K') {
				hand.add(pile.pull(suit, Card.KING));
			} else if (c == 'Q') {
				hand.add(pile.pull(suit, Card.QUEEN));
			} else if (c == 'J') {
				hand.add(pile.pull(suit, Card.JACK));
			} else if (c == 'T') {
				hand.add(pile.pull(suit, 10));
			} else if (c == 'A') {
				hand.add(pile.pull(suit, Card.ACE));
			} else if ( (c >= '2')&&(c <= '9') ) {
				hand.add(pile.pull(suit, parseInt(c) ));
			}
		}
	}

	/**
	 * ランダムな問題を作成します。
	 * 
	 * @param {number} boardNum ボード番号
	 * @param {number?} seed 指定した場合、ReproducibleRandom を seed で初期化します
	 * @returns	{Problem} 問題
	 */
	static random(boardNum, seed) {
		if (seed) ReproducibleRandom.setSeed(seed);
		/** @type {Problem} */ const p = new Problem();
		//
		// 1. まず、ランダムにハンドを配る
		//
		const pile = Packet.provideDeck();
		
		pile.shuffle();
		p.hands = [];
		for (let i = 0; i < 4; i++) p.hands.push(new Packet());
		Packet.deal(pile, p.hands, 0);
		for (let i = 0; i < 4; i++) p.hands[i].arrange();
		
		//
		// 2. スートごとの枚数、HCP、NS側のトリック数を基礎情報として計算する
		//
		const a = Problem.calculateAttributes(p.hands);
		
		//
		// 3. デノミネーション、サイドをトリック数から決定する
		//
		
		// 最大と最小の幅を計算する
		let maxNSTricks = -1;
		let maxDenom 	= -1;
		let minNSTricks = 1400;
		let minDenom	= -1;
		for (let i = 0; i < 5; i++) {
			if (a.trick[i] > maxNSTricks) {
				maxNSTricks = a.trick[i];
				if (i < 4) maxDenom = i + 1;
			}
			if (a.trick[i] < minNSTricks) {
				minNSTricks = a.trick[i];
				if (i < 4) minDenom = i + 1;
			}
		}
		let NSorEW = -1;
		/** @type {number} */ let denomination;
console.log("min " + minNSTricks + "  max " + maxNSTricks);
		if (maxNSTricks - minNSTricks <= 100) {
			denomination = Bid.NO_TRUMP;
			if (a.trick[Bid.NO_TRUMP] > 650) NSorEW = 0;	// NS側コントラクト
			else NSorEW = 1;
		} else {
			if (maxNSTricks > 1300 - minNSTricks) {
				NSorEW = 0;
				denomination = maxDenom;
			} else {
				NSorEW = 1;
				denomination = minDenom;
			}
		}

		//
		// 4. ディクレアラーを決定する
		//
		/** @type {number} */ let declarer;
		if (denomination === Bid.NO_TRUMP) {
			// HCP の大きい方をディクレアラーとする
			declarer = (a.hcp[NSorEW] > a.hcp[NSorEW + 2])?NSorEW:NSorEW + 2;
		} else {
			// スートコントラクトのときは、トランプの長い方
			declarer = (a.count[NSorEW][denomination-1] > a.count[NSorEW+2][denomination-1])?NSorEW:NSorEW + 2;
		}
		
		//
		// 5. レングスポイント、ダミーポイントを計算する
		//
		a.pts = Problem.calcPoints(denomination, declarer, NSorEW, a);
		
		//
		// 6. 点数レンジでレベルを決める
		//
		const totalPt = a.pts[NSorEW] + a.pts[NSorEW+2];
console.log("Total Point : " + totalPt);
		let level;
		if (totalPt > 36) {
			// グランドスラム
			level = 7;
		} else if (totalPt > 32) {
			// スモールスラム
			level = 6;
		} else if (totalPt > 29) {
			// ５の代
			level = 5;
		} else if (totalPt > 26) {
			// ４の代
			level = 4;
		} else if (totalPt > 23) {
			// ３の代
			level = 3;
		} else if (totalPt > 21) {
			// ２の代
			level = 2;
		} else {
			// １の代
			level = 1;
		}
		let kind = Bid.BID; // ダブルなし
		
		let tr = Math.floor(a.trick[denomination-1]/100);
		if (tr < 7) tr = 13 - tr;
		if (level + 5 > tr) kind = Bid.DOUBLE;
		p.contract = new Bid(kind, level, denomination);
		
		//
		// 7. ディクレアラーを SOUTH になるようにハンドを回転させる
		//
		for (let i = 0; i < ((declarer - Board.SOUTH) + 4) % 4; i++) {
			const tmp = p.hands[0];
			for (let j = 0; j < 3; j++) {
				p.hands[j] = p.hands[j+1];
			}
			p.hands[3] = tmp;
		}
		for (let i = 0; i < 4; i++) p.hands[i] = p.hands[i].toString();
		
		//
		// 8. 説明文をつくる
		//
		p.description = "あなたの " + p.getContractString() + " よ。\n切り札は";
		switch (denomination) {
		case Bid.NO_TRUMP:	p.description += "ありません。";	break;
		case Bid.SPADE:		p.description += "スペード、";	break;
		case Bid.HEART:		p.description += "ハート、";		break;
		case Bid.DIAMOND:	p.description += "ダイアモンド、";break;
		case Bid.CLUB:		p.description += "クラブ、";		break;
		default:			p.description += "なんでしょう。";break;
		}
		
		p.description += "\n13トリックのうち、" + (level + 6) + "トリック以上とってね";
		p.title = "Board " + (boardNum+1);
		return p;
	}

	/**
	 * スートごとの枚数、HCPを計算します
	 * @private
	 * @param {Packet[]} hands ハンド
	 * @returns {Object}
	 */
	static calculateAttributes(hands) {
		const a = {};
		// スートごとの枚数をカウントする
		// 各スートについて、
		a.count = [];
		for (let i = 0; i < 4; i++) {
			a.count[i] = [];
			for (let suit = 1; suit < 5; suit++) {
			// それぞれの枚数を数える
				a.count[i][suit-1] = hands[i].countSuit(suit);
			}
		}
		
		// HCPを計算する
		a.hcp = [];
		for (let i = 0; i < 4; i++) {
			a.hcp[i] = BridgeUtils.countHonerPoint(hands[i])[0];
		}
		
		// デノミネーションごとのトリック数を計算する。
		a.trick = [];
		for (let denomination = 1; denomination < 6; denomination++) {
			const b = new Board(1);
			b.deal(hands);
			b.setContract(new Bid(Bid.BID, 1, denomination), Board.SOUTH);
			
			const ob = new OptimizedBoard(b);
			a.trick[denomination-1] = 1300 - ob.calcApproximateTricks();
			// NS側のトリック * 100 とする
console.log(" denom : " + denomination + "  Tricks : " + a.trick[denomination-1]);
		}
		return a;
	}

	/**
	 * デノミネーション、ディクレアラーが決まった後で
	 * レングスポイント、ダミーポイントを計算する。
	 * @private
	 * @param {number} denomination
	 * @param {number} declarer
	 * @param {number} NSorEW
	 * @param {Object} a count を保有するオブジェクト
	 * @returns {number[]} pts
	 */
	static calcPoints(denomination, declarer, NSorEW, a) {
		const pts = [];
		//
		// レングスポイントなど計算する
		//
		for (let i = NSorEW; i < 4; i+=2) {
			pts[i] = a.hcp[i];
		}
		
		if (denomination !== Bid.NO_TRUMP) {
			// レングスポイントを加算する
			// レングスポイントは、NT/Suitコントラクトで (スートの枚数)-4 が正のとき
			// この数値を加算します(5枚スート..1pts  6枚スート..2pts ……)
			// トランプスートは、次の評価式とする
			// FP（フィットポイント・・和美の造語＝（フィット枚数－8）＊1.5
			for (let i = NSorEW; i < 4; i+=2) {
				for (let suit = 1; suit < 5; suit++) {
					if (suit === denomination) continue;
					const cnt = a.count[i][suit-1];
					if (cnt > 4) pts[i] += (cnt - 4);
				}
			}
			const fit = a.count[NSorEW][denomination-1] + a.count[NSorEW+2][denomination-1];
			if (fit > 8) pts[NSorEW] += Math.floor(((fit - 8) * 3 / 2));
			// NorEのみに加点しているが、トランプのときはtotalPointしか評価しない
			// ディクレアラーを決めるのに長さを使用しているのみ
		
			// ダミーポイント
			const dummy = (declarer + 2) % 4;
			const dummyTrumps = a.count[dummy][denomination-1];
			for (let suit = 1; suit < 5; suit++) {
				const cnt = a.count[dummy][suit-1];
				switch (cnt) {
				
				case 0:	// void
					pts[dummy] += Math.min(5, dummyTrumps*3);
					break;
				case 1: // singleton
					pts[dummy] += Math.min(3, (dummyTrumps-1)*3);
					break;
				case 2: // doubleton
					pts[dummy] += Math.min(1, (dummyTrumps-2)*3);
				default:	// fall through
				}
			}
		}
		return pts;
	}

}

/**
 * ゲーム選択ダイアログを生成します。
 * このクラスは bridge.css とともに使う必要があります。
 */
class SelectDialog {
	modalContent;
	modalOverlay;
	select;
	startButton;
	videoButton;
	replayButton;

	/**
	 * document に、ダイアログを追加します。
	 */
	constructor(titles) {
		this.modalContent = document.getElementById('modal-content');
		this.modalOverlay = document.getElementById('modal-overlay');
		if (!this.modalContent || !this.modalOverlay)
			throw new Error("html error. without modal-content or modal-overlay");
		const s = document.getElementsByName('select');
		if (s.length != 1) throw new Error("html doesn't have select");
		this.select = s[0];
		this.startButton = document.getElementById('startButton');
		this.videoButton = document.getElementById('videoButton');
		this.replayButton = document.getElementById('replayButton');
	}
/*------------------
 * instance methods
 */
	/**
	 * プルダウンに指定された文字列を設定します。
	 * @param	{string[]} titles プルダウンに設定する文字列
	 */
	setPulldown(titles) {
		// select の子要素を削除
		while (this.select.firstChild) { this.select.removeChild(this.select.firstChild); }
		for (let i = 0; i < titles.length; i++) {
			const opt = document.createElement('option');
			opt.setAttribute('value', 'prob'+(i+1));
			opt.innerHTML = titles[i];
			this.select.appendChild(opt);
		}
	}

	/**
	 * モーダルの選択画面を表示し、ボタン押下を待ちます。
	 * async としての返り値は文字列で、"prob{num}", "video", "replay" のいずれかです。
	 * 同時に選択画面上の各ボタンのハンドラを登録します。
	 * @async
	 * @returns	{Promise<string>} "prob{num}", "video", "replay"
	 */
	show() {
		let startListener, videoListener, replayListener;
		return new Promise( (res) => {
			startListener = () => {	res('prob'+this.select.selectedIndex); };
			videoListener = () => {	res('video'); };
			replayListener = () => { res('replay'); };
				// イベントハンドラを登録しておく
			this.startButton.addEventListener('click', startListener);
			this.replayButton.addEventListener('click', replayListener);
			this.videoButton.addEventListener('click', videoListener);

			// モーダル画面を表示する
			this.modalContent.style.display = 'inline';
			this.modalOverlay.style.display = 'inline';
		}).then( (val) => new Promise( (res) => {
			this.startButton.removeEventListener('click', startListener);
			this.videoButton.removeEventListener('click', videoListener);
			this.replayButton.removeEventListener('click', replayListener);
			// モーダル画面を隠す
			this.modalContent.style.display = 'none';
			this.modalOverlay.style.display = 'none';
			res(val);
		}));
	}
}

/**
 * ブリッジシミュレータにおける開始時の説明の絵を生成する Entity です。
 * この Entity には、コントラクトの内容などの説明書きが表示されます。
 *
 */
class Sumire extends Entity {
	static NORMAL = 0;
	static DELIGHTED = 1;
	static SAD = 2;
	
	static FONT = 'normal 14px SanSerif';
	/** @type {number} 次の行までのステップ */
	static Y_STEP = 20;
	static MSG_COLOR = 'rgb(255, 255, 200)';
	static BACK_COLOR = 'rgba(200, 255, 200, 0.5)';
	
	/** @type {Field} */
	field;
	/** @type {string[]} */
	lines;
	/** @type {number} */
	picNumber;
	/** @type {number} */
	face;
	
	/** @type {number} 描画を開始するｘ座標 */
	x0;
	/** @type {number} 描画を開始するｙ座標 */
	y0;
	
	
	/** @type {number} 文字部分の幅 */
	width;
	/** @type {number} 文字部分の高さ */
	height;
	
	/** @type {number} ふきだしの幅 */
	mw;
	/** @type {number} ふきだしの高さ */
	mh;
	
	/** @type {number[]} ふきだしの頂点x座標 */
	xp;
	/** @type {number[]} ふきだしの頂点y座標 */
	yp;
	
/*-------------
 * Constructor
 */
	/**
	 * 指定したコントラクトであることを説明する Entity を作成します。
	 * @param	{Field} field field
	 * @param	{string} msg 表示するメッセージ
	 */
	constructor(field, msg) {
		super();
		this.field = field;
		
		// 与えられた文字列を改行で区切り、配列に変換する
		this.lines = msg.split('\n');
		const lines = this.lines.length;
		
		// 大きさを決定する
		const ctx = field.ctx;
		ctx.font = Sumire.FONT;
		this.width = 0;
		
		for (let i = 0; i < lines; i++) {
			this.width = Math.max(this.width, ctx.measureText(this.lines[i]));
		}
		this.width += 20; // as a margin
		this.height	= Sumire.Y_STEP * lines + 20;
		
		this.setBounds(140, 120, 360, 240);
		this.x0 = 130 + 40;
		this.y0 = Math.floor(90 + 12 + Sumire.Y_STEP + 100 - Sumire.Y_STEP * lines * 2 / 3);
		const msgy0 = Math.floor(100 + 100 - Sumire.Y_STEP * lines * 2 / 3);
		this.mw = 380 - 40;
		this.mh = Sumire.Y_STEP * lines + 20;
		this.xp = [this.x0-20, this.x0-20+this.mw, this.x0-20+this.mw, 410, 405, 390, this.x0-20];
		this.yp = [msgy0, msgy0, msgy0+this.mh, msgy0+this.mh, msgy0+this.mh+10, msgy0+this.mh, msgy0+this.mh];
		
		this.picNumber = 0;
	}
	
/*-----------
 * Overrides
 */
	/**
	 * @override
	 * @param {Context} ctx グラフィックコンテキスト
	 */
	draw(ctx) {
		ctx.fillStyle = Sumire.BACK_COLOR;
		ctx.fillRect(this.x, this.y, this.w, this.h);
		ctx.strokeStyle = 'rgb(0,0,0)';
		ctx.fillStyle = Sumire.MSG_COLOR;
		ctx.beginPath();
		ctx.moveTo(this.xp[0], this.yp[0]);
		for (let i = 1; i < this.xp.length; i++) {
			ctx.lineTo(this.xp[i], this.yp[i]);
		}
		ctx.closePath();
		ctx.fill();
		ctx.stroke();
		
		ctx.fillStyle = 'rgb(0,0,0)';
		ctx.font = Sumire.FONT;
		let y = this.y0;
		
		for (let i = 0; i < this.lines.length; i++) {
			ctx.fillText(this.lines[i], this.x0, y);
			y += Sumire.Y_STEP;
		}
		
		ctx.drawImage(CardImageHolder.SUMIRE[this.picNumber], 400, 260);
	}
	
/*------------------
 * instance methods
 */
	/**
	 * わらったり、泣いたりのアニメーションを表示し、ブロックします。
	 * クリックを検知するとアニメーションを解除します。
	 * @async
	 * @param　{number} face 顔(1..笑い顔, 2..泣き顔)
	 */
	async animate(face) {
		this.field.add(this);
		this.face = face;
		try {
			while (true) {
				this.picNumber ^= this.face;
				this.field.draw();
				if (this.picNumber > 0) {
					if (await this.field.waitClick(500)) break;
				} else if (await this.field.waitClick(1000)) break;
			}
		} catch (e) {
			if (e instanceof QuitInterruptException) this.field.pull(this);
			throw e;
		}
		this.field.pull(this); // remove
	}
}

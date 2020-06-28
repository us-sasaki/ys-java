/**
 * アドホックなメインプログラムです。だんだん本格的になってきました。
 *
 * @version		a-release		19, June 2020
 * @author		Yusuke Sasaki
 */
class PlayMain {
    /** @type {string} */ canvasId;
    /** @type {Field} */ field;
    /** @type {Board} */ board;
    /** @type {Player[]} */ players;
    /** @type {number} */ handno;
    /** @type {string} */ contractString;
	/** @type {Problem[]} */ problems;
    /** @type {Sumire} */ sumire;
	/** @type {Button} */ quit;
    /** @type {Button} */ dd;
	/** @type {Button} */ textWindow;
	/** @type {SelectDialog} */ dialog;
    /** @type {YesNoDialog} */ confirmDialog;
	/** @type {boolean} */ exitSignal;
	
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
		this._placeTextButton_();
	}
	
/*-----------------------------
 * implements (ActionListener)
 */
    /**
     * dd ボタンを押したときの処理
     */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == quit) {
			if (mainThread != null) mainThread.interrupt();
		}
		
		// 以下、added 03/6/2
		if (e.getSource() == textWindow) {
			if (board != null) {
				TextInfoWindow.getInstance(board.toText());
			}
		}
		// 以上、added 03/6/2
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
		if (p.isValid()) this.problem.push(p);
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
			if (window.confirm('このボードを破棄して中断します')) {
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
			dd.caption = dd.doubleDummy?'通常に戻す':'ダブルダミー';
			this.board.getHand(Board.EAST).turn(dd.doubleDummy);
			this.board.getHand(Board.WEST).turn(dd.doubleDummy);
			this.field.draw();
		});
		this.dd = dd;
	}

	/**
	 * テキスト表示ボタン(this.textWindow)を生成、配置します。
	 * @private
	 */
	_placeTextButton_() {
		this.textWindow = new Button(this.field, "テキスト表示"); // テキスト表示
		this.field.add(this.textWindow);
		this.textWindow.setBounds(540, 86, 80, 24);
	}
	
	/**
	 * Board の初期化を行います。
	 * @async
	 */
	async start() {
		const titles = [];
		this.problems.forEach( prob => titles.push(prob.title));
		this.dialod.setPulldown(titles);
		this.field.draw();
		const result = await this.dialog.show();
		
		if ("video" ==  result) {
			this._makeVideohand_();
		} else if ("replay" == result) {
			this._makeLasthand_();
		} else {
			this.handno = parseInt(result.substring(4));
			this._makeNewhand_();
		}
		// field に board を追加
		this.field.add(this.board);
		this.board.setPosition(0, 0);
		this.board.setDirection(0);
		this.field.draw();
		
		this.handno = 0;
		await this.main();

		// field から board を削除
		this.field.pull(this.board);

	}
	
	/**
	 * ダイアログで新しいハンドを選択したときの処理です。
	 * @private
	 */
	_makeNewhand_() {
		const prob = this.problems[handno];
		
		this.board = new Board(1);
		this.board.setName(prob.title);
		
		// Player 設定
		_setPlayers(prob)_
		
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
		this.players = [];
		this.players[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
		this.players[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
		// Computer Player 設定
		if ( !prob.thinker || prob.thinker != "DoubleDummyPlayer") {
			this.players[Board.EAST ] = new SimplePlayer2(board, Board.EAST);
			this.players[Board.WEST ] = new SimplePlayer2(board, Board.WEST, prob.openingLead);
		} else if (prob.thinker == "DoubleDummyPlayer") {
			this.players[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
			this.players[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.openingLead);
		} else {
			this.players[Board.EAST ] = new NoRufPlayer(board, Board.EAST);
			this.players[Board.WEST ] = new NoRufPlayer(board, Board.WEST, prob.openingLead);
		}
	}
	
	/**
	 * 前のプレイを自動再生するプレイヤーを設定します。
	 * @private
	 */
	_makeVideohand_() {
		const oldBoard = this.board;
		this.board = new Board(1);
		this.board.setName(oldBoard.name);
		
		// Player 設定
		this.players = [];
		for (let i = 0; i < 4; i++)
			this.players.push(new VideoPlayer(this.board, oldBoard, i));
		
		// ディール
		const hands = BridgeUtils.calculateOriginalHand(oldBoard);
		
		this.board.deal(hands);
		
		// コントラクト設定を行う
		this.board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());

		// ビデオモードはオープン状態
		const east = this.board.getHand(Board.EAST);
		east.turn(true);
		const west = this.board.getHand(Board.WEST);
		west.turn(true);
		
		this.dd.doubleDummy = true;
		this.dd.caption = "通常に戻す";
		
	}
	
	/**
	 * 前回と同じハンドを設定します。
	 * @private
	 */
	_makeLasthand_() {
		const prob = problems[handno];
		const oldBoard = this.board;
		this.board = new Board(1);
		this.board.name = oldBoard.name;
		
		// Player 設定
		_setPlayers_(prob);

		// ディール
		const hands = BridgeUtils.calculateOriginalHand(oldBoard);
		
		this.board.deal(hands);
		
		// コントラクト設定を行う
		this.board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	/**
	 * 始めのすみれによる説明を表示する
	 * @async
	 * @throws	QuitInterruptException 中断が選択された
	 */
	async explain() {
		const prob = this.problem[this.handno];
		
		this.sumire = new Sumire(field, prob.description);
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
			this.field.setSpot(this.board.getTurn());
			this.field.draw();
			
			let c = null;
			while (c === null) {
				c = await player[this.board.getPlayer()].play(); // ブロックする
			}
			this.board.play(c);
			this.field.draw();
			
			if (this.board.status == Board.SCORING) break;
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
				c.turn(true);
				hand.add(c);
			}
		}

		this.board.layout();
		this.board.getHand().forEach( hand => { hand.arrange(); hand.layout(); });
		
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
			msg2 = "残念。もう一度がんばって！";
		}
		
		msg += "("+win+"トリック)\nN-S側のスコア："+Score.calculate(this.board, Board.SOUTH);
		msg += "\n \n" + msg2;
		
		this.sumire = new Sumire(field, msg);
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
			this.field.spot = -1;
			this.field.draw();
		}
	}
}

class Problem {
	/** @type {string} 問題タイトル */ title;
	/** @type {Bid} コントラクト */ contract;
	/** @type {Packet[]} 4人のハンド */ hands;
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

	estimateValidity() {
		//
	}

	/**
	 * 指定されたパラメータで問題を作成します。
	 * 
	 * @param {string} title 問題の表題
	 * @param {number} kind ビッド種別(Bid.PASSなど
	 * @param {number} level ビッドレベル
	 * @param {number} denomination Bid.DOUBLE, Bid.REDOUBLE など
	 * @param {string[]} hands ハンド文字列
	 * @param {string} description 問題の説明
	 * @param {string} openingLead オープニングリード指定(指定なしは null)
	 * @param {string} thinker 思考アルゴリズム名
	 * @returns	{Problem} 問題
	 */
	static regular(title, kind, level, denomination, hands, description, openingLead, thinker) {
		let p = new Problem();
		p.title = title;
		p.description = description;
		p.openingLead = openingLead;
		p.thinker = thinker;
		p.contract = new Bid(kind, level, denomination);

		hs = [];
		for (let i = 0; i < 4; i++) hs.push(new Packet());
		
		const pile = Packet.provideDeck();
		for (let i = 0; i < 4; i++) Problem.draw(pile, hs[i], hands[i]);
		
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
		p.hands = hs;
		return p;
	}

	/**
	 * この問題のハンドを生成します。
	 * (内部的に持つ Packet の shallow copy を作ります)
	 * @returns	{Packet[]} この問題のハンド
	 */
	createHands() {
		const result = [];
		for (let i = 0; i < 4; i++) {
			const h = new Packet();
			this.hands[i].children.forEach(c => h.add(c));
			result.push(h);
		}
		return result;
	}

	/**
	 * 文字列で与えられたハンド情報を解釈して hand に設定します。
	 * @param	{Packet} pile カードの山
	 * @param	{Packet} hand 設定先のハンド
	 * @param	{string} str ハンド文字列
	 */
	static draw(pile, hand, str) {
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
		while (true) {
			this.picNumber ^= this.face;
			this.field.draw();
			if (this.picNumber > 0) {
				if (await this.field.waitClick(500)) break;
			} else if (await this.field.waitClick(1000)) break;
		}
		this.field.pull(this); // remove
	}
}

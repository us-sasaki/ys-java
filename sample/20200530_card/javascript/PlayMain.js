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
		for (let i = 0; i < this.problems.length; i++) {
		const titles = [];
		this.problems.forEach( prob => titles.push(prob.title));
		this.dialod.setPulldown(titles);
		this.field.draw();
		const result = await this.dialog.show();
		
		if ("video" ==  result) {
			this.makeVideohand();
			const east = this.board.getHand(Board.EAST);
			east.turn(true);
			const west = this.board.getHand(Board.WEST);
			west.turn(true);
			
			this.dd.doubleDummy = true;	// added 02/9/16
			this.dd.caption = "通常に戻す";	// added 02/9/16
			
		} else if ("replay" == result) {
			this.makeLasthand();
		} else {
			this.handno = parseInt(result.substring(4));
			this.makeNewhand();
		}
		this.handno = 0;
		this.main();
	}
	
	/**
	 * ダイアログで新しいハンドを選択したときの処理です。
	 */
	makeNewhand() {
		const prob = problems[handno];
		prob.start();
		
		this.board = new Board(1);
		this.board.setName(prob.title);
		
		this.field.add(this.board);
		this.board.setPosition(0, 0);
		this.board.setDirection(0);
		
		// Player 設定
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
		
		// ディール
		const hand = prob.createHand();
		
		board.deal(hand);
		field.repaint();
		
		// ビッドを行う
		board.setContract(prob.getContract(), Board.SOUTH);
	}
	
	private void makeVideohand() {
		Board oldBoard = board;
		board = new GuiedBoard(new BoardImpl(1));
		board.setName(oldBoard.getName());
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player 設定
		//
		player = new Player[4];
		player[Board.NORTH] = new VideoPlayer(board, oldBoard, Board.NORTH);
		player[Board.EAST ] = new VideoPlayer(board, oldBoard, Board.EAST );
		player[Board.SOUTH] = new VideoPlayer(board, oldBoard, Board.SOUTH);
		player[Board.WEST ] = new VideoPlayer(board, oldBoard, Board.WEST );
		
		//
		// ディール
		//
		Packet[] hand = BridgeUtils.calculateOriginalHand(oldBoard);
		
		board.deal(hand);
		field.repaint();
		
		//
		// ビッドを行う
		//
		board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	private void makeLasthand() {
		Problem prob = (Problem)(problem.elementAt(handno));
		Board oldBoard = board;
		board = new GuiedBoard(new BoardImpl(1));
		board.setName(oldBoard.getName());
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player 設定
		//
		player = new Player[4];
		player[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
		player[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
		
		//
		// Computer Player 設定
		//
		if ((prob.getThinker() == null)||(!prob.getThinker().equals("DoubleDummyPlayer"))) {
			player[Board.EAST ] = new SimplePlayer2(board, Board.EAST);
			player[Board.WEST ] = new SimplePlayer2(board, Board.WEST, prob.getOpeningLead());
		} else if (prob.getThinker().equals("DoubleDummyPlayer")) {
			player[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
			player[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.getOpeningLead());
		} else {
			player[Board.EAST ] = new NoRufPlayer(board, Board.EAST);
			player[Board.WEST ] = new NoRufPlayer(board, Board.WEST, prob.getOpeningLead());
		}
		//
		// ディール
		//
		Packet[] hand = BridgeUtils.calculateOriginalHand(oldBoard);
		
		board.deal(hand);
		field.repaint();
		
		//
		// ビッドを行う
		//
		board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	/**
	 * start()と対になるメソッドで、start()で初期化したリソースの破棄を行います。
	 * start() と別のスレッドから呼ばれます。
	 * ダイアログのリソースなど終了処理が必要なものの破棄を行います。
	 * 上位から明示的にコールすることによってダイアログが残るバグは解消されます。
	 */
	public void stop() {
		if (field != null) {
			field.removeEntity(board);
		}
		if (dialog != null) dialog.disposeDialog();
		if (confirmDialog != null) confirmDialog.disposeDialog();
		if (player != null) {
			for (int i = 0; i < player.length; i++) {
				if ((player[i] != null)&&(player[i] instanceof HumanPlayer)) {
					((HumanPlayer)player[i]).dispose();
				}
			}
		}
	}
	
	public void dispose() {
		if (field != null) field.dispose();
	}
	
	/**
	 * 始めのすみれによる説明を表示する
	 * 中止ボタン処理が未実装
	 */
	async explain() {
		const prob = this.problem[this.handno];
		
		this.sumire = new Sumire(field, prob.description);
		this.contractString = prob.getContractString();
		this.sumire.animate(Sumire.NORMAL);
	}
	
	/**
	 * メインループ
	 * @async
	 */
	async mainLoop() {
		if (dd.doubleDummy) {
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
			
			if (this.board.getStatus() == Board.SCORING) break;
		}
	}
	
	/**
	 * 元のハンドの表示、スコアの表示を行う
	 */
	protected void displayScore() throws InterruptedBridgeException {
		field.removeSpot();
		field.repaint();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			if (confirmQuit()) throw new InterruptedBridgeException();
		}
		//
		// カードをもう一度表示する
		//
		Packet[] original = BridgeUtils.calculateOriginalHand(board);
		for (int i = 0; i < 4; i++) {
			GuiedPacket hand = (GuiedPacket)(board.getHand(i));
			for (int j = 0; j < original[i].size(); j++) {
				Card c = original[i].peek(j);
				c.turn(true);
				hand.add(c);
			}
		}

		board.layout();
		for (int i = 0; i < 4; i++) {
			GuiedPacket hand = (GuiedPacket)(board.getHand(i));
			hand.arrange();
			hand.layout();
		}
		
		//
		// スコア表示
		//
//		Score score = new Score();
		String msg = "結果：" + contractString + "  ";
		String msg2;
		// メイク数
		int win		= BridgeUtils.countDeclarerSideWinners(board);
		int up		= win - board.getContract().getLevel() - 6;
		int make	= win - 6;
		
		if (up >= 0) {
			// メイク
			msg += String.valueOf(make) + "メイク";
			msg2 = "おめでとう！！";
		} else {
			// ダウン
			msg += String.valueOf(-up) + "ダウン";
			msg2 = "残念。もう一度がんばって！";
		}
		
		msg += "("+win+"トリック)\nN-S側のスコア："+Score.calculate(board, Board.SOUTH);
		msg += "\n \n" + msg2;
		
		sumire = new Sumire(field, msg);
		if (up >= 0) 
			sumire.animate(Sumire.DELIGHTED);
		else
			sumire.animate(Sumire.SAD);
		field.addEntity(sumire);
		field.repaint();
		try {
			waitClick();
		} catch (InterruptedException e) {
			if (confirmQuit()) {
				field.removeEntity(sumire);
				clearHands();
				throw new InterruptedBridgeException();
			}
		}
		field.removeEntity(sumire);
		field.repaint();
		clearHands();
	}
	
	protected void clearHands() {
		//
		// Board の状態を正しいものにするため、ハンドをクリアする
		//
		for (int i = 0; i < 4; i++) {
			Packet hand = board.getHand(i);
			while (hand.size() > 0) {
				hand.draw();
			}
		}
	}
	
	protected boolean confirmQuit() {
		try {
			confirmDialog = new YesNoDialog(
								field.getCanvas(),
								"このボードを破棄して中断しますか？");
			confirmDialog.show();
			boolean yes = confirmDialog.isYes();
			confirmDialog.disposeDialog();
			return yes;
		} catch (Exception e) {
//			e.printStackTrace();
			return true;
		}
	}
	
	/**
	 * メインメソッドです。
	 */
	main() {
		try {
			explain();
			mainLoop();
			displayScore();
		} catch (InterruptedBridgeException e) {
			field.removeSpot();
			field.repaint();
		}
	}
}

class Problem {
	/** @type {string} */ title;
	/** @type {Bid} */ contract;
	/** @type {Packet[]} */ hands;
	/** @type {string} */ description;
	/** @type {string} */ openingLead;
	/** @type {string} */ thinker;

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

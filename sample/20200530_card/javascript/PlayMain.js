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
    /** @type {Player[]} */ player;
    /** @type {number} */ handno;
    /** @type {string} */ contractString;
	/** @type {Problem[]} */ problem;
    /** @type {Explanation} */ sumire;
	/** @type {Button} */ quit;
    /** @type {Button} */ dd;
	/** @type {Button} */ textWindow;
	/** @type {Thread} */ mainThread;
	/** @type {SelectDialog} */ dialog;
    /** @type {YesNoDialog} */ confirmDialog;
	/** @type {boolean} */ exitSignal;
	
	/**
	 * PlayMain オブジェクトを生成します。
	 */
	constructor(canvasId) {
		this.canvasId = canvasId;
		this.problem = [];
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
	 */
	placeQuitButton() {
		this.quit = new Button(this.field, "中断");
		this.quit.setBounds(540, 30, 80, 24);
		this.field.add(this.quit);
	}

	/**
	 * ダブルダミーボタン(this.dd)を生成、配置します。
	 * this.dd には doubleDummy {boolean} プロパティがあります。
	 */
	placeDDButton() {
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
	 */
	placeTextButton() {
		this.textWindow = new Button(this.field, "テキスト表示"); // テキスト表示
		this.field.add(this.textWindow);
		this.textWindow.setBounds(540, 86, 80, 24);
	}

	/**
	 * BridgeField, Board を初期化します。
	 */
	initialize() {
		this.field = new Field(this.canvasId);
        
		// ボタン
		this.placeQuitButton();
		this.placeDDButton();
		this.placeTextButton();
	}
	
	/**
	 * Board の初期化を行います。
	 */
	start() {
		//dialog = new SelectDialog(display, board);
		//for (int i = 0; i < problem.size(); i++) {
		//	Problem prob = (Problem)(problem.elementAt(i));
		//	dialog.addChoice(prob.getTitle());
		//}
		this.field.draw();
		//try {
		//	dialog.newHand.select(handno);
		//} catch (Exception e) {
		//	handno = -1;
		//}
		//dialog.show();
		//dialog.requestFocus();	// added 2000/8/16
		//String result = dialog.result; // dialog.dispose() によって result が失われるため
		const result = "test board";
		//dialog.disposeDialog();	// ここで行う added 2001/7/15
		//if (result == null) return;
		//if (result.equals("disposed")) {
		//	exitSignal = true;
		//	return;
		//}
		
		//mainThread = Thread.currentThread();
		//if ("Video".equals(result)) {
		//	makeVideohand();
		//	GuiedPacket east = (GuiedPacket)(board.getHand(Board.EAST));
		//	east.turn(true);
		//	GuiedPacket west = (GuiedPacket)(board.getHand(Board.WEST));
		//	west.turn(true);
		//	
		//	this.dd.doubleDummy = true;	// added 02/9/16
		//	this.dd.caption = "通常に戻す";	// added 02/9/16
		//	
		//} else if ("Same Hand".equals(result)) {
		//	makeLasthand();
		//} else {
		//	this.handno = -1;
		//	for (int i = 0; i < problem.size(); i++) {
		//		Problem p = (Problem)(problem.elementAt(i));
		//		
		//		if (p.getTitle().equals(result)) {
		//			this.handno = i;
		//			makeNewhand();
		//			break;
		//		}
		//	}
		//	if (this.handno == -1) { return; }
		//}
		this.handno = 0;
		main();
	}
	
	/**
	 * ダイアログで新しいハンドを選択したときの処理です。
	 */
	makeNewhand() {
		Problem prob = (Problem)(problem.elementAt(handno));
		prob.start();
		
		board = new GuiedBoard(new BoardImpl(1));
		board.setName(prob.getTitle());
		
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
		Packet[] hand = prob.getHand();
		
		board.deal(hand);
		field.repaint();
		
		//
		// ビッドを行う
		//
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
	 */
	protected void explain() throws InterruptedBridgeException {
		
		Problem prob = (Problem)(problem.elementAt(handno));
		
		sumire = new Explanation(field, prob.getDescription());
		contractString = prob.getContractString();
		
		field.addEntity(sumire);
		field.repaint();
		try {
			waitClick(); // クリックを待つ。 InterruptedException をスローするかも
		} catch (InterruptedException e) {
			if (confirmQuit()) {
				field.removeEntity(sumire);
				throw new InterruptedBridgeException();
			}
		}
		field.removeEntity(sumire); // これによってすみれのスレッドも終了する
		field.repaint();
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
			//
			// Spot を指定する
			//
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
		
		sumire = new Explanation(field, msg);
		if (up >= 0) 
			sumire.animate(Explanation.DELIGHTED);
		else
			sumire.animate(Explanation.SAD);
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
		for (let i = 0; i < 4; i++) draw(pile, hs[i], hands[i]);
		
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
	}

	/**
	 * 文字列で与えられたハンド情報を解釈して hand に設定します。
	 * @param	{Packet} pile カードの山
	 * @param	{Packet} hand 設定先のハンド
	 * @param	{string} str ハンド文字列
	 */
	draw(pile, hand, str) {
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
	 * @param	{string}	selectDialogId	html内のダイアログ追加用 div 要素の id
	 */
	constructor(selectName) {
		this.modalContent = document.getElementById('modal-content');
		this.modalOverlay = document.getElementById('modal-overlay');
		if (!this.modalContent || !this.modalOverlay)
			throw new Error("html error. without modal-content or modal-overlay");
		const s = document.getElementsByName('select');
		if (s.length != 1) throw new Error("html doesn't have select:"+selectName);
		this.select = s[0];
		// select の子要素を削除
		while (this.select.firstChild) { this.select.removeChild(this.select.firstChild); }
		const title = ['続ブリッジの問題(1)', '続とりあえず名前の長い問題(2)', '続問題(3)', '続問題(4)'];
		for (let i = 0; i < title.length; i++) {
			const opt = document.createElement('option');
			opt.setAttribute('value', 'prob'+(i+1));
			opt.innerHTML = title[i];
			this.select.appendChild(opt);
		}
		this.startButton = document.getElementById('startButton');
		this.videoButton = document.getElementById('videoButton');
		this.replayButton = document.getElementById('replayButton');
	}
/*	constructor(selectDialogId) {
		const sd = document.getElementById(selectDialogId);
		// overlay 追加
		const overlay = document.createElement('div');
		overlay.setAttribute('id', 'modal-overlay');
		sd.appendChild(overlay);
		// content 追加
		const content = document.createElement('div');
		content.setAttribute('id', 'modal-content');
		// content に select dialog 要素追加
		const outerTable = document.createElement('table');
		outerTable.setAttribute('class', 'full');
		// １行目　タイトル
		const r1 = document.createElement('tr');
		r1.appendChild(document.createElement('td')).innerHTML = 'ブリッジシミュレーター';
		outerTable.appendChild(r1);
		// ２行目　プルダウンと開始ボタンのテーブル
		const innerTable = document.createElement('table');
		innerTable.setAttribute('class','full');
		const ir = document.createElement('tr');
		const pulldown = ir.appendChild(document.createElement('td')).appendChild(document.createElement('select'));

		const title = ['ブリッジの問題(1)', 'とりあえず名前の長い問題(2)', '問題(3)', '問題(4)'];
		for (let i = 0; i < title.length; i++) {
			const opt = document.createElement('option');
			opt.setAttribute('value', 'prob'+(i+1));
			opt.innerHTML = title[i];
			pulldown.appendChild(opt);
		}
		this.startButton = document.createElement('input');
		this.startButton.setAttribute('type', 'button');
		this.startButton.setAttribute('value', '開始する');
		ir.appendChild(document.createElement('td')).appendChild(this.startButton);
		innerTable.appendChild(ir);

		const r2 = document.createElement('tr');
		r2.appendChild(document.createElement('td')).appendChild(innerTable);
		outerTable.appendChild(r2);
		// ３行目　プレイ自動再生
		const r3 = document.createElement('tr');
		const i1 = document.createElement('input');
		i1.setAttribute('type', 'button');
		i1.setAttribute('value', '今のプレイを自動再生する');
		r3.appendChild(document.createElement('td')).appendChild(i1);
		outerTable.appendChild(r3);
		// ４行目　同じハンド
		const r4 = document.createElement('tr');
		const i2 = document.createElement('input');
		i2.setAttribute('type', 'button');
		i2.setAttribute('value', '同じハンドをもう一度プレイする');
		r4.appendChild(document.createElement('td')).appendChild(i1);
		outerTable.appendChild(r4);

		content.appendChild(outerTable);
	}
*/
/*------------------
 * instance methods
 */
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

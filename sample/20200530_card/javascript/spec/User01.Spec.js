'use strict';

const startBridge = function() {

//window.alert(problem);
	// another test code
	const field = new Field('canvas');
	const board = new Board(3);
	board.deal(11);
	field.add(board);
	// ボタン
console.log('field.canvas='+field.canvas);
	const quit = new Button(field, "quit");
	quit.isVisible = true;
	quit.setBounds(540, 30, 80, 24);
	field.add(quit);
	const dd = new Button(field, "ダブルダミー");
	field.add(dd);
	dd.setBounds(540, 58, 80, 24);
	let doubleDummy = true;
	dd.setListener( () => {
console.log('dd pressed');
		if (board.status != Board.PLAYING) return;
		//
		doubleDummy = !doubleDummy;
		dd.caption = doubleDummy?'元に戻す':'ダブルダミー';
		board.getHand(Board.EAST).turn(doubleDummy);
		board.getHand(Board.WEST).turn(doubleDummy);
		field.draw();
	});

	//dd.addActionListener(this);
	
	const textWindow = new Button(field, "show text");
	field.add(textWindow);
	textWindow.setBounds(540, 86, 80, 24);
	//textWindow.addActionListener(this);

	CardImageHolder.setBackImage(1);
	//board.getHand().forEach( h => h.turn(true) );

	board.play(new Bid(Bid.BID, 1, Bid.CLUB));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	field.spot = board.getTurn();

//	console.log(board.allows(new Card(Card.DIAMOND, Card.KING)));

	const s = 500;
	( async () => {
		field.draw();
		const selectDialog = new SelectDialog('select');
		console.log(await selectDialog.show());
		const sumire = new Sumire(field, "メッセージ\n次の行\n3行目\n4行目\n5行目\n6行目");
		await sumire.animate(Sumire.NORMAL);

		await board.playWithGui(new Card(Card.DIAMOND, Card.KING));
		board.getHand(Board.NORTH).layout = new DummyHandLayout();
		await Field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, 9));
		await Field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, 4));
		await Field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, 5));
		await Field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, Card.ACE));
		await Field.sleep(s);
		field.spot = board.getTurn();
		field.draw();
	
		console.log(board.toString());
		console.log(board.trickGui);
	

		//const card = await field.waitCardSelect();
		//window.alert('async returned : '+card.toString());
		//console.log('async returned : '+card.toString());
	} )();

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
/**
 * ブリッジシミュレータにおける開始時の説明の絵を生成する Entity です。
 * この Entity には、コントラクトの内容などの説明書きが表示されます。
 *
 */
class Sumire extends Entity {
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
	
	/** @type {number} ふきだしの幅と高さ */
	mw;
	mh;
	
	/** @type {number[]} ふきだしの頂点座標 */
	xp;
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

/* 依存 Card.js */

/**
 * Player は Bid, または Playを行う主体です。
 * GUIに連動した人、コンピュータアルゴリズムなどが当てはまります。
 * Player は、各々１つのBoardと連動している。BoardManagerのもつmaster board
 * との整合性は、今のところPlayerのありえないプレイを契機として修復される。
 * 将来的には Board の状態変更メソッド内で hashCode を用いて随時検知したい。
 *
 * @version		making		12, June 2020
 * @author		Yusuke Sasaki
 */
class Player {
	/** @const {number} リードの順番を示す定数(0)で、リーダー（１番目）を示します。 */
	static LEAD		= Board.LEAD;
	
	/** @const {number} リードの順番を示す定数(1)で、セカンドハンド（２番目）を示します。 */
	static SECOND	= Board.SECOND;
	
	/** リードの順番を示す定数(2)で、サードハンド（３番目）を示します。 */
	static THIRD	= Board.THIRD;
	
	/** リードの順番を示す定数(3)で、フォースハンド（４番目）を示します。 */
	static FORTH	= Board.FORTH;
	
	/** プレイヤーの相対位置を示す定数(=0)で、自分の位置を示します。 */
	static ME		= 0;
	
	/** プレイヤーの相対位置を示す定数(=1)で、自分の左の席(left hand)を示します。 */
	static LEFT		= 1;
	
	/** プレイヤーの相対位置を示す定数(=2)で、パートナーの位置を示します。 */
	static PARTNER	= 2;
	
	/** プレイヤーの相対位置を示す定数(=3)で、自分の右の席(right hand)を示します。 */
	static RIGHT	= 3;
	
	/** @type {Board} ボード */
	myBoard;

	/** @type {number} 座席定数 */
	mySeat;
	
/*------------------
 * instance methods
 */
	/**
	 * このプレイヤーが参照するボードを設定します。
	 * 継承クラスのコンストラクタなどで使用します。
	 * 上書き可能です。
	 * @param	{Board} board	プレイヤーが参照するボード
	 */
	setBoard(board) {
		this.myBoard = board;
	}
	
	/**
	 * このプレイヤーの座っている場所(Board.NORTHなど)を指定します。
	 * @param	{number} seat	プレイヤーの座っている場所(座席定数)
	 */
	setMySeat(seat) {
		this.mySeat = seat;
	}
	
	/**
	 * 上位プログラムからコールされるメソッドで、
	 * このプレイヤーのビッド、プレイを返却します。
	 * @returns		{Bid|Card}	プレイ内容
	 */
	async play() {
		switch (this.myBoard.status) {
		
		case Board.BIDDING:
			while (true) {
				const b = await this.bid();
				if (this.myBoard.allows(b)) return b;
			}
			
		case Board.OPENING:
		case Board.PLAYING:
			while (true) {
				const c = await this.draw();
				if (this.myBoard.allows(c)) return c;
			}
			
		case Board.DEALING:
		case Board.SCORING:
			throw new Error("Player.play() が DEALING/SCORING 状態のボードで呼ばれました");
			
		default:
			throw new Error("play() internal error");
		}
	}
	
/*
 * サブクラスに提供する便利関数
 */
	/**
	 * パートナーの座っている場所(Board.NORTHなどの座席定数)を返却します。
	 *
	 * @returns		{number}	パートナーの座席番号
	 */
	getPartnerSeat() {
		return (this.mySeat + 2) % 4;
	}
	
	/**
	 * このプレイヤーのもつハンドを返却します。
	 * @returns		{Packet}	このプレイヤーのハンド
	 */
	getMyHand() {
		return this.myBoard.getHand(this.mySeat);
	}
	
	/**
	 * 現在プレイ順番となっているハンドを取得します。
	 * このプレイヤーがディクレアラーの場合、このメソッドを使用することで
	 * プレイ対象となっている自分、またはダミーのいずれかのハンドが取得できます。
	 * @returns		{Packet}	現在プレイ番となっているプレイヤーのハンド
	 */
	getHand() {
		return this.myBoard.getHand(this.myBoard.getTurn());
	}
	
	/**
	 * @returns		{Packet}	ダミーのハンド
	 */
	getDummyHand() {
		return this.myBoard.getHand(this.myBoard.getDummy());
	}
	
	/**
	 * 現在場に出ているカードを取得します。
	 * @returns		{Trick}	現在場に出ているカード
	 */
	getTrick() {
		return this.myBoard.getTrick();
	}
	
	/**
	 * リードされたカードを取得します。
	 * 自分がリードを行う番であった場合や、Board の状態が Board.PLAYING
	 * でなかった場合、null が返却されます。
	 * @returns		{Card} リードされたカード
	 */
	getLead() {
		const o = this.getPlayOrder();
		if ((o == Board.LEAD)||(o == -1)) return null;
		return this.getTrick().children[0];
	}
	
	/**
	 * 現在のプレイ順(lead, 2nd, 3rd, 4th)を返します。
	 * Board の状態が Board.OPENING, Board.PLAYING 以外の場合、 -1 が返却されます。
	 *
	 * @returns		{number} プレイ順を示す定数(LEAD, SECOND, THIRD, FORTH)
	 */
	getPlayOrder() {
		return this.myBoard.getPlayOrder();
	}
	
	/**
	 * ダミーの自分からの相対位置を返します。
	 * ダミーがまだ決定していない場合、Error がスローされます。
	 *
	 * @returns		{number} ダミーの相対位置
	 * @see			ME
	 * @see			LEFT
	 * @see			PARTNER
	 * @see			RIGHT
	 */
	getDummyPosition() {
		const dummySeat = this.myBoard.getDummy();
		if (dummySeat == -1)
			throw new Error("まだコントラクトが決定していません");
		
		return (dummySeat - this.mySeat + 4) % 4;
	}
	
	/**
	 * Leader の自分からの相対位置を返します。
	 * プレイの状態でないとき、IllegalStatusException がスローされます。
	 *
	 * @returns		{number} Leaderの相対位置
	 * @see			ME
	 * @see			LEFT
	 * @see			PARTNER
	 * @see			RIGHT
	 */
	getLeaderPosition() {
		if ( (this.myBoard.status != Board.OPENING) &&
				(this.myBoard.getStatus() != Board.PLAYING) )
			throw new Error("プレイが開始されていません");
		if (this.myBoard.status == Board.SCORING)
			throw new Error("ボードはすでに終了しています");
		
		const t = this.myBoard.getTrick();
		return (t.leader - this.mySeat + 4) % 4;
	}
	
	/**
	 * bid します
	 * @abstract
	 */
	async bid() {
		throw new Error("must implement bid()");
	}
	/**
	 * play します
	 * @abstract
	 */
	async draw() {
		throw new Error("must implement draw()");
	}
	
	/**
	 * Aceを14に変換します。
	 * @param {number|Card} valueOrCard value または Card
	 * @returns {number} ACE の場合 14, 他は変えない
	 */
	bridgeValue(valueOrCard) {
		if (valueOrCard instanceof Card) return this.bridgeValue(valueOrCard.value);
		return (valueOrCard === 1)? 14 : valueOrCard;
	}

	
}


/**
 * 可能なプレイをランダムに行うコンピュータプレイヤーです。
 * ビッドはつねにパスします。ディクレアラーとしてもプレイできます。
 *
 * @version		making		12, June 2020
 * @author		Yusuke Sasaki
 */
class RandomPlayer extends Player {
	/**
	 * 
	 * @param {Board?} board
	 * @param {number?} seat 
	 */
	constructor(board, seat) {
		super();
		if (board !== void 0 && seat !== void 0) {
			this.setBoard(board);
			this.setMySeat(seat);
		}
	}
	
	/**
	 * パスします。
	 * @return		パス
	 */
	async bid() {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * 可能なプレイをランダムに選択し、返却します。
	 * @return		ランダムなプレイ
	 */
	async draw() {
		const board = this.myBoard;
		const hand = this.getHand();
		// プレイすべきハンドを混ぜる
		hand.shuffle();
		let played = null;
		
		// 混ぜられたハンドの下から順にプレイ可能なカードを検索する
		for (let i = 0; i < hand.children.length; i++) {
			played = hand.children[i];
			if (board.allows(played)) break;
		}
		if (played == null) throw new Error();
		
		// ハンドを戻しておく
		hand.arrange();
		const field = this.myBoard.getField();
		if (field) await field.sleep(0); // 考えたふり
		return played;
	}
}

/**
 * 指定されたボード(ビッド、プレイ履歴)にしたがってビデオ再生する
 * コンピュータプレイヤーです。
 */
class VideoPlayer extends Player {
	/** @type {Board} シナリオとなる Board */
	scenario;
	
/*-------------
 * Constructor
 */
	/**
	 * 指定された Board 上でプレイし、シナリオ通りにプレイする VideoPlayer
	 * を作成します。
	 * @param {Board} board プレイする Board
	 * @param {Board} scenario プレイシナリオ(seat の座席のプレイ履歴をプレイ)
	 * @param {number} seat 座席番号(Board.NORTH など)
	 */
	constructor(board, scenario, seat) {
		super();
		this.setBoard(board);
		this.setMySeat(seat);
		
		this.scenario = scenario;
	}
	
	/**
	 * ビッド履歴から現在の状態でのビッドを取得し、返却します。(未実装)
	 * @async
	 * @returns		{Bid} 必ずパスします(未実装)
	 */
	async bid() {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * プレイ履歴から現在の状態でのプレイを取得し、返却します。
	 * @async
	 * @returns		{Card} シナリオ通りのプレイ
	 */
	async draw() {
		await this.myBoard.getField().sleep(400); // 考えた振り
		
		const trickCount = this.myBoard.getTricks();
		const num = this.myBoard.getTrick().children.length;
		
		const tr = this.scenario.playHist.getTrick(trickCount);
		
		return tr.children[num];
	}
}

/**
 * ＧＵＩ操作でプレイを行うヒューマンプレイヤーです。
 */
class HumanPlayer extends Player {
	/** @type {Field} マウスクリック検出する Field */
	field;

	/**
	 * 指定された Board 上でプレイするヒューマンプレイヤーを作成します。
	 * @param {Board} board プレイする Board
	 * @param {Field} field クリック検出のための Field
	 * @param {number} seat 座席番号
	 */
	constructor(board, field, seat) {
		super();
		this.setBoard(board);
		this.setMySeat(seat);
		this.field = field;
	}
	
/*------------
 * implements
 */
	/**
	 * ビッドはまだ実装されていません。自動的に必ずパスします。
	 *
	 * @returns		{Bid} パス
	 */
	async bid() {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * クリックされたカードがルール上可能なプレイであったら、そのカードを返却します。
	 * ルール上可能でない場合、適当なメッセージで正しいプレイを促します。
	 * @returns {Card} ランダムなプレイ
	 * @throws {QuitInterruptException} 中断ボタン押下時
	 */
	async draw() {
		const board = this.myBoard;
		/** @type {Card} */ let clicked = null;
		while (true) {
			clicked = await this.field.waitCardSelect();
			if (clicked != null) {
				if (board.allows(clicked)) break;
				if (this.getHand().indexOf(clicked) > -1) {
					window.alert("リードと同じスートを選択してください");
				} else {
					const turn = this.myBoard.getTurn();
					const seat = PlayHistory.SEAT_STRING[turn];
					window.alert(seat + "のハンドからカードを選択してください");
				}
			}
		}
		return clicked;
	}
	
}

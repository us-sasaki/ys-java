/**
 * @fileOverview ブリッジシミュレーターのソースです。
 */
/**
 * ReproducibleRandom class 定義
 * @classdesc 乱数 for test アルゴリズムは XorShift
 * {@link https://ja.wikipedia.org/wiki/Xorshift}
 * @constructor
 * @param	{number} seed 乱数シード
 */
class ReproducibleRandom {
	/** @const {ReproducibleRandom} */
	static instance;

	/** @type {number} */
	x;
	/** @type {number} */
	y;
	/** @type {number} */
	z;
	/** @type {number} */
	w;

	constructor(seed) {
		if (seed===void 0) seed = Math.floor(Math.random() * 0x7FFFFFFF);
		this.x = 123456789;
		this.y = 362436069;
		this.z = 521288629;
		this.w = seed;
	}

	/**
	 * 整数値を返却する
	 * @return	{number} 乱数値
	 */
	next() {
		const t = this.x ^ (this.x << 11);
	    this.x = this.y; this.y = this.z; this.z = this.w;
	    return this.w = (this.w ^ (this.w >>> 19)) ^ (t ^ (t >>> 8)); 
	}
	
	/**
	 * min以上max以下の乱数を生成する。厳密には max 値付近の値が出づらい。
	 * java の nextInt() 実装を参考にすると回避できるが、ごく微小なため許容。
	 * @param	{number} min 最小値(含む)
	 * @param	{number} max 最大値(含む)
	 * @return	{number} min～max の乱数値
	 */
	nextInt(min, max) {
		const r = Math.abs(this.next());
		return min + (r % (max + 1 - min));
	}

	/**
	 * static nextInt(min, max) で利用する乱数 instance を与えられた seed で
	 * 初期化します。
	 * @param {number} seed 乱数の種
	 */
	static setSeed(seed) {
		ReproducibleRandom.instance = new ReproducibleRandom(seed);
	}

	/**
	 * 2^128-1 の周期をもつ乱数を得ます。
	 * @param {number} min 最小の整数(含む)
	 * @param {number} max 最大の整数(含む)
	 * @returns {number} min～max の間の整数値を取る乱数
	 */
	static nextInt(min, max) {
		if (!ReproducibleRandom.instance)
			ReproducibleRandom.instance = new ReproducibleRandom();
		return ReproducibleRandom.instance.nextInt(min, max);
	}
}

/**
 * NaturalCardOrder class 定義
 *
 * @classdesc 自然なカードの並び順を表します。
 */
class NaturalCardOrder {
	/**
	 * スートの順位を表します。数値配列で、0,クラブ,ダイヤ,ハード,
	 * スペードの順に優先度を示す数値が格納されます。(大きい方が優先)
	 * @type	{number[]} 
	 */
	suitOrder;

/*--------------
 * static field
 */

	/** @const {number[]} Spade > Heart > Club > Diamondの順 */
	static SUIT_ORDER_SPADE = [ 0, 2, 1, 3, 4 ];
	
	/** @const {number[]} Heart > Club > Diamond > Spadeの順 */
	static SUIT_ORDER_HEART = [ 0, 3, 2, 4, 1 ];
	
	/** @const {number[]} Diamond > Spade > Heart > Clubの順 */
	static SUIT_ORDER_DIAMOND = [ 0, 1, 4, 2, 3];
	
	/** @const {number[]} Club > Diamond > Spade > Heartの順 */
	static SUIT_ORDER_CLUB = [ 0, 4, 3, 1, 2];
	
	/** @const {number[]} ACE が一番強く(大きく)、2 が一番弱い(小さい)並び順 */
	static VALUE_ORDER = [ 15, 14, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13];
	
	/** @const {number[]} 2 が一番強く(大きく)、ACE が一番弱い(小さい)並び順 */
	static REVERSE_VALUE_ORDER = [ 1,   2,15,14,13,12,11,10, 9, 8, 7 ,  6,  5,  4];

/*-------------
 * constructor
 */
	/**
	 * @constructor
	 * @param	{number}	trump	トランプスートを示します。{@link Card}
	 */
	constructor(trump) {
		switch (trump) {
		case Card.HEART:
			this.suitOrder = NaturalCardOrder.SUIT_ORDER_HEART;
			break;
		case Card.DIAMOND:
			this.suitOrder = NaturalCardOrder.SUIT_ORDER_DIAMOND;
			break;
		case Card.CLUB:
			this.suitOrder = NaturalCardOrder.SUIT_ORDER_CLUB;
			break;
		case Card.SPADE:
		default:
			this.suitOrder = NaturalCardOrder.SUIT_ORDER_SPADE;
		}
	}

/*------------------
 * instance methods
 */
	/**
	 * カードを並べ替えるため、カード同士を比較します。
	 *
	 * @param	{Card} a 比較対象のカード1
	 * @param	{Card} b 比較対象のカード2
	 * @return 	{number} カード1 > カード2 なら 1 、
	 *    カード1 = カード2 なら 0、
	 *    カード1 < カード2 なら -1 を返却します。
	 */
	compare(a, b) {
		if ( a.suit == b.suit && a.value == b.value ) return 0;
		
		const suitA = this.suitOrder[a.suit];
		const suitB = this.suitOrder[b.suit];
		if (suitA > suitB) return 1;
		if (suitA < suitB) return -1;

		const valueA = NaturalCardOrder.VALUE_ORDER[a.value];
		const valueB = NaturalCardOrder.VALUE_ORDER[b.value];
		if (valueA > valueB) return 1;
		return -1;
	}
}

/**
 * Entity class 定義
 * @classdesc Entity は canvas 上に描画されるオブジェクトを表します。
 * @constructor
 */
class Entity {

	/** 描画上、コンテナとなる Entity (parent)
	 * @type {Entities}
	 */
	parent;
	/** @type {number} この Entity の x 座標(右が正、pixel) */
	x;
	/** @type {number} この Entity の y 座標(下が正、pixel) */
	y;
	/** @type {number} この Entity の幅(pixel) */
	w;
	/** @type {number} この Entity の高さ(pixel) */
	h;
	/** @type {number} 向き(0..upright 1..right view 2..upside down 3..left view) */
	direction;
	/** @type {boolean} この Entity を表示するか */
	isVisible;

	constructor() {
		this.parent = undefined;
		this.w = 0;
		this.h = 0;
		this.x = 0;
		this.y = 0;
		this.direction = 0; // upright
		this.isVisible = true;
	}
/*------------------
 * static constants
 */
	/** @const {number} 下のプレイヤーから見て直立 */
	static UPRIGHT = 0; 
	/** @const {number}  右のプレイヤーから見て直立 */
	static RIGHT_VIEW = 1;
	/** @const {number} 上のプレイヤーから見て直立 */
	static UPSIDE_DOWN = 2;
	/** @const {number} 左のプレイヤーから見て直立 */
	static LEFT_VIEW = 3;

/*------------------
 * instance methods
 */
	/**
	 * 親オブジェクト(Entities)を設定します。this.parent への設定です。
	 * このメソッドは親オブジェクトからのみ呼ばれます。
	 * @param	{Entities} parent 親オブジェクト
	 */
	setParent(parent) {
		this.parent = parent;
	}
	/**
	 * この Entity の大きさを指定します。this.w, this.h への設定です。
	 * @param	{number} w 幅
	 * @param	{number} h 高さ
	 */
	setSize(w, h) {
		this.w = w;
		this.h = h;
	}

	/**
	 * この Entity のサイズを返却します。w, h を持つ object が返却されます。
	 * Entities ではこのメソッドがオーバーライドされ、設定されている
	 * layout に基づいたサイズが再計算され、返却されます。
	 * @returns		{object}	w:width, h:height
	 */
	getSize() {
		return {w: this.w, h: this.h};
	}
	/**
	 * 位置を指定します。左上が(0,0)で右下座標系です。
	 * this.x, this.y への設定です。
	 * @param	{number} x x座標
	 * @param	{number} y y座標
	 */
	setPosition(x, y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 位置と大きさを設定します。
	 * @param {number} x x座標
	 * @param {number} y y座標
	 * @param {number} w 幅
	 * @param {number} h 高さ
	 */
	setBounds(x, y, w, h) {
		this.setPosition(x, y);
		this.setSize(w, h);
	}
	
	/**
	 * この Entity の向き(0-3)を指定します。
	 * this.direction への設定です。
	 * 
	 * @param	{number} dir 向き(0-3)
	 */
	setDirection(dir) {
		if (dir!=0 && dir!=1 && dir!=2 && dir!=3)
			throw new Error("setDirection で direction の値が不正です:"+dir);
		if (( (this.direction ^ dir) & 1) > 0) {
			const tmp = this.w;
			this.w = this.h;
			this.h = tmp;
		}
		this.direction = dir;
	}
	
	/**
	 * この Entity の位置、大きさを取得します。
	 * 
	 * @return	{object} x: x座標, y: y座標, w: 幅, h: 高さ
	 */
	getRect() {
		return {x: this.x, y: this.y, w: this.w, h: this.h };
	}
	
	/**
	 * この Entity を描画します。
	 * このメソッドは親オブジェクトからのみ呼ばれます。
	 * 子クラスでは isVisible が false のとき描画しないようにする必要があります
	 * Entity での実装はエラーのスローです。
	 * 
	 * @param	{CanvasContext} ctx canvas.getContext('2d') で取得されるコンテキスト
	 */
	draw(ctx) {
		throw new Error("not implemented");
	}
}

/**
 * Entities class 定義
 * @classdesc Entities は複数の Entity をまとめる容器オブジェクトを表します。
 * @extends	{Entity}
 */
class Entities extends Entity {

	/**
	 * 子要素を格納します。子要素数は、this.children.length で取得できます。<br>
	 * <strong>直接書き込みは行わず、add を利用して下さい。</strong>
	 * @type	{Entity[]}
	 * @see	#add
	 */
	children;

	/**
	 * @constructor
	 */
	constructor() {
		super();
		this.children = [];
		this.setSize(0, 0);
	}

/*------------------
 * instance methods
 */
	/**
	 * 子オブジェクトを追加します。子オブジェクトの親として自分を設定します。
	 * また、このオブジェクトに layout が設定している場合、layoutします。
	 * @param	{Entity|Entities} entity	追加対象の Entity または Entities
	 */
	add(entity) {
		if (this.children.indexOf(entity) >= 0) return;
		entity.setParent(this);
		this.children.push(entity);
		if (this.layout != null) this.layout.layout(this);
	}
	
	/**
	 * 指定された子オブジェクトを引きます。ひかれたオブジェクトはこの
	 * オブジェクトの要素でなくなります。(parent が null になります)
	 *
	 * @param	{number|Entity} indexOrEntity	引くオブジェクトの番号(省略した場合、
	 *								最後の要素)
	 * @returns	{?Entity}	引かれた子オブジェクト(引かれなかった場合 null)
	 */
	pull(indexOrEntity) {
		if (indexOrEntity===void 0) indexOrEntity = this.children.length - 1;
		let index;
		if (indexOrEntity instanceof Entity) {
			index = this.children.indexOf(indexOrEntity);
			if (index === -1) return null;
		} else {
			index = indexOrEntity;
			if (index < 0 || index >= this.children.length)
				throw new Error("pull で指定された index が不正です:"+index);
		}
		const child = this.children[index];
		this.children.splice(index, 1); // 削除
		child.setParent(null);
		return child;
	}
	
	/**
	 * 向きを設定します。子の向きも変更します。
	 * @override
	 * @param	{number} direction	向き定数
	 */
	setDirection(direction) {
		this.direction = direction;
		this.children.forEach( (child) => {
			child.setDirection(direction);
		});
	}
	/**
	 * この Entities の位置、大きさを取得します。layout が設定されている場合、
	 * layout によりこのオブジェクトのサイズを変更後、返却します。
	 * @return	{object} x, y, w, h を含むオブジェクト
	 */
	getRect() {
		if (this.layout != null) {
			const d = this.layout.layoutSize(this);
			this.w = d.w;
			this.h = d.h;
		}
		
		return { x:this.x, y:this.y, w:this.w, h:this.h };
	}
	
	/**
	 * この Entities の大きさを取得します。layout が設定されている場合、
	 * layout によりこのオブジェクトのサイズを変更後、返却します。
	 * @return	{object} w, h を含むオブジェクト
	 */
	getSize() {
		if (this.layout != null) {
			const d = this.layout.layoutSize(this);
			this.w = d.w;
			this.h = d.h;
		}
		return { w: this.w, h: this.h };
	}
	
	/**
	 * レイアウトを持っている場合、レイアウトします。
	 */
	selfLayout() {
		if (this.layout) this.layout.layout(this);
	}

	/**
	 * この Entity を描画します。
	 * 描画前に設定されている layout があればレイアウトが再計算されます。
	 * このメソッドは親オブジェクトからのみ呼ばれます。
	 * @override
	 */
	draw(ctx) {
		if (this.layout != null) this.layout.layout(this);
		this.children.forEach( (child) => {
			child.draw(ctx);
		});
	}
	/**
	 * 指定された位置を表示位置として占有している Entity を取得します。
	 * このメソッドでは、上に位置するものを優先して返却します。
	 * Entities が返却されることはありません。
	 *
	 * @param	{number} x		表示位置 X
	 * @param	{number} y		表示位置 Y
	 * @returns	{Entity} その位置に表示している Entity
	 */
	getEntityAt(x, y) {
		//console.log('getEntityAt('+x+','+y+')');
		let n;
		for (n = this.children.length - 1; n >= 0; n--) {
			const bounds = this.children[n].getRect();
			//console.log('checking '+this.children[n].toString()+'('+bounds.x+','+bounds.y+')-('+(bounds.x+bounds.w)+','+(bounds.y+bounds.h)+') dir='+this.children[n].direction);
			if (x >= bounds.x && x <= (bounds.x + bounds.w) &&
				y >= bounds.y && y <= (bounds.y + bounds.h)) {
				if (this.children[n] instanceof Entities) {
					//console.log('recursive call getEntityAt() for '+this.children[n].toString());
					const ent = this.children[n].getEntityAt(x, y);
					//console.log('ent='+((ent)?ent.toString():'null'));
					if (ent) return ent;
				} else {
					return this.children[n];
				}
			}
		}
		return null;
	}
}

/**
 * Packet class 定義
 *
 * @classdesc Packet は複数の Card をまとめたハンドを表します。
 * 以前の GuiedPacket の機能の他、PacketFactory のクラスメソッドも
 * 合わせもちます。
 * @constructor
 * @extends Entities
 */
class Packet extends Entities {

/*----------------
 * instance field
 */
	layout;
	cardOrder;

	constructor(source) {
		super();
		if (source && source instanceof Packet) {
			this.cardOrder = source.cardOrder;
			this.layout = source.layout;
			source.children.forEach( c => this.add(c) );
		} else {
			this.layout = new CardHandLayout();
			this.cardOrder = new NaturalCardOrder(); // スペードスート
		}
	}

/*------------------
 * instance methods
 */
	/**
	 * 所属しているカードをシャッフルします。
	 * 利用する乱数は ReproducibleRandom です。
	 * @param		{number} seed	乱数の種。無指定の場合、利用中の乱数系列を使う。
	 */
	shuffle(seed) {
		if (this.children.length == 0) return;
		if (seed) {
			ReproducibleRandom.setSeed(seed); // seed
		}
		const tmp = []; // Card
		const size = this.children.length;
		for (let i = 0; i < size; i++) {
			const index = ReproducibleRandom.nextInt(0, this.children.length-1);
			tmp.push(this.children[index]); // 取得
			this.children.splice(index,1); // 削除
		}
		this.children = tmp;
	}
	
	/**
	 * カードを設定されている CardOrder に従って降順に並べ替えます。
	 */
	arrange() {
		const co = this.cardOrder;
		this.children.sort( function(a,b) { return co.compare(b, a); } );
	}
	
	/**
	 * 指定されたスートのカードの枚数を返却します。
	 * @param	{number} suit	スート
	 * @return	{number} スートの枚数
	 */
	countSuit(suit) {
		let c = 0;
		for (let i = 0; i < this.children.length; i++) {
			if (this.children[i].suit == suit) c++;
		}
		return c;
	}
	
	/**
	 * 指定されたバリューのカードの枚数を返却します。
	 * @param	{number} value	バリュー
	 * @return	{number} 枚数
	 */
	countValue(value) {
		let c = 0;
		const size = this.children.length;
		for (let i = 0; i < size; i++) {
			if (this.children[i].value == value) c++;
		}
		return c;
	}
	
	/**
	 * 指定されたスートのカードを抽出して返却します。
	 * @param	{number} suit	スート
	 * @return	{Packet}	指定されたスートからなる Packet
	 */
	subpacket(suit) {
		const result = new Packet();
		const size = this.children.length;
		for (let i = 0; i < size; i++) {
			const card = this.children[i];
			if (card.suit == suit) result.add(card);
		}
		return result;
	}
	
	/**
	 * 含まれるカードの表裏を設定します。
	 * @param	{boolean} head	表にする場合 true、裏にする場合 false
	 */
	turn(head) {
		for (let i = 0; i < this.children.length; i++) {
			this.children[i].isHead = head;
		}
	}
	
	/**
	 * Packet 同士の集合減算をします。this - target の Packet を返します。
	 * children に対する処理を行い、新しい Packet インスタンスが返却されます。
	 * サブクラスで利用することは一般にはできないので注意して下さい。
	 * @param	{Packet} target	差をとる対象
	 * @return	{Packet} this - target
	 */
	sub(target) {
		const result = new Packet();
		
		for (let i = 0; i < this.children.length; i++) {
			const c = this.children[i];
			if (target.children.indexOf(c) === -1) result.add(c);
		}
		return result;
	}

	/**
	 * インデックスまたはカードを指定してこの Packet からひきます。
	 * 引数を省略した場合、一番最後のカードを引きます。
	 * 
	 * @param {number|Card} indexOrCardOrSuit インデックスまたは Card または suit
	 * @param {number} value カードの数字、これを指定した場合第一パラメータは suit
	 * @returns		{Card} 引いたカード
	 */
	pull(indexOrCardOrSuit, value) {
		if (value === void 0) {
			if (typeof indexOrCardOrSuit === 'number' || indexOrCardOrSuit === void 0)
				return super.pull(indexOrCardOrSuit);
			return super.pull(this.indexOf(indexOrCardOrSuit));
		}
		return super.pull(this.indexOf(indexOrCardOrSuit, value));
	}

	/**
	 * 指定された Card または suit, value の組と同種の Card の index を返却します。
	 * 存在しない場合、 -1 を返却します。
	 * @param {Card|number} cardOrSuit Card または suit
	 * @param	{number} value カードの数字
	 * @return	{number} index
	 */
	indexOf(cardOrSuit, value) {
		let suit;
		if (value === void 0) {
			suit = cardOrSuit.suit;
			value = cardOrSuit.value;
		} else {
			suit = cardOrSuit;
		}

		for (let i = 0; i < this.children.length; i++) {
			if (this.children[i].suit === suit &&
				 this.children[i].value === value) return i;
		}
		return -1;
	}

	/**
	 * 指定された Card と同種のカードを含んでいる場合、この Packet の持つそのカード
	 * への参照を返却します。
	 * index 指定の peek(index) は、単に packet.children[index] を利用してください。
	 * 
	 * @param	{number|Card}	suitOrCard	suit または Card 種別
	 * @param	{number?}		value		value (suit を指定した場合)
	 * @returns	{Card}	指定された種別のカードへの参照、この Packet が保持していない場合 null
	 */
	peek(suitOrCard, value) {
		let suit;
		if (typeof suitOrCard === 'number' && value!==void 0 && typeof value === 'number') {
			suit = suitOrCard; // suit
		} else if (value === void 0) {
			suit = suitOrCard.suit;
			value = suitOrCard.value;
		} else {
			throw new Error("suirOrCard には Card を指定するか、value とともにカード種別を指定してください。");
		}
		for (let i = 0; i < this.children.length; i++) {
			if (this.children[i].suit === suit &&
				 this.children[i].value === value) return this.children[i];
		}
		return null;
	}

	/**
	 * 本 Packet に含まれていないカードの集合(残りカード)を取得する。
	 * このカードのインスタンスは provideDeck() で新たに生成されたものです。
	 * @returns {Packet} この Packet に含まれていないカードの集合
	 */
	complement() {
		return Packet.provideDeck().sub(this);
	}
	
	/**
	 * @override
	 */
	toString() {
		let s = "";
		const size = this.children.length;
		for (let i = 0; i < size; i++) {
			s += this.children[i].toString();
		}
		return "{" + s + "}";
	}
	
/*---------------
 * class methods
 */
	/**
	 * JOKER を含まない１揃いのカードを返却します。(4 x 13 = 52 枚)
	 * @return	{Packet} 52枚のカードセット
	 */
	static provideDeck() {
		const p = new Packet();
		for (let suit = 1; suit < 5; suit++) {
			for (let value = 1; value < 14; value++) {
				p.add(new Card(suit, value));
			}
		}
		return p;
	}
	
	/**
	 * pile から1枚ずつ hands (Packet の配列) に均等に配ります
	 *
	 * @param	{Packet}	pile	配る元の Packet
	 * @param	{Array.<Packet>}	hands	配り先の Packet の配列
	 * @param	{number}	begin	配りはじめる hand の番号
	 */
	static deal(pile, hands, begin) {
		if (begin===void 0) begin = 0;
		
		const n = hands.length;
		if (begin < 0 || begin >= n)
			throw new Error("deal で begin の値が不正です:"+begin);
		
		const c = pile.children.length;
		for (let i = 0; i < c; i++) {
			const card = pile.pull();
			hands[ (i+begin) % n ].add(card);
		}
	}
}

/**
 * Quit ボタンを押し、中断することを選択したことを示す例外です。
 * ブリッジの中で、ブロックするメソッド(async)は、中断決定時に
 * 即座にブロックを解除し、この例外をスローすることが求められます。
 */
class QuitInterruptException {
	//
}

/**
 * Field class 定義。Java 版の BridgeField に相当。
 * 実際の canvas への描画の他、クリック検出、一時停止など、表示、入力、
 * async メソッドを持ちます。
 * 
 * @classdesc カードなどが描画されるブラウザ上の画面領域
 * @constructor
 * @extends	Entities
 */
class Field extends Entities {
	/** @type {Element} html 内の canvas 要素 */
	canvas;
	/** @type {Canvas} canvas の 2D context */
	ctx;
	/** @type {number} spot light の位置 (0-3), -1 は描画しない */
	spot;
	/** @type {Function[]} 中断ボタン押下時に割り込み例外をスローするため reject を登録 */
	rejects;

	/**
	 * 指定された canvas 名に描画する Field を生成します。
	 * @param {string} canvasId 
	 */
	constructor(canvasId) {
		super();
		this.canvas = document.getElementById(canvasId);
		this.setSize(canvas.clientWidth, canvas.clientHeight);
		this.ctx = canvas.getContext('2d');
		this.spot = 0;
		this.rejects = [];
		
		this.layout = null; // pos/size は初期値のまま
	}


/*------------------
 * instance methods
 */
	/** @override */
	getRect() {
		return { x:this.x, y:this.y, w:this.w, h:this.h };
	}
	/** @override */
	draw() {
		this._drawBackground_();
		for (let i = 0; i < this.children.length; i++) {
			this.children[i].draw(this.ctx);
		}
		this._drawSpotlight_();
	}
	
	/**
	 * 背景を描画します(緑基調、下が明るい)
	 * @private
	 */
	_drawBackground_() {
		// バック(緑のグラデーション)
		const r = this.getRect();
		const step = r.h / 40;
		for (let i = 0; i < 40; i++) {
			this.ctx.fillStyle = 'rgb(' + (40+i/2) + ','+(80+i)+',30)'; // 緑
			this.ctx.fillRect(r.x, r.y+Math.floor(i*step), r.w, r.h+Math.floor(step+1));
		}
	}
	
	/**
	 * スポットライトを描画します(黄白色)
	 * spot に座席番号を指定します。-1 では描画しません。
	 * @private
	 */
	_drawSpotlight_() {
		const spot = this.spot;
		if (spot == -1) return;
		// 順番を示すスポットライト
		const r = this.getRect();
		const direction = this.direction;
		
		const d = (spot + direction)%4;
		let x, y;
		
		switch (d) {
		
		default:
		case Entity.UPRIGHT: // 上
			x = r.w / 2; y = 40;
			break;
			
		case 1: // 右
			x = r.w - 40; y = r.h /2;
			break;
			
		case 2: // 下
			x = r.w / 2; y = r.h - 40;
			break;
			
		case 3: // 左
			x = 40; y = r.h / 2;
			break;
			
		}
		
		for (let radius = 95; radius > 0; radius -=4) {
			this.ctx.fillStyle = 'rgba(255,255,192,'+((95.0-radius)/1000)+')'; // 明るい黄色(透明度つき)
			this.ctx.beginPath();
			this.ctx.arc(x, y, radius, 0, 2*Math.PI, false);
			this.ctx.fill();
		}
	}

	/**
	 * ブロック時に中断ボタンによる割り込み(Promiseのreject)を行うための関数を
	 * 登録します。配列への push を行っていますが、このプログラムでは同時に２つ
	 * 以上のブロックは発生しないため、多くて１つの要素からなるはずです。
	 * @param {Function} rej 実行をブロックする Promise での rejectFunction
	 */
	addReject(rej) {
		if (this.rejects.includes(rej)) return;
		this.rejects.push(rej);
	}

	/**
	 * ブロック処理が終了する際、登録していた Promise の reject を削除します。
	 * @param {Function} rej 実行をブロックする Promise での rejectFunction
	 */
	removeReject(rej) {
		for (let i = 0; i < this.rejects.length; i++) {
			if (this.rejects[i].id === rej.id) {
				this.rejects.splice(i, 1);
				i--;
			}
		}
	}

	/**
	 * 中断ボタン後 OK した際に、すべてのブロックしている async メソッドで
	 * QuitInterruptException をスローさせます。
	 * this.rejects は空になります。
	 */
	interrupt() {
		while (this.rejects.length > 0) {
			const rej = this.rejects.pop();
			rej.rej(new QuitInterruptException('quit'));
		}
	}

	newReject(rej) {
		if (!this.id) this.id = 0;
		return { id: this.id++, rej: rej};
	}

	/**
	 * カードがクリックされるのを検出する async 関数です。
	 * クリック位置は MouseEvent の offsetX, offsetY で検出します。
	 * @async
	 * @returns	{Promise<Card>} クリックされた Card オブジェクト。必ず値が入ります。
	 */
	waitCardSelect() {
		return new Promise( (res, rej) => {
			const reject = this.newReject(rej);
			this.addReject(reject);
			const listener = (e) => {
				const x = e.offsetX;
				const y = e.offsetY;
				const ent = this.getEntityAt(x, y);
				if (ent && (ent instanceof Card)) { // Card
					const selectedCard = ent;
//					console.log('selected ' + selectedCard.toString());
					this.canvas.removeEventListener('click', listener);
					this.removeReject(reject);
					res(selectedCard);
				}
			};
			this.canvas.addEventListener('click', listener);
		});
	}

	/**
	 * どこかがクリックされるまで待つ async 関数です。
	 * millis を指定することで、最大待ち時間を設定できます。
	 * @async
	 * @returns		{Promise<boolean>} クリックされた場合 true
	 */
	waitClick(millis) {
		return new Promise( (res, rej) => {
			const reject = this.newReject(rej);
			this.addReject(reject);
			const listener = () => {
				this.canvas.removeEventListener('click', listener);
				this.removeReject(reject);
				res(true);
			};
			this.canvas.addEventListener('click', listener);
			if (millis) {
				setTimeout( () => {
					this.canvas.removeEventListener('click', listener);
					this.removeReject(reject);
					res(false);
				}, millis);
			}
		});
	}

	/**
	 * 指定時間(msec)待つ async 関数です。
	 * quit 割り込みを管理するため instance method になっています。
	 * @async
	 * @param {number} millis
	 * @returns {Promise<void>}
	 */
	sleep(millis) {
		return new Promise( (res, rej) => {
			const reject = this.newReject(rej);
			this.addReject(reject);
			setTimeout( () => {
				this.removeReject(reject);
				res();
			}, millis);
		});
	}


}

/**
 * Canvas 上のボタンです。
 * 一度だけ生成、追加されることを期待しています。
 * field.canvas への event listner を削除しません。
 */
class Button extends Entity {
	listener;

	static BASE_COLOR = 'rgb(240,240,240)';
	static OVER_COLOR = 'rgb(128,128,128)';
	static PRESS_COLOR = 'rgb(100,100,100)';

	/**
	 * ボタンを生成します。
	 * @param {Field} field ブリッジの Field
	 * @param {string} caption ボタンに表示する文字列
	 */
	constructor(field, caption) {
		super();
		this.field = field;
		this.caption = caption;
		const canvas = field.canvas;
		canvas.addEventListener('mousemove', (evt) => this.onMouseMove(evt), false);
		canvas.addEventListener('click', (evt) => this.onClick(evt), false);
		canvas.addEventListener('mousedown', (evt) => this.onMouseDown(evt), false);
		canvas.addEventListener('mouseup', (evt) => this.onMouseUp(evt), false);
		this.baseColor = Button.BASE_COLOR;
		this.color = this.baseColor;
		this.pressed = 0;
	}

/*------------------
 * instance methods
 */
	/**
	 * クリック時のハンドラーを設定します。
	 * @param {Function} listener onClick 時に呼ばれる関数 void → void
	 */
	setListener(listener) {
		this.listener = listener;
	}

	/**
	 * 指定された座標がこのボタンの内側にあるかを判定します。
	 * @private
	 * @param {number} x 判定する x 座標
	 * @param {number} y 判定する y 座標
	 */
	_isInternal_(x, y) {
		return (this.x <= x && x <= (this.x + this.w) &&
			this.y <= y && y <= (this.y + this.h));
	}

	/**
	 * マウス移動を検知したときのハンドラ
	 * @param {MouseEvent} evt イベントオブジェクト
	 */
	onMouseMove(evt) {
		if (!this.isVisible) return;
		if (this._isInternal_(evt.offsetX, evt.offsetY)) {
			this.color = Button.OVER_COLOR;
		} else {
			this.color = Button.BASE_COLOR;
		}
		this.pressed = 0;
		this.field.draw();
	}

	/**
	 * マウスボタン押下したときのハンドラ
	 * @param {MouseEvent} evt イベントオブジェクト
	 */
	onMouseDown(evt) {
		if (!this.isVisible) return;
		if (!this._isInternal_(evt.offsetX, evt.offsetY)) return;
		this.color = Button.PRESS_COLOR;
		this.pressed = 1;
		this.field.draw();
	}
	
	/**
	 * マウスボタンを離したときのハンドラ
	 * @param {MouseEvent} evt イベントオブジェクト
	 */
	onMouseUp(evt) {
		if (!this.isVisible) return;
		if (!this._isInternal_(evt.offsetX, evt.offsetY)) return;
		this.color = Button.OVER_COLOR;
		this.pressed = 0;
		this.field.draw();
	}

	/**
	 * クリック時のハンドラ
	 * @param {MouseEvent} evt イベントオブジェクト
	 */
	onClick(evt) {
		if (!this.isVisible) return;
		if (!this._isInternal_(evt.offsetX, evt.offsetY)) return;
		this.pressed = 0;
		this.color = Button.BASE_COLOR;
		this.field.draw();
		if (this.listener) this.listener();
	}

	/**
	 * このボタンを描画します
	 * @param {Context} ctx グラフィックコンテキスト
	 */
	draw(ctx) {
		if (!this.isVisible) return;
		const r = this.getRect();
		ctx.fillStyle = this.color; //'rgb(0,32,0)'; // back color
		ctx.fillRect(r.x, r.y, r.w, r.h);
		ctx.font = 'normal 13px SanSerif';
		ctx.fillStyle = 'rgb(64,64,64)'; // gray
		const tm = ctx.measureText(this.caption);
		ctx.fillText(this.caption,
			this.pressed + r.x + Math.floor((r.w - tm.width)/2),
			this.pressed + r.y + 16);
	}
}
	
/**
 * Card class 定義
 *
 * @classdesc Card クラスでは、java 版の Unspecified の枠組みを省略しました。
 * また、GuiedCard となっています。
 * @constructor
 * @extends Entity
 */
class Card extends Entity {

/*---------------
 * static fields
 */
	static XSIZE = 59;
	static YSIZE = 87;
	static XSTEP = 14;
	static YSTEP = 16;

	/** @const {number} suit 値用の定数 */
	static JOKER = 0; // 利用されない(JOKER は value も同一値)
	/** @const {number} スペードを示す定数(4) */
	static SPADE = 4;
	/** @const {number} ハートを示す定数(3) */
	static HEART = 3;
	/** @const {number} ダイヤを示す定数(2) */
	static DIAMOND = 2;
	/** @const {number} クラブを示す定数(1) */
	static CLUB = 1;

	/** @const {number} ACE を表す value 値(1) */
	static ACE = 1;
	/** @const {number} JACK を表す value 値(11) */
	static JACK = 11;
	/** @const {number} QUEEN を表す value 値(12) */
	static QUEEN = 12;
	/** @const {number} KING を表す value 値(13) */
	static KING = 13;

	/** @const {string} "JCDHS" のスーツ文字列です */
	static SUIT_LETTERS = "JCDHS";
	/** @const {string} "oA23456789TJQK" の値文字列です */
	static VALUE_LETTERS = "oA23456789TJQK";

	/** @type	{Number} このカードのスートを表す 0～4 の数値。スート定数を参照。 */
	suit;

	/** @type	{Number} このカードのバリューを表す数値。1(ACE)～13(KING) の値をとる。 */
	value;

	/** @type	{Boolean} このカードが表向きかどうかを保持。 */
	isHead;

/*-------------
 * constructor
 */
	constructor(suit, value) {
		super();
		this.setSize(Card.XSIZE, Card.YSIZE);
		this.suit = suit;
		this.value = value;
		this.isHead = true;
	}

/*------------------
 * instance methods
 */
	/**
	 * カードを描画します。
	 * @param	{Context} ctx	2Dグラフィックコンテキスト
	 * @override
	 */
	draw(ctx) {
		if (!this.isVisible) return;
		let x,y, c,s;
		const r = this.getRect();
		switch (this.direction) {
		case 1:
			c = 0; s = 1;	x = r.y;		y = -r.x - r.w; break;
		case 2:
			c = -1; s = 0;	x = -r.x - r.w;	y = -r.y - r.h; break;
		case 3:
			c = 0; s = -1;	x = -r.y - r.h;	y = r.x; break;
		default:
			c = 1; s = 0;	x = r.x;		y = r.y; break;
		}
		let img;
		if (this.isHead) img = CardImageHolder.getImage(this.suit, this.value);
		else img = CardImageHolder.getBackImage();
		
		ctx.setTransform(c, s, -s, c, 0, 0);
		if ((this.direction & 1) === 0) ctx.drawImage(img, x, y, this.w, this.h);
		else ctx.drawImage(img, x, y, this.h, this.w);
		ctx.setTransform(1, 0, 0, 1, 0, 0);
	}
	
	/**
	 * この Entity の位置、大きさを取得します。
	 * @override
	 * @return		{Object} x: x座標, y: y座標, w: 幅, h: 高さ
	 */
	getRect() {
		return {x: this.x, y: this.y, w: this.w, h: this.h };
	}
	
	/**
	 * Card の文字列表現を得ます。
	 * @return	{string}	このカードの文字列表現
	 */
	toString() {
		let s;
		if (this.isHead) s = "/"; else s = "_";
		s += Card.SUIT_LETTERS[this.suit] + Card.VALUE_LETTERS[this.value];
		return s;
	}
}

/**
 * CardImageHolder class
 * @classdesc カードのグラフィックを取得する static メソッドを提供します。
 */
class CardImageHolder {
	constructor() {
		throw new Error("can not instantiate CardImageHolder");
	}

	/** @const	{Image[]} カード表面のイメージオブジェクトの配列 */
	static IMAGE = [];
	/** @const	{Image[]} カード裏面のイメージオブジェクト配列 */
	static BACK_IMAGE = [];
	/** @const	{Image[]} すみれ画像(3つ) */
	static SUMIRE = [];
	
	/** @type	{number} カード裏面イメージの番号(0-3) */
	static backImageNumber = 0;

	/**
	 * カードイメージを読み込みます。
	 */
	static loadImages(path) {
		if (CardImageHolder._loaded) return;
		if (path===void 0) path = 'images/';
		if (!path.endsWith('/')) path = path + '/';
		// カード表面の読み込み
		const s = 'cdhs';
		for (let i = 0; i < s.length; i++) {
			const suit = s[i];
			for (let value = 1; value < 14; value++) {
				const imgsrc = path+suit+value+'.gif?'+ new Date().getTime(); // nocache
				const img = new Image();
				img.src = imgsrc;
				CardImageHolder.IMAGE.push(img);
			}
		}
		// カード裏面の読み込み
		for (let i = 0; i < 4; i++) {
			const imgsrc = path+'back'+i+'.gif?'+ new Date().getTime();
			const img = new Image();
			img.src = imgsrc;
			CardImageHolder.BACK_IMAGE.push(img);
		}
		// すみれ画像の読み込み
		for (let i = 0; i < 3; i++) {
			const imgsrc = path+'sumire'+i+'.gif?'+ new Date().getTime();
			const img = new Image();
			img.src = imgsrc;
			CardImageHolder.SUMIRE.push(img);
		}
	}
	// execute loadImages(static initializer)
	static _loaded = CardImageHolder.loadImages();
	
	/**
	 * カードの表面イメージを取得します。
	 *
	 * @param	suit	スート(1,2,3,4)
	 * @param	value	値(1-13)
	 * @returns		{Image}		カード表面イメージ
	 */
	static getImage(suit, value) {
		const s = (suit-1)*13 + (value-1);
		return CardImageHolder.IMAGE[s];
	}
	
	/**
	 * カードの裏面イメージを取得します。
	 * @returns		{Image}		裏面イメージ
	 */
	static getBackImage() {
		return CardImageHolder.BACK_IMAGE[CardImageHolder.backImageNumber];
	}

	/**
	 * カードの裏面イメージを切り替えます。
	 * @param	{number} num	裏面イメージ番号(0-3)
	 */
	static setBackImage(num) {
		CardImageHolder.backImageNumber = num;
	}

}
	
/**
 * CardHandLayout class 定義
 */
class CardHandLayout {

/*------------------
 * instance methods
 */
	/**
	 * 通常のハンドのレイアウトを施します。
	 * @param	{Entities} entities レイアウトを行う対象 Entities
	 */
	layout(entities) {
		let xpos, ypos, n;
		
		n = entities.children.length;
		
		const direction = entities.direction;
		const r = entities.getRect();
		
		switch (direction) {
		
		case Entity.UPRIGHT:
			// direction == 0 の場合の実装
			xpos = r.x;
			ypos = r.y;
			
			for (let i = 0; i < n; i++) {
				const ent = entities.children[i];
				ent.setPosition(xpos, ypos);
				//ent.setSize(Card.XSIZE, Card.YSIZE);
				xpos += Card.XSTEP;
			}
			entities.setSize(Card.XSIZE + (n-1)*Card.XSTEP, Card.YSIZE);
			break;
			
		case Entity.RIGHT_VIEW:
			// direction == 1 の場合の実装
			xpos = r.x;
			ypos = r.y + (n-1) * Card.XSTEP;
			
			for (let i = 0; i < n; i++) {
				const ent = entities.children[i];
				ent.setPosition(xpos, ypos);
				//ent.setSize(Card.YSIZE, Card.XSIZE);
				ypos -= Card.XSTEP;
			}
			entities.setSize(Card.YSIZE, Card.XSIZE + (n-1)*Card.XSTEP);
			break;
			
		case Entity.UPSIDE_DOWN:
			// direction == 2 の場合の実装
			xpos = r.x + (n-1) * Card.XSTEP;
			ypos = r.y;
			
			for (let i = 0; i < n; i++) {
				const ent = entities.children[i];
				ent.setPosition(xpos, ypos);
				//ent.setSize(Card.XSIZE, Card.YSIZE);
				xpos -= Card.XSTEP;
			}
			entities.setSize(Card.XSIZE + (n-1)*Card.XSTEP, Card.YSIZE);
			break;
			
		case Entity.LEFT_VIEW:
			// direction == 1 の場合の実装
			xpos = r.x;
			ypos = r.y;
			
			for (let i = 0; i < n; i++) {
				const ent = entities.children[i];
				ent.setPosition(xpos, ypos);
				//ent.setSize(Card.YSIZE, Card.XSIZE);
				ypos += Card.XSTEP;
			}
			entities.setSize(Card.YSIZE, Card.XSIZE + (n-1)*Card.XSTEP);
			break;
		
		default:
			throw new Error("direction の値が不正です：" + direction);
		}
	}
	
	/**
	 * 実際のレイアウトは変更せず、レイアウトした場合の大きさを計算します。
	 * @param	{Entities} entities 計算対象の Entities
	 */
	layoutSize(entities) {
		const n = entities.children.length;
		
		if (n == 0) return { w: 0, h: 0 };
		
		const direction = entities.direction;
		
		switch (direction) {
		
		case Entity.UPRIGHT:
		case Entity.UPSIDE_DOWN:
			return { w:(n-1)*Card.XSTEP + Card.XSIZE, h:Card.YSIZE };
			
		case Entity.RIGHT_VIEW:
		case Entity.LEFT_VIEW:
			return { w:Card.YSIZE, h:(n-1)*Card.XSTEP + Card.XSIZE };
		
		default:
			throw new Error("direction の値が不正です：" + direction);
		}
	}
}

/**
 * Bid class定義
 *
 * @classdesc ビッド、またはコントラクトを表現するクラスです。
 * @param	{number} kind	ビッド種類(Bid.PASS, Bid.BID, Bid.DOUBLE,
 * 							Bid.REDOUBLE)
 * @param	{number} level	ビッドレベル(1～7)
 * @param	{number} suit	ビッドスーツ(1-4)、または Bid.NO_TRUMP(5)
 * @version		8, July 2018
 * @author		Yusuke Sasaki
 */
class Bid {

/*------------------
 * static constants
 */
	/** @const {number} kind (Bid) を表す定数. */
	static BID			= 0;
	
	/** @const {number} kind (Pass) を表す定数. */
	static PASS		= 1;
	
	/** @const {number} kind (Double) を表す定数. */
	static DOUBLE		= 2;
	
	/** @const {number} kind (Redouble) を表す定数. */
	static REDOUBLE	= 3;
	
	/** @const {number} bid suit (club) を表す定数(=1). */
	static CLUB		= Card.CLUB;	// = 1;
	
	/** @const {number} bid suit (diamond) を表す定数(=2). */
	static DIAMOND		= Card.DIAMOND;	// = 2;
	
	/** @const {number} bid suit (heart) を表す定数(=3). */
	static HEART		= Card.HEART;	// = 3;
	
	/** @const {number} bid suit (spade) を表す定数(=4). */
	static SPADE		= Card.SPADE;	// = 4;
	
	/** @const {number} bid suit (no trump) を表す定数(=5). */
	static NO_TRUMP	= 5;

	/** @type {number} ビッドの種類 Bid.BID, Bid.PASS, Bid.DOUBLE, Bid.REDOUBLE */
	kind;
	/** @type {number} ビッドのレベル(1-7) */
	level;
	/** @type {number} ビッドスート(Bid.CLUB=Card.CLUB など) */
	suit;

	constructor(kind, level, suit) {
		if (kind != Bid.PASS && (level===void 0 || suit===void 0)) {
			throw new Error("pass でない場合は引数が必要です");
		}
		if (kind == Bid.PASS && (level!==void 0 || level!==void 0))
			throw new Error("pass では引数は1つです");
		if (kind == Bid.PASS && level===void 0 && suit===void 0) {
			level = 0; suit = 0; // pass object
		}
		if ((kind < Bid.BID)||(kind > Bid.REDOUBLE))
			throw new Error("Illegal Bid kind : " + kind);
		if ((level < 0)||(level > 7))
			throw new Error("Illegal level : " + level);
		if ((suit < 0)||(suit > Bid.NO_TRUMP))
			throw new Error("Illegal suit : " + suit);
		this.kind  = kind;
		this.level = level;
		this.suit  = suit;
	}

	
/*------------------
 * instance methods
 */
	/**
	 * 指定されたコントラクトのあとに宣言可能か判定します。<BR>
	 * 例) [1C] < [1D] < [2C] < [2CX] < [2CXX] となります。
	 * [pass] は常に可能(true)です。
	 * @param	{Bid} contract 判定先となるコントラクト
	 * @returns	{boolean} ビッド可能か
	 */
	isBiddableOver(contract) {
		switch (this.kind) {
		
		case Bid.PASS:
			return true;
		
		case Bid.BID:
			if (contract == null) return true;
			if (this.level > contract.level) return true;
			if (this.level < contract.level) return false;
			if (this.suit > contract.suit) return true;
			return false;
			
		case Bid.DOUBLE:
			if (contract == null) return false;
			if (contract.kind != Bid.BID) return false;
			if (contract.suit != this.suit) return false;
			if (contract.level != this.level) return false;
			return true;
			
		case Bid.REDOUBLE:
			if (contract == null) return false;
			if (contract.kind != Bid.DOUBLE) return false;
			if (contract.suit != this.suit) return false;
			if (contract.level != this.level) return false;
			return true;
		
		default:
			throw new Error("kind が不正な値" + kind + "です");
		}
	}

/*-----------
 * Overrides
 */
	/**
	 * @override
	 */
	toString() {
		if (this.kind === Bid.PASS) return "[pass  ]";
		let trailer = "  ]";
		if (this.kind === Bid.DOUBLE) trailer = "X ]";
		else if (this.kind === Bid.REDOUBLE) trailer = "XX]";
		return "[" + this.level + " " +
				" C D H SNT".substring(this.suit*2-2, this.suit*2) + trailer;
	}
}
	
/**
 * DummyHandLayout class 定義
 *
 * @classdesc ダミーハンドのレイアウトです。
 * 			のレイアウトは、対象となる Packet が NaturalCardOrder で
 * 			arrange() されていることを想定して処理されます。
 */
class DummyHandLayout {
	constructor() {
	}

/*------------------
 * instance methods
 */
	/**
	 * ダミーハンドのレイアウトを施します。
	 * @param	{Entities} entities レイアウトを行う対象 Entities
	 */
	layout(packet) {
		const co = packet.cardOrder; // targeting NaturalCardOrder
		const suitOrder = co.suitOrder;
		
		const count = this._countSuits_(packet, suitOrder);
		
		if (count[0] + count[1] + count[2] + count[3] == 0) return;
		
		// サイズ計算用
		let maxCount = -1;
		for (let i = 0; i < 4; i++) {
			if (maxCount < count[i]) maxCount = count[i];
		}
		
		//
		let xpos, ypos, n = 0;
		const dir = ( packet.direction + 2 ) % 4;
		const r = packet.getRect();
		
		switch (packet.direction) {
		
		case Entity.UPRIGHT:
			 xpos = r.x + 3 * (Card.XSIZE + 3);;
			
			// それぞれのカードの配置を行う
			for (let i = 0; i < 4; i++) {
				ypos = r.y + (maxCount-1)*Card.YSTEP;
				for (let j = 0; j < count[i]; j++) {
					const ent = packet.children[n++];
					ent.setPosition(xpos, ypos);
					ent.setDirection(dir);
					ypos -= Card.YSTEP;
				}
				xpos -= Card.XSIZE + 3;
			}
			packet.setSize( (Card.XSIZE + 3)*4, Card.YSIZE + (maxCount-1)*Card.YSTEP );
			break;
			
		case Entity.RIGHT_VIEW:
			ypos = r.y;
			
			for (let i = 0; i < 4; i++) {
				xpos = r.x + (maxCount-1)*Card.YSTEP;
				
				for (let j = 0; j < count[i]; j++) {
					const ent = packet.children[n++];
					ent.setPosition(xpos, ypos);
					ent.setDirection(dir);
					xpos -= Card.YSTEP;
				}
				ypos += Card.XSIZE + 3;
			}
			packet.setSize( Card.YSIZE + (maxCount-1)*Card.YSTEP, (Card.XSIZE + 3)*4 );
			break;
		
		case Entity.UPSIDE_DOWN:
			xpos = r.x;
			
			// それぞれのカードの配置を行う
			for (let i = 0; i < 4; i++) {
				ypos = r.y;
				for (let j = 0; j < count[i]; j++) {
					const ent = packet.children[n++];
					ent.setPosition(xpos, ypos);
					ent.setDirection(dir);
					ypos += Card.YSTEP;
				}
				xpos += Card.XSIZE + 3;
			}
			packet.setSize( (Card.XSIZE + 3)*4, Card.YSIZE + (maxCount-1)*Card.YSTEP );
			break;
			
		case Entity.LEFT_VIEW:
			ypos = r.y + 3 * (Card.XSIZE + 3);
			
			for (let i = 0; i < 4; i++) {
				xpos = r.x;
				for (let j = 0; j < count[i]; j++) {
					const ent = packet.children[n++];
					ent.setPosition(xpos, ypos);
					ent.setDirection(dir);
					xpos += Card.YSTEP;
				}
				ypos -= Card.XSIZE + 3;
			}
			packet.setSize( Card.YSIZE + (maxCount-1)*Card.YSTEP, (Card.XSIZE + 3)*4 );
			break;
		
		default:
			throw new Error("Direction が不正です");
		}
	}
	
	/**
	 * それぞれのスートの枚数を数える.
	 * @private
	 */
	_countSuits_(packet, suitOrder) {
		// それぞれのスートの枚数を数える
		const count = [0, 0, 0, 0];
		
		for (let i = 0; i < packet.children.length; i++) {
			let card = packet.children[i];
			count[4 - suitOrder[card.suit]]++;
		}
		
		return count;
	}
	
	/**
	 * 実際のレイアウトは変更せず、レイアウトした場合の大きさを計算します。
	 * @param	{Entities} entities 計算対象の Entities
	 * @return	{object} w, h を含むオブジェクト
	 */
	layoutSize(packet) {
		const co = packet.cardOrder;
		const suitOrder = co.suitOrder;
		
		const count = this._countSuits_(packet, suitOrder);
		
		//
		// サイズ計算用
		//
		let maxCount = -1;
		for (let i = 0; i < 4; i++) {
			if (maxCount < count[i]) maxCount = count[i];
		}
		switch (packet.direction) {
		
		case Entity.UPRIGHT:
		case Entity.UPSIDE_DOWN:
			return { w:(Card.XSIZE + 3)*4, h:Card.YSIZE + (maxCount-1)*Card.YSTEP };
			
		case Entity.RIGHT_VIEW:
		case Entity.LEFT_VIEW:
			return { w:Card.YSIZE + (maxCount-1)*Card.YSTEP, h:(Card.XSIZE + 3)*4 };
		
		default:
			throw new Error("Direction が不正です");
		}
	}
}

/**
 * BiddingHistory class 定義
 *
 * @classdesc Board の機能のうち、オークションに関係する部分の実際の処理を
 * 受け持つクラスです。
 * @param	{number} dealer ディーラーの座席番号(0-3)
 * @version		8, July 2018
 * @author		Yusuke Sasaki
 */
class BiddingHistory {
	/** @type {number} north を表す座席定数(=0) */
	static NORTH = 0;
	/** @type {number} east を表す座席定数(=1) */
	static EAST = 1;
	/** @type {number} south を表す座席定数(=2) */
	static SOUTH = 2;
	/** @type {number} west を表す座席定数(=3) */
	static WEST = 3;

	/** @type	{number} ディーラーの座席番号(0-3) */
	dealer;
	/** @type	{Bid[]} ビッド履歴。配列の大きさはビッドの回数を示します。 */
	bid;
	/** @type	{Bid} 現時点のコントラクト。最終コントラクトとは限りません。 */
	contract;
	/** @type	{number} 現時点のディクレアラー。最終ディクレアラーとは限りません。 */
	declarer;
	/** @type	{boolean} コントラクトが終了している場合、true */
	finished;

	constructor(dealer) {
		this.dealer = dealer;
		this.bid = [];
		this.contract = null;
		this.declarer = -1;
		this.finished = false;
	}

/*------------------
 * instance methods
 */
	/**
	 * 指定されたビッドが、ビッディングシーケンス上許可されるかテストします。
	 * 座席関係、ビッドの強さなどが調べられます。
	 *
	 * @param		{Bid} b テストするビッド
	 * @return		{boolean} 許可されるビッドか
	 */
	allows(b) { // b is Bid
		if (this.finished) return false;
		
		if (this.contract == null) {
			switch (b.kind) {
			
			case Bid.PASS:
			case Bid.BID:
				return true;
			
			case Bid.DOUBLE:
			case Bid.REDOUBLE:
				return false;
				
			default:
				throw new Error("Bid instance status error: " + b);
			}
		}
		
		// double, redouble
		switch (b.kind) {
		
		case Bid.DOUBLE:
			if (this.contract.kind != Bid.BID) return false;
			if ( (this.contract.suit != b.suit)||
				(this.contract.level != b.level) ) return false;
			if (((this.declarer ^ this.bid.length ^ this.dealer) & 1) != 1) return false;
			break;
		
		case Bid.REDOUBLE:
			if (this.contract.kind != Bid.DOUBLE) return false;
			if ( (this.contract.suit != b.suit)||
				(this.contract.level != b.level) ) return false;
			if (((this.declarer ^ this.bid.length ^ this.dealer) & 1) != 0) return false;
			break;
			
		}
		
		// レベルによる判定
		if ((b.kind != Bid.PASS)&&
			(!b.isBiddableOver(this.contract))) return false;
		
		return true;
	}
	
	/**
	 * ビッドを行い，ビッディングシーケンスを進めます。
	 * 不可能なビッドを行おうとすると、Error がスローされます。
	 *
	 * @param		{Bid} newBid 新たに行うビッド
	 */
	play(newBid) {
		// ビッドできるかのチェックを行う.
		if (!this.allows(newBid))
			throw new Error("Illegal bid:" + newBid.toString());
		
		// ビッドできるので、ビッド履歴に加える.
		this.bid.push(newBid);
		
		// declarer, contract 更新, パス続いたか判定
		switch (newBid.kind) {
		case Bid.PASS:
			// パスが続いたか
			let passCount = 0;
			for (let i = this.bid.length - 1; i >= 0; i--) {
				if (this.bid[i].kind == Bid.PASS) passCount++;
				else break;
			}
			if (passCount < 3) break;
			if ((passCount == 3)&&(this.contract == null)) break;
			
			// Pass out ?
			this.finished = true;
			if (passCount == 4) {
				this.contract = new Bid(Bid.PASS, 0, 0);
				this.declarer = this.dealer;
				return;
			}
			break;
		case Bid.DOUBLE:
		case Bid.REDOUBLE:
			this.contract = newBid;
			break;
		
		case Bid.BID:
			this.contract = newBid;
			
			// declarer を見つける.
			let n;
			for (n = (1 - (this.bid.length & 1)); n < this.bid.length; n += 2) {
				const b = this.bid[n];
				if ((b.kind == Bid.BID)&&
					(b.suit == newBid.suit)) break;
			}
			this.declarer = (n + this.dealer)%4;
			break;
		}
	}
	
	/**
	 * 1手元に戻します。
	 */
	undo() {
		if (this.bid.length == 0)
			throw new Error("ビッドされていないので、undo() できません");
		this.contract	= null;
		this.bid.pop();
		this.declarer	= -1;
		this.finished	= false;
		
		// contract を見つける
		let lastBidCount = 0;
		for (let i = this.bid.length-1; i >= 0; i--) {
			if (this.bid[i].kind == Bid.BID) {
				this.contract = this.bid[i];
				lastBidCount = i+1;
				break;
			}
		}
		
		// declarer を見つける
		let n;
		for (n = (1 - (lastBidCount & 1)); n < lastBidCount; n += 2) {
			const b = this.bid[n];
			if ((b.kind == Bid.BID)&&
				(b.suit == this.contract.suit)) break;
		}
		this.declarer = (n + this.dealer)%4;
	}
	
	/**
	 * 行われたすべてのビッドを配列形式で返却します。
	 * 配列の添字 0 をディーラーのビッドとして以降座席順に格納されています。
	 * 現時点まででビッドされた回数分の要素を含みます。
	 *
	 * @return		{Array.<Bid>} すべてのビッド
	 */
	getAllBids() {
		return this.bid.slice(0, this.bid.length);
	}
	
	/**
	 * 次にビッドする席の番号を返します。
	 *
	 * @return		{number} 席番号
	 */
	getTurn() {
		return (this.dealer + this.bid.length) % 4;
	}
	
	/**
	 * コントラクトを設定します
	 * @param	{Bid} contract コントラクト
	 * @param	{number} ディクレアラーの座席番号
	 */
	setContract(contract, declarer) {
		if ( (this.bid.length != 0)||(this.finished) )
			throw new Error("すでにビッドされているためコントラクトを指定できません。");
		if ( (declarer < BiddingHistory.NORTH)
			||(declarer > BiddingHistory.WEST) )
			throw new Error("declarer の値が不正です。");
		
		this.contract = contract;
		this.declarer = declarer;
		this.finished = true;
	}
	
	/**
	 * ビッド履歴を初期化します。
	 * @param	{number} dealer ディーラー。省略した場合、変更しません。
	 */
	reset(dealer) {
		if (dealer!==void 0) { // 引数がある場合
			this.dealer = dealer;
			this.reset();
			return;
		}
		// 引数がない場合
		this.bid = [];
		this.contract = null;
		this.declarer = -1;
		this.finished = false;
	}
	
/*-----------
 * overrides
 */
	/**
	 * 文字列表現を得る.
	 * @override
	 */
	toString() {
		if ( (this.bid.length == 0)&&(this.finished) )
			return "Bidding Sequence Unknown";
		let result = "   N       E       S       W\n";
		for (let i = 0; i < this.dealer; i++) {
			result += "        ";
		}
		let seat = this.dealer;
		
		for (let i = 0; i < this.bid.length; i++) {
			result += this.bid[i].toString();
			seat++;
			if (seat == 4) {
				result += "\n";
				seat = 0;
			}
		}
		return result + "\n";
	}
}

/**
 * TrickLayout class 定義
 */
class TrickLayout {
	constructor() {
	}

	/**
	 * TrickLayout では、target のサイズを変更しません。
	 * @param	{Trick} target レイアウト対象の Trick
	 */
	layout(target) {
		const trick = target;
		const direction	= trick.direction;
		const leader = trick.leader;
		const size = trick.children.length;
		const x = trick.x;
		const y = trick.y;
		const w = trick.w;
		const h = trick.h;
		
		const d = (4 + leader - direction) % 4;
		
		for (let i = 0; i < size; i++) {
			const ent = trick.children[i];
			
			switch ( (i+d)%4 ) {
			
			case 0: // 上
				ent.setPosition(Math.floor(x + (w - ent.w)/2), y);
				break;
			case 1: // 右
				ent.setPosition(x + (w - ent.w), Math.floor(y + (h - ent.h)/2));
				break;
			case 2: // 下
				ent.setPosition(Math.floor(x + (w - ent.w)/2), y + (h - ent.h) );
				break;
			case 3: // 左
				ent.setPosition(x, Math.floor(y + (h - ent.h)/2));
				break;
			default:
				throw new Error();
			}
		}
	}
	
	/**
	 * size を計算する。
	 * @param	{Trick} target 計算対象の Trick
	 * @return	{object} w, h を含むオブジェクト
	 */
	layoutSize(target) {
		return {w: target.w, h: target.h };
	}
}
	
/**
 * Trick class 定義
 *
 * @classdesc Trick は、場に出ているトリック、プレイされたトリックをパックする。
 * Trick では、１トリックを構成するカードの保持の他、トリックのウィナー
 * の判定を行う。
 * hand に関する情報は保持しない。
 * constructor で Trick(Trick) は未実装。(GuiedTrick にはある)
 * @param	{number} leader leader の座席番号
 * @param	{number} trump trumpスート
 * @extends Packet
 */
class Trick extends Packet {
/*--------------
 * class fields
 */
	/** @const {number} トリックの幅 */
	static WIDTH  = 300;
	/** @const {number} トリックの高さ */
	static HEIGHT = 200;

	/** @type {number} リーダーを示す数値 */
	leader;
	/** @type {number} ウィナーを示す数値 */
	winner;
	/** @type {Card} ウィナーカード */
	winnerCard;
	/** @type {number} トランプスート */
	trump;
	
	/**
	 * 座席番号、トランプスーツを指定して Trick を生成します。
	 * Trick を指定すると、同等の Trick のコピーを作成します。
	 * @param {number|Trick} leaderOrTrick このトリックのleaderの座席番号またはコピー元のTrick
	 * @param {number} trump leaderOrTrick に leader を指定した場合、trump スーツ
	 */
	constructor(leaderOrTrick, trump) {
		super();
		const trickSpecified = (leaderOrTrick instanceof Trick)?leaderOrTrick:false;
		if (trickSpecified) {
			trump = leaderOrTrick.trump;
			leaderOrTrick = leaderOrTrick.leader;
		}
		this.leader = leaderOrTrick;
		this.winner = -1;
		this.winnerCard = null;
		this.trump = trump;
		
		this.direction = Entity.UPRIGHT;
		this.setSize(Trick.WIDTH, Trick.HEIGHT);
		this.layout = new TrickLayout();
		if (trickSpecified) {
			trickSpecified.children.forEach(c => this.add(c));
		}
	}


/*------------------
 * instance methods
 */
	/**
	 * 次は誰の番かを返す.
	 * NESW の順である. Dummy が返ることもある.
	 * @return	{number} 次の番の座席定数
	 */
	getTurn() {
		return ((this.children.length + this.leader) % 4);
	}
	
	/**
	 * このトリックが終っているかテストする。
	 * 終っている場合、winner, winnerCard の値が有効となる。
	 * @return	{boolean} このトリックが終わっているか
	 */
	isFinished() {
		return ( this.children.length == 4 );
	}
	
	/**
	 * Trick は４枚までしか保持せず、４枚そろった時には Winner がきまる.
	 * @param	{Card} card		追加するカード
	 * @override
	 */
	add(card) {
		if (this.isFinished())
			throw new Error("終了した Trick に対して add(Card) できません。");
		
		super.add(card);
		
		if (this.children.length == 4) this._setWinner_();
	}
	
	/**
	 * Winner を lead, trump などから決定します。
	 * @private
	 */
	_setWinner_() {
		if ( this.children.length  == 0) {
			this.winnerCard = null;
			return;
		}
		
		// winner をセットする
		this.winnerCard = this.children[0];
		this.winner = this.leader;
		const starter = this.winnerCard.suit;
		for (let i = 1; i < this.children.length; i++) {
			const c = this.children[i];
			if (this.winnerCard.suit == this.trump) {
				// NO_TRUMP のときはここにこない
				if ((c.suit == this.trump)
						&&(this.winnerCard.value != Card.ACE)) {
					if ((c.value > this.winnerCard.value)||
						(c.value == Card.ACE)) {
						this.winnerCard = c;
						this.winner = ( i + this.leader ) % 4;
					}
				}
			} else {
				// winner のスーツは場のスーツ
				if (c.suit == this.trump) {
					this.winnerCard = c;
					this.winner = ( i + this.leader ) % 4;
				} else if ((c.suit == starter)
							&&(this.winnerCard.value != Card.ACE)) {
					if ((c.value > this.winnerCard.value)
							||(c.value == Card.ACE)) {
						this.winnerCard = c;
						this.winner = ( i + this.leader ) % 4;
					}
				}
			}
		}
	}

	/**
	 * winner の座席番号を取得します。
	 * @returns		{number}	winner の座席番号
	 */
	getWinner() {
		if (this.winnerCard == null) this._setWinner_();
		return this.winner;
	}
	/**
	 * Winner カードを得る.
	 * まだプレイ中であった場合、null が返る仕様であったが、途中での winner も
	 * 返却するように変更された。(2002/8/29)
	 *
	 * @returns	{Card}	winnerカード
	 */
	getWinnerCard() {
		if (this.winnerCard == null) this._setWinner_();
		return this.winnerCard;
	}
	
	toString() {
		const result = "Leader:" + Board.SEAT_STRING[this.leader];
		return result + super.toString();
	}

}

/**
 * PlayHistory class 定義
 *
 * @classdesc PlayHistory クラスは、コントラクトブリッジにおけるプレイ部分
 * 				の状態、ルールをパックします。
 * 				本クラスは Board オブジェクトに保持され、プレイ部分の実処理を
 * 				行います。
 * @version		10, July 2018
 * @author		Yusuke Sasaki
 * @param		{PlayHistory} src コピー元のオブジェクト。
 *								省略時は新規オブジェクトを生成します。
 */
class PlayHistory {
	/** @const {number} 座席定数 */
	static NORTH = BiddingHistory.NORTH;
	/** @const {number} 座席定数 */
	static EAST = BiddingHistory.EAST;
	/** @const {number} 座席定数 */
	static SOUTH = BiddingHistory.SOUTH;
	/** @const {number} 座席定数 */
	static WEST = BiddingHistory.WEST;
	/** @const {string[]} 座席名("North" など) */
	static SEAT_STRING = [ "North", "East", "South", "West" ];

	/** @type	{Packet[]} プレイ中のハンド */
	hand;
	/** @type	{Trick[]} プレイ中のトリック。要素数は 13 以下。 */
	trick;
	/** @type	{number} 現在まででプレイされているトリック数(プレイ中は含まない) */
	trickCount;
	/** @type	{number} トランプスート */
	trump;

	constructor(src) {
		if (src!==void 0) {
			// 代入するのみ(意味は？)
			this.hand = src.hand;
			this.trick = src.trick;
			/** 現在まででプレイされているトリック数(プレイ中は含まない) */
			this.trickCount = src.trickCount;
			this.trump = src.trump;
		} else {
			this.hand = []; // Packet[4]
			this.trick = []; // Trick[13]
			this.trickCount = 0;
			this.trump = -1;
		}
	}


/*------------------
 * instance methods
 */
	/**
	 * ハンドを指定されたものに設定します。
	 * プレイが開始されている場合は設定できません。
	 *
	 * @param		{Packet} hand 設定するハンド
	 */
	setHand(hand) {
		if ( (this.trick[0])&&(this.trick[0].children.length > 0) )
			throw new Error("すでにプレイが開始されているため" +
											" setHand は行えません。");
		if (hand.length != 4)
			throw new Error("４人分のハンドが指定されていません。");
		
		for (let i = 0; i < 4; i++) {
			if (hand[i].children.length != 13)
				throw new Error("ハンド"+i
							+"のカード枚数が異常です。13枚指定して下さい。");
		}
		
		this.hand = hand;
	}
	
	/**
	 * コントラクト(leader, trump)を設定します。
	 * 一度設定されたら変更できません。
	 *
	 * @param	{number} leader	オープニングリーダー
	 * @param	{number} trump	トランプスーツ
	 * @see		Card
	 */
	setContract(leader, trump) {
		if (this.trump != -1)
			throw new Error("一度指定されたコントラクトを変更できません。");
		this.trump = trump;
		this.trick[0] = new Trick(leader, trump);
	}
	
	/**
	 * 指定されたプレイが可能であるか判定します。
	 * プレイヤーとして、この PlayHistory の getTurn() のプレイヤーが
	 * プレイしていると仮定しています。
	 * 具体的には、現在順番
	 * 
	 * @param		{Card} p プレイ可能か判定したいカード
	 * @return		{boolean} プレイできるかどうか
	 */
	allows(p) {
		const turn = this.trick[this.trickCount].getTurn();
		
		// hand[turn] が指定されたカード持っていない場合 false
		const card = this.hand[turn].peek(p);
		if (card == null) return false;
		
		// スートフォローに従っているか
		const lead = this.trick[this.trickCount].children[0];
		if (lead===void 0) return true;
		const suit = lead.suit;
		if (suit == p.suit) return true;
		// 持っていない場合
		if (this.hand[turn].countSuit(suit) == 0) return true;
		return false;
	}
	
	/**
	 * 指定されたカードをプレイして状態を更新します。
	 * プレイできないカードをプレイしようとすると Error
	 * がスローされます。
	 *
	 * @param		{Card} p		プレイするカード
	 */
	play(p) {
		if (!this.allows(p))
			throw new Error(p.toString() + "は現在プレイできません。");
		
		const turn = this.trick[this.trickCount].getTurn();
		
		const drawn = this.hand[turn].pull(p);
		drawn.isHead = true;
		const tr = this.trick[this.trickCount];
		tr.add(drawn);
		
		if (!tr.isFinished()) return;
		
		this.trickCount++;
		if (this.trickCount < 13) {
			this.trick[this.trickCount] = new Trick(tr.winner, this.trump);
		}
	}
	
	/**
	 * 誰の番かを返します。
	 * @return	{number} 座席番号
	 */
	getTurn() {
		return this.trick[this.trickCount].getTurn();
	}
	
	/**
	 * 現在まででプレイされているトリック数を取得する。
	 * プレイ中のトリックについてはカウントされない。
	 * @return	{number} this.trickCount を返します。
	 */
	getTricks() {
		return this.trickCount;
	}
	
	/**
	 * 指定されたラウンドのトリックを返却します。
	 * ラウンドは 0 から 12 までの整数値です。
	 * 省略した場合、現在プレイ中のトリックを返します。
	 * @param	{number} index ラウンド
	 * @return	{Trick} トリック
	 */
	getTrick(index) {
		if (index !== void 0) return this.trick[index];
		if (this.trickCount == 13) return this.trick[12];
		return this.trick[this.trickCount];
	}
	
	/**
	 * すべてのトリックを取得します。
	 * @return	{Array.<Trick>} トリックすべて(null のことがあります)
	 */
	getAllTricks() {
		if (this.trick.length == 0) return null;
		if (this.hand[0] == null) return null;
		
		let n = this.trickCount + 1;
		if (this.trickCount == 13) n--;
		return this.trick.slice(0, this.trick.length);
	}
	
	/**
	 * プレイが終了しているかを判定します。
	 * @return	{boolean} プレイが終了しているか
	 */
	isFinished() {
		return ( (this.trickCount == 13) && (this.trick[12].isFinished()) );
	}
	
	/**
	 * この PlayHistory を初期化します。
	 */
	reset() {
		this.hand = [];
		this.trick = [];
		this.trickCount	= 0;
		this.trump		= -1;
	}
	
	/**
	 * プレイにおける undo() を行います。最後にプレイされたカードを返却します。
	 * 初期状態では、Error をスローします。
	 * @return	{Card} 最後にプレイされたカード
	 */
	undo() {
		if (this.trick.length == 0)
			throw new Error("初期状態のため、undo() できません");
		if ((this.trickCount == 0)&&(this.trick[0].children.length == 0))
			throw new Error("初期状態にあるため、undo() できません");
		
		if (this.trickCount == 13) {
			this.trickCount--;
		} else if (this.trick[this.trickCount].children.length == 0) {
			// 現在リードの状態
			this.trick.pop();
			this.trickCount--;
		}
		
		// だれのハンドに戻すか
		const seatToBePushbacked = (this.trick[this.trickCount].getTurn() + 3) % 4;
		
		// 最後のプレイを取得する
		const lastPlay = this.trick[this.trickCount].pull();
		this.hand[seatToBePushbacked].add(lastPlay);
		this.hand[seatToBePushbacked].arrange();
		
		// 一番はじめにもどすための特殊処理( reset() 相当の処理 )
		if ((this.trickCount == 0)&&(this.trick.length == 0)) {
			// setContract 以前の状態まで戻す
			this.hand = [];
			this.trick = [];
			this.trump = -1;
		}
		return lastPlay;
	}
	
	/**
	 * @override
	 */
	toString() {
		let result = "";
		
		result += "N : " + this.hand[PlayHistory.NORTH]	+ "\n";
		result += "E : " + this.hand[PlayHistory.EAST]		+ "\n";
		result += "S : " + this.hand[PlayHistory.SOUTH]	+ "\n";
		result += "W : " + this.hand[PlayHistory.WEST]		+ "\n";
		
		result += "\n";
		
		if (this.trickCount < 13) result += this.trick[this.trickCount];
		
		result += "\n\n";
		for (let i = this.trickCount-1; i >= 0; i--) {
			if (i < 13) {
				result +="[";
				if (i < 10) result += " ";
				result += i;
				result += "]"+this.trick[i];
				result += "  win="+this.trick[i].winnerCard;
				result += "  " + PlayHistory.SEAT_STRING[this.trick[i].winner]+"\n";
			}
		}
		
		return result;
	}
}
	
/**
 * TableGui class 定義
 * @extends Entity
 */
class TableGui extends Entity {

/*------------------
 * static constants
 */
	static WIDTH = 640;
	static HEIGHT = 480;

	/** @type {Board} 属している Board */
	board;

	constructor(board) {
		super();
		this.board = board;
		this.setSize(TableGui.WIDTH, TableGui.HEIGHT);
	}

/*------------------
 * instance methods
 */
	/**
	 * 左上のボード情報枠を描画
	 * @private
	 * @param	{Context} ctx Canvas のグラフィックコンテキスト
	 */
	_drawContract_(ctx) {
		const DIRECTIONS = ["North","East","South","West"];
		const contract = this.board.getContract();
		
		// 枠
		ctx.fillStyle = 'rgb(0,32,0)'; // back color
		ctx.fillRect(18,32,172,72);
		ctx.fillStyle = 'rgb(0,160,0)'; // field color
		ctx.fillRect(10,24,172,72);
		
			ctx.font = 'bold 14px serif';
		// コントラクトがあれば、左上に表示する
		if (contract != null) {
			const kind = contract.kind;
			let contractStr;
			if (kind == Bid.PASS) {
				contractStr = "Pass Out";
			} else {
				contractStr = "Contract " + contract.level;
				const i = contract.suit-1;
				contractStr += " C D H SNT".substring(i*2, i*2+2);
				contractStr += ["", "p.o.", "X", "XX"][kind];
				contractStr += " by ";
				contractStr += DIRECTIONS[this.board.getDeclarer()];
			}
			this._fillTextWithShade_(ctx, contractStr, 18, 83);
			
		} else {
			// ディーラー
			let dealerStr = "ディーラー ";
			dealerStr += DIRECTIONS[this.board.getDealer()];
			this._fillTextWithShade_(ctx, dealerStr, 18, 83);
		}
		// バル
		const vulStr = ["Vul: Neither", "Vul: N-S", "Vul: E-W", "Vul: Both"];
		this._fillTextWithShade_(ctx, vulStr[this.board.vul], 18, 65);
		
		// タイトル
		ctx.font = 'italic bold 13px sans-serif';
		this._fillTextWithShade_(ctx, this.board.name, 18, 44);
		
		// 線
		ctx.fillStyle = 'white';
		//ctx.beginPath();
		//ctx.moveTo(18, 48);
		//ctx.lineTo(170, 48);
		//ctx.closePath();
		//ctx.stroke();
		ctx.fillRect(18,48,152,1);
	}
	
	/**
	 * 影付きの文字を描画
	 * @private
	 * @param	{Context} ctx Canvas のグラフィックコンテキスト
	 * @param	{string} str 描画対象の文字列
	 * @param	{number} x x座標
	 * @param	{number} y y座標
	 */
	_fillTextWithShade_(ctx, str, x, y) {
		ctx.fillStyle = 'rgb(0,0,96)'; // navy (影)
		ctx.fillText(str, x+2, y+2);
		ctx.fillStyle = 'rgb(255,255,255)'; // white
		ctx.fillText(str, x, y);
	}
	
	/**
	 * 真ん中の方角、バル関係を描画
	 * @private
	 * @param	{Context} ctx Canvas のグラフィックコンテキスト
	 */
	_drawDirection_(ctx) {
		//
		// 真ん中の NESW
		//
		
		// 枠線
		ctx.fillStyle = 'rgb(224,255,224)'; // 白っぽい緑
		ctx.fillRect(220, 140, 200, 8);
		ctx.fillRect(220, 140, 8, 200);
		ctx.fillRect(412, 140, 8, 200);
		ctx.fillRect(220, 332, 200, 8);
		
		// VULを示す赤線
		const vul = this.board.vul;
		ctx.fillStyle = 'rgb(170, 0, 0)'; // 暗めの赤
		if ( (vul & Board.VUL_NS) != 0) {
			ctx.fillRect(228, 148, 184, 32);
			ctx.fillRect(228, 300, 184, 32);
		}
		if ( (vul & Board.VUL_EW) != 0) {
			ctx.fillRect(228, 148, 32, 184);
			ctx.fillRect(380, 148, 32, 184);
		}
		
		// N E S W の文字
		ctx.fillStyle = 'rgb(224,255,224)'; // 白っぽい緑
		ctx.font = 'normal 28px SanSerif';
		ctx.fillText("N", 314, 178);
		ctx.fillText("E", 384, 250);
		ctx.fillText("S", 314, 326);
		ctx.fillText("W", 238, 250);
	}
/*-----------
 * overrides
 */
	/**
	 * @override
	 */
	draw(ctx) {
		this._drawContract_(ctx);
		this._drawDirection_(ctx);
	}
}

/**
 * WinnerCard class 定義
 */
class WinnerCard extends Entity {

/*------------------
 * static constants
 */
	static LONGER_EDGE = 48;
	static SHORTER_EDGE = 32;
	static SLIDE_STEP = 8;

	constructor(win) {
		super();
		this.win = win;
		
		this.setSize(WinnerCard.SHORTER_EDGE, WinnerCard.LONGER_EDGE);
		if (win) {
			this.direction = Entity.UPRIGHT;
		} else {
			this.direction = Entity.RIGHT_VIEW;
		}
	}

/*-----------
 * overrides
 */
	/**
	 * @override
	 * @param	{Context} ctx	グラフィックコンテキスト
	 */
	draw(ctx) {
		if (!this.isVisible) return;
		let x,y, c,s;
		const r = this.getRect();
		switch (this.direction) {
		case 1:
			c = 0; s = 1;	x = r.y;		y = -r.x - r.w; break;
		case 2:
			c = -1; s = 0;	x = -r.x - r.w;	y = -r.y - r.h; break;
		case 3:
			c = 0; s = -1;	x = -r.y - r.h;	y = r.x; break;
		default:
			c = 1; s = 0;	x = r.x;		y = r.y; break;
		}
		const img = CardImageHolder.getBackImage();
		
		ctx.setTransform(c, s, -s, c, 0, 0);
		ctx.drawImage(img, x, y, this.w, this.h);
		ctx.setTransform(1, 0, 0, 1, 0, 0);
	}
}

/**
 * WinnerGui class 定義
 */
class WinnerGui extends Entities {
	static WIN = true;
	static LOSE = false;

	constructor() {
		super();
		this.card = []; // WinnerCard[]
		this.count = 0;
		this.layout = null;
		this.setSize(WinnerCard.SLIDE_STEP * 12 + WinnerCard.LONGER_EDGE,
						WinnerCard.LONGER_EDGE);
	}

/*------------------
 * instance methods
 */
	/**
	 * @override
	 * @param	{boolean} win	勝ち(true)が縦、負け(false)が横
	 */
	add(win) {
		if (typeof win != "boolean")
			throw new Error("Winner Gui の add は boolean 値のみです");
		this.card[this.count] = new WinnerCard(win);
		this.card[this.count].setPosition(this.x + this.count * WinnerCard.SLIDE_STEP,
					(win)?this.y:(this.y + WinnerCard.LONGER_EDGE / 4));
		super.add(this.card[this.count]); // 正しいか？
		this.count++;
	}
}

/**
 * BoardLayout class 定義
 * @classdesc	Board のレイアウトです。
 * @constructor
 */
class BoardLayout {
	/** ハンドの表示位置関連 */
	static SIDE_MARGIN = 40;
	static VSIDE_MARGIN = 20;
	
	/** トリックの表示位置 */
	static TRICK_X = Math.floor((TableGui.WIDTH - Trick.WIDTH)/2);
	static TRICK_Y = Math.floor((TableGui.HEIGHT - Trick.HEIGHT)/2);
	
	/** ウィナー表示位置 */
	static WINNER_X = 460;
	static WINNER_Y = 400;
	
	constructor() {
	}

/*------------------
 * instance methods
 */
	/**
	 * 
	 * @param {Board} target layout対象となる Board
	 */
	layout(target) {
		if (!target instanceof Board)
			throw new Error("BoardLayout は Board 専用です:"+target);
		const board = target;
		const d = board.direction;
		const x = board.x;
		const y = board.y;
		
		// ハンドの位置指定
		
		if (board.getHand() != null) {
			for (let i = 0; i < 4; i++) {
				const ent = board.getHand()[i];
				if (!ent) {
					continue;
				}
				ent.setDirection((10 - i - d )%4);
				const lsize = ent.getSize();	// 大きさを計算させる
				
				let xx, yy;
				
				switch ( (i+d)%4 ) {
				case 0:		// 上のハンド
					xx = Math.floor((board.w - lsize.w) / 2);
					yy = BoardLayout.VSIDE_MARGIN;
					break;
				case 1:		// 右のハンド
					xx = board.w - lsize.w - BoardLayout.SIDE_MARGIN;
					yy = Math.floor((board.h - lsize.h) / 2);
					break;
				case 2:		// 下のハンド
					xx = Math.floor((board.w - lsize.w) / 2);
					yy = board.h - lsize.h - BoardLayout.VSIDE_MARGIN;
					break;
				case 3:		// 左のハンド
					xx = BoardLayout.SIDE_MARGIN;
					yy = Math.floor((board.h - lsize.h) / 2);
					break;
				default:
					throw new InternalError();
				}
				ent.setPosition(x + xx, y + yy);
				if (ent.layout) ent.layout.layout(ent);
			}
		}
		// トリックの位置指定
		const trick = board.trickGui;
		
		if (trick) {
			trick.setPosition(x + BoardLayout.TRICK_X, y + BoardLayout.TRICK_Y);
			trick.layout.layout(trick);
		}
		
		// ウィナーの位置指定
		const winnerGui = board.winnerGui;
		if (winnerGui) {
			winnerGui.setPosition(x + BoardLayout.WINNER_X, y + BoardLayout.WINNER_Y);
		}
	}
	
	/**
	 * 
	 * @param {Board} target layout 対象の Board
	 */
	layoutSize(target) {
		if (!target instanceof Board)
			throw new Error("BoardLayout は Board 専用です:"+target);
		return {w:target.w, h:target.h};
	}
}

/**
 * Board class 定義
 *
 * @classdesc ブリッジにおける１ボードをパックするオブジェクトです。
 * 		受動的なオブジェクトで、BoardManagerに対しては状態変化を起こす
 * 		メソッドを提供します。
 * 		Playerに対してBoard の情報を提供します。
 * @param	{number} dealerOrNum ディーラーまたは、ボード番号
 * @param	{number} vul ディーラーを指定したときはバルを指定する。
 * @extends	Entities
 */
class Board extends Entities {

/*------------------
 * static constants
 */
	/** @type {number} 座席定数 */
	static NORTH = PlayHistory.NORTH;
	/** @type {number} 座席定数 */
	static EAST = PlayHistory.EAST;
	/** @type {number} 座席定数 */
	static SOUTH = PlayHistory.SOUTH;
	/** @type {number} 座席定数 */
	static WEST = PlayHistory.WEST;
	
	static STATUS_STRING = [ "Dealing", "Bid", "Opening Lead", "Playing", "Scoring" ];
	static VUL_STRING = [ "neither", "N-S", "E-W", "both" ];
	static SEAT_STRING = PlayHistory.SEAT_STRING;
	
	/**
	 * vul 値として使用される定数です。
	 */
	static VUL_NEITHER = 0;
	static VUL_NS = 1;
	static VUL_EW = 2;
	static VUL_BOTH = 3;
	
	static VUL = [ 0,1,2,3, 1,2,3,0, 2,3,0,1, 3,0,1,2 ];
	
	static WIDTH = 640;
	static HEIGHT = 480;
	
	/** @const {number} ボードが新規作成され、まだカードがディールされていない status */
	static DEALING = 0;
	/** @const {number} ビッドが行われている status */
	static BIDDING = 1;
	/** @const {number} オープニングリード待ち status */
	static OPENING = 2;
	/** @const {number} プレイ中 status */
	static PLAYING = 3;
	/** @const {number} ボード終了 status */
	static SCORING = 4;
	
	/** @const {number} プレイ順番を示す定数です。 */
	static LEAD = 0;
	/** @const {number} プレイ順番を示す定数です。 */
	static SECOND = 1;
	/** @const {number} プレイ順番を示す定数です。 */
	static THIRD = 2;
	/** @const {number} プレイ順番を示す定数です。 */
	static FORTH = 3;

	/** @type {BiddingHistory} この Board の BiddingHistory */
	bidding;
	/**  @type {PlayHistory} この Board の PlayHistory */
	playHist;
	/** @type {number} vulnerability */
	vul;
	/** @type {number} status(DEALING/BIDDING/OPENING/PLAYING/SCORING) */
	status;
	/** @type {string} タイトル */
	name;

	/**
	 * ボード番号(1-16)、または dealer, vul を指定して Board を作成します
	 * @param {number} dealerOrNum ボード番号(1-16)、または dealer (0-3)
	 * @param {number} vul 
	 */
	constructor(dealerOrNum, vul) {
		super();
		if (dealerOrNum===void 0) throw new Error("Board() には引数が必要です");
		
		// vul が undefined の場合、dealerOrNum は num
		if (vul===void 0) {
			vul = Board.VUL[(dealerOrNum - 1)%16];
			dealerOrNum = (dealerOrNum - 1)%4;
		}
		const dealer = dealerOrNum;
		// new Board(Board) は未実装
		
		this.bidding = new BiddingHistory(dealer);
		this.playHist = new PlayHistory();
		this.vul = vul;
		this.status = Board.DEALING;
		this.name = "Bridge Board";
		/** @type {Packet} 既知のカードで、思考ルーチンで利用することを期待 */
		this.openCards = new Packet();
		
		this.direction = Entity.UPRIGHT;
		this.setSize(Board.WIDTH, Board.HEIGHT);
		
		// handGui は {Array.<Packet>} playHist.hand を使う
		/**
		 * 中央部分に表示されるトリック
		 * @type {Trick}
		 */
		this.trickGui = null;
		this.winnerGui = null;
		this.tableGui = null;

		this.layout = new BoardLayout();
	}

/*------------------
 * instance methods
 */
	/**
	 * GUI 要素の設定を行います。Entities として GUI 要素の add を
	 * 内部的に行います。
	 * @private
	 */
	_setBoardGui_() {
		this.tableGui = new TableGui(this);
		this.add(this.tableGui);
		
		this.winnerGui = new WinnerGui();
		this.add(this.winnerGui);
		
		for (let i = 0; i < 4; i++) {
			this.add(this.playHist.hand[i]);
		}
		
		this.layout = new BoardLayout();
	}
	
	/**
	 * カードを配って BIDDING 状態に移行します。
	 *
	 * @param	{?Packet[]|number} handOrSeed 初期ハンド指定(ない場合、新規カードを配る)
	 *								整数を指定したら乱数シードの意味
	 */
	deal(handOrSeed) {
		if (this.status != Board.DEALING)
			throw new Error("deal() は DEALING 状態のみで実行可能です。");
		
		let hand;
		if (handOrSeed===void 0 || !isNaN(handOrSeed) ) {
			// hand が指定されていないときは新規カードを配る
			const seed = handOrSeed;
			hand = []; // new Packet[4];
			for (let i = 0; i < 4; i++) hand[i] = new Packet();
			
			const pile = Packet.provideDeck();
			this.openCards = new Packet();
			
			// カードを裏向きにして配る
			pile.turn(false);
			pile.shuffle(seed); // hand is void0 or number
			
			Packet.deal(pile, hand);
			for (let i = 0; i < 4; i++) hand[i].arrange();
		} else {
			// hand 指定があればそれを使う
			hand = handOrSeed;
			// カードを裏向きにしておく
			for (let i = 0; i < hand.length; i++) {
				hand[i].turn(false);
				hand[i].arrange();
			}
		}
		
		this.playHist.setHand(hand);
		
		this.status = Board.BIDDING;
		
		// GUI処理
		this._setBoardGui_();
		
		// 下に表示されるハンドを表向きにする
		const turned = this.getHand( (this.direction + 2) % 4 );
		turned.turn(true);
	}
	
	/**
	 * この Board で使用する PlayHistory を指定します。
	 *
	 * @param		{PlayHistory} playHistory 設定したい playHistory
	 */
	setPlayHistory(playHistory) {
		this.playHist = playHistory;
	}
	
	/**
	 * この Board のコントラクトを指定します。
	 * @param	{Bid} contract コントラクト
	 * @param	{number} declarer ディクレアラーの座席定数
	 */
	setContract(contract, declarer) {
		this.bidding.setContract(contract, declarer);
		this.playHist.setContract((this.getDeclarer() + 1)%4, this.getTrump() );
		this.status = Board.OPENING;
		
		this._reorderHand_();
	}
	
	/**
	 * ビッド、プレイを行う。状態が変化する。
	 * @param	{Card} play CardオブジェクトまたはBidオブジェクトを指定
	 * @see	Bid
	 */
	play(play) {
		if (!this.allows(play))
			throw new Error(play.toString() + "は行えません。");
		switch (this.status) {
		
		case Board.BIDDING:
			if (play.kind===void 0 || play.level===void 0 || play.suit===void 0) //! instanceof Bid
				throw new Error("ビッドしなければなりません。" + play);
			this.bidding.play(play);
			if (this.bidding.finished) {
				if (this.getContract().kind == Bid.PASS) {
					// Pass out
					this.status = Board.SCORING;
					break;
				}
				this.status = Board.OPENING;
				this.playHist.setContract((this.getDeclarer() + 1)%4, this.getTrump() );
				
				this._reorderHand_();
				this.trickGui = this.getTrick();
				if (this.trickGui) this.add(this.trickGui);
		
			}
			break;
			
		case Board.OPENING:
		case Board.PLAYING:
			if (play.suit===void 0 || play.value===void 0) // ! instanceof Card
				throw new Error("プレイしなければなりません。" + play);
			this.playHist.play(play);
			this.openCards.add(play);
			
			if (this.status == Board.OPENING) this._dummyOpen_();
			if (this.playHist.isFinished()) this.status = Board.SCORING;
			else this.status = Board.PLAYING;
			
			break;
			
		case Board.DEALING:
		case Board.SCORING:
			throw new Error(play.toString() + "は行えません。");
		
		default:
			throw new Error("Board.status が不正な値"
							+ this.status + "になっています。");
		}
	}

	/**
	 * OPENING/PLAYING状態で、TrickAnimation 付きの play 処理を行います。
	 * TrickAnimation の終了を待って次の play を進めるようにするため
	 * async メソッドとする必要があり、別に切り出した。
	 * JavaScript では設計を MVC とするのが望ましいと思われる。
	 * @async
	 * @param {Card} play 
	 */
	async playWithGui(play) {
		if (this.status != Board.OPENING && this.status != Board.PLAYING) {
			throw new Error("playWithGui() は OPENING/PLAYING ステータスである必要があります");
		}
		const oldStatus = this.status;
		this.play(play);
		if (oldStatus === Board.OPENING) {
			this.trickGui = this.getTrick();
			this.add(this.getTrick());
			const d = (4 + this.trickGui.leader - this.trickGui.direction ) % 4;
			const ent = this.trickGui.children[0];
			ent.direction = ((10 - d )%4);
			this.trickGui.selfLayout();
			const dummy = this.getHand(this.getDummy());
			dummy.layout = new DummyHandLayout();
			dummy.selfLayout();
			this.selfLayout();
		}

		const field = this.getField();
		if (!this.trickGui.isFinished()) {
			field.draw();
			return;
		}

		// TrickAnimation 処理
		let trickGui = this.trickGui;
		const newTrickGui = this.getTrick(); // 現在のトリック

		field.draw();
		// ウィナーを点滅させる
		const toBlink = this.trickGui.getWinnerCard();
		let i = 0;
		for (; i < 12; i++) {
			const clicked = await field.waitClick(300);
			if (clicked) break;
			toBlink.isVisible = !toBlink.isVisible;
			field.draw();
		}
		if (i == 12) await field.sleep(300);
		toBlink.isVisible = true;

		// カードを裏返し、ウィナーの向きに揃える
		const dir = ((trickGui.getWinner() ^ this.direction) & 1) + 2;
		trickGui.children.forEach( c => {
			c.isHead = false;
			c.setDirection(dir);
		});
		trickGui.selfLayout();
		field.draw();
		await field.sleep(500);

		this.pull(trickGui);
		
		if (( (trickGui.getWinner() ^ this.direction) & 1) == 0)
			this.winnerGui.add(WinnerGui.WIN);
		else
			this.winnerGui.add(WinnerGui.LOSE);
		
		// 次の trickGui を設定
		if (trickGui !== newTrickGui) { // 最終トリックのみ == となる
			trickGui = this.trickGui = newTrickGui;
			this.add(newTrickGui);
		}
		this.selfLayout();
		if (trickGui != null) trickGui.selfLayout();
		field.draw();
	}
	
	/**
	 * １つ前の状態に戻します。
	 * 状態遷移を考える
	 *
	 * DEALING
	 *    |                           pass out
	 *    +----->BIDDING---------------------------------+
	 *    |              ＼                              |
	 *    +----------------->OPENING                     |
	 *                          |                        |
	 *                          +-------->PLAYING        |
	 *                          O.L.         |           V
	 *                                       +------->SCORING
	 *                                       last play
	 */
	undo() {
		switch (this.status) {
		case Board.DEALING:
			throw new Error("DEALING 状態で undo() はできません");
		
		case Board.BIDDING:
			if (this.bidding.bid.length == 0) {
				this.status = Board.DEALING;
				break;
			}
			this.bidding.undo();
			break;
		
		case Board.OPENING:
			if (this.bidding.bid.length == 0) {
				// setContract されている
				this.status = Board.DEALING;
				this.bidding.reset(this.bidding.dealer);
				break;
			}
			this.status = Board.BIDDING;
			this.bidding.undo();
			break;
		
		case Board.SCORING:
			this.status = Board.PLAYING;
			// fall through
			
		case Board.PLAYING:
			//
			// まず、PlayHistory を undo() する
			//
			const lastPlay = this.playHist.undo(); // PLAYING なので、Exception はでないはず
			const turn = this.playHist.getTurn();
			if (turn != this.getDummy()) lastPlay.isHead = false;
			this.openCards.pull(lastPlay);
			
			// undo() の結果、OPENING になる場合
			if ((this.getTricks() == 0)&&(this.playHist.getTrick() == null)) {
				// O.L.にもどる
				this._dummyClose_();
				this.status = Board.OPENING;
			}
			break;
			
		default:
			throw new Error("status が異常値 " + this.status);
		}
	}
	
	/**
	 * ビッド終了後に、トランプスートが左に来るように並び替えます。
	 */
	_reorderHand_() {
		let order; // CardOrder / stateless object
		
		switch (this.getTrump()) {
		case Bid.HEART:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_HEART);
			break;
		case Bid.DIAMOND:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_DIAMOND);
			break;
		case Bid.CLUB:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_CLUB);
			break;
		default:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_SPADE);
			break;
		}
		
		this.getHand(this.getDummy()).cardOrder = order;
		this.getHand(this.getDummy()).arrange();
		this.getHand(this.getDeclarer()).cardOrder = order;
		this.getHand(this.getDeclarer()).arrange();
	}
	
	/**
	 * オープニングリードの後に呼ばれ、ダミーの手を表向きに変更します。
	 * また、場に出ているカードにダミーハンドを追加します。
	 */
	_dummyOpen_() {
		const dummy = this.getHand(this.getDummy());
		dummy.turn(true);
		this.openCards.add(dummy);
	}
	
	/**
	 * undo() 時に Opening Lead 状態にもどすための処理を行います。
	 * ダミーの手を裏向きに変更します。
	 */
	_dummyClose_() {
		const dummy = this.getHand(this.getDummy());
		dummy.turn(false);
		this.openCards.sub(dummy);
		
		if (this.openCards.children.length != 0) throw new Error("openCards 枚数に矛盾があります");
	}
	
/*------------------------------
 * 状態参照メソッド(公開される)
 */
	/**
	 * プレイ可能であるかテストする。
	 *
	 * @param		{Object} play Object は Bid または Play
	 * @return		{boolean} true：可能    false:不可能
	 */
	allows(play) {
		switch (this.status) {
		
		case Board.BIDDING:
			return this.bidding.allows(play);
			
		case Board.OPENING:
		case Board.PLAYING:
			return this.playHist.allows(play);
			
		case Board.DEALING:
		case Board.SCORING:
			return false;
			
		default:
			throw new Error("Board.status が不正な値"
							+ this.status + "になっています。");
		}
	}
	
	/**
	 * ステータスが Board.OPENING, Board.PLAYING の場合にプレイ順を
	 * 示す定数を返却します。
	 *
	 * @return		{number} プレイ順を示す定数
	 */
	getPlayOrder() {
		switch (this.status) {
		
		case Board.OPENING:
		case Board.PLAYING:
			return this.getTrick().children.length;
		
		case Board.DEALING:
		case Board.BIDDING:
		case Board.SCORING:
			return -1;
		
		default:
			throw new Error("Board.status が不正な値"
							+ this.status + "になっています。");
		}
	}
	
	/**
	 * だれの番であるかを席番号で返却する。
	 * DEALING, SCORING のステータスでは -1 が返却される。
	 *
	 * @return	{number} 誰の番かを示す座席定数。
	 */
	getTurn() {
		switch (this.status) {
		
		case Board.BIDDING:
			return this.bidding.getTurn();
			
		case Board.OPENING:
		case Board.PLAYING:
			return this.playHist.getTurn();
			
		case Board.DEALING:
		case Board.SCORING:
			return -1;
			
		default:
			throw new Error("Board.status が不正な値"
							+ this.status + "になっています。");
		}
	}
	
	/**
	 * だれがプレイする番か取得します。
	 * getTurn() との違いは、プレイのとき、ダミーの番でディクレアラーの
	 * 席番号が返される点です。
	 *
	 * @param	{number} 現在のプレイ順のプレイヤー座席番号
	 */
	getPlayer() {
		const seat = this.getTurn();
		
		switch (this.status) {
		
		case Board.OPENING:
		case Board.PLAYING:
			if (seat == this.getDummy()) return this.getDeclarer();
			return seat;
			
		case Board.BIDDING:
			return seat;
			
		case Board.DEALING:
		case Board.SCORING:
		default:
			return -1;
		}
	}
	
	/**
	 * (現在までの)最終コントラクトを取得する。
	 * ビッドがまだ行われていない場合、null が返る。
	 *
	 * @return	{Bid} コントラクト
	 */
	getContract() {
		return this.bidding.contract;
	}
	
	/**
	 * (現在までの)最終トランプを取得します。
	 * ビッドがまだ行われていない場合、-1 が返ります。
	 * @return	{number}	トランプスーツ
	 */
	getTrump() {
		const contract = this.getContract();
		if (contract == null) return -1;
		return contract.suit;
	}
	
	/**
	 * (現在までで決定している)ディクレアラーの席番号を取得する。
	 * ビッドがまだ行われていない場合、-1 が返る。
	 * @return	{number}	ディクレアラーの座席番号
	 */
	getDeclarer() {
		return this.bidding.declarer;
	}
	
	/**
	 * (現在までで決定している)ダミーの席番号を取得する。
	 * ビッドがまだ行われていない場合、-1 が返る。
	 * @return	{number} ダミーの座席番号
	 */
	getDummy() {
		const dec = this.bidding.declarer;
		if (dec == -1) return -1;
		return (dec + 2) % 4;
	}
	
	/**
	 * (ビッドを開始する)ディーラーの席番号を取得する。
	 * ビッドがまだ行われていない場合、-1 が返る。
	 * @return	{number}	ディーラーの座席番号
	 */
	getDealer() {
		return this.bidding.dealer;
	}
	
	/**
	 * ハンドの情報を取得する。UnspecifiedCardが含まれることもある。
	 * @param	{?number} seat	座席番号。無指定の場合 Packet[] が返る。
	 * @returns	{Packet|Packet[]} ハンド。seat を指定しない場合 Packet[]
	 */
	getHand(seat) {
		if (seat!==void 0) {
			return this.playHist.hand[seat];
		}
		return this.playHist.hand;
	}
	
	/**
	 * 現在までにプレイされたトリック数を取得する。
	 * @return	{number}	プレイされたトリック数
	 */
	getTricks() {
		return this.playHist.getTricks();
	}
	
	/**
	 * 現在場に出ているトリックを取得する。
	 * @return	{Trick} 現在のトリック
	 */
	getTrick() {
		return this.playHist.getTrick();
	}
	
	/**
	 * プレイされた過去のトリックすべてを取得します。
	 * 本処理は、PlayHistory.getAllTricks() へ委譲しています。
	 * @return	{Array.<Trick>} 全トリック
	 * @see		PlayHistory#getAllTricks()
	 */
	getAllTricks() {
		return this.playHist.getAllTricks();
	}

	/**
	 * parent を検索し、Field を取得します。
	 * @returns		{?Field}		この Board の親系列にある Field。ない場合 null。
	 */
	getField() {
		let obj = this;
		while (true) {
			if (!obj) return null;
			obj = obj.parent;
			if (obj instanceof Field) return obj;
		}
	}
	
	/**
	 * 指定された座席がバルであるか判定します。
	 * @param	{number}	seat	座席番号
	 * @return	{number} VUL を示す定数
	 */
	isVul(seat) {
		let mask = 0;
		if ( (seat == Board.NORTH)||(seat == Board.SOUTH) ) mask = 1;
		else mask = 2;
		
		if ((this.vul & mask) > 0) return true;
		return false;
	}
	
	/**
	 * 文字列表現を得る。
	 * @return	{string} 文字列表現
	 * @override
	 */
	toString() {
		let result = "---------- Board Information ----------";
		result += "\n  [    Status     ]  : " + Board.STATUS_STRING[this.status];
		result += "\n  [ Vulnerability ]  : " + Board.VUL_STRING[this.vul];
		result += "\n  [    Dealer     ]  : " + Board.SEAT_STRING[this.getDealer()];
		result += "\n  [   Declarer    ]  : ";
		if (this.getDeclarer() == -1) result += "none";
		else result += Board.SEAT_STRING[this.getDeclarer()];
		result += "\n\n  [Bidding History]\n";
		result += this.bidding.toString();
		result += "\n  [  Table Info   ]\n";
		result += this.playHist.toString() + "\n";
		
		return result;
	}
	
	/**
	 * Board を初期化します。
	 * @param	{number} numOrDealer ボード番号またはディーラーの座席番号
	 * @param	{vul} バル。無指定のとき numOrDealer はボード番号と見なす。
	 */
	reset(numOrDealer, vul) {
		let dealer;
		if (vul===void 0) {
			vul = Board.VUL[(numOrDealer - 1)%16];
			dealer = (numOrDealer - 1)%4;
		} else {
			dealer = numOrDealer;
		}
		this.bidding.reset(dealer);
		this.playHist.reset();
		
		this.vul = vul;
		this.status = Board.DEALING;
	}
	
	/**
	 * 人間が見やすいテキスト表現に変換します。
	 * Contract 決定前の表示、スコア計算は未実装です。
	 * @return	{string} このボードのテキスト表現
	 */
	toText() {
		let s;
		const nl = "\n";
		
		s = this.name + nl
				+ "----- コントラクト -----" + nl
				+ "contract：" + this.getContract().toString()
				+ " by " + Board.SEAT_STRING[this.getDeclarer()] + nl
				+ "vul     ：" + Board.VUL_STRING[this.vul]
				+ nl + nl
				+ "----- オリジナルハンド -----" + nl;
		
		const hands = Board.calculateOriginalHand(this);
		
		// NORTH
		for (let suit = 4; suit >= 1; suit--) {
			s = s + "               "
					+ this._getHandString_(hands, 0, suit)+nl;
		}
		
		// WEST, EAST
		for (let suit = 4; suit >= 1; suit--) {
			let wstr = this._getHandString_(hands, 3, suit) + "               ";
			wstr = wstr.substring(0, 15);
			s = s + wstr;
			switch (suit) {
			case 4:
			s = s + "    N          "; break;
			case 3:
			s = s + "W       E      "; break;
			case 2:
			s = s + "               "; break;
			case 1:
			s = s + "    S          "; break;
			default:
			}
			
			s = s + this._getHandString_(hands, 1, suit) + nl;
		}
		// SOUTH
		for (let suit = 4; suit >= 1; suit--) {
			s = s + "               "
						+ this._getHandString_(hands, 2, suit) + nl;
		}
		
		// ビッド経過
		s += this.bidding.toString();
		s += nl;
		
		// プレイライン
		s = s + nl
				+"----- プレイ -----" + nl
		
				+"    1  2  3  4  5  6  7  8  9 10 11 12 13" + nl;
		
		const trick = this.getAllTricks();
		const nesw = [];
		for (let i = 0; i < 4; i++) {
			nesw[i] = "NESW".substring(i,i+1) + " ";
			if (this.getTricks() > 0) {
				const leaderSeat = trick[0].leader;
				if (i == leaderSeat) nesw[i] += "-";
				else nesw[i] += " ";
			}
		}
		
		for (let i = 0; i < this.getTricks(); i++) {
			const leaderSeat = trick[i].leader;
			const winnerSeat = trick[i].winner;
			for (let j = 0; j < 4; j++) {
				const seat = (j + leaderSeat) % 4;
				nesw[seat] += trick[i].children[j].toString().substring(1);
				if (seat === winnerSeat) nesw[seat] += '+';
				else nesw[seat] += " ";
			}
		}
		for (let i = 0; i < 4; i++) {
			s += nesw[i] + nl;
		}
		if (this.status === Board.SCORING) {
			s += nl+ "----- 結果 -----"+nl;
			// メイク数
			const win	= this._countWinners_();
			const up	= win - this.getContract().level - 6;
			const make	= win - 6;
			
			if (up >= 0) {
				// メイク
				s += make + "メイク  ";
			} else {
				// ダウン
				s += (-up) + "ダウン  ";
			}
			
			s += "("+win+"トリック)"+nl;
			s += " N-S側のスコア：" + Score.calculate(this, Board.SOUTH) + nl;
		}
		return s;
	}
	
	/**
	 * ハンド文字列を生成します。
	 * @private
	 * @param	{Array.<Packet>} hand ハンド
	 * @param	{number}	seat 座席番号
	 * @param	{number}	suit	スーツ
	 * @return	{string}	ハンド文字列
	 */
	_getHandString_(hand, seat, suit) {
		let s = "CDHS".substring(suit-1, suit)+":";
		const oneSuit = hand[seat].subpacket(suit);
		oneSuit.arrange();
		const size = oneSuit.children.length;
		for (let i = 0; i < size; i++) {
			const c = oneSuit.children[i];
			if ((c.isHead == true)||(this.openCards.indexOf(c)>=0)) {
				s = s + c.toString().substring(2);
			}
		}
		return s;
	}
	
	/**
	 * @private
	 * @return	{number}	ウィナーの数
	 */
	_countWinners_() {
		const tr = this.getAllTricks(); // Trick[]
		if (tr == null) return 0;
		
		let win = 0;
		const declarer = this.getDeclarer();
		
		// winner を数える(Board にあったほうが便利)
		for (let i = 0; i < tr.length; i++) {
			if ( ((tr[i].winner ^ declarer) & 1) == 0 ) win++;
		}
		
		return win;
	}

/*----------------
 * static methods
 */
	/**
	 * 与えられたボードにおけるオリジナルハンドを計算します。
	 * 本メソッドでは、現在持っているハンドにこれまでのトリックのカードを
	 * 追加して結果を求めています。
	 * 
	 * @param		{Board} board	オリジナルハンドを求めたい Board
	 * @return		{Array.<Packet>} オリジナルハンドの配列(添字には Board.NORTH などを指定)
	 */
	static calculateOriginalHand(board) {
		const result = []; //Packet[4];
		for (let i = 0; i < 4; i++) result[i] = new Packet();
		
		// 今もっているハンドをコピー
		const original = board.getHand();
		for (let i = 0; i < original.length; i++) {
			for (let j = 0; j < original[i].children.length; j++) {
				result[i].add(original[i].children[j]);
			}
		}
		
		// プレイされたハンドをコピー
		const trick = board.getAllTricks();
		for (let i = 0; i < trick.length; i++) {
			const tr = trick[i];
			if (tr == null) break;
			let seat = tr.leader;
			for (let j = 0; j < tr.children.length; j++) {
				result[seat].add(tr.children[j]);
				seat++;
				seat = (seat % 4);
			}
		}
		
		// 並べ替え
		for (let i = 0; i < 4; i++) {
			result[i].arrange();
		}
		return result;
	}
}

/**
 * @classdesc このクラスはコントラクトブリッジにおいてスコアを算出します。
 * 			当面、デュプリケート方式による算出をおこないますが、将来
 * 			ラバー、マッチポイントなどへの機能拡張を行いたいとおもっています。
 *
 */
class Score {
	static VUL_BONUSES		= [1250, 2000, 500, 50];
	static NONVUL_BONUSES	= [800, 1300, 300, 50];
	
	/**
	 * 与えられたボード、席における点数を計算します。
	 * 与えられたボードが終了していない場合、IllegalStatusException
	 * がスローされます。
	 *
	 * @param	{Board} board		計算対象のボード
	 * @param	{number} seat		計算を行う座席(Board.NORTH など)
	 * @return	{number} 得点
	 */
	static calculate(board, seat) {
		if (board.status != Board.SCORING)
			throw new Error("ボードはまだ終了していないため、点数の計算はできません。");
		
		if ( (seat < 0)||(seat > 3) )
			throw new Error("指定された座席番号"+seat+"は無効です。");
		
		const vul = board.vul;
		const contract = board.getContract();
		if (contract.kind === Bid.PASS) return 0; // Passed-Out Board
		const declarer	= board.getDeclarer();
		
		return Score._calcImpl_(contract, Score.countWinners(board), declarer, seat, vul);
	}
	
	/**
	 * 与えられたボードにおいてディクレアラー側のとったトリック数をカウントします。
	 *
	 * @param {Board} board		ウィナーを数える対象となるボード
	 */
	static countWinners(board) {
		return BridgeUtils.countDeclarerSideWinners(board);
	}
	
	/**
	 * 
	 * @param {Bid} contract 
	 * @param {number} win 
	 * @param {number} declarer 
	 * @param {number} seat 
	 * @param {number} vul 
	 */
	static _calcImpl_(contract, win, declarer, seat, vul) {
		const make = win - 6;
		const up = win - contract.level - 6;
		
		let score = 0;
		if (up >= 0) {
			// コントラクトに対する基本点計算
			let trickScore = 0;
			switch (contract.suit) {
			
			case Bid.NO_TRUMP:
				trickScore = 30;
				break;
			
			case Bid.SPADE:
			case Bid.HEART:
				trickScore = 30;
				break;
			
			case Bid.DIAMOND:
			case Bid.CLUB:
				trickScore = 20;
				break;
			
			default:
				throw new Error("コントラクトのスーツが不正です");
			}
			trickScore = trickScore * contract.level;
			if (contract.suit === Bid.NO_TRUMP) trickScore+=10;
			
			// ダブルの時の修正
			if (contract.kind == Bid.DOUBLE) trickScore *= 2;
			if (contract.kind == Bid.REDOUBLE) trickScore *= 4;
			
			// アップトリック
			let uptrickBonus = 0;
			switch (contract.suit) {
			
			case Bid.NO_TRUMP:
				uptrickBonus = 30;
				break;
			
			case Bid.SPADE:
			case Bid.HEART:
				uptrickBonus = 30;
				break;
			
			case Bid.DIAMOND:
			case Bid.CLUB:
				uptrickBonus = 20;
				break;
			
			default:
				throw new Error("コントラクトのスーツが不正です");
			}
			
			// ダブルの時の修正
			if (contract.kind == Bid.DOUBLE) {
				if (Score._isVul_(vul, declarer)) score = trickScore + 200 * up;
				else score = trickScore + 100 * up;
			} else if (contract.kind == Bid.REDOUBLE) {
				if (Score._isVul_(vul, declarer)) score = trickScore + 400 * up;
				else score = trickScore + 200 * up;
			} else score = trickScore + uptrickBonus * up;
			
			// ゲーム、スラムボーナス
			const bonuses = (Score._isVul_(vul, declarer))?
								Score.VUL_BONUSES : Score.NONVUL_BONUSES;

			// ダブルメイクのボーナス
			if (contract.kind === Bid.DOUBLE) score += 50;
			else if (contract.kind == Bid.REDOUBLE) score += 100;
			
			const level = contract.level;
			if (level == 6) score += bonuses[0]; // Small Slum
			else if (level == 7) score += bonuses[1]; // Grand Slum
			else if (trickScore >= 100) score += bonuses[2]; // Game
			else score += bonuses[3];	// partial
			
			if ( ((declarer ^ seat) & 1) === 1 ) {
				score = -score;
			}
		}
		else {
			let down = -up;
			
			if (contract.kind == Bid.BID) {
				if (!Score._isVul_(vul, declarer)) score = -50 * down;
				else score = -100 * down;
			}
			else if (contract.kind == Bid.DOUBLE) {
				if (!Score._isVul_(vul, seat)) {
					for (let i = 0; down > 0; i++) {
						if (i === 0) score -= 100;
						else if ( (i > 0)&&(i < 3) ) score -= 200;
						else score -= 300;
						down--;
					}
				}
				else {
					for (let i = 0; down > 0; i++) {
						if (i == 0) score -= 200;
						else score -= 300;
						down--;
					}
				}
			}
			else if (contract.kind == Bid.REDOUBLE) {
				if (!Score._isVul_(vul, declarer)) {
					for (let i = 0; down > 0; i++) {
						if (i == 0) score -= 200;
						else if ( (i > 0)&&(i < 3) ) score -= 400;
						else score -= 600;
						down--;
					}
				}
				else {
					for (let i = 0; down > 0; i++) {
						if (i == 0) score -= 400;
						else score -= 600;
						down--;
					}
				}
			}
			
			if ( ((declarer ^ seat) & 1) == 1 ) {
				score = -score;
			}
		}
		return score;
	}
	
	static _isVul_(vul, seat) {
		let mask;
		if ( (seat == Board.NORTH)||(seat == Board.SOUTH) ) mask = 1;
		else mask = 2;
		
		if ((vul & mask) > 0) return true;
		return false;
	}
}

/**
 * @classdesc ブリッジ固有の概念に関する各種便利 static 関数を提供します。
 */
class BridgeUtils {
	static VALUE_STRING = "jA23456789TJQK";
	/**
	 * 指定されたハンドにおいて、指定スーツのアナーの点をカウントします。
	 * アナー点は、A:4 K:3 Q:2 J:1 で計算します。
	 *
	 * @param		{Packet} hand		アナー点計算の対象となるハンド
	 * @param		{?number} suit		カウントしたいスートの指定
	 * @return		{number | number[]} アナー点(0から10の間),
	 * 					numberを省略した場合、アナー点に関する情報
	 * 					配列の添字０に全体点、添字 Card.CLUB(=1), ……, Card.SPADE(=4)に
	 * 					各スーツの点数が格納されます。
	 */
	static countHonerPoint(hand, suit) {
		if (suit !== void 0) {
			let result = 0;
			const oneSuit = hand.subpacket(suit);
			
			if (oneSuit.countValue(Card.ACE  ) > 0) result += 4;
			if (oneSuit.countValue(Card.KING ) > 0) result += 3;
			if (oneSuit.countValue(Card.QUEEN) > 0) result += 2;
			if (oneSuit.countValue(Card.JACK ) > 0) result += 1;
			
			return result;
		}
		let result = [0,0,0,0,0];
		for (let suit = 1; suit < 5; suit++) {
			result[suit] = BridgeUtils.countHonerPoint(hand, suit);
			result[0] += result[suit];
		}
		return result;
	}
	
	/**
	 * 指定されたハンドでのアナーの枚数をカウントします。
	 * ただし、アナーが１枚でもあるスートについては、10 もアナーとみなします。
	 * 
	 * @param	{Packet} hand
	 * @param	{number} suit
	 * @returns	{number} アナーの枚数
	 */
	static countHoners(hand, suit) {
		let result = 0;
		const oneSuit = hand.subpacket(suit);
		
		if (oneSuit.countValue(Card.ACE  ) > 0) result++;
		if (oneSuit.countValue(Card.KING ) > 0) result++;
		if (oneSuit.countValue(Card.QUEEN) > 0) result++;
		if (oneSuit.countValue(Card.JACK ) > 0) result++;
		if ((oneSuit.countValue(10) > 0)&&(result > 0)) result++;
		
		return result;
	}
	
	/**
	 * 指定されたハンドの指定されたスートについて、そのバリューを文字列にします。
	 * AKQ52 のように降順に変換されます。
	 * 
	 * @param	{Packet}	hand	ハンド情報
	 * @param	{number}	suit	スート
	 * @returns	{string}	AKQ52 のような文字列
	 */
	static valuePattern(hand, suit) {
		const p = hand.subpacket(suit);
		p.arrange();
		
		let s = "";
		for (let i = 0; i < p.children.length; i++) {
			const v = p.children[i].value;
			s += BridgeUtils.VALUE_STRING.charAt(v);
		}
		return s;
	}
	
	/**
	 * 指定されたハンドが指定されたハンドパターンに適合するかの判定を行います。
	 * 本メソッドで指定するハンドパターンは valuePattern() の形式に準拠しますが、
	 * ワイルドカードを使用することができます。ワイルドカードとして、以下の文字
	 * が使用できます。ただし、ハンドパターンは降順に記述する必要があります。<BR>
	 *<BR>
	 * x...2-9 のいずれか１枚 <BR>
	 * X...2-T のいずれか１枚 <BR>
	 * ?...いずれか１枚 <BR>
	 * *...いずれか０枚以上(現在末尾でしか正しく機能しません) <BR>
	 *
	 * @param	{Packet}	hand	ハンド
	 * @param	{string}	pattern	パターン文字列
	 * @param	{number}	suit
	 * @returns	{boolean}	パターンに適合するか(true/false)
	 */
	static patternMatch(hand, pattern, suit) {
		const handpat = BridgeUtils.valuePattern(hand, suit);
		for (let i = 0; i < handpat.length; i++) {
			if (i == pattern.length) return false;
			const conditionLetter = pattern.charAt(i);
			const target = handpat.charAt(i);
			
			switch (conditionLetter) {
			
			case 'x':
				if ((target >= '2')&&(target <= '9')) break;
				return false;
			case 'X':
				if ( ((target >= '2')&&(target <= '9'))||(target == 'T') ) break;
				return false;
			case '?':
				break;
			case '*':
				return true; // 手抜き
			default:
				if ( conditionLetter == target ) break;
				return false;
			}
		}
		if (pattern.length == handpat.length) return true;
		if ( (pattern.length == handpat.length + 1)&&(pattern.endsWith("*")) ) return true;
		return false;
	}
	
	/**
	 * 与えられたボードにおけるオリジナルハンドを計算します。
	 * 本メソッドでは、現在持っているハンドにこれまでのトリックのカードを
	 * 追加して結果を求めているため、指定 Board に unspecified Card が含まれて
	 * いた場合、結果には unspecified Card が含まれることとなります。
	 * 
	 * @param		{Board} board	オリジナルハンドを求めたい Board
	 *
	 * @returns		{Packet[]}		オリジナルハンドの配列(添字には Board.NORTH などを指定)
	 */
	static calculateOriginalHand(board) {
		const result = [];
		for (let i = 0; i < 4; i++) result.push(new Packet());
		
		// 今もっているハンドをコピー
		const original = board.getHand();
		for (let i = 0; i < original.length; i++) {
			for (let j = 0; j < original[i].children.length; j++) {
				result[i].add(original[i].children[j]);
			}
		}
		
		// プレイされたハンドをコピー
		const tricks = board.getAllTricks();
		for (let i = 0; i < tricks.length; i++) {
			const tr = tricks[i];
			if (tr == null) break;
			let seat = tr.leader;
			for (let j = 0; j < tr.children.length; j++) {
				result[seat].add(tr.children[j]);
				seat++;
				seat = (seat % 4);
			}
		}
		
		// 並べ替え
		for (let i = 0; i < 4; i++) {
			result[i].arrange();
		}
		return result;
	}
	
	/**
	 * 与えられたボードにおいてディクレアラー側のとったトリック数をカウントします。
	 *
	 * @param		{Board} board		ウィナーを数える対象となるボード
	 * @returns		{number} ディクレアラー側のとったトリック数
	 */
	static countDeclarerSideWinners(board) {
		const tr = board.getAllTricks();
		if (tr == null) return 0;
		
		let win = 0;
		const declarer = board.getDeclarer();
		
		for (let i = 0; i < tr.length; i++) {
			if (tr[i] == null) break;
			if (!tr[i].isFinished()) break;
			const winner = tr[i].getWinner();
			if ( ((winner ^ declarer) & 1) == 0 ) win++;
		}
		
		return win;
	}
	
	/**
	 * 与えられたボードにおいてディフェンダー側のとったトリック数をカウントします。
	 *
	 * @param		{Board} board		ウィナーを数える対象となるボード
	 * @returns		{number} ディフェンダー側のとったトリック数
	 */
	static countDefenderSideWinners(board) {
		return board.getTricks() - BridgeUtils.countDeclarerSideWinners(board);
	}
	
	/**
	 * スート定数の文字列表現を返却します
	 *
	 * @param		{number} suit	スート定数
	 * @return		{string} スートの文字列表現(* S H D C Jo)
	 * 
	 */
	static suitString(suit) {
		switch (suit) {
			case Card.SPADE: return "S";
			case Card.HEART: return "H";
			case Card.DIAMOND: return "D";
			case Card.CLUB: return "C";
			default:
				return "Jo";
		}
	}
	
	/**
	 * バリュー定数の文字列表現を返却します
	 *
	 * @param	{number} value	バリュー定数
	 * @returns	{string}	バリューの文字列表現(AKQJT98765432)
	 *
	 */
	static valueString(value) {
		return BridgeUtils.VALUE_STRING.charAt(value);
	}
	
}

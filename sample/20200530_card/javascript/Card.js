/**
 * @fileOverview ブリッジシミュレーターのソースです。
 */
var inherits = function(childCtor, parentCtor) {
  Object.setPrototypeOf(childCtor.prototype, parentCtor.prototype);
};

/**
 * ReproducibleRandom class 定義
 * @classdesc 乱数 for test アルゴリズムは XorShift
 * {@link https://ja.wikipedia.org/wiki/Xorshift}
 * @constructor
 * @param	{number} seed 乱数シード
 */
var ReproducibleRandom = function(seed) {
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
	ReproducibleRandom.prototype.next = function() {
		var t = this.x ^ (this.x << 11);
	    this.x = this.y; this.y = this.z; this.z = this.w;
	    return this.w = (this.w ^ (this.w >>> 19)) ^ (t ^ (t >>> 8)); 
	}
	
	/**
	 * min以上max以下の乱数を生成する
	 * @param	{number} min 最小値(含む)
	 * @param	{number} max 最大値(含む)
	 * @return	{number} min～max の乱数値
	 */
	ReproducibleRandom.prototype.nextInt = function(min, max) {
		var r = Math.abs(this.next());
		return min + (r % (max + 1 - min));
	}

/**
 * NaturalCardOrder class 定義
 *
 * @classdesc 自然なカードの並び順を表します。
 * @constructor
 * @param	{number}	trump	トランプスートを示します。{@link Card}
 */
var NaturalCardOrder = function(trump) {

/*----------------
 * instance field
 */
	switch (trump) {
	case Card.HEART:
		/**
		 * スートの順位を表します。数値配列で、0,クラブ,ダイヤ,ハード,
		 * スペードの順に優先度を示す数値が格納されます。(大きい方が優先)
		 * @type	{Array} 
		 */
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
/*--------------
 * static field
 */
	/** Spade > Heart > Club > Diamondの順 */
	NaturalCardOrder.SUIT_ORDER_SPADE = [ 0, 2, 1, 3, 4 ];
	
	/** Heart > Club > Diamond > Spadeの順 */
	NaturalCardOrder.SUIT_ORDER_HEART = [ 0, 3, 2, 4, 1 ];
	
	/** Diamond > Spade > Heart > Clubの順 */
	NaturalCardOrder.SUIT_ORDER_DIAMOND = [ 0, 1, 4, 2, 3];
	
	/** Club > Diamond > Spade > Heartの順 */
	NaturalCardOrder.SUIT_ORDER_CLUB = [ 0, 4, 3, 1, 2];
	
	/** ACE が一番強く(大きく)、2 が一番弱い(小さい)並び順 */
	NaturalCardOrder.VALUE_ORDER = [ 15, 14, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13];
	
	/** 2 が一番強く(大きく)、ACE が一番弱い(小さい)並び順 */
	NaturalCardOrder.REVERSE_VALUE_ORDER = [ 1,   2,15,14,13,12,11,10, 9, 8, 7 ,  6,  5,  4];

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
	NaturalCardOrder.prototype.compare = function(a, b) {
		if ( a.suit == b.suit && a.value == b.value ) return 0;
		
		var suitA = this.suitOrder[a.suit];
		var suitB = this.suitOrder[b.suit];
		if (suitA > suitB) return 1;
		if (suitA < suitB) return -1;

		var valueA = NaturalCardOrder.VALUE_ORDER[a.value];
		var valueB = NaturalCardOrder.VALUE_ORDER[b.value];
		if (valueA > valueB) return 1;
		return -1;
	}

/**
 * Entity class 定義
 * @classdesc Entity は canvas 上に描画されるオブジェクトを表します。
 * @constructor
 */
var Entity = function() {

/*----------------
 * instance field
 */
	/** 描画上、コンテナとなる Entity (parent)
	 * @type {Entities}
	 */
	this.parent = undefined;
	/** この Entity の幅(pixel) */
	this.w = 0;
	/** この Entity の高さ(pixel) */
	this.h = 0;
	/** この Entity の x 座標(右が正、pixel) */
	this.x = 0;
	/** この Entity の y 座標(下が正、pixel) */
	this.y = 0;
	/**
	 * この Entity の向き(0..upright 1..right view 2..upside down 3..left view)
	 * @type {number}
	 */
	this.direction = 0; // upright
	/** この Entity を表示するか
	 * @type {boolean}
	 */
	this.isVisible = true;
}
/*------------------
 * static constants
 */
	/**
	 * 下のプレイヤーから見て直立
	 * @const {number}
	 */
	Entity.UPRIGHT = 0; 
	/**
	 * 右のプレイヤーから見て直立
	 * @const {number}
	 */
	Entity.RIGHT_VIEW = 1;
	/**
	 * 上のプレイヤーから見て直立
	 * @const {number}
	 */
	Entity.UPSIDE_DOWN = 2;
	/**
	 * 左のプレイヤーから見て直立
	 * @const {number}
	 */
	Entity.LEFT_VIEW = 3;

/*------------------
 * instance methods
 */
	/**
	 * 親オブジェクト(Entities)を設定します。this.parent への設定です。
	 * このメソッドは親オブジェクトからのみ呼ばれます。
	 * @param	{Entities} parent 親オブジェクト
	 */
	Entity.prototype.setParent = function(parent) {
		this.parent = parent;
	}
	/**
	 * この Entity の大きさを指定します。this.w, this.h への設定です。
	 * @param	{number} w 幅
	 * @param	{number} h 高さ
	 */
	Entity.prototype.setSize = function(w, h) {
		this.w = w;
		this.h = h;
	}
	/**
	 * 位置を指定します。左上が(0,0)で右下座標系です。
	 * this.x, this.y への設定です。
	 * @param	{number} x x座標
	 * @param	{number} y y座標
	 */
	Entity.prototype.setPosition = function(x, y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * この Entity の向き(0-3)を指定します。
	 * this.direction への設定です。
	 * @param	{number} dir 向き(0-3)
	 */
	Entity.prototype.setDirection = function(dir) {
		if (dir!=0 && dir!=1 && dir!=2 && dir!=3)
			throw new Error("setDirection で direction の値が不正です:"+dir);
		this.direction = dir;
	}
	
	/**
	 * この Entity の位置、大きさを取得します。
	 * @return	{object} {x: x座標, y: y座標, w: 幅, h: 高さ}
	 */
	Entity.prototype.getRect = function() {
		return {
			x: this.x,
			y: this.y,
			w: this.w,
			h: this.h };
	}
	
	/**
	 * この Entity を描画します。
	 * このメソッドは親オブジェクトからのみ呼ばれます。
	 * 子クラスでは isVisible が false のとき描画しないようにする必要があります
	 * Entity での実装はエラーのスローです。
	 * @param	{CanvasContext} ctx canvas.getContext('2d') で取得されるコンテキスト
	 */
	Entity.prototype.draw = function(ctx) {
		throw new Error("not implemented");
	}

/**
 * Entities class 定義
 * @classdesc Entities は複数の Entity をまとめる容器オブジェクトを表します。
 * @constructor
 * @extends	{Entity}
 */
var Entities = function() {

/*----------------
 * instance field
 */
	/**
	 * 子要素を格納します。子要素数は、this.children.length で取得できます。<br>
	 * <strong>直接書き込みは行わず、add を利用して下さい。</strong>
	 * @type	{Array.<Entity>}
	 * @see	#add
	 */
	this.children = [];
	Entity.call(this);
	/**
	 * 子要素を描画する際のレイアウトオブジェクト。
	 * @type	{Layout}
	 */
	//this.layout = new CardHandLayout();
	this.setSize(0, 0);
}
inherits(Entities, Entity);

/*------------------
 * instance methods
 */
	/**
	 * 子オブジェクトを追加します。子オブジェクトの親として自分を設定します。
	 * また、このオブジェクトに layout が設定している場合、layoutします。
	 * @param	{Entity} child 追加対象の Entity
	 */
	Entities.prototype.add = function(child) {
		if (this.children.indexOf(child) >= 0) {
			return;
		}
		child.setParent(this);
		this.children.push(child);
		if (this.layout != null) this.layout.layout(this);
	}
	
	/**
	 * 指定された子オブジェクトを引きます。ひかれたオブジェクトはこの
	 * オブジェクトの要素でなくなります。(parent が null になります)
	 *
	 * @param		{number} index	引くオブジェクトの番号(省略した場合、
	 *								最後の要素)
	 */
	Entities.prototype.pull = function(index) {
		if (index===void 0) index = this.children.length - 1;
		
		if (index < 0 || index >= this.children.length)
			throw new Error("pull で指定された index が不正です:"+index);
		var child = this.children[index];
		this.children.splice(index, 1); // 削除
		child.setParent(null);
		return child;
	}
	
	/**
	 * 向きを設定します。
	 * @override
	 */
	Entities.prototype.setDirection = function(direction) {
		this.direction = direction;
		this.children.forEach( function(child) {
			child.setDirection(direction);
		});
	}
	/**
	 * この Entities の位置、大きさを取得します。layout が設定されている場合、
	 * layout によりこのオブジェクトのサイズを変更後、返却します。
	 * @return	{object} x, y, w, h を含むオブジェクト
	 */
	Entities.prototype.getRect = function() {
		if (this.layout != null) {
			var d = this.layout.layoutSize(this);
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
	Entities.prototype.getSize = function() {
		if (this.layout != null) {
			var d = this.layout.layoutSize(this);
			this.w = d.w;
			this.h = d.h;
		}
		return { w: this.w, h: this.h };
	}
	
	/**
	 * この Entity を描画します。
	 * 描画前に設定されている layout があればレイアウトが再計算されます。
	 * このメソッドは親オブジェクトからのみ呼ばれます。
	 * @override
	 */
	Entities.prototype.draw = function(ctx) {
		if (this.layout != null) this.layout.layout(this);
		this.children.forEach( function(child) {
			child.draw(ctx);
		});
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
var Packet = function() {

/*----------------
 * instance field
 */
	Entities.call(this);
	this.layout = new CardHandLayout();
	this.cardOrder = new NaturalCardOrder(); // スペードスート
}
inherits(Packet, Entities);

/*------------------
 * instance methods
 */
	/**
	 * 所属しているカードをシャッフルします。
	 * 利用する乱数は ReproducibleRandom です。
	 */
	Packet.prototype.shuffle = function(seed) {
		if (this.children.length == 0) return;
		
		var rnd = new ReproducibleRandom(seed); // seed
		var tmp = []; // Card
		var size = this.children.length;
		for (var i = 0; i < size; i++) {
			var index = rnd.nextInt(0, this.children.length-1);
			tmp.push(this.children[index]); // 取得
			this.children.splice(index,1); // 削除
		}
		this.children = tmp;
	}
	
	/**
	 * カードを設定されている CardOrder に従って降順に並べ替えます。
	 */
	Packet.prototype.arrange = function() {
		var co = this.cardOrder;
		this.children.sort( function(a,b) { return co.compare(b, a); } );
	}
	
	/**
	 * 指定されたスートのカードの枚数を返却します。
	 * @param	{number} suit	スート
	 * @return	{number} スートの枚数
	 */
	Packet.prototype.countSuit = function(suit) {
		var c = 0;
		for (var i = 0; i < this.children.length; i++) {
			if (this.children[i].suit == suit) c++;
		}
		return c;
	}
	
	/**
	 * 指定されたバリューのカードの枚数を返却します。
	 * @param	{number} value	バリュー
	 * @return	{number} 枚数
	 */
	Packet.prototype.countValue = function(value) {
		var c = 0;
		var size = this.children.length;
		for (var i = 0; i < size; i++) {
			if (this.children[i].value == value) c++;
		}
		return c;
	}
	
	/**
	 * 指定されたスートのカードを抽出して返却します。
	 * @param	{number} suit	スート
	 * @return	{Packet}	指定されたスートからなる Packet
	 */
	Packet.prototype.subpacket = function(suit) {
		var result = new Packet();
		var size = this.children.length;
		for (var i = 0; i < size; i++) {
			var card = this.children[i];
			if (card.suit == suit) result.add(card);
		}
		return result;
	}
	
	/**
	 * 含まれるカードの表裏を設定します。
	 * @param	{boolean} head	表にする場合 true、裏にする場合 false
	 */
	Packet.prototype.turn = function(head) {
		for (var i = 0; i < this.children.length; i++) {
			this.children.isHead = head;
		}
	}
	
	/**
	 * Packet 同士の集合減算をします。this - target の Packet を返します。
	 * @param	{Packet} target	差をとる対象
	 * @return	{Packet} this - target
	 */
	Packet.prototype.sub = function(target) {
		var result = new Packet();
		
		for (var i = 0; i < this.children.length; i++) {
			var c = this.children[i];
			if (target.children.indexOf(c) == -1) result.add(c);
		}
		return result;
	}
	
	/**
	 * @override
	 */
	Packet.prototype.toString = function() {
		var s = "";
		var size = this.children.length;
		for (var i = 0; i < size; i++) {
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
	Packet.provideDeck = function() {
		var p = new Packet();
		for (var suit = 1; suit < 5; suit++) {
			for (var value = 1; value < 14; value++) {
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
	Packet.deal = function(pile, hands, begin) {
		if (begin===void 0) begin = 0;
		
		var n = hands.length;
		if (begin < 0 || begin >= n)
			throw new Error("deal で begin の値が不正です:"+begin);
		
		var c = pile.children.length;
		for (var i = 0; i < c; i++) {
			var card = pile.pull();
			hands[ (i+begin) % n ].add(card);
		}
	}


/**
 * Field class 定義
 * @classdesc カードなどが描画されるブラウザ上の画面領域
 * @constructor
 * @extends	Entities
 */
var Field = function(canvasId) {

/*----------------
 * instance field
 */
	Entities.call(this);
	/** html 内の canvas 要素 */
	this.canvas = document.getElementById(canvasId);
	this.setSize(canvas.clientWidth, canvas.clientHeight);
	/** canvas の 2D context */
	this.ctx = canvas.getContext('2d');
	/** spot light の位置 (0-3) */
	this.spot = 0;
	
	this.layout = null; // pos/size は初期値のまま
}
inherits(Field, Entities);

/*------------------
 * instance methods
 */
	/** @override */
	Field.prototype.getRect = function() {
		//var d = this.layout.layoutSize(this);
		
		return { x:this.x, y:this.y, w:this.w, h:this.h };
	}
	/** override */
	Field.prototype.draw = function() {
		this._drawBackground_();
		for (var i = 0; i < this.children.length; i++) {
			this.children[i].draw(this.ctx);
		}
		this._drawSpotlight_();
	}
	
	/**
	 * 背景を描画します(緑基調、下が明るい)
	 * @private
	 */
	Field.prototype._drawBackground_ = function() {
		//
		// バック(緑のグラデーション)
		//
		var r = this.getRect();
		var step = r.h / 40;
		for (var i = 0; i < 40; i++) {
			this.ctx.fillStyle = 'rgb(' + (40+i/2) + ','+(80+i)+',30)'; // 緑
			this.ctx.fillRect(r.x, r.y+Math.floor(i*step), r.w, r.h+Math.floor(step+1));
		}
	}
	
	/**
	 * スポットライトを描画します(黄白色)
	 * @private
	 */
	Field.prototype._drawSpotlight_ = function() {
		//
		// 順番を示すスポットライト
		//
		var r = this.getRect();
		var spot = this.spot;
		var direction = this.direction;
		if (spot == -1) return;
		
		var d = (spot + direction)%4;
		var x, y;
		
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
		
		for (var radius = 95; radius > 0; radius -=4) {
			this.ctx.fillStyle = 'rgba(255,255,192,'+((95.0-radius)/1000)+')'; // 明るい黄色(透明度つき)
			this.ctx.beginPath();
			this.ctx.arc(x, y, radius, 0, 2*Math.PI, false);
			this.ctx.fill();
		}
	}
	
	/**
	 * Event type は
	 * https://developer.mozilla.org/en-US/docs/Web/Events
	 * を見ること。
	 */
	Field.prototype.addEventListener = function(type, listener, options) {
		this.canvas.addEventListener(type, listener, options);
	}
	
	Field.prototype.removeEventListener = function(type, listener, options) {
		this.canvas.addEventListener(type, listener, options);
	}
	
/**
 * Card class 定義
 *
 * @classdesc Card クラスでは、java 版の Unspecified の枠組みを省略しました。
 * また、GuiedCard となっています。
 * @constructor
 * @extends Entity
 */
var Card = function(suit, value) {

/*----------------
 * instance field
 */
	Entity.call(this);
	this.setSize(Card.XSIZE, Card.YSIZE);
	/**
	 * このカードのスートを表す 0～4 の数値。スート定数を参照。
	 * @type	{Number}
	 */
	this.suit = suit;
	
	/**
	 * このカードのバリューを表す数値。1(ACE)～13(KING) の値をとる。
	 * @type	{Number}
	 */
	this.value = value;
	
	/**
	 * このカードが表向きかどうかを保持。
	 * @type	{Boolean}
	 */
	this.isHead = true;
}
inherits(Card, Entity); // 継承

/*---------------
 * static fields
 */
	Card.XSIZE = 59;
	Card.YSIZE = 87;
	Card.XSTEP = 14;
	Card.YSTEP = 16;
	
	/** suit 値用の定数 */
	Card.JOKER = 0; // 利用されない(JOKER は value も同一値)
	/** スペードを示す定数(4) */
	Card.SPADE = 4;
	/** ハートを示す定数(3) */
	Card.HEART = 3;
	/** ダイヤを示す定数(2) */
	Card.DIAMOND = 2;
	/** クラブを示す定数(1) */
	Card.CLUB = 1;
	
	/** ACE を表す value 値(1) */
	Card.ACE = 1;
	/** JACK を表す value 値(11) */
	Card.JACK = 11;
	/** QUEEN を表す value 値(12) */
	Card.QUEEN = 12;
	/** KING を表す value 値(13) */
	Card.KING = 13;
	
/*------------------
 * instance methods
 */
	/**
	 * カードを描画します。
	 * @param	{Object} ctx	2Dグラフィックコンテキスト
	 * @override
	 */
	Card.prototype.draw = function(ctx) {
		if (!this.isVisible) return;
		var x,y, c,s;
		var r = this.getRect();
		switch (this.direction) {
		case 1:
			c = 0; s = 1;	x = r.y;		y = -r.x - r.h; break;
		case 2:
			c = -1; s = 0;	x = -r.x - r.w;	y = -r.y - r.h; break;
		case 3:
			c = 0; s = -1;	x = -r.y - r.w;	y = r.x; break;
		default:
			c = 1; s = 0;	x = r.x;		y = r.y; break;
		}
		var img;
		if (this.isHead) img = CardImageHolder.getImage(this.suit, this.value);
		else img = cardImageHolder.getBackImage();
		
		ctx.setTransform(c, s, -s, c, 0, 0);
		ctx.drawImage(img, x, y, this.w, this.h);
		ctx.setTransform(1, 0, 0, 1, 0, 0);
	}
	
	/**
	 * この Entity の位置、大きさを取得します。
	 * @override
	 * @return		{Object} {x: <<x座標>>, y: <<y座標>>, w: <<幅>>, h: <<高さ>>
	 */
	Card.prototype.getRect = function() {
		return {
			x: this.x,
			y: this.y,
			w: Card.XSIZE,
			h: Card.YSIZE };
	}
	
	/**
	 * Card の文字列表現を得ます。
	 * @return	{string}	このカードの文字列表現
	 */
	Card.prototype.toString = function() {
		var s;
		if (this.isHead) s = "/"; else s = "_";
		switch (this.suit) {
			case Card.SPADE:
				s += "S";
				break;
			case Card.HEART:
				s += "H";
				break;
			case Card.DIAMOND:
				s += "D";
				break;
			case Card.CLUB:
				s += "C";
				break;
			default:
				s += "Jo";
		}
		switch (this.value) {
			case 0:
				break;
			case Card.ACE:
				s = s + "A";
				break;
			case 10:
				s = s + "T";
				break;
			case Card.JACK:
				s = s + "J";
				break;
			case Card.QUEEN:
				s = s + "Q";
				break;
			case Card.KING:
				s = s + "K";
				break;
			default:
				s = s + this.value;
		}
		return s; // + "]";
	}

/**
 * CardImageHolder class
 * @classdesc カードのグラフィックを取得する static メソッドを提供します。
 * @constructor
 */
var CardImageHolder = function() {
}
	/**
	 * カード表面のイメージオブジェクトの配列
	 * @type	{Array.<Image>}
	 */
	CardImageHolder.IMAGE = [];
	/**
	 * カード裏面のイメージオブジェクト配列
	 * @type	{Array.<Image>}
	 */
	CardImageHolder.BACK_IMAGE = [];
	
	/**
	 * カードイメージを読み込みます。
	 */
	CardImageHolder.loadImages = function(path) {
		if (path===void 0) path = 'images/';
		if (!path.endsWith('/')) path = path + '/';
		// カード表面の読み込み
		var s = ['c', 'd', 'h', 's'];
		for (var i = 0; i < s.length; i++) {
			var suit = s[i];
			for (var value = 1; value < 14; value++) {
				var imgsrc = 'images/'+suit+value+'.gif?'+ new Date().getTime(); // nocache
				var img = new Image();
				img.src = imgsrc;
				CardImageHolder.IMAGE.push(img);
			}
		}
		// カード裏面の読み込み
		for (var i = 0; i < 4; i++) {
			var imgsrc = 'images/back'+i+'.gif?'+ new Date().getTime();
			var img = new Image();
			img.src = imgsrc;
			CardImageHolder.BACK_IMAGE.push(img);
		}
	}
	
	/**
	 * カードの表面イメージを取得します。
	 *
	 * @param	suit	スート(1,2,3,4)
	 * @param	value	値(1-13)
	 */
	CardImageHolder.getImage = function(suit, value) {
		var s = (suit-1)*13 + (value-1);
		return CardImageHolder.IMAGE[s];
	}
	
	/**
	 * カードの裏面イメージを取得します。
	 */
	
	CardImageHolder.getBackImage = function() {
		return CardImageHolder.BACK_IMAGE[0];
	}
	
// execute loadImages(static initializer)
CardImageHolder.loadImages();
	
/**
 * CardHandLayout class 定義
 * @constructor
 */
var CardHandLayout = function() {
}

/*------------------
 * instance methods
 */
	/**
	 * 通常のハンドのレイアウトを施します。
	 * @param	{Entities} entities レイアウトを行う対象 Entities
	 */
	CardHandLayout.prototype.layout = function(entities) {
		var xpos, ypos, n;
		
		n = entities.children.length;
		
		var direction = entities.direction;
		var r = entities.getRect();
		
		switch (direction) {
		
		case Entity.UPRIGHT:
			// direction == 0 の場合の実装
			xpos = r.x;
			ypos = r.y;
			
			for (var i = 0; i < n; i++) {
				var ent = entities.children[i];
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
			
			for (var i = 0; i < n; i++) {
				var ent = entities.children[i];
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
			
			for (var i = 0; i < n; i++) {
				var ent = entities.children[i];
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
			
			for (var i = 0; i < n; i++) {
				var ent = entities.children[i];
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
	CardHandLayout.prototype.layoutSize = function(entities) {
		var n = entities.children.length;
		
		if (n == 0) return { w: 0, h: 0 };
		
		var direction = entities.direction;
		
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

/**
 * Bid class定義
 *
 * @classdesc ビッド、またはコントラクトを表現するクラスです。
 * @constructor
 * @param	{number} kind	ビッド種類(Bid.PASS, Bid.BID, Bid.DOUBLE,
 * 							Bid.REDOUBLE)
 * @param	{number} level	ビッドレベル(1～7)
 * @param	{number} suit	ビッドスーツ(1-4)、または Bid.NO_TRUMP(5)
 * @version		8, July 2018
 * @author		Yusuke Sasaki
 */
var Bid = function(kind, level, suit) {
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
/*----------------
 * instance field
 */
	/** ビッドの種類 */
	this.kind  = kind;
	/** ビッドのレベル */
	this.level = level;
	/** ビッドスート */
	this.suit  = suit;
}
/*------------------
 * static constants
 */
	/** kind (Bid) を表す定数. */
	Bid.BID			= 0;
	
	/** kind (Pass) を表す定数. */
	Bid.PASS		= 1;
	
	/** kind (Double) を表す定数. */
	Bid.DOUBLE		= 2;
	
	/** kind (Redouble) を表す定数. */
	Bid.REDOUBLE	= 3;
	
	/** bid suit (club) を表す定数(=1). */
	Bid.CLUB		= Card.CLUB;	// = 1;
	
	/** bid suit (diamond) を表す定数(=2). */
	Bid.DIAMOND		= Card.DIAMOND;	// = 2;
	
	/** bid suit (heart) を表す定数(=3). */
	Bid.HEART		= Card.HEART;	// = 3;
	
	/** bid suit (spade) を表す定数(=4). */
	Bid.SPADE		= Card.SPADE;	// = 4;
	
	/** bid suit (no trump) を表す定数(=5). */
	Bid.NO_TRUMP	= 5;
	
/*------------------
 * instance methods
 */
	/**
	 * 指定されたコントラクトのあとに宣言可能か判定します。<BR>
	 * 例) [1C] < [1D] < [2C] < [2CX] < [2CXX] となります。
	 * [pass] は常に可能(true)です。
	 * @param	{Bid} contract 判定先となるコントラクト
	 */
	Bid.prototype.isBiddableOver = function(contract) {
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
	Bid.prototype.toString = function() {
		switch (this.kind) {
		case Bid.BID:
			return "[" + this.level + " " +
					" C D H SNT".substring(this.suit*2-2, this.suit*2) + "  ]";
		case Bid.PASS:
			return "[pass  ]";
		
		case Bid.DOUBLE:
			return "[" + this.level + " " +
					" C D H SNT".substring(this.suit*2-2, this.suit*2) + "X ]";
		
		case Bid.REDOUBLE:
			return "[" + this.level + " " +
					" C D H SNT".substring(this.suit*2-2, this.suit*2) + "XX]";
			
		default:
			return "[? bid ]";
		}
	}
	
/**
 * DummyHandLayout class 定義
 *
 * @classdesc ダミーハンドのレイアウトです。
 * 			のレイアウトは、対象となる Packet が NaturalCardOrder で
 * 			arrange() されていることを想定して処理されます。
 * @constructor
 */
var DummyHandLayout = function() {
}

/*------------------
 * instance methods
 */
	/**
	 * ダミーハンドのレイアウトを施します。
	 * @param	{Entities} entities レイアウトを行う対象 Entities
	 */
	DummyHandLayout.prototype.layout = function(packet) {
		var co = packet.cardOrder; // targeting NaturalCardOrder
		var suitOrder = co.suitOrder;
		
		var count = this._countSuits_(packet, suitOrder);
		
		if (count[0] + count[1] + count[2] + count[3] == 0) return;
		
		//
		// サイズ計算用
		//
		var maxCount = -1;
		for (var i = 0; i < 4; i++) {
			if (maxCount < count[i]) maxCount = count[i];
		}
		
		//
		var xpos, ypos, n = 0;
		var dir = ( packet.direction + 2 ) % 4;
		var r = packet.getRect();
		
		switch (packet.direction) {
		
		case Entity.UPRIGHT:
			 xpos = r.x + 3 * (Card.XSIZE + 3);;
			
			// それぞれのカードの配置を行う
			for (var i = 0; i < 4; i++) {
				ypos = r.y + (maxCount-1)*Card.YSTEP;
				for (var j = 0; j < count[i]; j++) {
					var ent = packet.children[n++];
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
			
			for (var i = 0; i < 4; i++) {
				xpos = r.x + (maxCount-1)*Card.YSTEP;
				
				for (var j = 0; j < count[i]; j++) {
					var ent = packet.children[n++];
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
			for (var i = 0; i < 4; i++) {
				ypos = r.y;
				for (var j = 0; j < count[i]; j++) {
					var ent = packet.children[n++];
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
			
			for (var i = 0; i < 4; i++) {
				xpos = r.x;
				for (var j = 0; j < count[i]; j++) {
					var ent = packet.children[n++];
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
	DummyHandLayout.prototype._countSuits_ = function(packet, suitOrder) {
		// それぞれのスートの枚数を数える
		var count = [0, 0, 0, 0];
		
		var lastSuit = -1;
		var suit = -1;
		
		for (var i = 0; i < packet.children.length; i++) {
			var card = packet.children[i];
			count[4 - suitOrder[card.suit]]++;
		}
		
		return count;
	}
	
	/**
	 * 実際のレイアウトは変更せず、レイアウトした場合の大きさを計算します。
	 * @param	{Entities} entities 計算対象の Entities
	 * @return	{object} w, h を含むオブジェクト
	 */
	DummyHandLayout.prototype.layoutSize = function(packet) {
		var co = packet.cardOrder;
		var suitOrder = co.suitOrder;
		
		var count = this._countSuits_(packet, suitOrder);
		
		//
		// サイズ計算用
		//
		var maxCount = -1;
		for (var i = 0; i < 4; i++) {
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

/**
 * BiddingHistory class 定義
 *
 * @classdesc Board の機能のうち、オークションに関係する部分の実際の処理を
 * 受け持つクラスです。
 * @constructor
 * @param	{number} dealer ディーラーの座席番号(0-3)
 * @version		8, July 2018
 * @author		Yusuke Sasaki
 */
var BiddingHistory = function(dealer) {

/*----------------
 * instance field
 */
	/**
	 * ディーラーの座席番号(0-3)
	 * @type	{number}
	 */
	this.dealer = dealer;
	/**
	 * ビッド履歴。配列の大きさはビッドの回数を示します。
	 * @type	{Array.<Bid>}
	 */
	this.bid = [];
	/**
	 * 現時点のコントラクト。最終コントラクトとは限りません。
	 * @type	{Bid}
	 */
	this.contract = null;
	/**
	 * 現時点のディクレアラー。最終ディクレアラーとは限りません。
	 * @type	{number}
	 */
	this.declarer = -1;
	/**
	 * コントラクトが終了している場合、true
	 * @type	{boolean}
	 */
	this.finished = false;
}

	/**
	 * 座席定数
	 */
	BiddingHistory.NORTH = 0;
	BiddingHistory.EAST = 1;
	BiddingHistory.SOUTH = 2;
	BiddingHistory.WEST = 3;

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
	BiddingHistory.prototype.allows = function(b) { // b is Bid
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
	 * 不可能なビッドを行おうとすると、IllegalPlayException がスローされます。
	 *
	 * @param		{Bid} newBid 新たに行うビッド
	 */
	BiddingHistory.prototype.play = function(newBid) {
		// ビッドできるかのチェックを行う.
		if (!this.allows(newBid))
			throw new Error("Illegal bid:" + newBid.toString());
		
		// ビッドできるので、ビッド履歴に加える.
		this.bid.push(newBid);
		
		// declarer, contract 更新, パス続いたか判定
		switch (newBid.kind) {
		case Bid.PASS:
			// パスが続いたか
			var passCount = 0;
			for (var i = this.bid.length - 1; i >= 0; i--) {
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
			var n;
			for (n = (1 - (this.bid.length & 1)); n < this.bid.length; n += 2) {
				var b = this.bid[n];
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
	BiddingHistory.prototype.undo = function() {
		if (this.bid.length == 0)
			throw new Error("ビッドされていないので、undo() できません");
		this.contract	= null;
		this.bid.pop();
		this.declarer	= -1;
		this.finished	= false;
		
		// contract を見つける
		var lastBidCount = 0;
		for (var i = this.bid.length-1; i >= 0; i--) {
			if (this.bid[i].kind == Bid.BID) {
				this.contract = this.bid[i];
				lastBidCount = i+1;
				break;
			}
		}
		
		// declarer を見つける
		var n;
		for (n = (1 - (lastBidCount & 1)); n < lastBidCount; n += 2) {
			var b = this.bid[n];
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
	BiddingHistory.prototype.getAllBids = function() {
		var result = this.bid.slice(0, this.bid.length);
		return result;
	}
	
	/**
	 * 次にビッドする席の番号を返します。
	 *
	 * @return		{number} 席番号
	 */
	BiddingHistory.prototype.getTurn = function() {
		return (this.dealer + this.bid.length) % 4;
	}
	
	/**
	 * コントラクトを設定します
	 * @param	{Bid} contract コントラクト
	 * @param	{number} ディクレアラーの座席番号
	 */
	BiddingHistory.prototype.setContract = function(contract, declarer) {
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
	BiddingHistory.prototype.reset = function(dealer) {
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
	BiddingHistory.prototype.toString = function() {
		if ( (this.bid.length == 0)&&(this.finished) )
			return "Bidding Sequence Unknown";
		var result = "   N       E       S       W\n";
		for (var i = 0; i < this.dealer; i++) {
			result += "        ";
		}
		var seat = this.dealer;
		
		for (var i = 0; i < this.bid.length; i++) {
			result += this.bid[i].toString();
			seat++;
			if (seat == 4) {
				result += "\n";
				seat = 0;
			}
		}
		return result + "\n";
	}

/**
 * TrickLayout class 定義
 * @constructor
 */
var TrickLayout = function() {
}

	/**
	 * TrickLayout では、target のサイズを変更しません。
	 * @param	{Trick} target レイアウト対象の Trick
	 */
	TrickLayout.prototype.layout = function(target) {
		var trick = target;
		var direction	= trick.direction;
		var leader		= trick.leader;
		var size		= trick.children.length;
		var x = trick.x;
		var y = trick.y;
		var w = trick.w;
		var h = trick.h;
		
		var d = (4 + leader - direction) % 4;
		
		for (var i = 0; i < size; i++) {
			var ent = trick.children[i];
			
			switch ( (i+d)%4 ) {
			
			case 0: // 上
				ent.setPosition(x + (w - ent.w)/2, y);
				break;
			case 1: // 右
				ent.setPosition(x + (w - ent.w),y + (h - ent.h)/2);
				break;
			case 2: // 下
				ent.setPosition(x + (w - ent.w)/2, y + (h - ent.h) );
				break;
			case 3: // 左
				ent.setPosition(xpos, ypos + (h - ent.h)/2);
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
	TrickLayout.prototype.layoutSize = function(target) {
		return {w: target.w, h: target.h };
	}
	
/**
 * Trick class 定義
 *
 * @classdesc Trick は、場に出ているトリック、プレイされたトリックをパックする。
 * Trick では、１トリックを構成するカードの保持の他、トリックのウィナー
 * の判定を行う。
 * hand に関する情報は保持しない。
 * constructor で Trick(Trick) は未実装。(GuiedTrick にはある)
 * @constructor
 * @param	{number} leader leader の座席番号
 * @param	{number} trump trumpスート
 * @extends Packet
 */
var Trick = function(leader, trump) {

/*----------------
 * instance field
 */
	Packet.call(this);
	/**
	 * リーダーを示す数値
	 * @type {Number}
	 */
	this.leader = leader;
	/**
	 * ウィナーを示す数値
	 * @type {Number}
	 */
	this.winner = -1;
	/**
	 * ウィナーカード
	 * @type {Card}
	 */
	this.winnerCard = null;
	/**
	 * トランプスート
	 * @type {Number}
	 */
	this.trump = trump;
	
	this.direction = Entity.UPRIGHT;
	this.setSize(Trick.WIDTH, Trick.HEIGHT);
	this.layout = new TrickLayout();
}
inherits(Trick, Packet);

/*--------------
 * class fields
 */
	Trick.WIDTH  = 300;
	Trick.HEIGHT = 200;

/*------------------
 * instance methods
 */
	/**
	 * 次は誰の番かを返す.
	 * NESW の順である. Dummy が返ることもある.
	 * @return	{number} 次の番の座席定数
	 */
	Trick.prototype.getTurn = function() {
		return ((this.children.length + this.leader) % 4);
	}
	
	/**
	 * このトリックが終っているかテストする。
	 * 終っている場合、winner, winnerCard の値が有効となる。
	 * @return	{boolean} このトリックが終わっているか
	 */
	Trick.prototype.isFinished = function() {
		return ( this.children.length == 4 );
	}
	
	/**
	 * Trick は４枚までしか保持せず、４枚そろった時には Winner がきまる.
	 * @param	{Card} card		追加するカード
	 * @override
	 */
	Trick.prototype.add = function(card) {
		if (this.isFinished())
			throw new Error("終了した Trick に対して add(Card) できません。");
		
		Packet.prototype.add.call(this, card); // super.add(card);
		
		if (this.children.length == 4) this._setWinner_();
	}
	
	/**
	 * Winner を lead, trump などから決定します。
	 * @private
	 */
	Trick.prototype._setWinner_ = function() {
		if ( this.children.length  == 0) {
			this.winnerCard = null;
			return;
		}
		
		// winner をセットする
		this.winnerCard = this.children[0];
		this.winner = 0;
		var starter = winnerCard.suit;
		for (var i = 1; i < this.children.length; i++) {
			var c = this.children[i];
			if (this.winnerCard.suit == this.trump) {
				// NO_TRUMP のときはここにこない
				if ((c.suit == this.trump)
						&&(this.winnerCard.value != Card.ACE)) {
					if ((c.value > this.winnerCard.value)||
						(c.value == Card.ACE)) {
						this.winnerCard = c;
						this.winner = i;
					}
				}
			} else {
				// winner のスーツは場のスーツ
				if (c.suit == trump) {
					this.winnerCard = c;
					this.winner = i;
				} else if ((c.suit == starter)
							&&(this.winnerCard.value != Card.ACE)) {
					if ((c.value > this.winnerCard.value)
							||(c.value == Card.ACE)) {
						this.winnerCard = c;
						this.winner = i;
					}
				}
			}
		}
	}
	
/**
 * PlayHistory class 定義
 *
 * @classdesc PlayHistory クラスは、コントラクトブリッジにおけるプレイ部分
 * 				の状態、ルールをパックします。
 * 				本クラスは Board オブジェクトに保持され、プレイ部分の実処理を
 * 				行います。
 * @constructor
 * @version		10, July 2018
 * @author		Yusuke Sasaki
 * @param		{PlayHistory} src コピー元のオブジェクト。
 *								省略時は新規オブジェクトを生成します。
 */
var PlayHistory = function(src) {

/*----------------
 * instance field
 */
	if (src!==void 0) {
		// 代入するのみ(意味は？)
		this.hand = src.hand;
		this.trick = src.trick;
		/** 現在まででプレイされているトリック数(プレイ中は含まない) */
		this.trickCount = src.trickCount;
		this.trump = src.trump;
	} else {
		/**
		 * プレイ中のハンド
		 * @type	{Array.<Packet>}
		 */
		this.hand = []; // Packet[4]
		/**
		 * プレイ中のトリック。要素数は 13 以下。
		 * @type	{Array.<Trick>}
		 */
		this.trick = []; // Trick[13]
		/**
		 * 現在まででプレイされているトリック数(プレイ中は含まない)
		 * @type	{number}
		 */
		this.trickCount = 0;
		/**
		 * トランプスート
		 * @type	{number}
		 */
		this.trump = -1;
	}
}

	/**
	 * 座席定数
	 */
	PlayHistory.NORTH = BiddingHistory.NORTH;
	PlayHistory.EAST = BiddingHistory.EAST;
	PlayHistory.SOUTH = BiddingHistory.SOUTH;
	PlayHistory.WEST = BiddingHistory.WEST;
	PlayHistory.SEAT_STRING = [ "North", "East", "South", "West" ];

/*------------------
 * instance methods
 */
	/**
	 * ハンドを指定されたものに設定します。
	 * プレイが開始されている場合は設定できません。
	 *
	 * @param		{Packet} hand 設定するハンド
	 */
	PlayHistory.prototype.setHand = function(hand) {
		if ( (this.trick[0])&&(this.trick[0].children.length > 0) )
			throw new Error("すでにプレイが開始されているため" +
											" setHand は行えません。");
		if (hand.length != 4)
			throw new Error("４人分のハンドが指定されていません。");
		
		for (var i = 0; i < 4; i++) {
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
	PlayHistory.prototype.setContract = function(leader, trump) {
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
	PlayHistory.prototype.allows = function(p) {
		var turn = this.trick[this.trickCount].getTurn();
		
		// hand[turn] が指定されたカード持っていない場合 false
		if (this.hand[turn].children.indexOf(p) == -1) return false;
		
		// スートフォローに従っているか
		var lead = this.trick[this.trickCount].children[0];
		if (lead===void 0) return true;
		var suit = lead.suit;
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
	PlayHistory.prototype.play = function(p) {
		if (!this.allows(p))
			throw new Error(p.toString() + "は現在プレイできません。");
		
		var turn = this.trick[this.trickCount].getTurn();
		
		var drawn = this.hand[turn].pull(p);
		drawn.isHead = true;
		var tr = this.trick[this.trickCount];
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
	PlayHistory.prototype.getTurn = function() {
		return this.trick[this.trickCount].getTurn();
	}
	
	/**
	 * 現在まででプレイされているトリック数を取得する。
	 * プレイ中のトリックについてはカウントされない。
	 * @return	{number} this.trickCount を返します。
	 */
	PlayHistory.prototype.getTricks = function() {
		return this.trickCount;
	}
	
	/**
	 * 指定されたラウンドのトリックを返却します。
	 * ラウンドは 0 から 12 までの整数値です。
	 * 省略した場合、現在プレイ中のトリックを返します。
	 * @param	{number} index ラウンド
	 * @return	{Trick} トリック
	 */
	PlayHistory.prototype.getTrick = function(index) {
		if (index!==void 0) return this.trick[index];
		if (this.trickCount == 13) return this.trick[12];
		return this.trick[this.trickCount];
	}
	
	/**
	 * すべてのトリックを取得します。
	 * @return	{Array.<Trick>} トリックすべて(null のことがあります)
	 */
	PlayHistory.prototype.getAllTricks = function() {
		if (this.trick.length == 0) return null;
		if (this.hand[0] == null) return null;
		
		var n = this.trickCount + 1;
		if (this.trickCount == 13) n--;
		return this.trick.slice(0, this.trick.length);
	}
	
	/**
	 * プレイが終了しているかを判定します。
	 * @return	{boolean} プレイが終了しているか
	 */
	PlayHistory.prototype.isFinished = function() {
		return ( (this.trickCount == 13) && (this.trick[12].children.length == 4) );
	}
	
	/**
	 * この PlayHistory を初期化します。
	 */
	PlayHistory.prototype.reset = function() {
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
	PlayHistory.prototype.undo = function() {
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
		var seatToBePushbacked = (this.trick[this.trickCount].getTurn() + 3) % 4;
		
		// 最後のプレイを取得する
		var lastPlay = this.trick[this.trickCount].pull();
//		lastPlay.turn(false); // このオブジェクトは Dummy が誰かを知らない
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
	PlayHistory.prototype.toString = function() {
		var result = "";
		
		result += "N : " + this.hand[PlayHistory.NORTH]	+ "\n";
		result += "E : " + this.hand[PlayHistory.EAST]		+ "\n";
		result += "S : " + this.hand[PlayHistory.SOUTH]	+ "\n";
		result += "W : " + this.hand[PlayHistory.WEST]		+ "\n";
		
		result += "\n";
		
		if (this.trickCount < 13) result += this.trick[this.trickCount];
		
		result += "\n\n";
		for (var i = this.trickCount-1; i >= 0; i--) {
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
	
/**
 * TableGui class 定義
 * @constructor
 * @extends Entity
 */
var TableGui = function(board) {
	Entity.call(this);
	
	this.board = board;
	this.setSize(TableGui.WIDTH, TableGui.HEIGHT);
}
inherits(TableGui, Entity);

/*------------------
 * static constants
 */
	TableGui.WIDTH = 640;
	TableGui.HEIGHT = 480;
	
/*------------------
 * instance methods
 */
	/**
	 * 左上のボード情報枠を描画
	 * @private
	 * @param	{Context} ctx Canvas のグラフィックコンテキスト
	 */
	TableGui.prototype._drawContract_ = function(ctx) {
		var DIRECTIONS = ["North","East","South","West"];
		var contract = this.board.getContract();
		
		// 枠
		ctx.fillStyle = 'rgb(0,32,0)'; // back color
		ctx.fillRect(18,32,172,72);
		ctx.fillStyle = 'rgb(0,160,0)'; // field color
		ctx.fillRect(10,24,172,72);
		
		// コントラクトがあれば、左上に表示する
		if (contract != null) {
			ctx.font = 'bold 16px Serif';
			var kind = contract.kind;
			var contractStr;
			if (kind == Bid.PASS) {
				contractStr = "Pass Out";
			} else {
				contractStr = "Contract " + contract.level;
				var i = contract.suit-1;
				contractStr += " C D H SNT".substring(i*2, i*2+2);
				contractStr += ["", "p.o.", "X", "XX"][kind];
				contractStr += " by ";
				contractStr += DIRECTIONS[this.board.getDeclarer()];
			}
			this._fillTextWithShade_(ctx, contractStr, 18, 83);
			
		} else {
			// ディーラー
			var dealerStr = "Dlr";
			dealerStr += DIRECTIONS[this.board.getDealer()];
			this._fillTextWithShade_(ctx, dealerStr, 18, 83);
		}
		// バル
		var vulStr = ["Vul: Neither", "Vul: N-S", "Vul: E-W", "Vul: Both"];
		this._fillTextWithShade_(ctx, vulStr[this.board.vul], 18, 65);
		
		// タイトル
		ctx.font = 'italic bold 14px Serif';
		this._fillTextWithShade_(ctx, this.board.name, 18, 44);
		
		// 線
		ctx.beginPath();
		ctx.moveTo(18, 48);
		ctx.lineTo(170, 48);
		ctx.closePath();
		ctx.stroke();
	}
	
	/**
	 * 影付きの文字を描画
	 * @private
	 * @param	{Context} ctx Canvas のグラフィックコンテキスト
	 * @param	{string} str 描画対象の文字列
	 * @param	{number} x x座標
	 * @param	{number} y y座標
	 */
	TableGui.prototype._fillTextWithShade_ = function(ctx, str, x, y) {
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
	TableGui.prototype._drawDirection_ = function(ctx) {
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
		var vul = this.board.vul;
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
		ctx.font = 'normal 28px, SanSerif';
		ctx.drawString("N", 314, 178);
		ctx.drawString("E", 384, 250);
		ctx.drawString("S", 314, 326);
		ctx.drawString("W", 238, 250);
	}
	
/*-----------
 * overrides
 */
	/**
	 * @override
	 */
	TableGui.prototype.draw = function(ctx) {
		this._drawContract_(ctx);
		this._drawDirection_(ctx);
	}
	
/**
 * WinnerCard class 定義
 * @constructor
 * @extends Entity
 */
var WinnerCard = function(win) {
	Entity.call(this);
	this.win = win;
	
	this.setSize(WinnerGui.SHORTER_EDGE, WinnerGui.LONGER_EDGE);
	if (win) {
		this.direction = Entity.UPRIGHT;
	} else {
		this.direction = Entity.RIGHT_VIEW;
	}
}
inherits(WinnerCard, Entity);

/*------------------
 * static constants
 */
	WinnerCard.WIN = true;
	WinnerCard.LOSE = false;
	WinnerCard.LONGER_EDGE = 48;
	WinnerCard.SHORTER_EDGE = 32;
	WinnerCard.SLIDE_STEP = 8;
	
/*-----------
 * overrides
 */
	/**
	 * @override
	 */
	WinnerCard.prototype.draw = function(ctx) {
		if (!this.isVisible) return;
		var x,y, c,s;
		var r = this.getRect();
		switch (this.direction) {
		case 1:
			c = 0; s = 1;	x = r.y;		y = -r.x - r.h; break;
		case 2:
			c = -1; s = 0;	x = -r.x - r.w;	y = -r.y - r.h; break;
		case 3:
			c = 0; s = -1;	x = -r.y - r.w;	y = r.x; break;
		default:
			c = 1; s = 0;	x = r.x;		y = r.y; break;
		}
		var img = CardImageHolder.getBackImage();
		
		ctx.setTransform(c, s, -s, c, 0, 0);
		ctx.drawImage(img, x, y, this.w, this.h);
		ctx.setTransform(1, 0, 0, 1, 0, 0);
	}

/**
 * WinnerGui class 定義
 * @constructor
 * @extends Entities
 */
var WinnerGui = function() {
	Entities.call(this);
	
	this.card = []; // WinnerCard[]
	this.count = 0;
	this.layout = null;
	this.setSize(WinnerCard.SLIDE_STEP * 12 + WinnerCard.LONGER_EDGE,
					WinnerCard.LONGER_EDGE);
}
inherits(WinnerGui, Entities);

/*------------------
 * instance methods
 */
	/**
	 * @override 
	 */
	WinnerGui.prototype.add = function(win) {
		if (typeof win != "boolean")
			throw new Error("Winner Gui の add は boolean 値のみです");
		card[this.count] = new WinnerCard(win);
		card[this.count].setPosition(this.x + this.count * WinnerCard.SLIDE_STEP,
					(win)?this.y:(this.y + WinnerCard.LONGER_EDGE / 4));
		Entity.prototype.add.call(this, card[count]);
		count++;
	}

/**
 * BoardLayout class 定義
 * @classdesc	Board のレイアウトです。
 * @constructor
 */
var BoardLayout = function() {
}
	/** ハンドの表示位置関連 */
	BoardLayout.SIDE_MARGIN = 40;
	BoardLayout.VSIDE_MARGIN = 20;
	
	/** トリックの表示位置 */
	BoardLayout.TRICK_X = (TableGui.WIDTH - Trick.WIDTH)/2;
	BoardLayout.TRICK_Y = (TableGui.HEIGHT - Trick.HEIGHT)/2;
	
	/** ウィナー表示位置 */
	BoardLayout.WINNER_X = 460;
	BoardLayout.WINNER_Y = 400;

/*------------------
 * instance methods
 */
	BoardLayout.prototype.layout = function(target) {
		if (target.bidding===void 0 || target.playHist===void 0)
			throw new Error("BoardLayout は Board 専用です:"+target);
		
		var board = target;
		var d = board.direction;
		var x = board.x;
		var y = board.y;
		
		//
		// ハンドの位置指定
		//
		
		if (board.getHand() != null) {
			for (var i = 0; i < 4; i++) {
				var ent = board.getHand()[i];
				if (ent == null) continue;
				ent.setDirection((10 - i - d )%4);
				//var lsize = ent.getSize();	// 大きさを計算させる
				
				var xx, yy;
				
				switch ( (i+d)%4 ) {
				case 0:		// 上のハンド
					xx = (board.w - ent.w) / 2;
					yy = BoardLayout.VSIDE_MARGIN;
					break;
				case 1:		// 右のハンド
					xx = board.w - ent.w - BoardLayout.SIDE_MARGIN;
					yy = (board.h - ent.h) / 2;
					break;
				case 2:		// 下のハンド
					xx = (board.w - ent.w) / 2;
					yy = board.h - ent.h - BoardLayout.VSIDE_MARGIN;
					break;
				case 3:		// 左のハンド
					xx = BoardLayout.SIDE_MARGIN;
					yy = (board.h - ent.h) / 2;
					break;
				default:
					throw new InternalError();
				}
				ent.setPosition(x + xx, y + yy);
				if (ent.layout!==void 0 && ent.layout!=null)
					ent.layout.layout(ent);
			}
		}
	}
	
	BoardLayout.prototype.layoutSize = function(target) {
		if (target.bidding===void 0 || target.playHist===void 0)
			throw new Error("BoardLayout は Board 専用です:"+target);
		return {w:target.w, h:target.h};
	}

/**
 * Board class 定義
 *
 * @classdesc ブリッジにおける１ボードをパックするオブジェクトです。
 * 		受動的なオブジェクトで、BoardManagerに対しては状態変化を起こす
 * 		メソッドを提供します。
 * 		Playerに対してBoard の情報を提供します。
 * @constructor
 * @param	{number} dealerOrNum ディーラーまたは、ボード番号
 * @param	{number} vul ディーラーを指定したときはバルを指定する。
 * @extends	Entities
 */
var Board = function(dealerOrNum, vul) {
	if (dealerOrNum===void 0) throw new Error("Board() には引数が必要です");
	
	Entities.call(this);
	
	// vul が undefined の場合、dealerOrNum は num
	if (vul===void 0) {
		vul = Board.VUL[(dealerOrNum - 1)%16];
		dealerOrNum = (dealerOrNum - 1)%4;
	}
	var dealer = dealerOrNum;
	// new Board(Board) は未実装
	
/*----------------
 * instance field
 */
	this.bidding = new BiddingHistory(dealer);
	this.playHist = new PlayHistory();
	this.vul = vul;
	this.status = Board.DEALING;
	this.name = "Bridge Board";
	/** 既知のカードで、思考ルーチンで利用することを期待 */
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
}
inherits(Board, Entities);

/*------------------
 * static constants
 */
	/**
	 * 座席定数
	 */
	Board.NORTH = PlayHistory.NORTH;
	Board.EAST = PlayHistory.EAST;
	Board.SOUTH = PlayHistory.SOUTH;
	Board.WEST = PlayHistory.WEST;
	
	Board.STATUS_STRING = [ "Dealing", "Bid", "Opening Lead", "Playing", "Scoring" ];
	Board.VUL_STRING = [ "neither", "N-S", "E-W", "both" ];
	Board.SEAT_STRING = PlayHistory.SEAT_STRING;
	
	/**
	 * vul 値として使用される定数です。
	 */
	Board.VUL_NEITHER = 0;
	Board.VUL_NS = 1;
	Board.VUL_EW = 2;
	Board.VUL_BOTH = 3;
	
	Board.VUL = [ 0,1,2,3, 1,2,3,0, 2,3,0,1, 3,0,1,2 ];
	
	Board.WIDTH = 640;
	Board.HEIGHT = 480;
	
	/**
	 * Status を示す定数です。
	 */
	// ボードが新規に作成され、まだプレイヤーにカードがディールされていない
	Board.DEALING = 0;
	// ビッドが行われている状態
	Board.BIDDING = 1;
	// オープニングリード待ち
	Board.OPENING = 2;
	// プレイ中
	Board.PLAYING = 3;
	// ボード終了
	Board.SCORING = 4;
	
	/**
	 * プレイ順番を示す定数です。
	 */
	Board.LEAD = 0;
	Board.SECOND = 1;
	Board.THIRD = 2;
	Board.FORTH = 3;

/*------------------
 * instance methods
 */
	/**
	 * GUI 要素の設定を行います。Entities として GUI 要素の add を
	 * 内部的に行います。
	 * @private
	 */
	Board.prototype._setBoardGui_ = function() {
		this.tableGui = new TableGui(this);
		this.add(this.tableGui);
		
		this.winnerGui = new WinnerGui();
		this.add(this.winnerGui);
		
		var i = 0;
		for (; i < 4; i++) {
			this.add(this.playHist.hand[i]);
		}
		
		this.trickGui = this.getTrick();
		if (this.trickGui != null) this.add(this.trickGui);
		
		this.layout = new BoardLayout();
	}
	
	/**
	 * カードを配って BIDDING 状態に移行します。
	 *
	 * @param	{Array.<Packet>} hand 初期ハンド指定(ない場合、新規カードを配る)
	 *								整数を指定したら乱数シードの意味
	 */
	Board.prototype.deal = function(handOrSeed) {
		if (this.status != Board.DEALING)
			throw new Error("deal() は DEALING 状態のみで実行可能です。");
		
		var hand;
		if (handOrSeed===void 0 || !isNaN(handOrSeed) ) {
			// hand が指定されていないときは新規カードを配る
			var seed = handOrSeed;
			hand = []; // new Packet[4];
			for (var i = 0; i < 4; i++) hand[i] = new Packet();
			
			var pile = Packet.provideDeck();
			this.openCards = new Packet();
			
			// カードを裏向きにして配る
			pile.turn(false);
			pile.shuffle(seed); // hand is void0 or number
			
			Packet.deal(pile, hand);
			for (var i = 0; i < 4; i++) hand[i].arrange();
		} else {
			// hand 指定があればそれを使う
			hand = handOrSeed;
			// カードを裏向きにしておく
			for (var i = 0; i < hand.length; i++) {
				hand[i].turn(false);
				hand[i].arrange();
			}
		}
		
		this.playHist.setHand(hand);
		
		this.status = Board.BIDDING;
		
		// GUI処理
		this._setBoardGui_();
		
		// 下に表示されるハンドを表向きにする
		var turned = this.getHand( (this.direction + 2) % 4 );
		turned.turn(true);
	}
	
	/**
	 * この Board で使用する PlayHistory を指定します。
	 *
	 * @param		{PlayHistory} playHistory 設定したい playHistory
	 */
	Board.prototype.setPlayHistory = function(playHistory) {
		this.playHist = playHistory;
	}
	
	/**
	 * この Board のコントラクトを指定します。
	 * @param	{Bid} contract コントラクト
	 * @param	{number} declarer ディクレアラーの座席定数
	 */
	Board.prototype.setContract = function(contract, declarer) {
		this.bidding.setContract(contract, declarer);
		this.playHist.setContract((this.getDeclarer() + 1)%4, this.getTrump() );
		this.status = Board.OPENING;
		
		_reorderHand_();
	}
	
	/**
	 * ビッド、プレイを行う。状態が変化する。
	 * @param	{Card} play CardオブジェクトまたはBidオブジェクトを指定
	 * @see	Bid
	 */
	Board.prototype.play = function(play) {
		if (!this.allows(play))
			throw new Error(play.toString() + "は行えません。");
		switch (this.status) {
		
		case Board.BIDDING:
			if (play.kind===void 0 || play.level===void 0 || play.suit===void 0) //! instanceof Bid
				throw new Error("ビッドしなければなりません。" + play);
			this.bidding.play(play);
			if (this.bidding.finished) {
				if (this.getContract().kind == Bid.PASS) {
					//
					// Pass out
					//
					this.status = Board.SCORING;
					break;
				}
				this.status = Board.OPENING;
				this.playHist.setContract((this.getDeclarer() + 1)%4, this.getTrump() );
				
				this._reorderHand_();
			}
			break;
			
		case Board.OPENING:
		case Board.PLAYING:
			if (play.suit===void 0 || play.value===void 0) // ! instanceof Card
				throw new Error("プレイしなければなりません。" + play);
			this.playHist.play(play);
			this.openCards.add(play);
			
			if (this.status == Board.OPENING) this.dummyOpen();
			
			if (this.playHist.isFinished()) status = Board.SCORING;
			else this.status = Board.PLAYING;
			
			// TrickAnimation 処理
			//
			// 未実装(2018/8/17)
			//
			
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
	Board.prototype.undo = function() {
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
			var lastPlay = this.playHist.undo(); // PLAYING なので、Exception はでないはず
			var turn = this.playHist.getTurn();
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
	Board.prototype._reorderHand_ = function() {
		var order; // CardOrder / stateless object
		
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
	Board.prototype._dummyOpen_ = function() {
		var dummy = this.getHand(this.getDummy());
		dummy.turn(true);
		this.openCards.add(dummy);
	}
	
	/**
	 * undo() 時に Opening Lead 状態にもどすための処理を行います。
	 * ダミーの手を裏向きに変更します。
	 */
	Board.prototype._dummyClose_ = function() {
		var dummy = this.getHand(this.getDummy());
		this.dummy.turn(false);
		this.openCards.sub(dummy);
		
		if (openCards.size() != 0) throw new InternalError("openCards 枚数に矛盾があります");
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
	Board.prototype.allows = function(play) {
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
	Board.prototype.getPlayOrder = function() {
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
	Board.prototype.getTurn = function() {
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
	Board.prototype.getPlayer = function() {
		var seat = this.getTurn();
		
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
	Board.prototype.getContract = function() {
		return this.bidding.contract;
	}
	
	/**
	 * (現在までの)最終トランプを取得します。
	 * ビッドがまだ行われていない場合、-1 が返ります。
	 * @return	{number}	トランプスーツ
	 */
	Board.prototype.getTrump = function() {
		var contract = this.getContract();
		if (contract == null) return -1;
		return contract.suit;
	}
	
	/**
	 * (現在までで決定している)ディクレアラーの席番号を取得する。
	 * ビッドがまだ行われていない場合、-1 が返る。
	 * @return	{number}	ディクレアラーの座席番号
	 */
	Board.prototype.getDeclarer = function() {
		return this.bidding.declarer;
	}
	
	/**
	 * (現在までで決定している)ダミーの席番号を取得する。
	 * ビッドがまだ行われていない場合、-1 が返る。
	 * @return	{number} ダミーの座席番号
	 */
	Board.prototype.getDummy = function() {
		var dec = this.bidding.declarer;
		if (dec == -1) return -1;
		return (dec + 2) % 4;
	}
	
	/**
	 * (ビッドを開始する)ディーラーの席番号を取得する。
	 * ビッドがまだ行われていない場合、-1 が返る。
	 * @return	{number}	ディーラーの座席番号
	 */
	Board.prototype.getDealer = function() {
		return this.bidding.dealer;
	}
	
	/**
	 * ハンドの情報を取得する。UnspecifiedCardが含まれることもある。
	 * @param	{number} seat	座席番号。無指定の場合 Packet[] が返る。
	 * @return	{Packet} ハンド。seat を指定しない場合 Packet[]
	 */
	Board.prototype.getHand = function(seat) {
		if (seat!==void 0) {
			return this.playHist.hand[seat];
		}
		return this.playHist.hand;
	}
	
	/**
	 * 現在までにプレイされたトリック数を取得する。
	 * @return	{number}	プレイされたトリック数
	 */
	Board.prototype.getTricks = function() {
		return this.playHist.getTricks();
	}
	
	/**
	 * 現在場に出ているトリックを取得する。
	 * @return	{Trick} 現在のトリック
	 */
	Board.prototype.getTrick = function() {
		return this.playHist.getTrick();
	}
	
	/**
	 * プレイされた過去のトリックすべてを取得します。
	 * 本処理は、PlayHistory.getAllTricks() へ委譲しています。
	 * @return	{Array.<Trick>} 全トリック
	 * @see		PlayHistory#getAllTricks()
	 */
	Board.prototype.getAllTricks = function() {
		return this.playHist.getAllTricks();
	}
	
	/**
	 * 指定された座席がバルであるか判定します。
	 * @param	{number}	seat	座席番号
	 * @return	{number} VUL を示す定数
	 */
	Board.prototype.isVul = function(seat) {
		var mask = 0;
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
	Board.prototype.toString = function() {
		var result = "---------- Board Information ----------";
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
	Board.prototype.reset = function(numOrDealer, vul) {
		var dealer;
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
	 * スコア計算は未実装です。
	 * @return	{string} このボードのテキスト表現
	 */
	Board.prototype.toText = function() {
		var s;
		var nl = "\n";
		
		s = s + this.name+ nl
				+ "----- コントラクト -----" + nl
				+ "contract：" + this.getContract().toString()
				+ " by " + Board.SEAT_STRING[this.getDeclarer()] + nl
				+ "vul     ：" + Board.VUL_STRING[this.getVulnerability()]
				+ nl + nl
				+ "----- オリジナルハンド -----" + nl;
		
		var hands = Board.calculateOriginalHand(this);
		
		// NORTH
		for (var suit = 4; suit >= 1; suit--) {
			s = s + "               "
					+ this._getHandString_(hands, 0, suit)+nl;
		}
		
		// WEST, EAST
		for (var suit = 4; suit >= 1; suit--) {
			var wstr = this._getHandString_(hands, 3, suit) + "               ";
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
		for (var suit = 4; suit >= 1; suit--) {
			s = s + "               ";
						+ getHandString(hands, 2, suit) + nl;
		}
		
		// ビッド経過
//		s.append(bidding.toString());
//		s.append(nl);
		
		// プレイライン
		s = s + nl
				+"----- プレイ -----" + nl
		
				+"    1  2  3  4  5  6  7  8  9 10 11 12 13" + nl;
		
		var trick = this.getAllTricks();
		var nesw = [];
		for (var i = 0; i < 4; i++) {
			nesw[i] = "";
			nesw[i] = nesw[i] + "NESW".substring(i,i+1) + " ";
			if (this.getTricks() > 0) {
				var leaderSeat = trick[0].leader;
				if (i == leaderSeat) nesw[i] = nesw[i] + "-";
				else nesw[i] = nesw[i] + " ";
			}
		}
		
		for (var i = 0; i < this.getTricks(); i++) {
			var leaderSeat = trick[i].leader;
			var winnerSeat = trick[i].winner;
			for (var j = 0; j < 4; j++) {
				var seat = (j + leaderSeat) % 4;
				nesw[seat] = nesw[seat] + trick[i].children[j].toString().substring(1);
				if (seat == winnerSeat) nesw[seat] = nesw[seat] + '+';
				else nesw[seat] = nesw[seat] + " ";
			}
		}
		for (var i = 0; i < 4; i++) {
			s = s + nesw[i] + nl;
		}
		if (this.status == Board.SCORING) {
			s = s + nl+ "----- 結果 -----"+nl;
			// メイク数
			var win		= this._countWinners_();
			var up		= win - this.getContract().level - 6;
			var make	= win - 6;
			
			if (up >= 0) {
				// メイク
				s = s + make + "メイク  ";
			} else {
				// ダウン
				s = s + (-up) + "ダウン  ";
			}
			
			s = s + "("+win+"トリック)\nN-S側のスコア："; //+Score.calculate(this, SOUTH));
			s = s + nl;
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
	Board.prototype._getHandString_ = function(hand, seat, suit) {
		var s = "CDHS".substring(suit-1, suit)+":";
		var oneSuit = hand[seat].subpacket(suit);
		oneSuit.arrange();
		var size = oneSuit.children.length;
		for (var i = 0; i < size; i++) {
			var c = oneSuit.children[i];
			if ((c.isHead == true)||(openCards.indexOf(c)>=0)) {
				s = s + c.toString().substring(2);
			}
		}
		return s;
	}
	
	/**
	 * @private
	 * @return	{number}	ウィナーの数
	 */
	Board.prototype._countWinners_ = function() {
		var tr = this.getAllTricks(); // Trick[]
		if (tr == null) return 0;
		
		var win = 0;
		var declarer = this.getDeclarer();
		
		// winner を数える(Board にあったほうが便利)
		for (var i = 0; i < tr.length; i++) {
			var winner = tr[i].winner;
			if ( ((winner ^ declarer) & 1) == 0 ) win++;
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
	Board.calculateOriginalHand = function(board) {
		var result = []; //Packet[4];
		for (var i = 0; i < 4; i++) result[i] = new Packet();
		
		// 今もっているハンドをコピー
		var original = board.getHand();
		for (var i = 0; i < original.length; i++) {
			for (var j = 0; j < original[i].children.length; j++) {
				result[i].add(original[i].children[j]);
			}
		}
		
		// プレイされたハンドをコピー
		var trick = board.getAllTricks();
		for (var i = 0; i < trick.length; i++) {
			var tr = trick[i];
			if (tr == null) break;
			var seat = tr.leader;
			for (var j = 0; j < tr.children.length; j++) {
				result[seat].add(tr.children[j]);
				seat++;
				seat = (seat % 4);
			}
		}
		
		// 並べ替え
		for (var i = 0; i < 4; i++) {
			result[i].arrange();
		}
		return result;
	}

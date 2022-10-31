console.log('Yus test2');

describe("ReproducibleRandom Test", () => {
	it("ReproducibleRandom new", () => {
		expect(new ReproducibleRandom()).toBeDefined();
	});
	it("ReproducibleRandom value", () => {
		expect(new ReproducibleRandom(1).next()).toEqual(-638953871);
	});
});

describe("NaturalCardOrder Test", () => {
	it("NaturalCardOrder static constant", () => {
		expect(NaturalCardOrder.SUIT_ORDER_SPADE).toBeDefined();
	});
});

describe("Entity Test", () => {
	it("Entity static constant", () => {
		expect(Entity.UPRIGHT).toBeDefined();
	});
	it("Entity new", () => {
		expect(new Entity()).toBeDefined();
	});
});

describe("Entities Test", () => {
	it("Entities new", () => {
		expect(new Entities()).toBeDefined();
	});
	it("Entities instance variable", () => {
		expect(new Entities().children).toBeDefined();
	});
});

describe("Packet Test", () => {
	it("Packet new", () => {
		expect(new Packet()).toBeDefined();
	});
	it("Packet provideDeck()", () => {
		const p = Packet.provideDeck();
		expect(p.children.length).toBe(52);
	});
	it("Packet countSuit()", () => {
		const p = Packet.provideDeck();
		expect(p.countSuit(Card.DIAMOND)).toBe(13);
	});
	it("Packet countValue()", () => {
		expect(Packet.provideDeck().countValue(Card.ACE)).toBe(4);
	});
	it("Packet subpacket()", () => {
		expect(Packet.provideDeck().subpacket(Card.CLUB).children.length).toBe(13);
	});
});

describe("Card Test", () => {
	it("Card static constant", () => {
		expect(Card.ACE).toBe(1);
	});
	it("Card new", () => {
		expect(new Card(Card.SPADE, Card.QUEEN)).toBeDefined();
	});
	it("Card variables", () => {
		const card = new Card(Card.HEART, Card.KING);
		expect(card.suit).toBe(Card.HEART);
		expect(card.value).toBe(Card.KING);
	});
	it("Card.toString()", () => {
		const card = new Card(Card.HEART, Card.KING);
		expect(card.toString()).toEqual("/HK");
		card.isHead = false;
		expect(card.toString()).toEqual("_HK");
	});
});

describe("CardImageHolder Test", () => {
	it("CardImageHolder images", () => {
		expect(CardImageHolder.IMAGE.length).toBe(52);
	});
	it("CardImageHolder back images", () => {
		expect(CardImageHolder.BACK_IMAGE.length).toBe(4);
	});
});

describe("CardHandLayout Test", () => {
	it("CardHandLayout new", () => {
		expect(new CardHandLayout()).toBeDefined();
	});
	it("CardHandLayout.layout()", () => {
		const chl = new CardHandLayout();
		const hand = Packet.provideDeck();
		hand.shuffle(2);
		chl.layout(hand);
		expect(chl.layoutSize(hand)).toEqual({w:977, h:87});
		hand.setDirection(Entity.RIGHT_VIEW);
		expect(chl.layoutSize(hand)).toEqual({w:87, h:977});
	});

});

describe("Bid Test", () => {
	it("Bid new", () => {
		expect(new Bid(Bid.BID, 7, Bid.NO_TRUMP)).toBeDefined();
	});
	const bid = new Bid(Bid.BID, 1, Bid.NO_TRUMP);
	it("Bid variables", () => {
		expect(bid.kind).toBe(Bid.BID);
		expect(bid.level).toBe(1);
		expect(bid.suit).toBe(Bid.NO_TRUMP);
	});
	const pass = new Bid(Bid.PASS);
	const twoSpades = new Bid(Bid.BID, 2, Bid.SPADE);
	it("Bid.isBiddableOver", () => {
		expect(bid.isBiddableOver(twoSpades)).toBeFalse();
		expect(twoSpades.isBiddableOver(bid)).toBeTrue();
		expect(pass.isBiddableOver(twoSpades)).toBeTrue();
	});

	it("Bid.toString()", () => {
		expect(bid.toString()).toEqual("[1 NT  ]");
		expect(pass.toString()).toEqual("[pass  ]");
		expect(twoSpades.toString()).toEqual("[2  S  ]");
	});
});

describe("DummyHandLayout Test", () => {
	it("DummyHandLayout new", () => {
		expect(new DummyHandLayout()).toBeDefined();
	});
	it("DummyHandLayout.layout()", () => {
		const chl = new DummyHandLayout();
		const hand = Packet.provideDeck();
		hand.shuffle(2);
		chl.layout(hand);
		expect(chl.layoutSize(hand)).toEqual({w:248, h:303});
		hand.setDirection(Entity.RIGHT_VIEW);
		expect(chl.layoutSize(hand)).toEqual({w:303, h:248});
	});
});

describe("BiddingHistory Test", () => {
	it("BiddingHistory static constants", () => {
		expect(BiddingHistory.SOUTH).toBeDefined();
	});
	it("BiddingHistory new", () => {
		expect(new BiddingHistory(BiddingHistory.NORTH)).toBeDefined();
	});
	it("BiddingHistory.allows(Bid)", () => {
		const bh = new BiddingHistory(BiddingHistory.SOUTH);
		const pass = new Bid(Bid.PASS);
		expect(bh.allows(pass)).toBeTrue();
	});
	it("BiddingHistory bidding sequence 1", () => {
		const bh = new BiddingHistory(BiddingHistory.SOUTH);
		const pass = new Bid(Bid.PASS);
		expect(bh.allows(pass)).toBeTrue();
		bh.play(pass); // p
		const oneSpades = new Bid(Bid.BID, 1, Bid.SPADE);
		expect(bh.allows(oneSpades)).toBeTrue();
		bh.play(oneSpades); // p 1S
		const xOneSpades = new Bid(Bid.DOUBLE, 1, Bid.SPADE);
		expect(bh.allows(xOneSpades)).toBeTrue();
		bh.play(xOneSpades); // p 1S X
		expect(bh.allows(oneSpades)).toBeFalse();
		expect(bh.allows(xOneSpades)).toBeFalse();
		bh.play(pass); // p 1S X p
		const xxOneSpades = new Bid(Bid.REDOUBLE, 1, Bid.SPADE);
		expect(bh.allows(xxOneSpades)).toBeFalse();
		bh.play(pass); // p 1S X p   p
		expect(bh.allows(xxOneSpades)).toBeTrue();
		bh.play(xxOneSpades); // p 1S X p   p XX
		bh.play(pass); // p 1S X p   p XX p
		expect(bh.getTurn()).toEqual(BiddingHistory.EAST);
		bh.play(pass); // p 1S X p   p XX p p
		expect(bh.finished).toBeFalse();
		bh.play(pass); // p 1S X p   p XX p p   p
		expect(bh.declarer).toEqual(BiddingHistory.WEST);
		expect(bh.finished).toBeTrue();
		expect(bh.toString()).toEqual(	"   N       E       S       W\n"+
										"                [pass  ][1  S  ]\n"+
										"[1  SX ][pass  ][pass  ][1  SXX]\n"+
										"[pass  ][pass  ][pass  ]\n");
	});

});

describe("TrickLayout Test", () => {
	it("TrickLayout new", () => {
		expect(new TrickLayout()).toBeDefined();
	});
});

describe("Trick Test", () => {
	it("Trick new", () => {
		expect(new Trick(BiddingHistory.NORTH, Bid.NO_TRUMP)).toBeDefined();
	});
	it("Trick add", () => {
		const t = new Trick(BiddingHistory.NORTH, Bid.NO_TRUMP);
		t.add(new Card(Card.SPADE, 10));
		t.add(new Card(Card.SPADE, 5));
		t.add(new Card(Card.HEART, Card.KING));
		expect(t.isFinished()).toBeFalse();
		t.add(new Card(Card.SPADE, Card.JACK));
		expect(t.isFinished()).toBeTrue();
		expect(t.winnerCard.suit).toEqual(Card.SPADE);
		expect(t.winnerCard.value).toEqual(Card.JACK);
		expect(t.winner).toEqual(BiddingHistory.WEST);
	});
});

describe("PlayHistory Test", () => {
	it("PlayHistory static constants", () => {
		expect(PlayHistory.NORTH).toBeDefined();
		expect(PlayHistory.SEAT_STRING.length).toBe(4);
	});
	it("PlayHistory static new", () => {
		expect(new PlayHistory()).toBeDefined();
	});
	it("PlayHistory sequence 1", () => {
		// ハンドを配る
		const p = new PlayHistory();
		const deck = Packet.provideDeck();
		const hands = [];
		hands.push(new Packet());
		hands.push(new Packet());
		hands.push(new Packet());
		hands.push(new Packet());
		deck.shuffle(3);
		Packet.deal(deck, hands, 0);
		hands.forEach(hand => { hand.arrange(); });
		p.setHand(hands);
		p.setContract(PlayHistory.NORTH, Bid.HEART);
		//console.log(p.toString());
		expect(p.isFinished()).toBeFalse();

		// North から 1 枚ずつプレイする
		for (let t = 0; t < 13; t++) {
			for (let i = 0; i < 4; i++) {
				const player = p.getTurn();
				for (let j = 0; j < hands[player].children.length; j++) {
					if (p.allows(hands[player].children[j])) {
						p.play(hands[player].children[j]);
						break;
					}
				}
			}
		}
		//console.log(p.toString());
		expect(p.isFinished()).toBeTrue();
	});
});


describe("TableGui Test", () => {
	it("TableGui new", () => {
		expect(new TableGui()).toBeDefined();
	});
});

describe("WinnerCard Test", () => {
	it("WinnerCard new", () => {
		expect(new WinnerCard()).toBeDefined();
	});
});

describe("WinnerGui Test", () => {
	it("WinnerGui new", () => {
		expect(new WinnerGui()).toBeDefined();
	});
});

describe("BoardLayout Test", () => {
	it("BoardLayout static constants", () => {
		expect(BoardLayout.SIDE_MARGIN).toBeDefined();
	});
	it("BoardLayout new", () => {
		expect(new BoardLayout()).toBeDefined();
	});
});

/**
 * テスト用のボードを生成します。以降の sequence はこのボードに対して実行することを
 * 想定しています。
 * @returns		{Board}	テスト用のボード 
 */
const createTestBoard = () => {
	return new Board(1);
};

/**
 * テスト用のdealを行います。後続のビッド、プレイはこのディールでなければなりません。
 * @param {Board} b deal を行う Board
 */
const dealSequence = (b) => {
	b.deal(11);
};

/**
 * ビッドを行います。1NT open から staymann を経て
 * 2NT double by West のコントラクトとなります。
 * 
 * @param {Board} b ビッドを行う Board
 */
const bidSequence = (b) => {
	b.play(new Bid(Bid.PASS));
	b.play(new Bid(Bid.PASS));
	b.play(new Bid(Bid.PASS));
	b.play(new Bid(Bid.BID, 1, Bid.NO_TRUMP)); // balanced 15-17
	b.play(new Bid(Bid.PASS));
	b.play(new Bid(Bid.BID, 2, Bid.CLUB)); // stayman
	b.play(new Bid(Bid.PASS));
	b.play(new Bid(Bid.BID, 2, Bid.HEART)); // heart 4+
	b.play(new Bid(Bid.PASS));
	b.play(new Bid(Bid.BID, 2, Bid.NO_TRUMP)); // heart 3-
	b.play(new Bid(Bid.PASS));
	b.play(new Bid(Bid.PASS)); // minimum and no 4 spades
	b.play(new Bid(Bid.PASS));
};

/**
 * テスト用のオープニングリードを行います。
 * @param {Board}} b オープニングリードを行う Board
 */
const openingSequence = (b) => {
	b.play(new Card(Card.SPADE, 7)); // N
};

/**
 * テスト用のプレイを行います。
 * @param {Board} b プレイを行う Board
 */
const playSequence = (b) => {
	b.play(new Card(Card.SPADE, 2)); // E
	b.play(new Card(Card.SPADE, Card.KING)); // S win
	b.play(new Card(Card.SPADE, 4)); // W
	// trick 2
	b.play(new Card(Card.SPADE, 10)); // S
	b.play(new Card(Card.SPADE, Card.JACK)); // W
	b.play(new Card(Card.SPADE, Card.QUEEN)); // N win
	b.play(new Card(Card.SPADE, 5)); // E duck
	// trick 3
	b.play(new Card(Card.SPADE, 9)); // N
	b.play(new Card(Card.SPADE, Card.ACE)); // E win
	b.play(new Card(Card.SPADE, 6)); // S
	b.play(new Card(Card.HEART, 6)); // W
	// trick 4
	b.play(new Card(Card.DIAMOND, 2)); // E
	b.play(new Card(Card.DIAMOND, 5)); // S
	b.play(new Card(Card.DIAMOND, 10)); // W
	b.play(new Card(Card.DIAMOND, Card.QUEEN)); // N win
	// trick 5
	b.play(new Card(Card.SPADE, 8)); // N win
	b.play(new Card(Card.SPADE, 3)); // E
	b.play(new Card(Card.HEART, 5)); // S com-on
	b.play(new Card(Card.CLUB, 4)); // W
	// trick 6
	b.play(new Card(Card.HEART, 3)); // N
	b.play(new Card(Card.HEART, 4)); // E
	b.play(new Card(Card.HEART, Card.QUEEN)); // S win
	b.play(new Card(Card.HEART, 8)); // W
	// trick 7
	b.play(new Card(Card.CLUB, Card.JACK)); // S
	b.play(new Card(Card.CLUB, Card.QUEEN)); // W finesse
	b.play(new Card(Card.CLUB, Card.KING)); // N win
	b.play(new Card(Card.CLUB, 3)); // E
	// trick 8
	b.play(new Card(Card.CLUB, 6)); // N
	b.play(new Card(Card.CLUB, 9)); // E
	b.play(new Card(Card.CLUB, 10)); // S
	b.play(new Card(Card.CLUB, Card.ACE)); // W win
	// trick 9
	b.play(new Card(Card.DIAMOND, Card.ACE)); // W win
	b.play(new Card(Card.DIAMOND, 6)); // N
	b.play(new Card(Card.DIAMOND, 7)); // E
	b.play(new Card(Card.DIAMOND, Card.JACK)); // S
	// trick 10
	b.play(new Card(Card.DIAMOND, Card.KING)); // W win
	b.play(new Card(Card.DIAMOND, 9)); // N
	b.play(new Card(Card.DIAMOND, 4)); // E
	b.play(new Card(Card.HEART, 2)); // S
	// trick 11
	b.play(new Card(Card.DIAMOND, 8)); // W win
	b.play(new Card(Card.CLUB, 2)); // N
	b.play(new Card(Card.DIAMOND, 3)); // E
	b.play(new Card(Card.HEART, 9)); // S
	// trick 12
	b.play(new Card(Card.HEART, Card.JACK)); // W
	b.play(new Card(Card.CLUB, 8)); // N
	b.play(new Card(Card.HEART, Card.ACE)); // E win
	b.play(new Card(Card.HEART, Card.KING)); // S
	// trick 13
	b.play(new Card(Card.HEART, 7)); // E
	b.play(new Card(Card.CLUB, 5)); // S
	b.play(new Card(Card.HEART, 10)); // W
	b.play(new Card(Card.CLUB, 7)); // N
};

describe("Board Test", () => {
	it("Board static constants", () => {
		expect(Board.LEAD).toBeDefined();
	});
	it("Board new", () => {
		expect(new Board(1)).toBeDefined();
	});
	it("Board new with dealer and vul", () => {
		for (let seat = 0; seat < 4; seat ++) {
			for (let vul = 0; vul < 4; vul++) {
				const b = new Board(seat, vul);
				expect(b).toBeDefined();
				expect(b.getDealer()).toBe(seat);
				expect(b.vul).toBe(vul);
			}
		}
	});
	it("Board sequence new board", () => {
		const b = createTestBoard(); // dealer North, none vul
		expect(b.getDealer()).toBe(Board.NORTH);
		expect(b.vul).toBe(Board.VUL_NEITHER);
		expect(b.status).toBe(Board.DEALING);
	});
	it("Board sequence deal", () => {
		const b = createTestBoard();
		// ハンド設定
		dealSequence(b);
		expect(b.status).toBe(Board.BIDDING);
		expect(b.getContract()).toBeNull();
	});
	it("Board sequence bid", () => {
		const b = createTestBoard();
		dealSequence(b);
		// bid
		bidSequence(b);
		expect(b.status).toBe(Board.OPENING);
		expect(b.getContract()).toBeDefined();
		expect(b.getContract().level).toBe(2);
		expect(b.getContract().suit).toBe(Bid.NO_TRUMP);
		expect(b.getDeclarer()).toBe(Board.WEST);
		expect(b.getDealer()).toBe(Board.NORTH);
	});
	it("Board sequence opening", () => {
		const b = createTestBoard();
		dealSequence(b);
		bidSequence(b);
		// opening lead
		openingSequence(b);
		expect(b.status).toBe(Board.PLAYING);
	});
	it("Board sequence play", () => {
		const b = createTestBoard();
		dealSequence(b);
		bidSequence(b);
		openingSequence(b);
		playSequence(b);
		expect(b.status).toBe(Board.SCORING);
	});
	it("Board sequence scoring", () => {
		const b = createTestBoard();
		dealSequence(b);
		bidSequence(b);
		openingSequence(b);
		playSequence(b);

		//console.log(b.toString());
		//console.log(b.toText());
		expect(Score.calculate(b, Board.SOUTH)).toBe(50);

	});
});

describe("BridgeUtils test", () => {
	it("BridgeUtils constant", () => {
		expect(BridgeUtils.VALUE_STRING).toBeDefined();
	});
	it("BridgeUtils countHonerPoint()", () => {
		const b = createTestBoard();
		dealSequence(b);
		const hand = b.getHand(Board.WEST);
		expect(BridgeUtils.countHonerPoint(hand, Card.SPADE)).toBe(1);
		expect(BridgeUtils.countHonerPoint(hand, Card.HEART)).toBe(1);
		expect(BridgeUtils.countHonerPoint(hand, Card.DIAMOND)).toBe(7);
		expect(BridgeUtils.countHonerPoint(hand, Card.CLUB)).toBe(6);

	});
	it("BridgeUtils countHonerPoint() without suit", () => {
		const b = createTestBoard();
		dealSequence(b);
		const hand = b.getHand(Board.NORTH);
		//console.log(hand.toString());
		expect(BridgeUtils.countHonerPoint(hand)).toEqual([7, 3, 2, 0, 2]);
	});
	it("BridgeUtils countHoners()", () => {
		const b = createTestBoard();
		dealSequence(b);
		const hand = b.getHand(Board.WEST);
		//console.log(hand.toString());
		expect(BridgeUtils.countHoners(hand, Card.SPADE)).toBe(1);
		expect(BridgeUtils.countHoners(hand, Card.HEART)).toBe(2);
		expect(BridgeUtils.countHoners(hand, Card.DIAMOND)).toBe(3);
		expect(BridgeUtils.countHoners(hand, Card.CLUB)).toBe(2);
	});
	it("BridgeUtils countHoners()", () => {
		const b = createTestBoard();
		dealSequence(b);
		const hand = b.getHand(Board.WEST);
		//console.log(hand.toString());
		expect(BridgeUtils.valuePattern(hand, Card.SPADE)).toEqual("J4");
		expect(BridgeUtils.valuePattern(hand, Card.HEART)).toEqual("JT86");
		expect(BridgeUtils.valuePattern(hand, Card.DIAMOND)).toEqual("AKT8");
		expect(BridgeUtils.valuePattern(hand, Card.CLUB)).toEqual("AQ4");
	});
	it("BridgeUtils patternMatch()", () => {
		const b = createTestBoard();
		dealSequence(b);
		const hand = b.getHand(Board.WEST);
		//console.log(hand.toString());
		expect(BridgeUtils.patternMatch(hand, "J*", Card.SPADE)).toBe(true);
		expect(BridgeUtils.patternMatch(hand, "A*", Card.SPADE)).toBe(false);
		expect(BridgeUtils.patternMatch(hand, "JT", Card.SPADE)).toBe(false);
		expect(BridgeUtils.patternMatch(hand, "JT*", Card.HEART)).toBe(true);
		expect(BridgeUtils.patternMatch(hand, "AKT?", Card.DIAMOND)).toBe(true);
		expect(BridgeUtils.patternMatch(hand, "AKT??", Card.DIAMOND)).toBe(false);
		expect(BridgeUtils.patternMatch(hand, "AQ4", Card.CLUB)).toBe(true);
		expect(BridgeUtils.patternMatch(hand, "???", Card.CLUB)).toBe(true);
		expect(BridgeUtils.patternMatch(hand, "??", Card.CLUB)).toBe(false);
	});
	it("BridgeUtils calculateOriginalHand()", () => {
		const b = createTestBoard();
		dealSequence(b);
		const org = [];
		for (let i = 0; i < 4; i++) {
			b.getHand(i).turn(true);
			org.push(b.getHand(i).toString());
		}
		bidSequence(b);
		openingSequence(b);
		playSequence(b);
		const calculated = BridgeUtils.calculateOriginalHand(b);
		const calcstr = [];
		calculated.forEach(h => calcstr.push(h.toString()));
		expect(calcstr).toEqual(org);
	});
});

describe("BoardStatistics Test", () =>{
	it("BoardStatistics new", () => {
	  expect(new BoardStatistics()).toBeDefined();
	});
  });
  
  describe("OptimizedBoard Test", () =>{
	it("OptimizedBoard new", () => {
	  const b = createTestBoard();
	  dealSequence(b);
	  bidSequence(b);
	  openingSequence(b);
	  const ob = new OptimizedBoard(b);
	  expect(ob).toBeDefined();
	  //console.log(ob.toString());

	  ob.calcPropData();

	});
  });
  
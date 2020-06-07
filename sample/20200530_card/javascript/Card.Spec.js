console.log('Yus test2');

describe("ReproducibleRandom Test", () => {
	it("ReproducibleRandom new", () => {
		expect(new ReproducibleRandom()).toBeDefined();
	});
	it("ReproducibleRandom value", () => {
		expect(new ReproducibleRandom(1).next()).toEqual(-638953871);
	})
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
		expect(chl.layoutSize(hand)).toEqual({w:773, h:87});
		hand.setDirection(Entity.RIGHT_VIEW);
		expect(chl.layoutSize(hand)).toEqual({w:87, h:773});
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
		expect(chl.layoutSize(hand)).toEqual({w:248, h:279});
		hand.setDirection(Entity.RIGHT_VIEW);
		expect(chl.layoutSize(hand)).toEqual({w:279, h:248});
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
	it("Board sequence", () => {
		const b = new Board(1); // dealer North, none vul

		// ハンド設定
		b.deal(11);

		// bid
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

		// opening lead
//		console.log(b.getHand(b.getTurn()).toString());
		b.play(new Card(Card.SPADE, 7));

		console.log(b.toString());

	});
});

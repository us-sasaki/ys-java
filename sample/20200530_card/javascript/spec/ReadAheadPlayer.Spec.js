
describe("SimplePlayer2 Test", () => {
	it("can instantiate", () => {
        const b = createTestBoard();

		expect(new SimplePlayer2(b, Board.SOUTH)).toBeDefined();
    });
	it("play on hand(1,11)", helperAsync(async () => {
        const b = new Board(1);
        const player = [new SimplePlayer2(b, Board.NORTH),
                        new SimplePlayer2(b, Board.EAST),
                        new SimplePlayer2(b, Board.SOUTH),
                        new SimplePlayer2(b, Board.WEST)];
        b.deal(11);
        b.setContract(new Bid(Bid.BID, 2, Bid.NO_TRUMP), Board.WEST);

        while (b.status !== Board.SCORING)
            b.play(await player[b.getTurn()].draw());
    }));
	it("play on sequencial random hands", helperAsync(async () => {
        lp:
        for (let boardNum = 2; boardNum < 50; boardNum++) {
            let handNo = boardNum + 10;
            const b = new Board(boardNum);
            const player = [new SimplePlayer2(b, Board.NORTH),
                            new SimplePlayer2(b, Board.EAST),
                            new SimplePlayer2(b, Board.SOUTH),
                            new SimplePlayer2(b, Board.WEST)];
            b.deal(handNo);
            const level = boardNum%6+2;
            const denom = boardNum%5+1;
            const declarer = (boardNum+Math.floor(boardNum/4))%4;
            b.setContract(new Bid(Bid.BID, level, denom), declarer);

            while (b.status !== Board.SCORING) {
                const p = await player[b.getTurn()].draw();
                b.play(p);
            }
        }
    }));

	it("play on random hands", helperAsync(async () => {
        ReproducibleRandom.setSeed(12345);

        lp:
        for (let i = 0; i < 30; i++) {
            const boardNum = ReproducibleRandom.nextInt(32)+1;
            let handNo = ReproducibleRandom.nextInt(1, 99999999);
            const b = new Board(boardNum);
            const player = [new SimplePlayer2(b, Board.NORTH),
                            new SimplePlayer2(b, Board.EAST),
                            new SimplePlayer2(b, Board.SOUTH),
                            new SimplePlayer2(b, Board.WEST)];
            b.deal(handNo);
            const level = ReproducibleRandom.nextInt(7)+1;
            const denom = ReproducibleRandom.nextInt(5)+1;
            const declarer = ReproducibleRandom.nextInt(4);
            b.setContract(new Bid(Bid.BID, level, denom), declarer);
            //console.log(b.getContract().toString()+" by "+Board.SEAT_STRING[declarer]);

            while (b.status !== Board.SCORING) {
                const p = await player[b.getTurn()].draw();
                b.play(p);
            }
        }
    }));
    
});

describe("OptimizedBoard Test", () => {
    let b, ob;
    beforeEach( () => {
        b = new Board(1);
        b.deal(1);
        b.setContract(new Bid(Bid.BID, 1, Bid.NO_TRUMP), Board.NORTH);
        ob = new OptimizedBoard(b);
    });
    it("can instantiate", () => {
        expect(ob).toBeDefined();
    });

    it("card size is 56", () => {
        expect(ob.card.length).toBe(56);
    });
    it("card value of index 13,27,41,55 is always 15", () => {
        [13,27,41,55].forEach( i => expect(ob.card[i]).toBe(15));
    });
    it ("card value is seat number", () => {
        const hs = b.getHand();
        for (let seat = 0; seat < 4; seat++) {
            for (card of hs[seat].children) {
                expect(ob.card[OptimizedBoard.getCardNumber(card)]).toBe(seat);
            }
        }
        //console.log(JSON.stringify(ob.card));

    })
});

describe("ReadAheadPlayer Test", () => {
	it("can instantiate", () => {
        const b = createTestBoard();

		expect(new ReadAheadPlayer(b, Board.SOUTH)).toBeDefined();
    });
	it("play on hand(1,11)", helperAsync(async () => {
        const b = new Board(1);
        ReadAheadPlayer.DEPTH = [5,5,5,5,5,5,5,5,5,5,5,5,5,5];
        const player = [new ReadAheadPlayer(b, Board.NORTH),
                        new ReadAheadPlayer(b, Board.EAST),
                        new ReadAheadPlayer(b, Board.SOUTH),
                        new ReadAheadPlayer(b, Board.WEST)];
        b.deal(11);
        b.setContract(new Bid(Bid.BID, 1, Bid.CLUB), Board.SOUTH);

        while (b.status !== Board.SCORING)
            b.play(await player[b.getTurn()].draw());
        console.log(b.toText());
    }));
});

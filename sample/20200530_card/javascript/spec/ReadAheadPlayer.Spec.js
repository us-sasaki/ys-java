
describe("SimplePlayer2 Test", () => {
	it("SimplePlayer2 new", () => {
        const b = createTestBoard();

		expect(new SimplePlayer2(b, Board.SOUTH)).toBeDefined();
    });
	it("SimplePlayer2 play on hand(1,11)", helperAsync(async () => {
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
	it("SimplePlayer2 play on sequencial random hands", helperAsync(async () => {
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

	it("SimplePlayer2 play on random hands", helperAsync(async () => {
        ReproducibleRandom.setSeed(12345);

        lp:
        for (let i = 0; i < 30; i++) {
            const boardNum = ReproducibleRandom.nextInt(1,32);
            let handNo = ReproducibleRandom.nextInt(1, 99999999);
            const b = new Board(boardNum);
            const player = [new SimplePlayer2(b, Board.NORTH),
                            new SimplePlayer2(b, Board.EAST),
                            new SimplePlayer2(b, Board.SOUTH),
                            new SimplePlayer2(b, Board.WEST)];
            b.deal(handNo);
            const level = ReproducibleRandom.nextInt(1,7);
            const denom = ReproducibleRandom.nextInt(1,5);
            const declarer = ReproducibleRandom.nextInt(0,3);
            b.setContract(new Bid(Bid.BID, level, denom), declarer);
            console.log(b.getContract().toString()+" by "+Board.SEAT_STRING[declarer]);

            while (b.status !== Board.SCORING) {
                const p = await player[b.getTurn()].draw();
                b.play(p);
            }
        }
    }));

    
});

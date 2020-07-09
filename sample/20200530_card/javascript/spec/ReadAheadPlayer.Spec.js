
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
        console.log(b.toString());
    }));
	it("SimplePlayer2 play on random hands", helperAsync(async () => {
        for (let boardNum = 2; boardNum < 10; boardNum++) {
            let handNo = boardNum + 10;
            const b = new Board(boardNum);
            const player = [new SimplePlayer2(b, Board.NORTH),
                            new SimplePlayer2(b, Board.EAST),
                            new SimplePlayer2(b, Board.SOUTH),
                            new SimplePlayer2(b, Board.WEST)];
            b.deal(handNo);
            const level = boardNum%6+2;
            const denom = boardNum%5
            const declarer = Board.WEST; //(boardNum+Math.floor(boardNum/4))%4;
            b.setContract(new Bid(Bid.BID, level, denom), declarer);

//            try {
                while (b.status !== Board.SCORING)
                    b.play(await player[b.getTurn()].draw());
//           } catch (e) {
//                console.log("error "+e);
//                console.log(`boardNum = ${boardNum}, level = ${level}, denom = ${denom}`);
//                console.log(b.toString());
//            }
        }
    }));

    
});

describe("Player Test", () => {
	it("Player constants", () => {
		expect(Player.LEAD).toBeDefined();
	});
});

/**
 * async �֐��e�X�g�p
 * @param {()=>Promise)} runAsync 
 */
function helperAsync(runAsync) {
  // done���g���đΉ�����
  return (done) => {
    runAsync().then(done, e => {
      fail(e);
      done();
    })
  };
}

describe("RandomPlayer Test", () => {
	it("RandomPlayer new", () => {
		expect(new RandomPlayer()).toBeDefined();
    });
    
    it("RandomPlayer play", helperAsync(async () => {
        const b = createTestBoard();
        dealSequence(b);
        bidSequence(b);
        const p = new RandomPlayer(b, b.getPlayer());
        const play = await p.play();
        expect(b.allows(play)).toBe(true);
    }));

    it("RandomPlayer play sequence", helperAsync(async () => {
        const b = createTestBoard();
        dealSequence(b);
        bidSequence(b);
        const p = new RandomPlayer(b, b.getPlayer());

        while (b.status != Board.SCORING) {
            const play = await p.play();
            b.play(play);
        }
        console.log(b.toText());
    }));

});



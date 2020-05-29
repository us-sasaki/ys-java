'use strict';


onload = function() {
	// test code
	var board = new Board(2);
	board.deal(5);
	
	board.play(new Bid(Bid.BID, 1, Bid.SPADE));
	board.play(new Bid(Bid.DOUBLE, 1, Bid.SPADE));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.REDOUBLE, 1, Bid.SPADE));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	
	window.alert(board.toString());
	
}



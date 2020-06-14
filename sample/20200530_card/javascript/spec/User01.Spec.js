'use strict';

onload = function() {
	
	// another test code
	const field = new Field('canvas');
	const board = new Board(3);
	board.deal(11);
	field.add(board);
	CardImageHolder.setBackImage(1);
	board.getHand().forEach( h => h.turn(true) );
	
	board.play(new Bid(Bid.BID, 1, Bid.CLUB));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	field.spot = board.getTurn();

//	console.log(board.allows(new Card(Card.DIAMOND, Card.KING)));

	( async () => {
		board.playWithGui(new Card(Card.DIAMOND, Card.KING));
		field.draw();
		await Field.sleep(1000);
		field.spot = board.getTurn();
		board.playWithGui(new Card(Card.DIAMOND, 9));
		field.draw();
		await Field.sleep(1000);
		field.spot = board.getTurn();
		board.playWithGui(new Card(Card.DIAMOND, 2));
		field.draw();
		await Field.sleep(1000);
		field.spot = board.getTurn();
		board.playWithGui(new Card(Card.DIAMOND, 5));
		field.draw();
		await Field.sleep(1000);
		field.spot = board.getTurn();
	
		console.log(board.toString());
		console.log(board.trickGui);
	
		board.layout.layout(board);
		field.draw();

		const card = await field.waitCardSelect();
		window.alert('async returned : '+card.toString());
		console.log('async returned : '+card.toString());
		await Field.sleep(1000);
		console.log('waited');
	} )();
}

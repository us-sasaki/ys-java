'use strict';

const startBridge = function() {
	//window.alert(problem);
	// another test code
	const field = new Field('canvas');
	const board = new Board(3);
	board.deal(11);
	field.add(board);
	// ボタン
console.log('field.canvas='+field.canvas);
	const quit = new Button(field, "quit");
	quit.isVisible = true;
	quit.setBounds(540, 30, 80, 24);
	field.add(quit);
	const dd = new Button(field, "ダブルダミー");
	field.add(dd);
	dd.setBounds(540, 58, 80, 24);
	let doubleDummy = true;
	dd.setListener( () => {
console.log('dd pressed');
		if (board.status != Board.PLAYING) return;
		//
		doubleDummy = !doubleDummy;
		dd.caption = doubleDummy?'元に戻す':'ダブルダミー';
		board.getHand(Board.EAST).turn(doubleDummy);
		board.getHand(Board.WEST).turn(doubleDummy);
		field.draw();
	});

	//dd.addActionListener(this);
	
	const textWindow = new Button(field, "show text");
	field.add(textWindow);
	textWindow.setBounds(540, 86, 80, 24);
	//textWindow.addActionListener(this);

	CardImageHolder.setBackImage(1);
	board.getHand().forEach( h => h.turn(true) );

	board.play(new Bid(Bid.BID, 1, Bid.CLUB));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	field.spot = board.getTurn();

//	console.log(board.allows(new Card(Card.DIAMOND, Card.KING)));

	const s = 500;
	( async () => {
		await board.playWithGui(new Card(Card.DIAMOND, Card.KING));
		await Field.sleep(s);
		field.spot = board.getTurn();
		board.getHand(Board.NORTH).layout = new DummyHandLayout();
		await board.playWithGui(new Card(Card.DIAMOND, 9));
		await Field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, 4));
		await Field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, 5));
		await Field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, Card.ACE));
		await Field.sleep(s);
		field.spot = board.getTurn();
		field.draw();
	
		console.log(board.toString());
		console.log(board.trickGui);
	

		//const card = await field.waitCardSelect();
		//window.alert('async returned : '+card.toString());
		//console.log('async returned : '+card.toString());
	} )();

}

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
	const quit = new Button(field, "中断");
	quit.isVisible = true;
	quit.setBounds(540, 30, 80, 24);
	field.add(quit);
	quit.setListener( () => {
		if (window.confirm('このボードを破棄して中断します')) {
			// 中断処理
			field.interrupt();
		}
	});


	const dd = new Button(field, "ダブルダミー");
	field.add(dd);
	dd.setBounds(540, 58, 80, 24);
	let doubleDummy = false;
	dd.setListener( () => {
console.log('dd pressed');
		if (board.status != Board.PLAYING) return;
		//
		doubleDummy = !doubleDummy;
		dd.caption = doubleDummy?'通常に戻す':'ダブルダミー';
		board.getHand(Board.EAST).turn(doubleDummy);
		board.getHand(Board.WEST).turn(doubleDummy);
		field.draw();
	});

	//dd.addActionListener(this);
	
	const textWindow = new Button(field, "テキスト表示");
	field.add(textWindow);
	textWindow.setBounds(540, 86, 80, 24);

	CardImageHolder.setBackImage(1);
	//board.getHand().forEach( h => h.turn(true) );

	board.play(new Bid(Bid.BID, 1, Bid.CLUB));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	board.play(new Bid(Bid.PASS));
	field.spot = board.getTurn();

//	console.log(board.allows(new Card(Card.DIAMOND, Card.KING)));

	const s = 500;
	( async () => {
		field.draw();
		const selectDialog = new SelectDialog('select');
		console.log(await selectDialog.show());
		const sumire = new Sumire(field, "メッセージ\n次の行\n3行目\n4行目\n5行目\n6行目");
		await sumire.animate(Sumire.NORMAL);

		await board.playWithGui(new Card(Card.DIAMOND, Card.KING));
		board.getHand(Board.NORTH).layout = new DummyHandLayout();
		await field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, 9));
		await field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, 4));
		await field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, 5));
		await field.sleep(s);
		field.spot = board.getTurn();
		await board.playWithGui(new Card(Card.DIAMOND, Card.ACE));
		await field.sleep(s);
		field.spot = board.getTurn();
		field.draw();
	
		console.log(board.toString());
		console.log(board.trickGui);
	

		//const card = await field.waitCardSelect();
		//window.alert('async returned : '+card.toString());
		//console.log('async returned : '+card.toString());
	} )();

}


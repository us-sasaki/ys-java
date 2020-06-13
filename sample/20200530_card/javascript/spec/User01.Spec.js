'use strict';

onload = function() {
	
	// another test code
	const field = new Field('canvas');
	const board = new Board(1);
	board.deal(11);
	field.add(board);
	
	field.draw();
}



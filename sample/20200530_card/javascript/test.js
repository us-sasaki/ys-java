'use strict';

onload = function() {
	
	// another test code
	var field = new Field('canvas');
	var packet = new Packet();
	var card = new Card(Card.CLUB, Card.KING);
	var card2 = new Card(Card.HEART, Card.ACE);
	var card3 = new Card(Card.SPADE, 10);
	
//	card2.isHead = false;
	packet.layout = new DummyHandLayout();
//	card.setPosition(200,200);
	packet.add(card);
	packet.add(card2); packet.add(new Card(Card.HEART, Card.JACK));
	packet.add(card3);
	packet.add(new Card(Card.DIAMOND, 5));
	field.add(packet);
	
	// provideDeck()
	var pack2 = Packet.provideDeck();
	pack2.shuffle();
	pack2.arrange();
	field.add(pack2);
	pack2.setPosition(10, 300);
	
	var i = 0;
	var dir = 1;
	var count = 5;
	
	var id = setInterval(function() {
		i += dir;
		if (i >= 600) i = 600;
		if (i < 0) i = 0;
		packet.setPosition(i, 20);
		packet.setDirection(Math.floor(i/50)%4);
		field.draw();
	}, 20);
	
	field.addEventListener('click', function() {
		dir = -dir;
		if (count-- == 0) clearInterval(id);
		
	});
}



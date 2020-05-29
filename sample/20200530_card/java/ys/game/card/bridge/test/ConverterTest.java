import ys.game.card.*;
import ys.game.card.bridge.*;

public class ConverterTest {
	public static void main(String[] args) throws Exception {
		Packet p = PacketFactory.provideDeck(PacketFactory.WITH_JOKER);
		
		p.peek(9).invalidate();
		p.peek(10).invalidate();
		String str = Converter.serialize(p);
		
		System.out.println(str);
		
		Packet deck = PacketFactory.provideUnspecifiedDeck(PacketFactory.WITH_JOKER);
		Packet q = Converter.deserializePacket(str, deck);
//		System.out.println(q);
		System.out.println(Converter.serialize(q));
		
		Board b = new BoardImpl(2);
		b.deal();
		b.play(new Bid(Bid.PASS));
		b.play(new Bid(Bid.BID, 1, Bid.SPADE));
		b.play(new Bid(Bid.DOUBLE, 1, Bid.SPADE));
		b.play(new Bid(Bid.REDOUBLE, 1, Bid.SPADE));
		b.play(new Bid(Bid.PASS));
		b.play(new Bid(Bid.PASS));
		b.play(new Bid(Bid.PASS));
		
		Player[] player = new Player[4];
		for (int i = 0; i < 4; i++) {
			player[i] = new RandomPlayer(b, i);
		}
		
		for (int i = 0; i < 4 * 10; i++) {
			b.play(player[b.getTurn()].play());
		}
		
//		System.out.println(b);
		String boardStr = Converter.serialize(b);
		System.out.println(boardStr);
		
		Board b2 = new BoardImpl(5);
		b2.deal();
		
		Converter.deserializeBoard(boardStr, b2);
		System.out.println(Converter.serialize(b2));
		
	}
}

import ys.game.card.bridge.*;

public class ThinkingUtilsTest {
	public static void main(String[] args) {
		Board b = new BoardImpl(1);
		
		b.deal();
		
		System.out.println(b);
		
		b.play(new Bid(Bid.BID, 1, Bid.CLUB));
		System.out.println(b);
		
		b.play(new Bid(Bid.PASS, 0, 0));
		System.out.println(b);
		
		b.play(new Bid(Bid.PASS, 0, 0));
		System.out.println(b);
		
		b.play(new Bid(Bid.DOUBLE, 1, Bid.CLUB));
		System.out.println(b);
		
		b.play(new Bid(Bid.REDOUBLE, 1, Bid.CLUB));
		System.out.println(b);
		
		b.play(new Bid(Bid.BID, 1, Bid.NO_TRUMP));
		System.out.println(b);
		
		b.play(new Bid(Bid.PASS, 0, 0));
		System.out.println(b);
		
		b.play(new Bid(Bid.PASS, 0, 0));
		System.out.println(b);
		
		b.play(new Bid(Bid.PASS, 0, 0));
		System.out.println(b);
		
		while (true) {
			int st = b.getStatus();
			int pl = b.getTurn();
			ys.game.card.Packet hand = b.getHand(pl);
			
			hand.shuffle();
			ys.game.card.Card c = null;
			for (int i = 0; i < hand.size(); i++) {
				c = hand.peek(i);
				if (b.allows(c)) break;
			}
			if (c == null) throw new RuntimeException("????");
			hand.arrange();
			
			b.play(c);
			
			hand.draw(c);
			System.out.println(b);
			
			if (b.getStatus() == Board.SCORING) break;
			
			int[][][] count = ThinkingUtils.countDistribution(b, Board.NORTH);
			System.out.println("-----------------------Distrubution(NORTH‚©‚ç‚Ý‚½EAST)-----");
			for (int i = 0; i < 4; i++) {
				System.out.print("(");
				System.out.print(count[Board.EAST][i][0]);
				System.out.print("/");
				System.out.print(count[Board.EAST][i][1]);
				System.out.println(")");
			}
			System.out.println("-----------------------Distrubution(NORTH‚©‚ç‚Ý‚½SOUTH)-----");
			for (int i = 0; i < 4; i++) {
				System.out.print("(");
				System.out.print(count[Board.SOUTH][i][0]);
				System.out.print("/");
				System.out.print(count[Board.SOUTH][i][1]);
				System.out.print(")");
			}
			System.out.println();
		}
		
	}
}

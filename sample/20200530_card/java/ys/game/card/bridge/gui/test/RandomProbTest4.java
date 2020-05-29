import ys.game.card.*;
import ys.game.card.gui.*;

import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;

public class RandomProbTest4 {
	public static void main(String[] args) throws Exception {
		int times = 30;
		if (args.length > 0) times = Integer.parseInt(args[0]);
		for (int i = 0; i < times; i++) {
			Problem p = new RandomProblem4();
			p.start();
			System.out.println(p.getContractString());
		Board b = new BoardImpl(1);
		b.deal(p.getHand());
		b.setContract(p.getContract(), Board.SOUTH); // 1NT
		for (int j = 0; j < 4; j++) b.getHand()[j].turn(true);
		System.out.println("---ƒ{[ƒhî•ñ---");
		System.out.println(b.toText());
//			if (p.getContractString().equals("1NT")) {
//				Packet[] hand = p.getHand();
//				for (int j = 0; j < 4; j++) {
//					System.out.print(hand[j]);
//					if (j == Board.SOUTH) System.out.println("****** DECLARER *******");
//					else System.out.println();
//				}
//			}
		}
	}
}

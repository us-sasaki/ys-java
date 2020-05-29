import ys.game.card.*;
import ys.game.card.gui.*;

import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;

public class RandomProbTest3 {
	public static void main(String[] args) throws Exception {
		int times = 30;
		if (args.length > 0) times = Integer.parseInt(args[0]);
		for (int i = 0; i < times; i++) {
			Problem p = new RandomProblem3();
			p.start();
			System.out.println(p.getContractString());
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

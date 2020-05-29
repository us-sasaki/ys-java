import ys.game.card.*;
import ys.game.card.gui.*;

import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;

public class RandomProbTest32 {
	public static void main(String[] args) throws Exception {
		Problem p = new RandomProblem3();
		p.start();
		System.out.println(p.getContractString());
		BoardImpl b = new BoardImpl(1);
		Packet[] pp = p.getHand();
		for (int i = 0 ; i < pp.length; i++) {
			pp[i].turn(true);
		}
		b.deal(pp);
		b.setContract(p.getContract(), Board.SOUTH);
		b.setName("Test");
		System.out.println(b.toText());
		System.out.println(b);
	}
}

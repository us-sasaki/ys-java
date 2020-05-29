import ys.game.card.bridge.thinking.*;
import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.bridge.*;

public class ConclusionTest {
/*------------------
 * �f�o�b�O�p���C��
 */
	public static void main(String[] args) throws Exception {
		Board board = new BoardImpl(1);
		board.deal();
		
		board.play(new Bid(Bid.BID, 1, Bid.CLUB));
		board.play(new Bid(Bid.PASS));
		board.play(new Bid(Bid.PASS));
		board.play(new Bid(Bid.PASS));
		
		// �O�̎�Ɠ������^����
//		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=SQJT6HKQT653CQ98&E=S742H84CAJ6D96543&S=SK98HJ7C432DAKT72&W=SA53HA92CKT75DQJ8&bid=1N-P-P-P&contract=1N&declarer=N", board);
//		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=S9432HQ73CAJDJ432&E=SK865HATC753DQT96&S=SQJ7HK865CT642DA5&W=SATHJ942CKQ98DK87&bid=1N-P-P-P&contract=1N&declarer=N", board);
		System.out.println("�͂��߂̏��");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		// ���̃A���S���Y���͂ǂ̐Ȃł��g���܂킵�ł���
		Player player = new RandomPlayer(board, Board.NORTH);
		
		for (int i = 0; i < 52 - 4*6; i++) {
			board.play(player.draw());
		}
		
		System.out.println("�Ō�̏��");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		long t0 = System.currentTimeMillis();
		Conclusion s = Conclusion.bestPlayOf(new OptimizedBoard(board));
		long t = System.currentTimeMillis();
		
		System.out.println("�ŗǂ̎�");
		System.out.println("�ō��_�F" + s.lastNSSideTricks);
		System.out.println("�őP��F" + OptimizedBoard.toString(s.bestPlay));
		
		System.out.println("���v���ԁF" + (t-t0));
	}
	public static final String[] DIR = new String[] { "N","E","S","W"};
	public static final String[] SUIT = new String[] { "C", "D", "H", "S", "*" };
	public static final String[] VALUE =
			new String[] { "2","3","4","5","6","7","8","9","T","J","Q","K","A"};
	
	private static void print(int[] t) {
		for (int i = 0; i < t.length; i++) {
			if (t[i] == -1) break;
			System.out.print(SUIT[t[i]/14]+VALUE[t[i]%14]);
		}
		System.out.println();
	}
	
}
import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

import ys.game.card.bridge.*;

public class UndoTest {
	public static void main(String[] args) throws Exception {
		Board board = new BoardImpl(1);
		board.deal();
		
		board.play(new Bid(Bid.BID, 1, Bid.CLUB));
		board.play(new Bid(Bid.PASS));
		board.play(new Bid(Bid.PASS));
		board.play(new Bid(Bid.PASS));
		
		// �O�̎�Ɠ������^����
		System.out.println("�͂��߂̏��");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		// ���̃A���S���Y���͂ǂ̐Ȃł��g���܂킵�ł���
		Player player = new RandomPlayer(board, Board.NORTH);
		
		for (int i = 0; i < 52; i++) {
			board.play(player.draw());
		}
		
		System.out.println("�Ō�̏��");
		System.out.println(board);
		System.out.println();
		
		System.out.println("������������ undo 3 �� ������������");
		for (int j = 0; j < 3; j++) board.undo();
		
		System.out.println("���� Board ��� ����");
		System.out.println(board);
		
		for (int j = 0; j < 3; j++) {
			board.play(player.draw());
			System.out.println("�� " + j);
			System.out.println(board);
		}
	}
}
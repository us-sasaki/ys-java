package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

public class Set {
	protected int	lastDeclarerSideScore;
	protected Card	bestPlay;
	
	public Set(int p, Card play) {
		lastDeclarerSideScore = p;
		bestPlay = play;
	}
	
	// �֐�������Ă݂�
	public static Set bestPlayOf(Board b) {
 		int declarer = b.getDeclarer();
		int seat = b.getTurn();
		
		Packet h = b.getHand(seat);
		
		// �ŗǂ̌��ʂ̕ۑ��ꏊ
		// �ŗǂ̓_��
		int maxScore		= Integer.MIN_VALUE;
		int decScore		= 0; // ���^�[�����邽�߂� declarer ���̓_��
		// ���̂Ƃ��̃v���C
		Card maxPlay		= null;
		
		// �\�ȃv���C�ɂ��ă��[�v���܂킷
		for (int i = 0; i < h.size(); i++) {
			// ���i�̃J�[�h�̓X�L�b�v����
			Card trial = h.peek(i);
			try {
				// �v���C�����Ƃ���
				// ���̃v���C�̂����炷���ʂ��ŗǂ̌��ʂƂȂ���̂�ۑ����Ă���
				b.play(trial);
				
				// �Ō�̃v���C�������ꍇ�́A���R���ʂ͈��
				if (b.getStatus() == Board.SCORING) {
//					maxScore = Score.calculate(b, seat);	// �x��
					maxScore = BridgeUtils.countDeclarerSideWinners(b);
					decScore = maxScore;
					if ( ((seat^declarer)&1) == 1) maxScore = 13-maxScore;
					maxPlay  = trial;
					b.undo();
					break;
				}
				
				// �v���C�����Ƃ��āA�_�����ǂ��Ȃ邩������B�_���͈�ӂɌ��܂�B
				// �G�̔Ԃ� best play �����Ă��邱�ƂɂȂ�
				Set s = bestPlayOf(b);
				
				// ���������̃X�R�A�ɒ���
				int decsc = s.lastDeclarerSideScore;
				int score;
				if ( ((seat^declarer)&1) == 1) score = 13-decsc; // Defender �� �}�C�i�X�ɂȂ�
				else score = decsc;
				
				if (maxScore < score) { // ���ǂ��v���C������
					maxScore	= score;
					decScore	= decsc;
					maxPlay		= trial;
				}
				// ���̂��߂� b �̏�Ԃ�߂��Ă���
				b.undo();
			} catch (IllegalPlayException ignored) {
			}
		}
		return new Set(decScore, maxPlay);
	}
	
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
		Player player = new DebugPlayer(board, Board.NORTH);
		
		for (int i = 0; i < 52 - 17; i++) {
			board.play(player.draw());
		}
		
		System.out.println("�Ō�̏��");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		//
		if (board.getStatus() == Board.SCORING) return;
		
		Set s = Set.bestPlayOf(board);
		
		System.out.println("�ŗǂ̎�");
		System.out.println("�ō��_�F" + s.lastDeclarerSideScore);
		System.out.println("�őP��F" + s.bestPlay);
	}
}


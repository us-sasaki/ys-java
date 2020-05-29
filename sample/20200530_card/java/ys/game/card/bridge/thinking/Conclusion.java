package ys.game.card.bridge.thinking;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.bridge.*;

public class Conclusion {
	public int	lastNSSideTricks;
	public int	bestPlay;
	
	public Conclusion(int p, int play) {
		lastNSSideTricks = p;
		bestPlay = play;
	}
	
	/**
	 * �^����ꂽ OptimizedBoard �ɑ΂��A���������̃g���b�N�����ő�Ƃ���v���C
	 * (�őP��)�ƁA���̂Ƃ��̃g���b�N����Ԃ��܂��B
	 * ���Ȃ�̌v�Z�ʂ��K�v�ŁA�Ō�̂T�g���b�N���x�����E��������Ȃ��B
	 *
	 */
	public static Conclusion bestPlayOf(OptimizedBoard b) {
		int seat = b.getTurn();
		
		// �v���C���𒊏o����
		int[] h = ShortCut.listOptions(b);
		
		// �ŗǂ̌��ʂ̕ۑ��ꏊ
		// �ŗǂ̓_��
		int maxTricks		= Integer.MIN_VALUE;
		int decTricks		= 0; // ���^�[�����邽�߂� NS ���̓_��
		// ���̂Ƃ��̃v���C
		int maxPlay		= -1;
		
		// �\�ȃv���C�ɂ��ă��[�v���܂킷
		for (int i = 0; i < h.length; i++) {
			if (h[i] == -1) break;
			int trial = h[i];
			// �v���C�����Ƃ���
			// ���̃v���C�̂����炷���ʂ��ŗǂ̌��ʂƂȂ���̂�ۑ����Ă���
			int leftPlays = b.play(trial);
			
			// �Ō�̃v���C�������ꍇ�́A���R���ʂ͈��
			if (leftPlays == 0) {
				maxTricks = b.countNSWinners();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13-maxTricks;
				maxPlay  = trial;
				b.undo();
				break;
			} else if (leftPlays == 4) {
				maxTricks = b.countNSWinnersLeavingLastTrick();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13-maxTricks;
				maxPlay  = trial;
				b.undo();
				break;
			}
			
			// �v���C�����Ƃ��āA�_�����ǂ��Ȃ邩������B�_���͈�ӂɌ��܂�B
			// �G�̔Ԃ� best play �����Ă��邱�ƂɂȂ�
			Conclusion s = bestPlayOf(b);
			
			// ���������̃X�R�A�ɒ���
			int decsc = s.lastNSSideTricks;
			int score;
			if ( (seat&1) == 1) score = 13-decsc; // Defender �� �}�C�i�X�ɂȂ�
			else score = decsc;
			
			if (maxTricks < score) { // ���ǂ��v���C������
				maxTricks	= score;
				decTricks	= decsc;
				maxPlay		= trial;
			}
			// ���̂��߂� b �̏�Ԃ�߂��Ă���
			b.undo();
		}
		return new Conclusion(decTricks, maxPlay);
	}
	
}


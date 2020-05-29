package ys.game.card.bridge.thinking;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.bridge.*;

public class Conclusion2 {
	/** bestPlays �p�̃J�E���^ */
	public static int[][] bpbuf;
	static {
		bpbuf = new int[52][];
		for (int i = 0; i < 52; i++) {
			bpbuf[i] = new int[13-i/4];
		}
	}
	
	public int	lastNSSideTricks;
	public int	bestPlay;
	public int[]	bestPlays;
	public int		bestPlayCount;
	
	public Conclusion2(int p, int play, int[] plays, int playCount) {
		lastNSSideTricks = p;
		bestPlay = play;
		bestPlays = plays;
		bestPlayCount = playCount;
	}
	
	/**
	 * �^����ꂽ OptimizedBoard �ɑ΂��A���������̃g���b�N�����ő�Ƃ���v���C
	 * (�őP��)�ƁA���̂Ƃ��̃g���b�N����Ԃ��܂��B
	 * ���Ȃ�̌v�Z�ʂ��K�v�ŁA�Ō�̂T�g���b�N���x�����E��������Ȃ��B
	 *
	 */
	public static Conclusion2 bestPlayOf(OptimizedBoard b) {
		int seat = b.getTurn();
		
		// �v���C���𒊏o����
		int[] h = b.listOptions();
		
		// �ǂ̃o�b�t�@���g����
		int[] bp = bpbuf[b.tricks * 4 + b.trickCount[b.tricks]];
		int   bpcnt = 0; // �őP��̌�␔
		
		// �ŗǂ̌��ʂ̕ۑ��ꏊ
		// �ŗǂ̓_��
		int maxTricks		= Integer.MIN_VALUE;
		int decTricks		= 0; // ���^�[�����邽�߂� NS ���̓_��
		int opponentOptions	= 14;
		// ���̂Ƃ��̃v���C
		int maxPlay		= -1;
		
		//
		// �P�i�� Only �̏���
		//
		// �\�ȃv���C���P��ނ����Ȃ��Ƃ��A���̃v���C���s��(highest)
		if ((h.length == 1)||(h[1] == -1)) {
			bp[0] = h[0];
			bpcnt = 1;
			return new Conclusion2(-1, h[0], bp, bpcnt);
		}
		
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
				opponentOptions	= 0;
				maxPlay			= trial;
				bp[bpcnt++]		= trial;
				b.undo();
				break;
			} else if (leftPlays == 4) {
				maxTricks = b.countNSWinnersLeavingLastTrick();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13-maxTricks;
				opponentOptions	= 1;
				maxPlay			= trial;
				bp[bpcnt++]		= trial;
				b.undo();
				break;
			}
			
			// �v���C�����Ƃ��āA�_�����ǂ��Ȃ邩������B�_���͈�ӂɌ��܂�B
			// �G�̔Ԃ� best play �����Ă��邱�ƂɂȂ�
			Conclusion2 s = bestPlayOfImpl(b, 2);
			
			// ���������̃X�R�A�ɒ���
			int decsc = s.lastNSSideTricks;
			int score;
			if ( (seat&1) == 1) score = 13-decsc; // Defender �� �}�C�i�X�ɂȂ�
			else score = decsc;
			
			if (maxTricks < score) { // �����A�܂��͂��ǂ��v���C������
				maxTricks	= score;
				decTricks	= decsc;
				maxPlay		= trial;
				
				opponentOptions = s.bestPlayCount;
				
				bpcnt = 0; // ���Z�b�g
				bp[bpcnt++] = trial;
			} else if (maxTricks == score) { // ���l�ɍőP�肾����
				// maxPlay ��u�������邩�H
				// �G�̍őP��I���������炷�悤�ɂ���
				if (s.bestPlayCount < opponentOptions) {
					opponentOptions = s.bestPlayCount;
					maxPlay = trial;
				}
				bp[bpcnt++] = trial;
			}
			// ���̂��߂� b �̏�Ԃ�߂��Ă���
			b.undo();
		}
		if (bpcnt < bp.length) bp[bpcnt] = -1;
		
		// ���i�̃J�[�h���܂߂Ȃ��ŕԂ�
		return new Conclusion2(decTricks, maxPlay, bp, bpcnt);
	}
	
	public static Conclusion2 bestPlayOfImpl(OptimizedBoard b, int depth) {
		int seat = b.getTurn();
		
		// �v���C���𒊏o����
		int[] h = b.listOptions();
		
		// �ǂ̃o�b�t�@���g����
		int[] bp = bpbuf[b.tricks * 4 + b.trickCount[b.tricks]];
		int   bpcnt = 0; // �őP��̌�␔
		
		// �ŗǂ̌��ʂ̕ۑ��ꏊ
		// �ŗǂ̓_��
		int maxTricks		= Integer.MIN_VALUE;
		int decTricks		= 0; // ���^�[�����邽�߂� NS ���̓_��
		int opponentOptions	= 14;
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
				opponentOptions	= 0;
				maxPlay			= trial;
				bp[bpcnt++]		= trial;
				b.undo();
				break;
			} else if (leftPlays == 4) {
				maxTricks = b.countNSWinnersLeavingLastTrick();
				decTricks = maxTricks;
				if ( (seat&1) == 1) maxTricks = 13-maxTricks;
				opponentOptions	= 1;
				maxPlay			= trial;
				bp[bpcnt++]		= trial;
				b.undo();
				break;
			}
			
			// �v���C�����Ƃ��āA�_�����ǂ��Ȃ邩������B�_���͈�ӂɌ��܂�B
			// �G�̔Ԃ� best play �����Ă��邱�ƂɂȂ�
			Conclusion2 s = bestPlayOfImpl(b, depth + 1);
			
			// ���������̃X�R�A�ɒ���
			int decsc = s.lastNSSideTricks;
			int score;
			if ( (seat&1) == 1) score = 13-decsc; // Defender �� �}�C�i�X�ɂȂ�
			else score = decsc;
			
			if (maxTricks < score) { // ���ǂ��v���C������
				maxTricks	= score;
				decTricks	= decsc;
				maxPlay		= trial;
				
				opponentOptions = s.bestPlayCount;
				
				bpcnt = 0; // ���Z�b�g
				bp[bpcnt++] = trial;
				
				// �R�i�ڈȍ~�ł́A
				// �c��S���Ƃ��ꍇ�́A����ȏ�̍őP��͂Ȃ��̂ŁAbreak ����B
				if ( (depth > 2)&&(maxTricks == (leftPlays / 4)) ) {
System.out.print(".");
					b.undo();
					break;
				}
			} else if (maxTricks == score) { // ���l�ɍőP�肾����
				// maxPlay ��u�������邩�H
				// �G�̍őP��I���������炷�悤�ɂ���
				if (s.bestPlayCount < opponentOptions) {
					opponentOptions = s.bestPlayCount;
					maxPlay = trial;
				}
				bp[bpcnt++] = trial;
			}
			// ���̂��߂� b �̏�Ԃ�߂��Ă���
			b.undo();
			
		}
		if (bpcnt < bp.length) bp[bpcnt] = -1;
		return new Conclusion2(decTricks, maxPlay, bp, bpcnt);
	}
}

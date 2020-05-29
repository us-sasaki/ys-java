package ys.game.card.bridge;

/*
 * 2001/ 8/ 7    �e���\�b�h�� static ��
 */

/**
 * ���̃N���X�̓R���g���N�g�u���b�W�ɂ����ăX�R�A���Z�o���܂��B
 * ���ʁA�f���v���P�[�g�����ɂ��Z�o�������Ȃ��܂����A����
 * ���o�[�A�}�b�`�|�C���g�Ȃǂւ̋@�\�g�����s�������Ƃ������Ă��܂��B
 *
 * @version		a-release		21, May 2000
 * @author		Yusuke Sasaki
 */
public class Score {
	static final int[] VUL_BONUSES		= new int[] { 1250, 2000, 500, 50 };
	static final int[] NONVUL_BONUSES	= new int[] { 800, 1300, 300, 50 };
	
	/**
	 * ���̃X�R�A�v�Z�I�u�W�F�N�g�����������܂��B
	 * �������A���݂͎�������Ă��܂���B
	 */
	public void init() {
	}
	
	/**
	 * �^����ꂽ�{�[�h�A�Ȃɂ�����_�����v�Z���܂��B
	 * �^����ꂽ�{�[�h���I�����Ă��Ȃ��ꍇ�AIllegalStatusException
	 * ���X���[����܂��B
	 *
	 * @param	board		�v�Z�Ώۂ̃{�[�h
	 * @param	seat		�v�Z���s������(Board.NORTH �Ȃ�)
	 *
	 * @return	���_
	 */
	public static int calculate(Board board, int seat) {
		if (board.getStatus() != Board.SCORING)
			throw new IllegalStatusException("�{�[�h�͂܂��I�����Ă��Ȃ����߁A�_���̌v�Z�͂ł��܂���B");
		
		if ( (seat < 0)||(seat > 3) )
			throw new IllegalArgumentException("�w�肳�ꂽ���Ȕԍ�"+seat+"�͖����ł��B");
		
		int		vul			= board.getVulnerability();
		Bid		contract	= board.getContract();
		if (contract.getKind() == Bid.PASS) return 0; // Passed-Out Board
		
		int		declarer	= board.getDeclarer();
		
		return calcImpl(contract, countWinners(board), declarer, seat, vul);
	}
	
	/**
	 * �^����ꂽ�{�[�h�ɂ����ăf�B�N���A���[���̂Ƃ����g���b�N�����J�E���g���܂��B
	 *
	 * @param		board		�E�B�i�[�𐔂���ΏۂƂȂ�{�[�h
	 */
	public static int countWinners(Board board) {
		return BridgeUtils.countDeclarerSideWinners(board);
	}
	
	public static int calculate(Bid contract, int win, int declarer, int seat, int vul) {
		return calcImpl(contract, win, declarer, seat, vul);
	}
	
	private static int calcImpl(Bid contract, int win, int declarer, int seat, int vul) {
		int make = win - 6;
		int up = win - contract.getLevel() - 6;
		
		int score = 0;
		if (up >= 0) {
			//
			// �R���g���N�g�ɑ΂����{�_�v�Z
			//
			int trickScore = 0;
			switch (contract.getSuit()) {
			
			case Bid.NO_TRUMP:
				trickScore = 30;
				break;
			
			case Bid.SPADE:
			case Bid.HEART:
				trickScore = 30;
				break;
			
			case Bid.DIAMOND:
			case Bid.CLUB:
				trickScore = 20;
				break;
			
			default:
				throw new InternalError("�R���g���N�g�̃X�[�c���s���ł�");
			}
			trickScore = trickScore * contract.getLevel();
			if (contract.getSuit() == Bid.NO_TRUMP) trickScore+=10;
			
			//
			// �_�u���̎��̏C��
			//
			if (contract.getKind() == Bid.DOUBLE) trickScore *= 2;
			if (contract.getKind() == Bid.REDOUBLE) trickScore *= 4;
			
			//
			// �A�b�v�g���b�N
			//
			int uptrickBonus = 0;
			switch (contract.getSuit()) {
			
			case Bid.NO_TRUMP:
				uptrickBonus = 30;
				break;
			
			case Bid.SPADE:
			case Bid.HEART:
				uptrickBonus = 30;
				break;
			
			case Bid.DIAMOND:
			case Bid.CLUB:
				uptrickBonus = 20;
				break;
			
			default:
				throw new InternalError("�R���g���N�g�̃X�[�c���s���ł�");
			}
			
			//
			// �_�u���̎��̏C��
			//
			if (contract.getKind() == Bid.DOUBLE) {
				if (isVul(vul, declarer)) score = trickScore + 200 * up;
				else score = trickScore + 100 * up;
			} else if (contract.getKind() == Bid.REDOUBLE) {
				if (isVul(vul, declarer)) score = trickScore + 400 * up;
				else score = trickScore + 200 * up;
			} else score = trickScore + uptrickBonus * up;
			
			//
			// �Q�[���A�X�����{�[�i�X
			//
			int[] bonuses;
			
			if (isVul(vul, declarer)) {
				//
				// �o���̏ꍇ
				//
				bonuses = VUL_BONUSES;
			}
			else {
				//
				// �m���o���̏ꍇ
				//
				bonuses = NONVUL_BONUSES;
			}
			//
			// �_�u�����C�N�̃{�[�i�X
			//
			if (contract.getKind() == Bid.DOUBLE) score += 50;
			else if (contract.getKind() == Bid.REDOUBLE) score += 100;
			
			
			int level = contract.getLevel();
			if (level == 6) score += bonuses[0]; // Small Slum
			else if (level == 7) score += bonuses[1]; // Grand Slum
			else if (trickScore >= 100) score += bonuses[2]; // Game
			else score += bonuses[3];	// partial
			
			if ( ((declarer ^ seat) & 1) == 1 ) {
				score = -score;
			}
		}
		else {
			int down = -up;
			
			if (contract.getKind() == Bid.BID) {
				if (!isVul(vul, declarer)) score = -50 * down;
				else score = -100 * down;
			}
			else if (contract.getKind() == Bid.DOUBLE) {
				if (!isVul(vul, seat)) {
					for (int i = 0; down > 0; i++) {
						if (i == 0) score -= 100;
						else if ( (i > 0)&&(i < 3) ) score -= 200;
						else score -= 300;
						down--;
					}
				}
				else {
					for (int i = 0; down > 0; i++) {
						if (i == 0) score -= 200;
						else score -= 300;
						down--;
					}
				}
			}
			else if (contract.getKind() == Bid.REDOUBLE) {
				if (!isVul(vul, declarer)) {
					for (int i = 0; down > 0; i++) {
						if (i == 0) score -= 200;
						else if ( (i > 0)&&(i < 3) ) score -= 400;
						else score -= 600;
						down--;
					}
				}
				else {
					for (int i = 0; down > 0; i++) {
						if (i == 0) score -= 400;
						else score -= 600;
						down--;
					}
				}
			}
			
			if ( ((declarer ^ seat) & 1) == 1 ) {
				score = -score;
			}
		}
		
		return score;
	}
	
	private static boolean isVul(int vul, int seat) {
		int mask;
		if ( (seat == Board.NORTH)||(seat == Board.SOUTH) ) mask = 1;
		else mask = 2;
		
		if ((vul & mask) > 0) return true;
		return false;
	}
	
/*
 * debug
 */
	public static void main(String[] args) throws Exception {
		Score s = new Score();
//				calcImpl(Bid contract, int win, int declarer, int seat, int vul) {

		int sc = s.calcImpl(new Bid(Bid.BID, 5, Bid.CLUB), 11, 0, 2, 3);
		System.out.println(sc);
		
	}
	
}

package ys.game.card.bridge.thinking;

import ys.game.card.bridge.Bid;

/**
 * Optimized Board �ɑ΂���]���֐��A�������Ɋւ���}����̊֐��Q�ł�
 */
public class ShortCut {
	/**
	 * n �Ԗڂ̃v���C�̃v���C���̃��X�g���i�[���邽�߂̗̈�ł��B
	 * �Y�����́A[n-1][0,1, ... , 14-n] �ƂȂ�܂�
	 */
	public static int[][]		playOptions;
	static {
		playOptions	= new int[52][];
		for (int i = 0; i < 52; i++)
			playOptions[i] = new int[13-(i/4)];
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �v���C���ƂȂ�J�[�h�̏W���𒊏o���܂��B
	 * �������̂��߁A���i�J�[�h�������������s���܂��B
	 * ���̃��\�b�h�͑��x�Ɛ��x�����߂邽�߁A��ʂōs���Ă��悢���\�b�h�ł����A
	 * �����ێ��\���ɗ������邽�߁A�{�N���X�Ɋ܂߂Ă��܂��B
	 * ���ʂ́A���� n �g���b�N�ڂƂ��āA�v�f���� 14-n �̔z��ƂȂ�܂��B
	 * ��␔�͏��Ȃ��ꍇ������A���̂Ƃ��͌�₪�I����Ă��邱�Ƃ��������߁A-1 ��
	 * �}������܂��B-1 �̌�̓��e�͕s��ł��B
	 *
	 * @return		�v���C���
	 */
	public static int[] listOptions(OptimizedBoard b) {
		int[] result = listPlayableCards(b);
		
		// �}����(1)
		// ���i�̃J�[�h�����O����
		int lastEntried = result[0];
		for (int i = 1; i < result.length; i++) {
			int next = result[i];
			if (next == -1) break;
			// lastEntried �Ɠ��i���ǂ���
			// �u���i�v�Ƃ́A�Q���̃J�[�h�̂�������o���Ă�����̌��ʂƂȂ邱��
			// lastEntried, next �̊Ԃ̃J�[�h�����ׂăv���C�ς݂ł���ꍇ�A�u���i�v
			// �����������A����ɏo�Ă���J�[�h�́u�v���C�ς݂łȂ��v�ƍl����K�v������I
			//
			// (1) lastEntried, next �Ƃ��ɓ����n���h�ɓ����Ă���
			// (2) result �ɂ͍~���ɃJ�[�h���܂܂�Ă���
			// 
			int j;
		loop: // �����������̃��[�v������̂ŁB�B�B
			for (j = lastEntried - 1; j > next; j--) {
				if (!b.isPlayed[j]) break;
				// �����̏����B�x���I�I �v���C�ς݃J�[�h�������Ȃ��Ƃ����ɂ��Ȃ��̂�
				// �e���͏��Ȃ��Ƃ͎v�����c�c
				// �����A�e�����������ꍇ�AisPlayed ���A�g���b�N���I����Ă��� true
				// �Ƃ��邱�Ƃł��̏����������ł���B(isPlayed �� isRemoved �ɂ���)
				// ���̏ꍇ�AOptimizedBoard �� draw, undo �݂̂�ύX����΂悢
				// ShortCut �ł́AisRemoved �̎g�����������Ă��Ȃ�
				for (int k = 0; k < b.trickCount[b.tricks]; k++) {
					if (j == b.trick[b.tricks][k]) break loop;
				}
			}
			lastEntried = next;
			if (j == next) {
				// ���i�B�߂�B���[�v��������Ɖ��悤�ɂ���B
				System.arraycopy(result, i+1, result, i, result.length-i-1);
				result[result.length - 1] = -1; // ���łɑO�� -1 �������Ă��邩������Ȃ���
				i--;
			}
		}
		
		return result;
	}
	
	/**
	 * ���݃v���C����n���h�̒��ŁA�w��J�[�h�Ɠ��i�̃J�[�h�𒊏o���܂��B
	 * �x�X�g�v���C�̃��X�g���o���Ƃ��ɁA���i�̃J�[�h���܂߂����ۂ̃��X�g��
	 * �߂��Ƃ��Ɏg�p���邽�߂̊֐��ł��B
	 * �{���\�b�h�͓��ɍ����ɂȂ�悤�ɂ͂����Ă��Ȃ����߁A���[�v�̊O����
	 * �ĂԕK�v������܂��B
	 *
	 * �������ݏ�ɏo�Ă���g���b�N�́A�u�܂��v���C����Ă��Ȃ��v�ƔF�����Ȃ��ƁA
	 *     ��ɏo�Ă�����̂��܂߂ăV�[�N�G���X�ɂȂ��Ă���ꍇ�A���[�J�[�h��
	 *     �t�H���[���Ă��܂����Ƃ�����B
	 *	 
	 * @param		card	���i�̃J�[�h��T�������J�[�h
	 * @return		���i�̃J�[�h
	 */
	public static int[] getEqualCards(OptimizedBoard b, int[] cards, int cardCount) {
		int		seat	= (b.leader[b.tricks]+b.trickCount[b.tricks])%4;
		
		// �v���C����Ă��Ȃ��J�[�h = 0 // �܂��͍���ɏo�Ă���J�[�h
		// �����Ă���J�[�h         = 1
		// �w�肳�ꂽ�J�[�h			= 2
		// �v���C���ꂽ�J�[�h       = 3
		// 0 �� delimiter �Ƃ��āAtoken ����؂�A2 ���܂܂�Ă��� token ��
		// 1 �� 2 �ɕύX����B2 �ƂȂ��Ă���J�[�h��ԋp����
		int[] tmp = new int[56];
		
		// �v���C���ꂽ���ǂ����Ńt���O��ݒ�
		for (int i = 0; i < 56; i++) {
			if (b.isPlayed[i]) tmp[i] = 3;
			else tmp[i] = 0;
		}
		// ����ɏo�Ă���J�[�h
		for (int i = 0; i < b.trickCount[b.tricks]; i++) {
			tmp[b.trick[b.tricks][i]] = 0; // 3 �ɂȂ��Ă����͂�
		}
		
		// �����Ă���J�[�h�̐ݒ�
		for (int i = 0; i < b.handCount[seat]; i++) {
			tmp[b.hand[seat][i]] = 1;
		}
		// �w�肳�ꂽ�J�[�h�̐ݒ�
		for (int i = 0; i < cardCount; i++) {
			if (tmp[cards[i]] != 1)
				throw new IllegalArgumentException("�J�[�h " + cards[i] + " �͎����Ă��܂���");
			tmp[cards[i]] = 2;
		}
		
		// token ���Ƃ̏���
		int tokenStartIndex = 0;
		int resultCount = 0;
		
		while (true) {
			// delimiter �łȂ��C���f�b�N�X��T�� --> tokenStartIndex
			for (; tokenStartIndex < 56; tokenStartIndex++) {
				if (tmp[tokenStartIndex] != 0) break;
			}
			if (tokenStartIndex == 56) break;
			
			int tokenEndIndex;
			boolean containsTargetCard = false;
			for (tokenEndIndex = tokenStartIndex; tokenEndIndex < 56; tokenEndIndex++) {
				if (tmp[tokenEndIndex] == 2)
					containsTargetCard = true;
				else if (tmp[tokenEndIndex] == 0) break;
			}
			
			if (containsTargetCard) {
				for (int i = tokenStartIndex; i < tokenEndIndex; i++) {
					if (tmp[i] != 3) {
						tmp[i] = 2;
						resultCount++;
					}
				}
			}
			tokenStartIndex = tokenEndIndex + 1;
			if (tokenStartIndex >= 56) break;
		}
		
		// ���ʂ̍쐬
		// ������ς��邱�ƂŎア���ɕ��ׂ邱�Ƃ��ł���
		int[] result = new int[resultCount];
		resultCount = 0;
		for (int i = 55; i >= 0; i--) {
			if (tmp[i] == 2)
				result[resultCount++] = i;
		}
		return result;
	}
	
	/**
	 * �{�N���X�� play() ���\�b�h�̈����Ƃ��Đݒ肷��A�u���b�W���[���ɂ̂��Ƃ���
	 * �J�[�h�S�̂̏W����ԋp���܂��B
	 *
	 * @return		���[����v���C�\�ȃJ�[�h�̏W��(�J�[�h�萔)
	 */
	public static int[] listPlayableCards(OptimizedBoard b) {
		int seat = (b.leader[b.tricks]+b.trickCount[b.tricks])%4;
		int playOptionsIndex = 4*b.tricks+b.trickCount[b.tricks];
		
		if (b.trickCount[b.tricks] != 0) {
			int leadSuit = b.trick[b.tricks][0]/14;
			int options = 0;
			
			// �X�[�g�t�H���[��T��
			for (int i = 0; i < b.handCount[seat]; i++) {
				if ((b.hand[seat][i]/14)==leadSuit) {
					// ������
					playOptions[playOptionsIndex][options++] = b.hand[seat][i];
				}
			}
			if (options > 0) {
				if (options < 13-b.tricks) playOptions[playOptionsIndex][options] = -1;
				return playOptions[playOptionsIndex];
			}
		}
		// ���[�h�̏ꍇ�A�܂��̓X�[�g�t�H���[�ł��Ȃ� ... ���ׂẴJ�[�h���o����
		System.arraycopy(b.hand[seat], 0, playOptions[playOptionsIndex], 0, b.handCount[seat]);
		// -1 ��}������K�v�͂Ȃ�
		
		return playOptions[playOptionsIndex];
	}
	
	/**
	 * NS ���̎���{�[�h�̔Ֆʕ]���ɂ���ċߎ��l���Z�o���܂��B
	 * ��ǂ݂̐[�����[���Ƃ��ɂ�����x�ȏ��̎�͖{�֐��ŕ]�����邱�Ƃɂ����
	 * ���������s���܂��B
	 * �{�֐��ł̓��[�h��ԂƂȂ��Ă���{�[�h�ȊO���w�肷�邱�Ƃ͂ł��܂���B
	 *
	 * @param		b		�]���Ώۂ� board
	 * @return		�Ֆʕ]���ɂ���ĊT�Z���ꂽ NS ���̃g���b�N��
	 * @exception	IllegalStateException		���[�h��ԈȊO�̃{�[�h���w�肳�ꂽ�ꍇ
	 */
	public static float countApproximateNSWinners(OptimizedBoard b) {
		if (b.trickCount[b.tricks] != 0)
			throw new IllegalStateException("���[�h��ԂłȂ��Ǝg���܂���");
		
		clearCountCache();
		int leader = b.leader[b.tricks];
		
		float leaderQuickTricks = countApproximateWinners(b, leader);
		float oppQT1 = countApproximateWinners(b, (leader+1)%4 );
		float oppQT2 = countApproximateWinners(b, (leader+3)%4 );
		float oppQuickTricks = ( oppQT1 + oppQT2 ) / 2f;
		
		float leftTricks = 13f - (float)b.tricks;
		float leaderTricks;
		if (leaderQuickTricks + oppQuickTricks > leftTricks) {
			leaderTricks = leaderQuickTricks;
			if (leaderTricks > leftTricks) leaderTricks = leftTricks;
		} else {
			leaderTricks = (leaderQuickTricks + (leftTricks - oppQuickTricks) ) / 2;
		}
		
		if ((leader % 2) == 0) return leaderTricks + (float)b.nsWins;
		return leftTricks - leaderTricks + (float)b.nsWins;
	}
	
	public static float countApproximateWinners(OptimizedBoard b, int seat) {
		float result = 0f;
		for (int suit = 0; suit < 4; suit++) {
			result += (float)countQuickWinnersInSuit(b, suit, seat);
		}
		return result;
	}
	
	/**
	 * ����X�[�g�ł̂m�r���� quick winner ���̊T�Z���s���܂��B
	 *
	 * @param		b		�]���Ώۂ� board
	 * @param		suit	�X�[�g(0 ���N���u�ł��� Optimized �t�^�ɂ��)
	 * @param		seat	���S�Ƃ��čl�������(�ʏ� leader ��z��)
	 */
	private static int countQuickWinnersInSuit(OptimizedBoard b, int suit, int seat) {
		
		int lead = seat; //b.leader[b.tricks];	// leader �̍��Ȓ萔
		int pard = (lead + 2) % 4;	// leader �̃p�[�g�i�[�̍��Ȓ萔
		
		// �܂��̓X�[�g�̒����𒲂ׁAleaderLength�Ɋi�[����B
		// ���łɂ��̃X�[�g�̃X�^�[�g�C���f�b�N�X�� leaderStartIndex �Ɋo����
		int leaderLength = 0;		// ���[�_�[�̂��̃X�[�g�̒���
		int lowestCardOfLeader	= -1;
		int highestCardOfLeader	= -1;
		int leaderStartIndex = -1;
		for (int i = 0; i < b.handCount[lead]; i++) {
			if ( (b.hand[lead][i] / 14) != suit ) continue;	// �֌W�Ȃ��X�[�g�͏��O
			lowestCardOfLeader = b.hand[lead][i];
			if (highestCardOfLeader == -1) {
				highestCardOfLeader = b.hand[lead][i];
				leaderStartIndex = i;
			}
			leaderLength++;
		}
		if (leaderLength == 0) return 0;
		
		// �p�[�g�i�[�ɂ��Ă��X�[�g�̒����𒲂ׂ�
		int partnerLength = 0;
		int lowestCardOfPard	= -1;
		int highestCardOfPard	= -1;
		int partnerStartIndex	= -1;
		for (int i = 0; i < b.handCount[pard]; i++) {
			if ( (b.hand[pard][i] / 14) != suit ) continue;	// �֌W�Ȃ��X�[�g�͏��O
			lowestCardOfPard = b.hand[pard][i];
			if (highestCardOfPard == -1) {
				highestCardOfPard = b.hand[pard][i];
				partnerStartIndex = i;
			}
			partnerLength++;
		}
		
		// longerLength, shorterLength, lowestCardOfShorterSuit, highestCardOfLongerSuit
		// �Ɋi�[����
		int longerLength, shorterLength, lowestCardOfShorterSuit, highestCardOfLongerSuit;
		if (leaderLength >= partnerLength) {
			longerLength	= leaderLength;
			shorterLength	= partnerLength;
			lowestCardOfShorterSuit = lowestCardOfPard;
			highestCardOfLongerSuit = highestCardOfLeader;
		} else {
			longerLength	= partnerLength;
			shorterLength	= leaderLength;
			lowestCardOfShorterSuit = lowestCardOfLeader;
			highestCardOfLongerSuit = highestCardOfPard;
		}
		
		// �E�B�i�[�̐����J�E���g����(totalWinners �ւ̊i�[)
		int cheapestWinner	= -1;
		int totalWinners	= 0;		// �E�B�i�[�̐�
		if (leaderStartIndex == -1) leaderStartIndex = b.handCount[lead];
		if (partnerStartIndex == -1) partnerStartIndex = b.handCount[pard];
		
		for (int i = suit * 14 + 12; i >= suit * 14; i--) {
			if (b.isPlayed[i]) continue;
			// i �ɂ� winner �̃J�[�h�萔�������Ă���
			if ( (leaderStartIndex < b.handCount[lead])
					&&(b.hand[lead][leaderStartIndex] == i) ) { // ���[�_�[�� winner �������Ă���
				totalWinners++;
				leaderStartIndex++;
				cheapestWinner = i;
			} else if ( (partnerStartIndex < b.handCount[pard])
					&&(b.hand[pard][partnerStartIndex] == i) ) { // �p�[�g�i�[�� winner �������Ă���
				totalWinners++;
				partnerStartIndex++;
				cheapestWinner = i;
			} else {	// �ǂ����������Ă��Ȃ�
				break;
			}
		}
		
		boolean lowestCardOfShorterSuitIsWinner = (cheapestWinner == lowestCardOfShorterSuit);
		
		//
		// �@���S�Ƀu���b�N���Ă���ꍇ
		//
		if ( (shorterLength > 0)
				&&(lowestCardOfShorterSuitIsWinner)
				&&( (lowestCardOfShorterSuit%14)>(highestCardOfLongerSuit%14) ) ) {
			if (suit+1 != b.trump) {
				return adjustForRuff(b, suit, seat, shorterLength);
			} else {
				// suit �͐؂�D�X�[�g�ł��邽�߁A���t�ɂ��C���͍s��Ȃ�
				// return min(longerLength, totalWinners);
				if (totalWinners > longerLength) return adjustForLongTrump(b, seat, longerLength, longerLength);
				else return adjustForLongTrump(b, seat, totalWinners, longerLength);
			}
		}
		
		//
		// �A���S�ɂ̓u���b�N���Ă��Ȃ��ꍇ
		//   �G�X�^�u���b�V���ŃE�B�i�[��������\��������
		//
		if (shorterLength == 0)
			return adjustForEstablishment(b, suit, seat, totalWinners, longerLength);
		int tempxs;
		if (lowestCardOfShorterSuitIsWinner) {
			if (totalWinners-1 > longerLength) tempxs = longerLength;
			else tempxs = totalWinners - 1;
		} else {
			if (totalWinners > longerLength) tempxs = longerLength;
			else tempxs = totalWinners;
		}
		if (suit+1 != b.trump)
			return adjustForEstablishment(b, suit, seat, tempxs, longerLength);
		else
			return adjustForLongTrump(b, seat, tempxs, longerLength);
	}
	
	/**
	 * �G�X�^�u���b�V���ɂ���đ������g���b�N��Ԃ��B
	 * (xs) �� (�e�I�|�[�l���g�̂��̃X�[�g�̖���)
	 * �ł������ꍇ�A�G�X�^�u���b�V�����AlongerLength ��ԋp����B�����łȂ��ꍇ�A
	 * xs ��ԋp����B
	 * ����ɁA���t�ɂ��␳(adjustForRuff)���s���B
	 */
	private static int adjustForEstablishment(
							OptimizedBoard b,
							int suit,
							int seat,
							int xs,
							int longerLength) {
		int opp1 = (seat + 1) % 4;
		int opp2 = (seat + 3) % 4;
		
		// ����
		if ( (xs >= countSuit(b, suit, opp1))&&(xs >= countSuit(b, suit, opp2)) ) {
				return adjustForRuff(b, suit, seat, longerLength);
		}
		return adjustForRuff(b, suit, seat, xs);
	}
	
	/**
	 * �I�|�[�l���g�̃��t�ɂ���ăg���b�N�������炷�C�����s���B
	 * 
	 */
	private static int adjustForRuff(
							OptimizedBoard b,
							int suit,
							int seat,
							int xs) {
		if (b.trump == Bid.NO_TRUMP) return xs;
		if (b.trump == (suit + 1)) return xs;
		
		// ���t�ɂ��␳��������
		int opp1 = (seat + 1)%4;
		
		if (countSuit(b, b.trump - 1, opp1) > 0) { // �g�����v�������Ă���
			int cnt = countSuit(b, suit, opp1);
			if (xs > cnt) xs = cnt;
		}
		
		int opp2 = (seat + 3)%4;
		
		if (countSuit(b, b.trump - 1, opp2) > 0) {
			int cnt = countSuit(b, suit, opp2);
			if (xs > cnt) xs = cnt;
		}
		return xs;
	}
	
	/**
	 * �����g�����v�X�[�g�ɂ��g���b�N�����Z���܂��B
	 */
	private static int adjustForLongTrump(
							OptimizedBoard b,
							int seat,
							int xs,
							int longerLength) {
		int opp1Len = countSuit(b, b.trump - 1, (seat + 1)%4);
		if (xs <= opp1Len) longerLength -= (opp1Len - xs);
		int opp2Len = countSuit(b, b.trump - 1, (seat + 3)%4);
		if (xs <= opp2Len) longerLength -= (opp2Len - xs);
		
		if (longerLength > xs) return longerLength; // ������ꍇ�̂ݓK�p
		return xs;
	}
	
	/**
	 * ����̃n���h�̎w��X�[�g�̖������J�E���g���܂��B
	 * �ŏ��� OptimizedBoard �̏�Ԃ��ς�����Ƃ��ɁAclearCountCache() ������K�v������܂��B
	 *
	 * @param		suit		Optimized Board �t�^�̒l���w�肵�܂�
	 */
	// countSuit() �Ŏg�p����J�E���g�l�̃L���b�V���ł�
	// ������x���ʂ������܂�A�Ή����y�Ȃ��ߎ������Ă��܂�
	private static int[][]			valueCache	= new int[4][4];
	
	private static int countSuit(OptimizedBoard b, int suit, int seat) {
		// �L���b�V���ɓ����Ă���΂����Ԃ�
		if (valueCache[suit][seat] >= 0) {
			return valueCache[suit][seat];
		}
		
		// �J�E���g����
		int count = 0;
		for (int i = 0; i < b.handCount[seat]; i++) {
			if ( (b.hand[seat][i] / 14) == suit ) count++;
		}
		
		valueCache[suit][seat] = count;
		
		return count;
	}
	
	private static void clearCountCache() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				valueCache[i][j] = -1;
			}
		}
	}
}

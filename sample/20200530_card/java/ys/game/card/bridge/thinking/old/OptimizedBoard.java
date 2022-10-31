package ys.game.card.bridge.thinking;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.bridge.Board;
import ys.game.card.bridge.Trick;
import ys.game.card.bridge.IllegalStatusException;

/**
 * ��ǂ݃��[�`���p�� Board(PlayHistory) �̖���������I�u�W�F�N�g�ŁA
 * play, undo �������Ɏ��s����܂��B
 * �{�N���X�̓����ł́A�J�[�h�𐮐��^�ŕێ����܂��B���ۂ� Card �Ƃ̑Ή��́A
 * Card.SPADE(=4), Card.HEART(=3), ..
 * �t�F�C�X�� Ace(=1), 2, 3, 4, ... ,T(=10), J(=11), .. , K(=13) �Ƃ����Ƃ��ɁA
 * �܂��A�t�F�C�X�� Ace �� 14 �ɑΉ������܂��B�����āA�J�[�h�萔�����̂悤��
 * �Z�o���܂��B<BR><BR>
 * (�J�[�h�萔) �� ( suit - 1 )�~�P�S�{( value - 2 )    <BR><BR>
 * 14 ���悶�Ă��邽�߁AC2(=0), ... ,CA(=12), D2(=14),...
 * �̂悤�ɃX�[�g�Ԃł́A�������P�����܂��B����́A�A������J�[�h�̔����
 * �����ɍs�����߂ɂ��Ă��܂��B
 *
 * @version		a-release		20 October, 2002
 * @author		Yusuke Sasaki
 */
public class OptimizedBoard {
	static final String[] SUIT = new String[] { "C", "D", "H", "S", "*" };
	static final String[] VALUE =
			new String[] { "2","3","4","5","6","7","8","9","T","J","Q","K","A"};

	// ���Ȓ萔
	public static final int NORTH		= 0;
	public static final int EAST		= 1;
	public static final int SOUTH		= 2;
	public static final int WEST		= 3;
	// �X�[�g�萔
	public static final int NO_TRUMP	= 5;
	public static final int SPADE		= 4;
	public static final int HEART		= 3;
	public static final int DIAMOND		= 2;
	public static final int CLUB		= 1;
	
	// �{�[�h�̏��(�Œ��)
	
	/**
	 * ���ꂼ��̍��Ȃ̃n���h������ێ����܂��B
	 */
	public int[]		handCount;
	/**
	 * �n���h�̓��e��ێ�����B�Y������ [seat][0-12]�ƂȂ�B
	 * 0-12�́A�O�l�߂Ŋi�[����B
	 * value �ɂ��ẮA2,3,4, ... ,11(=J),12(=Q),13(=K),14(=A) ���i�[�����
	 * �J�[�h������킷�萔�́A���̎��Ōv�Z����<BR><BR>
	 * (suit-1)*14 + (value-2)<BR><BR>
	 * 14���悶�Ă���̂́A�A�����Ă��邱�Ƃ̔���������ɍs�����߂ł���
	 * �i�[�́A�������̂��ߑ傫����(�~��)�Ɋi�[����
	 */
	public int[][]		hand;
	
	/** �����g���b�N��(=���݃v���C���̓Y����) */
	public int			tricks;	// ���������g���b�N��(=���ݐi�s���̓Y����)
	
	/**
	 * �v���C������ێ�����B�Y������ [0-12][0-3] �ƂȂ�B
	 */
	public int[]		trickCount;
	
	/** �e�g���b�N�̓��e���i�[���܂��B�Y�����́A[tricks][0-3] �ƂȂ�܂��B */
	public int[][]		trick;
	
	/** �e�g���b�N�� leader �����Ȓ萔�Ŋi�[���܂��B */
	public int[]		leader;
	
	/** �e�g���b�N�� winner ���i�[���܂��B */
	public int[]		winner;
	
	/**
	 * �g�����v�X�[�g(Card.SPADE �Ȃ�)���i�[���܂��B
	 */
	public int		trump;
	
	/**
	 * ���łɃv���C���ꂽ���ǂ����̃t���O
	 * �Y������ (suit-1)*14+(value-2) �Ōv�Z�����B
	 */
	public boolean[] isPlayed;
	
	/**
	 * n �Ԗڂ̃v���C�̃v���C���̃��X�g���i�[���邽�߂̗̈�ł��B
	 * �Y�����́A[n-1][0,1, ... , 14-n] �ƂȂ�܂�
	 */
	public int[][]		playOptions;
	
/*-------------
 * Constructor
 */
	/**
	 * �w�肳�ꂽ Board �ɑΉ����� OptimizedBoard �𐶐����܂��B
	 * �w�� Board �́APLAYING / OPENING �̃X�e�[�^�X�łȂ���΂Ȃ�܂���B
	 *
	 * @exception	IllegalStatusException �w�� Board �̃X�e�[�^�X�ُ�
	 */
	public OptimizedBoard(Board board) {
		int s = board.getStatus();
		if ( (s != Board.PLAYING)&&(s != Board.OPENING) )
			throw new IllegalStatusException("�w�肳�ꂽ Board �́AOPENING �܂��� PLAYING �X�e�[�^�X�łȂ���΂Ȃ�܂���");
		handCount	= new int[4];
		hand		= new int[4][13];
		
		tricks		= 0;
		trickCount	= new int[13];
		trick		= new int[13][4];
		leader		= new int[13];
		winner		= new int[13];
		
		isPlayed	= new boolean[56];
		for (int i = 0; i < 56; i++)	isPlayed[i] = false;
		
		playOptions	= new int[52][];
		for (int i = 0; i < 52; i++)
			playOptions[i] = new int[13-(i/4)];
		
		trump = board.getTrump();
		
		// �n���h��Ԃ̃R�s�[
		for (int seat = 0; seat < 4; seat++) {
			Packet h = board.getHand(seat);
			handCount[seat] = h.size();
			for (int n = 0; n < h.size(); n++) {
				Card c = h.peek(n);
				int value = c.getValue();
				if (value == Card.ACE) value = 14;
				hand[seat][n] = (c.getSuit() - 1)*14+(value-2);
			}
			// �n���h���e�̃\�[�g
			// �P�񂵂����Ȃ��̂ŁA�蔲��(bubble sort)
			for (int i = 0; i < handCount[seat] - 1; i++) {
				for (int j = i + 1; j < handCount[seat]; j++) {
					if (hand[seat][i] < hand[seat][j]) {
						int tmp = hand[seat][i];
						hand[seat][i] = hand[seat][j];
						hand[seat][j] = tmp;
					}
				}
			}
		}
		
		// �g���b�N��Ԃ̃R�s�[
		Trick[] tr = board.getAllTricks();
		for (int i = 0; i < tr.length; i++) {
			if (tr[i] == null) break;
			if (tr[i].size() == 4) tricks++;
			trickCount[i] = tr[i].size();
			for (int j = 0; j < tr[i].size(); j++) {
				Card c = tr[i].peek(j);
				int value = c.getValue();
				if (value == Card.ACE) value = 14;
				int index = (c.getSuit() - 1)*14+(value-2);
				trick[i][j] = index;
				isPlayed[index] = true;
			}
			leader[i] = tr[i].getLeader();
			winner[i] = tr[i].getWinner();
		}
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
	public int[] listOptions() {
		int[] result = listPlayableCards();
		
		// �}����(1)
		// ���i�̃J�[�h�����O����
		int lastEntried = result[0];
		for (int i = 1; i < result.length; i++) {
			int next = result[i];
			if (next == -1) break;
			// lastEntried �Ɠ��i���ǂ���
			// �u���i�v�Ƃ́A�Q���̃J�[�h�̂�������o���Ă�����̌��ʂƂȂ邱��
			// lastEntried, next �̊Ԃ̃J�[�h�����ׂăv���C�ς݂ł���ꍇ�A�u���i�v
			//
			// (1) lastEntried, next �Ƃ��ɓ����n���h�ɓ����Ă���
			// (2) result �ɂ͍~���ɃJ�[�h���܂܂�Ă���
			// 
			int j;
			for (j = lastEntried - 1; j > next; j--) {
				if (!isPlayed[j]) break;
			}
			lastEntried = next;
			if (j == next) {
				// ���i�B�߂�B���[�v��������Ɖ��悤�ɂ���B
				System.arraycopy(result, i+1, result, i, result.length-i-1);
				result[result.length - 1] = -1; // ���łɑO�� -1 �������Ă��邩������Ȃ���
				i--;
			}
		}
		
		// �}��(2)
		// �e�X�[�g���ƂɂW�ȉ��̃J�[�h�𓯈ꎋ����
/*		int lastSuitv = -1;
		boolean eightAppeared = false;
		for (int i = 0; i < result.length; i++) {
			if (result[i] == -1) break;
			if (lastSuitv != (result[i]/14)) {
				lastSuitv = (result[i]/14);
				eightAppeared = false;
			}
			if ((result[i]%14) < 9) {
				if (eightAppeared) {
					// �߂�
					System.arraycopy(result, i+1, result, i, result.length-i-1);
					result[result.length - 1] = -1; // ���łɑO�� -1 �������Ă��邩������Ȃ���
					i--;
				} else {
					eightAppeared = true;
				}
			}
		}
*/		
		return result;
	}
	
	/**
	 * ���݃v���C����n���h�̒��ŁA�w��J�[�h�Ɠ��i�̃J�[�h�𒊏o���܂��B
	 * �x�X�g�v���C�̃��X�g���o���Ƃ��ɁA���i�̃J�[�h���܂߂����ۂ̃��X�g��
	 * �߂��Ƃ��Ɏg�p���邽�߂̊֐��ł��B
	 * �{���\�b�h�͓��ɍ����ɂȂ�悤�ɂ͂����Ă��Ȃ����߁A���[�v�̊O����
	 * �ĂԕK�v������܂��B
	 *
	 * @param		card	���i�̃J�[�h��T�������J�[�h
	 * @return		���i�̃J�[�h
	 */
	public int[] getEqualCards(int[] cards, int cardCount) {
		int		seat	= (leader[tricks]+trickCount[tricks])%4;
		
		// �v���C����Ă��Ȃ��J�[�h = 0
		// �����Ă���J�[�h         = 1
		// �w�肳�ꂽ�J�[�h			= 2
		// �v���C���ꂽ�J�[�h       = 3
		// 0 �� delimiter �Ƃ��āAtoken ����؂�A2 ���܂܂�Ă��� token ��
		// 1 �� 2 �ɕύX����B2 �ƂȂ��Ă���J�[�h��ԋp����
		int[] tmp = new int[56];
		
		// �v���C���ꂽ���ǂ����Ńt���O��ݒ�
		for (int i = 0; i < 56; i++) {
			if (isPlayed[i]) tmp[i] = 3;
			else tmp[i] = 0;
		}
		// �����Ă���J�[�h�̐ݒ�
		for (int i = 0; i < handCount[seat]; i++) {
			tmp[hand[seat][i]] = 1;
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
	public int[] listPlayableCards() {
		int seat = (leader[tricks]+trickCount[tricks])%4;
		int playOptionsIndex = 4*tricks+trickCount[tricks];
		
		if (trickCount[tricks] != 0) {
			int leadSuit = trick[tricks][0]/14;
			int options = 0;
			
			// �X�[�g�t�H���[��T��
			for (int i = 0; i < handCount[seat]; i++) {
				if ((hand[seat][i]/14)==leadSuit) {
					// ������
					playOptions[playOptionsIndex][options++] = hand[seat][i];
				}
			}
			if (options > 0) {
				if (options < 13-tricks) playOptions[playOptionsIndex][options] = -1;
				return playOptions[playOptionsIndex];
			}
		}
		// ���[�h�̏ꍇ�A�܂��̓X�[�g�t�H���[�ł��Ȃ� ... ���ׂẴJ�[�h���o����
		System.arraycopy(hand[seat], 0, playOptions[playOptionsIndex], 0, handCount[seat]);
		// -1 ��}������K�v�͂Ȃ�
		
		return playOptions[playOptionsIndex];
	}
	
	/**
	 * �P��i�߂܂��B�X�[�g�t�H���[�̃`�F�b�N�͍s���܂���B
	 * �c��v���C����ԋp���܂��B
	 *
	 * @param		c	�v���C����J�[�h
	 * @return		�c��v���C��
	 */
	public int play(Card c) {
		int suit	= c.getSuit();
		int value	= c.getValue();
		if (value == Card.ACE) value = 14;
		
		return play( (suit-1)*14+(value-2) );
	}
	
	/**
	 * �w�肵���J�[�h�萔(�{�N���X�œƎ���`)���v���C�����Ƃ��āA
	 * �{�[�h�̏�Ԃ��X�V���܂��B�g���b�N���I��������A�E�B�i�[�̐ݒ�A���̃��[�_�[
	 * �̐ݒ�Ȃǂ̓�����Ԃ��X�V���܂��B�c��v���C�������ʂƂ��ĕԋp���܂��B
	 * �X�[�g�t�H���[�`�F�b�N�͍s���܂���B
	 * 
	 * @param		c	�v���C����J�[�h
	 * @exception	ArrayIndexOutOfBoundsException
	 *				�v���C���̐l�������Ă��Ȃ��J�[�h���v���C���悤�Ƃ���
	 *              �����I�����Ă����ԂŃv���C���悤�Ƃ���
	 */
	public int play(int c) {
		//
		// 1. �n���h����w��J�[�h��draw
		//    draw �����ɁA�t���O�� draw �������Ƃ��������@�����邪�A�t���O�`�F�b�N��
		//    �����鈫�e��������̂ŁAarraycopy �ɂ��l�߂��s�����ƂƂ��Ă���
		//
		int seat = (leader[tricks] + trickCount[tricks])%4;
		int n;
		
		// draw ����J�[�h����������
		for (n = 0; n < handCount[seat]; n++) {
			if (c == hand[seat][n]) break;
		}
		
		if (n == handCount[seat])
			throw new ArrayIndexOutOfBoundsException("�v���C����J�[�h " + toString(c) + "���܂�ł��܂���");
		
		// arraycopy ���g����draw
		handCount[seat]--;
		System.arraycopy(hand[seat], n+1, hand[seat], n, handCount[seat]-n);
		// �����ւ� pad �͓���Ȃ��ō���������(���̂܂܂̒l�����邱�ƂɂȂ�)
		
		//
		// 2. �g���b�N�Ɏw��J�[�h�� add
		//
		trick[tricks][trickCount[tricks]++] = c;
		
		//
		// 3. isPlayed �t���O�̍X�V
		//
		isPlayed[c] = true;
		
		//
		// 4. �g���b�N���I�������ꍇ�̏���
		//    winner ���� leader �̐ݒ�
		//
		if (trickCount[tricks] == 4) {
			// ���[�h�������Ă���Ƃ���
			int win		= leader[tricks];
			int winCard	= trick[tricks][0];
			int winCardSuitv = (winCard/14)+1;
			
			for (int i = 1; i < 4; i++) {
				int card = trick[tricks][i];
				if (winCardSuitv == trump) {
					// �g�����v�̏ꍇ�A�X�[�g�t�H���[�ő傫���ꍇ�̂ݏ��Ă�
					if (trump == (card/14+1) ) {
						// �X�[�g�t�H���[�������Ƃ��A�召��r�Ō��肷��
						if (winCard < card) {
							win = (leader[tricks]+i)%4;
							winCard = card;
							// winCardSuitv �͕ύX�Ȃ�
						}
					}
				} else {
					// �g�����v�łȂ��ꍇ�A�X�[�g�t�H���[�ő傫�����A�g�����v�̏ꍇ�ɏ���
					if (trump == (card/14+1)) {
						// �g�����v�̏ꍇ�A�������ɏ���
						win = (leader[tricks]+i)%4;
						winCard = card;
						winCardSuitv = trump;
					} else if (winCardSuitv == (card/14+1)) {
						// �X�[�g�t�H���[�̏ꍇ
						if (winCard < card) {
							win = (leader[tricks]+i)%4;
							winCard = card;
							// winCardSuitv �͕ύX�Ȃ�
						}
					}
				}
			}
			winner[tricks] = win;
			tricks++;
			if (tricks == 13) return 0;
			leader[tricks] = win;
		}
		return 52-tricks*4-trickCount[tricks];
	}
	
	/**
	 * �P��߂��܂��B
	 */
	public void undo() {
		//
		// 1. �Ώۃg���b�N��������
		//
		if ((tricks == 13)||(trickCount[tricks] == 0)) {
			tricks--;
		}
		
		//
		// 2. �g���b�N����P���J�[�h�����炷
		//    trickCount �����炷�̂�
		//
		int card = trick[tricks][--trickCount[tricks]];
		
		//
		// 3. �n���h�ɃJ�[�h��߂�
		//    �~���ɂȂ��Ă���̂ŁA�K���Ȉʒu�ɑ}������
		//
		int seat = (leader[tricks]+trickCount[tricks])%4;
		int n;
		for (n = 0; n < handCount[seat]; n++) {
			if (card > hand[seat][n]) break;
		}
		System.arraycopy(hand[seat], n, hand[seat], n+1, handCount[seat]-n);
		hand[seat][n] = card;
		handCount[seat]++;
		
		//
		// 4. �v���C�����t���O���Z�b�g
		//
		isPlayed[card] = false;
		
		//
		// 5. leader, winner �Ȃǂ̃N���A�͍s��Ȃ�
		//    tricks �Ŕ��f�ł��邽��
		//
	}
	
	/**
	 * ���݂܂ŏI�����Ă���g���b�N�ɂ��āA�m�r���̎�����g���b�N�����J�E���g���܂��B
	 *
	 * @return		�m�r���̃g���b�N��
	 */
	public int countNSWinners() {
		int result = 0;
		for (int i = 0; i < tricks; i++) {
			if ((winner[i]%2) == 0) result++;
		}
		return result;
	}
	
	/**
	 * �̂���S���ɂȂ����Ƃ��Ƀv���C�������� NS �� winner �����J�E���g���܂��B
	 * ��ǂ݃��[�`��������������ꍇ�Ɏg�p�ł��܂��B
	 * �c��S���łȂ��Ă��{���\�b�h�̓R�[���ł��܂����A���ʂ͕s��ł��B
	 *
	 * @return		�v���C�������������Ƃ��̂m�r���̃g���b�N��
	 */
	public int countNSWinnersLeavingLastTrick() {
		int result = 0;
		for (int i = 0; i < tricks; i++) {
			if ((winner[i]%2) == 0) result++;
		}
		
		// �Ō��winner�����ꂼ��̃n���h�����邱�ƂŌ���
		// ���[�h�������Ă���Ƃ���
		int win		= leader[tricks];
		int winCard	= hand[win][0];
		int winCardSuitv = (winCard/14)+1;
		
		for (int i = 1; i < 4; i++) {
			int c = hand[(leader[tricks]+i)%4][0];
			if (winCardSuitv == trump) {
				// �g�����v�̏ꍇ�A�X�[�g�t�H���[�ő傫���ꍇ�̂ݏ��Ă�
				if (trump == (c/14+1) ) {
					// �X�[�g�t�H���[�������Ƃ��A�召��r�Ō��肷��
					if (winCard < c) {
						win = (leader[tricks]+i)%4;
						winCard = c;
						// winCardSuitv �͕ύX�Ȃ�
					}
				}
			} else {
				// �g�����v�łȂ��ꍇ�A�X�[�g�t�H���[�ő傫�����A�g�����v�̏ꍇ�ɏ���
				if (trump == (c/14+1)) {
					// �g�����v�̏ꍇ�A�������ɏ���
					win = (leader[tricks]+i)%4;
					winCard = c;
					winCardSuitv = trump;
				} else if (winCardSuitv == (c/14+1)) {
					// �X�[�g�t�H���[�̏ꍇ
					if (winCard < c) {
						win = (leader[tricks]+i)%4;
						winCard = c;
						// winCardSuitv �͕ύX�Ȃ�
					}
				}
			}
		}
		if ((win%2) == 0) result++;
		
		return result;
	}
	
	/**
	 * ���݂̃g���b�N�̃��[�_�[�����Ȓ萔�Ŏ擾���܂��B
	 * �I����Ă��� Board �ɑ΂��Ė{���\�b�h���R�[������ƁA
	 * ArrayIndexOutOfBoundsException ���X���[����܂��B
	 *
	 * @return		���݂̃g���b�N�̃��[�_�[
	 */
	public int getLeader() {
		return leader[tricks];
	}
	
	/**
	 * ���݂܂łŊ������Ă���g���b�N�����擾���܂��B
	 *
	 * @return		�g���b�N��
	 */
	public int getTricks() {
		return tricks;
	}
	
	
	/**
	 * ���ݒN�̔ԂɂȂ��Ă��邩�����Ȓ萔�Ŏ擾���܂��B
	 */
	public int getTurn() {
		return (leader[tricks]+trickCount[tricks])%4;
	}
	
	public static String toString(int card) {
		return SUIT[card/14]+VALUE[card%14];
	}
}

package ys.game.card.bridge.ta;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.bridge.Board;
import ys.game.card.bridge.Trick;
import ys.game.card.bridge.IllegalStatusException;

/**
 * ��ǂ݂ɓ������� Board �I�ȃI�u�W�F�N�g
 * �����I�Ƀv���C�̏�Ԃ́A�J�[�h�̏�Ԃ��ω����邱�Ƃŕ\������B
 * int card[56] �����̏�Ԃ������ϐ��ł���B
 *
 */
public class OptimizedBoard {
	static final String[] SUIT_STR = new String[] { "C", "D", "H", "S", "*" };
	static final String[] VALUE_STR =
			new String[] { "2","3","4","5","6","7","8","9","T","J","Q","K","A", "_"};
	static final String[] SEAT_STR = new String[] { "N", "E", "S", "W", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
	
	static final int TRICK_MULTIPLICITY = 100;
	
	/**
	 * �J�[�h�̏�Ԃ������B
	 * ���ʂS�r�b�g�́A���L��(North=0, East=1, South=2, West=3)������킵�A
	 * �������������ʃr�b�g�̓v���C�ԍ�(1-52, 0�͖��v���C)�������B
	 * �Ȃ��A���ۂɂ���킷�J�[�h�̂Ȃ��Y����(14, 28, 42, 56)�̃J�[�h�ɂ� 15 ���i�[�����B
	 * �Y�����̓J�[�h�萔�ł���A�o�����[�ƃX�[�g������킷�P�̒l�ł���B
	 * �o�����[�́A2��0�ɁAA��12�ɑΉ������A�N���u��0, �X�y�[�h��3�ɑΉ�������B<br>
	 * (�J�[�h�萔)��(�o�����[)�{(�X�[�g)�~14 �ŋ��߂�B
	 */
	int[]	card;
	
	/**
	 * �v���C�ԍ�(1..52)����J�[�h�ԍ��𓾂�A�t�����p�C���f�b�N�X�B
	 */
	int[]	play;
	
	/**
	 * winner(����leader)�̍��Ȃ��i�[����B�Y�����́A0-13 �ƂȂ�B
	 */
	int[]	leader;
	
	/**
	 * ����܂ł̃v���C�J�E���g
	 * play() ���ĂԂƃJ�E���g�A�b�v����B
	 * Opening Lead ��Ԃł� 0 �ŁAOpening Lead ���v���C����� 1 �ɂȂ�B
	 * Scoring ��Ԃł� 52 �ƂȂ��Ă���B
	 */
	int		count;
	
	/**
	 * �g�����v�X�[�g(�N���u=0�A�X�y�[�h=3)
	 */
	int		trump;
	
	/**
	 * NS���̂Ƃ����g���b�N��
	 */
	int		nsWins;
	
	/**
	 * new �����Ȃ����߂� depth ���ƂɎg�p���� BoardStatistics �̃o�b�t�@
	 */
	BoardStatistics[]	statBuffer;
	
	/**
	 * �Ō�̂P�g���b�N�Z�o�p�̍������o�b�t�@�ł�
	 */
	int[]	lastPlayBuffer;
	
	/**
	 * �T�Z���[�h�Ɉڂ� depth �̎w��ł��B
	 * ���� depth �𒴂����ŏ��̃��[�h��ԂŊT�Z���s���܂��B
	 */
	int		depthBorder;
	
	int[]	bestPlay;
	//
	// �ȍ~�A�T�Z�A���S���Y���p
	//
	private static final int SEAT = 4;
	private static final int SUIT = 4;
	private static final int NS_OR_EW = 2;
	private static final int CARDS = 56;
	
	public	int[][] suitCount		= new int[SEAT][SUIT]; // �������i�[�����
	public	int[][] totalWinners	= new int[NS_OR_EW][SUIT]; // ns, ew �� Winner�̐�
	public	int[][] longerLength	= new int[NS_OR_EW][SUIT];
	public	int[][] shorterLength	= new int[NS_OR_EW][SUIT];
	public	int[][]	lowestCard		= new int[SEAT][SUIT];
	public	int[][]	highestCard		= new int[SEAT][SUIT];
	public	int[][] lowestCardOfShorterSuit	= new int[NS_OR_EW][SUIT];
	public	int[][] highestCardOfLongerSuit	= new int[NS_OR_EW][SUIT];
	
	public boolean[] isWinner = new boolean[CARDS];
	public int		limitTricks; // calcPropData

/*-------------
 * Constructor
 */
	/**
	 * 
	 */
	public OptimizedBoard(Board board) {
		int s = board.getStatus();
		if ( (s != Board.PLAYING)&&(s != Board.OPENING) )
			throw new IllegalStatusException("�w�肳�ꂽ Board �́AOPENING �܂��� PLAYING �X�e�[�^�X�łȂ���΂Ȃ�܂���");
		card	= new int[56];
		for (int i = 13; i < 56; i+=14) {
			card[i] = 15;
		}
		play	= new int[53];
		leader	= new int[14];
		count	= 0;
		nsWins	= 0;
		statBuffer	= new BoardStatistics[52];
		for (int i = 0; i < 52; i++) {
			statBuffer[i] = new BoardStatistics();
		}
		lastPlayBuffer = new int[4];
		
		depthBorder		= 4;		// �T�Z�͎g�p���Ȃ�
		
		trump = board.getTrump() - 1; // (==4 when NoTrump)
		
		// �n���h��Ԃ̃R�s�[
		for (int seat = 0; seat < 4; seat++) {
			Packet h = board.getHand(seat);
			for (int n = 0; n < h.size(); n++) {
				card[getCardNumber(h.peek(n))] = seat;
			}
		}
		
		// �g���b�N��Ԃ̃R�s�[
		Trick[] tr = board.getAllTricks();
		nsWins = 0;
		for (int i = 0; i < tr.length; i++) {
			if (tr[i] == null) break;
			for (int j = 0; j < tr[i].size(); j++) {
				Card c = tr[i].peek(j);
				int value = c.getValue();
				if (value == Card.ACE) value = 14;
				int index = (c.getSuit() - 1)*14+(value-2);
				card[index] = (i * 4 + j + 1) << 4;
				card[index] += (tr[i].getLeader() + j) % 4;
				play[i * 4 + j + 1] = index;
				count++;
			}
			leader[i] = tr[i].getLeader();
			if (!tr[i].isFinished()) break;
			leader[i+1] = tr[i].getWinner();
			if ((leader[i + 1] & 1) == 0) nsWins++;
		}
		
		bestPlay = new int[14];
	}
	
/*------------------
 * instance methods
 */
	/**
	 * ���ۂɂ͐�ǂ݃��[�`���̒��ŃC�����C���ɏ������ƂɂȂ邾�낤���\�b�h
	 *
	 * @param		playedCard		�v���C���ꂽ�J�[�h�萔
	 * @return		���̃v���C�̃v���C�J�E���g(1..52) 52���Ō�̃v���C�ƂȂ�
	 */
	public final int play(int playedCard) {
		count++;
		card[playedCard] += (count << 4);
		play[count] = playedCard;
		
		if ( (count%4) != 0 ) return count;
		
		// �P�g���b�N����
		
		// �E�B�i�[�̌���Aleader �ɐݒ肷��
		int		winner	= leader[count/4 - 1];
		int		winCard = play[count - 3];
		
		for (int i = -2; i <= 0; i++) {
			int card = play[count + i];
			if ( (winCard/14) == trump ) {
				if ( (card/14) == trump ) {
					if (card > winCard) {
						// winCard �� trump �̏ꍇ�́A�傫���g�����v���o���Ȃ���Ώ����Ȃ�
						winCard	= card;
						winner	= (leader[count/4 - 1] + i + 3) % 4;
					}
				}
			} else if ( (card/14) == trump ) {
				// �͂��߂ďo�� trump �͕K������
				winCard	= card;
				winner	= (leader[count/4 - 1] + i + 3) % 4;
			} else if ( (winCard / 14) == (card / 14) ) {
				if (card > winCard) {
					// �X�[�g�t�H���[�̏ꍇ�A�傫���o�����[�Ȃ珟��
					winCard	= card;
					winner	= (leader[count/4 - 1] + i + 3) % 4;
				}
			}
		}
		
//		2003/6/9 �R�����g�A�E�g �e���Ȃ����͖��e�X�g
//		for (int i = 0; i < 56; i++) isWinner[i] = false;
		
		// ���� leader ��ݒ肷��B
		leader[count/4]	= winner;
		
		if ((winner & 1) == 0) nsWins++;
		
		return count;
	}
	
	/**
	 * play() ���ĂԑO�̏�Ԃɖ߂��܂��B
	 * leader[] �̒l�̃��Z�b�g�͂Ƃ肠�����s���Ă��܂���B
	 * �܂�A���݈ȍ~��leader[] �̒l�͕s��ł��B
	 * �܂��Aplay[] �ɂ��Ă����Z�b�g���Ă��܂���B
	 */
	public final void undo() {
		if ( (count%4) == 0 ) {
			if ((leader[count/4] & 1) == 0) nsWins--;
		}
		card[play[count]]	&= 0x0F;
		count--;
		
		// leader �̃��Z�b�g�͍s��Ȃ��B(������)
	}
	
	/**
	 * �����ǂ݂��s������ݒ肷��B���I�ɕύX�\�B
	 * �O��ݒ肷��ƁA�͂��߂̃��[�h��Ԃ܂Ő�ǂ݂��s���B
	 * �P��ݒ肷��ƁA�P��ǂ݁A�Â��͂��߂̃��[�h��Ԃ܂Ő�ǂ݂��s���B
	 * 48�ȏ��ݒ肷��ƁA�Ō�܂œǂ݂���B
	 */
	public void setDepthBorder(int depthBorder) {
		this.depthBorder = depthBorder;
	}
	
/*------------
 * ��ǂݖ{��
 */
	/**
	 * ���̈��B��ǂ݃��[�`���Ƃقړ��������A�őP��̌����L�^����_���قȂ�
	 */
	public int[] getBestPlay() {
		// �v���C����T�����[�v
		int turn = (leader[count/4] + count) % 4;
		boolean nsside = ( (turn & 1) == 0 );
		
		//
		// �ċA�I����
		//
		int countAtLead		= (count/4)*4+1;
		
		// �X�[�g�t�H���[�ł��邩�ǂ����̔���
		// ���݁A�X�[�g�t�H���[�ł��邩�ǂ����̌����Ǝ��ۂɃv���C���郋�[�v��
		// �Q���܂킵�Ă��邪�A�C�����C���W�J���邱�ƂłP�ɂł���
		int	startIndex	= 0;
		int	endIndex	= 55;
		if ( (count % 4) != 0 ) {
			int suit	= play[countAtLead] / 14;
			int suit2	= suit * 14;
			for (int c = suit2; c < suit2 + 13; c++) {
				if (card[c] == turn) {
					// �X�[�g�t�H���[�ł���
					startIndex	= c;
					endIndex	= suit2 + 13;
					break;
				}
			}
		}
		
		boolean lastEntried	= false; // ���i�J�[�h���������߂̕ϐ�
		int bestPlayCount	=  0;
		int bestTricks		= -TRICK_MULTIPLICITY;
		
		int countAtLead2	= countAtLead << 4;
		
		// ���[�h�̏ꍇ�A�܂��̓X�[�g�t�H���[�ł��Ȃ��ꍇ(�Ȃ�ł��o����)
		// �{�[�h���I���ɋ߂Â��ɂ�Ė��ʂ������Ȃ�B....�ᑬ��
		for (int c = startIndex; c < endIndex; c++) {
			int tmp = card[c];
			if ((tmp > 15)&&(tmp < countAtLead2)) continue; // �v���C���ꂽ�J�[�h�͖���
			
			if (tmp == turn) { // ����ɏo�Ă���J�[�h�͂܂��v���C����Ă��Ȃ��ƍl����
				if (lastEntried) continue;
				lastEntried = true;
				// �����Ă��āA�v���C����Ă��Ȃ� ... c ���o����
				play(c);
				
				BoardStatistics stats = calculateImpl(0, 14 * TRICK_MULTIPLICITY, true);
System.out.println(getCardString(c) + " �̃{�[�h���v���");
System.out.println(stats);
				
				// best play ���ǂ����̔���AbestPlayCount, bestTricks �̍X�V
				int finalTricks;
				if (nsside) finalTricks = stats.finalNSTricks;
				else finalTricks = 13 * TRICK_MULTIPLICITY - stats.finalNSTricks;
				
				if (bestTricks < finalTricks) {
					bestTricks		= finalTricks;
					bestPlayCount	= 1;
					bestPlay[0]		= c;
				} else if (bestTricks == finalTricks) {
					bestPlay[bestPlayCount++] = c;
				}
				
				undo();
			} else {
				lastEntried = false; // ���̐l�������Ă��� or �f���~�^... �V�[�P���X���؂ꂽ
			}
			// ������̂́A���v���C���̃J�[�h�̂�
		}
		bestPlay[bestPlayCount] = -1;
		return bestPlay;
	}
	
/*
 * ��ǂ�(���ڈȍ~)
 */
	public final BoardStatistics calculate() {
		// ���p�����[�^�� 14 �̂Ƃ��A��O�p�����[�^�͕s��
		return calculateImpl(0, 14 * TRICK_MULTIPLICITY, true);
	}
	
	// border .. ��-���}���̂��߂�臒l
	private final BoardStatistics calculateImpl(int depth, int border, boolean borderNsside) {
		// ���ʃI�u�W�F�N�g�B�g���܂킷���ƂŁA�������B
		BoardStatistics result = statBuffer[depth];
		result.totalPlayCount = 0;
		
		// �v���C����T�����[�v
		int turn = (leader[count/4] + count) % 4;
		
		//
		boolean nsside = ( (turn & 1) == 0 );
		
		//
		// �A�[�@�̂͂���
		//
		if (count == 52) {
			// �ŏI�g���b�N�������ꍇ�̕ԋp
			result.totalPlayCount	= 1;
			result.bestPlayCount	= 1;
			result.bestPlayPaths	= 1;
			result.finalNSTricks	= nsWins * TRICK_MULTIPLICITY;
			
			return result;
		} else if (count == 48) {
			// �c��P�g���b�N�������ꍇ�̕ԋp
			result.totalPlayCount	= 1;
			result.bestPlayCount	= 1;
			result.bestPlayPaths	= 1;
			result.finalNSTricks	= nsWins * TRICK_MULTIPLICITY;
			
			// nsWins �����߂�
			
			// �S�����Ō�̂P�����������ׁAlastPlayBuffer �Ɋi�[����
			for (int i = 0; i < 55; i++) {
				if (card[i] < 4) {
					lastPlayBuffer[card[i]] = i;
				}
			}
			// �E�B�i�[�̌���Aleader �ɐݒ肷��
			int		winner	= leader[count/4];
			int		leaderSeat	= winner;
			int		winCard = lastPlayBuffer[leaderSeat];
			
			for (int i = 1; i < 4; i++) {
				int card = lastPlayBuffer[(leaderSeat + i) % 4];
				if ( (winCard/14) == trump ) {
					if ( (card/14) == trump ) {
						if (card > winCard) {
							// winCard �� trump �̏ꍇ�́A�傫���g�����v���o���Ȃ���Ώ����Ȃ�
							winCard	= card;
							winner	= (leaderSeat + i) % 4;
						}
					}
				} else if ( (card/14) == trump ) {
					// �͂��߂ďo�� trump �͕K������
					winCard	= card;
					winner	= (leaderSeat + i) % 4;
				} else if ( (winCard / 14) == (card / 14) ) {
					if (card > winCard) {
						// �X�[�g�t�H���[�̏ꍇ�A�傫���o�����[�Ȃ珟��
						winCard	= card;
						winner	= (leaderSeat + i) % 4;
					}
				}
			}
			
			if ((winner & 1) == 0) result.finalNSTricks += TRICK_MULTIPLICITY;
			
			return result;
		}
		
		//
		// depthBorder �𒴂��Ă��邩�H
		//
		if ((depth >= depthBorder)&&( (count%4) == 0 )) {
			// �����ŊT�Z���s��
			int tricks = calcApproximateTricks(); // ���݂̃��[�_�[���Ƃ��g���b�N��
			if ( (leader[count/4] % 2) == 0 ) {
				// ���[�_�[�� NS
				tricks += nsWins * TRICK_MULTIPLICITY;
			} else {
				// ���[�_�[�� EW
				tricks = (nsWins + 13 - (count/4)) * TRICK_MULTIPLICITY - tricks;
			}
			
			// �ŏI�g���b�N�������ꍇ�̕ԋp
			result.totalPlayCount	= 10;
			result.bestPlayCount	= 1;
			result.bestPlayPaths	= 1;
			result.finalNSTricks	= tricks;
			
			return result;
		}
		
		//
		// �ċA�I����
		//
		int countAtLead		= (count/4)*4+1;
		
		// �X�[�g�t�H���[�ł��邩�ǂ����̔���
		// ���݁A�X�[�g�t�H���[�ł��邩�ǂ����̌����Ǝ��ۂɃv���C���郋�[�v��
		// �Q���܂킵�Ă��邪�A�C�����C���W�J���邱�ƂłP�ɂł���
		int	startIndex	= 0;
		int	endIndex	= 55;
		if ( (count % 4) != 0 ) {
			int suit	= play[countAtLead] / 14;
			int suit2	= suit * 14;
			for (int c = suit2; c < suit2 + 13; c++) {
				if (card[c] == turn) {
					// �X�[�g�t�H���[�ł���
					startIndex	= c;
					endIndex	= suit2 + 13;
					break;
				}
			}
		}
		
		boolean lastEntried	= false; // ���i�J�[�h���������߂̕ϐ�
		int bestPlayCount	=  0;
		int bestPlayPath	=  0;
		int bestTricks		= -TRICK_MULTIPLICITY;
		
		int countAtLead2	= countAtLead << 4;
		
		// ���[�h�̏ꍇ�A�܂��̓X�[�g�t�H���[�ł��Ȃ��ꍇ(�Ȃ�ł��o����)
		// �{�[�h���I���ɋ߂Â��ɂ�Ė��ʂ������Ȃ�B....�ᑬ��
		for (int c = startIndex; c < endIndex; c++) {
			int tmp = card[c];
			if ((tmp > 15)&&(tmp < countAtLead2)) continue; // �v���C���ꂽ�J�[�h�͖���
			
			if (tmp == turn) { // ����ɏo�Ă���J�[�h�͂܂��v���C����Ă��Ȃ��ƍl����
				if (lastEntried) continue;
				lastEntried = true;
				// �����Ă��āA�v���C����Ă��Ȃ� ... c ���o����
				play(c);
				
				BoardStatistics stats = calculateImpl(depth+1, 13 * TRICK_MULTIPLICITY - bestTricks, nsside);
//System.out.println("c:" + count + " d:" + depth + " p:" + getCardString(c)+ " t: " + stats.finalNSTricks);
				
				// best play ���ǂ����̔���AbestPlayCount, bestTricks �̍X�V
				int finalTricks;
				if (nsside) finalTricks = stats.finalNSTricks;
				else finalTricks = 13 * TRICK_MULTIPLICITY - stats.finalNSTricks;
				if (bestTricks < finalTricks) {
					bestTricks		= finalTricks;
					bestPlayCount	= 1;
					bestPlayPath	= stats.bestPlayPaths;
					
					// ��-���}��
					// ����́A�O��� nsside(borderNsside)�ƍ���� nsside ������Ă���
					// �ꍇ�ɂ����K�p�ł��Ȃ��B�g���b�N�̐؂�ڂȂǂ� side �������ꍇ
					// ��-���͓K�p�s�A�Ƃ�������
					if ((nsside != borderNsside)&&(bestTricks > border)) {
						result.totalPlayCount += stats.totalPlayCount;
						undo();
						break;
					}
				} else if (bestTricks == finalTricks) {
					bestPlayCount++;
					bestPlayPath	+= stats.bestPlayPaths;
				}
				
				// result �̍X�V
				result.totalPlayCount += stats.totalPlayCount;
				
				undo();
			} else {
				lastEntried = false; // ���̐l�������Ă��� or �f���~�^... �V�[�P���X���؂ꂽ
			}
			// ������̂́A���v���C���̃J�[�h�̂�
		}
		
		//
		// ���ʐ���
		//
		result.bestPlayCount	= bestPlayCount;
		result.bestPlayPaths	= bestPlayPath;
		
		if (nsside) result.finalNSTricks	= bestTricks;
		else	result.finalNSTricks	= 13 * TRICK_MULTIPLICITY - bestTricks;
		
		return result;
	}
	
/*-----------------------------------
 * �a���ɂ�� Board �T�Z�A���S���Y��
 */
	public final int calcApproximateTricks() {
		int seat = (leader[count/4] + count) % 4;
		calcPropData();
		
		int leaderTricks	= calcX(seat);
		
		// �I�|�[�l���g���猩�� leader �̃g���b�N��(�c��g���b�N�� - opp�̃N�C�b�N�g���b�N)
		int opponentTricks	= limitTricks - calcMaxX(1 - (seat & 1));
		
		if (leaderTricks > opponentTricks)
			return leaderTricks * TRICK_MULTIPLICITY;
		
		return (leaderTricks + opponentTricks) * TRICK_MULTIPLICITY / 2;
	}
	
	/**
	 * �a���A���S���Y���ŋK�肳��Ă���ȉ��̒l���v�Z����B
	 * longerLength
	 * shorterLength
	 * totalWinners
	 * lowestCardOfShorterSuit
	 * highestCardOfLongerSuit
	 * isWinner
	 */
	public final void calcPropData() {
		//
		// suitCount �����߂�
		// highest card, lowest card �͉����H
		//
		for (int i = 0; i < SEAT; i++) {
			for (int j = 0; j < SUIT; j++) {
				suitCount[i][j]		= 0;
				lowestCard[i][j]	= 0;
			}
		}
		
		for (int i = 0; i < 55; i++) {
			if (card[i] < 4) {
				int tmp = i / 14;
				int tmp2 = card[i];
				if (lowestCard[tmp2][tmp] == 0)	lowestCard[tmp2][tmp] = i;
				suitCount[tmp2][tmp]++;
				highestCard[tmp2][tmp] = i;
			}
		}
		
		//
		// winner�̐��𐔂���
		// ���̏����́A����������Aplay �ōX�V�������������̂ł́H
		//
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				totalWinners[i][j] = 0;
			}
		}
		
		int NSorEW = -1;
		
		for (int i = 54; i >= 0; i--) {
			isWinner[i] = false;
			// ���[�h���ꂽ�J�[�h�͏��O�i���݃��[�h��Ԃ̂��߁A��ɂ���J�[�h�͂Ȃ��j
			if (card[i] > 15) continue;
			if (card[i] == 15) { // �f���~�^
				NSorEW = -1;
				continue;
			}
			if (NSorEW == -1) NSorEW = (card[i] & 1);
			if ( (card[i] & 1) == NSorEW ) {
				totalWinners[NSorEW][i/14]++;
				isWinner[i] = true;
			} else {
				// winner �V�[�P���X���؂ꂽ
				// skip ���� isWinner[] �� false �ɂ͂��Ȃ��ł悢
				// ����́A�P�{�[�h�ŁA����J�[�h�ɂ��� winner ���������̂�
				// winner �łȂ��Ȃ邱�Ƃ̓v���C���ꂽ�Ƃ��ȊO�ɂ͂Ȃ�
//				i = (i/14)*14;
				int tmp = (i/14)*14;
				for (i = i - 1; i > tmp; i--) isWinner[i] = false;
//if (i != tmp) System.out.println("asserted !! 568"); //(i/14)*14; // ���̂��ƁA-1 �����
				NSorEW = -1;
			}
		}
		
		//
		// assert
		//
//		for (int suit = 0; suit < 4; suit++) {
//			int totalWinner = 0;
//			for (int i = suit*14; i < suit*14+14; i++) {
//				if (isWinner[i]) totalWinner++;
//			}
//			if (totalWinner != (totalWinners[0][suit]+totalWinners[1][suit]))
//				System.out.println("asserted winner Count " + suit + "totalWinner " + totalWinner + " totalWinners[][] " + (totalWinners[0][suit]+totalWinners[1][suit]));
//		}
		
		//
		// longer, shorter ���l����
		//
		
		for (int suit = 0; suit < 4; suit++) {
			// NS �ōl����
			if (suitCount[0][suit] > suitCount[2][suit]) {
				highestCardOfLongerSuit[0][suit] = highestCard[0][suit];
				longerLength[0][suit]	= suitCount[0][suit];
				shorterLength[0][suit]	= suitCount[2][suit];
				lowestCardOfShorterSuit[0][suit] = lowestCard[2][suit];
			} else {
				highestCardOfLongerSuit[0][suit] = highestCard[2][suit];
				longerLength[0][suit]	= suitCount[2][suit];
				shorterLength[0][suit]	= suitCount[0][suit];
				lowestCardOfShorterSuit[0][suit] = lowestCard[0][suit];
			}
			
			// EW �ōl����
			if (suitCount[1][suit] > suitCount[3][suit]) {
				highestCardOfLongerSuit[1][suit] = highestCard[1][suit];
				longerLength[1][suit]	= suitCount[1][suit];
				shorterLength[1][suit]	= suitCount[3][suit];
				lowestCardOfShorterSuit[1][suit] = lowestCard[3][suit];
			} else {
				highestCardOfLongerSuit[1][suit] = highestCard[3][suit];
				longerLength[1][suit]	= suitCount[3][suit];
				shorterLength[1][suit]	= suitCount[1][suit];
				lowestCardOfShorterSuit[1][suit] = lowestCard[1][suit];
			}
		}
		
		limitTricks = 13 - (count/4);
	}
	
	/**
	 * �w�肳�ꂽ���Ȃł�(��)�N�C�b�N�g���b�N���w�����߂܂��B
	 */
	public final int calcX(int seat) {
		int NSorEW = (seat & 1);
		int result = 0;
		
		for (int suit = 0; suit < 4; suit++) {
			if (suitCount[ seat ][suit] > 0) result += calcXs(NSorEW, suit);
			else {
				if (suit == trump) {
					// 2003/5/31 �ǉ�
					// �g�����v�X�[�g�����́A�������{�C�h�ł��p�[�g�i�[�g���b�N�͊m��
					result += totalWinners[NSorEW][suit];
				}
			}
		}
		if (result > limitTricks) return limitTricks;
		return result;
	}
	
	/**
	 * Max(Te, Tw)
	 */
	public final int calcMaxX(int NSorEW) {
		int opp2 = NSorEW + 2; // opp1 = NSorEW
		int result1 = 0;
		int result2 = 0;
		
		for (int suit = 0; suit < 4; suit++) {
			int r = calcXs(NSorEW, suit);
			if (suitCount[NSorEW][suit] > 0) result1 += r;
			if (suitCount[ opp2 ][suit] > 0) result2 += r;
		}
		
		if (result1 > result2) {
			if (result1 > limitTricks) return limitTricks;
			return result1;
		}
		if (result2 > limitTricks) return limitTricks;
		return result2;
	}
	
	/**
	 * �w�肳�ꂽ NS/EW �ƃX�[�g�Ɋւ���(��)�N�C�b�N�g���b�N�� Xs �����߂܂��B
	 */
	public final int calcXs(int NSorEW, int suit) {
		int xs;
		// (A) �@���S�Ƀu���b�N���Ă���ꍇ
		if ( (shorterLength[NSorEW][suit] > 0)
				&&(isWinner[lowestCardOfShorterSuit[NSorEW][suit]])
				&&( (lowestCardOfShorterSuit[NSorEW][suit] % 14)
						> (highestCardOfLongerSuit[NSorEW][suit] % 14) ) ) {
			if (suit == trump) {
				// �g�����v�X�[�g�̏ꍇ�A
				// xs = min(totalWinners, longerLength)
				if (totalWinners[NSorEW][suit] > longerLength[NSorEW][suit]) {
					xs = longerLength[NSorEW][suit];
				} else {
					xs = totalWinners[NSorEW][suit];
				}
			} else {
				// �T�C�h�X�[�g�̏ꍇ
				xs = shorterLength[NSorEW][suit];
			}
		} else {
			// (A) �A���S�ɂ̓u���b�N���Ă��Ȃ��ꍇ
			if (shorterLength[NSorEW][suit] == 0) {
				xs = totalWinners[NSorEW][suit];
			} else {
				if (isWinner[lowestCardOfShorterSuit[NSorEW][suit]]) {
					// lowestCardOfShorterSuit �� winner
					// �I�[�o�[�e�C�N����
					if (totalWinners[NSorEW][suit]-1 > longerLength[NSorEW][suit]) {
						xs = longerLength[NSorEW][suit];
					} else {
						xs = totalWinners[NSorEW][suit]-1;
//						if (xs < 0) {
//							xs = 0;
//System.out.println("asserted !");
//						}
					}
				} else {
					// lowestCardOfShorterSuit �� winner �łȂ�
					if (totalWinners[NSorEW][suit] > longerLength[NSorEW][suit]) {
						xs = longerLength[NSorEW][suit];
					} else {
						xs = totalWinners[NSorEW][suit];
					}
				}
			}
			// (B) �G�X�^�u���b�V���ɂ�鏸�i���̏C��
			//   (A) �A�̂R�̏ꍇ�ɂ���...
			if (suit != trump) {
				int opp1 = NSorEW + 1;
				int opp2 = (NSorEW + 3)%4;
				if ( (xs >= suitCount[opp1][suit]) && (xs >= suitCount[opp2][suit]) ) {
					xs = longerLength[NSorEW][suit];
				}
			}
		}
		
		// (C) ����ɂ��̌�ŁA�I�|�[�l���g�Ƀ��t����镪���l������͂���
		if (suit != trump) {
			if (trump < 4) {
				int opp1 = (NSorEW + 1) % 4;
				int opp2 = (NSorEW + 3) % 4;
				
				if ( (suitCount[opp1][trump] > 0)&&(xs > suitCount[opp1][suit])) {
					xs = suitCount[opp1][suit];
				}
				if ( (suitCount[opp2][trump] > 0)&&(xs > suitCount[opp2][suit])) {
					xs = suitCount[opp2][suit];
				}
			}
		} else {
			// (D) s ���؂�D�X�[�g�̂Ƃ��A�X���[���J�[�h�ɂ��g���b�N���l�����A�C��
			int opp1 = (NSorEW + 1) % 4;
			int opp2 = (NSorEW + 3) % 4;
			int adj1 = suitCount[opp1][suit] - xs;
			if (adj1 < 0) adj1 = 0;
			int adj2 = suitCount[opp2][suit] - xs;
			if (adj2 < 0) adj2 = 0;
			
			int tmp = longerLength[NSorEW][suit] - adj1 - adj2;
			if (tmp > xs) xs = tmp;
			// �g�����v�̒Z��������v�Z����ƁA�N�C�b�N�g���b�N��������邽�߁A
			// ���̌v�Z���ʂ��N�C�b�N�g���b�N Xs ��舫���ꍇ�AXs ���̗p����
		}
		return xs;
	}

/*------------
 * toString()
 */
	/**
	 * �J�[�h�萔���� C5 �Ȃǂ̃J�[�h������������𓾂܂��B
	 */
	public static String getCardString(int card) {
		return SUIT_STR[card/14]+VALUE_STR[card%14];
	}
	
	/**
	 * �J�[�h�I�u�W�F�N�g����J�[�h�萔�����߂܂��B
	 */
	public static int getCardNumber(Card c) {
		int value = c.getValue();
		if (value == Card.ACE) value = 14;
		return (c.getSuit() - 1)*14+(value-2);
	}
	
	/**
	 * �f�o�b�O�p�̕�����ɕϊ����܂��B
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		String nl = System.getProperty("line.separator");
		
		s.append("���ϐ����e�\����");
		s.append(nl);
		s.append("count     �F");	s.append(count);	s.append(nl);
		s.append("nsWins    �F");	s.append(nsWins);	s.append(nl);
		s.append("trump     �F");	s.append(trump);	s.append(nl);
		s.append("�J�[�h��ԁF");
		for (int i = 0; i < card.length; i++) {
			s.append(getCardString(i));
			s.append(':');
			s.append(card[i]/16);
			s.append('/');
			s.append(SEAT_STR[card[i]%16]);
			s.append(' ');
		}
		s.append(nl);
		s.append("�v���C���ꂽ�J�[�h�F");
		for (int i = 1; i <= count; i++) {
			s.append(i);
			s.append(':');
			s.append(getCardString(play[i]));
			if ( (card[play[i]]/16) != i ) s.append("�_������");
			s.append(' ');
		}
		s.append(nl);
		s.append("leader�F");
		for (int i = 0; i <= (count/4); i++) {
			s.append(i);
			s.append(SEAT_STR[leader[i]]);
			s.append(' ');
		}
		s.append(nl);
		s.append("���{�[�h��ԕ\����");
		s.append(nl);
		s.append("�n���h���F");
		s.append(nl);
		
		// NORTH
		for (int suit = 3; suit >= 0; suit--) {
			s.append("               ");
			s.append(getHandString(0, suit));
			s.append(nl);
		}
		
		// WEST, EAST
		for (int suit = 3; suit >= 0; suit--) {
			String wstr = getHandString(3, suit) + "               ";
			wstr = wstr.substring(0, 15);
			s.append(wstr);
			s.append("               ");
			s.append(getHandString(1, suit));
			s.append(nl);
		}
		// SOUTH
		for (int suit = 3; suit >= 0; suit--) {
			s.append("               ");
			s.append(getHandString(2, suit));
			s.append(nl);
		}
		
		return s.toString();
	}
	
	private String getHandString(int seat, int suit) {
		StringBuffer s = new StringBuffer();
		s.append("CDHS".substring(suit, suit+1));
		s.append(':');
		for (int i = card.length-1; i >= 0; i--) {
			if (card[i] != seat) continue;
			if ((i/14) != suit) continue;
			s.append(getCardString(i).substring(1));
		}
		return s.toString();
	}
}

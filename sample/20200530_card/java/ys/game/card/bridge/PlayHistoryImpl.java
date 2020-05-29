package ys.game.card.bridge;

import ys.game.card.Packet;
import ys.game.card.Card;
import ys.game.card.UnspecifiedException;

/**
 * PlayHistory �N���X�́A�R���g���N�g�u���b�W�ɂ�����v���C����
 * �̏�ԁA���[�����p�b�N���܂��B
 * �{�N���X�� Board �I�u�W�F�N�g�ɕێ�����A�v���C�����̎�������
 * �s���܂��B
 *
 * @version		a-release		17, April 2000
 * @author		Yusuke Sasaki
 */
public class PlayHistoryImpl implements PlayHistory {
	
	/** ���ꂼ��̐Ȃ̃n���h��� */
	protected Packet[]	hand;
	
	/** ����܂Ńv���C���ꂽ�g���b�N�ł��B */
	protected Trick[]		trick;
	
	/** ���݃v���C���̃g���b�N���ł��B */
	protected int 		trickCount;
	
	/** �g�����v���w�肵�܂��B */
	protected int			trump;
	
/*-------------
 * constructor
 */
	public PlayHistoryImpl() {
		hand		= new Packet[4];
		trick		= new Trick[13];
		trickCount	= 0;
		trump		= -1;
	}
	
	/**
	 * �w�肳�ꂽ PlayHistory �Ɠ�����e�� PlayHistoryImpl �̃C���X�^���X��
	 * �V�K�ɐ������܂��B
	 * Trick, Hand �̓��e�ɂ��ẮA�R�s�[���̃C���X�^���X���g�p����܂��B
	 *
	 * @param		src		�R�s�[���� PlayHistory
	 */
	public PlayHistoryImpl(PlayHistory src) {
		this();
		
		//
		// Hand �̃R�s�[
		//
		hand = src.getHand();
		
		//
		// Trick �̃R�s�[
		//
		Trick[] srcTrick = src.getAllTricks();
		if (srcTrick != null) {
			System.arraycopy(srcTrick, 0, trick, 0, srcTrick.length);
			trickCount = srcTrick.length - 1;
			if ( src.isFinished() ) trickCount = 13;
		}
		
		//
		// trump �̃R�s�[
		//
		trump = src.getTrump();
	}
	
/*------------------
 * instance methods
 */

	public void setHand(Packet[] hand) {
		if ( (trick[0] != null)&&(trick[0].size() > 0) )
			throw new IllegalStatusException("���łɃv���C���J�n����Ă��邽��" +
											" setHand �͍s���܂���B");
		if (hand.length != 4)
			throw new IllegalArgumentException("�S�l���̃n���h���w�肳��Ă��܂���B");
		
		for (int i = 0; i < 4; i++) {
			if (hand[i].size() != 13)
				throw new IllegalArgumentException("�n���h"+i+"�̃J�[�h�������ُ�ł��B13���w�肵�ĉ������B");
		}
		
		this.hand = hand;
	}
	
	public void setContract(int leader, int trump) {
		if (this.trump != -1)
			throw new IllegalStatusException("��x�w�肳�ꂽ�R���g���N�g��ύX���邱�Ƃ͂ł��܂���B");
		this.trump = trump;
		trick[0] = createTrick(leader, trump);
	}
	
	/**
	 * �w�肳�ꂽ�v���C���\�ł��邩���肵�܂��B
	 * �v���C���[�Ƃ��āA���� PlayHistory �� getTurn() �̃v���C���[��
	 * �v���C���Ă���Ɖ��肵�Ă��܂��B
	 * ��̓I�ɂ́A���ݏ���
	 * 
	 * @param		p		�v���C�\�����肵�����J�[�h
	 * @return		�v���C�ł��邩�ǂ���
	 */
	public boolean allows(Card p) {
		int turn = trick[trickCount].getTurn();
		
		//
		// hand[turn] ���w�肳�ꂽ�J�[�h�����Ă��Ȃ��ꍇ false
		//
		try {
			if (!hand[turn].contains(p)) return false; // ��O�̕�����������
		}
		catch (UnspecifiedException ignored) {
			// �����Ă��Ă����������Ȃ�
		}
		
		//
		// �X�[�g�t�H���[�ɏ]���Ă��邩
		//
		Card lead = trick[trickCount].getLead();
		if (lead == null) return true;
		int suit = lead.getSuit();
		if (suit == p.getSuit()) return true;
		try {
			if (!hand[turn].containsSuit(suit)) return true;
			return false;
		}
		catch (UnspecifiedException e) {
			// �܂�ł��邩���f���t���Ȃ��ꍇ�A�n�j�Ƃ���B
			return true;
		}
	}
	
	/**
	 * �w�肳�ꂽ�J�[�h���v���C���ď�Ԃ��X�V���܂��B
	 * �v���C�ł��Ȃ��J�[�h���v���C���悤�Ƃ���� IllegalPlayException
	 * ���X���[����܂��B
	 *
	 * @param		p		�v���C����J�[�h
	 */
	public void play(Card p) {
		if (!this.allows(p))
			throw new IllegalPlayException(
					p.toString() + "�͌��݃v���C�ł��܂���B");
		
		int turn = trick[trickCount].getTurn();
		
		Card drawn;
		try {
			drawn = hand[turn].draw(p);
		}
		catch (UnspecifiedException e) {
			drawn = hand[turn].drawUnspecified();
			drawn.specify(p);
		}
		drawn.turn(true);
		Trick tr = trick[trickCount];
		tr.add(drawn);
		
		if (!tr.isFinished()) return;
		
		trickCount++;
		if (trickCount < 13) {
			trick[trickCount] = createTrick(tr.getWinner(), trump);
		}
	}
	
	public int getTurn() {
		return trick[trickCount].getTurn();
	}
	
	public Packet getHand(int seat) {
		return hand[seat];
	}
	
	public Packet[] getHand() {
		return hand;
	}
	
	public int getTrump() {
		return trump;
	}
	
	/**
	 * ���݂܂łŃv���C����Ă���g���b�N�����擾����B
	 * �v���C���̃g���b�N�ɂ��Ă̓J�E���g����Ȃ��B
	 */
	public int getTricks() {
		return trickCount;
	}
	
	/**
	 * ���݃v���C���̃g���b�N��Ԃ��B
	 */
	public Trick getTrick() {
		if (trickCount == 13) return trick[12];
		return trick[trickCount];
	}
	
	/**
	 * �w�肳�ꂽ���E���h�̃g���b�N��ԋp���܂��B
	 * ���E���h�� 0 ���� 12 �܂ł̐����l�ł��B
	 */
	public Trick getTrick(int index) {
		return trick[index];
	}
	
	public Trick[] getAllTricks() {
		if (trick[0] == null) return null;
		if (hand[0] == null) return null;
		
		int n = trickCount + 1;
		if (trickCount == 13) n--;
		Trick[] result = new Trick[n];
		System.arraycopy(trick, 0, result, 0, n);
		
		return result;
	}
	
	public boolean isFinished() {
		return ( (trickCount == 13) && (trick[12].size() == 4) );
	}
	
	protected Trick createTrick(int lead, int trump) {
		return new TrickImpl(lead, trump);
	}
	
	public void reset() {
		hand		= new Packet[4];
		trick		= new Trick[13];
		trickCount	= 0;
		trump		= -1;
	}
	
	/**
	 * �v���C�ɂ����� undo() ���s���܂��B�Ō�Ƀv���C���ꂽ�J�[�h��ԋp���܂��B
	 *
	 * ������Ԃł́AIllegalStatusException ���X���[���܂��B
	 */
	public Card undo() {
		if (trick[0] == null)
			throw new IllegalStatusException("������Ԃ̂��߁Aundo() �ł��܂���");
		if ((trickCount == 0)&&(trick[0].size() == 0))
			throw new IllegalStatusException("������Ԃɂ��邽�߁Aundo() �ł��܂���");
		
		if (trickCount == 13) {
			trickCount--;
		} else if (trick[trickCount].size() == 0) {
			// ���݃��[�h�̏��
			trick[trickCount] = null;
			trickCount--;
		}
		
		// ����̃n���h�ɖ߂���
		int seatToBePushbacked = (trick[trickCount].getTurn() + 3) % 4;
		
		// �Ō�̃v���C���擾����
		Card lastPlay = trick[trickCount].draw();
//		lastPlay.turn(false); // ���̃I�u�W�F�N�g�� Dummy ���N����m��Ȃ�
		hand[seatToBePushbacked].add(lastPlay);
		hand[seatToBePushbacked].arrange();
		
		// ��Ԃ͂��߂ɂ��ǂ����߂̓��ꏈ��( reset() �����̏��� )
		if ((trickCount == 0)&&(trick[0].size() == 0)) {
			// setContract �ȑO�̏�Ԃ܂Ŗ߂�
			hand[0] = hand[1] = hand[2] = hand[3] = null;
			trick[0]	= null;
			trump		= -1;
		}
		return lastPlay;
	}
	
	public String toString() {
		String result = "";
		
		result += "N : " + hand[Board.NORTH]	+ "\n";
		result += "E : " + hand[Board.EAST]		+ "\n";
		result += "S : " + hand[Board.SOUTH]	+ "\n";
		result += "W : " + hand[Board.WEST]		+ "\n";
		
		result += "\n";
		
		if (trickCount < 13) result += trick[trickCount];
		
		result += "\n\n";
		for (int i = trickCount-1; i >= 0; i--) {
			if (i < 13) {
				result +="[";
				if (i < 10) result += " ";
				result += String.valueOf(i);
				result += "]"+trick[i];
				result += "  win="+trick[i].getWinnerCard();
				result += "  " + Board.SEAT_STRING[trick[i].getWinner()]+"\n";
			}
		}
		
		return result;
	}
}

package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

/**
 * Trick �́A��ɏo�Ă���g���b�N�A�v���C���ꂽ�g���b�N���p�b�N����B
 * Trick �ł́A�P�g���b�N���\������J�[�h�̕ێ��̑��A�g���b�N�̃E�B�i�[
 * �̔�����s���B
 * hand �Ɋւ�����͕ێ����Ȃ��B
 *
 * @version		draft		29, August 2002
 * @author		Yusuke Sasaki
 */
public class TrickImpl extends PacketImpl implements Trick {
	/** leader �̐Ȕԍ� */
	private int		leader;
	
	/** �P�g���b�N���I�������ꍇ�Awinner �Ȕԍ����ݒ肳���B */
	private int		winner;
	
	/** �P�g���b�N���I�������ꍇ�Awinner �ƂȂ����J�[�h���ݒ肳���B */
	private Card	winnerCard;
	
	/** �g�����v */
	private int		trump;
	
/*-------------
 * Constructor
 */
	/**
	 * leader �� trump ���w�肵�� TrickImpl ���쐬���܂��B
	 */
	public TrickImpl(int leader, int trump) {
		super();
		this.leader		= leader;
		this.winner		= -1;
		this.winnerCard	= null;
		this.trump		= trump;
	}
	
	/**
	 * �R�s�[�R���X�g���N�^
	 */
	public TrickImpl(Trick src) {
		this(src.getLeader(), src.getTrump());
		
		int size = src.size();
		for (int i = 0; i < size; i++) {
			Card card = src.peek(i);
			add(card);
		}
		cardOrder = src.getCardOrder();
	}
	
/*-----------
 * Overrides
 */
	/**
	 * Trick �ł́A�C�ӂ̈ʒu�ւ̃J�[�h�}���͋�����܂���B
	 * RuntimeException ���X���[����܂��B
	 */
	public void insertAt(Card card, int index) {
		throw new RuntimeException(
					"Trick �ł́AinsertAt(Card, int)�͎g�p�ł��܂���B");
	}
	
	/**
	 * Trick �͂S���܂ł����ێ������A�S������������ɂ� Winner �����܂�.
	 */
	public void add(Card card) {
		if (isFinished())
			throw new IllegalStateException(
					"���łɏI������ Trick �ɑ΂��� add(Card) �͎��s�ł��܂���B");
		
		super.add(card);
		
		if (size() == 4) setWinner();
	}
	
	/**
	 * Winner �� lead, trump �Ȃǂ��猈�肵�܂��B
	 */
	private void setWinner() {
		if (size() == 0) {
			winnerCard = null;
			return;
		}
		
		// winner ���Z�b�g����
		winnerCard = peek(0);
		winner = 0;
		int starter = winnerCard.getSuit();
		for (int i = 1; i < size(); i++) {
			Card c = peek(i);
			if (winnerCard.getSuit() == trump) { // NO_TRUMP �̂Ƃ��͂����ɂ��Ȃ�
				if ((c.getSuit() == trump)&&(winnerCard.getValue() != Card.ACE)) {
					if ((c.getValue() > winnerCard.getValue())||
						(c.getValue() == Card.ACE)) {
						winnerCard = c;
						winner = i;
					}
				}
			} else { // winner �̃X�[�c�͏�̃X�[�c
				if (c.getSuit() == trump) {
					winnerCard = c;
					winner = i;
				}
				else if ((c.getSuit() == starter)&&(winnerCard.getValue() != Card.ACE)) {
					if ((c.getValue() > winnerCard.getValue())||(c.getValue() == Card.ACE)) {
						winnerCard = c;
						winner = i;
					}
				}
			}
		}
	}

	/**
	 * Trick �ł́A�J�[�h�����̕ύX�͋�����܂���B
	 * RuntimeException ���X���[����܂��B
	 */
	public void arrange() {
		throw new RuntimeException(
					"Trick �ł́Aarrange()�͎g�p�ł��܂���B");
	}
	
	/**
	 * Trick �ł́A�J�[�h�����̕ύX�͋�����܂���B
	 * RuntimeException ���X���[����܂��B
	 */
	public void shuffle() {
		throw new RuntimeException(
					"Trick �ł́Ashuffle()�͎g�p�ł��܂���B");
	}
	
/*-----------------------------------
 * instance methods(Trick�ŗL�̏���)
 */
	/**
	 * �͂��߂ɑ�D���o�����Ȃ̔ԍ����擾���܂��B
	 *
	 * @return		leader �̍��Ȕԍ�
	 */
	public int getLeader() {
		return leader;
	}
	
	/**
	 * �ݒ肳��Ă��� Trump ���擾���܂��B
	 *
	 * @return		�g�����v�X�[�g
	 */
	public int getTrump() {
		return trump;
	}
	
	/**
	 * ���͒N�̔Ԃ���Ԃ�.
	 * NESW �̏��ł���. Dummy ���Ԃ邱�Ƃ�����.
	 */
 	public int getTurn() {
 		return ((size() + leader) % 4);
 	}
 	
	/**
	 * ���̃g���b�N���I���Ă��邩�e�X�g����B
	 * �I���Ă���ꍇ�AgetWinner(), getWinnerCard() �̒l���L���ƂȂ�B
	 */
	public boolean isFinished() {
		return (size() == 4);
	}
	
	/**
	 * ��D���擾����B
	 * ��D���o�Ă��Ȃ��ꍇ�Anull ���Ԃ�B
	 */
	public Card getLead() {
		if (size() == 0) return null;
		return peek(0);
	}
	
	public int getWinner() {
		if (winnerCard == null) setWinner();
		return (leader + winner) % 4;
	}
	
	/**
	 * Winner �J�[�h�𓾂�.
	 * �܂��v���C���ł������ꍇ�Anull ���Ԃ�d�l�ł��������A�r���ł� winner ��
	 * �ԋp����悤�ɕύX���ꂽ�B(2002/8/29)
	 *
	 * @return		winner�J�[�h
	 */
	public Card getWinnerCard() {
		if (winnerCard == null) setWinner();
		return winnerCard;
	}
	
	public String toString() {
		String result = "Leader:" + Board.SEAT_STRING[leader];
		return result + super.toString();
	}
}

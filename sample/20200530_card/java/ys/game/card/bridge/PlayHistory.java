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
 * @version		a-release		4, May 2000
 * @author		Yusuke Sasaki
 */
public interface PlayHistory {
	
	/**
	 * ���� PlayHistory �Ŏg�p���鏉���n���h�S�̂�ݒ肵�܂��B
	 * ���łɃv���C����Ă����ꍇ�AIllegalStatusException ���X���[����܂��B
	 *
	 * @param		hand		�ݒ肷��n���h�f�[�^(North �� �Y�� 0 �Ɋi�[����Ă���)
	 */
	void setHand(Packet[] hand);
	
	/**
	 * ���� PlayHistory �ɂ�����R���g���N�g��ݒ肵�܂��B
	 * ���łɃv���C����Ă����ꍇ�AIllegalStatusException ���X���[����܂��B
	 *
	 * @param		leader		�I�[�v�j���O���[�_�[�̍��Ȕԍ�(Board.NORTH �Ȃ�)
	 * @param		trump		�g�����v(Bid.NO_TRUMP �Ȃ�)
	 */
	void setContract(int leader, int trump);
	
	/**
	 * �w�肳�ꂽ�v���C���\�ł��邩���肵�܂��B
	 * �v���C���[�Ƃ��āA���� PlayHistory �� getTurn() �̃v���C���[��
	 * �v���C���Ă���Ɖ��肵�Ă��܂��B
	 * ��̓I�ɂ́A���ݏ���
	 * 
	 * @param		p		�v���C�\�����肵�����J�[�h
	 * @return		�v���C�ł��邩�ǂ���
	 */
	boolean allows(Card p);
	
	/**
	 * �w�肳�ꂽ�J�[�h���v���C���ď�Ԃ��X�V���܂��B
	 * �v���C�ł��Ȃ��J�[�h���v���C���悤�Ƃ���� IllegalPlayException
	 * ���X���[����܂��B
	 *
	 * @param		p		�v���C����J�[�h
	 */
	void play(Card p);
	
	/**
	 * ���݃v���C���s���ׂ����Ȃ̔ԍ���Ԃ��܂��Bdummy �̐Ȕԍ����Ԃ�܂��B
	 *
	 * @return		�v���C�����҂������Ȃ̔ԍ�(Board.NORTH �Ȃ�)
	 */
	int getTurn();
	
	/**
	 * �w�肳�ꂽ���Ȃ̃n���h�����擾���܂��B
	 *
	 * @param		seat		���Ȕԍ�(Board.NORTH �Ȃ�)
	 * @return		�n���h���
	 */
	Packet getHand(int seat);
	
	/**
	 * NESW ���ׂĂ̍��Ȃ̃n���h����z��Ƃ��Ď擾���܂��B
	 * �Y���� 0,1,2,3 �����ꂼ�� N, E, S, W �̃n���h�������܂��B
	 *
	 * @return		�S�̂̃n���h���
	 */
	Packet[] getHand();
	
	/**
	 * �g�����v�X�[�c��Ԃ��܂��B
	 *
	 * @return		�g�����v�X�[�c(Bid.SPADE �Ȃ�, �ݒ肳��Ă��Ȃ��ꍇ -1)
	 */
	int getTrump();
	
	/**
	 * ���݂܂łŃv���C����Ă���g���b�N�����擾���܂��B
	 * ���݃v���C���̃g���b�N�ɂ��Ă̓J�E���g����܂���B
	 */
	int getTricks();
	
	/**
	 * ���݃v���C���̃g���b�N��Ԃ��܂��B
	 */
	Trick getTrick();
	
	/**
	 * �w�肳�ꂽ���E���h�̃g���b�N��ԋp���܂��B
	 * ���E���h�� 0 ���� 12 �܂ł̐����l�ł��B
	 */
	Trick getTrick(int index);
	
	/**
	 * ���݂܂Ńv���C���ꂽ�g���b�N���ׂĂ��擾���܂��B
	 * �v���C���I�����Ă���Ƃ��������āA�ŏI�v�f�͌��݃v���C���̃g���b�N�ł��B
	 * setHand(), setContract() �̂ǂ��炩���s���Ă��Ȃ���� null ���Ԃ�܂��B
	 *
	 * @return		���ׂẴg���b�N
	 */
	Trick[] getAllTricks();
	
	/**
	 * �v���C���P�R�g���b�N���ׂďI���������e�X�g���܂��B
	 *
	 * @return		�I��������(�I��..true)
	 */
	boolean isFinished();
	
	void reset();
	
	Card undo();
}

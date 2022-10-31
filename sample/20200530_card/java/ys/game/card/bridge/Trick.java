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
 * @version		a-release		5, May 2000
 * @author		Yusuke Sasaki
 */
public interface Trick extends Packet {
	
	/**
	 * �͂��߂ɑ�D���o�����Ȃ̔ԍ����擾���܂��B
	 *
	 * @return		leader �̍��Ȕԍ�
	 */
	int getLeader();
	
	/**
	 * �ݒ肳��Ă��� Trump ���擾���܂��B
	 *
	 * @return		�g�����v�X�[�g
	 */
	int getTrump();
	
	/**
	 * ���ɃJ�[�h���������Ȃ̔ԍ����Ԃ���܂��B
	 *
	 * @return		���ɃJ�[�h���o�����Ȃ̔ԍ�
	 */
 	int getTurn();
 	
	/**
	 * ���̃g���b�N���I���Ă��邩�e�X�g���܂��Bsize() == 4 �Ɠ����ł��B
	 * �I���Ă���ꍇ�AgetWinner(), getWinnerCard() �̒l���L���ƂȂ�܂��B
	 *
	 * @return		���̃g���b�N���I���Ă��邩
	 */
	boolean isFinished();
	
	/**
	 * ��D(���̃g���b�N�ōŏ��ɏo���ꂽ�J�[�h)���擾���܂��B
	 * ��D���o�Ă��Ȃ��ꍇ�Anull ���Ԃ�܂��B
	 *
	 * @return		��D
	 */
	Card getLead();
	
	/**
	 * ���̃g���b�N���I�����Ă���(isFinished()==true)�Ƃ��ɃE�B�i�[�ƂȂ���
	 * �J�[�h���o�������Ȃ̔ԍ����ԋp����܂��B���̃g���b�N�͂��̍��Ȃ���
	 * ���[�h����邱�ƂɂȂ�܂��B
	 * �܂��g���b�N���I�����Ă��Ȃ��ꍇ�A-1 ���ԋp����܂��B
	 *
	 * @return		���������Ȕԍ�
	 */
	int getWinner();
	
	/**
	 * ���̃g���b�N���I�����Ă���(isFinished()==true)�Ƃ��ɁA�E�B�i�[�J�[�h
	 * ���擾���܂��B
	 *
	 * @return		�������J�[�h
	 */
	Card getWinnerCard();
	
}

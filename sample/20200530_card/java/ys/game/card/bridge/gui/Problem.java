package ys.game.card.bridge.gui;

import ys.game.card.bridge.*;
import ys.game.card.*;

/**
 * �u���b�W�V�~�����[�^�̃n���h�Ȃǂ̃f�[�^���i�[����N���X�B
 *
 * @version		release		22, July 2001
 * @author		Yusuke Sasaki
 */
public interface Problem {
	/**
	 * ���� Problem ���I������A�g�p����邱�Ƃ����肵���ۂɌĂ΂�܂��B
	 * ���I�� Problem �ł́A���̒��Ńn���h���e�Ȃǂ̏��������s���܂��B
	 */
	void start();
	
	/**
	 * GUI �ɂ����邱�̖��̃n���h���ł��B
	 * start() �O�ɌĂ΂��\��������܂��B
	 */
	String getTitle();
	
	/**
	 * �R���g���N�g��Ԃ��܂��B
	 */
	Bid getContract();
	
	/**
	 * ���ꂼ��̃n���h��Ԃ��܂��B
	 * ���̃C���X�^���X�����ۂɃv���C�Ŏg�p����邽�߁A���̓��e�͕ύX����܂��B
	 * ���������āA�˂ɃC���X�^���X�̃R�s�[���쐬���邩�Astart()
	 * �̒��Ŗ���V�K�쐬�����C���X�^���X��Ԃ��K�v������܂��B
	 */
	Packet[] getHand();
	
	/**
	 * ���݂�̐������e��Ԃ��܂��B
	 */
	String getDescription();
	
	/**
	 * �R���g���N�g������킷 4S �Ȃǂ̕������Ԃ��܂��B
	 */
	String getContractString();
	
	/**
	 * �I�[�v�j���O���[�h�̎w��� Problem ����s�����Ƃ��ł��܂��B
	 */
	String getOpeningLead();
	
	/**
	 * �v�l���[�`���������������Ԃ��܂��B
	 */
	String getThinker();
	
	/**
	 * ���̖�肪�g�p�\�ł��邩�ǂ������e�X�g���܂��B
	 * valid �łȂ��ꍇ�AGUI �ɑ΂��ēo�^����܂���B
	 * start() �O�ɌĂ΂��\��������܂��B
	 */
	boolean isValid();
}

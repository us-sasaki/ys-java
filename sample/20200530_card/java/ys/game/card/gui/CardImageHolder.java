package ys.game.card.gui;

import java.awt.*;

import ys.game.card.*;

/**
 * CardImageHolder �N���X�́A�J�[�h�̕\���̃C���[�W��񋟂��܂��B
 * �J�[�h�t���[�����[�N����Apeered component �̎��(Applet�Ȃ�)�A
 * �C���[�W�̃��[�h���@(file, URL�Ȃ�)��؂藣���܂��B
 *
 * @version		a-release		20, July 2001
 * @author		Yusuke Sasaki
 */
public interface CardImageHolder {
	
	/**
	 * �X�[�g�ƃo�����[�A�������w�肵�ăJ�[�h�� Image ���擾���܂��B
	 *
	 * @param		suit		�X�[�g
	 * @param		value		�o�����[
	 * @param		direction	����
	 *
	 * @return		�J�[�h�̃C���[�W
	 */
	Image getImage(int suit, int value, int direction);
	
	/**
	 * �������w�肵�āA�J�[�h�w�ʂ� Image ���擾���܂��B
	 *
	 * @param		direction	����
	 *
	 * @return		�J�[�h�w�ʂ̃C���[�W
	 */
	Image getBackImage(int direction);
	
	/**
	 * ���̃I�u�W�F�N�g���ێ����Ă��� Image �� Graphics �R���e�L�X�g��
	 * �ێ����Ă��郊�\�[�X���J�����܂��B
	 *
	 * @since		20, July 2001
	 */
	void dispose();
	
	/**
	 * ���ʂ̊G�����w�肵�܂��B
	 *
	 * @param		�J�[�h�w�ʂ̊G���ԍ�
	 * @since		26, July 2001
	 */
	void setBackImage(int num);
	
}

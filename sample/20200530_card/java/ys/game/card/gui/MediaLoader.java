package ys.game.card.gui;

import java.util.Hashtable;
import java.awt.Image;

/**
 * MediaLoader �N���X�ł́A�摜�A�T�E���h��ێ����� Hashtable ���\�z���܂��B
 * CardImageHolder �̔ėp�łł��B
 * �g�p�\�ȃL�[�Ɠ��e�͎��̒ʂ�ł��B
 *
 * @version		a-release		28, July 2000
 * @author		Yusuke Sasaki
 */
public interface MediaLoader {
	
	/**
	 * ���f�B�A��ێ����� Hashtable ��ԋp���܂��B
	 *
	 * @return		�C���[�W�A�T�E���h�Ȃǂ̃��f�B�A��ێ����� Hashtable
	 */
	Hashtable getMediaTable();
	
	/**
	 * �w�肵���L�[�̃C���[�W��ԋp���܂��B
	 *
	 * @param		key		�L�[
	 * @return		�w�肵���L�[�̃C���[�W
	 */
	Image getImage(String key);
	
}

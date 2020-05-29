package ys.game.card.gui;

import java.awt.Dimension;

/**
 * Entities �̒��� Entity ���ǂ̂悤�ɔz�u����邩���K�肷��
 * �I�u�W�F�N�g�ł��B
 *
 * @version		a-release		29, April 2000
 * @author		Yusuke Sasaki
 */
public interface EntityLayout {
	
	/**
	 * ���� layout ���g�p�����ꍇ�̎w�肵�� Entities �̑傫�����v�Z���܂��B
	 *
	 * @param		target		�傫�����v�Z����Ώۂ� Entities
	 * @return		�傫��
	 */
    Dimension layoutSize(Entities target);
    
    /**
     * �w�肳�ꂽ Entities �Ɋ܂܂�� Entity ������ layout �̋K�肷��
     * ����ׂ��ʒu�Ɉړ����A�w�肳�ꂽ Entities �̑傫����ύX���܂��B
     *
     * @param		target		layout �����s���� Entities
     */
    void layout(Entities target);
}

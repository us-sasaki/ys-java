package ys.game.card.gui;

import java.awt.Dimension;

/**
 * Entities の中で Entity がどのように配置されるかを規定する
 * オブジェクトです。
 *
 * @version		a-release		29, April 2000
 * @author		Yusuke Sasaki
 */
public interface EntityLayout {
	
	/**
	 * この layout を使用した場合の指定した Entities の大きさを計算します。
	 *
	 * @param		target		大きさを計算する対象の Entities
	 * @return		大きさ
	 */
    Dimension layoutSize(Entities target);
    
    /**
     * 指定された Entities に含まれる Entity をこの layout の規定する
     * あるべき位置に移動し、指定された Entities の大きさを変更します。
     *
     * @param		target		layout を実行する Entities
     */
    void layout(Entities target);
}

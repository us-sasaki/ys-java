package ys.game.card.gui;

import java.awt.*;

import ys.game.card.*;

/**
 * CardImageHolder クラスは、カードの表裏のイメージを提供します。
 * カードフレームワークから、peered component の種別(Appletなど)、
 * イメージのロード方法(file, URLなど)を切り離します。
 *
 * @version		a-release		20, July 2001
 * @author		Yusuke Sasaki
 */
public interface CardImageHolder {
	
	/**
	 * スートとバリュー、向きを指定してカードの Image を取得します。
	 *
	 * @param		suit		スート
	 * @param		value		バリュー
	 * @param		direction	向き
	 *
	 * @return		カードのイメージ
	 */
	Image getImage(int suit, int value, int direction);
	
	/**
	 * 向きを指定して、カード背面の Image を取得します。
	 *
	 * @param		direction	向き
	 *
	 * @return		カード背面のイメージ
	 */
	Image getBackImage(int direction);
	
	/**
	 * このオブジェクトが保持している Image の Graphics コンテキストが
	 * 保持しているリソースを開放します。
	 *
	 * @since		20, July 2001
	 */
	void dispose();
	
	/**
	 * 裏面の絵柄を指定します。
	 *
	 * @param		カード背面の絵柄番号
	 * @since		26, July 2001
	 */
	void setBackImage(int num);
	
}

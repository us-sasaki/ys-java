package ys.game.card.gui;

import java.util.Hashtable;
import java.awt.Image;

/**
 * MediaLoader クラスでは、画像、サウンドを保持する Hashtable を構築します。
 * CardImageHolder の汎用版です。
 * 使用可能なキーと内容は次の通りです。
 *
 * @version		a-release		28, July 2000
 * @author		Yusuke Sasaki
 */
public interface MediaLoader {
	
	/**
	 * メディアを保持する Hashtable を返却します。
	 *
	 * @return		イメージ、サウンドなどのメディアを保持する Hashtable
	 */
	Hashtable getMediaTable();
	
	/**
	 * 指定したキーのイメージを返却します。
	 *
	 * @param		key		キー
	 * @return		指定したキーのイメージ
	 */
	Image getImage(String key);
	
}

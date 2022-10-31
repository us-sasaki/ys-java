package ys.game.card.gui;

import java.util.Hashtable;
import java.awt.*;

/**
 * MediaLoader クラスでは、画像、サウンドを保持する Hashtable を構築します。
 * CardImageHolder の汎用版です。
 * 使用可能なキーと内容は次の通りです。<BR>
 * <TABLE>
 * <TR><TD>キー</TD><TD>内容</TD></TR>
 * <TR><TD>sumire0</TD><TD>すみれイメージ(直立)</TD></TR>
 * <TR><TD>sumire1</TD><TD>すみれイメージ(直立にっこり)</TD></TR>
 * <TR><TD>sumire2</TD><TD>すみれイメージ(直立泣く)</TD></TR>
 * </TABLE>
 *
 * @version		making		2, August 2000
 * @author		Yusuke Sasaki
 */
public class LocalMediaLoader implements MediaLoader {
	protected static final String IMAGE_DIR =
					"C:\\private\\Programs\\ys\\game\\card\\gui\\images\\";
	
	protected Hashtable		table;
	
/*-------------
 * Constructor
 */
	public LocalMediaLoader() {
		table = new Hashtable();
		
		Toolkit	t = Toolkit.getDefaultToolkit();
		Frame	f = new Frame();
		f.pack();		// create peer
		
		MediaTracker mt = new MediaTracker(f);
		
		Image tmp;
		
		try {
			for (int i = 0; i < 3; i++) {
				tmp = t.getImage(IMAGE_DIR+"sumire"+i+".gif");
				mt.addImage(tmp, 0);
				table.put("sumire"+i, tmp);
			}
			mt.waitForAll();
		} catch (InterruptedException ignored) {
		}
		
	}
	
	/**
	 * メディアを保持する Hashtable を返却します。
	 *
	 * @return		イメージ、サウンドなどのメディアを保持する Hashtable
	 */
	public Hashtable getMediaTable() {
		return table;
	}
	
	public Image getImage(String key) {
		Object val = table.get(key);
		if (!(val instanceof Image))
			throw new IllegalArgumentException("指定したキー"+
							key+
							"に対応するオブジェクトはImageのインスタンスではありません。");
		return (Image)val;
	}
}

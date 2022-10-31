package ys.game.card.gui;

import java.util.Hashtable;
import java.awt.*;

/**
 * MediaLoader �N���X�ł́A�摜�A�T�E���h��ێ����� Hashtable ���\�z���܂��B
 * CardImageHolder �̔ėp�łł��B
 * �g�p�\�ȃL�[�Ɠ��e�͎��̒ʂ�ł��B<BR>
 * <TABLE>
 * <TR><TD>�L�[</TD><TD>���e</TD></TR>
 * <TR><TD>sumire0</TD><TD>���݂�C���[�W(����)</TD></TR>
 * <TR><TD>sumire1</TD><TD>���݂�C���[�W(�����ɂ�����)</TD></TR>
 * <TR><TD>sumire2</TD><TD>���݂�C���[�W(��������)</TD></TR>
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
	 * ���f�B�A��ێ����� Hashtable ��ԋp���܂��B
	 *
	 * @return		�C���[�W�A�T�E���h�Ȃǂ̃��f�B�A��ێ����� Hashtable
	 */
	public Hashtable getMediaTable() {
		return table;
	}
	
	public Image getImage(String key) {
		Object val = table.get(key);
		if (!(val instanceof Image))
			throw new IllegalArgumentException("�w�肵���L�["+
							key+
							"�ɑΉ�����I�u�W�F�N�g��Image�̃C���X�^���X�ł͂���܂���B");
		return (Image)val;
	}
}

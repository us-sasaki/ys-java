import java.awt.*;

/**
 * �`���b�g�̃��O��\�����镔���B�Ƃ肠�����쐬�����o�[�W�����B
 *
 * @author		Yusuke Sasaki
 * @version		making		10, December 2000
 */
public class LogArea extends Panel {
	protected static final int BUFFER_STEP = 10;
	protected static final int MAX_NUMBER = 5;
	
/*-------------
 * Constructor
 */
	/**
	 * LogArea ��V�K�ɍ쐬���܂��B
	 */
	public LogArea() {
		super();
//		buffer = new Label[BUFFER_STEP];
//		number = 0;
		
		setLayout(new GridLayout(MAX_NUMBER, 1));
		for (int i = 0; i < MAX_NUMBER; i++) add(new Label(""));
		doLayout();
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 1�s�ǉ����܂��B�����I�ɕ\�����X�V����܂��B
	 */
	public void addLine(String message) {
		add(new Label(message));
		remove(0);
		doLayout();
	}
}

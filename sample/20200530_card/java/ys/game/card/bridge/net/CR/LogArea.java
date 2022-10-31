import java.awt.*;

/**
 * チャットのログを表示する部分。とりあえず作成したバージョン。
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
	 * LogArea を新規に作成します。
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
	 * 1行追加します。自動的に表示が更新されます。
	 */
	public void addLine(String message) {
		add(new Label(message));
		remove(0);
		doLayout();
	}
}

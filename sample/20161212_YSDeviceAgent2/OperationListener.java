import java.io.IOException;

/**
 * Long-polling によるイベントの受け取り手です。
 *
 * @version		13 December, 2016
 * @author		Yusuke Sasaki
 */
public interface OperationListener {
	void onOperationCalled(OperationEvent op);
}

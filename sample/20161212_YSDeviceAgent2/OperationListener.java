import java.io.IOException;

/**
 * Long-polling �ɂ��C�x���g�̎󂯎���ł��B
 *
 * @version		13 December, 2016
 * @author		Yusuke Sasaki
 */
public interface OperationListener {
	void onOperationCalled(OperationEvent op);
}

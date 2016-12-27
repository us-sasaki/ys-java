import java.io.IOException;

import abdom.data.json.JsonObject;

/**
 * Long-polling によるイベントです。
 *
 * @version		13 December, 2016
 * @author		Yusuke Sasaki
 */
public class OperationEvent {
	protected JsonObject c8yMessage;
	
/*-------------
 * constructor
 */
	public OperationEvent(JsonObject message) {
		c8yMessage = message;
	}
	
/*------------------
 * instance methods
 */
	public JsonObject getMessage() {
		return c8yMessage;
	}
	
	public int
	
}

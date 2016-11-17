import java.io.IOException;

import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;

public class OperationListener implements Runnable {
	protected JsonType conf;
	
/*-------------
 * Constructor
 */
	public OperationListener(JsonType conf) {
		this.conf = conf;
	}
	
	
/*------------------
 * instance methods
 */
	public void listen() {
		Thread t = new Thread(this);
		t.start();
	}
	
/*-----------------------
 * implements (Runnable)
 */
	@Override
	public void run() {
		
	}
}

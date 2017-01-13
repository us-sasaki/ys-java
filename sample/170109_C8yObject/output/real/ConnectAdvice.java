package real;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;

/**
 * ConnectAdvice class
 * This source is machine-generated from c8y-markdown docs.
 */
public class ConnectAdvice extends C8yData {
	/**
	 * Interval between sending of connect message and response from server.
	 * Overrides server default settings for current request-response
	 * conversation.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public TC_Date timeout;
	
	/**
	 * Period above which server will close session, if not received next
	 * connect message from client. Overrides server default settings for
	 * current request-response conversation.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public int interval;
	
}

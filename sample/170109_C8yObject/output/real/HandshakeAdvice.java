package real;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;

/**
 * HandshakeAdvice class
 * This source is machine-generated from c8y-markdown docs.
 */
public class HandshakeAdvice extends C8yData {
	/**
	 * Max. time in milliseconds between sending of a connect message and
	 * response from server .Overrides server default settings for session.
	 * Default value : 3600000, maximum value 7200000
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public TC_Date timeout;
	
	/**
	 * Period above which server will close session, if not received next
	 * connect message from client. Overrides server default settings for
	 * session. Default value : 10000
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public int interval;
	
}

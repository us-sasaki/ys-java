package events;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.events.Event;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * EventCollection class
 * This source is machine-generated from c8y-markdown docs.
 */
public class EventCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of events, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Event[] events;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of events.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of events.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}

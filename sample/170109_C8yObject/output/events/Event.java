package events;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.inventory.ManagedObject;

/**
 * Event class
 * This source is machine-generated from c8y-markdown docs.
 */
public class Event extends C8yData {
	/**
	 * Uniquely identifies an event.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Time when event was created in the database.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public TC_Date creationTime;
	
	/**
	 * Identifies the type of this event.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String type;
	
	/**
	 * Time of the event.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public TC_Date time;
	
	/**
	 * Text description of the event.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String text;
	
	/**
	 * The ManagedObject that the event originated from, as object containing
	 * properties "id", "self", "name", and "type".
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public ManagedObject source;
	
	/**
	 * Additional properties of the event.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	//omitted since type, field equals "*"
	
}

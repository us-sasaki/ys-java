package measurements;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.inventory.ManagedObject;

/**
 * Measurement class
 * This source is machine-generated from c8y-markdown docs.
 */
public class Measurement extends C8yData {
	/**
	 * Uniquely identifies a measurement.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String self;
	
	/**
	 * Time of the measurement.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 * </pre>
	 */
	public TC_Date time;
	
	/**
	 * The most specific type of this entire measurement.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 * </pre>
	 */
	public String type;
	
	/**
	 * The ManagedObject which is the source of this measurement, as object
	 * containing the properties "id" and "self".
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : Mandatory
	 * </pre>
	 */
	public ManagedObject source;
	
	/**
	 * List of measurement fragments.
	 * <pre>
	 * Occurs : 0..n
	 * PUT/POST : Optional
	 * </pre>
	 */
	//omitted since type, field equals "*"
	
}

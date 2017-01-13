package identity;

import com.ntt.tc.data.C8yData;

/**
 * IdentityAPI class
 * This source is machine-generated from c8y-markdown docs.
 */
public class IdentityAPI extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Single external ID, represented by type of the external ID and the value
	 * of the external ID, both as strings (placeholders {type} and {value}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String externalId;
	
	/**
	 * Represents a collection of external ids for a specified global id
	 * (placeholder {globalId}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String externalIdsOfGlobalId;
	
}

package retention;

import com.ntt.tc.data.C8yData;

/**
 * RetentionRule class
 * This source is machine-generated from c8y-markdown docs.
 */
public class RetentionRule extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 0..1
	 * Visibility : Public
	 * </pre>
	 */
	public String self;
	
	/**
	 * RetentionRulle id.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * </pre>
	 */
	public long id;
	
	/**
	 * RetentionRule will be applied to this type of documents, possible values
	 * [ALARM, AUDIT, EVENT, MEASUREMENT, OPERATION, *].
	 * <pre>
	 * Occurs : 0..1
	 * Visibility : Public
	 * </pre>
	 */
	public String dataType;
	
	/**
	 * RetentionRule will be applied to documents with fragmentType.
	 * <pre>
	 * Occurs : 0..1
	 * Visibility : Public
	 * </pre>
	 */
	public String fragmentType;
	
	/**
	 * RetentionRule will be applied to documents with type.
	 * <pre>
	 * Occurs : 0..1
	 * Visibility : Public
	 * </pre>
	 */
	public String type;
	
	/**
	 * RetentionRule will be applied to documnets with source.
	 * <pre>
	 * Occurs : 0..1
	 * Visibility : Public
	 * </pre>
	 */
	public String source;
	
	/**
	 * Maximum age of document in days.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * </pre>
	 */
	public long maximumAge;
	
}

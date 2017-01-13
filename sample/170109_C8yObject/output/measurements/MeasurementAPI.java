package measurements;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.measurements.MeasurementCollection;

/**
 * MeasurementAPI class
 * This source is machine-generated from c8y-markdown docs.
 */
public class MeasurementAPI extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Collection of all measurements.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public MeasurementCollection measurements;
	
	/**
	 * Read-only collection of all measurements coming from a particular source
	 * object (placeholder {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForSource;
	
	/**
	 * Read-only collection of all measurements from a particular period
	 * (placeholder {dateFrom} and {dateTo}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForDate;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type (placeholder {fragmentType}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * (placeholder {type}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForType;
	
	/**
	 * Read-only collection of all measurements from a particular period and
	 * from a particular source object (placeholder {dateFrom}, {dateTo} and
	 * {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForSourceAndDate;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and coming from a particular source object (placeholder
	 * {fragmentType} and {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForSourceAndFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * and coming from a particular source object (placeholder {type} and
	 * {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForSourceAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and being from a particular period (placeholder
	 * {fragmentType}, {dateFrom} and {dateTo}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForDateAndFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * and being from a particular period (placeholder {type}, {dateFrom} and
	 * {dateTo}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForDateAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * and a particular fragment type(placeholder {type} and {fragmentType}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForFragmentTypeAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and being from a particular period and source object
	 * (placeholder {fragmentType}, {dateFrom}, {dateTo} and {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForSourceAndDateAndFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * and being from a particular period and source object (placeholder
	 * {type}, {dateFrom}, {dateTo} and {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForSourceAndDateAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and a particular type and source object (placeholder
	 * {fragmentType}, {type} and {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForSourceAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and being from a particular period and type object
	 * (placeholder {fragmentType}, {dateFrom}, {dateTo} and {type}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForDateAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and type object and being from a particular period and
	 * source object (placeholder {fragmentType}, {dateFrom}, {dateTo}, {type}
	 * and {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String measurementsForSourceAndDateAndFragmentTypeAndType;
	
}

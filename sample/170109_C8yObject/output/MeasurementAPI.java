package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.MeasurementCollection;
import com.ntt.tc.data.String;

public class MeasurementAPI extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * Collection of all measurements.
	 *
	 * Occurs : 1
	 */
	public MeasurementCollection measurements;
	
	/**
	 * Read-only collection of all measurements coming from a particular source
	 * object (placeholder {source}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForSource;
	
	/**
	 * Read-only collection of all measurements from a particular period
	 * (placeholder {dateFrom} and {dateTo}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForDate;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type (placeholder {fragmentType}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * (placeholder {type}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForType;
	
	/**
	 * Read-only collection of all measurements from a particular period and
	 * from a particular source object (placeholder {dateFrom}, {dateTo} and
	 * {source}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForSourceAndDate;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and coming from a particular source object (placeholder
	 * {fragmentType} and {source}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForSourceAndFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * and coming from a particular source object (placeholder {type} and
	 * {source}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForSourceAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and being from a particular period (placeholder
	 * {fragmentType}, {dateFrom} and {dateTo}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForDateAndFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * and being from a particular period (placeholder {type}, {dateFrom} and
	 * {dateTo}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForDateAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * and a particular fragment type(placeholder {type} and {fragmentType}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForFragmentTypeAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and being from a particular period and source object
	 * (placeholder {fragmentType}, {dateFrom}, {dateTo} and {source}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForSourceAndDateAndFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular type
	 * and being from a particular period and source object (placeholder
	 * {type}, {dateFrom}, {dateTo} and {source}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForSourceAndDateAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and a particular type and source object (placeholder
	 * {fragmentType}, {type} and {source}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForSourceAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and being from a particular period and type object
	 * (placeholder {fragmentType}, {dateFrom}, {dateTo} and {type}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForDateAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and type object and being from a particular period and
	 * source object (placeholder {fragmentType}, {dateFrom}, {dateTo}, {type}
	 * and {source}).
	 *
	 * Occurs : 1
	 */
	public String measurementsForSourceAndDateAndFragmentTypeAndType;
	
}

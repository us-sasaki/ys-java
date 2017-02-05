package com.ntt.tc.data.rest;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.Id;
import com.ntt.tc.data.sensor.C8y_SignalStrength;

/**
 * GET /measurement のレスポンス
 */
public class MeasurementResp extends C8yData {
	/**
	 * Link to this resource.
	 */
	public String self;
	
	/**
	 * Collection of all measurements.
	 */
	public JsonObject measurements;
	
	/**
	 * Read-only collection of all measurements coming from a particular
	 * source object (placeholder {source}).
	 */
	public String measurementsForSource;
	
	/**
	 * Read-only collection of all measurements from a particular period
	 * (placeholder {dateFrom} and {dateTo}).
	 */
	public String measurementsForDate;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type (placeholder {fragmentType}).
	 */
	public String measurementsForFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * type (placeholder {type}).
	 */
	public String measurementsForType;
	
	/**
	 * Read-only collection of all measurements from a particular period
	 * and from a particular source object
	 * (placeholder {dateFrom}, {dateTo} and {source}).
	 */
	public String measurementsForSourceAndDate;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and coming from a particular source object
	 * (placeholder {fragmentType} and {source}).
	 */
	public String measurementsForSourceAndFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * type and coming from a particular source object
	 * (placeholder {type} and {source}).
	 */
	public String measurementsForSourceAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and being from a particular period
	 * (placeholder {fragmentType}, {dateFrom} and {dateTo}).
	 */
	public String measurementsForDateAndFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * type and being from a particular period
	 * (placeholder {type}, {dateFrom} and {dateTo}).
	 */
	public String measurementsForDateAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * type and a particular fragment type
	 * (placeholder {type} and {fragmentType}).
	 */
	public String measurementsForFragmentTypeAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and being from a particular period and source object
	 * (placeholder {fragmentType}, {dateFrom}, {dateTo} and {source}).
	 */
	public String measurementsForSourceAndDateAndFragmentType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * type and being from a particular period and source object
	 * (placeholder {type}, {dateFrom}, {dateTo} and {source}).
	 */
	public String measurementsForSourceAndDateAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and a particular type and source object
	 * (placeholder {fragmentType}, {type} and {source}).
	 */
	public String measurementsForSourceAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and being from a particular period and type object
	 * (placeholder {fragmentType}, {dateFrom}, {dateTo} and {type}).
	 */
	public String measurementsForDateAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all measurements containing a particular
	 * fragment type and type object and being from a particular period
	 * and source object
	 * (placeholder {fragmentType}, {dateFrom}, {dateTo}, {type} and {source}).
	 */
	public String measurementsForSourceAndDateAndFragmentTypeAndType;
}

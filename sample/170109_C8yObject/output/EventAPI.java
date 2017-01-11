package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

/**
 * EventAPI class
 * This source is machine-generated.
 */
public class EventAPI extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Collection of all events.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public EventCollection events;
	
	/**
	 * Read-only collection of all events of a particular type (placeholder
	 * {type}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForType;
	
	/**
	 * Read-only collection of all events from a particular source object
	 * (placeholder {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForSource;
	
	/**
	 * Read-only collection of all events of a particular type and from a
	 * particular source (placeholders {type} and {source}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForSourceAndType;
	
	/**
	 * Read-only collection of all events from a particular period (placeholder
	 * {dateFrom}, {dateTo}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForTime;
	
	/**
	 * Read-only collection of all events containing a particular fragment type
	 * (placeholder {fragmentType}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForFragmentType;
	
	/**
	 * Read-only collection of all events from a particular source object from
	 * a particular period (placeholders {source}, {dateFrom}, {dateTo}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForSourceAndTime;
	
	/**
	 * Read-only collection of all events of a particular source object
	 * containing a particular fragment type (placeholders {source},
	 * {fragmentType}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForSourceAndFragmentType;
	
	/**
	 * Read-only collection of all events from a particular period containing a
	 * particular fragment type (placeholders {dateFrom}, {dateTo},
	 * {fragmentType}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForDateAndFragmentType;
	
	/**
	 * Read-only collection of all events of a particular type containing a
	 * particular fragment type (placeholders {fragmentType}, {type}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForFragmentTypeAndType;
	
	/**
	 * Read-only collection of all events with a particular type from a
	 * particular period (placeholders {type}, {dateFrom}, {dateTo}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForTimeAndType;
	
	/**
	 * Read-only collection of all events from a particular source object,
	 * containing a particular fragment type, from a particular period
	 * (placeholders {source}, {dateFrom}, {dateTo}, {fragmentType}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForSourceAndDateAndFragmentType;
	
	/**
	 * Read-only collection of all events from a particular source object, with
	 * a particular type, containing a particular fragment type, from a
	 * particular period (placeholders {source}, {dateFrom}, {dateTo},
	 * {fragmentType}, {type}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForSourceAndDateAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all events from a particular source object, with
	 * a particular type, containing a particular fragment type (placeholders
	 * {source}, {fragmentType}, {type}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForSourceAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all events from a particular source object, with
	 * a particular type, from a particular period (placeholders {source},
	 * {type}, {dateFrom}, {dateTo}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForSourceAndTimeAndType;
	
	/**
	 * Read-only collection of all events from a particular type, containing a
	 * particular fragment type, from a particular period (placeholders {type},
	 * {dateFrom}, {dateTo}, {fragmentType}).
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String eventsForDateAndFragmentTypeAndType;
	
}

package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.EventCollection;
import com.ntt.tc.data.String;

public class EventAPI extends C8yData {
	/**
	 * Link to this resource.
	 *
	 * Occurs : 1
	 */
	public String self;
	
	/**
	 * Collection of all events.
	 *
	 * Occurs : 1
	 */
	public EventCollection events;
	
	/**
	 * Read-only collection of all events of a particular type (placeholder
	 * {type}).
	 *
	 * Occurs : 1
	 */
	public String eventsForType;
	
	/**
	 * Read-only collection of all events from a particular source object
	 * (placeholder {source}).
	 *
	 * Occurs : 1
	 */
	public String eventsForSource;
	
	/**
	 * Read-only collection of all events of a particular type and from a
	 * particular source (placeholders {type} and {source}).
	 *
	 * Occurs : 1
	 */
	public String eventsForSourceAndType;
	
	/**
	 * Read-only collection of all events from a particular period (placeholder
	 * {dateFrom}, {dateTo}).
	 *
	 * Occurs : 1
	 */
	public String eventsForTime;
	
	/**
	 * Read-only collection of all events containing a particular fragment type
	 * (placeholder {fragmentType}).
	 *
	 * Occurs : 1
	 */
	public String eventsForFragmentType;
	
	/**
	 * Read-only collection of all events from a particular source object from
	 * a particular period (placeholders {source}, {dateFrom}, {dateTo}).
	 *
	 * Occurs : 1
	 */
	public String eventsForSourceAndTime;
	
	/**
	 * Read-only collection of all events of a particular source object
	 * containing a particular fragment type (placeholders {source},
	 * {fragmentType}).
	 *
	 * Occurs : 1
	 */
	public String eventsForSourceAndFragmentType;
	
	/**
	 * Read-only collection of all events from a particular period containing a
	 * particular fragment type (placeholders {dateFrom}, {dateTo},
	 * {fragmentType}).
	 *
	 * Occurs : 1
	 */
	public String eventsForDateAndFragmentType;
	
	/**
	 * Read-only collection of all events of a particular type containing a
	 * particular fragment type (placeholders {fragmentType}, {type}).
	 *
	 * Occurs : 1
	 */
	public String eventsForFragmentTypeAndType;
	
	/**
	 * Read-only collection of all events with a particular type from a
	 * particular period (placeholders {type}, {dateFrom}, {dateTo}).
	 *
	 * Occurs : 1
	 */
	public String eventsForTimeAndType;
	
	/**
	 * Read-only collection of all events from a particular source object,
	 * containing a particular fragment type, from a particular period
	 * (placeholders {source}, {dateFrom}, {dateTo}, {fragmentType}).
	 *
	 * Occurs : 1
	 */
	public String eventsForSourceAndDateAndFragmentType;
	
	/**
	 * Read-only collection of all events from a particular source object, with
	 * a particular type, containing a particular fragment type, from a
	 * particular period (placeholders {source}, {dateFrom}, {dateTo},
	 * {fragmentType}, {type}).
	 *
	 * Occurs : 1
	 */
	public String eventsForSourceAndDateAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all events from a particular source object, with
	 * a particular type, containing a particular fragment type (placeholders
	 * {source}, {fragmentType}, {type}).
	 *
	 * Occurs : 1
	 */
	public String eventsForSourceAndFragmentTypeAndType;
	
	/**
	 * Read-only collection of all events from a particular source object, with
	 * a particular type, from a particular period (placeholders {source},
	 * {type}, {dateFrom}, {dateTo}).
	 *
	 * Occurs : 1
	 */
	public String eventsForSourceAndTimeAndType;
	
	/**
	 * Read-only collection of all events from a particular type, containing a
	 * particular fragment type, from a particular period (placeholders {type},
	 * {dateFrom}, {dateTo}, {fragmentType}).
	 *
	 * Occurs : 1
	 */
	public String eventsForDateAndFragmentTypeAndType;
	
}

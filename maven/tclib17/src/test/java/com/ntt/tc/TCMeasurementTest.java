package com.ntt.tc;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.ntt.tc.data.*;
import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.alarms.*;
import com.ntt.tc.data.auditing.*;
import com.ntt.tc.data.binaries.*;
import com.ntt.tc.data.device.*;
import com.ntt.tc.data.events.*;
import com.ntt.tc.data.identity.*;
import com.ntt.tc.data.inventory.*;
import com.ntt.tc.data.measurements.*;
import com.ntt.tc.data.real.*;
import com.ntt.tc.data.rest.*;
import com.ntt.tc.data.retention.*;
import com.ntt.tc.data.sensor.*;
import com.ntt.tc.data.tenants.*;
import com.ntt.tc.data.users.*;

/**
 * Measurement のテスト。
 */
public class TCMeasurementTest extends TestCase{
	/**
	 * ほとんど JData の set のテスト(add の実装に set を使っている)
	 */
	public void testMeas() {
		Measurement m = new Measurement();
		m.put("c8y_TemperatureMeasurement","T",20,"C");
		m.put("testFrag", "testMeas", 100, "testUnit");
		assertEquals(m.toString(), "{\"c8y_TemperatureMeasurement\":{\"T\":{\"unit\":\"C\",\"value\":20.0}},\"testFrag\":{\"testMeas\":{\"unit\":\"testUnit\",\"value\":100.0}}}");
		assertEquals(m.c8y_TemperatureMeasurement.T.value, 20d);
	}
	
	public void testMeasCol() {
		MeasurementCollection mc = new MeasurementCollection();
		Measurement m = new Measurement();
		m.put("c8y_TemperatureMeasurement.T", 23d, "C");
		mc.add(m);
		assertEquals(mc.toString(), "{\"measurements\":[{\"c8y_TemperatureMeasurement\":{\"T\":{\"unit\":\"C\",\"value\":23.0}}}]}");
	}
}

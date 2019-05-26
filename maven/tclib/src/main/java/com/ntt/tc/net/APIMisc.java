package com.ntt.tc.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;
import abdom.data.json.object.Jsonizer;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;
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
import com.ntt.tc.util.Base64;

import static com.ntt.tc.net.Rest.Response;

/**
 * Things Cloud の Rest API でテストコードとして頻発するコードをまとめた
 * もので、実装内容を理解して利用する必要があります。
 * 温度 Measurement POST, Event POST など、これまで多数同一コードを書いたと
 * 思われるコードをまとめています。
 *
 * @author		Yusuke Sasaki
 * @version		May 27, 2019
 */
public class APIMisc {
	protected API api;
	
/*-------------
 * constructor
 */
	public APIMisc(API api) {
		this.api = api;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * -10℃～40℃の範囲で乱数の温度(c8y_Temperature)Measurement を生成します。
	 * <pre>
	 * type, fragment type = "c8y_Temperature"
	 * fragment series = "T"
	 * unit = "C"
	 * time = 現在時刻
	 * </pre>
	 * が設定されます。
	 *
	 * @param		source		source
	 * @throws		java.io.IOException		REST異常
	 */
	public void createTemperatureMeasurement(String source)
											throws IOException {
		Measurement m = new Measurement(source, "c8y_Temperature");
		double t = Math.random() * 50 - 10;
		m.put("c8y_Temperature.T", t, "C");
		api.createMeasurement(m);
	}
	
	/**
	 * テストイベントを生成します。
	 * <pre>
	 * type = "nttcom_TestEvent"
	 * time = 現在時刻
	 * text = "Test Event"
	 * </pre>
	 * が設定されます。
	 * 
	 * @param		source		source
	 * @throws		java.io.IOException		REST異常
	 */
	public void createTestEvent(String source)
											throws IOException {
		Event e = new Event(source, "nttcom_TestEvent", "Test Event");
		api.createEvent(e);
	}
}

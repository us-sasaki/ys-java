import java.io.*;

import abdom.data.json.*;
import abdom.data.json.object.*;

import com.ntt.tc.net.*;
import com.ntt.tc.data.*;
import com.ntt.tc.data.measurements.*;
import com.ntt.tc.data.events.*;
import com.ntt.tc.data.alarms.*;
import com.ntt.tc.data.inventory.*;

/**
 * Event 取得
 *
 * @author	Yusuke Sasaki
 */
public class EventGet {
	public static void main(String[] args) throws Exception {
		// REST アクセスオブジェクトの生成
		Rest r = new Rest("https://iottf_lora.je1.thingscloud.ntt.com", "", "");
		
		//------------------------------
		// EventCollection の取得
		//------------------------------
		int page = 1;
		while (true) {
			Rest.Response resp = r.get("/event/events?pageSize=1000&currentPage="+(page++));
			EventCollection ec = Jsonizer.fromJson(resp, EventCollection.class);
			
//System.out.println(ec.toString("  ")); // JSON表示
			for (int i = 0; i < ec.events.length; i++) {
				Event e = ec.events[i];
				System.out.print(e.creationTime.toString() + "\t" + e.time + "\t" + e.source.id + "\t" + e.type + "\t" + e.text);
				JsonType req = e.get("c8y_ActilityUplinkRequest");
				if (req != null) {
					System.out.print("\t" + req.get("devEui") + "\t" + req.get("error") + "\t" + req.get("payloadHex") + "\t" + req.get("smartrestResponse"));
//					for (Object key : e.getExtraKeySet()) {
//						JsonType val = e.get((String)key);
//						System.out.print("\t" + key + ":" + val);
//					}
				}
				System.out.println();
			}
			if (ec.next == null) break;
		}
	}
}

//c8y_ActilityUplinkRequest:{"devEui":"000DB53114873546","error":null,"payloadHex":"000e490211386b0813c635","smartrestResponse":null}
//"2017-05-24T16:35:10.353+09:00"	"2017-05-24T16:35:10.340+09:00"	10384	c8y_ActilityUplinkRequest	Actility uplink request	c8y_ActilityUplinkRequest:{"devEui":"4776E6ED00490038","error":"Failed to decode payload: No handler for payload (3448656c6c6f20776f726c64212020323031372d30352d32345431363a33353a30342e3030302b30393a303034): \ncom.cumulocity.actility.payload.ActilitySmartRestPayloadParsingException: Error parsing payload\ncom.cumulocity.actility.payload.AdeunisDemonstratorPayloadDecodingException: Payload must be of 1-32 hexchars length","payloadHex":"48656c6c6f20776f726c64212020323031372d30352d32345431363a33353a30342e3030302b30393a3030","smartrestResponse":null}

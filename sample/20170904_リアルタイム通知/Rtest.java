import abdom.data.json.*;
import abdom.data.json.object.*;
import com.ntt.tc.data.real.*;
import com.ntt.tc.data.inventory.*;
import com.ntt.tc.net.Rest;

import com.ntt.data.ByteArray;

import static com.ntt.tc.data.inventory.C8y_SupportedOperation.*;

public class Rtest {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://iottf.je1.thingscloud.ntt.com", "iottf", "us.sasaki@ntt.com", "Nttcomsasaki3");
		
		// managed object update
		ManagedObject mo = new ManagedObject();
		mo.c8y_SupportedOperations = new C8y_SupportedOperation[] { c8y_Restart, c8y_Configuration, c8y_Software, c8y_Firmware, c8y_Geofence, c8y_LogfileRequest };
		mo.type = "testDevice";
		mo.com_cumulocity_model_Agent = new JsonObject();
		Rest.Response resp = r.put("/inventory/managedObjects/2017613", mo);
		if (resp.code >= 400) throw new java.io.IOException("mo update failed"+resp.code+resp.message+resp);
		System.out.println(resp.toString("  "));
		
		// handshake
		HandshakeRequest hr = new HandshakeRequest();
		hr.advice = new Advice();
		hr.advice.interval = 60 * 1000; // 60 •b
		hr.advice.timeout	= 120 * 60 * 1000; // 120 •ª
		resp = r.post("/cep/realtime", hr);
		if (resp.code >= 400) throw new java.io.IOException("handshake failed");
		HandshakeResponse hrp = Jsonizer.fromJson(resp.toJson().get(0), HandshakeResponse.class);
		System.out.println(hrp.toString("  "));
		
		// subscribe
		SubscribeRequest cr = new SubscribeRequest();
		cr.clientId = hrp.clientId;
		cr.subscription = "/operations/2017613";
		resp = r.post("/cep/realtime", cr);
		if (resp.code >= 400) throw new java.io.IOException("subscribe failed");
		SubscribeResponse crp = Jsonizer.fromJson(resp.toJson().get(0), SubscribeResponse.class);
		System.out.println(crp.toString("  "));
		
		// connect
		resp = null;
		ConnectRequest cor = new ConnectRequest();
		cor.clientId = hrp.clientId;
		cor.advice = hr.advice;
		
		
		long t0 = System.currentTimeMillis();
		try {
			resp = r.post("/cep/realtime", cor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (resp != null) {
			System.out.println(new String(resp.toByteArray()));
			System.out.println(ByteArray.toDumpList(resp.toByteArray()));
			System.out.println(resp.message);
			System.out.println(resp.code);
			System.out.println(resp.toString("  "));
		}
		System.out.println("elapsed : " + (System.currentTimeMillis() - t0));		}
}




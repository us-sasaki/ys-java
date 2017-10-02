import abdom.data.json.*;
import abdom.data.json.object.*;
import com.ntt.tc.data.real.*;
import com.ntt.tc.data.inventory.*;
import com.ntt.tc.net.Rest;
import com.ntt.tc.net.API;

import com.ntt.data.ByteArray;

import static com.ntt.tc.data.inventory.C8y_SupportedOperation.*;

/**
 * long-polling �̃^�C���A�E�g���ԃe�X�g�p
 */
public class Rtest2 {
	public static void main(String[] args) throws Exception {
		API api = new API("https://iottf.je1.thingscloud.ntt.com", "iottf", "us.sasaki@ntt.com", "Nttcomsasaki3");
		Rest r = new Rest("https://iottf.je1.thingscloud.ntt.com", "iottf", "us.sasaki@ntt.com", "Nttcomsasaki3");
		
		// external id ���� mo �擾
		String extId = "realtest1002";
		String id = api.readIDByExternalID("com_ntt_test", extId);
		ManagedObject mo = null;
		if (id == null) {
			// �쐬
			mo = new ManagedObject();
			mo.com_cumulocity_model_Agent = new JsonObject();
			mo.c8y_IsDevice = new JsonObject();
			mo.name = "long-polling�e�X�g";
			mo.c8y_SupportedOperations =
					 new C8y_SupportedOperation[] {
					 	c8y_Restart, c8y_Configuration, c8y_Software,
					 	c8y_Firmware, c8y_Geofence, c8y_LogfileRequest };
			
			api.createManagedObject(mo);
			
			// extId �o�^
			api.createExternalID(mo.id, "com_ntt_test", extId);
			
		} else {
			mo = api.readManagedObject(id);
		}
		
		// managed object update
		
		// handshake
		HandshakeRequest hr = new HandshakeRequest();
		hr.advice = new Advice();
		hr.advice.interval = 1 * 1000; // 1 �b
		hr.advice.timeout	= 4 * 1000; // 4 �b
		Rest.Response resp = r.post("/cep/realtime", hr);
		HandshakeResponse hrp = Jsonizer.fromJson(resp.toJson().get(0), HandshakeResponse.class);
		System.out.println(hrp.toString("  "));
		
		// subscribe
		SubscribeRequest cr = new SubscribeRequest();
		cr.clientId = hrp.clientId;
		cr.subscription = "/operations/"+mo.id;
		resp = r.post("/cep/realtime", cr);
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
		System.out.println("elapsed : " + (System.currentTimeMillis() - t0));
		
		// re-connect (�������s�ł��邱�Ƃ��m�F)
		t0 = System.currentTimeMillis();
		try {
			resp = r.post("/cep/realtime", cor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("elapsed : " + (System.currentTimeMillis() - t0));
		
		// re-connect (�ł��Ȃ����Ƃ��m�F���ł���)
		Thread.sleep(5000L); // 5 �b
		
		t0 = System.currentTimeMillis();
		try {
			resp = r.post("/cep/realtime", cor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("elapsed : " + (System.currentTimeMillis() - t0));
		
		// re-connect (�ł��Ȃ����Ƃ��m�F���ł���)
		Thread.sleep(120000L); // 2 ��
		
		t0 = System.currentTimeMillis();
		try {
			resp = r.post("/cep/realtime", cor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("elapsed : " + (System.currentTimeMillis() - t0));
		// re-connect (�ł��Ȃ����Ƃ��m�F��12�͂ł��� 45�͂ł��Ă��Ȃ��@60�͂ł��ĂȂ��������@130�͂ł��Ȃ�����)
		// �ł������ǂ����́Aexception �łȂ��Asuccessful ������K�v����
		Thread.sleep(12*60*1000L); // 12 ��
		
		t0 = System.currentTimeMillis();
		try {
			resp = r.post("/cep/realtime", cor);
			System.out.println(ByteArray.toDumpList(resp.toByteArray()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("elapsed : " + (System.currentTimeMillis() - t0));
	}
}




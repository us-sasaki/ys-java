import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 3: デバイスを登録する
 * 
 * 新しいデバイスが作成された後、Step 1 に記載した既定識別子によって関連付け
 * られます。これにより、次の電源オンの後に Cumulocity での自身のデバイスを
 * 見つけられます。
 * 上の例ではハードウェアシリアル番号に関連するデバイス"2480300"が新しく割り
 * 付けられます。
 */
public class S3 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "device_ysdev000001", "Al00kgOFPv");
		JsonObject jo = new JsonObject();
		jo.add("type", "c8y_Serial")
			.add("externalId","VAIO-Serial-5102173");
			
		r.post("/identity/globalIds/12244450/externalIds", "externalId", jo.toString());
	}
}

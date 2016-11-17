import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 0: デバイスクレデンシャルを要求する
 *
 * Cumulocity に対するすべてのリクエストには認証が必要なため、デバイスからの
 * 要求もやはり認証が必要です。デバイスに個別の認証情報を付与したい場合、
 * 新しい認証情報を自動で生成する、device credentials API を利用できます。
 * これを行うには、最初の起動時にデバイスの認証情報をAPIでリクエストし、以降
 * のリクエストのためにローカルでデバイスに格納して下さい。
 * 処理は次のように進めます：
 * ・Cumulocityは、各デバイスが何らかの形式のユニークなIDを持っていると仮定
 *   しています。よいデバイスIDは、ネットワークカードのMACアドレス、モバイル
 *   デバイスのIMEI、製品シリアル番号のようなものです。
 * ・新しいデバイスを使い始めるとき、このユニークIDを Cumulocity の
 *   "Device registration" に入れてからデバイスを開始してください。
 * ・デバイスが Cumulocity に接続し、ユニークIDを続けて送信します。
 *   この目的のため、Cumulocity には固定的なホストがあります。このホストは
 *   support@cumulocity.com に聞いてください。
 * ・"Device Registration" の中で、デバイスからの接続をあなたは承認できます。
 *   この場合、 Cumulocity はデバイスに生成した認証情報を送信します。
 *
 * デバイスからみた場合、これは単一の REST リクエストです。
 *
 * <code>
 * </code>
 *
 * デバイスはこのリクエストを繰り返し発行します。ユーザがそのデバイスを
 * 登録、承認しないうちは、このリクエストは"404 Not Found." を返却します。
 * デバイスが承認されたのちは、次のようなレスポンスが返却されます。
 *
 * <code>
 * </code>
 *
 * これでデバイスはCumulocityに対し、tenantID, username, password を使用して
 * 接続することができます。
 *
 * ⇒　早速 403 forbidden が返された。nttcom.cumulo... と management.cumulo..
 *     両方で。
 * ⇒　Authorization を付けずに送ると、401 Unauthorized が返却。
 *
 * agent の真似をしたところ、以下のコードが返却された
 * <pre>
 * {
 *   "id":"5102173",
 *   "password":"WqHMSceCzd",
 *   "self":"http://management.cumulocity.com/devicecontrol/deviceCredentials/51 * 02173",
 *   "tenantId":"nttcom",
 *   "username":"device_5102173"
 * }
 * </pre>
 * また、以下は別のデバイスIDを登録したもの
 * <pre>
 * {
 *   "id":"ysdev000001",
 *   "password":"Al00kgOFPv",
 *   "self":"http://management.cumulocity.com/devicecontrol/deviceCredentials/ys * dev000001",
 *   "tenantId":"nttcom",
 *   "username":"device_ysdev000001"
 * }
 * </pre>
 */
public class S0 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "management", "devicebootstrap", "Fhdt1bb1f"); //"us.sasaki@ntt.com", "nttcomsasaki3");
		JsonObject jo = new JsonObject().add("id", "ysdev000010");
		jo.add("password",(String)null);
		jo.add("tenantId",(String)null);
		jo.add("username",(String)null);
		r.post("/devicecontrol/deviceCredentials", "deviceCredentials", jo.toString());
	}
}

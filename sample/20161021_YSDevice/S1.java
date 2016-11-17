import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 1: デバイスが登録されているか確認する
 * 
 * デバイスのユニークIDは、インベントリへのデバイスの登録に対しても使用されます。
 * この登録は Identity API を使用して実行できます。Identity API では、管理オブ
 * ジェクトは type で区別される複数のIDに関連させることができます。
 * type として例えば、製品シリアル番号に対する "c8y_Serial" や、MACアドレスに
 * 対する "c8y_MAC" や、IMEIに対する "c8y_IMEI" があります。
 * デバイスが登録されていることを確認するため、identity API に GET リクエストを
 * デバイスIDやそのtypeを使って行ってください。
 * 次の例は、Raspberry Pi の製品シリアル番号が 0000000017b79d5 であることを確認
 * します。
 *
 * <code></code>
 *
 * ⇒　結果は Response : 404
 *            Message  : Not Found
 *
 * MACアドレスはグローバルユニークに付与されるのに対し、製品シリアル番号は、
 * 異なる製品間で重複するかも知れないことに注意して下さい。
 * したがって、上の例では、シリアル番号に接頭辞 raspi- を付けています。
 * このケースでは、デバイスは既に登録されておりステータスコード 200 が
 * 返却されています。レスポンス内で、インベントリのデバイスへのURLは
 * "managedObject.self" で返却されています。このURLは後でデバイスに働きかける
 * ために利用できます。
 * デバイスがまだ登録されていない場合、404 Not Found ステータスコードと
 * エラーメッセージが返却されます。
 *
 * <code></code>
 *
 */
public class S1 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "device_ysdev000001", "Al00kgOFPv");
		r.get("/identity/externalIds/c8y_Serial/VAIO-5102173", "externalId");
	}
}

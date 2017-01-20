import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 4: インベントリのデバイスを更新する
 * 
 * 上のStep1でデバイスがすでに登録されている、と返却された場合、インベントリの
 * デバイスの表現が現在の実デバイスの状態に対して最新であることを確認する
 * 必要があります。
 * このため、インベントリのデバイスの URL に PUTリクエストが送信されます。
 * 実際に変更のあったフラグメントのみが送信されることに注意してください。
 * (フラグメントのさらなる情報は、Cumulocity のドメインモデルを参照ください)
 * 例えば、デバイスのハードウェア情報は通常変更されませんが、ソフトウェアイン
 * ストール情報は変更される可能性があります。したがって、インベントリのソフト
 * ウェア情報をデバイスリブート後に最新状態に合わせることをおわかりいただける
 * でしょう。
 *
 * <code></code>
 *
 * エージェントから、デバイスの名前を更新しないでください！ エージェントは
 * デバイスに対しデフォルト名を生成し、インベントリで識別できるようにします。
 * しかしながら、ユーザは資産管理の情報で名前を編集したり更新したりできる
 * ようにすべきです。
 *
 * Response : 406
 * Message  : Not Acceptable
 * となってしまった。ので、Accept ヘッダを付与 -> うまくいった
 */
public class S4 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "device_ysdev000001", "Al00kgOFPv");
		JsonType.setIndent(false);
		JsonObject jo = new JsonObject();
		jo.add("c8y_Software", new JsonObject().add("virtual-driver", "vd-1.0"));
		JsonType.setIndent(true);
		r.put("/inventory/managedObjects/12244450", "managedObject", jo.toString());
	}
}

/* 結果
{"assetParents":{"references":[],"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/assetParents"},"childAssets":{"references":[],"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/childAssets"},"childDevices":{"references":[{"managedObject":{"id":"9941768","self":"http://nttcom.cumulocity.com/inventory/managedObjects/9941768"},"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/childDevices/9941768"}],"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/childDevices"},"creationTime":"2016-10-24T08:51:18.349+02:00","deviceParents":{"references":[],"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/deviceParents"},"id":"12244450","lastUpdated":"2016-11-02T08:40:03.452+01:00","name":"VAIO YS's 5102173","owner":"device_ysdev000001","self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450","type":"YSAP","c8y_IsDevice":{},"c8y_Notes":"REST\u306b\u3088\u308a\u30c7\u30d0\u30a4\u30b9\u30af\u30ec\u30c7\u30f3\u30b7\u30e3\u30eb\u3067\u767b\u9332\u3057\u305f\u30c7\u30d0\u30a4\u30b9\u3002\nYS First Device \u3092\u5b50\u30c7\u30d0\u30a4\u30b9\u3068\u3057\u3066\u767b\u9332\u3057\u3066\u3044\u308b\u3002","c8y_Hardware":{"serialNumber":"5102173","CPU":"Core i5"},"c8y_Configuration":{"config":"on the YS Desk"},"c8y_Software":{"virtual-driver":"vd-1.0"}}
*/

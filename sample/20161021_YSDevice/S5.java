import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 5: 子デバイスを発見し、インベントリに生成/更新する
 * 
 * センサネットワークは複雑なので、デバイスは自分に紐づく子デバイスをもっている
 * ことがあります。よい例は、ホームオートメーションです。
 * 家庭の様々な部屋に、多数の異なるセンサ、コントロールを備えたホームオート
 * メーションゲートウェイがあるでしょう。子デバイスの登録の基本は、メインデバ
 * イスの登録に似ています。子デバイスは通常、エージェントインスタンスを
 * 実行しません。(したがって、"com_cumulocity_model_Agent" フラグメントが
 * 削除されています)
 * デバイスを子供にリンクするには、オブジェクトを生成する際に返却される子デバ
 * イスの URL に POST リクエストを送信してください。(上参照)
 * 
 * 例えば、URL "https://.../inventory/managedObjects/2543801" を持つ子デバイス
 * が登録されたとします。このデバイスに親をリンクするには、次を発行してください。
 *
 * <code></code>
 * 
 * 最後に、デバイスやリファレンスは、それらを示す URL に DELETE リクエストを
 * 発行することで削除できます。例えば、さっき作った親デバイスから子デバイスへ
 * のリファレンスは、次を発行することで削除できます。
 * 
 */
public class S5 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "us.sasaki@ntt.com", "nttcomsasaki3");
		JsonType.setIndent(false);
		JsonObject jo = new JsonObject();
		jo.add("managedObject", new JsonObject().add("self", "https://nttcom.cumulocity.com/inventory/managedObjects/9941768"));
		r.post("/inventory/managedObjects/12244450/childDevices", "managedObjectReference", jo.toString());
	}
}

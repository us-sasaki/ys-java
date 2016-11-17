import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 2: インベントリにデバイスを作成する
 * 
 * もし上記 Step1 でデバイスを表現する管理オブジェクトがない、と示されたら、
 * Cumulocity 内に管理オブジェクトを作成して下さい。管理オブジェクトは
 * デバイスのインスタンスデータとメタデータの両方を表します。
 * インスタンスデータは、シリアル番号、デバイス設定情報のようなハード
 * ウェア、ソフトウェア情報を含みます。メタデータは、サポートされる操作
 * のような、デバイスの機能を表します。
 * 管理オブジェクトを作成するには、インベントリAPIの管理オブジェクトコレクション
 * にPOSTリクエストを発行して下さい。
 * 次の例は　Linuxエージェント を使用した場合の RaspberryPi の作成です。
 * 
 *<code></code>
 *
 * 上の例は、デバイスのメタデータ項目を含んでいます。
 * ・"c8y_IsDevice" は、Cumulocity のデバイス管理で管理できることを示します
 * ・com_cumulocity_model_Agent" は、Cumulocity エージェントで実行している
 *   デバイスを示しています。
 * ・"c8y_SupportedOperations" は、このデバイスが再起動や設定ができることを
 *   述べています。さらに、ソフトウェアの実行やファームウェアのアップデート
 *   ができます。
 * さらなる情報は、デバイス管理ライブラリ　を参照下さい。
 * デバイスがうまく作られると、ステータスコード201が返却されます。
 * はじめのリクエストに例のように Accept ヘッダが含まれる場合、作成された
 * オブジェクト全体が ID と 将来のオブジェクト表現への URL をともに返却されます。
 */
public class S2 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "device_ysdev000001", "Al00kgOFPv");
		JsonObject jo = new JsonObject();
		jo.add("name", "VAIO YS's 5102173")
			.add("type", "YSAP")
			.add("c8y_IsDevice", new JsonObject())
			.add("c8y_Hardware",
						new JsonObject().add("serialNumber", "5102173")
										.add("CPU", "Core i5") )
			.add("c8y_Software",
						new JsonObject().add("virtual-driver", "vd-0.9") )
			.add("c8y_Configuration",
						new JsonObject().add("config", "on the YS Desk") );
			
		r.post("/inventory/managedObjects", "managedObject", jo.toString());
	}
}


import java.io.IOException;

import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;

import com.ntt.tc.net.Rest;

/**
 * Long-polling によるイベント監視スレッドを生成します。
 * このオブジェクトにリスナーを設定することで、Operation をハンドリング
 * できます。
 *
 * @version		29 November, 2016
 * @author		Yusuke Sasaki
 */
public class OperationWatcher implements Runnable {
	protected Device2 device;
	
	/** リスナーは１個だけ登録できる */
	protected OperationListener listener;
	
	/** デバイスクレデンシャル要求用のデバイスID(正式名称？) */
	protected DeviceCredentialsResp credential;
	
/*-------------
 * Constructor
 */
	public OperationWatcher(Device2 device) {
		this.device = device;
	}
	
	
/*------------------
 * instance methods
 */
	/**
	 * long-polling を開始します。
	 * このメソッドは、デバイスクレデンシャル取得後に呼んでください。
	 */
	public void watch() {
		Thread t = new Thread(this);
		t.start();
	}
	
/*-----------------------
 * implements (Runnable)
 */
	@Override
	public void run() {
		// Device からクレデンシャル付きの Rest を取得
		Rest r = device.getRest();
		
		// long-polling
		
		// hand-shake
		JsonType h = JsonType.o("id", "1")
					.put("supportedConnectionTypes", JsonType.a("long-polling"))
					.put("channel", "/meta/handshake")
					.put("version", "1.0");
		
	}
}

package com.ntt.tc.data.inventory;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;

import com.ntt.tc.data.C8yValue;
import com.ntt.tc.data.C8yFormatException;

/**
 * c8y_SupportedOperations lists the operations that are available for a
 * particular device, so that applications can trigger the operations.
 * For example, if the supported operations list contains "c8y_Restart"
 * (see below), a restart button will be available under the cogwheel in the
 * device management user interface.
 */
public class C8y_SupportedOperation extends C8yValue {
	/**
	 * To enable a "restart" button in the user interface, add "c8y_Restart"
	 * to the list of supported operations as described above.
	 */
	public static final C8y_SupportedOperation c8y_Restart = new C8y_SupportedOperation("c8y_Restart");
	/**
	 * To enable firmware installation through the user interface, add
	 * "c8y_Firmware" to the list of supported operations as described above.
	 */
	public static final C8y_SupportedOperation c8y_Firmware = new C8y_SupportedOperation("c8y_Firmware");
	/**
	 * To enable software installation through the user interface, add
	 * "c8y_SoftwareList" to the list of supported operations as described
	 * above.
	 */
	public static final C8y_SupportedOperation c8y_SoftwareList = new C8y_SupportedOperation("c8y_SoftwareList");
	/**
	 * To enable configuration through the user interface, add
	 * "c8y_Configuration" to the list of supported operations as described
	 * above.
	 */
	public static final C8y_SupportedOperation c8y_Configuration = new C8y_SupportedOperation("c8y_Configuration");
	/**
	 * To enable reloading configuration through the user interface, add
	 * "c8y_SendConfiguration" to the list of supported operations as
	 * described above.
	 */
	public static final C8y_SupportedOperation c8y_SendConfiguration = new C8y_SupportedOperation("c8y_SendConfiguration");
	public static final C8y_SupportedOperation c8y_Software = new C8y_SupportedOperation("c8y_Software");
	
	// 以下は、見守りテンプレート端末の managedObject で見つけたもの
	// ただし、意味は不明
	public static final C8y_SupportedOperation c8y_MotionTracking = new C8y_SupportedOperation("c8y_MotionTracking");
	public static final C8y_SupportedOperation c8y_Geofence = new C8y_SupportedOperation("c8y_Geofence");
	
	public static final C8y_SupportedOperation c8y_LogfileRequest = new C8y_SupportedOperation("c8y_LogfileRequest");
	/**
	 * 吉野さん情報(2017/7/10)
	 * String 値は、ntcagent, dmesg, logread, ipsec が取れるらしい
	 */
	public static final String[] c8y_SupportedLogs;
	
	/**
	 * Operation を表す文字列を格納する。
	 * このオブジェクトは JsonValue(string) であることが保証され、JsonValue
	 * は不変オブジェクトのため、toJson() で新たにインスタンスを生成しないよう
	 * JsonValue 形式として保持する。
	 */
	protected JsonValue operation;
	
/*-------------
 * constructor
 */
	public C8y_SupportedOperation() {
	}
	
	public C8y_SupportedOperation(JsonType operation) {
		if (operation.getType() != JsonType.TYPE_STRING) {
			throw new C8yFormatException();
		}
		this.operation = (JsonValue)operation;
	}
	
	public C8y_SupportedOperation(String operation) {
		this.operation = new JsonValue(operation);
	}
	
/*-----------
 * overrides
 */
	@Override
	public void fill(Jsonizable value) {
		JsonType jt = value.toJson();
		if (jt.getType() != JsonType.TYPE_STRING)
			throw new C8yFormatException();
		operation = (JsonValue)jt;
	}
	
	@Override
	public JsonType toJson() {
		return operation;
	}
}

package com.ntt.tc.data.inventory;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;

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
	public static final JsonValue c8y_Restart = new JsonValue("c8y_Restart");
	/**
	 * To enable firmware installation through the user interface, add
	 * "c8y_Firmware" to the list of supported operations as described above.
	 */
	public static final JsonValue c8y_Firmware = new JsonValue("c8y_Firmware");
	/**
	 * To enable software installation through the user interface, add
	 * "c8y_SoftwareList" to the list of supported operations as described
	 * above.
	 */
	public static final JsonValue c8y_SoftwareList = new JsonValue("c8y_SoftwareList");
	/**
	 * To enable configuration through the user interface, add
	 * "c8y_Configuration" to the list of supported operations as described
	 * above.
	 */
	public static final JsonValue c8y_Configuration = new JsonValue("c8y_Configuration");
	/**
	 * To enable reloading configuration through the user interface, add
	 * "c8y_SendConfiguration" to the list of supported operations as
	 * described above.
	 */
	public static final JsonValue c8y_SendConfiguration = new JsonValue("c8y_SendConfiguration");
	public static final JsonValue c8y_Software = new JsonValue("c8y_Software");
	
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
	public void fill(JsonType value) {
		if (value.getType() != JsonType.TYPE_STRING)
			throw new C8yFormatException();
		operation = (JsonValue)value;
	}
	
	@Override
	public JsonType toJson() {
		return operation;
	}
}
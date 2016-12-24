package abdom.data.json.object;

/**
 * JData において、指定された JsonType フィールドと JData インスタンス
 * フィールドに型の不一致があった場合にスローされる例外です。
 * 通常、設定値(JsonType)側に問題があります。
 */
public class IllegalFieldTypeException extends RuntimeException {
	public IllegalFieldTypeException() {
		super();
	}
	public IllegalFieldTypeException(String msg) {
		super(msg);
	}
}

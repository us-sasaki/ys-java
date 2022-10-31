package abdom.data.json.object;

/**
 * JData において、指定された JsonType フィールドと JData インスタンス
 * フィールドに型の不一致があった場合にスローされる例外です。
 * 通常、設定値(JsonType)側に問題があります。
 */
public class IllegalFieldTypeException extends IllegalArgumentException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1731684087349098474L;

	public IllegalFieldTypeException() {
		super();
	}
	public IllegalFieldTypeException(String msg) {
		super(msg);
	}
	public IllegalFieldTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public IllegalFieldTypeException(Throwable cause) {
		super(cause);
	}
}

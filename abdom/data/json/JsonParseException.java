package abdom.data.json;

/**
 * JsonType.parse において、指定された文字列のフォーマットが
 * JSON 文法上誤っていた場合にスローされる例外です。
 */
public class JsonParseException extends RuntimeException {
	public JsonParseException() {
		super();
	}
	public JsonParseException(String msg) {
		super(msg);
	}
}

package abdom.data.json;

/**
 * Json形式におけるプリミティブ型(文字列,数値)を表します
 */
public class JsonValue extends JsonType {
	public String value;
	public String quote = "";
	
/*-------------
 * constructor
 */
	/**
	 * \ でエスケープすべき文字もそのまま保存し、toString で返してしまう。
	 * ので、\", \', \\ は toString() 時にエスケープするよう変更する
	 */
	public JsonValue(String value) {
		if (value == null) this.value = "null";
		else {
			this.value = value;
			quote = "\"";
		}
	}
	
	public JsonValue(byte value) {	this.value = String.valueOf(value);	}
	public JsonValue(char value) {
		this.value = String.valueOf(value);
		quote = "\""; // string 扱い
	}
	public JsonValue(short value) { this.value = String.valueOf(value); }
	public JsonValue(int  value) {	this.value = String.valueOf(value); }
	public JsonValue(long value) {	this.value = String.valueOf(value); }
	public JsonValue(float value) {	this.value = String.valueOf(value); }
	public JsonValue(double value) {	this.value = String.valueOf(value); }
	public JsonValue(boolean value) {	this.value = value?"true":"false"; }
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		return toString("", false);
	}
	@Override
	protected String toString(String indent, boolean objElement) {
		return indent+quote+value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'")+quote; // "string" / number の形式
	}
}

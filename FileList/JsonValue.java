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
		this.value = value;
		quote = "\"";
	}
	
	public JsonValue(byte value) {	this.value = String.valueOf(value);	}
	public JsonValue(char value) {
		this.value = String.valueOf(value);
		quote = "\""; // string 扱い
	}
	public JsonValue(int  value) {	this.value = String.valueOf(value); }
	public JsonValue(long value) {	this.value = String.valueOf(value); }
	public JsonValue(float value) {	this.value = String.valueOf(value); }
	public JsonValue(double value) {	this.value = String.valueOf(value); }
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		return toString("");
	}
	@Override
	public String toString(String indent) {
		return indent+quote+value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'")+quote; // "string" / number の形式
	}
}

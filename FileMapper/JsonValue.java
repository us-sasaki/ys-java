public class JsonValue extends JsonType {
	public String value;
	
	public JsonValue(String value) {
		this.value = value;
	}
	
	public JsonValue(byte value) {	this.value = String.valueOf(value);	}
	public JsonValue(char value) {	this.value = String.valueOf(value); }
	public JsonValue(int  value) {	this.value = String.valueOf(value); }
	public JsonValue(long value) {	this.value = String.valueOf(value); }
	
	public String toString() {
		return "\""+value+"\""; // "value" ‚ÌŒ`Ž®
	}
}

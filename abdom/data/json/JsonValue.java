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
			this.value = escapeControlCodes(value);
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
	
	private static String escapeControlCodes(String value) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\'':
				sb.append("\\\'");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public String getValue() {
		// unescape
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = sb.charAt(i);
			if (c == '\\') {
				if (i++ == value.length()) break; // illegal but exit
				char c = sb.charAt(i);
				switch (c) {
				case 'b':
					sb.append('\b');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'f':
					sb.append('\f');
					break;
				case '\'':
					sb.append('\'');
					break;
				case '\"':
					sb.append('\"');
					break;
				case '\\':
					sb.append('\\');
					break;
				case 'u':
					if (i+4 >= value.length()) throw new InternalError();
					String hex = sb.substring(i, i+4);
					i += 4;
					char u = (char)Integer.parseInt(hex, 16);
					sb.append(u);
					break;
				default:
					throw new InternalError();
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
/*-----------
 * overrides
 */
	@Override
	public String getValue() {
	}
	
	@Override
	public String toString() {
		return quote+value+quote;
	}
	@Override
	protected String toString(String indent, String indentStep, int textwidth, boolean objElement) {
		return indent+quote+value+quote;
//		return indent+quote+value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'")+quote; // "string" / number の形式
	}
}

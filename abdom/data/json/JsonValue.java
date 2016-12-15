package abdom.data.json;

/**
 * Json形式におけるプリミティブ型(文字列,数値)を表します。
 * 文字列は内部的にはコントロールコードをエスケープシーケンスした文字列として
 * 保持します。getValue() でエスケープシーケンスを解除します。
 *
 * @version		November 25, 2016
 * @author		Yusuke Sasaki
 */
public class JsonValue extends JsonType {
	public String value;
	public String quote = "";
	
/*-------------
 * constructor
 */
	/**
	 * 指定された文字列を保持する Json 形式を作成します。
	 *
	 * @param	value	保持する String
	 */
	public JsonValue(String value) {
		if (value == null) this.value = "null";
		else {
			this.value = escapeControlCodes(value);
			quote = "\"";
		}
	}
	
	public JsonValue(byte value) {	this.value = String.valueOf(value);	}
	public JsonValue(char value) {	this(String.valueOf(value));	}
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
			case '\b':	sb.append("\\b");	break;
			case '\t':	sb.append("\\t");	break;
			case '\n':	sb.append("\\n");	break;
			case '\r':	sb.append("\\r");	break;
			case '\f':	sb.append("\\f");	break;
			case '\'':	sb.append("\\\'");	break;
			case '\"':	sb.append("\\\"");	break;
			case '\\':	sb.append("\\\\");	break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * この JsonValue の持つ文字列値(Java値)を返却します。
	 * クオーテーションやエスケープシーケンスは解除されます。
	 *
	 * @return	String 値
	 */
	@Override
	public String getValue() {
		if ("".equals(quote) && "null".equals(value)) return null;
		
		// unescape
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '\\') {
				if (i++ == value.length()) break; // illegal but exit
				c = value.charAt(i);
				switch (c) {
				case 'b':	sb.append('\b');	break;
				case 't':	sb.append('\t');	break;
				case 'n':	sb.append('\n');	break;
				case 'r':	sb.append('\r');	break;
				case 'f':	sb.append('\f');	break;
				case '\'':	sb.append('\'');	break;
				case '\"':	sb.append('\"');	break;
				case '\\':	sb.append('\\');	break;
				case 'u':
					if (i+4 >= value.length()) throw new InternalError();
					String hex = value.substring(i+1, i+5);
					i += 5;
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
	
	@Override
	public int getIntValue() {
		return Integer.parseInt(value);
	}
	
	@Override
	public double getDoubleValue() {
		return Double.parseDouble(value);
	}
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		return quote+value+quote;
	}
	@Override
	protected String toString(String indent, String indentStep, int textwidth, boolean objElement) {
		return indent+quote+value+quote;
	}
}

package abdom.data.json;

/**
 * Json形式におけるプリミティブ型(文字列,数値)を表します。
 * 文字列は内部的にはコントロールコードをエスケープシーケンスした文字列として
 * 保持します。getValue() でエスケープシーケンスを解除します。
 * このクラスのオブジェクトは不変です。
 *
 * @version		November 25, 2016
 * @author		Yusuke Sasaki
 */
public class JsonValue extends JsonType {
	protected String value;
	protected String quote = "";
	
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
	
/*------------------
 * instance methods
 */
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
	
/*-----------
 * overrides
 */
	/**
	 * この JsonValue の持つ文字列値(Java値)を返却します。
	 * クオーテーションやエスケープシーケンスは解除されます。
	 * 特に、このオブジェクトが null (TYPE_VOID) を表す場合、null (Java の)
	 * が返却されます。
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
					// value に不正なエスケープ文字が入ることはない
					throw new InternalError();
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	@Override
	public int intValue() {
		return Integer.parseInt(value);
	}
	
	@Override
	public long longValue() {
		return Long.parseLong(value);
	}
	
	@Override
	public float floatValue() {
		return Float.parseFloat(value);
	}
	
	@Override
	public double doubleValue() {
		return Double.parseDouble(value);
	}
	
	/**
	 * boolean 値を取得します。
	 * false となるのは、boolean の false となる場合か、null の場合に限られます。
	 * その他(true、数値、文字列)の場合、true が返却されます。
	 *
	 * @return		このオブジェクトの boolean としての値
	 */
	@Override
	public boolean booleanValue() {
		return !("".equals(quote) && ("false".equals(value) || "null".equals(value)));
	}
	
	@Override
	public int getType() {
		if ("\"".equals(quote)) return TYPE_STRING;
		if ("null".equals(value)) return TYPE_VOID;
		if ("true".equals(value)) return TYPE_BOOLEAN;
		if ("false".equals(value)) return TYPE_BOOLEAN;
		try {
			Long.parseLong(value);
			return TYPE_INT;
		} catch (NumberFormatException nfe) {
			try {
				Double.parseDouble(value);
				return TYPE_DOUBLE;
			} catch (NumberFormatException nfe2) {
			}
		}
		return TYPE_UNKNOWN;
	}
	
	@Override
	public String toString() {
		return quote+value+quote;
	}
	@Override
	protected String toString(String indent, String indentStep, int textwidth, boolean objElement) {
		checkIndentIsWhiteSpace(indent);
		return indent+quote+value+quote;
	}
	
	@Override
	public boolean equals(Object target) {
		if (!(target instanceof JsonValue)) return false;
		JsonValue jv = (JsonValue)target;
		return (jv.quote.equals(quote) && jv.value.equals(value));
	}
	
	@Override
	public int hashCode() {
		return quote.hashCode() ^ value.hashCode();
	}
}

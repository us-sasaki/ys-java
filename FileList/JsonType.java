import java.util.Deque;

/**
 * Json形式における型一般を表します(composite pat.)
 * 利便性のため、アクセスメソッドを提供します。
 */
public abstract class JsonType {

	public String getValue() {
		return ((JsonValue)this).value; // may throw ClassCastException
	}
	public JsonType get(String key) {
		JsonObject jo = (JsonObject)this; // may throw ClassCastException
		return jo.map.get(key);
	}
	public JsonType get(int index) {
		JsonArray ja = (JsonArray)this; // may throw ClassCastException
		return ja.array[index]; // may throw ArrayIndexOutOfBoundsException
	}
	public int size() {
		JsonArray ja = (JsonArray)this; // may throw ClassCastException
		return ja.array.length;
	}
	
	private static class ArrayMark extends JsonType {
		boolean open = true; // true .. [   false .. ]
	}
	private static class ObjectMark extends JsonType {
		boolean open = true; // true .. {   false .. }
	}
	private static class Comma extends JsonType {
	}
	private static class Colon extends JsonType {
	}
	private static class ObjectField extends JsonType {
		String name;
		String value; // クォーテーションがあったりなかったりする
		private ObjectField(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	private static class JString extends JsonType {
		String name; // クォーテーションがあったりなかったりする
		private JString(String name) { this.name = name; }
	}
/*	public static JsonType parse(String json) {
		Deque<JsonType> stack = new Deque<JsonType>();
		//
		// まず、要素分解して List に格納
		// 要素
		// [ ] { } , : "--" number
		//
		List<JsonType> split = new ArrayList<JsonType>();
		boolean inString = false;
		boolean inNumber = false;
		StringBuilder sb = null;
		for (int i = 0; i < json.length(); i++) {
			char c = json.charAt(i);
			if (inString) {
				// ダブルクォーテーションの途中
				
			} else if (inNumber) {
				// 数値型の途中
				if (c >= '0' && c <= '9') sb.append(c);
				else {
					inNumber = false;
					if (c == ' ' || c == '\e' || c == '\r' || c == '\t') continue;
					if (c == ',' || c == ']' || c == '}') {
						i--; // 再評価させるため戻す
						continue;
					}
					throw new RuntimeException("数値フォーマットエラー");
				}
			} else {
				if (c == ' ' || c == '\e' || c == '\r' || c == '\t') continue;
				if (c == '[') split.add(new ArrayMark(true));
				else if (c == ']') split.add(new ArrayMark(false));
				else if (c == '{') split.add(new ObjectMark(true));
				else if (c == '}') split.add(new ObjectMark(false));
				else if (c == ':') split.add(new Colon());
				else if (c == ',') split.add(new Comma());
				else if (c >= '0' && c <= '9') {
					inNumber = true;
					sb = new StringBuilder();
					sb.append(c);
				} else if (c == '\"') {
					inString = true;
					sb = new StringBuilder();
					sb.append('\"');
					sb.append(c);
				}
			}
		}
		
		
		
		
		boolean quoted = false;
		boolean afterColon = false;
		StringBuilder sb = null;
		for (int i = 0; i < json.length(); i++) {
			char c = json.charAt(i);
			if (quoted) {
				// ダブルクォーテーションの途中
				if (c == '\\') {
					if (i == json.length()-1)
						throw new RuntimeException("末尾に\\があります");
					c = json.charAt(++i); // エスケープ文字
					if (c == '\\') sb.append(c);
					else if (c == '\'') sb.append('\'');
					else if (c == '\"') sb.append('\"');
					else if (c == 'n') sb.append('\n');
					else if (c == 'r') sb.append('\r');
					else if (c == 't') sb.append('\t');
					else throw new RuntimeException("\\の次に来ている文字が不正です:"+c);
				} else {
					if (c == '\"') {
						// 文字列を抽出した
						quoted = false;
						if (afterColon) {
							// : のあと
							JsonType jt = stack.pop();
							FieldName f = (FieldName)jt;
							stack.push(new ObjectField(f.name, sb.toString());
							afterColon = false;
						} else {
							stack.push(new JString(sb.toString());
						}
					}
					sb.append(c); // 文字列の途中なので、すべての文字を追加
					// 改行はエラーとすべき
				}
			}
			else {
				// 通常状態
				if (c == ' ' || c == '\e' || c == '\r' || c == '\t') continue;
				if (c == '\"') quoted = true;
		}
	}

	/**
	 * 人が見やすい indent に対応するためのメソッド
	 */
	public String toString(String indent) {
		return indent + toString(); // デフォルトの実装
	}

}

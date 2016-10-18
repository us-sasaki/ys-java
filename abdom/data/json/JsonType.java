package abdom.data.json;

import java.util.Deque;

/**
 * Json�`���ɂ�����^��ʂ�\���܂�(composite pat.)
 * �L���X�g�����ɗ��p�ł���悤�A�A�N�Z�X���\�b�h��񋟂��܂��B
 * �A�N�Z�X�ł��Ȃ��^�ł������ꍇ�AClassCastException ���������܂��B
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
	public JsonObject add(String name, JsonType t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, String t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, boolean t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, byte t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, char t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, short t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, int t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, long t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, float t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, double t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, JsonType[] t) {
		return ((JsonObject)this).add(name, t);
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
		String value; // �N�H�[�e�[�V��������������Ȃ������肷��
		private ObjectField(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	private static class JString extends JsonType {
		String name; // �N�H�[�e�[�V��������������Ȃ������肷��
		private JString(String name) { this.name = name; }
	}
/*	public static JsonType parse(String json) {
		Deque<JsonType> stack = new Deque<JsonType>();
		//
		// �܂��A�v�f�������� List �Ɋi�[
		// �v�f
		// [ ] { } , : "--" number
		//
		List<JsonType> split = new ArrayList<JsonType>();
		boolean inString = false;
		boolean inNumber = false;
		StringBuilder sb = null;
		for (int i = 0; i < json.length(); i++) {
			char c = json.charAt(i);
			if (inString) {
				// �_�u���N�H�[�e�[�V�����̓r��
				
			} else if (inNumber) {
				// ���l�^�̓r��
				if (c >= '0' && c <= '9') sb.append(c);
				else {
					inNumber = false;
					if (c == ' ' || c == '\e' || c == '\r' || c == '\t') continue;
					if (c == ',' || c == ']' || c == '}') {
						i--; // �ĕ]�������邽�ߖ߂�
						continue;
					}
					throw new RuntimeException("���l�t�H�[�}�b�g�G���[");
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
				// �_�u���N�H�[�e�[�V�����̓r��
				if (c == '\\') {
					if (i == json.length()-1)
						throw new RuntimeException("������\\������܂�");
					c = json.charAt(++i); // �G�X�P�[�v����
					if (c == '\\') sb.append(c);
					else if (c == '\'') sb.append('\'');
					else if (c == '\"') sb.append('\"');
					else if (c == 'n') sb.append('\n');
					else if (c == 'r') sb.append('\r');
					else if (c == 't') sb.append('\t');
					else throw new RuntimeException("\\�̎��ɗ��Ă��镶�����s���ł�:"+c);
				} else {
					if (c == '\"') {
						// ������𒊏o����
						quoted = false;
						if (afterColon) {
							// : �̂���
							JsonType jt = stack.pop();
							FieldName f = (FieldName)jt;
							stack.push(new ObjectField(f.name, sb.toString());
							afterColon = false;
						} else {
							stack.push(new JString(sb.toString());
						}
					}
					sb.append(c); // ������̓r���Ȃ̂ŁA���ׂĂ̕�����ǉ�
					// ���s�̓G���[�Ƃ��ׂ�
				}
			}
			else {
				// �ʏ���
				if (c == ' ' || c == '\e' || c == '\r' || c == '\t') continue;
				if (c == '\"') quoted = true;
		}
	}

	/**
	 * �l�����₷�� indent �ɑΉ����邽�߂̃��\�b�h
	 */
	public String toString(String indent) {
		return indent + toString(); // �f�t�H���g�̎���
	}

}

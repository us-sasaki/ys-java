package abdom.data.json;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Json�`���ɂ�����^��ʂ�\���܂�(composite pat.)
 * �L���X�g�����ɗ��p�ł���悤�A�A�N�Z�X���\�b�h��񋟂��܂��B
 * �A�N�Z�X�ł��Ȃ��^�ł������ꍇ�AClassCastException ���������܂��B
 */
public abstract class JsonType {
	static String ls = System.getProperty("line.separator");
	static String indent = "  ";
	

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
	public Set<String> keySet() {
		return ((JsonObject)this).map.keySet(); // may throw ClassCastException
	}
	
	/**
	 * �l�����₷�� indent �ɑΉ����邽�߂̃��\�b�h
	 */
	public String toString(String indent) {
		return indent + toString(); // �f�t�H���g�̎���
	}
	
/*---------------
 * class methods
 */
	public static void setIndent(boolean withIndent) {
		if (withIndent) {
			ls = System.getProperty("line.separator");
			indent = "  ";
		} else {
			ls = "";
			indent = "";
		}
	}
	
	public static JsonType parse(String str) {
		try {
			return parse(new StringReader(str));
		} catch (IOException e) {
			throw new InternalError("StringReader �� IOException ���������܂���"+e);
		}
	}
	
	/**
	 * �w�肳�ꂽ InputStream ���� JSON value ���P�ǂݍ��݂܂��B
	 * InputStream ��JSON value�I���ʒu�܂œǂݍ��܂�Aclose() ����܂���B
	 * InputStream �͓����I�� PushbackInputStream �Ƃ��ė��p����܂��B
	 */
	public static JsonType parse(Reader in) throws IOException {
		PushbackReader pr = new PushbackReader(in);
		return parseValue(pr);
	}
	
	/**
	 * �X�y�[�X�������X�L�b�v���܂��B
	 * �X�y�[�X�����́Aspace, tab, cr, lf �ł��B
	 */
	private static void skipspaces(PushbackReader pr) throws IOException {
		while (true) {
			int c = pr.read();
			switch (c) {
			case ' ': continue;
			case '\t': continue;
			case '\r': continue;
			case '\n': continue;
			case -1: return;
			default:
				pr.unread(c);
				return;
			}
		}
	}
	
	/**
	 * �w�肳�ꂽ PushbackInputStream ���� JSON value ���P�ǂݍ��݁A
	 * JsonType �Ƃ��ĕԋp���܂��B
	 */
	private static JsonType parseValue(PushbackReader pr) throws IOException {
		skipspaces(pr);
		int c = pr.read();
		JsonType jt = null;
		switch (c) {
		case '-':
			pr.unread(c);
			jt = parseNumber(pr);
			break;
		case '{':
			jt = parseObject(pr);
			break;
		case '[':
			jt = parseArray(pr);
			break;
		case 't':
			expect(pr, "true");
			jt = new JsonValue(true);
			break;
		case 'f':
			expect(pr, "false");
			jt = new JsonValue(false);
			break;
		case 'n':
			expect(pr, "null");
			jt = new JsonValue(null);
			break;
		case '\"':
			jt = parseString(pr);
			break;
		default:
			if (c >= '0' && c <= '9') {
				pr.unread(c);
				jt = parseNumber(pr);
			}
		}
		if (jt == null)	throw new JsonParseException("value �̐擪�������s���ł� : " + (char)c);
		return jt;
	}
	
	/**
	 * �w�肵��������ƂȂ��Ă��邱�Ƃ��`�F�b�N���܂��B
	 * �X�g���[���̏I�������o������A�w�肵��������ƈقȂ��Ă���ꍇ�A
	 * JsonParseException ���X���[���܂��B
	 */
	private static void expect(PushbackReader pr, String expected) throws IOException {
		for (int i = 1; i < expected.length(); i++) {
			int c = pr.read();
			if (c == -1) throw new JsonParseException("�\�����Ȃ��I�������o���܂����B�\������������:"+expected);
			if (expected.charAt(i) != (char)c) throw new JsonParseException("�\�����Ȃ����������o���܂���:"+(char)c+" �\����������:"+expected.charAt(i)+" �\������������:"+expected);
		}
	}
	
	/**
	 * ���l�̉\���̂���g�[�N��(0-9, -+.eE ����Ȃ镶����)�𒊏o����B
	 */
	private static String readNumberToken(PushbackReader pr) throws IOException {
		StringBuilder result = new StringBuilder();
		while (true) {
			int c = pr.read();
			if (c == -1) break;
			if ((c >= '0' && c <= '9') || (c == '-' || c == '+' ||
					c == '.' || c == 'e' || c == 'E')) {
				result.append((char)c);
			} else {
				pr.unread(c);
				break;
			}
		}
		return result.toString();
	}
	
	/**
	 * ���l��ǂݍ��݂܂��B���l�łȂ��ꍇ�AJsonParseException ���X���[���܂��B
	 */
	private static JsonValue parseNumber(PushbackReader pr) throws IOException {
		String token = readNumberToken(pr);
		try {
			if (token.indexOf('.')>-1||token.indexOf('e')>-1||token.indexOf('E')>-1) {
				double v = Double.parseDouble(token);
				return new JsonValue(v);
			} else {
				int v = Integer.parseInt(token);
				return new JsonValue(v);
			}
		} catch (NumberFormatException nfe) {
			throw new JsonParseException("���l�t�H�[�}�b�g�ُ� : " + token);
		}
	}
	
	/**
	 * " �̎��̕����ɃX�g���[��������O��ŁA������������擾���܂��B
	 */
	private static JsonValue parseString(PushbackReader pr) throws IOException {
		return new JsonValue(readString(pr));
	}
	
	private static String readString(PushbackReader pr) throws IOException {
		StringBuilder result = new StringBuilder();
		while (true) {
			int c = pr.read();
			if (c == -1) throw new JsonParseException("������̓r���ŗ\�����Ȃ��I�������m���܂���");
			if (c == '\"') return result.toString();
			if (c < 32) throw new JsonParseException("������̓r���ŉ��s�Ȃǂ̃R���g���[���R�[�h�����m���܂����Bcode = " + c);
			if (c == '\\') {
				c = pr.read();
				if (c == -1) throw new JsonParseException("\\ �̎��ɗ\�����Ȃ��I�������m���܂���");
				switch (c) {
				case '\"':
				case '\\':
				case '/':
				case 'b':
				case 'f':
				case 'n':
				case 'r':
				case 't':
					result.append('\\');
					result.append((char)c);
					continue;
				case 'u':
					result.append('\\');
					result.append((char)c);
					for (int i = 0; i < 4; i++) {
						c = pr.read();
						if (c >= '0' && c <= '9') result.append( (char)c );
						else if (c >= 'A' && c <= 'F') result.append( (char)c );
						else if (c >= 'a' && c <= 'f') result.append( (char)c );
						else throw new JsonParseException("\\u�̌�̕����񂪕s���ł� : " + (char)c);
					}
				}
			}
			result.append((char)c);
		}
	}
	/**
	 * [ ������O��ŁA�����z����擾���܂��B
	 */
	private static JsonArray parseArray(PushbackReader pr) throws IOException {
		List<JsonType> array = new ArrayList<JsonType>();
		skipspaces(pr);
		int c = pr.read();
		if (c == ']') {
			return new JsonArray(array.toArray(new JsonType[0]));
		}
		pr.unread(c);
		while (true) {
			skipspaces(pr);
			JsonType j = parseValue(pr);
			array.add(j);
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("�z��̏I��̑O�ɏI�������m���܂���");
			if (c == ']') {
				return new JsonArray(array.toArray(new JsonType[0]));
			}
			if (c != ',') throw new JsonParseException("�z����ɕs���ȕ��������o���܂��� : " + (char)c);
		}
	}
	
	/**
	 * { ������O��ŁA�����I�u�W�F�N�g���擾���܂��B
	 */
	private static JsonObject parseObject(PushbackReader pr) throws IOException {
		JsonObject result = new JsonObject();
		skipspaces(pr);
		int c = pr.read();
		if (c == '}') return result;
		pr.unread(c);
		
		while (true) {
			skipspaces(pr);
			c = pr.read();
			if (c != '\"') throw new JsonParseException("�I�u�W�F�N�g���̗v�f���� \" �Ŏn�܂��Ă��܂���");
			String name = readString(pr);
			// ������ name �Ƃ��ē����Ă��Ă͂Ȃ�Ȃ��������`�F�b�N
			// �������A�K���������ĂȂ��̂Ŏ蔲��
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("�I�u�W�F�N�g�̗v�f���̌�ɗ\�����Ȃ��I�������m���܂���");
			if (c != ':') throw new JsonParseException("�I�u�W�F�N�g�̗v�f���̌�ɗ\�����Ȃ����������m���܂��� : "+(char)c);
			skipspaces(pr);
			JsonType jt = parseValue(pr);
			result.add(name, jt);
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("�I�u�W�F�N�g�̏I��̑O�ɗ\�����Ȃ��I�������m���܂���");
			if (c == ',') continue;
			if (c == '}') return result;
			throw new JsonParseException("�I�u�W�F�N�g���ɕs���ȕ��������o���܂��� : " + (char)c);
		}
	}

}
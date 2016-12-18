package abdom.data.json;

import java.util.Map;
import java.util.TreeMap;

/**
 * Json�`���ɂ�����I�u�W�F�N�g��\���܂��B
 * ���̃N���X�̃I�u�W�F�N�g�̓X���b�h�Z�[�t�ł͂���܂���B
 */
public class JsonObject extends JsonType {
	protected Map<String, JsonType> map;
	
/*-------------
 * constructor
 */
	public JsonObject() {
		map = new TreeMap<String, JsonType>();
	}
	
/*------------------
 * instance methods
 */

/*
 * add methods
 */
	@Override
	public JsonObject add(String name, JsonType obj) {
		return addImpl(name, obj);
	}
	@Override
	public JsonObject add(String name, String value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, boolean value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, byte value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, char value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, int value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, long value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, float value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, double value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, JsonType[] array) {
		return addImpl(name, new JsonArray((Object[])array));
	}
	
	private JsonObject addImpl(String name, JsonType t) {
		if (map.containsKey(name)) {
			// ���� name �̃G���g�������łɂ������ꍇ�Avalue �� JsonArray ������
			JsonType v = map.get(name);
			if (v instanceof JsonArray) {
				// ���ł� JsonArray �ɂȂ��Ă����ꍇ�A�v�f�ǉ�
				JsonArray src = (JsonArray)v;
				src.array.add(t);
				return this;
			} else {
				// JsonArray �ɂȂ��Ă��Ȃ��ꍇ�AJsonArray������
				JsonArray newArray = new JsonArray();
				newArray.array.add(v);
				newArray.array.add(t);
				
				map.put(name, newArray);
				return this;
			}
		} else {
			// ���񏉂߂Ă̒ǉ�(�ʗႱ�̏ꍇ�ƂȂ�)
			map.put(name, t);
			return this;
		}
	}
	
/*
 * put methods
 */
	@Override
	public JsonObject put(String name, JsonType obj) {
		return putImpl(name, obj);
	}
	@Override
	public JsonObject put(String name, String value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, boolean value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, byte value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, char value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, int value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, long value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, float value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, double value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, JsonType[] array) {
		return putImpl(name, new JsonArray((Object[])array));
	}
	
	private JsonObject putImpl(String name, JsonType t) {
		// �㏑��
		map.put(name, t);
		return this;
	}
	
/*-----------
 * overrides
 */
	@Override
	public JsonType get(String key) {
		return map.get(key);
	}
	
	@Override
	public JsonType cut(String key) {
		return map.remove(key);
	}
	
	@Override
	public java.util.Set<String> keySet() {
		return map.keySet();
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (String name : map.keySet() ) {
			if (!first) sb.append(",");
			else first = false;
			sb.append("\"");
			sb.append(name);
			sb.append("\":");
			JsonType jt = map.get(name);
			sb.append(jt);
		}
		sb.append("}");
		
		return sb.toString();
	}
	
	@Override
	protected String toString(String indent, String indentStep,
						int textwidth, boolean objElement) {
		StringBuilder sb = new StringBuilder();
		
		// object �ŁA"name": �̌ゾ���C���f���g�����Ȃ����߂̃t���O
		if (!objElement) sb.append(indent);
		sb.append('{');
		boolean first = true;
		for (String name : map.keySet() ) {
			if (!first) sb.append(',');
			else first = false;
			sb.append(JsonType.LS);
			sb.append(indent);
			sb.append(indentStep);
			sb.append("\"");
			sb.append(name);
			sb.append("\": ");
			JsonType jt = map.get(name);
			if (jt instanceof JsonValue) sb.append(jt);
			else if (jt instanceof JsonObject) {
				// JsonObject �̏ꍇ
				if (((JsonObject)jt).map.size() == 0) sb.append("{}");
				else if (textwidth > 0) {
					// JsonObject �ŁAtextWidth �w��͈̔͂ň�s�������݂�
					int len = indent.length() + indentStep.length() +
								5 + name.length();
					String tryShort = jt.toString();
					if (len + tryShort.length() <= textwidth) {
						sb.append(tryShort);
					} else {
						sb.append(jt.toString(indent+indentStep, indentStep,
									textwidth, true));
					}
				} else {
					sb.append(jt.toString(indent+indentStep, indentStep,
									textwidth, true));
				}
			} else if (((JsonArray)jt).array.size() == 0) {
				// JsonArray �ł́A�v�f���� 0 �̏ꍇ�ȗ��\��
				// 1 �ȏ�̏ꍇ�� textwidth �w��ɂ��
				sb.append("[]");
			} else if (textwidth > 0) {
				// JsonArray �ŁAtextwidth �w��͈̔͂ň�s�������݂�
				
				// ��s�������݂�(toString() �ɂ��A�������J�E���g)
				int len = indent.length() + indentStep.length()
							+ 5 + name.length();
				String tryShort = jt.toString();
				if (len + tryShort.length() <= textwidth) {
					// textwidth �ȓ��ɂ͂܂�
					sb.append(tryShort);
				} else {
					sb.append(jt.toString(indent+indentStep, indentStep, textwidth, true));
				}
			} else {
				// textwidth �� 0�ȉ����w�肷��ƁA��s�������݂Ȃ�(����)
				sb.append(jt.toString(indent+indentStep, indentStep, textwidth, true));
			}
		}
		sb.append(JsonType.LS);
		sb.append(indent);
		sb.append('}');
		
		return sb.toString();
	}
}

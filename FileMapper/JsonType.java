import java.util.*;

/**
 * Json�`���ɂ�����^��ʂ�\���܂�(composite pat.)
 * ���֐��̂��߁A�A�N�Z�X���\�b�h��񋟂��܂��B
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
	
//	public static JsonType parse(String json) {
//	�������Bstack �Ŏ�������Ɨǂ�����
//	}

	/**
	 * �l�����₷�� indent �ɑΉ����邽�߂̃��\�b�h
	 */
	public abstract String toString(String indent);

}

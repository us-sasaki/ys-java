package abdom.data.json;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Json�`���ɂ�����z���\���܂��B
 * �����ł͔z��� ArrayList<JsonType> �ŕێ����܂��B
 * ���̃N���X�̃I�u�W�F�N�g�̓X���b�h�Z�[�t�ł͂���܂���B
 */
public class JsonArray extends JsonType implements Iterable<JsonType> {
	protected List<JsonType> array = new ArrayList<JsonType>();
	
/*-------------
 * constructor
 */
	/**
	 * ��(�v�f��0)�� JsonArray ���쐬���܂��B
	 */
	public JsonArray() {
	}
	
	/**
	 * �w�肳�ꂽ�v�f������ JsonArray ���쐬���܂��B
	 *
	 * @param	array	�z��v�f�̎w��
	 */
	public JsonArray(Object... array) {
		set(array);
	}
	
/*-----------
 * overrides
 */
	@Override
	public void set(Object... toSet) {
		this.array.clear();
		
		for (Object t : toSet) {
			if (t instanceof JsonType) array.add((JsonType)t);
			else if (t instanceof String)
				array.add(new JsonValue((String)t));
			else if (t instanceof Byte)
				array.add(new JsonValue( ((Byte)t).byteValue() ));
			else if (t instanceof Character)
				array.add(new JsonValue( ((Character)t).charValue() ));
			else if (t instanceof Short)
				array.add(new JsonValue( ((Short)t).shortValue() ));
			else if (t instanceof Integer)
				array.add(new JsonValue( ((Integer)t).intValue() ));
			else if (t instanceof Long)
				array.add(new JsonValue( ((Long)t).longValue() ));
			else if (t instanceof Float)
				array.add(new JsonValue( ((Float)t).floatValue() ));
			else if (t instanceof Double)
				array.add(new JsonValue( ((Double)t).doubleValue() ));
			else if (t instanceof Boolean)
				array.add(new JsonValue( ((Boolean)t).booleanValue() ));
			else throw new ClassCastException(t.getClass() + " �� JsonArray �̗v�f�Ɏw��ł��܂���");
		}
	}
/*
 * push
 */
	@Override
	public JsonArray push(JsonType val) {
		this.array.add(val);
		return this;
	}
	@Override
	public JsonArray push(String val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray push(byte val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray push(char val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray push(short val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray push(int val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray push(long val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray push(float val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray push(double val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray push(boolean val) {
		this.array.add(new JsonValue(val));
		return this;
	}
/*
 * pop
 */
	@Override
	public JsonType pop() {
		return this.array.remove(array.size()-1);
	}
	
/*
 * shift
 */
	@Override
	public JsonArray shift(JsonType val) {
		this.array.add(0,val);
		return this;
	}
	@Override
	public JsonArray shift(String val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray shift(byte val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray shift(char val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray shift(short val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray shift(int val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray shift(long val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray shift(float val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray shift(double val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	@Override
	public JsonArray shift(boolean val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
/*
 * unshift
 */
	@Override
	public JsonType unshift() {
		return this.array.remove(0);
	}
	
	/**
	 * JavaScript �ɂ����� slice ����ł��B
	 * 
	 * @param	s	�R�s�[����ŏ��̃C���f�b�N�X(�܂݂܂�)
	 * @param	e	�R�s�[���閖���̃C���f�b�N�X(�܂݂܂���)
	 * @return	�؂����� JsonArray (�v�f�͎Q�Ƃł�(shallow copy))
	 */
	@Override
	public JsonArray slice(int s, int e) {
		Object[] a = array.toArray(new Object[0]);
		return new JsonArray(Arrays.copyOfRange(a, s, e));
	}
	
	/**
	 * JavaScript �ɂ����� concat (�����A��j��I�Ō��̒l��ۂ�) �ł��B
	 * JsonArray �ȊO���w�肷��ƁAClassCastException ���X���[����܂��B
	 *
	 * @param	target	�������� JsonArray
	 * @return	������� JsonArray�B���� JsonArray (this) �͕ύX����܂���B
	 */
	@Override
	public JsonArray concat(JsonType target) {
		List<JsonType> result = new ArrayList<JsonType>(array);
		result.addAll(((JsonArray)target).array);
		JsonArray ja = new JsonArray();
		ja.array = result;
		return ja;
	}
	
/*
 * splice
 */
	/**
	 * splice
	 * ���̃��\�b�h�͔j��I(���̃C���X�^���X���ύX�����)�ł��B
	 *
	 * @param	index	�}������ʒu
	 * @param	delete	�}������ʒu�ō폜����v�f��
	 * @param	toAdd	�}������I�u�W�F�N�g�̔z��
	 * @return	�ύX��̃C���X�^���X
	 */
	@Override
	public JsonArray splice(int index, int delete, JsonType toAdd) {
		if (toAdd instanceof JsonArray) {
			JsonType[] ja = ((JsonArray)toAdd).array.toArray(new JsonType[0]);
			return spliceImpl(index, delete, ja);
		}
		return spliceImpl(index, delete, new JsonType[] { toAdd });
	}
	
	/**
	 * splice �̎���
	 * ���̃��\�b�h�͔j��I(���̃C���X�^���X���ύX�����)�ł��B
	 */
	private JsonArray spliceImpl(int index, int delete, JsonType[] toAdd) {
		if (index < 0 || index >= array.size())
			throw new ArrayIndexOutOfBoundsException("Out of bounds : " + index + " array size = " + array.size());
		if (delete < 0)
			throw new IllegalArgumentException("Delete count must not be negative : " + delete);
		
		// �폜���钷�������߂�
		int deleteLen = Math.min(array.size() - index, delete);
		
		// �폜
		for (int i = 0; i < deleteLen; i++) array.remove(index);
		
		// �}��
		for (int i = 0; i < toAdd.length; i++) array.add(index, toAdd[i]);
		
		return this;
	}
	
	@Override
	public JsonArray splice(int index, int delete, Object... toAdd) {
		if (toAdd instanceof JsonType[])
			return spliceImpl(index, delete, (JsonType[])toAdd);
		
		JsonType[] a = new JsonType[toAdd.length];
		
		for (int i = 0; i < a.length; i++) {
			Object t = toAdd[i];
			if (t instanceof JsonType) a[i] = (JsonType)t;
			else if (t instanceof String) a[i] = new JsonValue((String)t);
			else if (t instanceof Byte)
				a[i] = new JsonValue( ((Byte)t).byteValue() );
			else if (t instanceof Character)
				a[i] = new JsonValue( ((Character)t).charValue() );
			else if (t instanceof Short)
				a[i] = new JsonValue( ((Short)t).shortValue() );
			else if (t instanceof Integer)
				a[i] = new JsonValue( ((Integer)t).intValue() );
			else if (t instanceof Long)
				a[i] = new JsonValue( ((Long)t).longValue() );
			else if (t instanceof Float)
				a[i] = new JsonValue( ((Float)t).floatValue() );
			else if (t instanceof Double)
				a[i] = new JsonValue( ((Double)t).doubleValue() );
			else if (t instanceof Boolean)
				a[i] = new JsonValue( ((Boolean)t).booleanValue() );
			else throw new ClassCastException(t.getClass() + " �� JsonArray �̗v�f�Ɏw��ł��܂���");
		}
		return spliceImpl(index, delete, a);
	}
	
	@Override
	public JsonType get(int index) {
		return array.get(index);
	}
	
	@Override
	public JsonType cut(int index) {
		return array.remove(index);
	}
	
	@Override
	public int size() {
		return array.size();
	}
	
	@Override
	public java.util.Iterator<JsonType> iterator() {
		return array.iterator();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean first = true;
		for (JsonType obj : array) {
			if (!first) sb.append(",");
			else first = false;
			sb.append(obj.toString());
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	protected String toString(String indent, String indentStep,
								int textwidth, boolean objElement) {
		// textwidth �w�肪����ꍇ�A��s�������݂�
		// ��s���͏����R�X�g�������邽�߁Asize() ���傫�����炩�Ɏ��܂�Ȃ�
		// �Ƃ��̓X�L�b�v����
		if ( (textwidth > 0)&&(2 * array.size() + 3 <= textwidth) ) {
			int len = indent.length();
			// �R�X�g��������A���ʂɂȂ邩������Ȃ�����
			String tryShort = toString();
			if (len + tryShort.length() <= textwidth) return tryShort;
		}
		StringBuffer sb = new StringBuffer();
		
		if (!objElement) sb.append(indent);
		sb.append("[");
		boolean first = true;
		for (JsonType obj : array) {
			if (!first) {
				sb.append(",");
				sb.append(JsonType.LS);
			} else {
				sb.append(JsonType.LS);
				first = false;
			}
			sb.append(obj.toString(indent+indentStep, indentStep, textwidth, false));
		}
		sb.append(JsonType.LS);
		sb.append(indent);
		sb.append("]");
		
		return sb.toString();
	}
	
}

package abdom.data.json;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Json�`���ɂ�����z���\���܂��B
 * �����ł͔z��� ArrayList<JsonType> �ŕێ����܂��B
 */
public class JsonArray extends JsonType implements Iterable<JsonType> {
	public List<JsonType> array = new ArrayList<JsonType>();
	
/*-------------
 * constructor
 */
	public JsonArray() {
	}
	public JsonArray(JsonType... array) {	set(array); }
	public JsonArray(byte... array) {	set(array);	}
	public JsonArray(char... array) {	set(array);	}
	public JsonArray(short... array) {	set(array); }
	public JsonArray(int... array) {	set(array);	}
	public JsonArray(long... array) {	set(array);	}
	public JsonArray(float... array) {	set(array);	}
	public JsonArray(double... array) {	set(array);	}
	public JsonArray(boolean... array) {	set(array);	}
	public JsonArray(String... array) {	set(array);	}
	
/*------------------
 * instance methods
 */
	public void set(JsonType... array) {
		this.array.clear();
		for (JsonType t : array) this.array.add(t);
	}
	public void set(String... array) {
		this.array.clear();
		for (String t : array) this.array.add(new JsonValue(t));
	}
	public void set(byte... array) {
		this.array.clear();
		for (byte t : array) this.array.add(new JsonValue(t));
	}
	public void set(char... array) {
		this.array.clear();
		for (char t : array) this.array.add(new JsonValue(t));
	}
	public void set(short... array) {
		this.array.clear();
		for (short t : array) this.array.add(new JsonValue(t));
	}
	public void set(int... array) {
		this.array.clear();
		for (int t : array) this.array.add(new JsonValue(t));
	}
	public void set(long... array) {
		this.array.clear();
		for (long t : array) this.array.add(new JsonValue(t));
	}
	public void set(float... array) {
		this.array.clear();
		for (float t : array) this.array.add(new JsonValue(t));
	}
	public void set(double... array) {
		this.array.clear();
		for (double t : array) this.array.add(new JsonValue(t));
	}
	public void set(boolean... array) {
		this.array.clear();
		for (boolean t : array) this.array.add(new JsonValue(t));
	}
/*
 * push
 */
	public JsonArray push(JsonType val) {
		this.array.add(val);
		return this;
	}
	public JsonArray push(String val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	public JsonArray push(byte val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	public JsonArray push(char val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	public JsonArray push(short val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	public JsonArray push(int val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	public JsonArray push(long val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	public JsonArray push(float val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	public JsonArray push(double val) {
		this.array.add(new JsonValue(val));
		return this;
	}
	public JsonArray push(boolean val) {
		this.array.add(new JsonValue(val));
		return this;
	}
/*
 * pop
 */
	public JsonType pop() {
		return this.array.remove(array.size()-1);
	}
	
/*
 * shift
 */
	public JsonArray shift(JsonType val) {
		this.array.add(0,val);
		return this;
	}
	public JsonArray shift(String val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	public JsonArray shift(byte val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	public JsonArray shift(char val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	public JsonArray shift(short val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	public JsonArray shift(int val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	public JsonArray shift(long val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	public JsonArray shift(float val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	public JsonArray shift(double val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
	public JsonArray shift(boolean val) {
		this.array.add(0, new JsonValue(val));
		return this;
	}
/*
 * unshift
 */
	public JsonType unshift() {
		return this.array.remove(0);
	}
	
	/**
	 * JavaScript �ɂ����� slice ����ł��B
	 * 
	 * @param	s	�R�s�[����ŏ��̃C���f�b�N�X(�܂݂܂�)
	 * @param	e	�R�s�[���閖���̃C���f�b�N�X(�܂݂܂���)
	 * @return	�؂����� JsonArray (�v�f�͎Q��(shallow copy)�ł�)
	 */
	public JsonArray slice(int s, int e) {
		JsonType[] a = array.toArray(new JsonType[0]);
		return new JsonArray(Arrays.copyOfRange(a, s, e));
	}
	
	/**
	 * JavaScript �ɂ����� concat (�����A���̒l��ۂ�) �ł��B
	 */
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
	public JsonArray splice(int index, int delete, JsonType... toAdd) {
		if (toAdd.length == 1 && (toAdd[0] instanceof JsonArray)) {
			// �P�Ƃ� JsonArray ���w�肳�ꂽ�ꍇ
			toAdd = ((JsonArray)toAdd[0]).array.toArray(new JsonType[0]);
		}
		JsonType[] a = array.toArray(new JsonType[0]);
		// �ŏI�I�Ȓ������v�Z
		int deleteLen = Math.min(a.length - index, delete);
		int len = a.length - deleteLen + toAdd.length;
		JsonType[] result = new JsonType[len];
		System.arraycopy(a, 0, result, 0, index);
		System.arraycopy(toAdd, 0, result, index, toAdd.length);
		System.arraycopy(a, index+deleteLen, result, index+toAdd.length, a.length-index-deleteLen);
		
		return new JsonArray(result);
	}
	public JsonArray splice(int index, int delete, String... val) {
		JsonType[] a = new JsonArray(val).array.toArray(new JsonType[0]);
		return splice(index, delete, val);
	}
	public JsonArray splice(int index, int delete, byte... val) {
		JsonType[] a = new JsonArray(val).array.toArray(new JsonType[0]);
		return splice(index, delete, val);
	}
	public JsonArray splice(int index, int delete, char... val) {
		JsonType[] a = new JsonArray(val).array.toArray(new JsonType[0]);
		return splice(index, delete, val);
	}
	public JsonArray splice(int index, int delete, short... val) {
		JsonType[] a = new JsonArray(val).array.toArray(new JsonType[0]);
		return splice(index, delete, val);
	}
	public JsonArray splice(int index, int delete, int... val) {
		JsonType[] a = new JsonArray(val).array.toArray(new JsonType[0]);
		return splice(index, delete, val);
	}
	public JsonArray splice(int index, int delete, long... val) {
		JsonType[] a = new JsonArray(val).array.toArray(new JsonType[0]);
		return splice(index, delete, val);
	}
	public JsonArray splice(int index, int delete, float... val) {
		JsonType[] a = new JsonArray(val).array.toArray(new JsonType[0]);
		return splice(index, delete, val);
	}
	public JsonArray splice(int index, int delete, double... val) {
		JsonType[] a = new JsonArray(val).array.toArray(new JsonType[0]);
		return splice(index, delete, val);
	}
	public JsonArray splice(int index, int delete, boolean... val) {
		JsonType[] a = new JsonArray(val).array.toArray(new JsonType[0]);
		return splice(index, delete, val);
	}
	
/*-----------
 * overrides
 */
	@Override
	public JsonType get(int index) {
		return array.get(index);
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

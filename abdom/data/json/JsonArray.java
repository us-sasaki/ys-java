package abdom.data.json;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Json形式における配列を表します。
 * 内部では配列を ArrayList<JsonType> で保持します。
 * このクラスのオブジェクトはスレッドセーフではありません。
 */
public class JsonArray extends JsonType implements Iterable<JsonType> {
	protected List<JsonType> array = new ArrayList<JsonType>();
	
/*-------------
 * constructor
 */
	/**
	 * 空(要素数0)の JsonArray を作成します。
	 */
	public JsonArray() {
	}
	
	/**
	 * 指定された要素を持つ JsonArray を作成します。
	 *
	 * @param	array	配列要素の指定
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
			else throw new ClassCastException(t.getClass() + " は JsonArray の要素に指定できません");
		}
	}
/*
 * push
 */
	@Override
	public JsonArray push(JsonType val) {
		if (val == null) val = new JsonValue(null);
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
	 * JavaScript における slice 操作です。
	 * 
	 * @param	s	コピーする最初のインデックス(含みます)
	 * @param	e	コピーする末尾のインデックス(含みません)
	 * @return	切り取った JsonArray (要素は参照です(shallow copy))
	 */
	@Override
	public JsonArray slice(int s, int e) {
		Object[] a = array.toArray(new Object[0]);
		return new JsonArray(Arrays.copyOfRange(a, s, e));
	}
	
	/**
	 * JavaScript における concat (結合、非破壊的で元の値を保つ) です。
	 * JsonArray 以外を指定すると、ClassCastException がスローされます。
	 *
	 * @param	target	結合する JsonArray
	 * @return	結合後の JsonArray。元の JsonArray (this) は変更されません。
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
	 * このメソッドは破壊的(元のインスタンスが変更される)です。
	 *
	 * @param	index	挿入する位置
	 * @param	delete	挿入する位置で削除する要素数
	 * @param	toAdd	挿入するオブジェクトの配列
	 * @return	変更後のインスタンス
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
	 * splice の実装
	 * このメソッドは破壊的(元のインスタンスが変更される)です。
	 */
	private JsonArray spliceImpl(int index, int delete, JsonType[] toAdd) {
		if (index < 0 || index >= array.size())
			throw new ArrayIndexOutOfBoundsException("Out of bounds : " + index + " array size = " + array.size());
		if (delete < 0)
			throw new IllegalArgumentException("Delete count must not be negative : " + delete);
		
		// 削除する長さを求める
		int deleteLen = Math.min(array.size() - index, delete);
		
		// 削除
		for (int i = 0; i < deleteLen; i++) array.remove(index);
		
		// 挿入
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
			else throw new ClassCastException(t.getClass() + " は JsonArray の要素に指定できません");
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
		sb.append('[');
		boolean first = true;
		for (JsonType obj : array) {
			if (!first) sb.append(",");
			else first = false;
			sb.append(obj.toString());
		}
		sb.append(']');
		return sb.toString();
	}
	
	@Override
	protected String toString(String indent, String indentStep,
								int textwidth, boolean objElement) {
		// textwidth 指定がある場合、一行化を試みる
		// 一行化は処理コストがかかるため、size() が大きく明らかに収まらない
		// ときはスキップする
		if ( (textwidth > 0)&&(2 * array.size() + 3 <= textwidth) ) {
			int len = indent.length();
			// コストがかかり、無駄になるかもしれない処理
			String tryShort = toString();
			if (len + tryShort.length() <= textwidth) return tryShort;
		}
		StringBuffer sb = new StringBuffer();
		
		if (!objElement) sb.append(indent);
		sb.append('[');
		boolean first = true;
		for (JsonType obj : array) {
			if (!first) {
				sb.append(',');
				sb.append(JsonType.LS);
			} else {
				sb.append(JsonType.LS);
				first = false;
			}
			sb.append(obj.toString(indent+indentStep, indentStep, textwidth, false));
		}
		sb.append(JsonType.LS);
		sb.append(indent);
		sb.append(']');
		
		return sb.toString();
	}
	
}

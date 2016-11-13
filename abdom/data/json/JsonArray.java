package abdom.data.json;

import java.util.List;
import java.util.ArrayList;

/**
 * Json形式における配列を表します。
 * 内部では配列を ArrayList<JsonType> で保持します。
 */
public class JsonArray extends JsonType {
	public List<JsonType> array = new ArrayList<JsonType>();
	
/*-------------
 * constructor
 */
	public JsonArray() {
	}
	public JsonArray(JsonType[] array) {	set(array); }
	public JsonArray(byte[] array) {	set(array);	}
	public JsonArray(char[] array) {	set(array);	}
	public JsonArray(short[] array) {	set(array); }
	public JsonArray(int[] array) {	set(array);	}
	public JsonArray(long[] array) {	set(array);	}
	public JsonArray(float[] array) {	set(array);	}
	public JsonArray(double[] array) {	set(array);	}
	public JsonArray(boolean[] array) {	set(array);	}
	public JsonArray(String[] array) {	set(array);	}
	
/*------------------
 * instance methods
 */
	public void set(JsonType[] array) {
		this.array.clear();
		for (JsonType t : array) this.array.add(t);
	}
	public void set(String[] array) {
		this.array.clear();
		for (String t : array) this.array.add(new JsonValue(t));
	}
	public void set(byte[] array) {
		this.array.clear();
		for (byte t : array) this.array.add(new JsonValue(t));
	}
	public void set(char[] array) {
		this.array.clear();
		for (char t : array) this.array.add(new JsonValue(t));
	}
	public void set(short[] array) {
		this.array.clear();
		for (short t : array) this.array.add(new JsonValue(t));
	}
	public void set(int[] array) {
		this.array.clear();
		for (int t : array) this.array.add(new JsonValue(t));
	}
	public void set(long[] array) {
		this.array.clear();
		for (long t : array) this.array.add(new JsonValue(t));
	}
	public void set(float[] array) {
		this.array.clear();
		for (float t : array) this.array.add(new JsonValue(t));
	}
	public void set(double[] array) {
		this.array.clear();
		for (double t : array) this.array.add(new JsonValue(t));
	}
	public void set(boolean[] array) {
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
	public JsonType pop(JsonType val) {
		return this.array.remove(array.size()-1);
	}
	public JsonType pop(String val) {
		return this.array.remove(array.size()-1);
	}
	public JsonType pop(byte val) {
		return this.array.remove(array.size()-1);
	}
	public JsonType pop(char val) {
		return this.array.remove(array.size()-1);
	}
	public JsonType pop(short val) {
		return this.array.remove(array.size()-1);
	}
	public JsonType pop(int val) {
		return this.array.remove(array.size()-1);
	}
	public JsonType pop(long val) {
		return this.array.remove(array.size()-1);
	}
	public JsonType pop(float val) {
		return this.array.remove(array.size()-1);
	}
	public JsonType pop(double val) {
		return this.array.remove(array.size()-1);
	}
	public JsonType pop(boolean val) {
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
	public JsonType unshift(JsonType val) {
		return this.array.remove(0);
	}
	public JsonType unshift(String val) {
		return this.array.remove(0);
	}
	public JsonType unshift(byte val) {
		return this.array.remove(0);
	}
	public JsonType unshift(char val) {
		return this.array.remove(0);
	}
	public JsonType unshift(short val) {
		return this.array.remove(0);
	}
	public JsonType unshift(int val) {
		return this.array.remove(0);
	}
	public JsonType unshift(long val) {
		return this.array.remove(0);
	}
	public JsonType unshift(float val) {
		return this.array.remove(0);
	}
	public JsonType unshift(double val) {
		return this.array.remove(0);
	}
	public JsonType unshift(boolean val) {
		return this.array.remove(0);
	}
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean first = true;
		for (JsonType obj : array) {
			if (!first) {
				sb.append(",");
			} else {
				first = false;
			}
			sb.append(obj.toString());
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	protected String toString(String indent, String indentStep,
								int textwidth, boolean objElement) {
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

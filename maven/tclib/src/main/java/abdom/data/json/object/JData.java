package abdom.data.json.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;

/**
 * JSON オブジェクトを Java オブジェクトによって模倣します。
 * このクラスを継承することで、Java オブジェクトと JSON 形式の相互変換が
 * 容易になります。つまり、Java オブジェクトのインスタンス変数が、
 * JSON 形式として直列化でき、また逆に JSON 形式から Java オブジェクトの
 * フィールドを設定できるようになります。
 * Java オブジェクトにおいて次に定義する「プロパティ」が変換対象となります。<br>
 * 1.public メンバ変数。プロパティ名は変数名になります。<br>
 * 2.public getter, setter メソッドの対。プロパティ名は Java Beans 命名規則<br>
 *   によります。さらに対は getter は引数なし、setter は引数ありで getter <br>
 * 　の返値型と setter の引数型が一致し、JData カテゴリに含まれるもの<br>
 * <br>
 * JData カテゴリは、以下の型です。<pre>
 *
 * boolean, int, long, float, double, String, JValue(,JData), JsonObject
 * および、これらの型の配列
 *
 * </pre>暗黙のフィールドとして、_extra (JsonObject型) を持っており
 * fill() の際に未定義のフィールド値はすべてここに格納されます。
 * また、toJson() では _extra フィールドは存在する(not null)場合のみJSON
 * メンバとして現れます。
 * 子クラスで、JSON形式との相互変換対象外とする変数を定義したい場合、
 * transient 修飾子をつけて下さい。
 *
 * @version	December 23, 2016
 * @author	Yusuke Sasaki
 */
public abstract class JData extends JValue {

	/** fill できなかった値を格納する予約領域 */
	protected transient JsonObject _extra;
	
/*-------------
 * constructor
 */
	protected JData() {
		// JData のプロパティ宣言が規約に基づいていない場合、
		// JDataDefinitionException が発生するが、なるべく早期
		// (生成時)にチェックしデバッグを容易にするため、デフォルト
		// コンストラクタ内で getAccessors() を呼んでおく
		Jsonizer.getAccessors(this);
	}
	
/*------------------
 * instance methods
 */
	/**
	 * extra を持つかどうかテストします。
	 *
	 * @return	extra を持つ場合、true
	 */
	public boolean hasExtras() {
		return (_extra != null);
	}
	
	/**
	 * extra の keySet を返却します。ない場合、null となります。
	 *
	 * @return	extra のキー(extra が存在しない場合、null)
	 */
	public Set<String> getExtraKeySet() {
		if (_extra == null) return null;
		return _extra.keySet();
	}
	
	/**
	 * extra アクセスメソッドで、JsonType 値を取得します。
	 * extra がない場合、あっても指定されたキーを持たない場合、
	 * null が返却されます。
	 *
	 * @param	key	extra の key 情報
	 * @return	key に対応する値(null の場合があります)
	 */
	public JsonType getExtra(String key) {
		if (_extra == null) return null;
		return _extra.get(key);
	}
	
	/**
	 * extra アクセスメソッドで、Jsonizable 値を設定します。
	 *
	 * @param	key	extra の key 情報
	 * @param	jt	設定する値を指定。toJson() による JsonType が設定されます。
	 */
	public void putExtra(String key, Jsonizable jt) {
		if (Jsonizer.hasProperty(this, key))
			throw new IllegalFieldTypeException("The key " + key + " is property of " + this.getClass() + ", so it can't be assigned as extra.");
		if (_extra == null) _extra = new JsonObject();
		_extra.put(key, jt.toJson());
	}
	
	/**
	 * このインスタンスが持つ extra オブジェクト(JsonObject)
	 * の参照を返却します。内容の参照/変更を簡便に行うことを想定した
	 * メソッドです。
	 *
	 * @return	extra オブジェクト(JsonObject)。null の場合があります。
	 */
	public JsonObject getExtras() {
		return _extra;
	}
	
	/**
	 * このオブジェクトの指定されたフィールドまたは extra の値を JsonType
	 * として返却します。
	 * このオブジェクトを toJson().get(String) した場合と同様の挙動ですが、
	 * 単一フィールドに対する実装のため、高速です。
	 * フィールド値には dot(.) オペレーションが利用可能です。
	 *
	 * @param	name	フィールド名(extra の場合を含む)
	 * @return	取得された JsonType
	 */
	public JsonType get(String name) {
		int index = name.indexOf('.');
		if (index == -1) {
			JsonType result = Jsonizer.get(this, name);
			if (result != null) return result;
			if (_extra == null) return null;
			return _extra.get(name);
		} else {
			JsonType jt = Jsonizer.get(this, name.substring(0, index));
			JsonType result = ((JsonObject)jt).get(name.substring(index+1));
			if (result != null) return result;
			if (_extra == null) return null;
			return _extra.get(name);
		}
	}
	
	/**
	 * このオブジェクトの指定されたフィールドまたは extra の値を、
	 * 指定された Jsonizable の値に設定します。
	 * 単一フィールドに対する fill() のような操作です。
	 *
	 * @param	name	フィールド名(extra の場合を含む)
	 * @param	arg		設定値
	 */
	public void set(String name, Jsonizable arg) {
		JsonType result = Jsonizer.set(this, name, arg);
		if (result != null) {
			_extra.put(name, result);
		}
	}
	
	/**
	 * 指定された JsonObject の内容をこのオブジェクトに設定します。
	 * 引数の型は、利便性のため JsonType としていますが、JsonObject
	 * 以外を指定すると、ClassCastException がスローされます。
	 * このメソッドは値を追加し、既存値は上書きされなければ保存される
	 * ことに注意してください。_extra も同様です。
	 *
	 * @param	json	このオブジェクトに値を与える JsonType
	 */
	@Override
	public void fill(Jsonizable json) {
		JsonType rest = Jsonizer.fill(this, json);
		if (rest == null) return;
		if (_extra == null) _extra = (JsonObject)rest;
		else {
			for (String key : rest.keySet()) {
				JsonType val = rest.get(key);
				if (val.getType() == JsonType.TYPE_VOID) {
					_extra.cut(key);
				} else {
					_extra.put(key, val);
				}
			}
		}
	}
	
	/**
	 * このオブジェクトを JsonObject に変換します。
	 *
	 * @return	JsonObject
	 */
	@Override
	public JsonType toJson() {
		JsonType json = Jsonizer.toJson(this);
		// _extra を追加
		if (_extra == null) return json;
		for (String key : _extra.keySet()) {
			json.put(key, _extra.get(key));
		}
		return json;
	}
	
}

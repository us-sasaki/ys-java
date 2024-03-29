package abdom.data.json.object;

import java.util.Set;

import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;

/**
 * JsonType との相互変換機能を Java オブジェクトに付加する抽象クラスです。
 * このクラスを継承することで、JsonType 値を元にインスタンス変数を設定したり、
 * 逆に Java オブジェクトのインスタンス変数を JsonType や JSON に変換可能に
 * なります。
 * また、Java オブジェクトとして定義されていない未知のフィールドも取り扱う
 * ことができます。
 * この目的で、暗黙のフィールドとして _extra (JsonObject型) を持っており
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

	/** Jsonizer.fill できなかった値を格納する予約領域 */
	protected JsonObject _extra;
	
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
	 * key 値には、オブジェクトの階層をたどるための. (dot) 記法が
	 * 利用できます。
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
	 * key 値には、オブジェクトの階層をたどるための. (dot) 記法が
	 * 利用できます。
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
	
	public void putExtra(String key, boolean arg) {
		putExtra(key, arg?JsonType.TRUE:JsonType.FALSE);
	}
	
	public void putExtra(String key, byte arg) {
		putExtra(key, new JsonValue(arg));
	}
	
	public void putExtra(String key, short arg) {
		putExtra(key, new JsonValue(arg));
	}
	
	public void putExtra(String key, char arg) {
		putExtra(key, new JsonValue(arg));
	}
	
	public void putExtra(String key, int arg) {
		putExtra(key, new JsonValue(arg));
	}
	
	public void putExtra(String key, long arg) {
		putExtra(key, new JsonValue(arg));
	}
	
	public void putExtra(String key, float arg) {
		putExtra(key, new JsonValue(arg));
	}
	
	public void putExtra(String key, double arg) {
		putExtra(key, new JsonValue(arg));
	}
	
	public void putExtra(String key, String arg) {
		putExtra(key, new JsonValue(arg));
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
		return Jsonizer.get(this, name);
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
		Jsonizer.set(this, name, arg);
	}
	
	public void set(String key, boolean arg) {
		set(key, arg?JsonType.TRUE:JsonType.FALSE);
	}
	
	public void set(String key, byte arg) {
		set(key, new JsonValue(arg));
	}
	
	public void set(String key, short arg) {
		set(key, new JsonValue(arg));
	}
	
	public void set(String key, char arg) {
		set(key, new JsonValue(arg));
	}
	
	public void set(String key, int arg) {
		set(key, new JsonValue(arg));
	}
	
	public void set(String key, long arg) {
		set(key, new JsonValue(arg));
	}
	
	public void set(String key, float arg) {
		set(key, new JsonValue(arg));
	}
	
	public void set(String key, double arg) {
		set(key, new JsonValue(arg));
	}
	
	public void set(String key, String arg) {
		set(key, new JsonValue(arg));
	}
	
	/**
	 * 指定された JsonObject の内容をこのオブジェクトに設定します。
	 * 引数の型は、利便性のため Jsonizable としていますが、object
	 * 以外を指定すると、ClassCastException がスローされます。
	 * このメソッドは値を追加し、既存値は上書きされなければ保存される
	 * ことに注意してください。_extra も同様です。
	 *
	 * @param	json	このオブジェクトに値を与える JsonType
	 */
	@Override
	public void fill(Jsonizable json) {
		// fill は特例的に Jsonizer 内で extra 処理を行う
		Jsonizer.fill(this, json);
	}
	
	/**
	 * このオブジェクトを JsonType に変換します。
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

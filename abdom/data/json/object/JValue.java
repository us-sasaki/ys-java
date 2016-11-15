package abdom.data.json.object;

import abdom.data.json.JsonValue;

/**
 * Json バリューを Java オブジェクトによって模倣します。
 * このクラスを継承することで、JData のメンバとして定義することができます。
 * JData との違いの一つは、キーバリュー型でなく、バリューのみからなる点です。
 * JsonValue との相互変換メソッドとして、fill(JsonValue), toJson()
 * 実装する必要があります。
 * JData のようにメンバ変数は直列化されず、前記２メソッドを通じて直列化
 * が行われます。
 *
 * @version	November 15, 2016
 * @author	Yusuke Sasaki
 */
public abstract class JValue {
	
	/**
	 * JsonType に変換します
	 *
	 * @return	変換された JsonType (JsonValue型)
	 */
	public abstract JsonValue toJson();
	
	/**
	 * JsonValue によってインスタンス状態を埋めます。
	 *
	 * @param	value として null 値や、JSON における null が指定される場合が
	 *			あり、これを念頭に実装して下さい。
	 */
	public abstract void fill(JsonValue value);
}

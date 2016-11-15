package abdom.data.json.object;

import abdom.data.json.JsonType;

/**
 * JSON と Java の間の相互変換に関するクラスのテンプレートです。
 * 通常、JsonData を継承してください。
 *
 * このクラスを直接継承するのは、以下のような場合です。<br/>
 * JsonValue を Java オブジェクトによって模倣する場合<br/>
 * 特定の JsonObject/JsonArray 構造がまとまった意味を持ち、1つのJavaオブジェ
 * クトとして表したい場合<br/>
 * 直接継承する場合、JData のようにメンバ変数の直列化機能は持たないため、
 * JsonType との相互変換メソッドとして、fill(JsonValue), toJson() を実装する
 * 必要があります。
 *
 * @version	November 15, 2016
 * @author	Yusuke Sasaki
 */
public abstract class JValue {
	
	/**
	 * JsonType に変換します
	 *
	 * @return	変換された JsonType
	 */
	public abstract JsonType toJson();
	
	/**
	 * JsonType によってインスタンス状態を埋めます。
	 *
	 * @param	value として null 値や、JSON における null が指定される場合が
	 *			あり、これを念頭に実装して下さい。
	 */
	public abstract void fill(JsonType value);
}

package abdom.data.json.object;

import abdom.data.json.JsonType;

/**
 * JSON と Java の間の相互変換に関するクラスのテンプレートです。
 * 通常、JData を継承してください。
 *
 * このクラスを直接継承するのは、以下のような場合です。<br/>
 * JsonValue を Java オブジェクトによって模倣する場合<br/>
 * 特定の JsonObject/JsonArray 構造がまとまった意味を持ち、1つのJavaオブジェ
 * クトとして表したい場合<br/>
 * 直接継承する場合は JData のようなメンバ変数の直列化機能は持たないため、
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
	 *			 (通常、NullPointerExceptionが発生しない
	 *			ように null の場合は何もしない実装になります)
	 */
	public abstract void fill(JsonType value);
	
	/**
	 * JSON形式の文字列でフィールドを埋めます。内部的には、文字列から
	 * JsonType を構成し、fill(JsonType) を呼んでいます。
	 *
	 * @param	jsonString	値を保持する JSON 文字列
	 * @return	value 値の中で、このオブジェクトにプロパティがなく、設定
	 *			しなかった項目(extra)。ただし、この JValue が object 型でない
	 *			場合、null。
	 */
	public void fill(String jsonString) {
		fill(JsonType.parse(jsonString));
	}
	
	/**
	 * このオブジェクトを人が見やすいインデントを含む JSON 形式に変換します。
	 * 最大横幅はデフォルト値(80)が設定されます。
	 *
	 * @param	indent	インデント文字列(複数のスペースやタブ)
	 * @return	インデント、改行を含む JSON 形式
	 */
	public String toString(String indent) {
		return toJson().toString(indent);
	}
	
	/**
	 * 人が見やすいインデントを含んだ JSON 形式で文字列化します。
	 * object, array 値を一行で表せるなら改行させないための、一行の
	 * 文字数を指定します。
	 *
	 * @param	indent		インデント(複数のスペースやタブ)
	 * @param	textwidth	object, array に関し、この文字数に収まる場合
	 *						複数行に分けない処理を行うための閾値。
	 *						0 以下を指定すると、一行化を試みず、常に複数行化
	 *						されます。(この方が高速)
	 * @return	インデント、改行を含む JSON 文字列
	 */
	public String toString(String indent, int textwidth) {
		return toJson().toString(indent, textwidth);
	}
	
	/**
	 * 文字列表現を返却します。文字列表現は、改行やスペース
	 * 文字を含まない JSON 形式です。
	 * string 型 (JsonValue で保持する値が String の場合) では
	 * 結果は ""(ダブルクオーテーション) で括られることに注意してください。
	 *
	 * @return	このオブジェクトの JSON 形式(文字列)
	 */
	@Override
	public String toString() {
		return toJson().toString();
	}
	
}

package abdom.data.json;

/**
 * JsonType化、JSON化可能であることを表すインターフェースです。
 * メソッドへの引数として、JSON値として扱えるオブジェクト(JsonType, JData)を
 * 包括的に指定するためのインターフェースです。
 *
 * @version		February 3, 2017
 * @author		Yusuke Sasaki
 */
public interface Jsonizable {
	/**
	 * JsonType に変換します。
	 *
	 * @return	このオブジェクトの JsonType 化。
	 */
	JsonType toJson();
	
	/**
	 * スペースやインデントを含まない JSON 表現を得ます。
	 *
	 * @return	このオブジェクトの JSON 表現
	 */
	String toString();
	
	/**
	 * 人が見やすいようにインデントを含んだ JSON 表現(pretty dump)を得ます。
	 * 最大横文字数(80)未満であれば、要素の配列やオブジェクトを
	 * 一行で表現します。
	 * ただし、要素そのものが最大横文字数を超えている場合、JSONでは改行
	 * できないため、はみ出します。
	 *
	 * @param	indent	インデント(スペースやタブ)
	 * @return	インデントを含む JSON 文字列
	 */
	String toString(String indent);
	
	/**
	 * 人が見やすいようにインデントを含んだ JSON 表現(pretty dump)です。
	 * 指定された最大横文字数未満であれば、要素の配列やオブジェクトを
	 * 一行で表現します。
	 * ただし、要素そのものが最大横文字数を超えている場合、JSONでは改行
	 * できないため、はみ出します。
	 *
	 * @param	indent	インデント(スペースやタブ)
	 * @param	textwidth	一行で示す最大横文字数。0以下を指定すると、
	 *						一行化しません(この方が高速です)。
	 * @return	インデントを含む JSON 文字列
	 */
	String toString(String indent, int textwidth);
}

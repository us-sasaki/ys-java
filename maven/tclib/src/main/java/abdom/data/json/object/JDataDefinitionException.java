package abdom.data.json.object;

/**
 * JData において、fill メソッドなどで自動的に JData オブジェクトを生成
 * する際、データでなく継承したクラス定義側に問題がある場合にスローされる
 * 例外です。
 * <pre>
 * 例１．デフォルトコンストラクタが定義されていない等でインスタンス化
 * に失敗したとき
 * 例２．フィールドが、アクセス不可能であるかfinalである場合
 * </pre>
 *
 * @version		December 10, 2016
 * @author		Yusuke Sasaki
 */
public class JDataDefinitionException extends RuntimeException {
	/**
	 *
	 */
	private static final long serialVersionUID = -4231036125978343240L;

	public JDataDefinitionException() {
		super();
	}
	public JDataDefinitionException(String msg) {
		super(msg);
	}
	public JDataDefinitionException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public JDataDefinitionException(Throwable cause) {
		super(cause);
	}
}

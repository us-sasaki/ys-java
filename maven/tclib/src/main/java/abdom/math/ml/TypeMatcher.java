package abdom.math.ml;

/**
 * 特定の型であるか判定するオブジェクトです。
 */
interface TypeMatcher {
	String getName();
	boolean matches(String target);
}

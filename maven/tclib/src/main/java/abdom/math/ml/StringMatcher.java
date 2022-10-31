package abdom.math.ml;

/**
 * String 型であるか判定するオブジェクトです。
 */
class StringMatcher implements TypeMatcher {
	public String getName() {
		return "String";
	}
	
	public boolean matches(String target) {
		return true;
	}
}

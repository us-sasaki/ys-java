package abdom.math.ml;

/**
 * long 型であるか判定するオブジェクトです。
 */
class LongMatcher extends DoubleMatcher implements TypeMatcher {
	public String getName() {
		return "long";
	}
	
	public boolean matches(String target) {
		if ("".equals(target)) return true;
		try {
			Long.parseLong(target);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}
}

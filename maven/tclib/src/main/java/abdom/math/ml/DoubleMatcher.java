package abdom.math.ml;

/**
 * double 型であるか判定するオブジェクトです。
 */
class DoubleMatcher implements TypeMatcher {
	public String getName() {
		return "double";
	}
	
	public boolean matches(String target) {
		if ("".equals(target)) return true;
		try {
			Double.parseDouble(target);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}
	
	public double toDouble(String target) {
		return Double.parseDouble(target);
	}
}

package abdom.math.stats;

public class DimensionMismatchException extends SMException {
	public DimensionMismatchException() {
	}
	
	public DimensionMismatchException(String msg) {
		super(msg);
	}
	
	public DimensionMismatchException(Throwable cause) {
		super(cause);
	}
	
	public DimensionMismatchException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

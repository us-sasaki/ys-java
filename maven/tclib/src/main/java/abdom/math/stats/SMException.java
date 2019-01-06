package abdom.math.stats;

public class SMException extends RuntimeException {
	public SMException() {
	}
	
	public SMException(String msg) {
		super(msg);
	}
	
	public SMException(Throwable cause) {
		super(cause);
	}
	
	public SMException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

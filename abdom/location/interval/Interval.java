package abdom.location.interval;

public class Interval implements Comparable<Interval> {
	/** ‹æŠÔ‚Ì‚Í‚¶‚ß */
	public int sind;
	/** ‹æŠÔ‚ÌI‚è */
	public int eind;
	/** ‹æŠÔ‚ÌŠÔ */
	public long time;
	/**
	 * ‹æŠÔ‚Ìƒ‰ƒxƒ‹B
	 * stop : ’â~‹æŠÔ
	 * move : ˆÚ“®‹æŠÔ
	 */
	public String label;
	
/*-------------
 * constructor
 */
	public Interval() {
	}
	
	public Interval(int s, int e, long t, String l) {
		sind = s;
		eind = e;
		time = t;
		label = l;
	}
	
/*-----------------------
 * implements Comparable
 */
	/**
	 * time ‚ª’·‚¢‚à‚Ì‚ªæ“ª‚É‚­‚é‡˜
	 */
	@Override
	public int compareTo(Interval a) {
		return (time == a.time)?0:((time - a.time > 0)?-1:1); //time~‡
	}
/*------------------
 * instance methods
 */
	public boolean intersects(Interval a) {
		return ( (sind <= a.eind)&&(sind >= a.sind) )|| // a ‚ª sind ‚ğŠÜ‚Ş
				( (eind <= a.eind)&&(eind >= a.sind) ); // a ‚ª eind ‚ğŠÜ‚Ş
	}
	
	@Override
	public String toString() {
		return "["+sind+","+eind+"] t="+time+" ";
	}
}

package abdom.location.interval;

public class Interval implements Comparable<Interval> {
	/** 区間のはじめ */
	public int sind;
	/** 区間の終り */
	public int eind;
	/** 区間の時間 */
	public long time;
	/**
	 * 区間のラベル。
	 * stop : 停止区間
	 * move : 移動区間
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
	 * time が長いものが先頭にくる順序
	 */
	@Override
	public int compareTo(Interval a) {
		return (time == a.time)?0:((time - a.time > 0)?-1:1); //time降順
	}
/*------------------
 * instance methods
 */
	public boolean intersects(Interval a) {
		return ( (sind <= a.eind)&&(sind >= a.sind) )|| // a が sind を含む
				( (eind <= a.eind)&&(eind >= a.sind) ); // a が eind を含む
	}
	
	@Override
	public String toString() {
		return "["+sind+","+eind+"] t="+time+" ";
	}
}

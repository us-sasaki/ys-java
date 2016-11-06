package abdom.location.interval;

public class Interval implements Comparable<Interval> {
	/** ��Ԃ̂͂��� */
	public int sind;
	/** ��Ԃ̏I�� */
	public int eind;
	/** ��Ԃ̎��� */
	public long time;
	/**
	 * ��Ԃ̃��x���B
	 * stop : ��~���
	 * move : �ړ����
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
	 * time ���������̂��擪�ɂ��鏇��
	 */
	@Override
	public int compareTo(Interval a) {
		return (time == a.time)?0:((time - a.time > 0)?-1:1); //time�~��
	}
/*------------------
 * instance methods
 */
	public boolean intersects(Interval a) {
		return ( (sind <= a.eind)&&(sind >= a.sind) )|| // a �� sind ���܂�
				( (eind <= a.eind)&&(eind >= a.sind) ); // a �� eind ���܂�
	}
	
	@Override
	public String toString() {
		return "["+sind+","+eind+"] t="+time+" ";
	}
}

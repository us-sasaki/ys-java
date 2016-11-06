package abdom.location.interval;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.Coord;

/**
 * ��ԁAList<Plot> ��^���Alabel �� move �ƂȂ��Ă����Ԃ�
 * ���x�ɂ���ĕ�������B�����̎d���́A���U���ŏ��ƂȂ�悤�ɂ���B
 * (����؂ɂ���A�ł̕����@�B�����炭���a�̑���ɃG���g��
 *  �s�[���g�����@������)
 *
 */
public class IntervalDivider {
	
	protected List<Plot> plots;
	
	
/*-------------
 * Constructor
 */
	public IntervalDivider(List<Plot> plots) {
		this.plots = plots;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �s���S�̂𑬓x���z�ɂ���ĕ�������
	 */
	public List<Interval> divideByVelocity(List<Interval> total) {
		// move �𒊏o����
		List<Interval> moves = new ArrayList<Interval>();
		List<Interval> rests = new ArrayList<Interval>();
		for (Interval i : total) {
			if (i.label.equals("move")) moves.add(i);
			else rests.add(i);
		}
		
		List<Interval> divided = moves;
		for (int i = 0; i < 2; i++) { // 100 �͂��߂��H -> 2 �ɂ��Ă���
			divided = divideMoves(divided);
		}
		
		divided.addAll(rests);
		
		divided.sort( (a,b) -> (a.sind - b.sind) ); // sind �̏���
		return divided;
	}
	
	/**
	 *
	 */
	private List<Interval> divideMoves(List<Interval> moves) {
		int divi = searchDivide(moves);
		List<Interval> result = new ArrayList<Interval>();
		for (Interval i : moves) {
			if ( (i.sind < divi)&&(divi <= i.eind) ) {
				Interval i1 = new Interval(i.sind, divi-1, plots.get(divi-1).time - plots.get(i.sind).time, "move");
				Interval i2 = new Interval(divi, i.eind, plots.get(i.eind).time - plots.get(divi).time, "move");
				result.add(i1);
				result.add(i2);
			} else {
				result.add(i);
			}
		}
		return result;
	}
	
	/**
	 * �ł����U���������Ȃ� index ����������
	 */
	private int searchDivide(List<Interval> target) {
		double minv = Double.MAX_VALUE;
		int mini = -1;
		for (Interval i : target) {
			for (int j = i.sind; j < i.eind+1; j++) {
				double v = calcVariance(target, j);
				if (minv > v) {
					minv = v;
					mini = j;
				}
			}
		}
//System.out.println("�ŏ��̕��U," + minv);
		return mini;
	}
	
	/**
	 * index �ŕ��������ꍇ�� variance ��ԋp����
	 * �����́A �`(index-1) , (index)�` �̈ʒu�ŕ�����B
	 * Interval �͘A�����Ă��Ȃ��Ă悭�A�����łȂ��Ă��悢�B
	 * disjoint(never intersects) �ł��邱�Ƃ͕K�v�B
	 *
	 * @param	target	�����Ώۂ� Interval �� List
	 * @param	index	�����ӏ�
	 * @return	���������ꍇ��(���x�l��)���U
	 */
	private double calcVariance(List<Interval> target, int index) {
		double squErr = 0d;
		int n = 0;
		for (Interval i : target) {
			if ( (i.sind < index)&&(index <= i.eind) ) {
				Interval i1 = new Interval(i.sind, index-1, plots.get(index-1).time - plots.get(i.sind).time, "move");
				Interval i2 = new Interval(index, i.eind, plots.get(i.eind).time - plots.get(index).time, "move");
				squErr += getSquErr(i1) + getSquErr(i2);
			} else {
				squErr += getSquErr(i);
			}
			n += i.eind - i.sind + 1;
		}
		return squErr / n; // Plot �̗v�f���Ŋ���
	}
	
	/**
	 * Interval �� List �ɑ΂��A���a���v�Z����
	 */
	private double getSquErr(List<Interval> target) {
		double squErr = 0d;
		for (Interval i : target) {
			squErr += getSquErr(i);
		}
		return squErr;
	}
	
	/**
	 * Interval �ɑ΂��A���a���v�Z����
	 */
	private double getSquErr(Interval interval) {
		double sum = 0d;
		for (int i = interval.sind; i <= interval.eind; i++) {
			sum += plots.get(i).velocity;
		}
		double mean = sum / (interval.eind - interval.sind + 1);
		
		double squErr = 0d;
		for (int i = interval.sind; i <= interval.eind; i++) {
			double v = (plots.get(i).velocity - mean);
			squErr += v * v;
		}
		return squErr;
	}
}

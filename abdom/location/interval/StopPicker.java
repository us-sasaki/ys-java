package abdom.location.interval;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.Coord;

/**
 * ��~���Ă��鎞�Ԃ𒊏o
 *
 * ��~�̒�`�͈ȉ��̒ʂ�F
 * �@����(s�`t)�ɂ����āA
 * �@�@�C�ӂ̂Q�_�̋����� R(m) �ȉ�
 * �@�@(���̂Ƃ��A���a 2*R/sqrt(3) �̉~���ɂ���B�t�͕s����)
 * �@�At-s �� T msec �ȏ�
 */
public class StopPicker {
	private static final double R = 50.0 * Math.sqrt(3) / 2; // ���a 50m �̉~��
	private static final long	T = 3 * 60 * 1000; // 3 min.
	
	protected List<Plot> plots;
	
/*-------------
 * Constructor
 */
	public StopPicker(List<Plot> plots) {
		this.plots = plots;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �s���S�̂� stop ���܂ދ�Ԃ��܂ނ悤�ɕ�������
	 */
	public List<Interval> divideByStop() {
		List<Interval> ss = pickStopList();
		List<Interval> total = new ArrayList<Interval>();
		int s = 0;
		for (Interval interval : ss) {
			if (interval.sind > s) { // interval �Ԃ� gap ����
				total.add(new Interval(s, interval.sind-1, plots.get(interval.sind-1).time - plots.get(s).time, "move"));
			}
			total.add(interval);
			s = interval.eind+1;
		}
		if (s < plots.size() - 1) { // �Ō�� gap ����
			total.add(new Interval(s, plots.size()-1, plots.get(plots.size()-1).time - plots.get(s).time, "move"));
		}
		return total;
	}
	/**
	 * �������ׂĒ��o���A�悢����(���Ԃ̒�������)��I������
	 */
	public List<Interval> pickStopList() {
		// �����i�[���郊�X�g
		List<Interval> r = new ArrayList<Interval>();
		
		for (int i = 0; i < plots.size(); i++) {
			Interval found = searchStandstill(i);
			if (found == null) break; // i �ȍ~�͊Y�����Ȃ����� break
			r.add(found);
			i = found.sind;
		}
		
		// ���בւ����s��(���R���� : time �~��)
		r.sort(null);
		
		for (int i = 0; i < r.size(); i++) {
			// intersect ����v�f���폜
			for (int j = i+1; j < r.size(); j++) {
				if (r.get(i).intersects(r.get(j))) {
					r.remove(j);
					j--;
				}
			}
		}
		
		r.sort( (a,b) -> (a.sind - b.sind) ); // start index �̏���
		
		return r;
	}
	
	/**
	 * �w�肳�ꂽ index �ȍ~�ɏ����𖞂�����Ԃ����邩�ǂ�������������
	 * ����ꍇ�Aindex ���ł��Ⴂ���̂� index �� sind, tind �Ɋi�[����B
	 * �ł����Ԃ̒����Ȃ��Ԃ������������ǂ��ƍl�����邪�A�Ƃ肠����
	 * �ł��Ⴂ���̂𒊏o����B
	 * �������ׂĒ��o���A�悢���̂�I�����鏈�������邱�ƂŎ����ł���B
	 *
	 * @return		��������(Interval) / ������Ȃ�����(null)
	 */
	private Interval searchStandstill(int startIndex) {
		for (int i = startIndex; i < plots.size() - 1; i++) {
			// ei - 1 �܂ł� i �Ƃ̋����� R �ȓ��Ɏ��܂��Ă���A���Ԃ� T �ȓ�
			int ei = searchFirstExceedIndex(i);
			if (ei == -1) continue; // �Y�����Ȃ�����
			
			// �Y�����������ꍇ�A�C�ӂ̂Q�_�ɂ��ċ����� R �ȓ��ƂȂ���̂�
			// ���邩���m�F
			for (int j = ei; j > i; j--) {
				long t = plots.get(j).time - plots.get(i).time;
				if (t < T) break;
				if (checkArbitraryPairIsClose(i, j)) {
					Interval result = new Interval(i, j, t, "stop");
					
					return result;
				}
			}
		}
		return null;
	}
	/**
	 * �ŏ��� R �𒴂��� index ������(�������̂��߂̊ȈՔ���)
	 */
	private int searchFirstExceedIndex(int i) {
		if (i == plots.size()-1) return -1;
		Plot p = plots.get(i);
		int j = i + 1;
		for (; j < plots.size(); j++) {
			if (Coord.calcDistHubeny(p, plots.get(j)) > R) break;
		}
		if (j == plots.size()) return j -1;
		// �ŏ��� R �𒴂��� index �܂ł̎��Ԃ� T �����̒Z���ԂȂ� -1 ��ԋp
		if (plots.get(j).time - p.time < T) return -1;
		
		// j�܂ł̂��ׂĂ�index�ɂ��āAi �Ƃ̋����� R �ȓ��A���� T �ȏ�̎���
		return j;
	}
	
	/**
	 * s <= i <= e �ƂȂ�C�ӂ̂Q�_a,b�ɂ��� d(a,b)<=R �ƂȂ邩���e�X�g����
	 * ���Ԃ����ȏ�ł��邱�Ƃ̓`�F�b�N���Ȃ�
	 */
	private boolean checkArbitraryPairIsClose(int s, int e) {
		for (int i = s; i < e; i++) {
			for (int j = i+1; j < e+1; j++) {
				if (Coord.calcDistHubeny(plots.get(i), plots.get(j)) > R) return false;
			}
		}
		return true;
	}
}

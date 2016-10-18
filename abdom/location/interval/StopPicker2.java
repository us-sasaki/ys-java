package abdom.location.interval;

import java.util.List;

import abdom.location.Plot;
import abdom.location.filter.AveragingPlotsFilter;
import abdom.location.filter.MedianPlotsFilter;

/**
 * ��~���Ă��鎞�Ԃ𒊏o�BAveraging �̌�� StopPicker �̕]�����s���B
 *
 * ��~�̒�`�͈ȉ��̒ʂ�F
 * �@����(s�`t)�ɂ����āA
 * �@�@�C�ӂ̂Q�_�̋����� R(m) �ȉ�
 * �@�@(���̂Ƃ��A���a 2*R/sqrt(3) �̉~���ɂ���B�t�͕s����)
 * �@�At-s �� T msec �ȏ�
 */
public class StopPicker2 {
	protected int groups = 13; // as default
	protected StopPicker s;
	protected List<Plot> plots;
	
/*-------------
 * constructor
 */
	public StopPicker2(List<Plot> plots) {
		this.plots = plots;
		s = new StopPicker(new MedianPlotsFilter(groups).apply(plots), 30d, 60);
	}
	
/*------------------
 * instance methods
 */
	public List<Interval> divideByStop() {
		List<Interval> sl = s.divideByStop();
		
		// Averaging �ɂ�� groups -1 ���Ȃ��Ȃ��Ă���̂ŁA���ɖ߂�
		int gap = (groups - 1)/2;
		for (Interval i : sl) {
			if (i.sind > 0) i.sind += gap; // �ŏ����܂�ł�����ŏ�����
			assert i.sind >= 0 : "sind < 0";
			if (i.eind == plots.size() - groups) {
				i.eind = plots.size() -1; // �Ō���܂�ł�����Ō�܂�
			} else {
				i.eind += gap;
			}
			assert i.eind <= plots.size() - groups : "index exceeded";
			
		}
		return sl;
	}
}


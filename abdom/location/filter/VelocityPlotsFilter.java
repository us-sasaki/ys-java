package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Coord;
import abdom.location.Plot;
import abdom.location.PlotUtils;

/**
 * ���ȏ�̑��x�ƂȂ��Ă���_���J�b�g���� PlotsFilter�B
 * ���� Filter �͌��� List ��ύX���APlot �͑��x�A�����Čv�Z�ȊO�̕ύX��
 * ���܂���B
 *
 * @author	Yusuke Sasaki
 * @version	2016/9/28
 */
public class VelocityPlotsFilter implements PlotsFilter {
	
	protected double threshold;
	
/*-------------
 * Constructor
 */
	/**
	 * �f�t�H���g�� threshold (42m/s = 150km/h) ������Ƃ���
	 * VelocityPlotsFilter ���쐬���܂��B
	 */
	public VelocityPlotsFilter() {
		this(42d); // 150km/h
	}
	
	/**
	 * �w�肳�ꂽ threshold (m/s) ������Ƃ��� VelocityPlotsFilter ��
	 * �쐬���܂��B
	 *
	 * @param	threshold	�J�b�g�ΏۂƂȂ�Ȃ����x�̏��(m/s)
	 */
	public VelocityPlotsFilter(double threshold) {
		this.threshold = threshold;
	}

/*------------
 * implements
 */
	@Override
	public List<Plot> apply(List<Plot> plots) {
		PlotUtils.calcVelocity(plots); // CutReturning �͑��x��j�󂷂�
		
		for (int i = 0; i < plots.size(); i++) {
			Plot p = plots.get(i);
			if (p.velocity > threshold) {
				double latxlng = p.latitude * p.longitude; // ����_���J�b�g����
				// �J�b�g
				remove(plots, latxlng);
				i = -1; // �ŏ�����
			}
		}
		return plots;
	}
	
/*------------------
 * instance methods
 */
	private void remove(List<Plot> plots, double latxlng) {
		for (int i = 0; i < plots.size(); i++) {
			Plot p = plots.get(i);
			if (p.latitude * p.longitude == latxlng) {
				// �J�b�g
				plots.remove(i);
				if (i >= plots.size()-1) return; // �Ō�̓_��������I���
				// ���̓_�� velocity �Čv�Z
				Plot pre = null;
				if (i == 0) pre = plots.get(0);
				else pre = plots.get(i-1);
				p = plots.get(i); // ���̂��
				double dist = Coord.calcDistHubeny(p, pre);
				double time = (double)(p.time - pre.time);
				if (time == 0d) p.velocity = 0d;
				else p.velocity = dist/time*1000d;
				p.distance = dist;
				i--;
			}
		}
	}
}

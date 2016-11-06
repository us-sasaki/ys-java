package abdom.location.filter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import abdom.location.Plot;
import abdom.location.PlotUtils;

/**
 * �A�����������_�ɖ߂����ꍇ�A�߂����_�ƌ��̓_���Ƃ��ɍ폜���܂��B
 * �߂����_�̒���Ɋւ��ẮA�A�����铯��_���폜���܂��B
 * CutSamePlotsFilter �����s����ƊY������_�͍폜����Ă��܂����߁A
 * ���s�����ɒ��ӂ��Ă��������B
 * �����_�ł��邩�ǂ����́Alatitude �� double �l�� == ���ǂ����Ŕ���
 * ���Ă��܂��Blongitude �͌��Ă��܂���B
 * ���� Filter �͌��� List ��ύX���܂����APlot �͕ύX���܂���B
 * �폜������ velocity �Čv�Z�����܂��B
 */
public class CutReturningCertainPlotsFilter implements PlotsFilter {
	/**
	 * 3. �A�������ɓ����_�ɖ߂鋓�����������ꍇ�A���̓_�� outlier �Ƃ���
	 *    ���ӁF 2.�����ƊY������_�͂Ȃ��Ȃ��Ă��܂�
	 *
	 *    ���@���\���ʂ�����悤��
	 *
	 * ������ List�����Č��ʂ��ς���Ă��� ������
	 */
	public List<Plot> apply(List<Plot> plots) {
		Map<Double, Plot> map = new TreeMap<Double, Plot>();
		Plot lastPlot = plots.get(0);
		map.put(lastPlot.latitude, lastPlot);
		for (int i = 1; i < plots.size(); i++) {
			Plot p = plots.get(i);
			Plot previous = map.put(p.latitude, p); // lat�������ĂȂ�
			if ( (previous != null)&&(lastPlot != previous) ) {
				// ����̓_�ɖ߂��Ă���A�����O�łȂ�
				if (plots.remove(previous)) i--;
				if (plots.remove(p)) i--;
				// p ���폜�_�ɂȂ����Ƃ��́A������������_���폜���邽�߁A
				// lastPlot ���X�V���Ȃ�
			} else {
				// �폜����Ȃ������Ō�̓_
				lastPlot = p; 
			}
		}
		PlotUtils.calcVelocity(plots);
		return plots;
	}
	

}

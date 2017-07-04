package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import abdom.location.Plot;
import abdom.location.Coord;

/**
 * �����_�ɖ߂�_���ŏ��̂P�������č폜���܂��B
 * ���� Filter �͌��� List ��ύX���APlot �͕ύX���܂���B
 * �폜������ velocity �Čv�Z�����܂��B
 */
public class CutSamePlotsFilter implements PlotsFilter {

/*------------------
 * instance methods
 */
 	/**
	 * 2. �S�������_�ɕ����s���Ă���ꍇ�A�ŏ��̂P�������Ă��ׂ� outlier �Ƃ���
	 */
	public List<Plot> apply(List<Plot> plots) {
		Map<Double, Plot> map = new TreeMap<Double, Plot>();
		Plot pre = plots.get(0);
		map.put(pre.latitude, pre);
		for (int i = 1; i < plots.size(); i++) {
			Plot p = plots.get(i);
			Plot previous = map.put(p.latitude, p); // lat�������ĂȂ�
			if (previous != null) {
				plots.remove(i);
				// velocity �Čv�Z
				if (i >= plots.size() -1) break;
				p = plots.get(i);
				double dist = Coord.calcDistHubeny(p, pre);
				double time = (double)( p.time - pre.time );
				if (time == 0d) p.velocity = 0d;
				else p.velocity = dist / time * 1000d;
				p.distance = dist;
				i--;
			} else {
				pre = p;
			}
		}
		return plots;
	}

}

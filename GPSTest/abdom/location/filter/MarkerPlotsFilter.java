package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import abdom.location.Plot;

/**
 * Google Maps �ŁA�e�_�Ɋ���\�������܂��B
 * ���� Filter �́AList, Plot(photoFileName�ȊO) ��ύX���܂���B
 */
public class MarkerPlotsFilter implements PlotsFilter {
	protected int step;
	
	public MarkerPlotsFilter() {
		this(30);
	}
	
	public MarkerPlotsFilter(int step) {
		this.step = step;
	}
	
	/**
	 * �}�[�N(photoFileName�Z�b�g)
	 * �@�@�}�[�N����݂̂ō폜���Ȃ�
	 *
	 * @param	target	�}�[�N����Ώۂ� List<Plot>
	 * @return	�}�[�N�� List<Plot>
	 */
	public List<Plot> apply(List<Plot> plots) {
		int count = 0;
		long t0 = plots.get(0).time;
		for (int i = 0; i < plots.size(); i++) {
			Plot p = plots.get(i);
			if (((i % step) == 0)&&(p.photoFileName == null)) {
				float v = (float)((int)(p.velocity * 10))/10f;
				float t = (float)((p.time - t0)/100L)/10f;
				p.photoFileName = String.valueOf(count++) + ":v="+v+" t="+t; //p.toString();
			}
		}
		return plots;
	}
}

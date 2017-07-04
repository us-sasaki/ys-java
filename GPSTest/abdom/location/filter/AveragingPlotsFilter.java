package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.PlotUtils;

/**
 * �ׂ荇�������� Plot �̕��ψʒu(�d�S)��V���� Plot �Ƃ��܂��B
 * Plot �̐��́A(group-1)�������܂��B����(time)�����ϒl�Ƃ��܂��B
 * accuracy ��������(-1f ���ݒ肳���)���Ƃɒ��ӂ��ĉ������B
 * 3�̕��ω��ł���΁AAccuracy ���������� ULMPlotsFilter ����ʂɐ��x��
 * �ǂ��Ȃ�܂��B
 * ���� Filter �͐V���� List, Plot �𐶐����A���� List, Plot ��ύX���܂���B
 *
 */
public class AveragingPlotsFilter implements PlotsFilter {
	protected int group;
	
/*-------------
 * constructor
 */
	/**
	 * ���ω��Ɏg�p����_�̌����f�t�H���g�l(3)�Ƃ���
	 * AveragingPlotsFilter ���쐬���܂��B
	 */
	public AveragingPlotsFilter() {
		this.group = 3;
	}
	/**
	 * ���ω��Ɏg�p����_�̌����w�肵�� AveragingPlotsFilter ���쐬���܂�
	 *
	 * @param	group	�������O���[�s���O���邩
	 */
	public AveragingPlotsFilter(int group) {
		this.group = group;
	}

/*------------
 * implements
 */
	/**
	 *
	 * @param	plots	���ω�����Ώۂ� plot
	 */
	@Override
	public List<Plot> apply(List<Plot> plots) {
		List<Plot> result = new ArrayList<Plot>();
		Plot[] groups = new Plot[group]; // memory�ȗ͉�
		
		loop:
		for (int i = 0; i < plots.size(); i++) {
			int n = 0;
			for (int j = 0; j < plots.size(); j++) {
				if (i+j >= plots.size()) break loop;
				groups[n++] = plots.get(i+j); // copy
				if (n == group) break;
			}
			// ���ω�����
			long startTime = groups[0].time; // �����ӂꂳ���Ȃ����߂̊�l
			double lat = 0d;
			double lng = 0d;
			long time = 0L;
			
			for (int j = 0; j < group; j++) {
				lat += groups[j].latitude;
				lng += groups[j].longitude;
				time += (groups[j].time - startTime);
			}
			Plot newone = new Plot(lat/group, lng/group, (time/group)+startTime, -1f);
			result.add(newone);
		}
		PlotUtils.calcVelocity(result);
		return result;
	}
	
}
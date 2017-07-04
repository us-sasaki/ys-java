package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import abdom.location.Plot;
import abdom.location.PlotUtils;

/**
 * �ׂ荇�������� Plot �̃��W�A��(���Ԓl)��V���� Plot �Ƃ��܂��B
 * Plot �̐��́A(group-1)�������܂��B����(time)�����Ԓl�Ƃ��܂��B
 * accuracy ��������(-1f��ݒ�)���Ƃɒ��ӂ��Ă��������B
 * ���� Filter �͐V���� List, Plot �𐶐����A���� List, Plot ��ύX���܂���B
 *
 * @author	Yusuke Sasaki
 * @version	2016/10/7
 */
public class MedianPlotsFilter implements PlotsFilter {
	protected int group;
	
/*-------------
 * constructor
 */
	/**
	 * �����l���o�Ɏg�p����_�̌����f�t�H���g�l(5)�Ƃ���
	 * MedianPlotsFilter ���쐬���܂��B
	 */
	public MedianPlotsFilter() {
		this.group = 5;
	}
	/**
	 * �����l���o�Ɏg�p����_�̌����w�肵�� MedianPlotsFilter ���쐬���܂�
	 *
	 * @param	group	�������O���[�s���O���邩
	 */
	public MedianPlotsFilter(int group) {
		this.group = group;
	}

/*------------
 * implements
 */
	/**
	 *
	 * @param	plots	�����l������Ώۂ� plot
	 */
	@Override
	public List<Plot> apply(List<Plot> plots) {
		List<Plot> result = new ArrayList<Plot>();
		List<Double> xgroups = new ArrayList<Double>(); // memory�ȗ͉�
		List<Double> ygroups = new ArrayList<Double>();
		List<Long> tgroups = new ArrayList<Long>();
		int mid = group / 2;
		
	loop:
		for (int i = 0; i < plots.size(); i++) {
			xgroups.clear();
			ygroups.clear();
			tgroups.clear();
			for (int j = 0; j < group; j++) {
				if (i+j >= plots.size()) break loop;
				Plot p = plots.get(i+j);
				xgroups.add(p.latitude);
				ygroups.add(p.longitude);
				tgroups.add(p.time);
			}
			// �����l���o
			xgroups.sort(null);
			ygroups.sort(null);
			tgroups.sort(null);
			
			Plot newone = new Plot(xgroups.get(mid), ygroups.get(mid), tgroups.get(mid), -1f);
			result.add(newone);
		}
		PlotUtils.calcVelocity(result);
		return result;
	}
	
}
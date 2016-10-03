package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.Coord;
import abdom.location.PlotUtils;
import abdom.math.stats.Stats;
import abdom.math.stats.Tdist;

/**
 * ���x�Ɋւ��ASmirnov-Grubbs ������s�� Filter �ł��B
 * �f�t�H���g�̊��p���A�ő�J�b�g�����͂Ƃ��ɂT���ł��B
 * ��Ԃ��Ƃɑ��x���قȂ�ꍇ�A�����Ƃ��������x����͂��ꂽ���x������
 * �J�b�g����Ă����܂��B
 * ���������Č덷�����K���z�ɏ]���悤�ȏꍇ�ɂ��� filter �͍ł��L���ł��B
 * �����łȂ��ꍇ�AmaxCutRate �𒲐����邱�Ƃŉ��P���邱�Ƃ�����܂��B
 * �����Ɨǂ��̂́A�����K�E�X���f�����̕��ނ�p���Ċe��Ԃ̕��ϑ��x��
 * ���܂�ς��Ȃ��悤�ɕ���������ŋ�ԒP�ʂɂ���filter ���g�����Ƃ�
 * �v���܂��B
 * ���� Filter �͌��� List ��ύX���APlot �͑��x�A�����Čv�Z�ȊO�̕ύX��
 * ���܂���B
 *
 * @author	Yusuke Sasaki
 * @version	2016/9/24
 */
public class CutOutlierPlotsFilter implements PlotsFilter {

	/** 5%���� */
	private double rejection = 0.05d;
	
	private double maxCutRate = 0.05d; // 5%
	
/*-------------
 * Constructor
 */
	public CutOutlierPlotsFilter() {
	}
	
	/**
	 * ���藦(���p��)���w�肵�� Smirnov-Grubbs������s�� filter ���쐬���܂��B
	 *
	 * @param	rejection	���p��(0.05 = ���� 5% ���͂���ƌ��Ȃ�)
	 */
	public CutOutlierPlotsFilter(double rejection) {
		this.rejection = rejection;
	}
	
	/**
	 * ���藦(���p��)�A�ő�J�b�g�������w�肵�� Smirnov-Grubbs������s��
	 * filter ���쐬���܂��B
	 * 
	 * @param	rejection	���p��(0.05 = ���� 5% ���͂���ƌ��Ȃ�)
	 * @param	maxCutRate	�ő�J�b�g����(0.05 = �S�̂� 5% �J�b�g������I��)
	 */
	public CutOutlierPlotsFilter(double rejection, double maxCutRate) {
		this.rejection = rejection;
		this.maxCutRate = maxCutRate;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 4. ���x�ɂ��āA�O��l�����O����(Smirnov-Grubbs����)
	 * �w�肳�ꂽ plots �� velocity �͍ŏ��ɍČv�Z����܂��B
	 *
	 * @param	plots	�O��l�������s���Ώ�
	 * @return	�O��l�������s��������
	 */
	public List<Plot> apply(List<Plot> plots) {
		plots = PlotUtils.calcVelocity(plots); // non outlier ��ΏۂɍČv�Z
		int count = 0;
		int maxCount = (int)(plots.size() * maxCutRate);
		while (true) {
			int oi = chooseOneOutlier(plots);
			if (oi == -1) break; // ���p������̂͂Ȃ�
			count++;
			if (count >= maxCount) break;
		}
		return plots;
	}
	
	
	//
	// outlier �t���O�����āA�K�v�Ȃ���� velo �Čv�Z����
	//
	private int chooseOneOutlier(List<Plot> plots) {
		// ���x�Ɋւ����{���v�ʂ����߂�
		Stats<Plot> vs = new Stats<Plot>();
		vs.apply(plots, (plot -> plot.velocity ));
		
		// �X�~���m�t�E�O���u�X�̃������߂�
		// t : ���R�x n-2 �� t���z�� (��/���~100) �p�[�Z���^�C��
		double t = Tdist.dist(vs.n - 2, rejection/vs.n*100); // �萔(95�p�[�Z���^�C��)
		double gamma = ((vs.n-1) * t)/Math.sqrt(vs.n * (vs.n-2) + vs.n * t * t);
		
		// ���肷��
		int index = -1;
		double maxv = gamma; // ���𒴂�����̂͊O��l
		
		for (int i = 0; i < plots.size(); i++) {
			// �������肵�Ă��邪�A�����Б�
			double v = Math.abs(plots.get(i).velocity - vs.mean) / vs.deviation;
			if (v > maxv) { // �O��l����
				index = i;
				maxv = v;
			}
		}
		
		if (index == -1) return index; // �O��l���Ȃ�����
		plots.remove(index); // �폜
		
		// ���� non outlier �� velocity �̏C�����K�v
		if (index == 0) { // �ŏ��� plot ���}�[�N���ꂽ
			plots.get(0).velocity = 0d; // �f�t�H���g�l
		} else if (index <= plots.size() - 1) {
			double dist = Coord.calcDistHubeny(plots.get(index-1), plots.get(index));
			double time = (double)(plots.get(index).time - plots.get(index-1).time);
			if (time == 0d) plots.get(index).velocity = 0d;
			else plots.get(index).velocity = dist/time * 1000d;
			plots.get(index).distance = dist;
		}
		return index;
	}
}

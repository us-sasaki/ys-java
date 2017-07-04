package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.Coord;
import abdom.location.PlotUtils;

/**
 * Uniform Linear Motion
 * ���������^��������AAccuracy���������Ĉʒu��␳����B
 *
 * �A������R�_(p(-1), p(0), p(1))�ɂ��āA���������^���ƂȂ�悤�ȁA
 * �R�_��Accuracy�𓥂܂��m��(�ޓx)���ő�ƂȂ�O�Ղ����߂�B<br>
 * x(-1), x(0), x(1)
 *    s.t. F(p(-1),x(-1)) * F(p(0),x(0)) * F(p(1),x(1)) ���ő�
 *         ���� x(-1), x(0), x(1) �͓��������^��
 * ������ F�Fp()�ɂ����� �W���΍�=accuracy �𖞂����m�����x�֐�(���K���z)�Ƃ���B
 * 
 * �␳�́Ap(0) -> x(0) �̂悤�ɐ^�񒆂̓_�ɂ��čs���B
 *
 * ���� Filter �́A�V���� List ��ԋp���A���� List �𑬓x�A������������
 * �ύX���܂���B���s��A���x�A�������ύX����܂����A�V���� List �Ɋ
 * ���l���ݒ肳��Ă��邽�߁A���� List �Ƃ��Ă͕s���Ȓl�ƂȂ��Ă��邱��
 * �ɒ��ӂ��ĉ������B
 * ���������邽�߂ɂ� PlotUtils.calcVelocity ������ List �ɑ΂��Ďg�p��
 * �Ă��������B
 */
public class ULMPlotsFilter implements PlotsFilter {

	// �Q�_�Ԃ̎��Ԃ��󂭂ƌ덷���傫���Ȃ�B�␳���X�L�b�v����臒l
	private double maxTimeRange = 10d;
	
/*-------------
 * constructor
 */
	public ULMPlotsFilter() {
	}
	
	public ULMPlotsFilter(double maxTimeRange) {
		this.maxTimeRange = maxTimeRange;
	}
	
/*------------------
 * instance methods
 */
 	/**
	 * �Ǐ��I(10�b���x)�ɓ��������^�������肵���␳���s���B
	 * ���ʂ� List, Plot �I�u�W�F�N�g�͐V�K�ɍ쐬����܂��B
	 */
	public List<Plot> apply(List<Plot> plots) {
		List<Plot> result = new ArrayList<Plot>();
		
		for (int i = 0; i < plots.size(); i++) {
			// �R�_���Ƃ�
			Plot a = null;
			if (i == 0) a = plots.get(i); else a = plots.get(i-1);
			Plot b = plots.get(i); // ��_
			Plot c = null;
			if (i == plots.size()-1) c = plots.get(i); else c = plots.get(i+1);
			
			// �v�Z
			
			// ����(sec)
			double s = (double)(b.time - a.time) / 1000d;
			double t = (double)(c.time - b.time) / 1000d;
			
			Plot x = new Plot();
			if ( (s > maxTimeRange)||(t > maxTimeRange) ) {
				// �R�_�̎��Ԃ����ꂷ���Ă��邽�߁A�␳���Ȃ������悢
				// x = b �Ƃ��Ă��悢���A�V�����I�u�W�F�N�g�Ƃ��邱�Ƃɓ���
				x.latitude	= b.latitude;
				x.longitude	= b.longitude;
				x.time		= b.time;
				x.isOutlier	= b.isOutlier;
				x.date		= b.date;
				x.accuracy	= b.accuracy;
			} else {
				// �R�_�̎��Ԃ���r�I�߂�(�e�_��10�b�ȓ���ڈ�)�̂ŕ␳
				
				// �W���΍�(m)�@�̂Q��(���U)�̋t��
				double va = 1d / (double)a.accuracy / (double)a.accuracy;
				double vb = 1d / (double)b.accuracy / (double)b.accuracy;
				double vc = 1d / (double)c.accuracy / (double)c.accuracy;
				
				// ����
				double m = va*vc*(t+s)*(t+s)+vb*(va*s*s+vc*t*t);
				
				// a,b,c �̌W��
				double ka = va*vc*t*(s+t)/m;
				double kb = vb*(va*s*s+vc*t*t)/m;
				double kc = va*vc*s*(s+t)/m;
				
				// x �����߂�
				
				// ���Ԃ� m �ɍ��킹�Ȃ��Ă悢(�Ǐ��I�ɕ��ʂƉ���)
				// ka, kb, kc �̓��[�g���P�ʂ̒l�����A���̂܂� lat,lng
				// �ɓK�p���Ă���(lat, lng �����[�g���ɔ��=�Ǐ��I�ɕ��ʁ@�A�Ɖ���)
				x.latitude = ka*a.latitude + kb*b.latitude + kc*c.latitude;
				x.longitude = ka*a.longitude + kb*b.longitude + kc*c.longitude;
				
				// velocity �͋��߂Ȃ�(�Ō�ɂ܂Ƃ߂Ă��)
				
				x.time = b.time;
				x.isOutlier = false;
				x.date = b.date;
				x.accuracy = b.accuracy; // accuracy�͕ς��Ȃ��ł���
				
				//
				// ���Ȃ݂� v ��
				// (����) = m �œ���
				// ka = va*va*(-vb*vb*s-vc*vc*t-vc*vc*s) / m
				// kb = vb*vb*(va*va*s-vc*vc*t) / m
				// kc = vc*vc*(va*va*s-va*va*t-vb*vb*t) / m
				//
				// x - sv ��1�O�̓_
				// x + tv ��1��̓_
			}
			result.add(x);
		}
		result = PlotUtils.calcVelocity(result);
		
		return result;
	}
	
/*--------------
 * test �p main
 */
	public static void main(String[] args) throws Exception {
		List<Plot> plots = new ArrayList<Plot>();
		
		Plot a = new Plot();
		a.latitude = -1;
		a.longitude = 1;
		a.time = 1000L;
		a.accuracy = 50;
		plots.add(a);
		
		Plot b = new Plot();
		b.latitude = 0;
		b.longitude = 0;
		b.time = 2000L;
		b.accuracy = 50;
		plots.add(b);
		
		Plot c = new Plot();
		c.latitude = 1;
		c.longitude = 1;
		c.time = 3000L;
		c.accuracy = 90;
		plots.add(c);
		
		plots = new ULMPlotsFilter().apply(plots);
		
		for (Plot p : plots) {
			System.out.println(p);
		}
		
	}

}

package abdom.math.stats;

import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.List;

/**
 * ��{�I�ȓ��v�ʂ����߂�B
 * T �͓��v�ʂ����߂���double�l���o�͂ł���N���X�B
 * ���ۂ̒l�̏o�͂� apply �̑�Q����(Function<T, Double>)�Ŏw�肷��B
 */
public class Stats<T> {
	public boolean applied = false;
	
	/** ���a */
	public double sum;
	
	/** ���� */
	public double mean;
	
	/** �v�f��(apply�̑�Q������null��Ԃ����̂͊܂܂Ȃ�) */
	public int n;
	
	/** ���U */
	public double variance;
	
	/** �W���΍� */
	public double deviation;
	
	/** �ő�l */
	public double max;
	
	/** �ŏ��l */
	public double min;
	
/*------------------
 * instance methods
 */
	/**
	 * �z��ƁAdouble�l���o�֐����w�肵�A���v�ʂ�ݒ肷��
	 */
	public void apply(T[] data, Function<T, Double> f) {
		n = 0;
		sum = 0d;
		variance = 0d;
		max = -Double.MAX_VALUE;
		min = Double.MAX_VALUE;
		
		for (int i = 0; i < data.length; i++) {
			Double d = f.apply(data[i]);
			if (d != null) { // d == null �͏��O����
				n++;
				sum += d;
				if (d > max) max = d;
				else if (d < min) min = d;
			}
		}
		mean = sum / n;
		
		for (int i = 0; i < data.length; i++) {
			Double d = f.apply(data[i]);
			if (d != null) {
				double a = d - mean;
				variance += a*a;
			}
		}
		variance = variance / n;
		deviation = Math.sqrt(variance);
		
		applied = true;
	}
	
	/**
	 * �f�[�^�� List �ŗ^������ꍇ
	 */
	public void apply(List<T> data, Function<T, Double> f) {
		n = 0;
		sum = 0d;
		variance = 0d;
		max = -Double.MAX_VALUE;
		min = Double.MAX_VALUE;
		
		for (T datum: data) {
			Double d = f.apply(datum);
			if (d != null) { // d == null �͏��O����
				n++;
				sum += d;
				if (d > max) max = d;
				else if (d < min) min = d;
			}
		}
		mean = sum / n;
		
		for (T datum : data) {
			Double d = f.apply(datum);
			if (d != null) {
				double a = d - mean;
				variance += a*a;
			}
		}
		variance = variance / n;
		deviation = Math.sqrt(variance);
		
		applied = true;
	}
	
	/**
	 * List �ƁAList �̗v�f����Y��������p���� double ���o�͂���ꍇ�B
	 *<pre>
	 * ��) T = java.awt.Point
	 *     BiFunction = (pList, i) ->
	 *                    ( pList.get(i).x - pList.get( (i==0)?0:i-1 ).x )
	 *     (1�O�̓_�Ƃ� x ���̍�) �Ɋւ��铝�v�ʂ𓾂�
	 *</pre>
	 *
	 */
	public void apply(List<T> data, BiFunction<List<T>, Integer, Double> f) {
		n = 0;
		sum = 0d;
		variance = 0d;
		max = -Double.MAX_VALUE;
		min = Double.MAX_VALUE;
		
		for (int i = 0 ; i < data.size(); i++) {
			Double d = f.apply(data, i);
			if (d != null) { // d == null �͏��O����
				n++;
				sum += d;
				if (d > max) max = d;
				else if (d < min) min = d;
			}
		}
		mean = sum / n;
		
		for (int i = 0; i < data.size(); i++) {
			Double d = f.apply(data, i);
			if (d != null) {
				double a = d - mean;
				variance += a*a;
			}
		}
		variance = variance / n;
		deviation = Math.sqrt(variance);
		
		applied = true;
	}
	
	/**
	 * ���K���z(1����)�̊m�����x�֐�
	 *
	 * @param	x		���߂����_
	 * @param	mean	���ϒl(��)
	 * @param	dev		�W���΍�
	 * @return	���K���z�̊m�����x�֐��̒l
	 */
	public static double gaussian(double x, double mean, double dev) {
		double var = dev * dev; // ���U
		double t = 1d / Math.sqrt(2d * Math.PI * var); // �W��
		double t2 = (x - mean) * (x - mean) / 2d / var;
		return t * Math.exp(-t2);
	}
	
	/**
	 * ���̃I�u�W�F�N�g�������ρA���U(�W���΍�)��p�������K���z�̊m�����x�֐�
	 * 
	 * @param	x		���߂����_
	 * @return	���K���z�̊m�����x�֐��̒l
	 */
	public double gaussian(double x) {
		double t = 1d / Math.sqrt(2d * Math.PI * variance); // �W��
		double t2 = (x - mean) * (x - mean) / 2d / variance;
		return t * Math.exp(-t2);
	}
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("n     = "+n+"\n");
		sb.append("mean  = "+mean+"\n");
		sb.append("var.  = "+variance+"\n");
		sb.append("dev.  = "+deviation+"\n");
		sb.append("max   = "+max+"\n");
		sb.append("min   = "+min+"\n");
		
		return sb.toString();
	}
}

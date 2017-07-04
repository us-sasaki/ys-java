package abdom.location.filter;

import java.util.List;

import abdom.location.Plot;
import abdom.math.stats.Stats;

/**
 * plots �̓��v���� System.out �ɏo�͂��� Filter �ł��B
 * �f�o�b�O��A���x���P�ȂǂɎg�p���邱�Ƃ�z�肵�Ă��܂��B
 * ���� Filter �� List, Plot ��ύX���܂���B
 */
public class PrintPlotsFilter implements PlotsFilter {
	protected String title;
	
/*-------------
 * constructor
 */
	/**
	 * �f�t�H���g�̃^�C�g���� PrintPlotsFilter ���쐬���܂��B
	 */
	public PrintPlotsFilter() {
		this("--- Plot\'s Velocity Stats. ---");
	}
	
	/**
	 * �^�C�g�����w�肵�� PrintPlotsFilter ���쐬���܂��B
	 */
	public PrintPlotsFilter(String title) {
		this.title = title;
	}
	
/*------------
 * implements
 */
	@Override
	public List<Plot> apply(List<Plot> plots) {
		Stats<Plot> vstats = new Stats<Plot>();
		vstats.apply(plots, p -> p.velocity);
		System.out.println(title);
		System.out.println(vstats);
		return plots;
	}
}

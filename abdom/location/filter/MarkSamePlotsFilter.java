package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import abdom.location.Plot;

/**
 * �����_�ɕ�����s���Ă���ꏊ�� photoFileName ���Z�b�g���܂��B
 * Google Maps �ł́A�����\������܂��B
 * ���� Filter �́AList, Plot(photoFileName�ȊO) ��ύX���܂���B
 */
public class MarkSamePlotsFilter implements PlotsFilter {
	/**
	 * 1. �S�������_�ɕ�����s���Ă���Ƃ�����}�[�N(photoFileName�Z�b�g)
	 * �@�@�}�[�N����݂̂ō폜���Ȃ�
	 *
	 * @param	target	�}�[�N����Ώۂ� List<Plot>
	 * @return	�}�[�N�� List<Plot>
	 */
	public List<Plot> apply(List<Plot> target) {
		Map<Double, Plot> map = new TreeMap<Double, Plot>();
		for (Plot p : target) {
			Plot recent = map.put(p.latitude, p); // lat�������ĂȂ�
			if (recent != null) {
				recent.photoFileName = recent.toString();
				p.photoFileName = p.toString();
			}
		}
		return target;
	}
}

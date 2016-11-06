package abdom.location;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import abdom.data.json.*;
import abdom.math.stats.Stats;


public class PlotUtils {
	/**
	 * �w�肳�ꂽGPSLog�t�@�C����ǂݍ��݁AList<Plot> �Ƃ��ĕԋp����
	 */
	public static List<Plot> readGPSLog(String gpslogDir, String fname) throws IOException {
		// �ǂ�
		Plot[] plots = File2Plot.read(gpslogDir + fname);
		
		// Arrays.asList(Plot<>) �łł��� List �� Arrays$ArrayList �N���X��
		// remove ���ł��Ȃ�(UnsupportedOperationException)�̂Ŏ蓮�쐬
		List<Plot> result = new ArrayList<Plot>();
		for (Plot p : plots) result.add(p);
		
		return calcVelocity(result);
	}
	
	/**
	 * ���x�A������(��)�v�Z����B
	 * ���� plot �̑��x�́A���̂ЂƂO�� plot �Ƃ̋����A�o�ߎ��Ԃɂ���Čv�Z�����B
	 * �����̒P�ʂ� m�A���Ԃ̒P�ʂ� msec �����A�i�[���鑬�x�� m/s �Ƃ���B
	 */
	public static List<Plot> calcVelocity(List<Plot> plots) {
		// ������Ȃ��ꍇ�A���̍s�� ArrayIndexOutOfBounds ���X���[�����
		plots.get(0).velocity = 0d;
		Plot last = plots.get(0);
		for (int i = 1; i < plots.size(); i++) {
			Plot p = plots.get(i);
			double dist = Coord.calcDistHubeny(last, p);
			double time = (double)(p.time - last.time);
			if (time == 0d) p.velocity = 0d;
			else p.velocity = dist/time * 1000d;
			p.distance = dist;
			last = p;
		}
		
		return plots;
	}
	
	/**
	 * �w�肳�ꂽ plots �̑��s��(m)���v�Z����
	 */
	public static double calcTotalDistance(List<Plot> plots) {
		double sum = 0d;
		for (Plot p : plots) sum += p.distance;
		return sum;
	}
	
	/**
	 * �w�肳�ꂽ Plot[] �̊�{���v�ʂ�\������
	 * ��{���v�ʂ� plot.velocity �Ɋւ��Čv�Z����B
	 *
	 * @param	plots	��{���v�ʂ��v�Z����ΏۂƂȂ� Plot[]
	 */
	public static void printStats(List<Plot> plots) {
		Stats<Plot> vStats = new Stats<Plot>();
		vStats.apply(plots, (plot -> plot.velocity) );
		
		System.out.println(vStats);
	}
	
	/**
	 * �w�肳�ꂽ Plot[] ���A�w�肳�ꂽ�t�@�C����(�g���q .txt -> .json)�ŕۑ����܂��B
	 * �����R�[�h�� UTF-8 ���w�肵�܂��B
	 *
	 * @param	fname	�ۑ����� Json �t�@�C����(�������g���q .txt �Œ�)
	 * @param	plots	�ۑ����� Plot �f�[�^
	 */
	@Deprecated
	public static void writeJson(String jsonDir, String fname, List<Plot> plots) throws IOException {
		// �t�@�C�����𐶐�
		String jsonfname = changeExtension(fname, ".json");
		
		// ����
		writeString(jsonDir + jsonfname, plotsToJson(plots).toString());
	}
	
	/**
	 * �t�@�C���Ƃ��Ďw�肳�ꂽ��������������݂܂��B
	 * JSON ���������ނ��Ƃ�z�肵�Ă��܂��BUTF-8���g�p����܂��B
	 *
	 * @param	path	�t�@�C���p�X(�t���p�X�A���΃p�X)
	 * @param	content	�������ޓ��e�B�Ō�ɂ͉��s������܂��B
	 */
	public static void writeString(String path, String content) throws IOException {
		// ����
		PrintWriter p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
		p.println(content);
		p.close();
	}
	
	/**
	 * �t�@�C���Ƃ��Ďw�肳�ꂽ��������������݂܂��B
	 * �t�@�C���p�X�̊g���q�͎w�肳�ꂽ���̂ɒu���������܂��B�t�@�C�����{�̂�
	 * �ύX����܂���B
	 *
	 * @param	path	�t�@�C���p�X(�t���p�X�A���΃p�X)
	 * @param	ext		�ύX��̊g���q
	 */
	public static void writeString(String path, String ext, String content) throws IOException {
		String fname = changeExtension(path, ext);
		writeString(fname, content);
	}
	
	/**
	 * List<Plot> �� JsonArray �ɕϊ����܂��B
	 */
	public static JsonArray plotsToJson(List<Plot> plots) {
		JsonType[] jt = Plot.toJson(plots.toArray(new Plot[0]));
		
		return new JsonArray(jt);
	}
	
	/**
	 * �p�X��������̂����A�Ō�� . �ȍ~�̕������w�肳�ꂽ�g���q��
	 * �ϊ����܂��B�p�X��������́A�g���q���̎��̂̂���t�@�C���ł��邱�Ƃ�
	 * ���肵�Ă��܂��B
	 */
	public static String changeExtension(String path, String ext) {
		if (!ext.startsWith(".")) throw new IllegalArgumentException("ext �� . �ł͂��߂ĉ�����");
		int idx = path.lastIndexOf(".");
		if (idx == -1) return path + ".dummy";
		String name = path.substring(0, idx);
		return name + ext;
	}
	
	/**
	 * �w�肳�ꂽ Plot[] ���A�w�肳�ꂽ�t�@�C����(�g���q .txt -> .csv)�ŕۑ�����
	 * ���ԂƑ��x�̊֌W�� csv �ŕۑ�����B
	 *
	 * @param	fname	�ۑ����� csv �t�@�C����
	 * @param	plots	�ۑ����� Plot �f�[�^
	 */
	public static void writeVelocity(String fname, List<Plot> plots) throws IOException {
		
		// �t�@�C�����𐶐�
		int idx = fname.indexOf(".txt");
		String csvfname = fname.substring(0, idx);
		
		// ����
		PrintWriter p = new PrintWriter(new FileWriter(csvfname+".csv"));
		for (Plot plot : plots) {
			p.println(plot.time + "," + (plot.time-1463797343309L)/1000 + "," + plot.velocity);
		}
		
		p.close();
	}
	
	/**
	 * �w�肳�ꂽ Plot[] �̏d�S�����߂�
	 * lat, lng, time �͏d�S�Ƃ���
	 */
	public static Plot calcCentroid(List<Plot> plots) {
		double slat = 0d;
		double slng = 0d;
		long stime = 0L;
		float acc = Float.MAX_VALUE;
		
		for (Plot p: plots) {
			slat += p.latitude;
			slng += p.longitude;
			stime += p.time;
			if (acc > p.accuracy) acc = p.accuracy;
		}
		int n = plots.size();
		Plot result = new Plot(slat/n, slng/n, stime/n, acc);
		
		return result;
	}
	
	
	
}

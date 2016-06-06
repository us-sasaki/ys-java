import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class SimilarFilePicker {
	// (FileEntry, FileEntry) -> distance
	private List<FileDistance> dist;
	static class FileDistance {
		FileEntry a;
		FileEntry b;
		int dist;
		FileDistance(FileEntry a, FileEntry b, Integer dist) {
			this.a = a;
			this.b = b;
			this.dist = dist;
		}
	}
	
/*-------------
 * Constructor
 */
	public SimilarFilePicker(List<FileEntry> list) {
		//
		dist = new ArrayList<FileDistance>();
		
		System.out.println("SimilarFilePicker: calculating distances");
		int size = list.size();
		
		int loopCount = (size - 1) * size / 2;
		int count = 0;
		int percentage = 0;
		
		// ���Ȃ�x���BCPU 25%�����g���ĂȂ��̂ŁA�}���`�X���b�h�ɂ��������悢����
		for (int i = 0; i < size - 1; i++) {
			for (int j = i+1; j < size; j++) {
				FileEntry a = list.get(i);
				FileEntry b = list.get(j);
				
				dist.add(new FileDistance(a, b, d(a,b) ));
				
				count++;
				if (count >= loopCount/100*percentage) {
					System.out.print("" + percentage +"%..");
					System.out.flush();
					percentage += 5;
				}
			}
		}
		
		System.out.println("SimilarFilePicker: sorting by distance");
		// Integer �Ń\�[�g(����)
		dist.sort(new Comparator<FileDistance>() {
				public int compare(FileDistance a, FileDistance b) {
					if (a.dist != b.dist) return (a.dist - b.dist);
					if (a.a.path.compareTo(b.a.path) != 0) return (a.a.path.compareTo(b.a.path));
					return (a.b.path.compareTo(b.b.path));
				}
				public boolean equals(FileDistance a, FileDistance b) {
					return (a.dist == b.dist)&&(a.a.path.equals(b.a.path))&&(a.b.path.equals(b.b.path));
				}
			} );
		
	}
	
/*------------------
 * instance methods
 */
	public List<FileDistance> getDistanceList() {
		return dist;
	}
	
/*----------------
 * static methods
 */
	private static String longestCommonSubsequence(String a, String b) {
		if (a.length() > b.length() ) {
			// a <= b �ƂȂ�悤�ɂ���
			String c = a; a = b; b = c;
		}
		
		int maxLen = 0;
		int maxLenStartIdxA = -1;
		int alen = a.length();
		int blen = b.length();
		
		for (int i = 0; i < alen; i++) {
			//if (alen - i < maxLen) break; // ��߂��ċL�^�X�V�ł��Ȃ�
			// ��(�t�ɒx���Ȃ肻���Ȃ̂�cut)
			char c = a.charAt(i);
			int bidx = b.indexOf(c);
			if (bidx == -1) continue;
			if (blen - bidx < maxLen) break; // ��߂��ċL�^�X�V�ł��Ȃ�
			
			// 1����������
			int len = 1;
			bidx++;
			for (int j = i+1; j < alen; j++) { // �ǂ��܂ō����Â��邩
				if (bidx >= blen) break;
				if (a.charAt(j) != b.charAt(bidx)) break;
				len++;
				bidx++;
			}
			if (len > maxLen) { // �L�^�X�V
				maxLen = len;
				maxLenStartIdxA = i;
			}
		}
		if (maxLen == 0) return "";
		return a.substring(maxLenStartIdxA, maxLenStartIdxA+maxLen);
		
//		���̎����͒x������̂Ŗv
//		int length = a.length();
//		String subs = null;
//		
//		loop:
//		for (int len = length; len > 0; len--) {
//			for (int i = 0; i <= length-len; i++) {
//				subs = a.substring(i, i+len);
//				if (b.indexOf(subs) >= 0) return subs;
//			}
//		}
//		return "";
	}
	
	/**
	 * �Q��FileEntry�̋����𑪂�B
	 * �������A !FileEntry.isDirectory �ł���K�v������B
	 */
	private static int d(FileEntry a, FileEntry b) {
		//
		// �O����
		//
		
		// �t�@�C�������擾
		String na = new File(a.path).getName();
		String nb = new File(b.path).getName();
		
		// extension �Ɩ��O�{�̂��擾
		int ind = na.lastIndexOf('.');
		String pa = na;
		String ea = "";
		if (ind > -1) { pa = na.substring(0,ind); ea = na.substring(ind+1); }
		ind = nb.lastIndexOf('.');
		String pb = nb;
		String eb = "";
		if (ind > -1) { pb = nb.substring(0,ind); eb = nb.substring(ind+1); }
		
		//
		// �����Z�o
		//
		int d = 0;
		
		// extension ���Ⴄ�Ƃ��Ȃ�Ⴄ�t�@�C��
		// (�f�[�^������Β萔���w�K�����������悢�H)
		if (!ea.equals(eb)) d += 10000;
		
		// �ő勤�ʕ�����T��
		String sub = longestCommonSubsequence(pa, pb);
		int ia = pa.indexOf(sub);
		int ib = pb.indexOf(sub);
		// ����������
		String diff = pa.substring(0, ia) + pa.substring(ia+sub.length())
						+ pb.substring(0, ib) + pb.substring(ib+sub.length());
		// ����������̎�ނɂ���ċ������قȂ�
		// ���t�f�[�^������Ε�����Ƌ����͊w�K�����������邩���m��Ȃ�
		for (int i = 0; i < diff.length(); i++) {
			char c = diff.charAt(i);
			if (c >= '0' && c <= '9') d += 2;
			else if (c == '_')  d += 2;
			else if (c == '-')  d += 2;
			else if (c == ' ')  d += 2;
			else if (c == '�@') d += 2;
			else if (c == '(')  d += 2;
			else if (c == ')')  d += 2;
			else if (c == '�i') d += 2;
			else if (c == '�j') d += 2;
			else if (c == '[')  d += 2;
			else if (c == ']')  d += 2;
			else if (c == '�u') d += 2;
			else if (c == '�v') d += 2;
			else if (c == '<')  d += 2;
			else if (c == '>')  d += 2;
			else if (c == '��') d += 2;
			else if (c == '��') d += 2;
			else d += 20;
		}
		
		// ��v���Ă��镔�����������Ƌ����͒���(���͓K��)
		d = d * 1000 / (sub.length() + 5); // �w�K�̗]�n����H
		
		// �T�C�Y�ɂ�锻�����������
		long sizeGap = a.size - b.size;
		long minSize = b.size;
		if (sizeGap < 0) {
			sizeGap = -sizeGap;
			minSize = a.size;
		}
		if (sizeGap > 0) d += (int)(Math.log(sizeGap) * 200); // �w�K�̗]�n����H
		
		// �X�V�����ɂ�锻����ǉ�
		long dateGap = a.lastModified - b.lastModified;
		if (dateGap < 0) dateGap = -dateGap;
		
		d += (int)(Math.log(dateGap) * 10); // �w�K�̗]�n����H
		
		return d;
	}
	
	public static void main(String[] args) throws Exception {
		String s = longestCommonSubsequence("�K���ȕ�����35", "������ƈႤ�K���ȕ�����");
		System.out.println(s);
		System.out.println("����="+s.length());
		
		FileList l = MakeFileUsage.readFiles(".");
		List<FileDistance> fdl = new SimilarFilePicker(l.list).getDistanceList();
		for (int i = 0; i < 1000; i++) {
			FileDistance f = fdl.get(i);
			System.out.println("---------------------------------------");
			System.out.println("           " + f.dist + "             ");
			System.out.println("---------------------------------------");
			System.out.println(f.a);
			System.out.println(f.b);
		}
		
	}
}

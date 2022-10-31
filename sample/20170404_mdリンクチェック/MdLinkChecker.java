import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * md �t�@�C����ǂݍ��݁A�����N���؂�Ă��Ȃ����`�F�b�N����B
 * http �Ŏn�܂���̂͊O�������N�Ƃ���
 *
 */
public class MdLinkChecker {
	private static final String ENCODING = "UTF-8";
	
	protected String rootUrl;
	protected String base; // �t�@�C���̃��[�g�f�B���N�g��
	protected File basefile;
	
	protected List<String> resources;
	protected List<Link> links;
	protected Set<String> imageResources;
	
	protected static class Link {
		String link;
		String fname;
		String line;
	}
	
/*-------------
 * constructor
 */
	public MdLinkChecker(String rootUrl, String base) {
		this.rootUrl = rootUrl;
		this.base = base;
		this.basefile = new File(base);
		
		resources = new ArrayList<String>();
		links = new ArrayList<Link>();
		imageResources = new TreeSet<String>();
	}
	
	/**
	 * base �����[�g�p�X�Afname �𑊑΃p�X�Ƃ��� md �t�@�C����ǂݍ���
	 *
	 * @param	fname	md�t�@�C���̑��΃p�X
	 */
	public void read(String fname) throws IOException {
		// ���g��o�^
		if (!fname.endsWith(".md")) return;
		String htmlPath = fname.substring(0, fname.length()-3);
		String htmlPath2 = htmlPath.substring(0, htmlPath.length()-5);
		resources.add(rootUrl + htmlPath);
		resources.add(rootUrl + htmlPath2);
		
		// md �t�@�C������͂��Aresource, link ��ǉ����Ă���
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(base + fname), ENCODING));
		
		while (true) {
			String line = br.readLine();
			if (line == null) break;
			
			//
			// <a �����邩�ǂ���������
			//
			if (line.indexOf("< ") > -1)
				System.out.println("warn: < ������܂����Bresource �ǉ�����܂���F" + base + fname + ":" + line);
			int index = line.indexOf("<a ");
			if (index == -1) index = line.indexOf("<A ");
			if (index >= 0) {
				// <a �� <A ������
				// ���� < a �� < A ���͌��������Bregex ���g���ׂ�
				// name �^�O��������
				int ind2 = line.indexOf("name", index);
				if (ind2 == -1) ind2 = line.indexOf("id=", index);
				if (ind2 > -1) {
					ind2 = line.indexOf("\"", ind2);
					int ei = line.indexOf("\"", ind2+1);
					String res = line.substring(ind2+1, ei);
					resources.add(rootUrl + htmlPath + "#" + res);
					resources.add(rootUrl + htmlPath2 + "#" + res);
					if (line.indexOf("<a", ei) > -1)
						System.out.println("warn: 2�ڂ�<a ������܂�:" + base + fname + ":" + line);
					if (line.indexOf("<A", ei) > -1)
						System.out.println("warn: 2�ڂ�<A ������܂�:" + base + fname + ":" + line);
					continue;
				}
				// name �ł͂Ȃ�
				
			}
			index = line.indexOf("href");
			if (index > -1)
				System.out.println("warn: href ������܂��F" + base + fname + ":" + line);
			
			// # �ł͂��܂�s�� id(name) ����
			if (line.startsWith("#")) {
				int i = line.indexOf(" ");
				if (i > -1) {
					resources.add(rootUrl + htmlPath + "#" + line.substring(i + 1, line.length()));
					resources.add(rootUrl + htmlPath2 + "#" + line.substring(i + 1, line.length()));
				}
				continue;
			}
			
			// ]( ��T��
			index = 0;
			while (true) {
				index = line.indexOf("](", index+1);
				if (index == -1) break;
				// [ �ȉ��̓����N��, �������摜������
				int ind2 = line.indexOf(")", index);
				if (ind2 == -1) {
					System.out.println("err : ]( �̌�� ) ��������܂���F" + base + fname + ":" + line);
					continue;
				}
				String link = line.substring(index + 2, ind2);
				int exti = link.lastIndexOf(".");
				if (exti > -1) {
					// �摜�̊g���q�̓����N�łȂ�
					String ext = link.substring(exti);
					if (ext.equalsIgnoreCase(".png") ||
							ext.equalsIgnoreCase(".jpg") ||
							ext.equalsIgnoreCase(".gif") ||
							ext.equalsIgnoreCase(".svg") ) {
						imageResources.add(link);
						continue;
					}
				}
				if (link.startsWith("http")) {
					Link l = new Link();
					l.link = link;
					l.fname = fname;
					l.line = line;
					links.add(l); // �t���p�X
					System.out.println("�O��link? : " + link + " : " + fname); //  + ":"+ line);
				} else if (link.startsWith("/iot/")) {
					Link l = new Link();
					l.link = rootUrl + link.substring(4);
					l.fname = fname;
					l.line = line;
					links.add(l); // ���΃p�X
				} else if (link.startsWith("/guides/")) { // ����̓_���p�^�[��
					Link l = new Link();
					l.link = rootUrl + link;
					l.fname = fname;
					l.line = line;
					links.add(l); // ���΃p�X
				} else if (link.startsWith("localhost:")) {
					// link �ł��O�������N�ł��Ȃ��B�X�L�b�v����B
				} else {
					Link l = new Link();
					l.link = rootUrl + htmlPath + link;
					l.fname = fname;
					l.line = line;
					links.add(l); // ���΃p�X
					if (!link.startsWith("/") && !link.startsWith("#")
						&& !link.startsWith("localhost:"))
						System.out.println("warn : link �� /, #, localhost �ł͂��܂��Ă��܂���B�s����URL���Q�Ƃ��Ă��܂��F" + base + fname + ":" + line);
				}
			}
		}
		br.close();
	}
	
	/**
	 * resources, links �̓��e�Ń`�F�b�N
	 */
	public void check() {
		int missingLinks = 0;
		// ���ׂĂ� link(�t���p�X) �ɑ΂��Aresources �����邱�Ƃ��m�F����B
		for (Link l : links) {
//			System.out.println("link : " + l.link);
			if (l.link.startsWith(rootUrl)) {
				// ���������N
				// check ����
				boolean found = false;
				for (String resource : resources) {
					if (l.link.equalsIgnoreCase(resource)) {
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println("------------------------------------");
					System.out.println("err : �����N�؂� : " + l.link);
					System.out.println("      �t�@�C���� : " + l.fname);
					System.out.println("      �Y���s     : " + l.line);
					missingLinks++;
				}
			} else {
				// �O�������N
				// ���ɏ������Ȃ�
			}
		}
		System.out.println("�����N�؂�:" + missingLinks);
		System.out.println("�����N����Ă���C���[�W�t�@�C��");
		
		for (String resource : imageResources) {
			System.out.println(resource);
		}
	}
	
	public void readDirectory() throws IOException {
		readDirImpl("");
	}
	
	private void readDirImpl(String path) throws IOException {
		File f = new File(basefile, path);
		if (!f.isDirectory()) throw new IOException("" + f + " �̓f�B���N�g���ł͂���܂���");
		String[] lists = f.list();
		for (String list : lists) {
			File file = new File(f, list);
			if (file.isDirectory()) {
				readDirImpl(path + "/" + list);
			} else {
				if (list.endsWith(".md")) {
					System.out.println("info : �t�@�C����ǂ݂܂�: " + path + "/" + list);
					read(path + "/" + list);
				} else {
					System.out.println("warn : �t�@�C���ǂݍ��݂��X�L�b�v���܂�: " + path + "/" + list);
				}
			}
		}
	}
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		MdLinkChecker mc = new MdLinkChecker("https://developer.ntt.com/iot", new File("C:\\Users\\Yusuke\\Documents\\GitHub\\gitlab\\markdown\\src\\documents\\iot").getCanonicalPath());
		
		mc.readDirectory();
		mc.check();
	}
}

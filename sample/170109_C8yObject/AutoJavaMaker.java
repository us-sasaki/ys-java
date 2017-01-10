import java.io.*;
import java.util.*;

public class AutoJavaMaker {
	private static final String PACKAGE = "com.ntt.tc.data.rest";
	private static final Map<String, String> PRIMITIVE_TYPES;
	static {
		PRIMITIVE_TYPES = new HashMap<String, String>();
		PRIMITIVE_TYPES.put("String", "String");
		PRIMITIVE_TYPES.put("Long", "long");
		PRIMITIVE_TYPES.put("Object", "Object");
		PRIMITIVE_TYPES.put("Number", "double");
		PRIMITIVE_TYPES.put("URI", "String");
		PRIMITIVE_TYPES.put("URL", "String");
	}
	
	private String className;
	private String apName;
	private List<String> columns;
	private List<String> fieldNames;
	private List<String> typeNames;
	private List<String> descriptions;
	private List<TreeMap<String, String>> attributes;
	
	public AutoJavaMaker() {
	}
	
	/**
	 * １オブジェクト分の情報を読み込みます。
	 * 情報のフォーマットは、c8y文書のオブジェクト定義を Chrome で表示、
	 * ドラッグしてコピー、テキストファイルにペーストした形式です。
	 *
	 * @return	オブジェクトがあった場合、true、EOF は false
	 */
	public boolean parse(BufferedReader br) throws IOException {
		String line;
		// クラス名を検索
		for (;;) {
			line = br.readLine();
			if (line == null) return false;
			if (!line.equals("")) {
				className = line;
				break;
			}
		}
		if (className.indexOf("\t") > -1)
			throw new IOException("オブジェクト名が見つかりません:"+line);
		
		// 続くアプリケーション名を取得
		apName = br.readLine();
		if (apName == null || apName.equals("") || apName.indexOf("\t")> -1)
			throw new IOException("AP名が見つかりません:"+className);
		
		// 表のタイトルを取得
		for (;;) {
			line = br.readLine();
			if (line == null)
				throw new IOException("表のタイトルが見つからず、EOF検出:"+className);
			if (line.equals("")) continue;
			if (line.indexOf("\t") == -1)
				throw new IOException("表のタイトルがありません:"+line);
			columns = Arrays.asList(line.split("\t"));
			break;
		}
		
		// 表を読み込む
		typeNames	= new ArrayList<String>();
		fieldNames	= new ArrayList<String>();
		descriptions= new ArrayList<String>();
		attributes	= new ArrayList<TreeMap<String, String>>();
		
		for (;;) {
			line = br.readLine();
			if (line == null || line.equals("")) break;
			String[] contents = line.split("\t");
			Iterator<String> cols = columns.iterator();
			TreeMap<String, String> attr = new TreeMap<String, String>();
			
			for (String content : contents) {
				String col = cols.next();
				switch (col.toLowerCase()) {
				case "name":
					fieldNames.add(content);
					break;
				case "type":
					typeNames.add(content);
					break;
				case "description":
					descriptions.add(content);
					break;
				default:
					attr.put(col, content);
				}
			}
			attributes.add(attr);
		}
		return true;
	}
	
	/**
	 * 読み込まれた１オブジェクト分の情報をファイルとして出力します。
	 */
	public void output() throws IOException {
		// ファイル名
		String fname = getJclassName() + ".java";
		
		PrintWriter p = new PrintWriter("output/" + fname);
		
		// package
		p.println("package " + PACKAGE + ";");
		p.println();
		
		// import
		p.println("import com.ntt.tc.data.C8yData;");
		
		HashSet<String> imported = new HashSet<String>();
		for (String typeName : typeNames) {
			if (PRIMITIVE_TYPES.get(typeName) != null) continue;
			if (imported.contains(typeName)) continue;
			p.println("import com.ntt.tc.data." + convJclassStyle(typeName) + ";");
			imported.add(typeName);
		}
		p.println();
		p.println("public class " + getJclassName() + " extends C8yData {");
		Iterator<String> types = typeNames.iterator();
		Iterator<String> descs  = descriptions.iterator();
		Iterator<TreeMap<String, String>> attrs = attributes.iterator();
		
		for (String field : fieldNames) {
			p.println("\t/**");
			String[] dividedDescs = divideTokensByLength(descs.next(), 80-4-3);
			for (String desc : dividedDescs) {
				p.println("\t *" + desc);
			}
			p.println("\t *");
			TreeMap<String, String> attr = attrs.next();
			for (String attrName : attr.keySet()) {
				p.println("\t * " + attrName + " : " + attr.get(attrName));
			}
			p.println("\t */");
			String type = types.next();
			type = PRIMITIVE_TYPES.get(type) == null? type : PRIMITIVE_TYPES.get(type);
			p.println("\tpublic " + convJclassStyle(type) + " " + cutSpace(field) + ";");
			p.println("\t");
		}
		
		p.println("}");
		
		p.close();
	}
	
	/**
	 * Java Class 名の書式を取得します。
	 */
	private String getJclassName() {
		return convJclassStyle(className);
	}
	
	private static String convJclassStyle(String source) {
		String type;
		if (source.contains("URI") && source.contains("template")) {
			type = "String";
		} else if (PRIMITIVE_TYPES.get(source) == null) {
			type = source;
		} else {
			type = PRIMITIVE_TYPES.get(source);
		}
		
		StringBuffer sb = new StringBuffer();
		String[] tokens = type.split(" ");
		for (String token : tokens) {
			if (token.length() == 1) {
				sb.append(token.toUpperCase());
			} else if (Character.isUpperCase(token.charAt(1))) {
				sb.append(token);
			} else {
				sb.append(Character.toUpperCase(token.charAt(0)));
				sb.append(token.substring(1));
			}
		}
		
		return sb.toString();
	}
	
	private static String cutSpace(String source) {
		StringBuilder sb = new StringBuilder();
		String[] tokens = source.split(" ");
		for (String token : tokens) sb.append(token);
		
		return sb.toString();
	}
	
	private static String[] divideTokensByLength(String msg, int length) {
		String[] tokens = msg.split(" ");
		List<String> result = new ArrayList<String>();
		
		String s = tokens[0];
		String line = "";
		
		int i = 1;
		for (;;) {
			if (line.length() + s.length() + 1 > length) {
				result.add(line);
				line = " "+s;
			} else {
				line = line + " " + s;
			}
			if (i >= tokens.length) {
				result.add(line);
				break;
			}
			s = tokens[i++];
		}
		
		return result.toArray(new String[0]);
	}
	
	private static void deleteDirectory(File dir) throws IOException {
		if (dir.isDirectory()) {
			String[] list = dir.list();
			for (String f : list) {
				File file = new File(dir, f);
				if (file.isDirectory()) continue;
				file.delete();
			}
		}
	}
	
	/**
	 * メインプログラムです。
	 */
	public static void main(String[] args) throws Exception {
		deleteDirectory(new File("output"));
		AutoJavaMaker a = new AutoJavaMaker();
		
		BufferedReader br = new BufferedReader(new FileReader("source.txt"));
		
		while (a.parse(br)) {
			a.output();
		}
		
		br.close();
		
	}
}

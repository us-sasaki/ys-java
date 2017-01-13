import java.io.*;
import java.util.*;

public class AutoJavaMaker {
	private static final Map<String, String> PRIMITIVE_TYPES;
	static {
		PRIMITIVE_TYPES = new HashMap<String, String>();
		PRIMITIVE_TYPES.put("String", "String");
		PRIMITIVE_TYPES.put("int", "int");
		PRIMITIVE_TYPES.put("integer", "int");
		PRIMITIVE_TYPES.put("long", "long");
		PRIMITIVE_TYPES.put("boolean", "boolean");
		PRIMITIVE_TYPES.put("double", "double");
		PRIMITIVE_TYPES.put("Int", "int");
		PRIMITIVE_TYPES.put("Integer", "int");
		PRIMITIVE_TYPES.put("Long", "long");
		PRIMITIVE_TYPES.put("Boolean", "boolean");
		PRIMITIVE_TYPES.put("Double", "double");
		PRIMITIVE_TYPES.put("Object", "Object");
		PRIMITIVE_TYPES.put("Number", "double");
		PRIMITIVE_TYPES.put("URI", "String");
		PRIMITIVE_TYPES.put("URL", "String");
		PRIMITIVE_TYPES.put("TimeStamp", "TC_Date");
		PRIMITIVE_TYPES.put("List", "JsonObject");
		PRIMITIVE_TYPES.put("Array", "String[]");
	}
	
	private static Map<String, Iterator<String>> enumName;
	
	private String packageName;
	private String className;
	private String apName;
	private List<String> columns;
	private List<String> fieldNames;
	private List<String> typeNames;
	private List<String> descriptions;
	private List<TreeMap<String, String>> attributes;
	
	private static Map<String, String> packages;
	static {
		packages = new HashMap<String, String>();
		packages.put("C8yData", "com.ntt.tc.data");
		packages.put("TC_Date", "com.ntt.tc.data");
		packages.put("JsonObject", "abdom.data.json");
	}
	
	
	public AutoJavaMaker(String filename) {
		int i = filename.indexOf(".");
		String p = filename;
		if (i > 0) p = filename.substring(0, i);
		i = p.indexOf("-");
		if (i >= 0) p = p.substring(0, i);
		packageName = p;
	}
	
	private static void resetEnumName() {
		enumName = new HashMap<String, Iterator<String>>();
		enumName.put("Request", 
			Arrays.asList("HandshakeRequest",	"SubscriveRequest",
						"UnsubscribeRequest",	"ConnectRequest",
						"DisconnectRequest", "1","2"	).iterator());
		enumName.put("Advice",
			Arrays.asList("HandshakeAdvice",	"ConnectAdvice", "1", "2").iterator());
		enumName.put("Response", 
			Arrays.asList("HandshakeResponse",	"SubscriveResponse",
						"UnsubscribeResponse",	"ConnectResponse",
						"DisconnectResponse","1","2"	).iterator());
	}
	
	
	/**
	 * �P�I�u�W�F�N�g���̏���ǂݍ��݂܂��B
	 * ���̃t�H�[�}�b�g�́Ac8y�����̃I�u�W�F�N�g��`�� Chrome �ŕ\���A
	 * �h���b�O���ăR�s�[�A�e�L�X�g�t�@�C���Ƀy�[�X�g�����`���ł��B
	 *
	 * @return	�I�u�W�F�N�g���������ꍇ�Atrue�AEOF �� false
	 */
	public boolean parse(BufferedReader br) throws IOException {
		String line;
		
		// �N���X��������
	loop:
		for (;;) {
			String mayClassName = null;
			int mayClassCount = 0;
			
			String mayApName = null;
			int mayApCount = 0;
			
			int count = 0;
			// �\�̃^�C�g�����擾
			for (;;) {
				line = br.readLine();
				count++;
				if (line == null) return false;
				if (line.equals("")) continue;
				if (line.startsWith("## ")) { // class ������
					mayClassName = line.substring(3);
					mayClassCount = count;
					continue;
				}
				if (line.startsWith("### ")) { // ap ������
					mayApName = line.substring(4);
					mayApCount = count;
					continue;
				}
				// �\�̌��o
				// �\�� | �� Type �� Desc ���܂ލs�Ƃ��Č��o����B
				if (line.indexOf("|") != -1 &&
						line.toLowerCase().indexOf("type") != -1 &&
						line.toLowerCase().indexOf("desc") != -1) {
					if (mayApCount - mayClassCount > 4) {
						mayClassName = null;
					}
					if (mayClassName == null && mayApName != null) {
						int ind = mayApName.indexOf("[appli");
						if (ind > -1) {
							mayClassName = mayApName.substring(0, ind-1);
							mayClassCount = mayApCount - 2;
						} else {
							mayClassName = mayApName;
							mayClassCount = mayApCount - 2;
						}
					}
					if (mayClassName == null || mayApName == null) continue loop;
					if (count - mayApCount > 4) continue loop;
					if (mayApCount < mayClassCount) continue loop;
					className = mayClassName;
					int index = mayApName.indexOf("[");
					if (className.toLowerCase().contains("api")) {
					} else if (index > -1) {
						className = mayApName.substring(0, index-1);
					} else {
						className = mayApName;
					}
					apName = mayApName;
					columns = Arrays.asList(line.substring(1).replace("||", "| |").split("\\x7c"));
					break;
				}
			}
			break;
		}
		br.readLine();
		
		// �\��ǂݍ���
		typeNames	= new ArrayList<String>();
		fieldNames	= new ArrayList<String>();
		descriptions= new ArrayList<String>();
		attributes	= new ArrayList<TreeMap<String, String>>();
		
		for (;;) {
			line = br.readLine();
			if (line == null || line.equals("") || !line.contains("|")) break;
			String[] contents = line.substring(1).replace("||", "| |").split("\\x7c");
			Iterator<String> cols = columns.iterator();
			TreeMap<String, String> attr = new TreeMap<String, String>();
			
			for (String content : contents) {
				String col;
				try {
					col = cols.next();
				} catch (NoSuchElementException nsee) {
					continue;
				}
				switch (col.toLowerCase()) {
				case "field name":
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
		
		// ����� field ���ɑ΂� type ��ύX
		for (int i = 0; i < typeNames.size(); i++) {
			if (fieldNames.get(i).toLowerCase().contains("time")) {
				typeNames.remove(i);
				typeNames.add(i, "TC_Date");
			}
		}
		
		// Request �ȂǓ��ꖼ�̃I�u�W�F�N�g������������̂𕪂���
		if (enumName.get(className) != null) {
			className = enumName.get(className).next();
		}
		// �p�b�P�[�W��o�^
		packages.put(convJclassStyle(className), "com.ntt.tc.data."+packageName);
		
		return true;
	}
	
	/**
	 * �ǂݍ��܂ꂽ�P�I�u�W�F�N�g���̏����t�@�C���Ƃ��ďo�͂��܂��B
	 */
	public void output() throws IOException {
		// �f�B���N�g�����Ȃ���΍��
		File packdir = new File("output/"+packageName);
		if (!packdir.exists()) packdir.mkdir();
		
		// �t�@�C����
		String fname = packageName + "/" + getJclassName() + ".java";
		
		PrintWriter p = new PrintWriter("output/" + fname);
		
		// package
		p.println("package " + packageName + ";");
		p.println();
		
		// import
		p.println("import com.ntt.tc.data.C8yData;");
		
		HashSet<String> imported = new HashSet<String>();
		for (String typeName : typeNames) {
			typeName = convJclassStyle(typeName);
			// primitive �Ȃ� import ���Ȃ�
			if (PRIMITIVE_TYPES.get(typeName) != null) continue;
			// import �ς��������d import ���Ȃ�
			if (imported.contains(typeName)) continue;
			// ����p�b�P�[�W�Ȃ� import ���Ȃ�
			if (packages.get(typeName) == null) {
				System.out.println(typeName);
				continue;
			}
			if (packages.get(typeName).equals(packageName)) continue;
			
			p.println("import "+ packages.get(typeName) + "." + typeName + ";");
			imported.add(typeName);
		}
		p.println();
		
		// �N���X���R�����g
		p.println("/**");
		p.println(" * " + getJclassName() + " class");
		p.println(" * This source is machine-generated from c8y-markdown docs.");
		p.println(" */");
		
		// �N���X�錾
		p.println("public class " + getJclassName() + " extends C8yData {");
		
		// �ϐ��錾
		Iterator<String> types = typeNames.iterator();
		Iterator<String> descs  = descriptions.iterator();
		Iterator<TreeMap<String, String>> attrs = attributes.iterator();
		
		for (String field : fieldNames) {
			// �ϐ��̃R�����g
			p.println("\t/**");
			String[] dividedDescs = divideTokensByLength(descs.next(), 80-4-3);
			for (String desc : dividedDescs) {
				p.println("\t *" + desc);
			}
			TreeMap<String, String> attr = attrs.next();
			if (attr.keySet().size() > 0) {
				p.println("\t * <pre>");
				for (String attrName : attr.keySet()) {
					p.println("\t * " + attrName + " : " + attr.get(attrName));
				}
				p.println("\t * </pre>");
			}
			p.println("\t */");
			
			// �ϐ��錾�{��
			String type = types.next();
			type = PRIMITIVE_TYPES.get(type) == null? type : PRIMITIVE_TYPES.get(type);
			String f = cutSpace(field);
			
			// �ȉ��́Ac8y �����ł̋L�@�ւ̑Ή�
			// type, field �� * �����邱�Ƃ�����
			// Occurs 1..n �Ŕz���\��
			if (type.contains("*") || type.equalsIgnoreCase("Object")) {
				if (f.contains("*")) {
					p.println("\t//This field has omitted because of type and field = \"*\"");
					p.println("\t");
					continue;
				} else {
					type = "JsonObject";
				}
			}
			
			if (attr.get("Occurs") != null &&
					attr.get("Occurs").contains("n")) {
				type = type + "[]";
			}
			
			p.println("\tpublic " + convJclassStyle(type) + " " + f + ";");
			p.println("\t");
		}
		
		p.println("}");
		
		p.close();
	}
	
	/**
	 * Java Class ���̏������擾���܂��B
	 */
	private String getJclassName() {
		return convJclassStyle(className);
	}
	
	/**
	 * Java Class ���ɕϊ����܂�
	 */
	private String convJclassStyle(String source) {
		String type;
		if (source.startsWith("String:")) {
			type = "String"; // String:MaxLength="32" �̂悤�Ȍ`��������
		} else if (source.contains("URI") && source.contains("emplate")) {
			type = "String";
		} else if (PRIMITIVE_TYPES.get(source) == null) {
			type = source;
		} else {
			return PRIMITIVE_TYPES.get(source);
		}
		
		StringBuffer sb = new StringBuffer();
		String[] tokens = type.split(" ");
		for (String token : tokens) {
			if (token.length() == 0) {
			} else if (token.length() == 1) {
				sb.append(token.toUpperCase());
			} else if (Character.isUpperCase(token.charAt(1))) {
				sb.append(token);
			} else {
				sb.append(Character.toUpperCase(token.charAt(0)));
				sb.append(token.substring(1));
			}
		}
		
		return cutSpace(sb.toString());
	}
	
	private static String cutSpace(String source) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			if (c == ' ') continue;
			//if (Character.isJavaIdentifierStart(c)) sb.append(c);
			if (c >= '0' && c <= '9') sb.append(c);
			else if (c >= 'A' && c <= 'Z') sb.append(c);
			else if (c >= 'a' && c <= 'z') sb.append(c);
			else if (c == '_') sb.append(c);
			else if (c == '*') sb.append(c);
			else if (c == '[') sb.append(c);
			else if (c == ']') sb.append(c);
		}
		
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
				if (file.isDirectory()) deleteDirectory(file);
				else if (file.getPath().endsWith(".java")) file.delete();
				else if (file.getPath().endsWith(".class")) file.delete();
			}
		}
	}
	
	/**
	 * ���C���v���O�����ł��B
	 */
	public static void main(String[] args) throws Exception {
		deleteDirectory(new File("output"));
		
		resetEnumName();
		processDirectory(new File("."), false);
		resetEnumName();
		processDirectory(new File("."), true);
	}
	
	private static void processDirectory(File f, boolean output) throws IOException{
		if (!f.isDirectory())
			throw new IllegalArgumentException(String.valueOf(f) + "�̓f�B���N�g���ł͂���܂���");
		
		String[] fnames = f.list();
		for (String fname : fnames) {
			File f2 = new File(f, fname);
			if (f2.isDirectory()) processDirectory(f2, output);
			else if (fname.endsWith(".md")) {
				System.out.println("processing.. " + fname);
				AutoJavaMaker a = new AutoJavaMaker(fname);
				BufferedReader br = new BufferedReader(new FileReader(f2));
				while (a.parse(br)) {
					if (output) a.output();
				}
				br.close();
			}
		}
	}
}

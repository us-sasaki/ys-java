import abdom.data.json.*;
import abdom.util.CsvReader;

/**
 * CSV�t�@�C����JsonArray �ɕϊ����܂��B
 * �e�J�����̌^�́A�������肳��܂��B
 * long > double > String �̏��ɔ��肳��܂��B
 * �����I�Ɏ����^����Ώۂ𑝂₷���Ƃ�z�肵�A�C���X�^���X���f���Ƃ��Ă��܂��B
 *
 * @version		2017/8/29
 * @author		Yusuke Sasaki
 */
public class Csv2Json {
	
	public JsonArray read(String csv) {
		String[] columnName = null;
		String[] type = null; // ���� type ���m
		
		// pass 1  type ���m����
		// �D�揇�� long > double > String(�Ⴂ)
		int size = 0;
		boolean first = true;
		for (String[] row : CsvReader.rows(csv)) {
			// �^�C�g���s�H
			if (first) {
				// �^�C�g���s
				first = false;
				columnName = row;
				type = new String[row.length];
				continue;
			}
			// �^�C�g���s�ȊO(�ʏ�f�[�^)
			int i = 0;
			for (String column : row) {
				if (type[i] == null) type[i] = "long";
				if (type[i].equals("String")) continue;
				try {
					Double.parseDouble(column);
					type[i] = "double";
					Long.parseLong(column);
					type[i] = "long";
				} catch (NumberFormatException nfe) {
					type[i] = "String";
				}
				i++;
			}
			size++;
		}
		
		// �f�[�^��ǂݍ���
		JsonArray result = new JsonArray();
		first = true;
		for (String[] row : CsvReader.rows(csv)) {
			// �^�C�g���s�̓X�L�b�v
			if (first) {
				first = false;
				continue;
			}
			// �ʏ�f�[�^�̍s
			JsonObject jo = new JsonObject();
			int i = 0;
			for (String column : row) {
				JsonType jt;
				if (type[i].equals("long")) {
					jt = new JsonValue(Long.parseLong(column));
				} else if (type[i].equals("double")) {
					jt = new JsonValue(Double.parseDouble(column));
				} else {
					jt = new JsonValue(column);
				}
				jo.put(columnName[i], jt);
				i++;
			}
			result.push(jo);
		}
		return result;
	}
	
/*---------------
 * main for test
 */
	/**
	 *
	 */
	public static void main(String[] args) {
		Csv2Json c2j = new Csv2Json();
		JsonArray ja = c2j.read("CarData.csv");
		System.out.println(ja.toString("  "));
	}
}

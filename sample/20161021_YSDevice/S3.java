import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 3: �f�o�C�X��o�^����
 * 
 * �V�����f�o�C�X���쐬���ꂽ��AStep 1 �ɋL�ڂ������莯�ʎq�ɂ���Ċ֘A�t��
 * ���܂��B����ɂ��A���̓d���I���̌�� Cumulocity �ł̎��g�̃f�o�C�X��
 * �������܂��B
 * ��̗�ł̓n�[�h�E�F�A�V���A���ԍ��Ɋ֘A����f�o�C�X"2480300"���V��������
 * �t�����܂��B
 */
public class S3 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "device_ysdev000001", "Al00kgOFPv");
		JsonObject jo = new JsonObject();
		jo.add("type", "c8y_Serial")
			.add("externalId","VAIO-Serial-5102173");
			
		r.post("/identity/globalIds/12244450/externalIds", "externalId", jo.toString());
	}
}

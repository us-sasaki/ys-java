import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 4: �C���x���g���̃f�o�C�X���X�V����
 * 
 * ���Step1�Ńf�o�C�X�����łɓo�^����Ă���A�ƕԋp���ꂽ�ꍇ�A�C���x���g����
 * �f�o�C�X�̕\�������݂̎��f�o�C�X�̏�Ԃɑ΂��čŐV�ł��邱�Ƃ��m�F����
 * �K�v������܂��B
 * ���̂��߁A�C���x���g���̃f�o�C�X�� URL �� PUT���N�G�X�g�����M����܂��B
 * ���ۂɕύX�̂������t���O�����g�݂̂����M����邱�Ƃɒ��ӂ��Ă��������B
 * (�t���O�����g�̂���Ȃ���́ACumulocity �̃h���C�����f�����Q�Ƃ�������)
 * �Ⴆ�΁A�f�o�C�X�̃n�[�h�E�F�A���͒ʏ�ύX����܂��񂪁A�\�t�g�E�F�A�C��
 * �X�g�[�����͕ύX�����\��������܂��B���������āA�C���x���g���̃\�t�g
 * �E�F�A�����f�o�C�X���u�[�g��ɍŐV��Ԃɍ��킹�邱�Ƃ����킩�肢��������
 * �ł��傤�B
 *
 * <code></code>
 *
 * �G�[�W�F���g����A�f�o�C�X�̖��O���X�V���Ȃ��ł��������I �G�[�W�F���g��
 * �f�o�C�X�ɑ΂��f�t�H���g���𐶐����A�C���x���g���Ŏ��ʂł���悤�ɂ��܂��B
 * �������Ȃ���A���[�U�͎��Y�Ǘ��̏��Ŗ��O��ҏW������X�V������ł���
 * �悤�ɂ��ׂ��ł��B
 *
 * Response : 406
 * Message  : Not Acceptable
 * �ƂȂ��Ă��܂����B�̂ŁAAccept �w�b�_��t�^ -> ���܂�������
 */
public class S4 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "device_ysdev000001", "Al00kgOFPv");
		JsonType.setIndent(false);
		JsonObject jo = new JsonObject();
		jo.add("c8y_Software", new JsonObject().add("virtual-driver", "vd-1.0"));
		JsonType.setIndent(true);
		r.put("/inventory/managedObjects/12244450", "managedObject", jo.toString());
	}
}

/* ����
{"assetParents":{"references":[],"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/assetParents"},"childAssets":{"references":[],"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/childAssets"},"childDevices":{"references":[{"managedObject":{"id":"9941768","self":"http://nttcom.cumulocity.com/inventory/managedObjects/9941768"},"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/childDevices/9941768"}],"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/childDevices"},"creationTime":"2016-10-24T08:51:18.349+02:00","deviceParents":{"references":[],"self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450/deviceParents"},"id":"12244450","lastUpdated":"2016-11-02T08:40:03.452+01:00","name":"VAIO YS's 5102173","owner":"device_ysdev000001","self":"http://nttcom.cumulocity.com/inventory/managedObjects/12244450","type":"YSAP","c8y_IsDevice":{},"c8y_Notes":"REST\u306b\u3088\u308a\u30c7\u30d0\u30a4\u30b9\u30af\u30ec\u30c7\u30f3\u30b7\u30e3\u30eb\u3067\u767b\u9332\u3057\u305f\u30c7\u30d0\u30a4\u30b9\u3002\nYS First Device \u3092\u5b50\u30c7\u30d0\u30a4\u30b9\u3068\u3057\u3066\u767b\u9332\u3057\u3066\u3044\u308b\u3002","c8y_Hardware":{"serialNumber":"5102173","CPU":"Core i5"},"c8y_Configuration":{"config":"on the YS Desk"},"c8y_Software":{"virtual-driver":"vd-1.0"}}
*/

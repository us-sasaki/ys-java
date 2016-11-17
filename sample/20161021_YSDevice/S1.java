import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 1: �f�o�C�X���o�^����Ă��邩�m�F����
 * 
 * �f�o�C�X�̃��j�[�NID�́A�C���x���g���ւ̃f�o�C�X�̓o�^�ɑ΂��Ă��g�p����܂��B
 * ���̓o�^�� Identity API ���g�p���Ď��s�ł��܂��BIdentity API �ł́A�Ǘ��I�u
 * �W�F�N�g�� type �ŋ�ʂ���镡����ID�Ɋ֘A�����邱�Ƃ��ł��܂��B
 * type �Ƃ��ėႦ�΁A���i�V���A���ԍ��ɑ΂��� "c8y_Serial" ��AMAC�A�h���X��
 * �΂��� "c8y_MAC" ��AIMEI�ɑ΂��� "c8y_IMEI" ������܂��B
 * �f�o�C�X���o�^����Ă��邱�Ƃ��m�F���邽�߁Aidentity API �� GET ���N�G�X�g��
 * �f�o�C�XID�₻��type���g���čs���Ă��������B
 * ���̗�́ARaspberry Pi �̐��i�V���A���ԍ��� 0000000017b79d5 �ł��邱�Ƃ��m�F
 * ���܂��B
 *
 * <code></code>
 *
 * �ˁ@���ʂ� Response : 404
 *            Message  : Not Found
 *
 * MAC�A�h���X�̓O���[�o�����j�[�N�ɕt�^�����̂ɑ΂��A���i�V���A���ԍ��́A
 * �قȂ鐻�i�Ԃŏd�����邩���m��Ȃ����Ƃɒ��ӂ��ĉ������B
 * ���������āA��̗�ł́A�V���A���ԍ��ɐړ��� raspi- ��t���Ă��܂��B
 * ���̃P�[�X�ł́A�f�o�C�X�͊��ɓo�^����Ă���X�e�[�^�X�R�[�h 200 ��
 * �ԋp����Ă��܂��B���X�|���X���ŁA�C���x���g���̃f�o�C�X�ւ�URL��
 * "managedObject.self" �ŕԋp����Ă��܂��B����URL�͌�Ńf�o�C�X�ɓ���������
 * ���߂ɗ��p�ł��܂��B
 * �f�o�C�X���܂��o�^����Ă��Ȃ��ꍇ�A404 Not Found �X�e�[�^�X�R�[�h��
 * �G���[���b�Z�[�W���ԋp����܂��B
 *
 * <code></code>
 *
 */
public class S1 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "device_ysdev000001", "Al00kgOFPv");
		r.get("/identity/externalIds/c8y_Serial/VAIO-5102173", "externalId");
	}
}

import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 0: �f�o�C�X�N���f���V������v������
 *
 * Cumulocity �ɑ΂��邷�ׂẴ��N�G�X�g�ɂ͔F�؂��K�v�Ȃ��߁A�f�o�C�X�����
 * �v������͂�F�؂��K�v�ł��B�f�o�C�X�Ɍʂ̔F�؏���t�^�������ꍇ�A
 * �V�����F�؏��������Ő�������Adevice credentials API �𗘗p�ł��܂��B
 * ������s���ɂ́A�ŏ��̋N�����Ƀf�o�C�X�̔F�؏���API�Ń��N�G�X�g���A�ȍ~
 * �̃��N�G�X�g�̂��߂Ƀ��[�J���Ńf�o�C�X�Ɋi�[���ĉ������B
 * �����͎��̂悤�ɐi�߂܂��F
 * �ECumulocity�́A�e�f�o�C�X�����炩�̌`���̃��j�[�N��ID�������Ă���Ɖ���
 *   ���Ă��܂��B�悢�f�o�C�XID�́A�l�b�g���[�N�J�[�h��MAC�A�h���X�A���o�C��
 *   �f�o�C�X��IMEI�A���i�V���A���ԍ��̂悤�Ȃ��̂ł��B
 * �E�V�����f�o�C�X���g���n�߂�Ƃ��A���̃��j�[�NID�� Cumulocity ��
 *   "Device registration" �ɓ���Ă���f�o�C�X���J�n���Ă��������B
 * �E�f�o�C�X�� Cumulocity �ɐڑ����A���j�[�NID�𑱂��đ��M���܂��B
 *   ���̖ړI�̂��߁ACumulocity �ɂ͌Œ�I�ȃz�X�g������܂��B���̃z�X�g��
 *   support@cumulocity.com �ɕ����Ă��������B
 * �E"Device Registration" �̒��ŁA�f�o�C�X����̐ڑ������Ȃ��͏��F�ł��܂��B
 *   ���̏ꍇ�A Cumulocity �̓f�o�C�X�ɐ��������F�؏��𑗐M���܂��B
 *
 * �f�o�C�X����݂��ꍇ�A����͒P��� REST ���N�G�X�g�ł��B
 *
 * <code>
 * </code>
 *
 * �f�o�C�X�͂��̃��N�G�X�g���J��Ԃ����s���܂��B���[�U�����̃f�o�C�X��
 * �o�^�A���F���Ȃ������́A���̃��N�G�X�g��"404 Not Found." ��ԋp���܂��B
 * �f�o�C�X�����F���ꂽ�̂��́A���̂悤�ȃ��X�|���X���ԋp����܂��B
 *
 * <code>
 * </code>
 *
 * ����Ńf�o�C�X��Cumulocity�ɑ΂��AtenantID, username, password ���g�p����
 * �ڑ����邱�Ƃ��ł��܂��B
 *
 * �ˁ@���� 403 forbidden ���Ԃ��ꂽ�Bnttcom.cumulo... �� management.cumulo..
 *     �����ŁB
 * �ˁ@Authorization ��t�����ɑ���ƁA401 Unauthorized ���ԋp�B
 *
 * agent �̐^���������Ƃ���A�ȉ��̃R�[�h���ԋp���ꂽ
 * <pre>
 * {
 *   "id":"5102173",
 *   "password":"WqHMSceCzd",
 *   "self":"http://management.cumulocity.com/devicecontrol/deviceCredentials/51 * 02173",
 *   "tenantId":"nttcom",
 *   "username":"device_5102173"
 * }
 * </pre>
 * �܂��A�ȉ��͕ʂ̃f�o�C�XID��o�^��������
 * <pre>
 * {
 *   "id":"ysdev000001",
 *   "password":"Al00kgOFPv",
 *   "self":"http://management.cumulocity.com/devicecontrol/deviceCredentials/ys * dev000001",
 *   "tenantId":"nttcom",
 *   "username":"device_ysdev000001"
 * }
 * </pre>
 */
public class S0 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "management", "devicebootstrap", "Fhdt1bb1f"); //"us.sasaki@ntt.com", "nttcomsasaki3");
		JsonObject jo = new JsonObject().add("id", "ysdev000010");
		jo.add("password",(String)null);
		jo.add("tenantId",(String)null);
		jo.add("username",(String)null);
		r.post("/devicecontrol/deviceCredentials", "deviceCredentials", jo.toString());
	}
}

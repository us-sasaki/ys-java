import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 2: �C���x���g���Ƀf�o�C�X���쐬����
 * 
 * ������L Step1 �Ńf�o�C�X��\������Ǘ��I�u�W�F�N�g���Ȃ��A�Ǝ����ꂽ��A
 * Cumulocity ���ɊǗ��I�u�W�F�N�g���쐬���ĉ������B�Ǘ��I�u�W�F�N�g��
 * �f�o�C�X�̃C���X�^���X�f�[�^�ƃ��^�f�[�^�̗�����\���܂��B
 * �C���X�^���X�f�[�^�́A�V���A���ԍ��A�f�o�C�X�ݒ���̂悤�ȃn�[�h
 * �E�F�A�A�\�t�g�E�F�A�����܂݂܂��B���^�f�[�^�́A�T�|�[�g����鑀��
 * �̂悤�ȁA�f�o�C�X�̋@�\��\���܂��B
 * �Ǘ��I�u�W�F�N�g���쐬����ɂ́A�C���x���g��API�̊Ǘ��I�u�W�F�N�g�R���N�V����
 * ��POST���N�G�X�g�𔭍s���ĉ������B
 * ���̗�́@Linux�G�[�W�F���g ���g�p�����ꍇ�� RaspberryPi �̍쐬�ł��B
 * 
 *<code></code>
 *
 * ��̗�́A�f�o�C�X�̃��^�f�[�^���ڂ��܂�ł��܂��B
 * �E"c8y_IsDevice" �́ACumulocity �̃f�o�C�X�Ǘ��ŊǗ��ł��邱�Ƃ������܂�
 * �Ecom_cumulocity_model_Agent" �́ACumulocity �G�[�W�F���g�Ŏ��s���Ă���
 *   �f�o�C�X�������Ă��܂��B
 * �E"c8y_SupportedOperations" �́A���̃f�o�C�X���ċN����ݒ肪�ł��邱�Ƃ�
 *   �q�ׂĂ��܂��B����ɁA�\�t�g�E�F�A�̎��s��t�@�[���E�F�A�̃A�b�v�f�[�g
 *   ���ł��܂��B
 * ����Ȃ���́A�f�o�C�X�Ǘ����C�u�����@���Q�Ɖ������B
 * �f�o�C�X�����܂������ƁA�X�e�[�^�X�R�[�h201���ԋp����܂��B
 * �͂��߂̃��N�G�X�g�ɗ�̂悤�� Accept �w�b�_���܂܂��ꍇ�A�쐬���ꂽ
 * �I�u�W�F�N�g�S�̂� ID �� �����̃I�u�W�F�N�g�\���ւ� URL ���Ƃ��ɕԋp����܂��B
 */
public class S2 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "device_ysdev000001", "Al00kgOFPv");
		JsonObject jo = new JsonObject();
		jo.add("name", "VAIO YS's 5102173")
			.add("type", "YSAP")
			.add("c8y_IsDevice", new JsonObject())
			.add("c8y_Hardware",
						new JsonObject().add("serialNumber", "5102173")
										.add("CPU", "Core i5") )
			.add("c8y_Software",
						new JsonObject().add("virtual-driver", "vd-0.9") )
			.add("c8y_Configuration",
						new JsonObject().add("config", "on the YS Desk") );
			
		r.post("/inventory/managedObjects", "managedObject", jo.toString());
	}
}


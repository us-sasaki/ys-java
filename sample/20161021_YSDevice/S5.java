import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

/**
 * Step 5: �q�f�o�C�X�𔭌����A�C���x���g���ɐ���/�X�V����
 * 
 * �Z���T�l�b�g���[�N�͕��G�Ȃ̂ŁA�f�o�C�X�͎����ɕR�Â��q�f�o�C�X�������Ă���
 * ���Ƃ�����܂��B�悢��́A�z�[���I�[�g���[�V�����ł��B
 * �ƒ�̗l�X�ȕ����ɁA�����̈قȂ�Z���T�A�R���g���[����������z�[���I�[�g
 * ���[�V�����Q�[�g�E�F�C������ł��傤�B�q�f�o�C�X�̓o�^�̊�{�́A���C���f�o
 * �C�X�̓o�^�Ɏ��Ă��܂��B�q�f�o�C�X�͒ʏ�A�G�[�W�F���g�C���X�^���X��
 * ���s���܂���B(���������āA"com_cumulocity_model_Agent" �t���O�����g��
 * �폜����Ă��܂�)
 * �f�o�C�X���q���Ƀ����N����ɂ́A�I�u�W�F�N�g�𐶐�����ۂɕԋp�����q�f�o
 * �C�X�� URL �� POST ���N�G�X�g�𑗐M���Ă��������B(��Q��)
 * 
 * �Ⴆ�΁AURL "https://.../inventory/managedObjects/2543801" �����q�f�o�C�X
 * ���o�^���ꂽ�Ƃ��܂��B���̃f�o�C�X�ɐe�������N����ɂ́A���𔭍s���Ă��������B
 *
 * <code></code>
 * 
 * �Ō�ɁA�f�o�C�X�⃊�t�@�����X�́A���������� URL �� DELETE ���N�G�X�g��
 * ���s���邱�Ƃō폜�ł��܂��B�Ⴆ�΁A������������e�f�o�C�X����q�f�o�C�X��
 * �̃��t�@�����X�́A���𔭍s���邱�Ƃō폜�ł��܂��B
 * 
 */
public class S5 {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "us.sasaki@ntt.com", "nttcomsasaki3");
		JsonType.setIndent(false);
		JsonObject jo = new JsonObject();
		jo.add("managedObject", new JsonObject().add("self", "https://nttcom.cumulocity.com/inventory/managedObjects/9941768"));
		r.post("/inventory/managedObjects/12244450/childDevices", "managedObjectReference", jo.toString());
	}
}

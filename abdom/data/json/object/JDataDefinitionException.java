package abdom.data.json.object;

/**
 * JData �ɂ����āAfill ���\�b�h�ȂǂŎ����I�� JData �I�u�W�F�N�g�𐶐�
 * ����ہA�p�������N���X��`�ɖ�肪����ꍇ�ɃX���[������O�ł��B
 * <pre>
 * ��P�D�f�t�H���g�R���X�g���N�^����`����Ă��Ȃ����ŃC���X�^���X��
 * �Ɏ��s�����Ƃ�
 * ��Q�D�t�B�[���h���A�A�N�Z�X�s�\�ł��邩final�ł���ꍇ
 * </pre>
 *
 * @version		December 10, 2016
 * @author		Yusuke Sasaki
 */
public class JDataDefinitionException extends RuntimeException {
	public JDataDefinitionException() {
		super();
	}
	public JDataDefinitionException(String msg) {
		super(msg);
	}
}

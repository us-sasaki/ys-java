package abdom.data.json.object;

/**
 * JData �ɂ����āA�w�肳�ꂽ JsonType �t�B�[���h�� JData �C���X�^���X
 * �t�B�[���h�Ɍ^�̕s��v���������ꍇ�ɃX���[������O�ł��B
 */
public class IllegalFieldTypeException extends RuntimeException {
	public IllegalFieldTypeException() {
		super();
	}
	public IllegalFieldTypeException(String msg) {
		super(msg);
	}
}

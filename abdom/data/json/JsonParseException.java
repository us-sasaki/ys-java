package abdom.data.json;

/**
 * JsonType.parse �ɂ����āA�w�肳�ꂽ������̃t�H�[�}�b�g��
 * JSON ���@�����Ă����ꍇ�ɃX���[������O�ł��B
 */
public class JsonParseException extends RuntimeException {
	public JsonParseException() {
		super();
	}
	public JsonParseException(String msg) {
		super(msg);
	}
}

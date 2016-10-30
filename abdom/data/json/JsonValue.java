package abdom.data.json;

/**
 * Json�`���ɂ�����v���~�e�B�u�^(������,���l)��\���܂�
 */
public class JsonValue extends JsonType {
	public String value;
	public String quote = "";
	
/*-------------
 * constructor
 */
	/**
	 * \ �ŃG�X�P�[�v���ׂ����������̂܂ܕۑ����AtoString �ŕԂ��Ă��܂��B
	 * �̂ŁA\", \', \\ �� toString() ���ɃG�X�P�[�v����悤�ύX����
	 */
	public JsonValue(String value) {
		if (value == null) this.value = "null";
		else {
			this.value = value;
			quote = "\"";
		}
	}
	
	public JsonValue(byte value) {	this.value = String.valueOf(value);	}
	public JsonValue(char value) {
		this.value = String.valueOf(value);
		quote = "\""; // string ����
	}
	public JsonValue(short value) { this.value = String.valueOf(value); }
	public JsonValue(int  value) {	this.value = String.valueOf(value); }
	public JsonValue(long value) {	this.value = String.valueOf(value); }
	public JsonValue(float value) {	this.value = String.valueOf(value); }
	public JsonValue(double value) {	this.value = String.valueOf(value); }
	public JsonValue(boolean value) {	this.value = value?"true":"false"; }
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		return toString("", false);
	}
	@Override
	protected String toString(String indent, boolean objElement) {
		return indent+quote+value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'")+quote; // "string" / number �̌`��
	}
}

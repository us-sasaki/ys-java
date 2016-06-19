/**
 * Json�`���ɂ�����v���~�e�B�u�^(������,���l)��\���܂�
 */
public class JsonValue extends JsonType {
	public String value;
	public boolean isString = false;
	
/*-------------
 * constructor
 */
	/**
	 * \ �ŃG�X�P�[�v���ׂ����������̂܂ܕۑ����AtoString �ŕԂ��Ă��܂��B
	 * �̂ŁA\", \', \\ �� toString() ���ɃG�X�P�[�v����悤�ύX����
	 */
	public JsonValue(String value) {
		this.value = value;
		this.isString = true;
	}
	
	public JsonValue(byte value) {	this.value = String.valueOf(value);	}
	public JsonValue(char value) {	this.value = String.valueOf(value); }
	public JsonValue(int  value) {	this.value = String.valueOf(value); }
	public JsonValue(long value) {	this.value = String.valueOf(value); }
	public JsonValue(float value) {	this.value = String.valueOf(value); }
	public JsonValue(double value) {	this.value = String.valueOf(value); }
	
/*-----------
 * overrides
 */
	public String toString() {
		return toString("");
	}
	public String toString(String indent) {
		String quote = "";
		if (this.isString) quote = "\"";
		return indent+quote+value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'")+quote; // "value" �̌`��
	}
}

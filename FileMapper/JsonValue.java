public class JsonValue extends JsonType {
	public String value;
	
/*-------------
 * constructor
 */
	/**
	 * \ �ŃG�X�P�[�v���ׂ����������̂܂ܕۑ����AtoString �ŕԂ��Ă��܂��B
	 * \", \', \\ �� toString() ���ɃG�X�P�[�v����悤�ύX����
	 */
	public JsonValue(String value) {
		this.value = value;
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
		return "\""+value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'")+"\""; // "value" �̌`��
	}
}

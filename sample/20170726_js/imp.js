/*
 * CEP �ŕ��������邱�Ƃ�z�肵���X�N���v�g
 * FCntUp, DevAddr �� CEP���擾�ł���K�v�����邱�Ƃɒ���
 */

/**
 * �w�L�T�����񂩂琔�l�̔z���ԋp����
 */
function parse(hexstring) {
	var result = [];
	for (var i = 0; i < hexstring.length(); i+=2) {
		var s = hexstring.substring(i, i+2);
		result.push(parseInt(s, 16));
	}
	return result;
}

/**
 * byte�z����w�L�T������ɕϊ����܂�
 */
function toString(jbyte) {
	var result = "";
	for (var i = 0; i < jbyte.length; i++) {
		var n = jbyte[i];
		if (n < 16) result = result + "0";
		result = result + jbyte[i].toString(16);
	}
	return result;
}

/**
 * devaddr, fcntup ����l��ԋp����
 *
 * devAddr �̓��g���G���f�B�A���ł���K�v������
 */
function decrypt(devAddr, fcntup, enc) {
	var start = "01" + "00000000";
	var dir = "00"; // uplink
	var i = "0001";
	var SecretKeySpec = Java.type("javax.crypto.spec.SecretKeySpec");
	var Cipher = Java.type("javax.crypto.Cipher");
	var IvParameterSpec = Java.type("javax.crypto.spec.IvParameterSpec");
	
	var keyByteArray = parse("2B7E151628AED2A6ABF7158809CF4F3C");
	var jkeyByteArray = Java.to(keyByteArray, "byte[]");
	var ivByteArray = parse(start + dir + devAddr + fcntup + i);
	var jivByteArray = Java.to(ivByteArray, "byte[]");
	
	var key = new SecretKeySpec(jkeyByteArray, "AES");
	var iv = new IvParameterSpec(jivByteArray);
	var cipher = Cipher.getInstance("AES/CTR/NOPADDING");
	cipher.init(Cipher.DECRYPT_MODE, key, iv);
	
	var jenc = Java.to(parse(enc), "byte[]");
	var result = cipher.doFinal(jenc);
	
	return result;
}

var System = Java.type("java.lang.System");
for (var i = 0; i < 5; i++) {
	var t0 = System.currentTimeMillis();
	print(toString(decrypt("930D7900", "0b000000", "90ada2ccf937393e229e")));
	
	print(System.currentTimeMillis() - t0);
}


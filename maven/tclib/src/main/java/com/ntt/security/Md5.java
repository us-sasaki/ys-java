package com.ntt.security;

import java.security.*;

/**
 * MessageDigest のうち、よく使う部分を抜き出したクラス.
 */
public class Md5 {
	protected MessageDigest md;
	
/*-------------
 * Constructor
 */
	public Md5() {
		try {
			md = MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e) {
			throw new RuntimeException("environment error !  MD5 hash was not found at Sha class.");
		}
	}
	
/*----------------------------------------
 * instance methods(MessageDigest に委譲)
 */
	public void reset() {
		md.reset();
	}
	
	public void update(byte d) {
		md.update(d);
	}
	
	public void update(byte[] d) {
		md.update(d);
	}
	
	public void update(byte[] d, int offset, int len) {
		md.update(d, offset, len);
	}
	
	public byte[] digest(byte[] d) {
		return md.digest(d);
	}
	
	public byte[] digest() {
		return md.digest();
	}

/*--------------
 * class method
 */
	/**
	 * インスタンスを作らずにハッシュ計算する. 便利な関数.
	 *
	 * @param		d		ハッシュ計算対象のデータ
	 * @return		ハッシュ値
	 */
	public static byte[] hash(byte[] d) {
		Md5 instance = new Md5();
		return instance.digest(d);
	}
}

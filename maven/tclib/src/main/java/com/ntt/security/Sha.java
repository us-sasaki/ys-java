package com.ntt.security;

import java.security.*;

/**
 * MessageDigest のうち、よく使う部分を抜き出したクラス.
 */
public class Sha {
	protected MessageDigest md;
	
/*-------------
 * Constructor
 */
	public Sha() {
		try {
			md = MessageDigest.getInstance("SHA");
		}
		catch(NoSuchAlgorithmException e) {
			throw new RuntimeException("environment error !  SHA hash was not found at Sha class.");
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
	 */
	public static byte[] hash(byte[] d) {
		Sha instance = new Sha();
		return instance.digest(d);
	}
}

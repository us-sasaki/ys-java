package com.ntt.tc.util;

import java.io.*;

/**
 * Base 64 エンコード、デコードに関連する機能を提供する、便利クラスです。
 * エンコード、デコードのロジックは、com.ntt.io.Base64EncodeStream,
 * com.ntt.io.Base64DecodeStream を使用します。
 */
public class Base64 {
	/**
	 * base64 エンコーディングを行ないます。
	 * 改行は、デフォルトの 20 blocks ( 40 letters ) ごとに挿入します。
	 */
	public static byte[] encode(byte[] target) {
		try {
			ByteArrayOutputStream	baos	= new ByteArrayOutputStream();
			Base64EncodeStream		enc		= new Base64EncodeStream(baos);
			
			enc.write(target);
			enc.close();
			
			return baos.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("base64エンコード中の例外："+e);
		}
	}
	
	public static String encodeToString(byte[] target) {
		return new String(encode(target));
	}
	
	/**
	 * base64 デコーディングを行ないます。
	 */
	public static byte[] decode(byte[] target) {
		try {
			ByteArrayInputStream	bais	= new ByteArrayInputStream(target);
			Base64DecodeStream		dec		= new Base64DecodeStream(bais);
			
			ByteArrayOutputStream	baos	= new ByteArrayOutputStream();
			
			while (true) {
				int c = dec.read();
				if (c == -1) break;
				baos.write(c);
			}
			dec.close();
			baos.close();
			
			return baos.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("base64デコード中の例外："+e);
		}
	}
	
	public static byte[] decodeFromString(String target) {
		return decode(target.getBytes());
	}
}

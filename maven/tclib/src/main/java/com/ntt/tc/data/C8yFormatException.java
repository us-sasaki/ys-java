package com.ntt.tc.data;

/**
 * C8y で決めている日付やオブジェクト構造等のフォーマット異常が
 * あった場合にスローされる例外です。
 *
 */
public class C8yFormatException extends IllegalArgumentException {
	public C8yFormatException() {
		super();
	}
	public C8yFormatException(String msg) {
		super(msg);
	}
}

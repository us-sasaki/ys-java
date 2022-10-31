package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

/**
 * Value は Measurement の中で頻繁に利用される、数値と単位の
 * 組み合わせ。
 * docs では明示的な定義がないが、頻出のためクラス化。
 */
public class Value extends C8yData {
	public double value;
	public String unit;
	
	public Value() {
		super();
	}
	public Value(double value, String unit) {
		super();
		this.value	= value;
		this.unit	= unit;
	}
}

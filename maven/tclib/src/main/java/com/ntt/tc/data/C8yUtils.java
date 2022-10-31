package com.ntt.tc.data;

/**
 * C8y の各種データのチェックメソッドを提供します。
 * このメソッドでチェックすることで、クラス間のチェック基準、エラー時の
 * メッセージを統一化することを想定しています。
 */
public final class C8yUtils {
	public static String[] ICONS = {"th"};
	/**
	 * インスタンス化させないための定義
	 */
	private C8yUtils() {
	}
	
	/**
	 * #ffba04 のような RGB 色文字列であることを判定します。
	 * フォーマット異常の場合、IllegalArgumentException がスローされます。
	 *
	 * @param		rgbColor	色文字列
	 * @throws		java.lang.IllegalArgumentException	フォーマット異常
	 */
	public static void checkRGBColor(String rgbColor) {
		if (!rgbColor.startsWith("#"))
			throw new IllegalArgumentException("color format #xxxxxx: "
								+rgbColor);
		if (rgbColor.length() != 7)
			throw new IllegalArgumentException("color format #xxxxxx: "
								+rgbColor);
		try {
			Integer.parseInt(rgbColor.substring(1), 16);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("color format #xxxxxx: "
								+rgbColor);
		}
	}
	
	/**
	 * measurement の fragment 文字列であることを確認します。
	 * 実際には、.(ピリオド)が含まれていないことのチェックを行っています。
	 *
	 * @param		fragment		measurement fragment 文字列
	 * @throws		java.lang.IllegalArgumentException	フォーマット異常
	 */
	public static void checkMeasurementFragment(String fragment) {
		if (fragment.contains("."))
			throw new IllegalArgumentException("fragment に . は含められません");
	}
	
	/**
	 * measurement の series 文字列であることを確認します。
	 * 実際には、.(ピリオド)が含まれていないことのチェックを行っています。
	 *
	 * @param		series		measurement series 文字列
	 * @throws		java.lang.IllegalArgumentException	フォーマット異常
	 */
	public static void checkMeasurementSeries(String series) {
		if (series.contains("."))
			throw new IllegalArgumentException("series に . は含められません");
	}
	
	public static void checkIcon(String icon) {
	}
}

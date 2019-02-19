package com.ntt.tc.data.services;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ManagedObject;

/**
 * Kpi class.
 * データポイントを表す Managed Object。
 * KPI は通常の inventory POST で登録されるため、API.createManagedObject
 * を利用できるよう ManagedObject の子クラスとしています。
 *
 * このクラスは KPI のファクトリメソッドを提供します。
 *
 * @version		February 18, 2019
 * @author		Yusuke Sasaki
 */
public final class Kpi {
	private Kpi() {
	}
	
/*-----------------
 * Factory methods
 */
	/**
	 * 指定されたパラメータで Kpi を生成します。
	 *
	 * @param		label		データポイントのラベル
	 * @param		fragment	データポイントの fragment
	 * @param		series		データポイントの series
	 * @param		unit		データポイントの 単位
	 * @param		color		#55FB2C のような形式の色
	 * @param		target		Target value (from c8y-docs)
	 * @param		min			y 軸で表示される最小値
	 * @param		max			y 軸で表示される最大値
	 * @param		yellowRangeMin	MINOR alarm の対象となる黄色レンジの最小値
	 * @param		yellowRangeMin	MINOR alarm の対象となる黄色レンジの最大値
	 * @param		redRangeMin		CRITICAL alarm となる赤色レンジの最小値
	 * @param		redRangeMax		CRITICAL alarm となる赤色レンジの最大値
	 */
	public static ManagedObject of(String label,
									String fragment, String series,
									String unit,
									String color,
									double target,
									double min, double max,
									double yellowRangeMin,
									double yellowRangeMax,
									double redRangeMin,
									double redRangeMax) {
		if (!color.startsWith("#"))
			throw new IllegalArgumentException("color format #xxxxxx: "+color);
		if (color.length() != 7)
			throw new IllegalArgumentException("color format #xxxxxx: "+color);
		try {
			Integer.parseInt(color.substring(1), 16);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("color format #xxxxxx: "+color);
		}
		if (fragment.contains("."))
			throw new IllegalArgumentException("fragment に . は含められません");
		if (series.contains("."))
			throw new IllegalArgumentException("series に . は含められません");
		
		ManagedObject result = new ManagedObject();
		result.set("c8y_Global", new JsonObject());
		result.set("c8y_Kpi.color", color);
		result.set("c8y_Kpi.fragment", fragment);
		result.set("c8y_Kpi.label", label);
		result.set("c8y_Kpi.max", max);
		result.set("c8y_Kpi.min", min);
		result.set("c8y_Kpi.redRangeMax", redRangeMax);
		result.set("c8y_Kpi.redRangeMin", redRangeMin);
		result.set("c8y_Kpi.series", series);
		result.set("c8y_Kpi.target", target);
		result.set("c8y_Kpi.unit", unit);
		result.set("c8y_Kpi.yellowRangeMax", yellowRangeMax);
		result.set("c8y_Kpi.yellowRangeMin", yellowRangeMin);
		
		return result;
	}
}

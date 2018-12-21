package com.ntt.tc.data.measurements;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import abdom.data.json.JsonObject;
import abdom.data.json.JsonType;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ID;
import com.ntt.tc.data.inventory.ManagedObject;
import com.ntt.tc.data.measurements.Measurement;

/**
 * MeasurementSeriesCollection class
 * docs 内にオブジェクト構造定義がないため、新規作成
 *
 * @version		July 19, 2018
 * @author		Yusuke Sasaki
 */
public class MeasurementSeriesCollection extends C8yData {
	/**
	 * values は、キーとして TC_Date 型の日付が入り、その値として
	 * series に対応する値(min, max)の配列が格納されます。
	 */
	public JsonObject values;
	public Series[] series;
	public boolean truncated;
	/**
	 * 独自に追加
	 */
	public ManagedObject source;
	
/*-----------------
 * instace methods
 */
	/**
	 * series に含まれる type 文字列を抽出し、返却します。
	 *
	 * @return		type 文字列のリスト
	 */
	public List<String> getTypeList() {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < series.length; i++) {
			result.add(series[i].get("type").getValue());
		}
		return result;
	}
	
	/**
	 * この Collection に含まれる measurement の Iterable を取得します。
	 *
	 * @return		Iterable
	 */
	public Iterable<Measurement> measurements() {
		return ( () -> new SeriesIterator() );
	}
	
/*-------------
 * inner class
 */
	private class SeriesIterator implements Iterator<Measurement> {
		private Iterator<String> key;
		
		private SeriesIterator() {
			key = values.keySet().iterator();
		}
		
		@Override
		public boolean hasNext() {
			return (key.hasNext());
		}
		
		@Override
		public Measurement next() {
			Measurement result = new Measurement();
			result.source = new ID(source.id);
			JsonType vs = values.get(key.next());
			boolean first = true;
			for (int i = 0; i < series.length; i++) {
				Series serie = series[i];
				JsonType v = vs.get(i);
				if (first) {
					result.type = serie.type;
					first = false;
				}
				result.put(serie.type, serie.name, v.doubleValue(), serie.unit);
			}
			return result;
		}
		
	}
	
}

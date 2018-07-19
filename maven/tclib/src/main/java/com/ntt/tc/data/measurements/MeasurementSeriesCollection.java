package com.ntt.tc.data.measurements;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ManagedObject;
import com.ntt.tc.data.measurements.Measurement;

/**
 * MeasurementSeriesCollection class
 * docs 内にオブジェクト構造定義がないため、新規作成
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
	
	public List<String> getTypeList() {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < series.length; i++) {
			result.add(series[i].get("type").getValue());
		}
		return result;
	}
	
	public Iterable<Measurement> measurements() {
		return ( () -> new SeriesIterator() );
	}
	
	private class SeriesIterator implements Iterator<Measurement> {
		private int cursor;
		private Set<String> key;
		
		private SeriesIterator() {
			cursor = 0;
			key = values.keySet();
		}
		
		@Override
		public boolean hasNext() {
			return (cursor < key.size());
		}
		
		@Override
		public Measurement next() {
			Measurement result = new Measurement();
			// 実装されていない
			cursor++;
			return result;
		}
		
	}
	
}

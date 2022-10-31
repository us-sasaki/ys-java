package abdom.math;

import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

public class MathSet {
	
	/**
	 * 複数 Iterator の多重ネストを表現する Iterator。
	 * 集合としては、積集合。
	 */
	private static class ProductIterator implements Iterator<Object[]> {
		private Iterable<?>[] iterables;
		private Iterator<?>[] currentIterators;
		private int len;
		private Object[] holder;
		
		private ProductIterator(Iterable<?>[] iterables) {
			this.iterables = iterables;
			len = iterables.length;
			holder = new Object[len];
			currentIterators = new Iterator<?>[len];
			for (int i = 0; i < len; i++) {
				currentIterators[i] = iterables[i].iterator();
				if (i == len-1) holder[i] = null;
				else holder[i] = currentIterators[i].next();
			}
		}
		
		@Override
		public boolean hasNext() {
			for (int i = 0; i < len; i++) {
				if (currentIterators[i].hasNext()) return true;
			}
			return false;
		}
		
		@Override
		public Object[] next() {
			for (int i = len-1; i >= 0; i--) {
				if (holder[i] == null || currentIterators[i].hasNext()) {
					holder[i] = currentIterators[i].next();
					break;
				} else {
					currentIterators[i] = iterables[i].iterator();
					holder[i] = currentIterators[i].next();
				}
			}
			return holder;
		}
	}
	
	/**
	 * 複数の Iterable の積集合(全パターン網羅)の Iterable を生成します。
	 * 後ろに指定したものが内側のループになります。
	 *
	 * この Iterable はマルチスレッドで利用することはできません。
	 * 返却値は、それぞれの Iterable の要素の組です。
	 * この配列は高速化のため Iterable の中で再利用され参照が変更されます。
	 * 必要に応じてコピーして下さい。配列要素の参照先は変更されません。
	 *
	 * @param		iterables	Iterable, または配列
	 * @return		複数 Iterable の要素を順に持つ配列
	 */
	public static Iterable<Object[]> product(Object... iterables) {
		Iterable<?>[] lists = new Iterable<?>[iterables.length];
		
		for (int i = 0; i < iterables.length; i++) {
			if (iterables[i] instanceof Object[]) {
				lists[i] = Arrays.asList((Object[])iterables[i]);
			} else if (iterables[i] instanceof Iterable) {
				lists[i] = (Iterable<?>)iterables[i];
			} else {
				throw new IllegalArgumentException("product の引数は、Iterable もしくは 配列のみです。指定された値のクラス=" + iterables[i].getClass());
			}
		}
		return ( () -> new ProductIterator(lists) );
	}
}

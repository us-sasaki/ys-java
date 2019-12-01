package abdom.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * BinaryIndexedTree テスト
 */
class BinaryIndexedTreeTest {
	static final int N = 100000; // ≦ sqrt(Integer.MAX_VALUE)
	static final int LOOP = 100;
	
	@Nested
	class aggregatorがmax関数の場合 {
		
		@BeforeEach void init() {
		}
		
		@Test void ランダムテスト() {
			// 演算領域 (単位的半群) 設定
			Integer id = Integer.MIN_VALUE;
			BinaryOperator<Integer> agg = (a,b) -> Math.max(a,b);
			//UnaryOperator<Integer> inv = // undefinable
			
			// 問題設定
			BinaryIndexedTree<Integer> bit =
					new BinaryIndexedTree<>(N, agg, id);
			Integer[] val = new Integer[N];
			Random r = new Random(12345L);
			for (int i = 0; i < N; i++) val[i] = r.nextInt(N);
			bit.construct(val);
			
			for (int i = 0; i < LOOP; i++) {
				int b = (int)(r.nextDouble() * N);
				int bitm = bit.calculate(b);
				int bitc = id;
				for (int j = 0; j < b; j++) bitc = agg.apply(bitc, val[j]);
				assertEquals(bitc, bitm);
			}
		}
	}
	@Nested
	class aggregatorが足し算の場合 {
		
		@BeforeEach void init() {
		}
		
		@Test void ランダムテスト() {
			// 演算領域 (群(単位的半群)) 設定
			Integer id = 0;
			BinaryOperator<Integer> agg = (a,b) -> a+b;
			UnaryOperator<Integer> inv = a -> -a;
			
			// 問題設定
			BinaryIndexedTree<Integer> bit =
					new BinaryIndexedTree<>(N, agg, id, inv);
			Integer[] val = new Integer[N];
			Random r = new Random(12345L);
			for (int i = 0; i < N; i++) val[i] = r.nextInt(N);
			bit.construct(val);
			
			for (int i = 0; i < LOOP; i++) {
				int a = (int)(r.nextDouble() * N);
				int b = (int)(r.nextDouble() * N);
				if (a == b) continue;
				if (a > b) {
					int tmp = a; a = b; b = tmp;
				}
				int bitm = bit.calculate(a, b);
				int bitc = id;
				for (int j = a; j < b; j++) bitc = agg.apply(bitc, val[j]);
				
				assertEquals(bitc, bitm);
			}
		}
	}
}


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
 * BinaryIndexedTree2D テスト
 */
class BinaryIndexedTree2DTest {
	static final int N = 100; // ≦ 4thRoot(Integer.MAX_VALUE)
	static final int M = 150; // ≦ 4thRoot(Integer.MAX_VALUE)
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
			BinaryIndexedTree2D<Integer> bit =
					new BinaryIndexedTree2D<>(N, M, agg, id);
			Integer[][] val = new Integer[N][M];
			Random r = new Random(12345L);
			for (int i = 0; i < N; i++)
				for (int j = 0; j < M; j++) val[i][j] = r.nextInt(N);
			bit.construct(val);
			
			for (int i = 0; i < LOOP; i++) {
				int n = (int)(r.nextDouble() * N);
				int m = (int)(r.nextDouble() * M);
				int bitm = bit.calculate(n, m);
				int bitc = id;
				for (int j = 0; j < n; j++)
					for (int k = 0; k < m; k++)
						bitc = agg.apply(bitc, val[j][k]);
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
			BinaryIndexedTree2D<Integer> bit =
					new BinaryIndexedTree2D<>(N, M, agg, id, inv);
			Integer[][] val = new Integer[N][M];
			Random r = new Random(12345L);
			for (int i = 0; i < N; i++)
				for (int j = 0; j < M; j++)
					val[i][j] = r.nextInt(N);
			bit.construct(val);
			
			for (int i = 0; i < LOOP; i++) {
				int a = (int)(r.nextDouble() * N);
				int b = (int)(r.nextDouble() * N);
				if (a == b) continue;
				if (a > b) {
					int tmp = a; a = b; b = tmp;
				}
				int c = (int)(r.nextDouble() * M);
				int d = (int)(r.nextDouble() * M);
				if (c == d) continue;
				if (c > d) {
					int tmp = c; c = d; d = tmp;
				}
				int bitm = bit.calculate(a, b, c, d);
				int bitc = id;
				for (int j = a; j < b; j++)
					for (int k = c; k < d; k++)
						bitc = agg.apply(bitc, val[j][k]);
				
				assertEquals(bitc, bitm);
			}
		}
	}
}


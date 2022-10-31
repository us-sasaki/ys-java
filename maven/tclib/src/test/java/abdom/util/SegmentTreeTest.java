package abdom.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.BinaryOperator;

/**
 * SegmentTree テスト
 */
class SegmentTreeTest {
	static final int N = 40000; // ≦ sqrt(Integer.MAX_VALUE)
	static final int LOOP = 100;
	
	@Nested
	class aggregatorがmax関数の場合 {
		
		@BeforeEach void init() {
		}
		
		@Test void ランダムテスト() {
			Integer id = Integer.MIN_VALUE;
			BinaryOperator<Integer> agg = (a,b) -> Math.max(a,b);
			SegmentTree<Integer> st = new SegmentTree<>(N, agg, id);
			Integer[] val = new Integer[N];
			Random r = new Random(12345L);
			for (int i = 0; i < N; i++) val[i] = r.nextInt(N);
			st.construct(val);
			
			for (int i = 0; i < LOOP; i++) {
				int a = (int)(r.nextDouble() * N);
				int b = (int)(r.nextDouble() * N);
				if (a == b) continue;
				if (a > b) {
					int tmp = a; a = b; b = tmp;
				}
				int stm = st.calculate(a, b);
				int stc = id;
				for (int j = a; j < b; j++) {
					stc = Math.max(stc, val[j]);
				}
				assertEquals(stc, stm);
				
			}
		}
	}
}


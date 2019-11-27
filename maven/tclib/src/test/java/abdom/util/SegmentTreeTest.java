package abdom.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * SegmentTree テスト
 */
class SegmentTreeTest {
	static final int N = 20000; // ≦ sqrt(Integer.MAX_VALUE)
	static final int LOOP = 100;
	static final int ID = Integer.MIN_VALUE;
	
	@Nested
	class aggregatorがmax関数の場合 {
		
		@BeforeEach void init() {
		}
		
		@Test void 型() {
/*			SegmentTree<Integer> st = new SegmentTree<>(N,
													(a,b)->Math.max(a,b),
													ID);
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
				int stm = st.getSegmentResult(a, b);
				int stc = ID;
				for (int j = a; j < b; j++) {
					stc = Math.max(stc, val[j]);
				}
				assertEquals(stc, stm);
				
			}
*/		}
	}
}


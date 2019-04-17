package abdom.math.stats;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Stats テスト。JUnit5
 */
class StatsTest {
	static class DoubleSupplyObj {
		double theValue;
	}
	@Nested
	class statsの各計算 {
		double[] d1;
		DoubleSupplyObj[] d2;
		
		@BeforeEach void init() {
			d1 = new double[]{0d, 1d, 2d};
			d2 = new DoubleSupplyObj[3];
			for (int i = 0; i < 3; i++) {
				d2[i] = new DoubleSupplyObj();
				d2[i].theValue = (double)i;
			}
		}
		
		@Test void double配列に適用できる() {
			Stats s = Stats.value(d1);
			assertEquals(3, s.n);
			double mean = 3d/3;
			assertEquals(mean, s.mean);
			assertEquals(2d, s.max);
			assertEquals(0d, s.min);
			assertEquals( ((0d-mean)*(0d-mean)+(1d-mean)*(1d-mean)+(2d-mean)*(2d-mean))/3d, s.variance);
		}
		
		@Test void Double保持Obj配列に適用できる() {
			Stats s = Stats.value(d2, v -> v.theValue);
			assertEquals(3, s.n);
			double mean = 3d/3;
			assertEquals(mean, s.mean);
			assertEquals(2d, s.max);
			assertEquals(0d, s.min);
			assertEquals( ((0d-mean)*(0d-mean)+(1d-mean)*(1d-mean)+(2d-mean)*(2d-mean))/3d, s.variance);
		}
		
		@Test void Double保持ObjIterableに適用できる() {
			Stats s = Stats.value(Arrays.asList(d2), v -> v.theValue);
			assertEquals(3, s.n);
			double mean = 3d/3;
			assertEquals(mean, s.mean);
			assertEquals(2d, s.max);
			assertEquals(0d, s.min);
			assertEquals( ((0d-mean)*(0d-mean)+(1d-mean)*(1d-mean)+(2d-mean)*(2d-mean))/3d, s.variance);
		}
		
//		@Test void Double出力２変数関数に適用できる() {
//			Stats s = Stats.value(Arrays.asList(d2), (l, index) -> l.get(index).theValue );
//			System.out.println(s);
//		}
	}
}

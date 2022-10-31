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
	
	static double[] testValues;
	static DoubleSupplyObj[] testObjs;
	static final int N = 5;
	static final double MEAN;
	static final double MAX;
	static final double MIN;
	static final double VARIANCE;
	static final double DEVIATION;
	
	static {
		testValues = new double[N];
		double sum = 0d;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < testValues.length; i++) {
			testValues[i] = (double)(i*i);
			if (min > testValues[i]) min = testValues[i];
			if (max < testValues[i]) max = testValues[i];
			sum += testValues[i];
		}
		MIN = min;
		MAX = max;
		MEAN = sum / N;
		double v = 0d;
		for (int i = 0; i < testValues.length; i++) {
			v += (testValues[i]-MEAN)*(testValues[i]-MEAN);
		}
		VARIANCE = v / N;
		DEVIATION = Math.sqrt(VARIANCE);
		testObjs = new DoubleSupplyObj[testValues.length];
		for (int i = 0; i < testObjs.length; i++) {
			testObjs[i] = new DoubleSupplyObj();
			testObjs[i].theValue = testValues[i];
		}
	}
	
	static void checkStats(Stats s) {
		assertEquals(N, s.n);
		assertEquals(MEAN, s.mean);
		assertEquals(MAX, s.max);
		assertEquals(MIN, s.min);
		assertEquals(VARIANCE, s.variance);
		assertEquals(DEVIATION, s.deviation);
	}
	
	@Nested
	class statsの各計算 {
		
		@Test void double配列に適用できる() {
			Stats s = Stats.value(testValues);
			checkStats(s);
		}
		
		@Test void Double保持Obj配列に適用できる() {
			Stats s = Stats.value(testObjs, v -> v.theValue);
			checkStats(s);
		}
		
		@Test void Double保持ObjIterableに適用できる() {
			Stats s = Stats.value(Arrays.asList(testObjs), v -> v.theValue);
			checkStats(s);
		}
		
		@Test void Double出力２変数関数に適用できる() {
			Stats s = Stats.value(Arrays.asList(testObjs),
									(l, index) -> l.get(index).theValue );
			checkStats(s);
		}
	}
}

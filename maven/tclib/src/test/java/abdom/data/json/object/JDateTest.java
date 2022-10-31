package abdom.data.json.object;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abdom.data.json.*;

/**
 * JDateTest
 */
class JDateTest {
	private static void sleep(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException ie) {
		}
	}
	String s1 = null;
	String s2 = null;
	Thread t1 = new Thread( () -> {
				JDate j = new JDate("2019-07-14T00:00:00.000+09:00");
				//System.out.println("★１");
				sleep(100L);
				//System.out.println("★３");
				s1 = j.getValue();
			});
	Thread t2 = new Thread( () -> {
				sleep(50L);
				//System.out.println("★２");
				JDate.setDefaultFormat(new java.text.SimpleDateFormat("yy-MM-dd"));
				JDate j = new JDate("19-07-13");
				sleep(100L);
				s2 = j.getValue();
			});
	
	@Nested
	class マルチスレッド {
		@Test void スレッド別にフォーマットが利用できる() {
			try {
				t1.start();
				t2.start();
				t2.join();
				t1.join();
				assertEquals("2019-07-14T00:00:00.000+09:00", s1);
				assertEquals("19-07-13", s2);
			} catch (InterruptedException ie) {
				fail(ie);
			}
		}
	}
}

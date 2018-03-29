package abdom.data.json.object;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import abdom.data.json.*;

/**
 * JDouble テスト。
 */
public class JDoubleTest extends TestCase{
	public JDoubleTest(String testName) {
		super(testName);
	}
	protected void setUp() throws Exception {
		super.setUp();
	}
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	public static Test suite() {
		return new TestSuite( JDataTest.class );
	}
	
	public void test1() {
		JDouble j = new JDouble();
		assertEquals(j.toJson().getType(), JsonType.TYPE_VOID);
		
		JDouble j2 = new JDouble(0);
		assertEquals(j2.doubleValue(), 0d);
		assertEquals(j2.toJson().doubleValue(), 0d);
		
		JDouble j3 = new JDouble("1");
		assertEquals(j3.doubleValue(), 1d);
		assertEquals(j3.toJson().doubleValue(), 1d);
		
		try {
			JDouble j4 = new JDouble("x");
			fail();
		} catch (NumberFormatException nfe) {
		}
		
		JDouble j5 = new JDouble("");
		assertEquals(j5.toJson().getType(), JsonType.TYPE_VOID);
		
		j5.fill(new JsonValue(-1));
		assertEquals(j5.toString(), "-1.0");
		
	}
}

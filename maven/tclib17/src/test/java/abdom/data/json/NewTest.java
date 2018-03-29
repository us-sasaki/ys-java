package abdom.data.json;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertTrue ;
import org.junit.Test;

/**
 * 練習で作った JsonValue テスト。JUnit4 ベース
 */
public class NewTest {
	
	@Test
	public void 論理値テスト() {
		assertEquals(new JsonValue(true).toString(), "true");
		assertEquals(new JsonValue(true).getValue(), "true");
		assertTrue(new JsonValue(false).getValue().equals("false"));
		assertTrue(new JsonValue(true).getType() == JsonType.TYPE_BOOLEAN);
	}
}

package tdd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SemVerTest {
	
	@BeforeEach // junit5, "@Before" when junit4
	void 前準備() {
	}
	
	@Test
	void _1を渡すと文字列1を返す() throws Exception {
		System.out.println("1を実行");
		// 前準備
		FizzBuzz fizzbuzz = new FizzBuzz();
		// 実行
		String actual = fizzbuzz.convert(1);
		// 検証
		assertEquals("1", actual);
	}
	
	@DisplayName("名前を変えられる")
	@Nested
	class _大枠のテスト分類_仕様の表現 {
		@Test
		void _具体的な項目() throws Exception {
			assertThrows(Exception.class, () -> e.exec() );
		}
	}
}

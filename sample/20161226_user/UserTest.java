import java.io.*;

import com.ntt.tc.data.*;
import com.ntt.tc.net.Rest;

import abdom.data.json.*;

/**
 * 一度実行すると２度目からは 409 Conflict で異常終了するので注意すること
 */
public class UserTest {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://iottest05.cumulocity.com", "Y.Sasaki", "iottest05");
		for (int i = 5; i < 999; i++) {
			createTestUser(r, i);
		}
	}
	
	/**
	 * テストユーザを CREATE します。
	 * パスワードの制約や、メールアドレスが一致すると NG
	 */
	private static void createTestUser(Rest r, int no) throws IOException {
		String noStr = "000" + no;
		noStr = noStr.substring(noStr.length() - 4);
		User user = new User("TestUser"+noStr, "password0123", "Test"+noStr, "User"+noStr , "+81367000129", JsonType.o("language", "en"), "test"+noStr+"@user.example.com", true, false);
		
		Rest.Response resp = r.post("/user/iottest05/users", user.toString());
		
		User u3 = new User();
		u3.fill(resp.toJson());
		System.out.println(u3.toString("  "));
		System.err.println("No. "+no+" created.");
	}
}

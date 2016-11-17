import java.util.Base64;

import com.ntt.data.ByteArray;

public class t1 {
	public static void main(String[] args) {
		byte[] decoded = Base64.getDecoder().decode("bWFuYWdlbWVudC9kZXZpY2Vib290c3RyYXA6RmhkdDFiYjFm");
		System.out.println(ByteArray.toDumpList(decoded));
		System.out.println(new String(decoded));
	}
}

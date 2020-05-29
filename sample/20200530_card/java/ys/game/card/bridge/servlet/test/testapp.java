import java.applet.*;
import java.net.URL;

public class testapp extends Applet {
	public void start() {
		try {
			getAppletContext().showDocument(new URL("http://www.yahoo.co.jp?hogeo=hoge&abs=34"));
		} catch (Exception e) {
			System.out.println("hhh");
		}
	}
}

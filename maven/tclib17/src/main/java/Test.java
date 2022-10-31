import java.io.*;
import abdom.data.json.*;
import abdom.data.json.object.*;

import com.ntt.tc.data.*;
import com.ntt.tc.data.inventory.*;
import com.ntt.tc.data.measurements.*;
import com.ntt.tc.data.events.*;
import com.ntt.tc.net.*;

public class Test {
    public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("./conf.txt"));
		String host = br.readLine();
		String tenant = br.readLine();
		String user = br.readLine();
		String pass = br.readLine();
		String id = br.readLine();
		if (id == null || id.equals("")) id = "10503";
		br.close();
		API api = new API(host, tenant, user, pass);
		System.out.println("host="+host+"/tenant="+tenant+"/user="+user);
		System.out.println("ManagedObject id = "+id);
		System.out.println(api.readManagedObject(id).toString("  "));
	}
}

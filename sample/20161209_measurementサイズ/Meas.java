import java.io.*;

import abdom.data.json.*;
import com.ntt.tc.data.*;
import com.ntt.tc.data.sensor.*;

public class Meas {
	public static void main(String[] args) throws Exception {
		Reader r = new FileReader("Device.conf");
		JsonType conf		= JsonType.parse(r);
		r.close();
		
		ManagedObject managedObject = new ManagedObject();
		if (conf.get("managedObject") != null) {
			managedObject.fill(conf.get("managedObject"));
		};
		
		float temp = ((float)(int)((Math.random() * 20.0 + 15.0)*100))/100;
		System.out.println("sending Temperature meas." + temp);
		
		// c8y_TemperatureMeasurement
		Measurement m = new Measurement(managedObject, "c8y_PTCMeasurement");
		
		m.c8y_TemperatureMeasurement = new C8y_TemperatureMeasurement(temp);
		m.c8y_Battery = new C8y_Battery(23);
		
		System.out.println(m.toJson());
	}
}



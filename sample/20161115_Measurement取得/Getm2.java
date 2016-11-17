import java.io.*;

import abdom.data.json.*;
import com.ntt.tc.data.*;
import com.ntt.tc.data.rest.*;

/**
 * ğŒ•t‚«æ“¾‚ğ‚·
 * http://nttcom.cumulocity.com/measurement/measurements?source={source}&fragmentType={fragmentType}&type={type}
 *
 *     {
 *      "c8y_SpeedTest": {
 *        "Avg RTT": {
 *          "unit": "ms",
 *          "value": 53.647
 *        },
 *        "Download": {
 *          "unit": "Mbps",
 *          "value": 2.781
 *        },
 *        "Upload": {
 *          "unit": "Mbps",
 *          "value": 5.905
 *        }
 *      },
 *      "id": "8113638",
 *      "self": "http://nttcom.cumulocity.com/measurement/measurements/8113638",
 *      "source": {
 *        "id": "7207742",
 *        "self": "http://nttcom.cumulocity.com/inventory/managedObjects/7207742"
 *      },
 *      "time": "2016-10-09T08:12:01.100+09:00",
 *      "type": "c8y_Measurement"
 *    },
 *
 * 13•bA‚¿‚å‚Á‚Æ’x‚¢
 */
public class Getm2 {
	public static void main(String[] args) throws Exception {
		Rest r = Rest.getDefaultC8YInstance();
		
		long t0 = System.currentTimeMillis();
		Rest.Response resp = r.get("/measurement/measurements?pageSize=2000&currentPage=1&type=RestroomDoorCondition");
		System.out.println(resp.toJson().toString().length());
		System.out.println("elapsed time = " + (System.currentTimeMillis() - t0));
		MeasurementsResp meas = new MeasurementsResp();
		meas.fill(resp.toJson());
		System.out.println("measures.measurements.length = " + meas.measurements.length);
		// statistics.pageSize ‚Å‚í‚©‚é
		System.out.println("measures.statistics.pageSize = " + meas.statistics.pageSize);
	}
}

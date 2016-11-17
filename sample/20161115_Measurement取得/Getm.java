import java.io.*;

import abdom.data.json.*;
import com.ntt.tc.data.*;
import com.ntt.tc.data.rest.*;

public class Getm {
	public static void main(String[] args) throws Exception {
		Rest r = Rest.getDefaultC8YInstance();
		
		MeasurementResp mr = null;
		Rest.Response resp = r.get("/measurement", "");
		if (resp.code == 200) {
			mr = new MeasurementResp();
			mr.fill(resp.toJson());
			System.out.println(mr.toJson().toString("  "));
		} else {
			return;
		}
		
		String url = mr.measurements.get("self").getValue();
		int size = 0;
		long t0 = System.currentTimeMillis();
		int n = 1;
		
		for (int i = 0; i < n; i++) {
			Rest.Response rr = r.get(url);
			if (rr.code != 200) break;
			MeasurementsResp msr = new MeasurementsResp();
			JsonType jt = rr.toJson();
			msr.fill(jt);
			url = msr.next;
			size += jt.toString().length();
//			System.out.println(msr.toJson().toString("  "));
		}
		System.out.println("get size = " + size);
		System.out.println("elapsed time = " + (System.currentTimeMillis() - t0));
		System.out.println("count = " + n);
		
		t0 = System.nanoTime();
		resp = r.get("/measurement/measurements?pageSize=2000&currentPage=1");
		System.out.println(resp.toJson().toString().length());
		System.out.println("elapsed time = " + ((System.nanoTime() - t0)/1000000));
		MeasurementsResp meas = new MeasurementsResp();
		meas.fill(resp.toJson());
		System.out.println("measures.measurements.length = " + meas.measurements.length);
		// statistics.pageSize ‚Å‚í‚©‚é
		System.out.println("measures.statistics.pageSize = " + meas.statistics.pageSize);
	}
}

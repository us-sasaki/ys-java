import java.io.*;

import abdom.data.json.*;
import com.ntt.tc.net.Rest;

import com.ntt.tc.data.*;
import com.ntt.tc.data.rest.*;

public class TenantStats {
	public static void main(String[] args) throws Exception {
		Rest r = new Rest("https://nttcom.cumulocity.com", "us.sasaki@ntt.com", "nttcomsasaki3");
		
		long storageSizeSum = 0L;
		int page = 1;
		for (;;) {
			TenantStatisticsResp tsr = new TenantStatisticsResp();
			
			Rest.Response resp = r.get("/tenant/statistics?pageSize=5&currentPage="+(page++));
			System.out.println("--------------------------");
			System.out.println(resp.toJson().toString("  "));
			tsr.fill(resp.toJson());
			for (TenantUsageStatistics tus : tsr.usageStatistics) {
				storageSizeSum += tus.storageSize;
			}
			System.err.println("++++++++"+(page-1));
			if (tsr.next == null || tsr.next.equals("")) break;
		}
		
		// 足しても意味ない。毎日の履歴データのため。
		System.out.println("storage size sum = " + storageSizeSum);
	}
}

		
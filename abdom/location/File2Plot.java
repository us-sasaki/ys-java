package abdom.location;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * GPS Logger �̏o�͂��� CSV �`���̈ړ����� Plot[] �Ƃ��ēǂݎ��
 */
public class File2Plot {
	public static Plot[] read(String fname) throws IOException {
		List<Plot> list = new ArrayList<Plot>();
		
		BufferedReader br = new BufferedReader(new FileReader(fname));
		br.readLine(); // �P�s�ǂݎ̂�(�J�������s)
		while (true) {
			String line = br.readLine();
			if (line == null || "".equals(line)) break;
			
			String[] cols = line.split(",");
			
			double  lat = Double.parseDouble(cols[3]);
			double  lng = Double.parseDouble(cols[4]);
			float	acc = Float.parseFloat(cols[2]);
			long   time = Long.parseLong(cols[1]);
//			String date = cols[0];
			Plot plot = new Plot(lat, lng, time, acc);
			
			list.add(plot);
		}
		br.close();
		
		return list.toArray(new Plot[0]);
	}
}

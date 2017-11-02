import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class MergeLines {
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("usage");
			System.out.println("java MergeLines list20171004_Y.csv list20171005_Z.csv");
			System.out.println("then file \"list20171004.csv\" will be created.");
			System.exit(-1);
		}
		List<String> merged = new ArrayList<String>();
		
		for (String fname : args) {
			List<String> lines = Files.readAllLines(Paths.get(fname), Charset.forName("MS932"));
			for (String line : lines) {
				merged.add(line);
			}
		}
		
		String f = args[0];
		int index = f.lastIndexOf('.');
		f = f.substring(0, index-2) + f.substring(index);
		
		Files.write(Paths.get(f), merged,
					Charset.forName("MS932"),
					StandardOpenOption.CREATE);
	}
}

import abdom.util.*;

import java.nio.file.*;
import java.util.*;

public class Grep {
	public static void main(String[] args) throws Exception {
		List<Path> l = FileUtils.listOfFiles("./forTest2", p -> p.toString().endsWith(".html.md"));
		for (Path p : l) {
			for (String line : Files.readAllLines(p)) {
				if (line.contains("/usecase/akidoko")) {
					System.out.println(p);
					break;
				}
				if (line.contains("/usecase/tiot")) {
					System.out.println(p);
					break;
				}
				if (line.contains("lab.api")) {
					System.out.println(p);
					break;
				}
			}
		}
	}
}

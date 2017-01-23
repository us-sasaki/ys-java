//package sample;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class MarkExtractor {
	
	protected static final List<String> MARKS;
	static {
		MARKS = Arrays.asList( new String[] {
			"# ","## ","### ","#### ","##### ",
			"* ","- ",
			"1. ","2. ","3. ","4. ","5. ", "6. ","7. ","8. ","9. ","10. ",
			"    ",
			"|"
		} );
	}
	
	public List<String> extract(List<String> target) {
		List<String> result = new ArrayList<String>();
		for (String line : target) {
			int index = 0;
			for (String mark : MARKS) {
				if (line.startsWith(mark)) break;
				index++;
			}
			if (index < MARKS.size()) result.add(MARKS.get(index));
			else if (line.equals("")) result.add("");
			else result.add("sentence");
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath("ja-netcommwireless.html.md"), Charset.defaultCharset());
        
        MarkExtractor me = new MarkExtractor();
        
        List<String> extracted = me.extract(lines);
        for (String line : extracted) {
        	System.out.println(line);
        }
	}
}

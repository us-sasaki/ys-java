package misc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import misc.*;

/**
 * MarkExtractor のテスト。
 */
public class MarkExtractorTest extends TestCase{
	static final String PATH = "G:\\GitHub\\ys-java\\maven\\diffmd\\testdocs\\";
	static final String EN_ORG = "event-language-en-old\\";
	static final String EN_NEW = "event-language-en-new\\";
	static final String JA_ORG = "event-language-ja-old\\";
	static final String FILENAME = "introduction.html.md";
	
	public MarkExtractorTest(String testName) {
		super(testName);
	}
	protected void setUp() throws Exception {
		super.setUp();
	}
	protected void tearDown() throws Exception {
		super.tearDown();
	}
    public static Test suite() {
        return new TestSuite( MarkExtractorTest.class );
    }
    
    /**
     * test main
     */
	public void test0() throws IOException {
		// ファイル読み込み
        List<String> oldLines = Files.readAllLines(FileSystems.getDefault().getPath(PATH + EN_ORG + FILENAME), Charset.availableCharsets().get("UTF-8"));
        List<String> newLines = Files.readAllLines(FileSystems.getDefault().getPath(PATH + EN_NEW + FILENAME), Charset.availableCharsets().get("UTF-8"));
        List<String> oldJaLines = Files.readAllLines(FileSystems.getDefault().getPath(PATH + JA_ORG + FILENAME), Charset.availableCharsets().get("UTF-8"));
        
		MarkExtractor me = new MarkExtractor();
		List<String> enMark = me.extract(oldLines);
		List<String> jaMark = me.extract(oldJaLines);
		
		LooseMap enJaMap = new LooseMap(enMark, jaMark);
		
		//java.util.Iterator iterator = jaMark.iterator();
		//for (String line : enMark ) {
		//	System.out.println(line + "," + iterator.next());
		//}
       	
//       	for (int i = 0; i < enMark.size(); i++) {
//       		System.out.println("Eng:" + i + " Ja(min):" + enJaMap.min(i) + " Ja(max):" + enJaMap.max(i));
//       		System.out.print(oldLines.get(i));
//       		System.out.print("■");
//       		for (int j = enJaMap.min(i); j <= enJaMap.max(i); j++) {
//				System.out.print(oldJaLines.get(j));
//				System.out.print("■");
//       		}
//       		System.out.println();
//       	}
	}
	public void test1() throws IOException {
		// ファイル読み込み
		testFile("introduction.html.md", "introduction.html.txt");
		testFile("advanced.html.md", "advanced.html.txt");
		testFile("best-practices.html.md", "best-practices.html.txt");
		testFile("data-model.html.md", "data-model.html.txt");
		testFile("examples.html.md", "examples.html.txt");
		testFile("geofence.html.md", "geofence.html.txt");
	}
	
	private void testFile(String filename, String resultFname)
					throws IOException {
        List<String> oldLines = Files.readAllLines(FileSystems.getDefault().getPath(PATH + EN_ORG + filename), StandardCharsets.UTF_8);
        List<String> newLines = Files.readAllLines(FileSystems.getDefault().getPath(PATH + EN_NEW + filename), StandardCharsets.UTF_8);
        List<String> oldJaLines = Files.readAllLines(FileSystems.getDefault().getPath(PATH + JA_ORG + filename), StandardCharsets.UTF_8);
		
		DiffMdInTranslate dmit = new DiffMdInTranslate(oldLines, newLines, oldJaLines);
		List<String> text = dmit.toText();
		
		
		Files.write(FileSystems.getDefault().getPath("./" + resultFname), text, StandardCharsets.UTF_8 );
	}
	public void test2() {
	}
	public void test3() {
	}
	public void test4() {
	}
}

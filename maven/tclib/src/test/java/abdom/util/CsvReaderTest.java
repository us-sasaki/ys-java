package abdom.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class CsvReaderTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CsvReaderTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CsvReaderTest.class );
    }

    /**
     * テスト１　0byte ファイルが null で読まれること。
     */
    public void testCsvReader() throws java.io.IOException {
    	CsvReader cr = new CsvReader(new java.io.FileReader("./src/test/java/abdom/util/test1.csv"));
    	String[] row = cr.readRow();
    	assertEquals(row, null);
    }
    
    /**
     * テスト２
     */
    public void testCsvIterator() {
    	//
    	java.util.Iterator<String[]> it = CsvReader.rows("./src/test/java/abdom/util/test2.csv").iterator();
    	
    	assertTrue(it.hasNext());
    	String[] row = it.next();
    	assertEquals(row.length, 2);
    	row = it.next();
    	assertEquals(row[0], "a");
    	assertEquals(row[1], "b");
    	assertEquals(row[2], "c");
    	assertTrue(it.hasNext());
    	row = it.next();
    	assertEquals(row[0], "this is a pen.");
    	assertEquals(row[1], "that");
    	assertEquals(row[2], "is a\r\npencil.");
    	assertEquals(row[3], "quote \"quo\" ");
    	assertEquals(row.length, 4);
    	row = it.next();
    	assertEquals(row.length, 1);
    	assertEquals(row[0], "");
    	
    	assertFalse(it.hasNext());
    }
}

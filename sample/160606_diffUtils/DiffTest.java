import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


/**
 * http://hito4-t.hatenablog.com/entry/2015/02/09/223006
 */
public class DiffTest {
    
    public static void main(String[] args) throws IOException {
        List<String> oldLines = Files.readAllLines(FileSystems.getDefault().getPath(args[0]), Charset.defaultCharset());
        List<String> newLines = Files.readAllLines(FileSystems.getDefault().getPath(args[1]), Charset.defaultCharset());
        
        Patch patch = DiffUtils.diff(oldLines, newLines);
        for (Delta delta : patch.getDeltas()) {
            System.out.println(String.format("[変更前(%d)行目]", delta.getOriginal().getPosition() + 1));
            for (Object line : delta.getOriginal().getLines()) {
                System.out.println(line);
            }
            
            System.out.println("　↓");
            
            System.out.println(String.format("[変更後(%d)行目]", delta.getRevised().getPosition() + 1));
            for (Object line : delta.getRevised().getLines()) {
                System.out.println(line);
            }
            System.out.println();
        }
    }

}
import org.atilika.kuromoji.*;
import java.util.List;
import java.util.Arrays;
import java.io.*;

public class Kuro {
	public static void main(String[] args) throws Exception {
		String a = "ガートナー社リサーチサービス支払い(定期振替・1年)-ガートナー社リサーチサービス利用料";
		String fname = "kuro.txt";
		if (args.length > 0) {
			fname = args[0];
			BufferedReader br = new BufferedReader(new FileReader(fname));
			a = br.readLine();
			br.close();
		}
		// この２行で解析できる
		Tokenizer tokenizer = Tokenizer.builder().build();
		List<Token> tokens = tokenizer.tokenize(a);
		
		//
		PrintWriter p = new PrintWriter(new FileWriter(fname + ".result.txt"));
		
		// 単純分解
		for (Token token : tokens) {
			p.println(token.getSurfaceForm());
		}
		
		// 結果を出力してみる
		for (Token token : tokens) {
		    p.println("==================================================");
		    p.println("allFeatures : " + token.getAllFeatures());
		    p.println("partOfSpeech : " + token.getPartOfSpeech());
		    p.println("position : " + token.getPosition());
		    p.println("reading : " + token.getReading());
		    p.println("surfaceFrom : " + token.getSurfaceForm());
		    p.println("allFeaturesArray : " + Arrays.asList(token.getAllFeaturesArray()));
		    p.println("辞書にある言葉? : " + token.isKnown());
		    p.println("未知語? : " + token.isUnknown());
		    p.println("ユーザ定義? : " + token.isUser());
		}
		p.close();
	}
}



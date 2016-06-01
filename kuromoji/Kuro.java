import org.atilika.kuromoji.*;
import java.util.List;
import java.util.Arrays;
import java.io.*;

public class Kuro {
	public static void main(String[] args) throws Exception {
		String a = "�K�[�g�i�[�Ѓ��T�[�`�T�[�r�X�x����(����U�ցE1�N)-�K�[�g�i�[�Ѓ��T�[�`�T�[�r�X���p��";
		String fname = "kuro.txt";
		if (args.length > 0) {
			fname = args[0];
			BufferedReader br = new BufferedReader(new FileReader(fname));
			a = br.readLine();
			br.close();
		}
		// ���̂Q�s�ŉ�͂ł���
		Tokenizer tokenizer = Tokenizer.builder().build();
		List<Token> tokens = tokenizer.tokenize(a);
		
		//
		PrintWriter p = new PrintWriter(new FileWriter(fname + ".result.txt"));
		
		// �P������
		for (Token token : tokens) {
			p.println(token.getSurfaceForm());
		}
		
		// ���ʂ��o�͂��Ă݂�
		for (Token token : tokens) {
		    p.println("==================================================");
		    p.println("allFeatures : " + token.getAllFeatures());
		    p.println("partOfSpeech : " + token.getPartOfSpeech());
		    p.println("position : " + token.getPosition());
		    p.println("reading : " + token.getReading());
		    p.println("surfaceFrom : " + token.getSurfaceForm());
		    p.println("allFeaturesArray : " + Arrays.asList(token.getAllFeaturesArray()));
		    p.println("�����ɂ��錾�t? : " + token.isKnown());
		    p.println("���m��? : " + token.isUnknown());
		    p.println("���[�U��`? : " + token.isUser());
		}
		p.close();
	}
}



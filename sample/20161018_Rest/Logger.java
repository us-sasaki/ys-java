import java.io.*;
import java.net.*;

/**
 * ���ȈՂȃT�[�o�B�N����A�P���Socket�ڑ����󂯁A���͓��e��
 * System.out �Ƀ_���v����B�_���v�͓��̓R�[�h�����̂܂܏o�͂���B
 * (ASCII������œ��͂���邱�Ƃ�z�肵�Ă���)
 *
 *
 * Cumulocity �� REST Request �d���擾�p��ړI�Ƃ���B
 */
public class Logger {
	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(8080, 10);
		System.out.println("Server Started");
		Socket sock = ss.accept();
		System.out.println("Socket Accepted");
		InputStream in = sock.getInputStream();
		
		while (true) {
			int c = in.read();
			if (c == -1) break;
			System.out.write(c);
			System.out.flush();
		}
		
		sock.close();
	}
}

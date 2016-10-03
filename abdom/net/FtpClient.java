package abdom.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Apache Commons �𗘗p�����������g���₷�����邽�߂̃��b�p�[
 * <pre>�g����
 * FtpClient f = new FtpClient("ip or host", "user", "password");
 * f.connect();
 * f.setBinary(false); // �Ȃ��Ă��悢(default true)
 * f.setChmod("644"); // �Ȃ��Ă��悢(default 644)
 * f.put("FtpPut.java", "/home/yusuke/FtpPut.java");
 * f.put("FtpPut.java", "/home/yusuke/FtpPut.java");
 * f.close();
 * </pre>
 */
public class FtpClient {

	protected String host;
	protected String user;
	protected String password;
	protected String chmod = "644";
	protected boolean binary = true;
	
	protected FTPClient ftp;
	
/*-------------
 * constructor
 */
	/**
	 * �w�肳�ꂽ host, user, password ��ێ����� FtpClient ���쐬���܂��B
	 *
	 * @param	host	�ڑ���T�[�o�A�h���X
	 * @param	user	���O�C�����[�U��
	 * @param	password	���O�C���p�X���[�h
	 */
	public FtpClient(String host, String user, String password) {
		this.host = host;
		this.user = user;
		this.password = password;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * ���ڑ���Ԃ̏ꍇ�A�ڑ����܂��B
	 */
	public void connect() throws IOException {
		connectIfNeeded();
	}
	
	/**
	 * �o�C�i�����[�h���A�A�X�L�[���[�h����ݒ肵�܂��B
	 * �f�t�H���g�̓o�C�i�����[�h�ł��B
	 *
	 * @param	binary	true..binary���[�h false..ASCII���[�h
	 */
	public void setBinary(boolean binary) {
		this.binary = binary;
	}
	
	/**
	 * �t�@�C���]����ASITE CHMOD �R�}���h�� chmod ����l��
	 * �w�肵�܂��B
	 *
	 * @param	chmod	644(-rw-r--r--), 600(-rw-------) �Ȃǂ̒l
	 */
	public void setChmod(String chmod) {
		this.chmod = chmod;
	}
	
	/**
	 * ���[�J���t�@�C�����A�����[�g�t�@�C����(�t���p�X)���w�肵��
	 * �t�@�C���� put ���܂��B
	 */
	public void put(String localFileName, String remoteFileName) throws IOException {
		connectIfNeeded();
		putImpl(localFileName, remoteFileName);
	}
	
	private void connectIfNeeded() throws IOException {
		if (ftp == null) {
			ftp = new FTPClient();
		}
		if (!ftp.isConnected()) {
			ftp.connect(host); // server, port �Ƃ����w�������
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				throw new IOException(host + " ��ftp�ڑ��ł��܂���");
			}
		}
		if (!ftp.login(user, password)) {
			ftp.logout();
			ftp.disconnect();
			throw new IOException(host + " �� " + user + " �Ń��O�C���ł��܂���");
		}
	}
	
	private void putImpl(String local, String remote) throws IOException {
		if (binary) {
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
		} else {
			ftp.setFileType(FTP.ASCII_FILE_TYPE);
		}
		// passive ���[�h
		ftp.enterLocalPassiveMode();
		ftp.setUseEPSVwithIPv4(false); // EPSV (Extended PASV) �͂킩��񂪎g��Ȃ�
		InputStream input = new FileInputStream(local);
		ftp.storeFile(remote, input);
		input.close();
		
		//
		if (!ftp.doCommand("site chmod " + chmod, remote)) {
			ftp.logout();
			ftp.disconnect();
			throw new IOException("chmod ���s : " + ftp.getReplyString());
		}
	}
	
	/**
	 * �ڑ����N���[�Y���܂��B
	 */
	public void close() throws IOException {
		if (ftp != null) {
			ftp.logout();
			ftp.disconnect();
			ftp = null;
		}
	}
	
/*------------
 * test�pmain
 */
	public static void main(String[] args) throws IOException {
		abdom.net.FtpClient f = new abdom.net.FtpClient("192.168.0.209", "yusuke", "gradius3");
		f.connect();
		f.put("makeDocs.bat", "/home/yusuke/makeDocs.bat");
		f.put("makeMyDoc.bat", "/home/yusuke/makeMyDoc.bat");
		f.close();
	}
	
}

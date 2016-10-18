package abdom.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Apache Commons �𗘗p�����������g���₷�����邽�߂̃��b�p�[
 * <pre>�g����
 * FtpClient f = new FtpClient("ip or host", "user", "password");
 * f.connect();
 * f.setBinary(false); // �Ȃ��Ă��悢(default true)
 * f.setChmod("644"); // �Ȃ��Ă��悢(default 644)
 * f.put("FtpPut.java", "/home/yusuke/FtpPut.java");
 * f.close();
 * </pre>
 * Apache Commons http://commons.apache.org/proper/commons-net/
 */
public class FtpClient {

	protected String host;
	protected String user;
	protected String password;
	
	protected String enc = "SJIS";
	protected String chmod = "644";
	protected boolean binary = true;
	
	protected FTPClient ftp;
	
	protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
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
	 * �R���g���[���G���R�[�f�B���O���w�肵�܂��B
	 * connect() �ȑO�Ɏ��s����K�v������܂��B
	 * �ȗ������ꍇ�ASJIS �ɂȂ�܂��B
	 */
	public void setControlEncoding(String enc) {
		this.enc = enc;
	}
	
	/**
	 * �t�@�C���]����ASITE CHMOD �R�}���h�� chmod ����l��
	 * �w�肵�܂��B
	 * �f�t�H���g�� 644(-rw-r--r--) �ł�
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
	
	/**
	 * �����[�g�t�@�C�������݂��Ȃ��A�܂��͌Â��ꍇ�Aput ���܂��B
	 *
	 * @return	put ������
	 */
	public boolean putIfNew(String localFileName, String remoteFileName)
									throws IOException {
		Date rd = peekDate(remoteFileName);
		if (rd != null) {
			File f = new File(localFileName);
			if (f.lastModified() <= rd.getTime()) {
//				System.out.println("���[�J���t�@�C��:"+localFileName+" �̕����Â����߁A�A�b�v���[�h���X�L�b�v���܂�");
				return false;
			}
		}
		putImpl(localFileName, remoteFileName);
		return true;
	}
	
	/**
	 * Connect ����Ă��Ȃ��ꍇ�Aconnect ���A���O�C�����܂��B
	 * �������A�^�C���A�E�g�̌�̋����͌��؂���Ă��܂���B
	 */
	private void connectIfNeeded() throws IOException {
		if (ftp == null) {
			ftp = new FTPClient();
			ftp.setControlEncoding(enc);
		}
		if (!ftp.isConnected()) {
			ftp.connect(host); // server, port �Ƃ����w�������
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				throw new IOException(host + " ��ftp�ڑ��ł��܂���");
			}
			if (!ftp.login(user, password)) {
				ftp.logout();
				ftp.disconnect();
				throw new IOException(host + " �� " + user + " �Ń��O�C���ł��܂���");
			}
		}
	}
	
	/**
	 * put �����̎����ł��B����t�@�C���������[�g�ɂ������ꍇ�A�㏑������܂��B
	 *
	 * @param	local	���[�J���t�@�C����
	 * @param	remote	�����[�g�t�@�C���p�X(�t���p�X)
	 */
	private void putImpl(String local, String remote) throws IOException {
		if (binary) {
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
		} else {
			ftp.setFileType(FTP.ASCII_FILE_TYPE);
		}
		// passive ���[�h
		ftp.enterLocalPassiveMode();
		ftp.setUseEPSVwithIPv4(false); // EPSV (Extended PASV) �͂킩��񂪎g��Ȃ�
		InputStream input = new BufferedInputStream(new FileInputStream(local), 16384);
		ftp.storeFile(remote, input);
		input.close();
		
		// chmod �����s
		if (!ftp.doCommand("site chmod " + chmod, remote)) {
			ftp.logout();
			ftp.disconnect();
			throw new IOException("chmod ���s : " + ftp.getReplyString());
		}
	}
	
	/**
	 * MLSD �R�}���h�ɂ��A��^�t�H�[�}�b�g�̃t�@�C�������擾���܂��B
	 * �T�[�o���N���C�A���g�ƃf�t�H���g�^�C���]�[������v���Ă��邱�Ƃ�
	 * ���肵�Ă��܂��B
	 *
	 * @param	remote	Date���擾����Ώۂ̃t�@�C���p�X
	 * @return	�t�@�C���̍X�V�����A�t�@�C�������݂��Ȃ��ꍇ null
	 */
	public Date peekDate(String remote) throws IOException {
		connectIfNeeded();
		FTPFile f = ftp.mdtmFile(remote);
		if (f == null) return null; // �t�@�C�����Ȃ�
		
		String tz = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
		try {
			return sdf.parse(f.toFormattedString(tz).substring(43,66));
		} catch (java.text.ParseException pe) {
			System.out.println("FTP �� MLSD �����`�����s���ł�"+f.toFormattedString("JST").substring(43,66));
		}
		return null;
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
//		f.put("makeMyDoc.bat", "/home/yusuke/makeMyDoc.bat");
		f.close();
	}
	
}

package abdom.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Apache Commons を利用した自分が使いやすくするためのラッパー
 * <pre>使い方
 * FtpClient f = new FtpClient("ip or host", "user", "password");
 * f.connect();
 * f.setBinary(false); // なくてもよい(default true)
 * f.setChmod("644"); // なくてもよい(default 644)
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
	 * 指定された host, user, password を保持する FtpClient を作成します。
	 *
	 * @param	host	接続先サーバアドレス
	 * @param	user	ログインユーザ名
	 * @param	password	ログインパスワード
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
	 * 未接続状態の場合、接続します。
	 */
	public void connect() throws IOException {
		connectIfNeeded();
	}
	
	/**
	 * バイナリモードか、アスキーモードかを設定します。
	 * デフォルトはバイナリモードです。
	 *
	 * @param	binary	true..binaryモード false..ASCIIモード
	 */
	public void setBinary(boolean binary) {
		this.binary = binary;
	}
	
	/**
	 * ファイル転送後、SITE CHMOD コマンドで chmod する値を
	 * 指定します。
	 *
	 * @param	chmod	644(-rw-r--r--), 600(-rw-------) などの値
	 */
	public void setChmod(String chmod) {
		this.chmod = chmod;
	}
	
	/**
	 * ローカルファイル名、リモートファイル名(フルパス)を指定して
	 * ファイルを put します。
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
			ftp.connect(host); // server, port という指定もある
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				throw new IOException(host + " にftp接続できません");
			}
		}
		if (!ftp.login(user, password)) {
			ftp.logout();
			ftp.disconnect();
			throw new IOException(host + " に " + user + " でログインできません");
		}
	}
	
	private void putImpl(String local, String remote) throws IOException {
		if (binary) {
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
		} else {
			ftp.setFileType(FTP.ASCII_FILE_TYPE);
		}
		// passive モード
		ftp.enterLocalPassiveMode();
		ftp.setUseEPSVwithIPv4(false); // EPSV (Extended PASV) はわからんが使わない
		InputStream input = new FileInputStream(local);
		ftp.storeFile(remote, input);
		input.close();
		
		//
		if (!ftp.doCommand("site chmod " + chmod, remote)) {
			ftp.logout();
			ftp.disconnect();
			throw new IOException("chmod 失敗 : " + ftp.getReplyString());
		}
	}
	
	/**
	 * 接続をクローズします。
	 */
	public void close() throws IOException {
		if (ftp != null) {
			ftp.logout();
			ftp.disconnect();
			ftp = null;
		}
	}
	
/*------------
 * test用main
 */
	public static void main(String[] args) throws IOException {
		abdom.net.FtpClient f = new abdom.net.FtpClient("192.168.0.209", "yusuke", "gradius3");
		f.connect();
		f.put("makeDocs.bat", "/home/yusuke/makeDocs.bat");
		f.put("makeMyDoc.bat", "/home/yusuke/makeMyDoc.bat");
		f.close();
	}
	
}

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
 * Apache Commons を利用した自分が使いやすくするためのラッパー
 * <pre>使い方
 * FtpClient f = new FtpClient("ip or host", "user", "password");
 * f.connect();
 * f.setBinary(false); // なくてもよい(default true)
 * f.setChmod("644"); // なくてもよい(default 644)
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
	 * コントロールエンコーディングを指定します。
	 * connect() 以前に実行する必要があります。
	 * 省略した場合、SJIS になります。
	 */
	public void setControlEncoding(String enc) {
		this.enc = enc;
	}
	
	/**
	 * ファイル転送後、SITE CHMOD コマンドで chmod する値を
	 * 指定します。
	 * デフォルトは 644(-rw-r--r--) です
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
	
	/**
	 * リモートファイルが存在しない、または古い場合、put します。
	 *
	 * @return	put したか
	 */
	public boolean putIfNew(String localFileName, String remoteFileName)
									throws IOException {
		Date rd = peekDate(remoteFileName);
		if (rd != null) {
			File f = new File(localFileName);
			if (f.lastModified() <= rd.getTime()) {
//				System.out.println("ローカルファイル:"+localFileName+" の方が古いため、アップロードをスキップします");
				return false;
			}
		}
		putImpl(localFileName, remoteFileName);
		return true;
	}
	
	/**
	 * Connect されていない場合、connect し、ログインします。
	 * ただし、タイムアウトの後の挙動は検証されていません。
	 */
	private void connectIfNeeded() throws IOException {
		if (ftp == null) {
			ftp = new FTPClient();
			ftp.setControlEncoding(enc);
		}
		if (!ftp.isConnected()) {
			ftp.connect(host); // server, port という指定もある
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				throw new IOException(host + " にftp接続できません");
			}
			if (!ftp.login(user, password)) {
				ftp.logout();
				ftp.disconnect();
				throw new IOException(host + " に " + user + " でログインできません");
			}
		}
	}
	
	/**
	 * put 処理の実装です。同一ファイルがリモートにあった場合、上書きされます。
	 *
	 * @param	local	ローカルファイル名
	 * @param	remote	リモートファイルパス(フルパス)
	 */
	private void putImpl(String local, String remote) throws IOException {
		if (binary) {
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
		} else {
			ftp.setFileType(FTP.ASCII_FILE_TYPE);
		}
		// passive モード
		ftp.enterLocalPassiveMode();
		ftp.setUseEPSVwithIPv4(false); // EPSV (Extended PASV) はわからんが使わない
		InputStream input = new BufferedInputStream(new FileInputStream(local), 16384);
		ftp.storeFile(remote, input);
		input.close();
		
		// chmod を実行
		if (!ftp.doCommand("site chmod " + chmod, remote)) {
			ftp.logout();
			ftp.disconnect();
			throw new IOException("chmod 失敗 : " + ftp.getReplyString());
		}
	}
	
	/**
	 * MLSD コマンドにより、定型フォーマットのファイル情報を取得します。
	 * サーバがクライアントとデフォルトタイムゾーンが一致していることを
	 * 仮定しています。
	 *
	 * @param	remote	Dateを取得する対象のファイルパス
	 * @return	ファイルの更新日時、ファイルが存在しない場合 null
	 */
	public Date peekDate(String remote) throws IOException {
		connectIfNeeded();
		FTPFile f = ftp.mdtmFile(remote);
		if (f == null) return null; // ファイルがない
		
		String tz = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
		try {
			return sdf.parse(f.toFormattedString(tz).substring(43,66));
		} catch (java.text.ParseException pe) {
			System.out.println("FTP の MLSD 応答形式が不正です"+f.toFormattedString("JST").substring(43,66));
		}
		return null;
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
//		f.put("makeMyDoc.bat", "/home/yusuke/makeMyDoc.bat");
		f.close();
	}
	
}

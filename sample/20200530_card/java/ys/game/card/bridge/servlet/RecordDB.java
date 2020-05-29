package ys.game.card.bridge.servlet;

import java.sql.*;

/**
 * 練習用アプレットで使用する、得点板ＤＢにアクセスするクラスです。
 * ＤＢは、Postgres DB PracticeApplet の Record テーブルを使用します。
 * 設計がよくないため、排他制御に関する制限があります。
 * ＤＢアクセスに関する各関数は排他的に実行する必要があり、nextRecord()
 * を実行中に、insert(), getRank() を呼び出すことはできません。
 */
public class RecordDB {
	protected static final String	DBNAME	= "PracticeApplet";
	protected static final String	TABLE	= "Record";
	protected static final String	URI		= "jdbc:postgresql://192.168.0.19:5432/"+DBNAME;
	protected static final String	USER	= "postgres";
	protected static final String	PASS	= "postgres1";
	protected static final int		BOARDS	= 4;
	
	protected static Connection		db;
	
	protected Statement				st;
	protected ResultSet				rs;
	
/*-------------
 * Constructor
 */
	public RecordDB() {
		try {
			Class.forName("org.postgresql.Driver");
			if (db == null) {
				db = DriverManager.getConnection(URI, USER, PASS);
				db.setAutoCommit(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalError("DB Connect Error" + e);
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 指定されたデータでのインサートを行います。
	 *
	 */
	public void insert(int score, String[] contract, int[] made, String name, String comment, int intcode, String board) {
		try {
			// シーケンスオブジェクトから id を払い出す
			String nextValSql = "select NEXTVAL('PlaySerial')";
			st = db.createStatement();
			ResultSet rs = st.executeQuery(nextValSql);
			rs.next();
			int id = rs.getInt("nextval");
			
			// 二重登録チェックを行う
			// intcodeの一致するidと同一のplaylineを持つカラムが存在するかをチェックする
//			StringBuffer sql1 = new StringBuffer();
//			sql1.append("select id from ");
//			sql1.append(TABLE);
//			sql1.append(" where intcode=");
//			sql1.append(intcode);
//			sql1
			
			// Record テーブルへの sql insert 文を構成します
			StringBuffer sql = new StringBuffer();
			sql.append("insert into ");
			sql.append(TABLE);
			sql.append("(score, cont1, made1, cont2, made2, cont3, made3, cont4, made4, name, comment, id, intcode) values(");
			sql.append(score);
			sql.append(",");
			for (int i = 0; i < BOARDS; i++) {
				sql.append("\'");
				sql.append(contract[i]);
				sql.append("\'");
				sql.append(",");
				sql.append(made[i]);
				sql.append(",");
			}
			sql.append("\'");
			sql.append(name);
			sql.append("\',\'");
			sql.append(comment);
			sql.append("\',");
			sql.append(id);
			sql.append(",");
			sql.append(intcode);
			sql.append(")");
			System.out.println(sql);
			
			// insert 文を発行します
			int rows =  st.executeUpdate(sql.toString());
			
			// PlayLine テーブルへの sql insert 文を構成します
			sql = new StringBuffer();
			sql.append("insert into playline (id, board) values(");
			sql.append(id);
			sql.append(",\'");
			sql.append(board);
			sql.append("\')");
			rows = st.executeUpdate(sql.toString());
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		closeStatement();
	}
	
	public void insert(Record rec) {
		insert(rec.score, rec.contract, rec.made, rec.name, rec.comment, rec.intcode, rec.board);
	}
	
	/**
	 * @return		rank	score順に並べたときの順位
	 */
	public int getRank(int score) {
		try {
			// sql 文をつくる
			StringBuffer sql = new StringBuffer();
			sql.append("select count(*) from ");
			sql.append(TABLE);
			sql.append(" where score<");
			sql.append(score);
			
			// select 文の発行
			st = db.createStatement();
			rs = st.executeQuery(sql.toString());
			rs.next();
			int count = rs.getInt(1);
			
			return count + 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeStatement();
		
		return -1;
	}
	
	/**
	 * 得点順にならべて行の取得を開始します。
	 */
	public void select() {
		try {
			// sql 文をつくる
			StringBuffer sql = new StringBuffer();
			sql.append("select * from ");
			sql.append("record, playline where record.id=playline.id");
			sql.append(" order by score DESC, record.id ASC");
			
			// select 文の発行
			st = db.createStatement();
			rs = st.executeQuery(sql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * select() の後に呼び、次の行を取得します。
	 * ただし、本オブジェクトによる insert() の呼び出しがあると取得できなくなります
	 */
	public Record nextRecord() {
		try {
			if (rs == null)
				throw new IllegalStateException("nextRecord() 前に select() を実行してください");
			boolean valid = rs.next();
			if (!valid) return null;
			
			Record result = new Record();
			result.timestamp	= rs.getTimestamp("date");
			result.score		= rs.getInt("score");
			result.contract		= new String[BOARDS];
			result.made			= new int[BOARDS];
			for (int i = 0; i < BOARDS; i++) {
				result.contract[i]	= rs.getString("cont"+(i+1));
				result.made[i]		= rs.getInt("made"+(i+1));
			}
			result.name			= rs.getString("name");
			result.comment		= rs.getString("comment");
			result.id			= rs.getInt("id");
			result.intcode		= rs.getInt("intcode");
			result.board		= rs.getString("board");
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void closeStatement() {
		// Statement のクローズを試みる(必要？)
		try {
			if (st != null) st.close();
		} catch (SQLException ignored) {
			ignored.printStackTrace();
		}
		st = null;
	}
	
	
	public void finalize() throws Throwable {
		super.finalize();
		try {
			if (st != null) st.close();
		} catch (SQLException e) {
		}
	}
	
/*-----------------
 * デバッグ用 main
 */
	public static void main(String[] args) throws Throwable {
		RecordDB db = new RecordDB();
//		db.insert(10, new String[] {"1NT","3C", "6SXX", "5D"}, new int[] {7, 9, 13, 10}, "hogeo", "日本語", 1234, "boardinfo1");
//		Thread.sleep(400);
//		db.insert(20, new String[] {"3NT","3D", "7NTXX", "5D"}, new int[] {9, 9, 13, 11}, "hogetarou", "コメント", 1234, "boardinfo2");
		
		System.out.println("Rank of 5 is " + (db.getRank(5)));
		System.out.println("Rank of 10 is " + (db.getRank(5)));
		System.out.println("Rank of 15 is " + (db.getRank(15)));
		System.out.println("Rank of 25 is " + (db.getRank(25)));
		
		db.select();
		while (true) {
			Record r = db.nextRecord();
			if (r == null) break;
			System.out.println(r);
		}
		db.finalize();
	}
}

class Record {
	Timestamp	timestamp;
	int			score;
	String[]	contract;
	int[]		made;
	String		name;
	String		comment;
	int			id;
	int			intcode;
	String		board;
	
	public String toString() {
		StringBuffer st = new StringBuffer();
		st.append("name="+name+"\n");
		st.append("comment="+comment+"\n");
		st.append("score="+score+"\n");
		for (int i = 0; i < 4; i++) {
			st.append("contract="+contract[i]);
			st.append("  made="+made[i]+"\n");
		}
		st.append(timestamp);
		st.append("\n");
		st.append("id    = " + id + "\n");
		st.append("board = " + board);
		return st.toString();
	}
}

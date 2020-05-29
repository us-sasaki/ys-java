package ys.game.card.bridge.servlet;

import java.sql.*;

/**
 * ���K�p�A�v���b�g�Ŏg�p����A���_�c�a�ɃA�N�Z�X����N���X�ł��B
 * �c�a�́APostgres DB PracticeApplet �� Record �e�[�u�����g�p���܂��B
 * �݌v���悭�Ȃ����߁A�r������Ɋւ��鐧��������܂��B
 * �c�a�A�N�Z�X�Ɋւ���e�֐��͔r���I�Ɏ��s����K�v������AnextRecord()
 * �����s���ɁAinsert(), getRank() ���Ăяo�����Ƃ͂ł��܂���B
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
	 * �w�肳�ꂽ�f�[�^�ł̃C���T�[�g���s���܂��B
	 *
	 */
	public void insert(int score, String[] contract, int[] made, String name, String comment, int intcode, String board) {
		try {
			// �V�[�P���X�I�u�W�F�N�g���� id �𕥂��o��
			String nextValSql = "select NEXTVAL('PlaySerial')";
			st = db.createStatement();
			ResultSet rs = st.executeQuery(nextValSql);
			rs.next();
			int id = rs.getInt("nextval");
			
			// ��d�o�^�`�F�b�N���s��
			// intcode�̈�v����id�Ɠ����playline�����J���������݂��邩���`�F�b�N����
//			StringBuffer sql1 = new StringBuffer();
//			sql1.append("select id from ");
//			sql1.append(TABLE);
//			sql1.append(" where intcode=");
//			sql1.append(intcode);
//			sql1
			
			// Record �e�[�u���ւ� sql insert �����\�����܂�
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
			
			// insert ���𔭍s���܂�
			int rows =  st.executeUpdate(sql.toString());
			
			// PlayLine �e�[�u���ւ� sql insert �����\�����܂�
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
	 * @return		rank	score���ɕ��ׂ��Ƃ��̏���
	 */
	public int getRank(int score) {
		try {
			// sql ��������
			StringBuffer sql = new StringBuffer();
			sql.append("select count(*) from ");
			sql.append(TABLE);
			sql.append(" where score<");
			sql.append(score);
			
			// select ���̔��s
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
	 * ���_���ɂȂ�ׂčs�̎擾���J�n���܂��B
	 */
	public void select() {
		try {
			// sql ��������
			StringBuffer sql = new StringBuffer();
			sql.append("select * from ");
			sql.append("record, playline where record.id=playline.id");
			sql.append(" order by score DESC, record.id ASC");
			
			// select ���̔��s
			st = db.createStatement();
			rs = st.executeQuery(sql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * select() �̌�ɌĂсA���̍s���擾���܂��B
	 * �������A�{�I�u�W�F�N�g�ɂ�� insert() �̌Ăяo��������Ǝ擾�ł��Ȃ��Ȃ�܂�
	 */
	public Record nextRecord() {
		try {
			if (rs == null)
				throw new IllegalStateException("nextRecord() �O�� select() �����s���Ă�������");
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
		// Statement �̃N���[�Y�����݂�(�K�v�H)
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
 * �f�o�b�O�p main
 */
	public static void main(String[] args) throws Throwable {
		RecordDB db = new RecordDB();
//		db.insert(10, new String[] {"1NT","3C", "6SXX", "5D"}, new int[] {7, 9, 13, 10}, "hogeo", "���{��", 1234, "boardinfo1");
//		Thread.sleep(400);
//		db.insert(20, new String[] {"3NT","3D", "7NTXX", "5D"}, new int[] {9, 9, 13, 11}, "hogetarou", "�R�����g", 1234, "boardinfo2");
		
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

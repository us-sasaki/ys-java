import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.Calendar.*;

/**
 * 定期問題で、20日まで年休が取れる場合
 */
public class Teiki2 {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private static final Calendar START = getCalendar("2017/04/01");
	private static final Calendar END	= getCalendar("2018/03/31");
	
	/** 休日のリスト */
	private static final Set<Calendar>	HOLIDAYS;
	static {
		HOLIDAYS = new HashSet<Calendar>();
		
		// 土日を登録
		for (Calendar c = START; !isFinished(c); c = nextDay(c)) {
			int day = c.get(DAY_OF_WEEK);
			if (day == SATURDAY || day == SUNDAY) {
				HOLIDAYS.add( c ); // c は毎回新しいインスタンス
			}
		}
		
		// 他の日(祝日など)
		HOLIDAYS.add( getCalendar("2017/05/03") );
		HOLIDAYS.add( getCalendar("2017/05/04") );
		HOLIDAYS.add( getCalendar("2017/05/05") );
		HOLIDAYS.add( getCalendar("2017/07/17") ); // 海の日
		HOLIDAYS.add( getCalendar("2017/08/11") ); // 山の日
		HOLIDAYS.add( getCalendar("2017/08/14") ); // 夏休み
		HOLIDAYS.add( getCalendar("2017/08/15") );
		HOLIDAYS.add( getCalendar("2017/08/16") );
		HOLIDAYS.add( getCalendar("2017/08/17") );
		HOLIDAYS.add( getCalendar("2017/08/18") );
		HOLIDAYS.add( getCalendar("2017/09/18") ); // 敬老の日
		HOLIDAYS.add( getCalendar("2017/10/09") ); // 体育の日
		HOLIDAYS.add( getCalendar("2017/11/03") ); // 文化の日
		HOLIDAYS.add( getCalendar("2017/11/23") ); // 勤労感謝の日
		HOLIDAYS.add( getCalendar("2017/12/29") ); // 正月休み
		HOLIDAYS.add( getCalendar("2018/01/01") );
		HOLIDAYS.add( getCalendar("2018/01/02") );
		HOLIDAYS.add( getCalendar("2018/01/03") );
		HOLIDAYS.add( getCalendar("2018/01/08") ); // 成人の日
		HOLIDAYS.add( getCalendar("2018/02/12") );
		HOLIDAYS.add( getCalendar("2018/03/21") );
	}
	
	/**
	 * (日付, 残年休)→Strat の写像。
	 * i.e. 日付、残年休に対する最適解 Strat が格納される。
	 * 一度計算したものをストックするために利用(高速化)。
	 */
	private static Map<Calendar, Map<Integer, Strat>> minimumStrat;
	static {
		minimumStrat = new HashMap<Calendar, Map<Integer, Strat>>();
	}
	
	/**
	 * 定期券などの１回分の券を表すクラス
	 */
	static class Ticket {
		int type; // -1..休日 0.. 1日, 1.. 1ヵ月, 2.. 3ヵ月, 3..6ヵ月
		Calendar start; // 含む
		Calendar end; // 含む
		int fare;
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(sdf.format(start.getTime()));
			sb.append('～');
			sb.append(sdf.format(end.getTime()));
			
			switch (type) {
			case -2:	sb.append(" 休日　"); break;
			case -1:	sb.append(" 年休　"); break;
			case  0:	sb.append(" 片道x2"); break;
			case  1:    sb.append(" １ヵ月"); break;
			case  2:	sb.append(" ３ヵ月"); break;
			case  3:	sb.append(" ６ヵ月"); break;
			default:	sb.append(" ＊＊　");
			}
			sb.append(" \\");
			sb.append(fare);
			return sb.toString();
		}
	}
	
	/**
	 * 買い方を表すクラス
	 */
	static class Strat {
		List<Ticket> seq;
		int fare;
		
		/**
		 * 指定された Ticket を買い、続く日は follow の買い方をする
		 * Strat を作成します。
		 */
		Strat(Ticket start, Strat follow) {
			seq = new ArrayList<Ticket>();
			fare = start.fare;
			seq.add(start);
			if (follow != null) {
				seq.addAll(follow.seq);
				fare += follow.fare;
			}
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < seq.size(); i++) {
				String n = "   " + String.valueOf(i);
				sb.append(n.substring(n.length()-4));
				sb.append(' ');
				sb.append(seq.get(i));
				sb.append("\r\n");
			}
			return sb.toString();
		}
	}
	
/*----------------
 * static methods
 */
	/**
	 * 最小金額を与える Strat を計算します。
	 * 再帰呼び出しを利用しています。
	 *
	 * @param	start	開始年月日
	 * @param	paidVacationCount	残年休日数
	 * @return	最小金額を与える Strat
	 */
	static Strat calcMinimumFare(Calendar start, int paidVacationCount) {
		Map<Integer, Strat> r = minimumStrat.get(start);
		Strat result = null;
		
		if (r != null) {
			result = r.get(paidVacationCount);
			if (result != null) return result; // 計算済み
		}
		
		int pvc = paidVacationCount;
		if (isFinished(start)) result = null;
		else if (HOLIDAYS.contains(start)) {
			// 休日の場合、切符はつねに買わない
			// 常に買わない、としても最適解が見つかることは自明でないかも
			result = new Strat(getTicket(-2, start),
								calcMinimumFare(nextDay(start), pvc));
		} else {
			// 平日の場合
			int minFare = Integer.MAX_VALUE;
			
			// start の日から始まる切符の各パターンを計算
			// 年休が残っている場合、年休(-1)も試す
			// なければ買う(0～)
			int type = (paidVacationCount > 0)?-1:0;
			
			for (; type < 4; type++) {
				paidVacationCount = (type == -1)? pvc - 1:pvc; // -1 は年休利用
				Ticket nextTicket = getTicket(type, start);
				// 切符の end 以降は、calcMinimumFare が最小を返すので
				// 再帰的に呼ぶ
				Strat nextStrat = new Strat(nextTicket,
									calcMinimumFare(nextDay(nextTicket.end), paidVacationCount));
				if (nextStrat.fare < minFare) {
					minFare = nextStrat.fare;
					result = nextStrat;
				}
			}
		}
		if (r == null) r = new HashMap<Integer, Strat>();
		r.put(pvc, result);
		minimumStrat.put(start, r);
		return result;
	}
	
	/**
	 * 指定された日付が END を超えたら true を返却します
	 */
	private static boolean isFinished(Calendar target) {
		return (target.compareTo(END) > 0);
	}
	
	/**
	 * 指定された文字列形式の日付を表す Calendar を生成します。
	 */
	private static Calendar getCalendar(String format) {
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(format));
			return c;
		} catch (java.text.ParseException pe) {
			throw new IllegalArgumentException(pe);
		}
	}
	/**
	 * 指定された type, 開始日付の Ticket を返却します。
	 *
	 * @param	type	券種
	 * @param	start	開始日付
	 * @return	指定の Ticket
	 */
	private static Ticket getTicket(int type, Calendar start) {
		Ticket result = new Ticket();
		result.type		= type;
		result.start	= start;
		
		switch (type) {
		case -2: // 休日
		case -1: // 年休
			result.end	= start;
			result.fare	= 0;
			break;
		case 0: // 片道切符
			result.end	= start;
			result.fare	= 720;
			break;
		case 1: // 1ヵ月定期
			result.end	= nextMonth(start, 1);
			result.fare	= 13500;
			break;
		case 2: // 3ヵ月定期
			result.end	= nextMonth(start, 3);
			result.fare	= 38480;
			break;
		case 3: // 6ヵ月定期
			result.end	= nextMonth(start, 6);
			result.fare	= 72900;
			break;
		default:
			throw new IllegalArgumentException("不正な type : " + type +
						"type = 0,1,2,3 でなければなりません");
		}
		return result;
	}
	
	/**
	 * 指定された日付の翌日を返す。
	 */
	private static Calendar nextDay(Calendar target) {
		Calendar result = (Calendar)target.clone();
		result.add(DAY_OF_MONTH, 1);
		return result;
	}
	
	/**
	 * 指定された値だけ月をずらす。また、ずらした日が同一日であったら
	 * 1日減らす。(定期の最終日とする)
	 */
	private static Calendar nextMonth(Calendar target, int amount) {
		Calendar result = (Calendar)target.clone();
		result.add(MONTH, amount);
		if (target.get(DAY_OF_MONTH) == result.get(DAY_OF_MONTH))
			result.add(DAY_OF_MONTH, -1);
		
		return result;
	}
	
	private static String format(Calendar c) {
		return sdf.format(c.getTime());
	}
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		Strat s = calcMinimumFare(START, 20);
		System.out.println(s);
		System.out.println("総額：" + s.fare);
	}
}

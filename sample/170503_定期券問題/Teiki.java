import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.Calendar.*;

/**
 * 問：
 * 2017年4月1日から2018年3月31までの、京王八王子駅から新宿駅までの通勤費の
 * 最小値とその方法を、以下の条件のもとで求めよ。
 * 
 * ・土日、国民の祝日、8/14(月)~8/18(金)、12/29(金)~1/3(水)は通勤しない。
 * ・それ以外の日は毎日、一日1往復する。
 * ・交通費支払方法は以下の4通り
 * ①	片道切符：360円
 * ②	1カ月定期券　13,500円
 * ③	3カ月定期券　38,480円
 * ④	6カ月定期券　72,900円
 * 
 * （注）定期券の有効期間は、有効最終月の有効開始日と同じ日の１日前までとなる。
 * ただし ・上記の日付が存在しない場合はその月の月末まで
 * ・有効開始日の日付が１日の場合は有効最終月の月末まで
 *  ＜有効期限の例＞
 *  有効開始日　　　　１か月　　　３か月　　　６か月
 * 2016/06/01　→　2016/06/30　2016/08/31　2016/11/30
 *  2016/08/31　→　2016/09/30　2016/11/30　2017/02/28
 *  2016/10/10　→　2016/11/09　2017/01/09　2017/04/09
 *  2016/12/01　→　2016/12/31　2017/02/28　2017/05/31
 *  2017/01/31　→　2017/02/28　2017/04/30　2017/07/30
 */
public class Teiki {
	
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
	
	private static Map<Calendar, Strat> minimumStrat;
	static {
		minimumStrat = new HashMap<Calendar, Strat>();
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
			case -1:	sb.append(" 休日　"); break;
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
	 * 再帰的に最小金額を与える Strat を計算する。
	 *
	 * @param	start	開始年月日
	 * @return	最小金額を与える Strat
	 */
	static Strat calcMinimumFare(Calendar start) {
		Strat result = minimumStrat.get(start);
		if (result != null) return result; // 計算済み
		
		if (isFinished(start)) result = null;
		else if (HOLIDAYS.contains(start)) {
			// 休日の場合、切符はつねに買わない
			result = new Strat(getTicket(-1, start),
								calcMinimumFare(nextDay(start)));
		} else {
			// 一般の場合
			int minFare = Integer.MAX_VALUE;
			
			// start の日から始まる切符の各パターンを計算
			for (int type = 0; type < 4; type++) {
				Ticket nextTicket = getTicket(type, start);
				// 切符の切れる日以降は、calcMinimumFare が最小
				Strat nextStrat = new Strat(nextTicket,
									calcMinimumFare(nextDay(nextTicket.end)));
				if (nextStrat.fare < minFare) {
					minFare = nextStrat.fare;
					result = nextStrat;
				}
			}
		}
		minimumStrat.put(start, result);
		return result;
	}
	
	private static boolean isFinished(Calendar target) {
		return (target.compareTo(END) > 0);
	}
	
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
		case -1: // 休日
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
	
	private static Calendar nextDay(Calendar target) {
//System.out.println("nextDay : " + target.getTime());
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
		Strat s = calcMinimumFare(START);
		System.out.println(s);
		System.out.println("総額：" + s.fare);
	}
}

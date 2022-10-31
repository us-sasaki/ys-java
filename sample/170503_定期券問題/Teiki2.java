import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.Calendar.*;

/**
 * ������ŁA20���܂ŔN�x������ꍇ
 */
public class Teiki2 {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private static final Calendar START = getCalendar("2017/04/01");
	private static final Calendar END	= getCalendar("2018/03/31");
	
	/** �x���̃��X�g */
	private static final Set<Calendar>	HOLIDAYS;
	static {
		HOLIDAYS = new HashSet<Calendar>();
		
		// �y����o�^
		for (Calendar c = START; !isFinished(c); c = nextDay(c)) {
			int day = c.get(DAY_OF_WEEK);
			if (day == SATURDAY || day == SUNDAY) {
				HOLIDAYS.add( c ); // c �͖���V�����C���X�^���X
			}
		}
		
		// ���̓�(�j���Ȃ�)
		HOLIDAYS.add( getCalendar("2017/05/03") );
		HOLIDAYS.add( getCalendar("2017/05/04") );
		HOLIDAYS.add( getCalendar("2017/05/05") );
		HOLIDAYS.add( getCalendar("2017/07/17") ); // �C�̓�
		HOLIDAYS.add( getCalendar("2017/08/11") ); // �R�̓�
		HOLIDAYS.add( getCalendar("2017/08/14") ); // �ċx��
		HOLIDAYS.add( getCalendar("2017/08/15") );
		HOLIDAYS.add( getCalendar("2017/08/16") );
		HOLIDAYS.add( getCalendar("2017/08/17") );
		HOLIDAYS.add( getCalendar("2017/08/18") );
		HOLIDAYS.add( getCalendar("2017/09/18") ); // �h�V�̓�
		HOLIDAYS.add( getCalendar("2017/10/09") ); // �̈�̓�
		HOLIDAYS.add( getCalendar("2017/11/03") ); // �����̓�
		HOLIDAYS.add( getCalendar("2017/11/23") ); // �ΘJ���ӂ̓�
		HOLIDAYS.add( getCalendar("2017/12/29") ); // �����x��
		HOLIDAYS.add( getCalendar("2018/01/01") );
		HOLIDAYS.add( getCalendar("2018/01/02") );
		HOLIDAYS.add( getCalendar("2018/01/03") );
		HOLIDAYS.add( getCalendar("2018/01/08") ); // ���l�̓�
		HOLIDAYS.add( getCalendar("2018/02/12") );
		HOLIDAYS.add( getCalendar("2018/03/21") );
	}
	
	/**
	 * (���t, �c�N�x)��Strat �̎ʑ��B
	 * i.e. ���t�A�c�N�x�ɑ΂���œK�� Strat ���i�[�����B
	 * ��x�v�Z�������̂��X�g�b�N���邽�߂ɗ��p(������)�B
	 */
	private static Map<Calendar, Map<Integer, Strat>> minimumStrat;
	static {
		minimumStrat = new HashMap<Calendar, Map<Integer, Strat>>();
	}
	
	/**
	 * ������Ȃǂ̂P�񕪂̌���\���N���X
	 */
	static class Ticket {
		int type; // -1..�x�� 0.. 1��, 1.. 1����, 2.. 3����, 3..6����
		Calendar start; // �܂�
		Calendar end; // �܂�
		int fare;
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(sdf.format(start.getTime()));
			sb.append('�`');
			sb.append(sdf.format(end.getTime()));
			
			switch (type) {
			case -2:	sb.append(" �x���@"); break;
			case -1:	sb.append(" �N�x�@"); break;
			case  0:	sb.append(" �Г�x2"); break;
			case  1:    sb.append(" �P����"); break;
			case  2:	sb.append(" �R����"); break;
			case  3:	sb.append(" �U����"); break;
			default:	sb.append(" �����@");
			}
			sb.append(" \\");
			sb.append(fare);
			return sb.toString();
		}
	}
	
	/**
	 * ��������\���N���X
	 */
	static class Strat {
		List<Ticket> seq;
		int fare;
		
		/**
		 * �w�肳�ꂽ Ticket �𔃂��A�������� follow �̔�����������
		 * Strat ���쐬���܂��B
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
	 * �ŏ����z��^���� Strat ���v�Z���܂��B
	 * �ċA�Ăяo���𗘗p���Ă��܂��B
	 *
	 * @param	start	�J�n�N����
	 * @param	paidVacationCount	�c�N�x����
	 * @return	�ŏ����z��^���� Strat
	 */
	static Strat calcMinimumFare(Calendar start, int paidVacationCount) {
		Map<Integer, Strat> r = minimumStrat.get(start);
		Strat result = null;
		
		if (r != null) {
			result = r.get(paidVacationCount);
			if (result != null) return result; // �v�Z�ς�
		}
		
		int pvc = paidVacationCount;
		if (isFinished(start)) result = null;
		else if (HOLIDAYS.contains(start)) {
			// �x���̏ꍇ�A�ؕ��͂˂ɔ���Ȃ�
			// ��ɔ���Ȃ��A�Ƃ��Ă��œK���������邱�Ƃ͎����łȂ�����
			result = new Strat(getTicket(-2, start),
								calcMinimumFare(nextDay(start), pvc));
		} else {
			// �����̏ꍇ
			int minFare = Integer.MAX_VALUE;
			
			// start �̓�����n�܂�ؕ��̊e�p�^�[�����v�Z
			// �N�x���c���Ă���ꍇ�A�N�x(-1)������
			// �Ȃ���Δ���(0�`)
			int type = (paidVacationCount > 0)?-1:0;
			
			for (; type < 4; type++) {
				paidVacationCount = (type == -1)? pvc - 1:pvc; // -1 �͔N�x���p
				Ticket nextTicket = getTicket(type, start);
				// �ؕ��� end �ȍ~�́AcalcMinimumFare ���ŏ���Ԃ��̂�
				// �ċA�I�ɌĂ�
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
	 * �w�肳�ꂽ���t�� END �𒴂����� true ��ԋp���܂�
	 */
	private static boolean isFinished(Calendar target) {
		return (target.compareTo(END) > 0);
	}
	
	/**
	 * �w�肳�ꂽ������`���̓��t��\�� Calendar �𐶐����܂��B
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
	 * �w�肳�ꂽ type, �J�n���t�� Ticket ��ԋp���܂��B
	 *
	 * @param	type	����
	 * @param	start	�J�n���t
	 * @return	�w��� Ticket
	 */
	private static Ticket getTicket(int type, Calendar start) {
		Ticket result = new Ticket();
		result.type		= type;
		result.start	= start;
		
		switch (type) {
		case -2: // �x��
		case -1: // �N�x
			result.end	= start;
			result.fare	= 0;
			break;
		case 0: // �Г��ؕ�
			result.end	= start;
			result.fare	= 720;
			break;
		case 1: // 1�������
			result.end	= nextMonth(start, 1);
			result.fare	= 13500;
			break;
		case 2: // 3�������
			result.end	= nextMonth(start, 3);
			result.fare	= 38480;
			break;
		case 3: // 6�������
			result.end	= nextMonth(start, 6);
			result.fare	= 72900;
			break;
		default:
			throw new IllegalArgumentException("�s���� type : " + type +
						"type = 0,1,2,3 �łȂ���΂Ȃ�܂���");
		}
		return result;
	}
	
	/**
	 * �w�肳�ꂽ���t�̗�����Ԃ��B
	 */
	private static Calendar nextDay(Calendar target) {
		Calendar result = (Calendar)target.clone();
		result.add(DAY_OF_MONTH, 1);
		return result;
	}
	
	/**
	 * �w�肳�ꂽ�l�����������炷�B�܂��A���炵������������ł�������
	 * 1�����炷�B(����̍ŏI���Ƃ���)
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
		System.out.println("���z�F" + s.fare);
	}
}

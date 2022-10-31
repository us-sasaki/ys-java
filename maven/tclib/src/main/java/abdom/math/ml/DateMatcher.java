package abdom.math.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Date 型であるか判定するオブジェクトです。
 */
class DateMatcher implements TypeMatcher {
	private List<SimpleDateFormat> sdfs = new ArrayList<SimpleDateFormat>();
	{
		addSDF("yyyy-MM-dd HH:mm:ss");
		addSDF("yyyy/MM/dd HH:mm:ss");
		addSDF("yyyyMMdd HH:mm:ss");
		addSDF("yyyy-MM-dd HH:mm");
		addSDF("yyyy/MM/dd HH:mm");
		addSDF("yyyyMMdd HH:mm");
		addSDF("yyyy-MM-dd");
		addSDF("yyyy/MM/dd");
		addSDF("yyyyMMdd");
	}
	
	private Date lastParsed;
	private SimpleDateFormat lastParser;
	
	private void addSDF(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(true);
		sdfs.add(sdf);
	}
	
	public String getName() {
		if (lastParser == null) return "Date";
		return "Date" + lastParser.toPattern();
	}
	
	public boolean matches(String target) {
		if ("".equals(target)) return true;
		try {
			String head = target.substring(0, 2);
			Integer.parseInt(head);
		} catch (NumberFormatException nfe) {
			return false;
		} catch (StringIndexOutOfBoundsException sioobe) {
		}
		for (SimpleDateFormat sdf : sdfs) {
			try {
				lastParsed = sdf.parse(target);
				lastParser = sdf;
				return true;
			} catch (ParseException pe) {
			}
		}
		return false;
	}
	
	public Date toDate(String target) {
		for (SimpleDateFormat sdf : sdfs) {
			try {
				return sdf.parse(target);
			} catch (ParseException pe) {
			}
		}
		return null;
	}
}

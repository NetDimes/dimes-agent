package dimes.util.time;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeUtils {

	public static Timestamp getTimeStamp() {
		TimeZone tz = TimeZone.getDefault();
		int offset = tz.getRawOffset();
		return new Timestamp(System.currentTimeMillis() - offset); //for GMT
	}

	public static String getLocalTime() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(cal.getTime());
	}

}

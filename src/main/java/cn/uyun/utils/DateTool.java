package cn.uyun.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/** 
 * @ClassName: DateTool 
 * @Description: TODO(时间工具类) 
 * @author
 * @date 2017年5月8日17:21:18
 *  
 */
public class DateTool {
	private static final Logger logger = LoggerFactory.getLogger(DateTool.class);
	/**
	 * 功能：获取系统时间
	 * 
	 * @param strDateFormat
	 *            时间类格式：yyyy-MM-dd HH:mm:ss.SSS / yyyyMMdd /yyyyMMdd HH:mm:ss
	 * @return ：返回指定格式的系统时间字符串
	 */
	public static String getSysTime(String strDateFormat) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
		Date date = new Date();
		String currentSysTime = dateFormat.format(date);
		return currentSysTime;
	}

	/**
	 * 获取制定格式的日期字符串 时间类格式：yyyy-MM-dd HH:mm:ss.SSS / yyyyMMdd /yyyyMMdd HH:mm:ss
	 * 
	 * @param date
	 *            时间
	 * @param strDateFormat
	 *            时间类格式：yyyy-MM-dd HH:mm:ss.SSS / yyyyMMdd /yyyyMMdd HH:mm:ss
	 * @return
	 */
	public static String dateToString(Date date, String strDateFormat) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
		String strTime = dateFormat.format(date);
		return strTime;
	}

	/**
	 * 字符串转为日期
	 * 
	 * @param strDate
	 *            日期字符串
	 * @param strDateFormat
	 *            日期格式，时间类格式：yyyy-MM-dd HH:mm:ss.SSS / yyyyMMdd /yyyyMMdd
	 *            HH:mm:ss
	 * @return
	 */
	public static Date StringToDate(String strDate, String strDateFormat) {
		Date date = null;
		try {
			date = new SimpleDateFormat(strDateFormat).parse(strDate);
		} catch (Exception e) {
			logger.error("字符串转为日期出现错误："+e.toString());
		}
		return date;
	}

	/**
	 * 根据指定的侯，计算所属的时间列表
	 * 
	 * @param iYear:年
	 * @param iPen:候(中国候1-72候)
	 * @return
	 */
	public static List<Date> getZHTimesInTheFiveOfYear(int iYear, int iPen) {
		List<Date> ret = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);

		int month = (iPen - 1) / 6;
		int month_fiveInx = (iPen - 1) % 6;
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, iYear);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

		int dayCount = 5;
		if (month_fiveInx == 5)
			dayCount = days - 25;

		for (int i = 0; i < dayCount; i++) {
			cal.set(Calendar.DAY_OF_MONTH, month_fiveInx * 5 + 1);
			cal.add(Calendar.DAY_OF_MONTH, i);
			ret.add(cal.getTime());
		}

		return ret;
	}

	/**
	 * 根据指定时间获取所属侯的所有时间点,中国侯
	 *
	 * @param date
	 * @return
	 */
	public static List<Date> getZHTimesInTheFiveOfYear(Date date) {
		List<Date> ret = new ArrayList<Date>();
		int fiveOfYear = getZHFiveOfYear(date);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		int mon = cal.get(Calendar.MONTH);

		int tmp = fiveOfYear % 6;
		if (0 == tmp) {// 如果是第6侯,则为每个月前5侯后剩余的天数
			cal.set(Calendar.MONTH, mon);// 设置月份
			cal.set(Calendar.DAY_OF_MONTH, 5 * 5 + 1);// 设置侯的起始日期
			ret.add(cal.getTime());
			int iDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			for (int i = 1; i < (iDays - 5 * 5); i++) {
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
		} else {
			cal.set(Calendar.MONTH, mon);// 设置月份
			cal.set(Calendar.DAY_OF_MONTH, ((tmp - 1) * 5 + 1));// 设置侯的起始日期
			ret.add(cal.getTime());
			for (int i = 1; i < 5; i++) {
				cal.add(Calendar.DAY_OF_MONTH, 1);
				ret.add(cal.getTime());
			}
		}
		return ret;
	}

	/**
	 * 功能：获取指定时间是一年中的第几侯，世界候
	 *
	 * @param date
	 * @return
	 */
	public static int getFiveOfYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int day = cal.get(Calendar.DAY_OF_YEAR);
		if (isLeapYear(year)) // 润年，366天
		{
			if (day < 56) {
				return ((day - 1) / 5) + 1;
			} else if (day >= 56 && day <= 61) {
				return 12;
			} else {
				return 12 + (day - 61 - 1) / 5 + 1;
			}
		} else // 普通年份，365天
		{
			return ((day - 1) / 5) + 1;
		}
	}

	/**
	 * 功能：根据指定时间返回该时间属于第几季度 其中上一年12、1、2月为第4季，3、4、5月为第1季，6、7、8月为第2季，9、10、11月为第3季
	 *
	 * @param cal
	 *            :待确定季度的日期
	 * @return ：日期所属的季度
	 */
	public static int getSeasonOfYear(Calendar cal) {
		int ret = -1;
		int month = cal.get(Calendar.MONTH); /// 月份为0-11
		switch (month) {
		case 0:
		case 1:
			ret = 4;
			break;
		case 2:
		case 3:
		case 4:
			ret = 1;
			break;
		case 5:
		case 6:
		case 7:
			ret = 2;
			break;
		case 8:
		case 9:
		case 10:
			ret = 3;
			break;
		case 11:
			ret = 4;
			break;
		}
		return ret;
	}

	/**
	 * 功能：获取指定时间是一年中的第几侯,中国侯
	 *
	 * @param date：待获取候值的日期
	 * @return
	 */
	public static int getZHFiveOfYear(Date date) {
		int ret = -1;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int mon = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if (day > 25) {
			ret = 6;
		} else {
			ret = ((day - 1) / 5) + 1;
		}
		return ret + (mon * 6);
	}

	/**
	 * 根据指定年份和季度值,计算出该季度所包含的 第一天(起始月份中的第一天)和最后一天(最后月份在的最后一天)
	 *
	 * @param iYear
	 *            年份
	 * @param iSea
	 *            第几季(1-4)
	 * @return
	 */
	public static List<Date> getStartAndEndTimeInTheSeasonOfYear(int iYear, int iSea) {
		assert iSea < 0 || iSea > 4;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.YEAR, iYear);
		switch (iSea) {
		case 4:
			cal.add(Calendar.MONTH, -1); /// 前一年12月
			break;
		case 1:
			cal.set(Calendar.MONTH, 2); /// 3月
			break;
		case 2:
			cal.set(Calendar.MONTH, 5); /// 6月
			break;
		case 3:
			cal.set(Calendar.MONTH, 8); /// 9月
			break;
		}
		List<Date> ret = new ArrayList<Date>();
		ret.add(cal.getTime());
		for (int i = 1; i < 2; i++) {
			cal.add(Calendar.MONTH, 2);
			cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			ret.add(cal.getTime());
		}
		return ret;
	}

	/**
	 * 获取月的索引1-12
	 *
	 * @param obstime：时间字符串
	 *            格式yyyy-MM-dd HH:mm:ss
	 * @return iMon 1-12
	 */
	public static int getMonthIndex(String obstime) {
		obstime = obstime.substring(0, 10);
		Date date = StringToDate(obstime.trim(), "yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int iMon = cal.get(Calendar.MONTH) + 1;

		return iMon;
	}

	/**
	 * 功能：判断指定年份是否为润年
	 *
	 * @param year：指定年份
	 * @return true-是闰年 false-是平年
	 */
	public static boolean isLeapYear(int year) {
		return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
	}

	/**
	 * 功能：判断指定日期是一年的第几旬(全年36旬)
	 *
	 * @param cal：待判定的日期
	 * @return ：一年的第几旬(1-36)
	 */
	public static int getTenOfYear(Calendar cal) {
		int iRet = -1;
		int iMon = cal.get(Calendar.MONTH);
		int iDay = cal.get(Calendar.DAY_OF_MONTH);
		int iTen = 0;
		if (iDay < 11) // 第1旬
		{
			iTen = 1;
		} else if (iDay < 21) // 第2旬
		{
			iTen = 2;
		} else // 第3旬
		{
			iTen = 3;
		}

		iRet = iMon * 3 + iTen;
		return iRet;
	}

	/**
	 * 功能：获取某年指定候的第一天日期 世界候，从1月1日开始，每5天1个候，一直算到12月31日，共计73候。
	 * 其中，闰年将2月25日-3月1日算作一个候（6天）。
	 *
	 * @param iYear：指定年份
	 * @param iPen：指定候（1-73）
	 *
	 */
	public static Calendar getFirstDayOfPen(int iYear, int iPen) {
		Calendar cal = Calendar.getInstance();
		int iDaysOfYear = (iPen - 1) * 5 + 1; /// 第iPen候的第一天
		if (isLeapYear(iYear) && iPen > 12) /// 若为闰年且为12候后（即3月1日后），增加一天（即3月1号）
		{
			iDaysOfYear++;
		}
		cal.set(Calendar.YEAR, iYear);
		cal.set(Calendar.DAY_OF_YEAR, iDaysOfYear);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * 功能：获取某年指定候的所有日期 世界候，从1月1日开始，每5天1个候，一直算到12月31日，共计73候。
	 * 其中，闰年将2月25日-3月1日算作一个候（6天）。
	 *
	 * @param iYear：指定年份
	 * @param iPen：指定候（1-73）
	 *
	 */
	public static List<Calendar> getDayOfPen(int iYear, int iPen) {
		Calendar cal = Calendar.getInstance();
		int iDaysOfYear = (iPen - 1) * 5 + 1; /// 第iPen候的第一天
		if (isLeapYear(iYear) && iPen > 12) /// 若为闰年且为12候后（即3月1日后），增加一天（即3月1号）
		{
			iDaysOfYear++;
		}
		cal.set(Calendar.YEAR, iYear);
		cal.set(Calendar.DAY_OF_YEAR, iDaysOfYear);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		List<Calendar> calList = new ArrayList<Calendar>();
		int iDays = 5;
		if (isLeapYear(iYear) && iPen == 12) {
			iDays = 6;
		}
		Calendar calTime = null;
		for (int i = 0; i < iDays; i++) {
			calTime = Calendar.getInstance();
			calTime.setTime(cal.getTime());
			calTime.add(Calendar.DATE, i);
			calList.add(calTime);
		}
		return calList;
	}

	/**
	 * 获得一个传入日期0点0分0秒的时间
	 * 
	 * @param date
	 *            要获取哪天的0点0分0秒
	 * @author ZhangLu
	 * @return 传入日期0点0分0秒的时间
	 */
	public static Date getDateStartTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Calendar getDateStartTime(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * 和获得当天的0点0分0秒相反，该方法是获取一个日期的最后一个时间点，即23:59:59:999
	 * 
	 * @author ZhangLu
	 * @param date
	 * @return 一个日期的最后一个时间点，即23:59:59:999
	 */
	public static Date getDateEndTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Calendar getDateEndTime(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	/**
	 * unix时间戳转换成需要的指定格式的时间
	 * @author ZhangLu 2017年6月14日10:34:47
	 * @param unixTime unix时间戳 单位为毫秒
	 * @param format 要转换成的时间的格式
	 * @return 自定义格式的时间
	 */
	public static String unixDateFormat(Long unixTime,String format){
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(unixTime);
		SimpleDateFormat dateFmt=new SimpleDateFormat(format);
		return dateFmt.format(cal.getTime());
	}
	
	public static String unixDateFormat(String unixTimeStr,String format){
		 try {
			 long unixTime = Long.valueOf(unixTimeStr).longValue();
			 return unixDateFormat(unixTime, format);
		} catch (Exception e) {
			logger.error("时间转换出现错误");
			return null;
		}
	}
	
	/**
	 * 获取当前时间世界时日期
	 * 例:2017年8月2日09时51分36秒 取 世界时 时间为 2017年8月2日01时51分36秒 
	 * @author ZhangLu 2017年8月2日09:51:36
	 * @return 世界时Date
	 */
	public static Date getCurrWorldDate(){
		Calendar cal=Calendar.getInstance();
		return cal.getTime();
	}
	
	/** 
	 * getWorldDateByDate:(获取一个日期的世界时)
	 * 
	 * @author ZhangLu
	 * @param date 要获取世界时的时间
	 * @return 世界时Date
	 */ 
	public static Date getWorldDateByDate(Date date){
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR, -8);
		return cal.getTime();
	}
	
	/**
	 * 设置起始时间为当天0时,结束时间为当前时间
	 * 
	 * @author KunWang
	 * @return 
	 */
	public static Map<String, Object> getGoZero() {
		long to = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
		long from = calendar.getTimeInMillis();
		Map<String, Object> time = new HashMap<>();
		time.put("start", from);
		time.put("end", to);
		return time;
	}
	/**
	 * 设置起始时间为当天-7天,结束时间为当前时间
	 * 
	 * @author KunWang
	 * @return 
	 */
	public static Map<String, Object> getSevenDaysAgo() {
		Calendar cal = Calendar.getInstance();
		long to = cal.getTimeInMillis();
		cal.add(Calendar.DATE, -7);  
		long from = cal.getTimeInMillis();
		Map<String, Object> time = new HashMap<>();
		time.put("start", from);
		time.put("end", to);
		return time;
	}
	/**
	 * 设置起始时间为当天-30天,结束时间为当前时间
	 * 
	 * @author KunWang
	 * @return 
	 */
	public static Map<String, Object> getThirtyDaysAgo() {
		Calendar cal = Calendar.getInstance();
		long to = cal.getTimeInMillis();
		cal.add(Calendar.DATE, -30);  
		long from = cal.getTimeInMillis();
		Map<String, Object> time = new HashMap<>();
		time.put("start", from);
		time.put("end", to);
		return time;
	}

	public static Date getOldTime(int millis){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MILLISECOND,millis);
		return cal.getTime();
	}

	public static String getTime(Date date,int hour){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR,hour);
		return dateToString(cal.getTime(),"yyyy-MM-dd HH:mm");
	}

}

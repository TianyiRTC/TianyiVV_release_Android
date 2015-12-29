package com.sip.rtcclient.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jni.sip.JniLib;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.config.SysConfig;

public class CommFunc {

	private final static String DATE_FORMAT_FOR_SERVICE = "yyyy-MM-dd HH:mm:ss";
	private final static String DATE_FORMAT_FOR_RECORD = "yyyy-MM-dd HH:mm";
	private final static String DATE_TASK = "yyyy-MM-dd";
	private final static String DATE_TASK_TITLE = "yyyy/MM/dd";
	private final static String DATE_FORMAT_FOR_SERVICE_VISIT = "HH:mm:ss";
	private static final String DELIMITER = ":";

	
    public static String[] SplictStr(String str,String split)
    {
        String[] els = null;       
        els = str.split(split);
        return els;
    }

	/**
	 * 保存信息到SharedPreferences中 TODO preference_name 在 properties中配置
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void saveDataToSharedXml(Context context, String[] key,
			Object[] value) {
		SharedPreferences sharedPreferences_share = context
				.getSharedPreferences(
						SysConfig.SHARE_NAME,
						Context.MODE_PRIVATE);
		SharedPreferences.Editor shared_editor = sharedPreferences_share.edit();
		for (int i = 0; i < key.length; i++) {
			if (value[i] instanceof String) {
				shared_editor.putString(key[i], (String) value[i]);
			} else if (value[i] instanceof Boolean) {
				shared_editor.putBoolean(key[i], (Boolean) value[i]);
			}
		}
		shared_editor.commit();

	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	
	public static Long toLongDate(long date) {
		if (date != 0) { // kk:mm:ss
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm"); // ss
			String str = sdf.format(new Date(date));
			return Long.parseLong(str);
		}
		return date;

	}

	/**
	 * long型转换成日期(存库用)
	 * 
	 * @param date
	 * @return
	 */
	
	public static String toDate(long date) {
		if (date != 0) { // kk:mm:ss
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_FOR_SERVICE);
			return sdf.format(new Date(date));
		}
		return null;
	}

	/**
	 * 年月日时分格式转时间戳
	 * 
	 * @param dateFormat
	 * @return
	 */
	
	public static long dateToLong(String dateFormat) {
		if (dateFormat == null) {
			return 0;
		}
		try {
			if (dateFormat.length() > 12) {
				dateFormat = dateFormat.substring(0, 12);
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm"); // 201304251014
			df.setLenient(false);
			return df.parse(dateFormat).getTime();
		} catch (ParseException e) {
		    CommFunc.PrintLog(1,"dateToLong",MyApplication.getInstance().getString(
					R.string.exception_parse_data));
		}
		return 0;
	}

	/**
	 * long型转换成日期(通话记录显示)
	 * 
	 * @param date
	 * @return
	 */
	
	public static String toRecordDate(long date) {
		Calendar cal = Calendar.getInstance(Locale.CHINA);
		int Year = cal.get(Calendar.YEAR);
		int Month = cal.get(Calendar.MONTH);
		int Day = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.YEAR, Year);
		cal.set(Calendar.MONTH, Month);
		cal.set(Calendar.DAY_OF_MONTH, Day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long today = cal.getTimeInMillis();
		int NewDay = Day - 1;
		cal.set(Calendar.YEAR, Year);
		cal.set(Calendar.MONTH, Month);
		cal.set(Calendar.DAY_OF_MONTH, NewDay);
		long yesterday = cal.getTimeInMillis();
		cal.set(Calendar.YEAR, Year);
		cal.set(Calendar.MONTH, Month);
		cal.set(Calendar.DAY_OF_MONTH, Day + 1);
		long tomorrow = cal.getTimeInMillis();
		if (date != 0) { // kk:mm:ss
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_FOR_RECORD);
			String dateString = sdf.format(new Date(date));
			String backString = "";
			if (date > tomorrow) {
				backString = dateString.substring(5);
			} else if (date > today) {
				backString = MyApplication.getInstance().getString(
						R.string.str_today)
						+ " " + dateString.substring(11);
			} else if (date > yesterday) {
				backString = MyApplication.getInstance().getString(
						R.string.str_tomorrow)
						+ " " + dateString.substring(11);
			} else {
				backString = dateString.substring(5);
			}
			return backString;
		}
		return null;
	}

	/**
	 * long型转换成日期(存库用)
	 * 
	 * @param date
	 * @return
	 */
	
	public static String toServiceVisitDate(long date) {
		if (date != 0) { // kk:mm:ss
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_FOR_SERVICE_VISIT);
			return sdf.format(new Date(date));
		}
		return null;
	}

	/**
	 * 
	 * @param datestr
	 *            日期字符串
	 * @param day
	 *            相对天数，为正数表示之后，为负数表示之前
	 * @return 指定日期字符串n天之前或者之后的日期
	 */
	
	public static Calendar getBeforeAfterDate(String datestr, int day) {
		SimpleDateFormat df = new SimpleDateFormat(DATE_TASK);
		Date olddate = null;
		try {
			df.setLenient(false);
			olddate = new java.sql.Date(df.parse(datestr).getTime());
		} catch (ParseException e) {
			throw new RuntimeException(MyApplication.getInstance().getString(
					R.string.exception_parse_data));
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(olddate);

		int Year = cal.get(Calendar.YEAR);
		int Month = cal.get(Calendar.MONTH);
		int Day = cal.get(Calendar.DAY_OF_MONTH);

		int NewDay = Day + (day * 7);

		cal.set(Calendar.YEAR, Year);
		cal.set(Calendar.MONTH, Month);
		cal.set(Calendar.DAY_OF_MONTH, NewDay);

		return cal;
	}
	
	/**
	 * 获取呼叫开始日期
	 * @return
	 */
	public static String getStartDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date());
		return date;
	}
	
	/**
	 * 获取呼叫结束日期
	 * @return
	 */
	public static String getEndDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date());
		return date;
	}

	/**
	 * 获取呼叫开始时间
	 * @return
	 */
	public static String getStartTime() {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		String date = format.format(new Date());
		return date;
	}
	
	/**
	 * 获取呼叫结束时间
	 * @return
	 */
	public static String getEndTime() {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		String date = format.format(new Date());
		return date;
	}
	
	/**
	 * 年月日时分格式转时间戳
	 * @param dateFormat
	 * @return
	 */
	public static long dateToLang(String dateFormat){
		if (dateFormat == null) {
			return 0;
		}
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //2013-07-16 15:39:37  
			df.setLenient(false);
			return df.parse(dateFormat).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * 获取通话时长
	 * 暂为启用(备用)
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static long getTotalTime(String startTime, String endTime) {
		long time = dateToLang(endTime) - dateToLang(startTime);
		return time;
	}

	/**
	 * 获取Task开始执行时间
	 * 
	 * @param cal
	 * @return
	 */
	public static String getTaskDateFormat(Calendar cal) {
		return DateFormat.format(DATE_TASK, cal.getTimeInMillis()).toString();
	}

	/**
	 * 获取Task执行时间并格式化
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static String getTaskDateTitleFormat(long startTime, long endTime) {
		return DateFormat.format(DATE_TASK_TITLE, startTime).toString() + "--"
				+ DateFormat.format(DATE_TASK_TITLE, endTime).toString();
	}

	/**
	 * 获取Task停止时间
	 * 
	 * @param calendar
	 * @return
	 */
	public static long getWorkTaskDeadline(Calendar calendar) {
		Calendar mCalendar = Calendar.getInstance();
		int Year = calendar.get(Calendar.YEAR);
		int Month = calendar.get(Calendar.MONTH);
		int Day = calendar.get(Calendar.DAY_OF_MONTH);

		int NewDay;
		int week = calendar.get(Calendar.DAY_OF_WEEK); // 获取当前日期星期，英国那边的算法(周日算一周第一天)
		if (week > 4 || week == 1) {// 星期天为1
			NewDay = (12 - week) + Day;
		} else {
			NewDay = (5 - week) + Day;
		}
		mCalendar.set(Calendar.YEAR, Year);
		mCalendar.set(Calendar.MONTH, Month);
		mCalendar.set(Calendar.DAY_OF_MONTH, NewDay);
		mCalendar.set(Calendar.HOUR_OF_DAY, 0);
		mCalendar.set(Calendar.MINUTE, 0);
		mCalendar.set(Calendar.SECOND, 0);
		return mCalendar.getTimeInMillis();
	}

	/**
	 * 获取Task停止后重新开始执行的时间
	 * 
	 * @param calendar
	 * @return
	 */
	public static long getTaskStartTimeDeadline(Calendar calendar) {
		Calendar mCalendar = Calendar.getInstance();
		int Year = calendar.get(Calendar.YEAR);
		int Month = calendar.get(Calendar.MONTH);
		int Day = calendar.get(Calendar.DAY_OF_MONTH);

		int NewDay;
		int week = calendar.get(Calendar.DAY_OF_WEEK); // 获取当前日期星期，英国那边的算法(周日算一周第一天)
		if (week > 4 || week == 1) {
			NewDay = (16 - week) + Day;
		} else {
			NewDay = (9 - week) + Day;
		}

		mCalendar.set(Calendar.YEAR, Year);
		mCalendar.set(Calendar.MONTH, Month);
		mCalendar.set(Calendar.DAY_OF_MONTH, NewDay);
		mCalendar.set(Calendar.HOUR_OF_DAY, 0);
		mCalendar.set(Calendar.MINUTE, 0);
		mCalendar.set(Calendar.SECOND, 0);

		return mCalendar.getTimeInMillis();
	}

	/**
	 * 计算时间
	 * 
	 * @param len
	 * @return
	 */
	public static String computingTime(long len) {
		if (len <= 0) {
			return "00:00:00";
		}
		String time = "";
		int hour = (int) ((len / (3600)));
		int minute = (int) ((len / 60) % 60);
		int second = (int) (len % 60);
		if (hour > 0) {
			if (hour < 10) {
				time += "0" + hour + DELIMITER;
			} else {
				time += hour + DELIMITER;
			}
		} else {
			time += "00" + DELIMITER;
		}
		if (minute > 0) {
			if (minute < 10) {
				time += "0" + minute + DELIMITER;
			} else {
				time += minute + DELIMITER;
			}
		} else {
			time += "00" + DELIMITER;
		}

		if (second < 10) {
			time += "0" + second;
		} else {
			time += second;
		}
		return time;
	}

	/**
	 * 获取n天前的日期
	 * 
	 * @return
	 */
	
	public static String getDayAgoDate(int n) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

		Calendar cal = Calendar.getInstance(Locale.CHINA);

		int Year = cal.get(Calendar.YEAR);
		int Month = cal.get(Calendar.MONTH);
		int Day = cal.get(Calendar.DAY_OF_MONTH);

		int NewDay = Day - n;

		cal.set(Calendar.YEAR, Year);
		cal.set(Calendar.MONTH, Month);
		cal.set(Calendar.DAY_OF_MONTH, NewDay);

		return df.format(new Date(cal.getTimeInMillis()));
	}

	/**
	 * 判断email格式是否正确
	 * 
	 * @param email
	 * @return 正确：true 不正确为：false
	 */
	public static boolean isEmail(String email) {
		if (email == null || email.trim().equals("")) {
			return true;
		}
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}
	
	/**
	 * 图片灰度处理
	 * @param bitmap
	 * @return
	 */
	public static Bitmap convert2Gray(Drawable drawable){
		Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return convert2Gray(bitmap);
	}
	
	/**
	 * 图片灰度处理
	 * @param bitmap
	 * @return
	 */
	public static Bitmap convert2Gray(Bitmap bitmap)  {  
        /** 
         *  [ a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t ] When applied to a color [r, g, b, a],  
         *  the resulting color is computed as (after clamping)  
         *  R' = a*R + b*G + c*B + d*A + e; G' = f*R + g*G + h*B + i*A + j;  
         *  B' = k*R + l*G + m*B + n*A + o; A' = p*R + q*G + r*B + s*A + t;  
         */  
        ColorMatrix colorMatrix = new ColorMatrix();  
        /** 
         * Set the matrix to affect the saturation(饱和度) of colors.  
         * A value of 0 maps the color to gray-scale（灰阶）. 1 is identity 
         */  
        colorMatrix.setSaturation(0);  
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);  
          
        Paint paint = new Paint();  
        paint.setColorFilter(filter);  
          
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(result);  
        canvas.drawBitmap(bitmap, 0, 0, paint);  
        return result;  
    }  

	/**
	 * 判断字符串是否由数字组成
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	   private static Toast toast;
	    public static void DisplayToast(Context context, String str) {
	        if (toast == null) {//hesn 保持只显示最新的toast，其他的都取消
	            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
	        }else {
	            toast.setText(str);
	        }
	        toast.show();
	    }

	    public static void DisplayToast(Context context, int strId) {

	        if (toast == null) {
	            toast = Toast.makeText(context, context.getString(strId), Toast.LENGTH_SHORT);
	        }else {
	            toast.setText(strId);
	        }
	        toast.show();
	    }
	    public static void PrintLog(int level, String logtag, String description) {
	        // v d i w e��Ӧ 0,1,2,3,4
	        JniLib.Log(level+1, logtag, description);
//	        if (description == null)
//	            return;
//	        if(SysConfig.LOG_FILE==0)
//	            ConnectionLog.getInstance().log(level+" "+logtag+" "+description);
//	        switch (level) {
//	        case 0:
//	            Log.v(logtag, description);
//	            break;
//	        case 1:
//	            Log.d(logtag, description);
//	            break;
//	        case 2:
//	            Log.i(logtag, description);
//	            break;
//	        case 3:
//	            Log.w(logtag, description);
//	            break;
//	        case 4:
//	        default:
//	            Log.e(logtag, description);
//	            break;
//	        }
	    }

	/**
	 * 两个数组合并
	 * @param first
	 * @param second
	 * @return
	 */
	public static String[] concat(String[] first, String[] second) {
		String[] all = new String[first.length + second.length];
		System.arraycopy(first, 0, all, 0, first.length);
		System.arraycopy(second, 0, all, first.length, second.length);
		return all;
	}
    public static String FormatTime(long date) {
        if (date != 0) { // kk:mm:ss
            long l=date;
            long day=l/(24*60*60*1000);
            long hour=(l/(60*60*1000)-day*24);
            long min=((l/(60*1000))-day*24*60-hour*60);
            long s=(l/1000-day*24*60*60-hour*60*60-min*60);
            System.out.println(""+day+"天"+hour+"小时"+min+"分"+s+"秒");
            String time="hh:"+hour+"mm:"+min+"ss:"+s;
            return time;
         }
        return null;
    }
}

/**
 * 
 */
package com.example.padim.util;

import java.util.Calendar;

/**
 *日期时间类帮助 
 *@author zhuft
 *@version 1.0
 *2011-8-24
 */
public class DateUtil {
	
	public static void main(String[] args){
		System.out.println(getDateFileName());
	}
	
	/**
	 * 根据时间生成文件名字
	 * @return
	 */
	public static String getDateFileName(){
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		//int milSecond = c.get(Calendar.MILLISECOND);
		
		return "" + year + month + day + hour + minute + 
			"_" + second;
	}
	
	/**
	 * 返回当前时间
	 * @return
	 */
	public static long getTime(){
		Calendar c = Calendar.getInstance();
		return c.getTimeInMillis();
	}
	
	/**
	 * 返回设定的时间
	 * @return
	 */
	public static long getTime(int year, int month, int day){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		return c.getTimeInMillis();
	}
	
	/**
	 * 得到时间字符串
	 * 2011-8-16 (13:07)
	 * 8-16 (13:07)
	 * @param time
	 * @return
	 */
	public static String getTimeStr(long time){
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		c.setTimeInMillis(time);
		int setYear = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		if(setYear != year){
			return "" + setYear + "-" + month + "-" + day
			+ " (" + hour + ":" + minute + ")";
		}else{
			String minuteStr = String.valueOf(minute);
			if(minute < 10){
				minuteStr = "0" + minute;
			}
			return "" + month + "-" + day
			+ " (" + hour + ":" + minuteStr + ")";
		}
	}
	
	/**
	 * 得到时间字符串
	 * 2011年8月16日 13:07
	 * 8月16日 13:07
	 * @param time
	 * @return
	 */
	public static String getTimeStr2(long time){
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		c.setTimeInMillis(time);
		int setYear = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		if(setYear != year){
			return "" + setYear + "年" + month + "月" + day
			+ "日" + hour + "时" + minute + "分"+second+"秒";
		}else{
			String minuteStr = String.valueOf(minute);
			String secondStr = String.valueOf(second);
			if(minute < 10){
				minuteStr = "0" + minute;
			}
			if(second < 10){
				secondStr = "0" + second;
			}
			return "" + month + "月" + day
			+ "日" + hour + "时" + minuteStr + "分"+secondStr+"秒";
		}
	}
	
	/**
	 * 得到日期
	 * 2011-1-15
	 * @param time
	 * @return
	 */
	public static String getDateStr(long time){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		int setYear = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		return "" + setYear + "-" + month + "-" + day;
	}
	
}

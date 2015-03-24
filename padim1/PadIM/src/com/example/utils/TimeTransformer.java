package com.example.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**   
*    
* 项目名称：PadIM   
* 类名称：TimeTransformer   
* 类描述： 时间戳转换的工具类   
* 创建人：ystgx   
* 创建时间：2014年4月23日 下午9:43:33   
* @version        
*/
public class TimeTransformer {
	
	/**
	 * 将时间戳转换为带日期的时间
	 * @param time
	 * @return
	 */
	public static String longToTime(long time){
		String result = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		Date date = new Date(time);
		Date current = new Date();
		long t = current.getTime() - time;
		
//		long mill = (long)Math.ceil(t/1000);//多少秒前
//		long minute = (long)Math.ceil(t/60/1000.0f);//多少分钟前
//		long hour = (long)Math.ceil(t/60/60/1000.0f);//多少小时前
		long day = (long)Math.ceil(t/24/60/60/1000.0f);//多少天前
		
		if(day > 2)
			result = sdf.format(date);
		else if (2 == day)
			result = " 前天 " + date.getHours() + ":" + date.getMinutes();
		else if (1 == day)
			result = " 昨天 " + date.getHours() + ":" + date.getMinutes();
		else
			result = " 今天 " + date.getHours() + ":" + date.getMinutes();
		
		return result;
	}
	
	
	
	/**
	 * 将时间戳转换为日期
	 * @param time
	 * @return
	 */
	public static String longToDate(long time){
		String result = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
		
		Date date = new Date(time);
		Date current = new Date();
		long t = current.getTime() - time;
		
//		long mill = (long)Math.ceil(t/1000);//多少秒前
//		long minute = (long)Math.ceil(t/60/1000.0f);//多少分钟前
//		long hour = (long)Math.ceil(t/60/60/1000.0f);//多少小时前
		long day = (long)Math.ceil(t/24/60/60/1000.0f);//多少天前
		
		if(day > 2)
			result = sdf.format(date);
		else if (2 == day)
			result = " 前天 " ;
		else if (1 ==day)
			result = " 昨天 " ;
		else
			result = " 今天 " ;
		
		return result;
	}
	
	
	
	
	public static String longToHour(long time){
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date date = new Date(time);
		return sdf.format(date);
	}
	
	
}

/**
 * 
 */
package com.example.padim.util;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.example.padim.PadIMApplication;

/**
 * 帮助函数
 * 
 * @author zhuft
 * @version 1.0 2011-11-6
 */
public class PadImUtil {
	public static float scale;

	public static int dip2px(Context context, float dipValue) {
		if (scale == 0) {
			scale = context.getResources().getDisplayMetrics().density;
//			System.out.println("he scale="+scale);
		}
//		System.out.println("return scale="+(dipValue * scale + 0.5f));
		return (int) (dipValue * scale + 0.5f);
	}
	
	public static boolean isConnected(Activity activity){
		ConnectivityManager cManager=(ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cManager.getActiveNetworkInfo(); 
		  if (info != null && info.isAvailable()){ 
		       //do something 
		       //能联网 
		        return true; 
		  }else{ 
		       //do something 
		       //不能联网 
		        return false; 
		  } 
	}

	/**
	 * 生成唯一ID码，用户ID 1000000
	 * 
	 * @return
	 */
	public static int getMyId() {
		String imei = PhoneStateUtil.getIMEI(PadIMApplication.getInstance()
				.getApplicationContext()); 
		
		return Math.abs(imei.hashCode() % 10000);
	}

	public static String getMsgPersons(List<String> list) {
		Collections.sort(list); //这里若取消排序是为了使第一个是发起者，便于标记发起者
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(list.get(i));
		}
		return sb.toString();
	}

	public static String getOtherPersons(String except, String msgPersons) {
		String others = msgPersons.replace(except + ",", "").replace(
				"," + except, "");
		return others;
	}

	/**
	 * int to ip转换
	 * 
	 * @param i
	 * @return
	 */
	public static String intToIp(int i) {
		String ip = ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
				+ ((i >> 8) & 0xFF) + "." + (i & 0xFF);
		return ip;
	}

	/**
	 * 转换文件大小
	 * 
	 * @param fileS
	 * @return
	 */
	public static String formatFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = fileS + "B";
			// fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}
}

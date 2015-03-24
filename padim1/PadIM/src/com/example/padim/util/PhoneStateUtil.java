package com.example.padim.util;

import java.util.Random;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class PhoneStateUtil {
	public static String getIMEI(Context context) {
		String imei = "";
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService("phone");
			imei = telephonyManager.getDeviceId();
		} catch (Exception e) {
			e.printStackTrace();
			imei = "";
		}

		if (TextUtils.isEmpty(imei)) {
			imei = getWifiAddress(context);
		}

		return imei;
	}
	public static String getImsi(Context context) {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			String imsi = telephonyManager.getSubscriberId();
			return imsi;
		} catch (Exception e) {
			e.printStackTrace();
			return "imsierror";
		}
	}
	public static String models = "Desire S,HTC Wildfire,TD668,W830,LG-F160L,H998,MI 1S,Lenovo A789,R805,HUAWEI C8812E,lephone 1800,PULID F7,ZTE N855D,V926,generic,MI 1S,vivo S7,GT-I9100G,Philips W626,MI 2,Coolpad7295";
	public static final String devicenames = "sprd,motorola,K-Touch,ZTE,asus,MOT,LENOVO,ChangHong,DOOV,unknown,TIANYU,BBK,samsung,GiONEE,OPPO,YuLong,alps,HUAWEI,SMART PHONE,Sony,Conor,BESTSONNY,Xiaomi,MBX,G3EA3HQKJ,KLITON,HTC";

	public static String getRandomImei() {
		Random r = new Random();
		// http://en.wikipedia.org/wiki/Reporting_Body_Identifier
		String[] rbi = new String[] { "00", "01", "02", "03", "04", "05", "06",
				"07", "08", "09", "10", "30", "33", "35", "44", "45", "49",
				"50", "51", "52", "53", "54", "86", "91", "98", "99" };
		String imei = rbi[r.nextInt(rbi.length)];
		while (imei.length() < 14)
			imei += Character.forDigit(r.nextInt(10), 10);
		imei += getLuhnDigit(imei);
		return imei;
	}

	public static String getRandomModel() {
		Random r = new Random();
		String[] array = models.split(",");
		return array[r.nextInt(array.length)];
	}

	public static final String sims = "898600,898601,898603";

	public static String getRandomMANUFACTURER() {
		Random r = new Random();
		String[] array = devicenames.split(",");
		return array[r.nextInt(array.length)];
	}

	public static String getRandomSim() {
		Random r = new Random();
		String[] array = sims.split(",");
		return array[r.nextInt(array.length)];
	}

	private static char getLuhnDigit(String x) {
		// http://en.wikipedia.org/wiki/Luhn_algorithm
		int sum = 0;
		for (int i = 0; i < x.length(); i++) {
			int n = Character.digit(x.charAt(x.length() - 1 - i), 10);
			if (i % 2 == 0) {
				n *= 2;
				if (n > 9)
					n -= 9; // n = (n % 10) + 1;
			}
			sum += n;
		}
		return Character.forDigit((sum * 9) % 10, 10);
	}

	public static String getSim(Context context) {
		String sim = "";
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService("phone");
		sim = tm.getSimSerialNumber();
		return sim;
	}

	public static String getDeviceName(Context context) {
		String deviceName = "";
		try {
			if (TextUtils.isEmpty(deviceName))
				deviceName = Build.MANUFACTURER;
		} catch (Exception e) {
			e.printStackTrace();
			deviceName = "";
		}

		return deviceName;
	}

	public static String getWifiAddress(Context context) {
		String macAddress = "";
		try {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService("wifi");
			macAddress = wifiManager.getConnectionInfo().getMacAddress();
		} catch (Exception e) {
			e.printStackTrace();
			macAddress = "";
		}
		return macAddress;
	}

	public static String getMacAddress(Context context) {
		String macAddress = "";
		try {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService("wifi");
			macAddress = wifiManager.getConnectionInfo().getBSSID();
		} catch (Exception e) {
			e.printStackTrace();
			macAddress = "";
		}
		return macAddress;
	}

	public static String getSdkOsVersion() {
		String deviceOsVersion = "";
		try {
			deviceOsVersion = Build.VERSION.SDK;
		} catch (Exception e) {
			e.printStackTrace();
			deviceOsVersion = "";
		}

		return deviceOsVersion;
	}

	public static int getScreenWidth(Context context) {
		try {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager windowManager = (WindowManager) context
					.getSystemService("window");
			windowManager.getDefaultDisplay().getMetrics(dm);
			return dm.widthPixels;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int getScreenHeight(Context context) {
		try {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager windowManager = (WindowManager) context
					.getSystemService("window");
			windowManager.getDefaultDisplay().getMetrics(dm);
			return dm.heightPixels;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
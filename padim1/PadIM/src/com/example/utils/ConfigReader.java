package com.example.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.padim.R;
import com.example.padim.beans.DeviceItem;

/**
 * 
 * 项目名称：PadIM 类名称：ConfigReader 类描述：用于读取SD卡中的config配置文件数据，并进行解析 创建人：ystgx
 * 创建时间：2014年3月28日 上午9:55:01
 * 
 * @version
 */
public class ConfigReader {
	/**
	 * 联系人项目中字段的个数以及各项属性在解析 出来的属性数组中的位置
	 **/
	public static final int ATTR_NUM = 4;
	public static final int ATTR_IP = 0;
	public static final int ATTR_MAC = 1;
	public static final int ATTR_NAME = 2;
	public static final int ATTR_SEX = 3;
	/** config配置文件中的属性项数组，在配置信息的解析过程中匹配相关属性 **/
	public static final String ATTR_ARRAY[] = { "ip", "mac", "name", "sex" };

	private Context context;
	/** SD卡是否正常挂载 **/
	private boolean hasSDCard;
	private static final String DIR_NAME = "PadIM";
	private static final String FILE_NAME = "config.txt";
	private static String workPath;
	private static String configPath;
	/** SD卡目录 **/
	private String SD_PATH;
	private String localIp;

	public ConfigReader(Context context) throws Exception {
		this.context = context;
		hasSDCard = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (hasSDCard) {
			SD_PATH = android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			workPath = SD_PATH + File.separator + DIR_NAME;
			configPath = workPath + File.separator + FILE_NAME;
		}
		initFile();
		if ((localIp = getLocalIp()) == null) {
			Log.i("localip", "读取本机IP失败！！");
			Toast.makeText(context, "网络初始化失败，请检查WIFI网络是否可用！！",
					Toast.LENGTH_LONG).show();
			// throw new Exception("读取本机IP失败");
			localIp = "0.0.0.0";// 如果程序启动时读取本机ip失败，暂时将ip设置为该值，等待服务检测到联网后重新获取ip
		}
	}

	/**
	 * 检查SD卡挂载状态，并在指定工作目录中检查是否存在 config配置文件，如果没有，系统则创建默认配置
	 **/
	private void initFile() throws Exception {
		if (hasSDCard) {

			File dir = new File(workPath);
			if (!dir.exists())
				dir.mkdir();
			if (!(new File(configPath)).exists()) {
				InputStream is = context.getResources().openRawResource(
						R.raw.config);
				FileOutputStream fos = new FileOutputStream(configPath);
				byte buffer[] = new byte[1024];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
		}

		else {
			Toast.makeText(context, "配置文件初始化失败，请检查SD是否可用！", Toast.LENGTH_LONG)
					.show();
		}
	}

	/** 读取配置文件，获取配置信息 **/
	public void readConfig(ArrayList<DeviceItem> list) throws Exception {
		if (list == null) {
			Log.i("config", "设备列表对象初始化失败");
			Toast.makeText(context, "程序初始化失败！", Toast.LENGTH_LONG).show();
		}

		list.clear();
		String encoding = "GBK";
		File config = new File(configPath);

		if (config.isFile() && config.exists()) {
			Log.i("config", "config.exists");
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					config), encoding);
			BufferedReader buffer = new BufferedReader(read);
			String lineTxt = null;
			/** 按行读取字符串，对字符串进行简单的解析 **/

			while ((lineTxt = buffer.readLine()) != null) {
				if (lineTxt.startsWith("/**")) // 判断是否是注释行
					continue;
				if (lineTxt.length() == 0) // 判断是否是空行
					continue;
				/** 将读取到的配置行以";"进行分割，得到每一条配置的属性数组 **/
				String deviceAttr[] = lineTxt.toString().split(";");
				/**
				 * 如果得到的配置属性个数与预定的不一致，说明配置 文件中有错误， 抛出异常，停止解析
				 **/
				if (deviceAttr.length == ATTR_NUM) {
					/**
					 * 检验分割得到的属性数组中的项是否与预定的 属性对应，如果不对应，抛出异常，停止解析
					 **/
					for (int i = 0; i < deviceAttr.length; ++i) {
						if (!deviceAttr[i].startsWith(ATTR_ARRAY[i])) {
							Log.i("config", "配置文件编写格式有误");
							Toast.makeText(context, "配置文件编写格式有误，请检测配置文件！", 3000)
									.show();
							throw new Exception("属性名不匹配");
						}
						// 去除配置信息中“=”号之前的的内容，得到配置值
						else {
							deviceAttr[i] = deviceAttr[i]
									.substring(ATTR_ARRAY[i].length() + 1);
						}
					}
					// 将解析成功的值填充进对象中
					DeviceItem item = new DeviceItem();
					if (!localIp.equalsIgnoreCase(deviceAttr[ATTR_IP])) {
						item.setmIp(deviceAttr[ATTR_IP]);
						item.setmMac(deviceAttr[ATTR_MAC]);
						item.setmName(deviceAttr[ATTR_NAME]);
						item.setSex(deviceAttr[ATTR_SEX]);
						list.add(item);
					} else {
						item.setmIp(deviceAttr[ATTR_IP]);
						item.setmMac(deviceAttr[ATTR_MAC]);
						item.setmName(deviceAttr[ATTR_NAME]);
						item.setSex(deviceAttr[ATTR_SEX]); 
					}
				} else {
					Log.i("config", "config格式错误");
					Toast.makeText(context, "配置文件编写格式有误，请检测配置文件！", 3000).show();
				}
			}
			buffer.close();
			read.close();
		}

		else {
			Toast.makeText(context, configPath + "下找不到指定的配置文件！", 3000).show();
			throw new Exception(configPath + "下找不到指定的配置文件");
		}

	}

	private String getLocalIp() {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		long ipAddress = wifiInfo.getIpAddress();
		Log.d("localip", "int ip " + ipAddress);
		if (ipAddress == 0)
			return null;

		return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
				+ (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
	}

}

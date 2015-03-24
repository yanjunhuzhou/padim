package com.example.padim.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import android.os.Environment;

/**
 * 常量定义：系统action,命令协议等
 * 
 * @author zhuft
 * @version 1.0 2011-11-6
 */
public class Constant {

	public static Map<String, Integer> exts = new HashMap<String, Integer>();
	static {

	}

	// 自定义Action
	/** 自定义action头部,与包名一致 */
	public static final String actionHead = "com.mhz.duijiangji";
	/** 更新 */
	public static final String updateMyInformationAction = actionHead
			+ ".updateMyInformation";
	/** 用户信息更新了 */
	public static final String personHasChangedAction = actionHead
			+ ".personHasChanged";
	/** 消息更新 */
	public static final String hasMsgUpdatedAction = actionHead
			+ ".hasMsgUpdated";
	/** 接收发送文件请求 */
	public static final String receivedSendFileRequestAction = actionHead
			+ ".receivedSendFileRequest";
	/** 拒绝接收文件 */
	public static final String refuseReceiveFileAction = actionHead
			+ ".refuseReceiveFile";
	/** 远程用户拒绝接收文件 */
	public static final String remoteUserRefuseReceiveFileAction = actionHead
			+ ".remoteUserRefuseReceiveFile";
	/** 数据接收错误 */
	public static final String dataReceiveErrorAction = actionHead
			+ ".dataReceiveError";
	/** 数据发送错误 */
	public static final String dataSendErrorAction = actionHead
			+ ".dataSendError";
	/** 监听特殊命令的端口 */
	public static final String SpecialComdAction = actionHead
			+ ".specialComd";
	/** 询问当前那个Activity是激活状态 */
	public static final String whoIsAliveAction = actionHead + ".whoIsAlive";
	public static final String imAliveNow = actionHead + ".imAliveNow";
	public static final String remoteUserUnAliveAction = actionHead
			+ ".remoteUserUnAlive";
	public static final String fileSendStateUpdateAction = actionHead
			+ ".fileSendStateUpdate";
	public static final String fileReceiveStateUpdateAction = actionHead
			+ ".fileReceiveStateUpdate";
	public static final String RecordSendedAction = actionHead 
			+ ".RecordSended";
	
	/** 接收通话 */
	public static final String receivedTalkRequestAction = actionHead
			+ ".receivedTalkRequest";
	/** 接受通话请求 */
	public static final String acceptTalkRequestAction = actionHead
			+ ".acceptTalkRequest";
	/** 远程用户关闭通话 */
	public static final String remoteUserClosedTalkAction = actionHead
			+ ".remoteUserClosedTalk";

	/**  外部触发命令 */
	public static final String[] specialuserName = {"A", "B", "C","N"};
	public static final String[] specialCod = {"A1", "A2", "A3", "A4", "B1", "B2", "C1", "C2", "C3","N1","N2"};
	public static final String[] specialComd = { "1#直流配电板报警,href=www.baidu.com", "2#直接配电板报警", "1#逆变装置报警", "1#柴油机滑油压力过高", "II舱CO浓度高"
		, "III舱氧气浓度报警", "ENI滤波器报警", "高频通信终端报警", "***电阻箱报警", "请到会议室集合", "开饭"}; 
 
	// 系统Actionw
	public static final String bootCompleted = "android.intent.action.BOOT_COMPLETED";
	public static final String WIFIACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	public static final String ETHACTION = "android.intent.action.ETH_STATE";

	// other 其它定义，另外消息长度为60个汉字，utf-8中定义一个汉字占3个字节，所以消息长度为180bytes
	// 文件长度为30个汉字，所以总长度为90个字节
	public static final int bufferSize = 256;
	public static final int msgLength = 180;
	public static final int fileNameLength = 90;
	public static final int readBufferSize = 4096;// 文件读写缓存
	public static final byte[] pkgHead = "AND".getBytes();
	public static final int CMD80 = 80;
	public static final int CMD81 = 81;
	public static final int CMD82 = 82;
	public static final int CMD83 = 83;
	public static final int CMD_TYPE1 = 1;
	public static final int CMD_TYPE2 = 2;
	public static final int CMD_TYPE3 = 3;
	public static final int OPR_CMD1 = 1;
	public static final int OPR_CMD2 = 2;
	public static final int OPR_CMD3 = 3;
	public static final int OPR_CMD4 = 4;
	public static final int OPR_CMD5 = 5;
	public static final int OPR_CMD6 = 6;
	public static final int OPR_CMD7 = 7;
	public static final int OPR_CMD10 = 10;
	public static String MULTICAST_IP = "255.255.255.255";
	public static String downloadPath = Environment.getExternalStorageDirectory() + File.separator+
			"PadIM"+File.separator+"rev_file"+File.separator;
	public static final int PORT = 50002;
	public static final int AUDIO_PORT = 50005;
	public static final int SPECIALCOMD_PORT = 50008;

	// 其它定义
	public static final int FILE_RESULT_CODE = 1;
	public static final int SELECT_FILES = 1;// 是否要在文件选择器中显示文件
	public static final int SELECT_FILE_PATH = 2;// 文件选择器只显示文件夹
	// 文件选择状态保存
	public static TreeMap<Integer, Boolean> fileSelectedState = new TreeMap<Integer, Boolean>();

	public static final String NoGuanggaoEndTimeKey = "NoGuanggaoEndTimeKey";

	public static final String isFirst = "isFirst";

	public final static float TARGET_HEAP_UTILIZATION = 0.75f;

	/** 采样频率 */
	public final static int[] sampleRates = new int[] { 8000, 11025, 16000,
			22050, 32000, 44100, 48000 };
}

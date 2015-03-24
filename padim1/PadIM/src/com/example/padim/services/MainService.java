package com.example.padim.services;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.example.padim.PadIMApplication;
import com.example.padim.dbutils.MessageDao;
import com.example.padim.dbutils.MessageData;
import com.example.padim.dbutils.Person;
import com.example.padim.dbutils.PersonDao;
import com.example.padim.model.FileName;
import com.example.padim.model.FileState;
import com.example.padim.threads.AudioHandler;
import com.example.padim.threads.FileHandler;
import com.example.padim.util.ByteAndInt;
import com.example.padim.util.Constant;
import com.example.padim.util.DateUtil;
import com.example.padim.util.HandleTimer;
import com.example.padim.util.PadImUtil;

public class MainService extends Service {

	private PadIMApplication app = PadIMApplication.getInstance();

	/** 服务绑定器 */
	private ServiceBinder sBinder = new ServiceBinder();

	private WifiManager wifiManager = null;
	private ServiceBroadcastReceiver receiver = null;

	/** 通讯与协议解析模块 */
	private CommunicationBridge comBridge = null;
	/**监听特殊命令*/
	private ListentoSpecialComd spcialListen = null;
	/** 本机网络注册交互指令 */
	public byte[] regBuffer = new byte[Constant.bufferSize];
	/** 信息发送交互 */
	public byte[] msgSendBuffer = new byte[Constant.bufferSize];
	/** 文件发送交互指令 */
	public byte[] fileSendBuffer = new byte[Constant.bufferSize];
	/** 通话指令 */
	public byte[] talkCmdBuffer = new byte[Constant.bufferSize];
	public byte[] recvBuffer = new byte[Constant.bufferSize];

	public static boolean isrecording = true;

	@Override
	public IBinder onBind(Intent arg0) {
		return sBinder;
	}

	PadIMApplication application;

	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}

	@Override
	public void onRebind(Intent intent) {

	}
	
	@Override
	public void onCreate() {

	}

	Context mContext;

	@Override
	public void onStart(Intent intent, int startId) {
		application = PadIMApplication.getInstance();
		comBridge = new CommunicationBridge();// 启动socket连接
		comBridge.start();

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		new CheckNetConnectivity().start();// 侦测网络状态，获取IP地址

		regBroadcastReceiver();// 注册广播接收器
  
		spcialListen = new ListentoSpecialComd();
		spcialListen.start();
		new UpdateMe().start();// 向网络发送心跳包，并注册
		// timer.restart(1000, 1000);
		new CheckUserOnline().start();// 检查用户列表是否有超时用户
		sendPersonHasChangedBroadcast();// 通知有新用户加入或退出
		Log.e("", "Service 启动...");
	}

	public void refreshFriends() {
		comBridge.joinOrganization();
	}

	private class ListentoSpecialComd extends Thread {
		private DatagramSocket specialudp = null;	
		@Override
		public void run() {
			byte[] recv = new byte[2];
			try {
				specialudp = new DatagramSocket(Constant.SPECIALCOMD_PORT);
				while (null != specialudp && !specialudp.isClosed()) {
					 Log.e("", "wait specialcomnand...");
					for (int i = 0; i < 2; i++) {
						recv[i] = 0;
					}
					DatagramPacket rdp = new DatagramPacket(recv,
							recv.length);
					specialudp.receive(rdp);//阻塞等待一个数据包的到来
					String str = new String(recv);
					Log.i("msg", "str="+str);
					for(int i=0;i<Constant.specialCod.length;i++){
		              	if(str.equals(Constant.specialCod[i])){
		               		Intent intent = new Intent();
		               		intent.putExtra("special", i);
		               		Log.i("msg", "i="+i);
		               		intent.setAction(Constant.SpecialComdAction);
		               		sendBroadcast(intent);
		               		//dianl 
		               		MessageData data = new MessageData();
							long time = System.currentTimeMillis();
							data.setTime(time);
							data.setFrom(Constant.specialCod[i].substring(0, 1));
							data.setTo(application.me.getUserName());
							data.setContent(Constant.specialComd[i]);
							data.setMsgPersons("-1000,"+application.me.getUserId());
							MessageDao.getInstance().createOrUpdate(data);
							
							
		               		break;
		               	}
					}
                    
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				if (null != specialudp && !specialudp.isClosed()) {
					specialudp.close();
				}
				e.printStackTrace();
			}
		}
		
		private void release(){
			specialudp.close();
			specialudp = null;
		}
	}
	
	private class UpdateMe extends Thread {
		@Override
		public void run() {

			try {
				comBridge.joinOrganization();
				sleep(5000);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			try {
				comBridge.joinOrganization();
				sleep(10000);
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			while (!isStopUpdateMe) {
				try {
					comBridge.joinOrganization();
					sleep(20000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 服务绑定
	 * 
	 * @author zhuft
	 * @version 1.0 2011-11-6
	 */
	public class ServiceBinder extends Binder {
		public MainService getService() {
			return MainService.this;
		}
	}

	/**
	 * 检测网络连接状态,获得本机IP地址
	 * 
	 * @author zhuft
	 * @version 1.0 2011-11-6
	 */
	private class CheckNetConnectivity extends Thread {
		public void run() {
			try {
				if (!wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(true);
				}
//				int i=0,j=0;
				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							if (inetAddress.isReachable(1000) && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
								String localIp = inetAddress.getHostAddress()
										.toString();
								byte[] localIpBytes = inetAddress.getAddress();
								app.me.ipAddress = localIp;
//								Log.i("msg","无双自己ip="+app.me.ipAddress);
								System.arraycopy(localIpBytes, 0, regBuffer,
										44, 4);
							}
						}
					}
				}
				byte[] By = new byte[4];
				System.arraycopy(regBuffer,44, By, 0, 4);
				InetAddress targetIp = InetAddress
						.getByAddress(By);
//				Log.i("msg","自己localIpBytes="+targetIp);
				
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		};
	};

	/** 每隔10秒发送一个心跳包 */
	boolean isStopUpdateMe = false;

	HandleTimer timer = new HandleTimer() {

		@Override
		protected void onTime() {
			if (!isStopUpdateMe) {
				comBridge.joinOrganization();
			}
		}
	};

	/**
	 * 检测用户是否在线，如果超过15说明用户已离线，秒则从列表中清除该用户
	 * 
	 * @author zhuft
	 * @version 1.0 2011-11-6
	 */
	private class CheckUserOnline extends Thread {
		@Override
		public void run() {
			super.run();
			boolean hasChanged = false;
			while (!isStopUpdateMe) {
				if (app.onlineUsers.size() > 0) {
					Set<Integer> keys = app.onlineUsers.keySet();
					for (Integer key : keys) {
						// 用户收到注册应答后会设置对方的timeStamp为当前时间
						if (System.currentTimeMillis()
								- app.onlineUsers.get(key).loginTime > 20000) {
							app.onlineUsers.remove(key);
							app.onlineUsersId.remove(Integer.valueOf(key));
							Person p = PersonDao.getInstance()
									.getPersonByUserId(key + "");
							if (p != null) {
								p.onLine = false;
								PersonDao.getInstance().createOrUpdate(p);
							}

							hasChanged = true;
						}
					}
				}
				if (hasChanged) {
					sendPersonHasChangedBroadcast();
				}
				try {
					sleep(20000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 发送用户更新广播
	 */
	private void sendPersonHasChangedBroadcast() {
		Intent intent = new Intent();
		intent.setAction(Constant.personHasChangedAction);
		sendBroadcast(intent);
	}

	/**
	 * 注册广播接收器
	 */
	private void regBroadcastReceiver() {
		receiver = new ServiceBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.WIFIACTION);
		filter.addAction(Constant.ETHACTION);
		filter.addAction(Constant.updateMyInformationAction);
		filter.addAction(Constant.refuseReceiveFileAction);
		filter.addAction(Constant.imAliveNow);
		registerReceiver(receiver, filter);
	}

	/**
	 * 广播接收器处理类
	 * 
	 * @author zhuft
	 * @version 1.0 2011-11-6
	 */
	private class ServiceBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.WIFIACTION)
					|| intent.getAction().equals(Constant.ETHACTION)) {
				new CheckNetConnectivity().start();
				Log.i("msg","Constant.WIFIACTION");
			} else if (intent.getAction().equals(
					Constant.updateMyInformationAction)) {
				comBridge.getMyInfomation();
				comBridge.joinOrganization();
			} else if (intent.getAction().equals(
					Constant.refuseReceiveFileAction)) {
				comBridge.refuseReceiveFile();
			} else if (intent.getAction().equals(Constant.imAliveNow)) {

			}
		}
	}

	/**
	 * 录音
	 * 
	 * 
	 * 
	 * 
	 */
	
	/** 录音最长不超过60 s */
	boolean isTimeout = false;

	public File record() {
		
		String sendPath = Environment.getExternalStorageDirectory()
				+ File.separator+"PadIM"+File.separator+"voice"+File.separator+"send"+File.separator;
		File path = new File(sendPath);
		if (!path.exists()) {
			path.mkdirs();
		}
		PadIMApplication.getInstance().sendTime = System.currentTimeMillis();
		String recordName = DateUtil.getTimeStr2(PadIMApplication.getInstance().sendTime).trim();
		File file = new File(sendPath + recordName + ".pcm");
		if (file.exists()) {
			file.delete();
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create "
					+ file.toString());
		}
		
		
		// 录音
		try {
			OutputStream os = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			DataOutputStream dos = new DataOutputStream(bos);

			int bufferSize = AudioRecord.getMinBufferSize(
					PadIMApplication.getInstance().sampRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			Log.e("msg", "record缓冲区大小" + bufferSize);
			
			
			AudioRecord recorder = new AudioRecord(
					MediaRecorder.AudioSource.MIC,
					PadIMApplication.getInstance().sampRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize * 10);
			
			new Thread(new Runnable(){
				@Override
				public void run() {		
					final Timer timer = new Timer();
					final long start = System.currentTimeMillis();
					
					TimerTask task = new TimerTask() {
						public void run() {
							if(isrecording){
								if( (System.currentTimeMillis() - start) > 60000 ){//超过60s才停止录音
									isTimeout = true;
									timer.cancel();
									Log.i("msg","超时，停止录音");
								}else{
									Log.i("msg","录音未超时");
								}
							} else {
								isTimeout = false;
//								Log.i("msg","为录音");
							    timer.cancel();
							}
						}
					};
					timer.schedule(task, 1000,1000);
					}	
				}).start();
			
			recorder.startRecording();// 开始录音
			byte[] readBuffer = new byte[640];// 录音缓冲区

			int length = 0;
//			Log.i("msg","isTimeout="+isTimeout);
			while (isrecording && !isTimeout) {
				Log.i("msg","录音");
				length = recorder.read(readBuffer, 0, 640);
				if (length > 0) {
					dos.write(readBuffer,0,length);
				}
			}
			isTimeout = false;
//			Log.i("msg","aaaisTimeout="+isTimeout);
			recorder.stop();
			recorder.release();
			recorder = null;
			dos.close();
		} catch (Throwable t) {
			Log.e("AudioRecord", "Recording Failed");
		}
		return file;
	}

	/**
	 * 发送录音消息
	 * 
	 * 
	 */
	public void sendRecord(String Ip, File file) {
		final String userip = Ip;
		final String name = file.getName();
		final long size = file.length();
		final String filename = file.getAbsolutePath();
		Log.i("msg","name="+name);
		
		final byte[] sendFileCmd = new byte[Constant.bufferSize];
		for (int i = 0; i < Constant.bufferSize; i++)
			sendFileCmd[i] = 0;
		System.arraycopy(Constant.pkgHead, 0, sendFileCmd, 0, 3);
		sendFileCmd[3] = Constant.CMD82;
		sendFileCmd[4] = Constant.CMD_TYPE1;
		sendFileCmd[5] = Constant.OPR_CMD7;
		System.arraycopy(ByteAndInt.int2ByteArray(PadIMApplication
				.getInstance().me.getIntUserId()), 0, sendFileCmd, 6, 4);
//		Log.i("msg", "send Thread start");
		new Thread(){
			public void run() {
				Socket socket = null;
				OutputStream output = null;
				InputStream input = null;
				try{
					socket = new Socket(userip, Constant.PORT);
					byte[] fileNameBytes = name.getBytes();
					int fileNameLength = Constant.fileNameLength + 10;// 清除头文件包的文件名存储区域，以便写新的文件名
					for (int i = 10; i < fileNameLength; i++)
						sendFileCmd[i] = 0;
					System.arraycopy(fileNameBytes, 0, sendFileCmd, 10,
							fileNameBytes.length);// 把文件名放入头数据包
					System.arraycopy(
							ByteAndInt.longToByteArray(size), 0,
							sendFileCmd, 100, 8);
					output = socket.getOutputStream();// 构造一个输出流
					output.write(sendFileCmd);// 把头数据包发给对方
					output.flush();
					sleep(1000);// sleep 1秒钟，等待对方处理完
					byte[] readBuffer = new byte[Constant.readBufferSize];// 文件读写缓存
					input = new FileInputStream(new File(filename));// 打开一个文件输入流
					int readSize = 0;
					while (-1 != (readSize = input.read(readBuffer))) {// 循环把文件内容发送给对方
						output.write(readBuffer, 0, readSize);// 把内容写到输出流中发送给对方
						output.flush();
					}
					PadIMApplication.getInstance().voiceMsg = true;
					
				}catch (Exception e){
					e.printStackTrace();
				}finally {
					try {
						if (null != output)
							output.close();
						if (null != input)
							input.close();
						if (!socket.isClosed())
							socket.close();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			};
		}.start();

	}

	/**
	 * 发送信息
	 * 
	 * @param personId
	 * @param msg
	 */
	public void sendMsg(final int personId, final String msg) {
		comBridge.sendMsg(personId, msg);
	}

	/**
	 * 发送文件
	 * 
	 * @param personId
	 * @param files
	 */
	public void sendFiles(final int personId, final ArrayList<FileName> files) {
		comBridge.sendFiles(personId, files);

	}

	/**
	 * 接收文件
	 * 
	 * @param fileSavePath
	 */
	public void receiveFiles(final String fileSavePath) {
		comBridge.receiveFiles(fileSavePath);
	}

	/**
	 * 获得欲接收的文件名
	 * 
	 * @return
	 */
	public ArrayList<FileState> getReceivedFileNames() {
		return comBridge.getReceivedFileNames();
	}

	/**
	 * 获得欲发送的文件名
	 * 
	 * @return
	 */
	public ArrayList<FileState> getBeSendFileNames() {
		return comBridge.getBeSendFileNames();
	}

	/**
	 * 开始语音呼叫
	 * 
	 * @param personId
	 */
	public void startTalk(final int personId) {
		if (comBridge != null)
			comBridge.startTalk(personId);

	}

	/**
	 * 结束语音呼叫
	 * 
	 * @param personId
	 */
	public void stopTalk(final int personId) {
		if (comBridge != null)
			comBridge.stopTalk(personId);

	}

	/**
	 * 接受远程语音呼叫
	 * 
	 * @param personId
	 */
	public void acceptTalk(final int personId) {
		comBridge.acceptTalk(personId);

	}

	@Override
	public void onDestroy() {
		comBridge.release();
		spcialListen.release();
		unregisterReceiver(receiver);
		isStopUpdateMe = true;
		Log.e("", "Service 销毁...");
	}

	static int fileSenderUid = 0;

	private class CommunicationBridge extends Thread {
		private AudioHandler audioHandler = null;
		private ArrayList<FileState> beSendFileNames = new ArrayList();
		private FileHandler fileHandler = null;
		private String fileSavePath = null;

		private boolean isBusyNow = false;
		private boolean isRecording = true;
		private DatagramSocket broadcastSocket = null;
		// private DatagramSocket revSocket =null;
		private DatagramSocket udp = null;
		private int UDP_PORT = 50000;
		private ArrayList<FileState> receivedFileNames = new ArrayList();
		File recordingFile;
		private byte[] recvBuffer = new byte[256];
		private ArrayList<FileName> tempFiles = null;
		private int tempUid = 0;

		public CommunicationBridge() {

			initCmdBuffer();
			getMyInfomation();
			// allowMulticast();

			fileHandler = new FileHandler();
			fileHandler.start();

			audioHandler = new AudioHandler();
			audioHandler.start();

		}

		/**
		 * 初始化指令缓存
		 */
		private void initCmdBuffer() {
			// 初始化用户注册指令缓存
			for (int i = 0; i < Constant.bufferSize; i++) {
				regBuffer[i] = 0;
			}
			System.arraycopy(Constant.pkgHead, 0, regBuffer, 0, 3);
			regBuffer[3] = Constant.CMD80;
			regBuffer[4] = Constant.CMD_TYPE1;
			regBuffer[5] = Constant.OPR_CMD1;

			// 初始化信息发送指令缓存
			for (int i = 0; i < Constant.bufferSize; i++) {
				msgSendBuffer[i] = 0;
			}
			System.arraycopy(Constant.pkgHead, 0, msgSendBuffer, 0, 3);
			msgSendBuffer[3] = Constant.CMD81;
			msgSendBuffer[4] = Constant.CMD_TYPE1;
			msgSendBuffer[5] = Constant.OPR_CMD1;

			// 初始化发送文件指令缓存
			for (int i = 0; i < Constant.bufferSize; i++) {
				fileSendBuffer[i] = 0;
			}
			System.arraycopy(Constant.pkgHead, 0, fileSendBuffer, 0, 3);
			fileSendBuffer[3] = Constant.CMD82;
			fileSendBuffer[4] = Constant.CMD_TYPE1;
			fileSendBuffer[5] = Constant.OPR_CMD1;

			// 初始化通话指令
			for (int i = 0; i < Constant.bufferSize; i++) {
				talkCmdBuffer[i] = 0;
			}
			System.arraycopy(Constant.pkgHead, 0, talkCmdBuffer, 0, 3);
			talkCmdBuffer[3] = Constant.CMD83;
			talkCmdBuffer[4] = Constant.CMD_TYPE1;
			talkCmdBuffer[5] = Constant.OPR_CMD1;
		}

		/**
		 * 获得自已的相关信息
		 */
		public void getMyInfomation() {
			application.me = PersonDao.getInstance().getPersonByUserId(
					PadImUtil.getMyId() + "");
			// me.ipAddress = localIp; //MainService启动时候会设置此IP

			// 更新注册命令用户数据
			System.arraycopy(ByteAndInt.int2ByteArray(Integer
					.valueOf(application.me.getUserId())), 0, regBuffer, 6, 4);
			System.arraycopy(
					ByteAndInt.int2ByteArray(application.me.getHeadIconId()),
					0, regBuffer, 10, 4);
			for (int i = 14; i < 44; i++)
				regBuffer[i] = 0;// 把原来的昵称内容清空
			byte[] nickeNameBytes = application.me.getUserName().getBytes();
			System.arraycopy(nickeNameBytes, 0, regBuffer, 14,
					nickeNameBytes.length);

			// 更新通话命令用户数据
			System.arraycopy(ByteAndInt.int2ByteArray(Integer
					.valueOf(application.me.getUserId())), 0, talkCmdBuffer, 6,
					4);
			System.arraycopy(
					ByteAndInt.int2ByteArray(application.me.getHeadIconId()),
					0, talkCmdBuffer, 10, 4);
			for (int i = 14; i < 44; i++)
				talkCmdBuffer[i] = 0;// 把原来的昵称内容清空
			System.arraycopy(nickeNameBytes, 0, talkCmdBuffer, 14,
					nickeNameBytes.length);
		}

		// 打开组播端口，准备组播通讯
		@Override
		public void run() {
			super.run();
			// Log.i("msg","socket没启动吗");
			try {

				broadcastSocket = new DatagramSocket();
				udp = new DatagramSocket(UDP_PORT);
				// Log.i("msg","socket启动了");
				// String mode = PreferenceManager.getDefaultSharedPreferences(
				// PadIMApplication.getInstance()).getString("mode1", "0");
				// if (mode.equals("1")) {
				// broadcastSocket.setNetworkInterface(NetworkInterface
				// .getByInetAddress(InetAddress.getLocalHost()));
				// }
				// broadcastSocket.setLoopbackMode(false);
				// broadcastSocket.joinGroup(InetAddress.getByName(Constant.MULTICAST_IP));
				// broadcastSocket.setTimeToLive(1);
				// revSocket = new DatagramSocket(UDP_PORT);
				Log.e("", "Socket 启动...");
				// Log.e("", "broadcastSocket.isClosed()" +
				// broadcastSocket.isClosed());
				// Log.e("", "revSocket.isClosed()" + revSocket.isClosed());
				while (null != udp && !udp.isClosed()) {
					// Log.e("", "wait parsePackage...");
					for (int i = 0; i < Constant.bufferSize; i++) {
						recvBuffer[i] = 0;
					}
					DatagramPacket rdp = new DatagramPacket(recvBuffer,
							recvBuffer.length);
					// Log.i("msg","udp.isClosed()="+udp.isClosed());
					udp.receive(rdp);//阻塞等待一个数据包的到来
					String str = new String(recvBuffer);
					parsePackage(recvBuffer);

					// Log.e("", "parsePackageed");
				}
			} catch (Exception e) {
				try {
					Log.e("", "catch udp.isClosed()" + udp.isClosed());
					if (null != broadcastSocket && !broadcastSocket.isClosed()) {
						// broadcastSocket.leaveGroup(InetAddress.getByName(Constant.MULTICAST_IP));
						broadcastSocket.close();
					}
					if (null != udp && !udp.isClosed()) {
						udp.close();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}

		/**
		 * 解析接收到的数据包
		 * 
		 * @param pkg
		 */
		private void parsePackage(byte[] pkg) {
			int CMD = pkg[3];// 命令字
			int cmdType = pkg[4];// 命令类型
			int oprCmd = pkg[5];// 操作命令

			// 获得用户ID号
			byte[] uId = new byte[4];
			System.arraycopy(pkg, 6, uId, 0, 4);
			int userId = ByteAndInt.byteArray2Int(uId);
		
			switch (CMD) {
			case Constant.CMD80:
				switch (cmdType) {
				case Constant.CMD_TYPE1:
					// 如果该信息不是自己发出则给对方发送回应包,并把对方加入用户列表
					if (userId == application.me.getIntUserId()) {
						// Log.e("", "收到自己的注册请求");
					}
					if (userId != application.me.getIntUserId()) {
						Person p = updatePerson(userId, pkg);
						// Log.e("", "收到注册请求:" + p.getUserName() + " " + userId
						// + "---自己:"
						// + application.me.getUserName());
						// 发送应答包
						byte[] ipBytes = new byte[4];// 获得请求方的ip地址
						
						System.arraycopy(pkg, 44, ipBytes, 0, 4);

						try {
							InetAddress targetIp = InetAddress
									.getByAddress(ipBytes);
							Log.i("msg","targetIp="+targetIp);
							regBuffer[4] = Constant.CMD_TYPE2;// 把自己的注册信息修改成应答信息标志，把自己的信息发送给请求方
							DatagramPacket dp = new DatagramPacket(regBuffer,
									Constant.bufferSize, targetIp, UDP_PORT);
							broadcastSocket.send(dp);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				case Constant.CMD_TYPE2:
					if (userId != application.me.getIntUserId()) {
						Person p = updatePerson(userId, pkg);
						 Log.e("", "收到注册应答包:" + p.getUserName() + " " + userId
						 + "---自己:"
						 + application.me.getUserName());
					}
					break;
				case Constant.CMD_TYPE3:
					if (userId != application.me.getIntUserId()) {
						application.onlineUsers.remove(userId);
						application.onlineUsersId.remove(Integer
								.valueOf(userId));
						Person p = PersonDao.getInstance().getPersonByUserId(
								userId + "");
						if (p != null) {
							p.setOnLine(false);
						}
						Log.e("", "收到注销命令" + p.getUserName());
						PersonDao.getInstance().createOrUpdate(p);
						sendPersonHasChangedBroadcast();
					}
					break;
				}
				break;
			case Constant.CMD81:// 收到信息
				switch (cmdType) {
				case Constant.CMD_TYPE1:
					Log.i("msg", "收到发来的消息呀");
					List<MessageData> messages = null;
					if (application.msgContainer.containsKey(userId)) {
						messages = application.msgContainer.get(userId);
					} else {
						messages = new ArrayList<MessageData>();
					}
					byte[] msgBytes = new byte[Constant.msgLength];
					System.arraycopy(pkg, 10, msgBytes, 0, Constant.msgLength);
					String msgStr = new String(msgBytes).trim();
					MessageData data = new MessageData();
					long time = System.currentTimeMillis();
					data.setTime(time);
					Log.i("msg", "msgStr="+msgStr);
					if(PadIMApplication.getInstance().isVoice){ //群组聊天时，msgStr的值为语音消息+接受者列表，形如“语音消息#1449,2075,4087,7546”
						PadIMApplication.getInstance().recTime = time;
						Log.i("msg", "recTime="+PadIMApplication.getInstance().recTime);
						data.setPlay(false);
						while (!PadIMApplication.getInstance().voiceStore); //循环等待文件已存储好，因为在本地还有一个改文件名字的过程
						PadIMApplication.getInstance().voiceStore = false; //恢复初值
						PadIMApplication.getInstance().isVoice = false;
					}else{
						data.setPlay(true);
					}
					
					messages.add(data);
					application.msgContainer.put(userId, messages);
					data.setFrom(userId + "");

					String[] array = msgStr.split("#");
					String msgPersons;
					String to;
					if (array.length == 2) {// 第二个位置保存的是会话的所有人userId，用逗号分割
						msgPersons = array[1];
						to = PadImUtil.getOtherPersons(userId + "", msgPersons);
						List<MessageData> list = application.msgContainer
								.get(userId);
						if (list != null) {
							list.clear();
						}
						
					} else {// 一个人会话
						List<String> userIds = new ArrayList<String>();
						userIds.add(userId + "");
						userIds.add(PadIMApplication.getInstance().me
								.getUserId());
						to = PadIMApplication.getInstance().me.getUserId();
						msgPersons = PadImUtil.getMsgPersons(userIds);
						
						
						data.setContent(msgStr);
					}
					data.setTo(to);
					data.setMsgPersons(msgPersons);
					Log.i("msgs", "msgPersons"+msgPersons);
					MessageDao.getInstance().createOrUpdate(data);

					Intent intent = new Intent();
					intent.putExtra("msgPersons", msgPersons);
					intent.setAction(Constant.hasMsgUpdatedAction);
					intent.putExtra("userId", userId);
					intent.putExtra("msgCount", messages.size());
					intent.putExtra("msgContent", data.getContent());
					PadIMApplication.getInstance().sendBroadcast(intent);
					break;
				case Constant.CMD_TYPE2:
					break;
				}
				break;
			case Constant.CMD82:
				switch (cmdType) {
				case Constant.CMD_TYPE1:// 收到文件传输请求
					switch (oprCmd) {
					case Constant.OPR_CMD1:
						// 发送广播，通知界面有文件需要传输
						if (!isBusyNow) {
							// isBusyNow = true;
							fileSenderUid = userId;// 保存文件发送者的id号，以便后面若接收者拒绝接收文件时可以通过该id找到发送者，并给发送者发送拒绝接收指令
							Log.i("msg", "fileSenderUid=" + fileSenderUid);
							Person person = application.onlineUsers.get(Integer
									.valueOf(userId));
							Intent intent = new Intent();
							intent.putExtra("person", person);
							intent.setAction(Constant.receivedSendFileRequestAction);
							PadIMApplication.getInstance()
									.sendBroadcast(intent);
						} else {// 如果当前正在收发文件则向对方发送忙指令
							Person person = application.onlineUsers.get(Integer
									.valueOf(userId));
							fileSendBuffer[4] = Constant.CMD_TYPE2;
							fileSendBuffer[5] = Constant.OPR_CMD4;
							byte[] meIdBytes = ByteAndInt
									.int2ByteArray(application.me
											.getIntUserId());
							System.arraycopy(meIdBytes, 0, fileSendBuffer, 6, 4);
							try {
								DatagramPacket dp = new DatagramPacket(
										fileSendBuffer,
										Constant.bufferSize,
										InetAddress.getByName(person.ipAddress),
										UDP_PORT);
								broadcastSocket.send(dp);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
					case Constant.OPR_CMD5:// 接收对方传过来的文件名信息
						byte[] fileNameBytes = new byte[Constant.fileNameLength];
						byte[] fileSizeByte = new byte[8];
						System.arraycopy(pkg, 10, fileNameBytes, 0,
								Constant.fileNameLength);
						System.arraycopy(pkg, 100, fileSizeByte, 0, 8);
						FileState fs = new FileState();
						fs.fileName = new String(fileNameBytes).trim();
						fs.fileSize = Long.valueOf(ByteAndInt
								.byteArrayToLong(fileSizeByte));
						fileHandler.receivedFileNames.add(fs);
						break;
					}
					break;
				case Constant.CMD_TYPE2:
					switch (oprCmd) {
					case Constant.OPR_CMD2:// 对方同意接收文件
						fileHandler.startSendFile();
						Log.e("", "开始发送文件给远程用户...");
						break;
					case Constant.OPR_CMD3:// 对方拒绝接收文件
						Intent intent = new Intent();
						intent.setAction(Constant.remoteUserRefuseReceiveFileAction);
						PadIMApplication.getInstance().sendBroadcast(intent);
						Log.e("", "对方拒绝接收文件...");
						break;
					case Constant.OPR_CMD4:// 对方现在忙
						Log.e("", "对方忙...");
						break;
					}
					break;
				}
				break;
			case Constant.CMD83:// 83命令，语音通讯相关
				switch (cmdType) {
				case Constant.CMD_TYPE1:
					switch (oprCmd) {
					case Constant.OPR_CMD1:// 接收到远程语音通话请求
						Log.e("", " 接收到远程语音通话请求 ...");
						audioHandler.isStopTalk = false;
						Person person = application.onlineUsers.get(Integer
								.valueOf(userId));
						Intent intent = new Intent();
						intent.putExtra("person", person);
						intent.setAction(Constant.receivedTalkRequestAction);
						PadIMApplication.getInstance().sendBroadcast(intent);
						break;
					case Constant.OPR_CMD2:
						// 收到关闭指令，关闭语音通话
						Log.e("", "收到远程用户关闭通话消息 ...");
						audioHandler.isStopTalk = true;
						Person p = application.onlineUsers.get(Integer
								.valueOf(userId));
						Intent i = new Intent();
						i.putExtra("person", p);
						i.setAction(Constant.remoteUserClosedTalkAction);
						PadIMApplication.getInstance().sendBroadcast(i);
						break;
					}
					break;
				case Constant.CMD_TYPE2:
					switch (oprCmd) {
					case Constant.OPR_CMD1:
						// 被叫应答，开始语音通话
						if (!audioHandler.isStopTalk) {
							Log.e("", "准备和远程用户通话 ...");
							Person person = application.onlineUsers.get(Integer
									.valueOf(userId));
							audioHandler.audioSend(person);
							Log.i("msg", "person==" + person.getUserName());
						}
						break;
					}
					break;
				}
				break;
			}
		}

		/**
		 * 收到注册应答 更新或加用户信息到用户列表中
		 * 
		 * @param userId
		 * @param pkg
		 */
		private Person updatePerson(int userId, byte[] pkg) {
			Person person = new Person();
			getPerson(pkg, person);
			application.onlineUsers.put(userId, person);

			person.setOnLine(true);
			PersonDao.getInstance().createOrUpdate(person);

			if (!application.onlineUsersId.contains(Integer.valueOf(userId)))
				application.onlineUsersId.add(Integer.valueOf(userId));
			if (!application.allUser.contains(application.onlineUsers))
				application.allUser.add(application.onlineUsers);
			sendPersonHasChangedBroadcast();
			return person;
		}

		/**
		 * 发送用户更新广播
		 */
		private void sendPersonHasChangedBroadcast() {
			Intent intent = new Intent();
			intent.setAction(Constant.personHasChangedAction);
			PadIMApplication.getInstance().sendBroadcast(intent);
		}

		/**
		 * 关闭Socket连接
		 */
		public void release() {
			PadIMApplication app = PadIMApplication.getInstance();
			try {
				Log.e("", "发送注销命令 ...");
				if (app.onlineUsers.size() > 0) {
					Set<Integer> keys = app.onlineUsers.keySet();
					for (Integer key : keys) {
						Person temp = app.onlineUsers.get(key);

						regBuffer[4] = Constant.CMD_TYPE3;// 把命令类型修改成注消标志，并广播发送，从所有用户中退出
						DatagramPacket dp = new DatagramPacket(regBuffer,
								Constant.bufferSize,
								InetAddress.getByName(temp.ipAddress), UDP_PORT);
						broadcastSocket.send(dp);
					}
				}
				// broadcastSocket.leaveGroup(InetAddress.getByName(Constant.MULTICAST_IP));
				broadcastSocket.close();
				udp.close();

				udp = null;
				broadcastSocket = null;
				// if (multicastLock != null)
				// multicastLock.release();
				app.exit();
				Log.e("", "broadcastSocket关闭...");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				fileHandler.release();
				audioHandler.release();
			}
		}

		/**
		 * 分析数据包并获取一个用户信息
		 * 
		 * @param pkg
		 * @param person
		 */
		private void getPerson(byte[] pkg, Person person) {

			byte[] personIdBytes = new byte[4];
			byte[] iconIdBytes = new byte[4];
			byte[] nickeNameBytes = new byte[30];
			byte[] personIpBytes = new byte[4];

			System.arraycopy(pkg, 6, personIdBytes, 0, 4);
			System.arraycopy(pkg, 10, iconIdBytes, 0, 4);
			System.arraycopy(pkg, 14, nickeNameBytes, 0, 30);
			System.arraycopy(pkg, 44, personIpBytes, 0, 4);

			person.setUserId(ByteAndInt.byteArray2Int(personIdBytes) + "");
			person.setHeadIconId(ByteAndInt.byteArray2Int(iconIdBytes));
			person.setUserName(new String(nickeNameBytes).trim());
			person.setIpAddress(PadImUtil.intToIp(ByteAndInt
					.byteArray2Int(personIpBytes)));
			person.setLoginTime(System.currentTimeMillis());
		}

		
		
	
		/**
		 * 注册自己到网络中
		 */
		public void joinOrganization() {
			try {

				if (null != broadcastSocket && !broadcastSocket.isClosed()) {
//					Log.e("", "发心跳包****");


byte[] ipBytes = new byte[4];// 获得请求方的ip地址

System.arraycopy(regBuffer,44, ipBytes, 0, 4);
	InetAddress targetIp = InetAddress
			.getByAddress(ipBytes);
//	Log.i("msg","心跳 targetIp="+targetIp);
	            if(targetIp.toString().equals("/0.0.0.0")){
	            	Log.i("msg","不发送心跳包");
	            }else{
						regBuffer[4] = Constant.CMD_TYPE1;// 恢复成注册请求标志，向网络中注册自己
						DatagramPacket dp = new DatagramPacket(regBuffer,
								Constant.bufferSize,
								InetAddress.getByName(Constant.MULTICAST_IP),
								UDP_PORT);
						broadcastSocket.send(dp);
	            	}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 发送信息
		 * 
		 * @param personId
		 * @param msg
		 */
		public void sendMsg(int personId, String msg) {
			try {
				Person psn = application.onlineUsers.get(personId);
				if (null != psn) {
					Log.i("msg", "为什么");
					System.arraycopy(ByteAndInt.int2ByteArray(application.me
							.getIntUserId()), 0, msgSendBuffer, 6, 4);
					int msgLength = Constant.msgLength + 10;
					for (int i = 10; i < msgLength; i++) {
						msgSendBuffer[i] = 0;
					}
					Log.i("msg", "因为");
					byte[] msgBytes = msg.getBytes();
					System.arraycopy(msgBytes, 0, msgSendBuffer, 10,
							msgBytes.length);
					Log.i("msg", "爱情");
					DatagramPacket dp = new DatagramPacket(msgSendBuffer,
							Constant.bufferSize,
							InetAddress.getByName(psn.ipAddress), UDP_PORT);
					Log.i("msg", "psn.ipaddress=" + psn.ipAddress);

					broadcastSocket.send(dp);
					Log.i("msg", "发送信息1");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 向对方发送请求接收文件指令
		 * 
		 * @param personId
		 * @param files
		 */
		public void sendFiles(int personId, ArrayList<FileName> files) {
			Log.i("msg", "发送文件");
			Log.i("msg", "personId=" + personId);
			Log.i("msg", "files=" + files);
			Log.i("msg", "files.size()=" + files.size());
			if (personId > 0 && null != files && files.size() > 0) {
				try {
					fileHandler.tempUid = personId;
					fileHandler.tempFiles = files;
					Person person = application.onlineUsers.get(Integer
							.valueOf(fileHandler.tempUid));
					fileSendBuffer[4] = Constant.CMD_TYPE1;
					fileSendBuffer[5] = Constant.OPR_CMD5;
					byte[] meIdBytes = ByteAndInt.int2ByteArray(application.me
							.getIntUserId());
					System.arraycopy(meIdBytes, 0, fileSendBuffer, 6, 4);
					int fileNameLength = Constant.fileNameLength + 10;// 清除头文件包的文件名存储区域，以便写新的文件名
					// 把要传送的所有文件名传送给对方
					for (final FileName file : fileHandler.tempFiles) {
						// 收集生成要发送文件的文相关资料
						FileState fs = new FileState(file.fileSize, 0,
								file.getFileName());
						fileHandler.beSendFileNames.add(fs);

						byte[] fileNameBytes = file.getFileName().getBytes();
						for (int i = 10; i < fileNameLength; i++)
							fileSendBuffer[i] = 0;
						System.arraycopy(fileNameBytes, 0, fileSendBuffer, 10,
								fileNameBytes.length);// 把文件名放入头数据包
						System.arraycopy(
								ByteAndInt.longToByteArray(file.fileSize), 0,
								fileSendBuffer, 100, 8);
						DatagramPacket dp = new DatagramPacket(fileSendBuffer,
								Constant.bufferSize,
								InetAddress.getByName(person.ipAddress),
								UDP_PORT);
						broadcastSocket.send(dp);
						Log.i("msg", "发送文件相关资料");
					}
					// 对方发送请求接收文件指令
					fileSendBuffer[5] = Constant.OPR_CMD1;
					DatagramPacket dp = new DatagramPacket(fileSendBuffer,
							Constant.bufferSize,
							InetAddress.getByName(person.ipAddress), UDP_PORT);
					broadcastSocket.send(dp);
					Log.i("msg", "发送请求命令");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * 向对方响应同意接收文件指令
		 * 
		 * @param fileSavePath
		 */
		public void receiveFiles(String fileSavePath) {
			fileHandler.fileSavePath = fileSavePath;
			Person person = application.onlineUsers.get(Integer
					.valueOf(fileSenderUid));
			fileSendBuffer[4] = Constant.CMD_TYPE2;
			fileSendBuffer[5] = Constant.OPR_CMD2;
			byte[] meIdBytes = ByteAndInt.int2ByteArray(application.me
					.getIntUserId());
			System.arraycopy(meIdBytes, 0, fileSendBuffer, 6, 4);
			try {
				DatagramPacket dp = new DatagramPacket(fileSendBuffer,
						Constant.bufferSize,
						InetAddress.getByName(person.ipAddress), UDP_PORT);
				broadcastSocket.send(dp);
				Log.i("msg", "同意接收指令发送成功");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 向文件发送者发送拒绝接收文件指令
		 */
		public void refuseReceiveFile() {
			// isBusyNow = false;
			Person person = application.onlineUsers.get(Integer
					.valueOf(fileSenderUid));
			fileSendBuffer[4] = Constant.CMD_TYPE2;
			fileSendBuffer[5] = Constant.OPR_CMD3;
			byte[] meIdBytes = ByteAndInt.int2ByteArray(application.me
					.getIntUserId());
			System.arraycopy(meIdBytes, 0, fileSendBuffer, 6, 4);
			try {
				DatagramPacket dp = new DatagramPacket(fileSendBuffer,
						Constant.bufferSize,
						InetAddress.getByName(person.ipAddress), UDP_PORT);
				broadcastSocket.send(dp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 获得欲接收文件的文件名
		 * 
		 * @return
		 */
		public ArrayList<FileState> getReceivedFileNames() {
			return fileHandler.receivedFileNames;
		}

		/**
		 * 获得欲发送文件的文件名
		 * 
		 * @return
		 */
		public ArrayList<FileState> getBeSendFileNames() {
			return fileHandler.beSendFileNames;
		}

		/**
		 * 开始语音呼叫，向远方发送语音呼叫请求
		 * 
		 * @param personId
		 */
		public void startTalk(int personId) {
			try {
				audioHandler.isStopTalk = false;
				talkCmdBuffer[4] = Constant.CMD_TYPE1;
				talkCmdBuffer[5] = Constant.OPR_CMD1;
				System.arraycopy(InetAddress
						.getByName(application.me.ipAddress).getAddress(), 0,
						talkCmdBuffer, 44, 4);
				Person person = application.onlineUsers.get(Integer
						.valueOf(personId));
				DatagramPacket dp = new DatagramPacket(talkCmdBuffer,
						Constant.bufferSize,
						InetAddress.getByName(person.ipAddress), UDP_PORT);
				broadcastSocket.send(dp);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 结束语音呼叫
		 * 
		 * @param personId
		 */
		public void stopTalk(int personId) {
			audioHandler.isStopTalk = true;
			talkCmdBuffer[4] = Constant.CMD_TYPE1;
			talkCmdBuffer[5] = Constant.OPR_CMD2;
			Person person = application.onlineUsers.get(Integer
					.valueOf(personId));
			try {
				System.arraycopy(InetAddress
						.getByName(application.me.ipAddress).getAddress(), 0,
						talkCmdBuffer, 44, 4);
				DatagramPacket dp = new DatagramPacket(talkCmdBuffer,
						Constant.bufferSize,
						InetAddress.getByName(person.ipAddress), UDP_PORT);
				broadcastSocket.send(dp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 接受远程语音呼叫请求，并向远程发送语音数据
		 * 
		 * @param personId
		 */
		public void acceptTalk(int personId) {
			talkCmdBuffer[3] = Constant.CMD83;
			talkCmdBuffer[4] = Constant.CMD_TYPE2;
			talkCmdBuffer[5] = Constant.OPR_CMD1;
			try {
				// 发送接受语音指令
				System.arraycopy(InetAddress
						.getByName(application.me.ipAddress).getAddress(), 0,
						talkCmdBuffer, 44, 4);
				Person person = application.onlineUsers.get(Integer
						.valueOf(personId));
				if (person == null) {
					Log.i("e", "acceptTalk ,person==null");
					return;
				}
				Log.i("msg", "person=" + person.getUserName());
				DatagramPacket dp = new DatagramPacket(talkCmdBuffer,
						Constant.bufferSize,
						InetAddress.getByName(person.ipAddress), UDP_PORT);
				broadcastSocket.send(dp);
				audioHandler.audioSend(person);// 同时向对方发送音频数据
				Log.i("msg", "向对方发送音频数据");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private long getTime() {
			Date d = new Date();
			return d.getTime();
		}

		/**
		 * 获得组播锁
		 */
		private void allowMulticast() {
			// WifiManager wm = (WifiManager)
			// PadIMApplication.getInstance().getSystemService(
			// Context.WIFI_SERVICE);
			// multicastLock = wm.createMulticastLock("mydebuginfo");
			// multicastLock.acquire();
		}
	}
	
}

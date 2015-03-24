/**
 * 
 */
package com.example.padim.threads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.example.padim.PadIMApplication;
import com.example.padim.dbutils.Person;
import com.example.padim.model.FileName;
import com.example.padim.model.FileState;
import com.example.padim.util.ByteAndInt;
import com.example.padim.util.Constant;
import com.example.padim.util.DateUtil;

/**
 * 基于Tcp传输的文件收发模块
 * 
 * @author zhuft
 * @version 1.0 2011-11-6
 */
public class FileHandler extends Thread {

	private ServerSocket sSocket = null;
	
	/** 多文件传输时用来标记是否完成文件传输 任务的辅助标记*/
    public static int multiFilesNum = 0;
    public static int finishedNum = 0;

	/** 用来保存接收到的文件 */
	public static String fileSavePath = null;
	/** 用来临时保存需要发送的文件名 */
	public static ArrayList<FileName> tempFiles = null;
	/** 用来临时保存需要发送文件的用户id(接受文件方的用户id) */
	public static int tempUid = 0;
	public static ArrayList<FileState> receivedFileNames = new ArrayList<FileState>();
	public static ArrayList<FileState> beSendFileNames = new ArrayList<FileState>();

	public FileHandler() {
	}

	@Override
	public void run() {
		super.run();
		try {
			sSocket = new ServerSocket(Constant.PORT);
			Log.e("", "文件处理socket 开启 ...");
			while (!sSocket.isClosed() && null != sSocket) {
				Log.i("msg","sSocket.isClosed()="+sSocket.isClosed());
				Socket socket = sSocket.accept();
				socket.setSoTimeout(5000);
				new SaveFileToDisk(socket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存接收到的数据
	 * 
	 * @author zhuft
	 * @version 1.0 2011-11-6
	 */
	private class SaveFileToDisk extends Thread {
		private Socket socket = null;

		public SaveFileToDisk(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			super.run();
			OutputStream output = null;
			InputStream input = null;
			try {
				byte[] recvFileCmd = new byte[Constant.bufferSize];// 接收对方第一次发过来的数据，该数据包中包含了要发送的文件名
				input = socket.getInputStream();
				input.read(recvFileCmd);// 读取对方发过来的数据
				int cmdType = recvFileCmd[4];// 按协议这位为命令类型
				int oprCmd = recvFileCmd[5];// 操作命令
				if (cmdType == Constant.CMD_TYPE1
						&& oprCmd == Constant.OPR_CMD6) {
					byte[] fileNameBytes = new byte[Constant.fileNameLength];// 从收到的数据包中提取文件名
					System.arraycopy(recvFileCmd, 10, fileNameBytes, 0,
							Constant.fileNameLength);
					StringBuffer sb = new StringBuffer();
					String fName = new String(fileNameBytes).trim();
					sb.append(fileSavePath).append(File.separator)
							.append(fName);// 组合成完整的文件名
					String fileName = sb.toString();
					File file = new File(fileName);// 根据获得的文件名创建文件
					// 定义数据接收缓冲区，准备接收对方传过来的文件内容
					byte[] readBuffer = new byte[Constant.readBufferSize];
					output = new FileOutputStream(file);// 打开文件输出流准备把接收到的内容写到文件中
					int readSize = 0;
					int length = 0;
					long count = 0;
					FileState fs = getFileStateByName(fName, receivedFileNames);

					while (-1 != (readSize = input.read(readBuffer))) {// 循环读取内容
						output.write(readBuffer, 0, readSize);// 把接收到的内容写到文件中
						output.flush();
						length += readSize;
						count++;
						if (count % 10 == 0) {
							fs.currentSize = length;
							fs.percent = ((int) ((Float.valueOf(length) / Float
									.valueOf(fs.fileSize)) * 100));
							if(100 == fs.percent){
								PadIMApplication.getInstance().transferFileFinished=true;
							}
							Intent intent = new Intent();
							intent.setAction(Constant.fileReceiveStateUpdateAction);
							PadIMApplication.getInstance()
									.sendBroadcast(intent);
						}
					}
					fs.currentSize = length;
					fs.percent = ((int) ((Float.valueOf(length) / Float
							.valueOf(fs.fileSize)) * 100));
					if(100 == fs.percent){
						PadIMApplication.getInstance().transferFileFinished=true;
					}
					Intent intent = new Intent();
					intent.setAction(Constant.fileReceiveStateUpdateAction);
					PadIMApplication.getInstance().sendBroadcast(intent);
				} else if(cmdType == Constant.CMD_TYPE1
						&& oprCmd == Constant.OPR_CMD7){
					Log.i("msg","音频文件传输");
					PadIMApplication.getInstance().isVoice = true ;
					byte[] fileNameBytes = new byte[Constant.fileNameLength];// 从收到的数据包中提取文件名
					System.arraycopy(recvFileCmd, 10, fileNameBytes, 0,
							Constant.fileNameLength);
					StringBuffer sb = new StringBuffer();
					String fName = new String(fileNameBytes).trim();
					Log.i("msg","fName="+fName);
					String rec_filepath = Environment.getExternalStorageDirectory()+File.separator
							+ "PadIM" + File.separator +"voice"+File.separator+"rec"+ File.separator;
					File rec_path = new File(rec_filepath);
					if(!rec_path.exists()){
						boolean s = rec_path.mkdirs();
						Log.i("msg","s="+s);
					}
					sb.append(rec_filepath).append(fName);//完整名字
					String fileName = sb.toString();
					Log.i("msg","fileName="+fileName);
					File file = new File(fileName);// 根据获得的文件名创建文件
					if(file.exists()){
						file.delete();
						Log.i("msg","删除文件");
					}
					
					try {
						file.createNewFile();
					} catch (IOException e) {
						throw new IllegalStateException("Failed to create "
								+ file.toString());
					}
					
					// 定义数据接收缓冲区，准备接收对方传过来的文件内容
					byte[] readBuffer = new byte[Constant.readBufferSize];
					output = new FileOutputStream(file);// 打开文件输出流准备把接收到的内容写到文件中
					int readSize = 0;
					while (-1 != (readSize = input.read(readBuffer))) {// 循环读取内容
						output.write(readBuffer, 0, readSize);// 把接收到的内容写到文件中
						output.flush();
					}
					Log.i("msg","已成功接收音频文件");
					while(PadIMApplication.getInstance().recTime == 0){
//						Log.i("msg","等待recTime不为0");
					}
					
					String newPath = rec_filepath + DateUtil.getTimeStr2(PadIMApplication.getInstance().recTime)+".pcm";
					Log.i("msg","newPath="+newPath);
					PadIMApplication.getInstance().recTime = 0;
					File newfile = new File(newPath);
					if(newfile.exists()){
						newfile.delete();
					}
					
					try {
						newfile.createNewFile();
					} catch (IOException e) {
						throw new IllegalStateException("Failed to create "
								+ newfile.toString());
					}
					
					file.renameTo(newfile);
					PadIMApplication.getInstance().voiceStore = true;//改名字成功后
				} else {
					Intent intent = new Intent();
					intent.putExtra("msg", "数据接收错误");
					intent.setAction(Constant.dataReceiveErrorAction);
					PadIMApplication.getInstance().sendBroadcast(intent);
				}
			} catch (Exception e) {
				Log.i("msg","传输错误");
				Intent intent = new Intent();
				intent.putExtra("msg", e.getMessage());
				intent.setAction(Constant.dataReceiveErrorAction);
				PadIMApplication.getInstance().sendBroadcast(intent);
				e.printStackTrace();
			} finally {
				try {
					if (null != input)
						input.close();
					if (null != output)
						output.close();
					if (!socket.isClosed())
						socket.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * 开始给对方发送文件
	 */
	public void startSendFile() {
		// 获得接收方信息
		Person person = PadIMApplication.getInstance().onlineUsers.get(Integer
				.valueOf(tempUid));
		final String userIp = person.ipAddress;
		// 组合头数据包，该数据包中包括要发送的文件名
		final byte[] sendFileCmd = new byte[Constant.bufferSize];
		for (int i = 0; i < Constant.bufferSize; i++)
			sendFileCmd[i] = 0;
		System.arraycopy(Constant.pkgHead, 0, sendFileCmd, 0, 3);
		sendFileCmd[3] = Constant.CMD82;
		sendFileCmd[4] = Constant.CMD_TYPE1;
		sendFileCmd[5] = Constant.OPR_CMD6;
		System.arraycopy(ByteAndInt.int2ByteArray(PadIMApplication
				.getInstance().me.getIntUserId()), 0, sendFileCmd, 6, 4);
		
		multiFilesNum = tempFiles.size();
		
		for (final FileName file : tempFiles) {// 采用多线程发送文件,一个线程发送一个文件
			new Thread() {
				@Override
				public void run() {
					Socket socket = null;
					OutputStream output = null;
					InputStream input = null;
					try {
						socket = new Socket(userIp, Constant.PORT);
						byte[] fileNameBytes = file.getFileName().getBytes();
						int fileNameLength = Constant.fileNameLength + 10;// 清除头文件包的文件名存储区域，以便写新的文件名
						for (int i = 10; i < fileNameLength; i++)
							sendFileCmd[i] = 0;
						System.arraycopy(fileNameBytes, 0, sendFileCmd, 10,
								fileNameBytes.length);// 把文件名放入头数据包
						System.arraycopy(
								ByteAndInt.longToByteArray(file.fileSize), 0,
								sendFileCmd, 100, 8);
						output = socket.getOutputStream();// 构造一个输出流
						output.write(sendFileCmd);// 把头数据包发给对方
						output.flush();
						sleep(1000);// sleep 1秒钟，等待对方处理完
						// 定义数据发送缓冲区
						byte[] readBuffer = new byte[Constant.readBufferSize];// 文件读写缓存
						input = new FileInputStream(new File(file.fileName));// 打开一个文件输入流
						int readSize = 0;
						int length = 0;
						long count = 0;
						FileState fs = getFileStateByName(file.getFileName(),
								beSendFileNames);
						while (-1 != (readSize = input.read(readBuffer))) {// 循环把文件内容发送给对方
							output.write(readBuffer, 0, readSize);// 把内容写到输出流中发送给对方
							output.flush();
							length += readSize;

							count++;
							if (count % 10 == 0) {
								fs.currentSize = length;
								fs.percent = ((int) ((Float.valueOf(length) / Float
										.valueOf(fs.fileSize)) * 100));
								//针对多文件传输
								if(100 == fs.percent){
									++finishedNum;
								}
								if(multiFilesNum == finishedNum){
									multiFilesNum=0;
									finishedNum=0;
									PadIMApplication.getInstance().transferFileFinished=true;
								}
								Intent intent = new Intent();
								intent.setAction(Constant.fileSendStateUpdateAction);
								PadIMApplication.getInstance().sendBroadcast(
										intent);
							}
						}
						fs.currentSize = length;
						fs.percent = ((int) ((Float.valueOf(length) / Float
								.valueOf(fs.fileSize)) * 100));
						if(100 == fs.percent){
							++finishedNum;
						}
						if(multiFilesNum == finishedNum){
							multiFilesNum=0;
							finishedNum=0;
							PadIMApplication.getInstance().transferFileFinished=true;
						}
						Intent intent = new Intent();
						intent.setAction(Constant.fileSendStateUpdateAction);
						PadIMApplication.getInstance().sendBroadcast(intent);
					} catch (Exception e) {
						// 往界面层发送文件传输出错信息
						Intent intent = new Intent();
						intent.putExtra("msg", e.getMessage());
						intent.setAction(Constant.dataSendErrorAction);
						PadIMApplication.getInstance().sendBroadcast(intent);
						e.printStackTrace();
					} finally {
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
				}
			}.start();
		}
	}

	/**
	 * 根据文件名从文件状态列表中获得该文件状态
	 * 
	 * @param fileName
	 * @param fileStates
	 * @return
	 */
	private FileState getFileStateByName(String fileName,
			ArrayList<FileState> fileStates) {
		for (FileState fileState : fileStates) {
			if (fileState.fileName.equals(fileName)) {
				return fileState;
			}
		}
		return null;
	}

	public void release() {
		try {
			Log.e("", "文件处理 socket 关闭 ...");
			if (null != sSocket) {
				sSocket.close();
				sSocket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

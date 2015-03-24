/**
 * 
 */
package com.example.padim.threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.padim.PadIMApplication;
import com.example.padim.dbutils.Person;
import com.example.padim.util.Constant;

/**
 * 基于Tcp语音传输模块
 * 
 * @author zhuft
 * @version 1.0 2011-11-6
 */
public class AudioHandler extends Thread {
	private ServerSocket sSocket = null;

	/** 通话结束标志 */
	public static boolean isStopTalk = false;

	// private G711Codec codec;
	public AudioHandler() {
	}

	@Override
	public void run() {
		super.run();
		try {
			sSocket = new ServerSocket(Constant.AUDIO_PORT);// 监听音频端口
			Log.e("", "声音处理socket 开启...");
			Log.i("msg","isStopTalk="+isStopTalk);
			while (!sSocket.isClosed() && null != sSocket) {
				Socket socket = sSocket.accept();
				socket.setSoTimeout(5000);
				audioPlay(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用来启动音频播放子线程
	 * 
	 * @param socket
	 */
	public void audioPlay(Socket socket) {
		Log.i("msh","启动音频播放");
		new AudioPlay(socket).start();
	}

	/**
	 * 用来启动音频发送子线程
	 * 
	 * @param person
	 */
	public void audioSend(Person person) {
		new AudioSend(person).start();
	}

	/**
	 * 音频播线程
	 * 
	 * @author zhuft
	 * @version 1.0 2011-11-6
	 */
	public class AudioPlay extends Thread {
		Socket socket = null;

		public AudioPlay(Socket socket) {
			this.socket = socket;
			// android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		}

		@Override
		public void run() {
			super.run();
			try {
				InputStream is = socket.getInputStream();
				// 获得音频缓冲区大小
				int bufferSize = android.media.AudioTrack.getMinBufferSize(PadIMApplication.getInstance().sampRate,
						AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
				Log.e("", "播放缓冲区大小" + bufferSize);

				// 获得音轨对象
				AudioTrack player = new AudioTrack(PadIMApplication.getInstance().getVoiceMode(),
						PadIMApplication.getInstance().sampRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
						AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

				// 设置喇叭音量
				player.setStereoVolume(1.0f, 1.0f);

				// 开始播放声音
				player.play();
				byte[] audio = new byte[160];// 音频读取缓存
				int length = 0;

				while (!isStopTalk) {
					try{
					length = is.read(audio);// 从网络读取音频数据
					Log.i("msg","length="+length);
					}catch(Exception e){
						e.printStackTrace();
					}
					byte[] temp = audio.clone();
//					if (length > 0 && length % 2 == 0) {
						if (length > 0) {
						player.write(audio, 0, temp.length);// 播放音频数据
					}
				}
				player.stop();
				player.release();
				player = null;
				is.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void calc1(short[] lin, int off,int len){
		int i,j;
		for(i=0;i<len;i++) {
			j = lin[i+off];
			lin[i+off] = (short)(j >> 2);
		}
	}
	/**
	 * 音频发送线程
	 * 
	 * @author zhuft
	 * @version 1.0 2011-11-6
	 */
	public class AudioSend extends Thread {
		Person person = null;

		public AudioSend(Person person) {
			this.person = person;
			// android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		}

		@Override
		public void run() {
			super.run();
			Socket socket = null;
			OutputStream os = null;
			AudioRecord recorder = null;
			try {
				socket = new Socket(person.ipAddress, Constant.AUDIO_PORT);
				socket.setSoTimeout(5000);
				os = socket.getOutputStream();
				// 获得录音缓冲区大小
				int bufferSize = AudioRecord.getMinBufferSize(PadIMApplication.getInstance().sampRate,
						AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
				Log.e("", "录音缓冲区大小" + bufferSize);

				// 获得录音机对象
				recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, PadIMApplication.getInstance().sampRate,
						AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 10);

				recorder.startRecording();// 开始录音
				byte[] readBuffer = new byte[640];// 录音缓冲区

				int length = 0;

				while (!isStopTalk) {
					Log.i("msg","录音");
					length = recorder.read(readBuffer, 0, 640);// 从mic读取音频数据
//					if (length > 0 && length % 2 == 0) {
					if (length > 0){
						os.write(readBuffer, 0, length);// 写入到输出流，把音频数据通过网络发送给对方
					}
				}
				recorder.stop();
				recorder.release();
				recorder = null;
				os.close();
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void release() {
		try {
			Log.e("", "声音处理socket 关闭 ...");
			if (null != sSocket) {
				sSocket.close();
				sSocket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

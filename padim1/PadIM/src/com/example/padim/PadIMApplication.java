package com.example.padim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.padim.dbutils.DatabaseHelper;
import com.example.padim.dbutils.MessageData;
import com.example.padim.dbutils.Person;
import com.example.padim.dbutils.PersonDao;
import com.example.padim.util.Constant;
import com.example.padim.util.PadImUtil;
import com.example.padim.util.PhoneStateUtil;
import com.j256.ormlite.android.apptools.OpenHelperManager;

/**
 * 
 * 项目名称：PadIM 类名称：PadIMApplication 类描述： 在类内部定义全局变量，在不同activity中获取访问 创建人：ystgx 创建时间：2014年3月23日 下午9:29:01
 * 
 * @version
 */
public class PadIMApplication extends Application {

    private static PadIMApplication instance;
    /** 用来标记是否完成文件传输 任务*/
    public boolean transferFileFinished = false;
    
    /**用来标记在home下是否有文件传输请求*/
    public boolean hasFileTransfer = false;

    /**语音信息到来时用来通知contactfragment更新界面*/
    public boolean updatelist = false;
    
    /** 在群组发语音消息时用来标记在本地只发送一次消息*/
    public boolean single = false;
    
    /**一次发送语音消息是否完毕, 发送端*/
    public boolean voiceMsg = false;
    
    /**标记一个语音消息的文件已存储好， 接收端*/
    public boolean voiceStore = false;
    
    /**用来区分当前的信息是否是音频消息, 接收端*/
    public boolean isVoice = false;
    
    /**语音信息时缓存最近一次接收的时间，即文件名*/
    public long recTime = 0;
    /**语音信息时缓存最近一次发送的时间，即文件名*/
    public long sendTime = 0;
    /** 用来保存自身的相关信息 */
    public Person me = null;

    /** 保存所有组中的用户，每个map对象保存一个组的全部用户 */
    public ArrayList<Map<Integer, Person>> allUser = new ArrayList<Map<Integer, Person>>();
    /** 当前在线用户 */
    public Map<Integer, Person> onlineUsers = new ConcurrentHashMap<Integer, Person>();
    /** 当前在线用户id */
    public ArrayList<Integer> onlineUsersId = new ArrayList<Integer>();
    /** 所有用户信息容器 */  //保存着未读信息
    public Map<Integer, List<MessageData>> msgContainer = new ConcurrentHashMap<Integer, List<MessageData>>();

    /** 音频采样频率 */
    public int sampRate = 8000;

    public static PadIMApplication getInstance() {
        return instance;
    }

    private void init() {
    	ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cManager.getActiveNetworkInfo(); 
		  if (info == null || !info.isAvailable()){ 
			  Toast.makeText(getApplicationContext(), "无网络环境，打开失败", Toast.LENGTH_LONG).show();
			  return;
		  }
    	
        int myUserId = PadImUtil.getMyId();
        
        PadIMApplication.getInstance().me = PersonDao.getInstance().getPersonByUserId(
                PadImUtil.getMyId() + "");
        Person p = PersonDao.getInstance().getPersonByUserId(myUserId + "");
        String IMEI = PhoneStateUtil.getIMEI(PadIMApplication.getInstance()
				.getApplicationContext());
        Log.i("msg","IMApplication IMEI="+IMEI);
        String UserName = IMEI.substring(IMEI.length()-2) + Build.MODEL;
        if (p == null) {
            p = new Person();
            p.setUserName(UserName);
            p.setUserId(myUserId + "");
            p.setHeadIconId(0);
            PersonDao.getInstance().createOrUpdate(p);
            Editor et = PreferenceManager.getDefaultSharedPreferences(this).edit();
            et.putString("downloadPath", Constant.downloadPath).commit();
            et.putString("username", UserName).commit();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ip = prefs.getString("ip", "");
        if (TextUtils.isEmpty(ip)) {
            Editor et = PreferenceManager.getDefaultSharedPreferences(this).edit();
            et.putString("ip", Constant.MULTICAST_IP);
            et.putString("mode1", "0").commit();
        }

        Constant.MULTICAST_IP = prefs.getString("ip", Constant.MULTICAST_IP);
        String dir = Constant.downloadPath;
        File f = new File(dir);
        if(!f.exists()){
        	f.mkdirs();
        }
    }

    DatabaseHelper dataHelper;

    public DatabaseHelper getHelper() {
        if (dataHelper == null) {
            dataHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return dataHelper;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
        init();
        initRecorderParameters(Constant.sampleRates);
        dealMemory();

        getHelper();
    }

    /**
     * 内存处理
     */
    public void dealMemory() {
        // 可以增强程序堆内存的处理效率
        // VMRuntime.getRuntime().setTargetHeapUtilization(Constant.TARGET_HEAP_UTILIZATION);
    }

    /**
     * 根据用户id获得该用户的消息
     * 
     * @param personId
     * @return
     */
    public List<MessageData> getMessagesById(int personId) {
        return msgContainer.get(personId);
    }

    /**
     * 根据用户id获得该用户的消息数量
     * 
     * @param personId
     * @return
     */
    public int getMessagesCountById(int personId) {
        List<MessageData> msgs = msgContainer.get(personId);
        if (null != msgs) {
            return msgs.size();
        } else {
            return 0;
        }
    }

    /**
     * 退出程序时的操作
     */
    public void exit() {
        allUser.clear();
        onlineUsers.clear();
        onlineUsersId.clear();
        msgContainer.clear();
    }

    private int VOICE_MODE = AudioManager.STREAM_VOICE_CALL;

    public int getVoiceMode() {
        return VOICE_MODE;
    }

    public void setVoiceTingtong() {
        VOICE_MODE = AudioManager.STREAM_VOICE_CALL;
    }

    public void setVoiceYangshengqi() {
        VOICE_MODE = AudioManager.STREAM_MUSIC;
    }

    /**
     * 初始化音频参数
     * 
     * @param sampleRates
     */
    public void initRecorderParameters(int[] sampleRates) {
        for (int i = 0; i < sampleRates.length; ++i) {
            try {
                // Log.e("", "Indexing " + sampleRates[i] + "Hz Sample Rate");
                int tmpBufferSize = AudioRecord.getMinBufferSize(sampleRates[i],
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                // Test the minimum allowed buffer size with this configuration
                // on this device.
                if (tmpBufferSize != AudioRecord.ERROR_BAD_VALUE) {
                    // Seems like we have ourself the optimum AudioRecord
                    // parameter for this device.
                    AudioRecord tmpRecoder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                            sampleRates[i], AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, tmpBufferSize);
                    // Test if an AudioRecord instance can be initialized with
                    // the given parameters.
                    if (tmpRecoder.getState() == AudioRecord.STATE_INITIALIZED) {
                        Log.e("", "支持采样频率:" + sampleRates[i]);
                        // +++Release temporary recorder resources and leave.
                        sampRate = sampleRates[i];
                        tmpRecoder.release();
                        tmpRecoder = null;
                        return;
                    }
                } else {
                    Log.e("", "Incorrect buffer size. Continue sweeping Sampling Rate...");
                }
            } catch (IllegalArgumentException e) {
                Log.e("", "The " + sampleRates[i]
                        + "Hz Sampling Rate is not supported on this device");
            }
        }
    }

}

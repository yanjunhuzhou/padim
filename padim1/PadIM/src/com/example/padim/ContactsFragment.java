		package com.example.padim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.padim.adapters.OnlineListAdapter;
import com.example.padim.adapters.ReceiveSendFileListAdapter;
import com.example.padim.beans.DeviceItemHolder;
import com.example.padim.dbutils.MessageDao;
import com.example.padim.dbutils.MessageData;
import com.example.padim.dbutils.Person;
import com.example.padim.dbutils.PersonDao;
import com.example.padim.model.FileState;
import com.example.padim.services.FlyGeonMainBroadcastRecv;
import com.example.padim.services.MainService;
import com.example.padim.util.Constant;
import com.example.padim.util.ImReceiver;
import com.example.padim.util.PadImUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import de.greenrobot.event.EventBus;

/**
 * 
 * 项目名称：PadIM 类名称：ContactsFragment 类描述： 联系人列表页面 创建人：ystgx 创建时间：2014年3月2日 下午10:02:46
 * 
 * @version
 */
public class ContactsFragment extends BaseFragment implements ImReceiver {

    protected View mMainView;
    protected Context mContext;
    

    public ContactsFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();

    }

    public OnlineListAdapter adapter = null;
    PullToRefreshListView pullListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.contacts_fragment, container, false);
        pullListView = (PullToRefreshListView) mMainView.findViewById(R.id.list);
       
        final ListView listview = pullListView.getRefreshableView();
        listview.setFadingEdgeLength(0);
        listview.setDivider(null);
        listview.setCacheColorHint(0x000000);
        listview.setSelector(R.drawable.transparent);
        pullListView.setMode(Mode.PULL_DOWN_TO_REFRESH);

        adapter = new OnlineListAdapter(getActivity());
        listview.setAdapter(adapter);
        pullListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Person p = adapter.getmList().get(position - listview.getHeaderViewsCount());
                if(p.getIntUserId() == (PadIMApplication.getInstance().me.getIntUserId())){
                	//do nothing
                } else
                	openChartPage(p);
            }
        });
        pullListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                
                mService.refreshFriends();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        
                        pullListView.onRefreshComplete();
                    }
                }, 2000);

            }
        });
        listview.setCacheColorHint(0);
        return mMainView;
    }

    /**
     * 打开发短信页面
     * 
     * @param person
     */
    private void openChartPage(Person person) {
        Intent intent = new Intent(getActivity(), ChatMsgActivity.class);
        List<String> userIds = new ArrayList<String>();
        userIds.add(person.getUserId());
        userIds.add(PadIMApplication.getInstance().me.getUserId());
        intent.putExtra("msgPersons", PadImUtil.getMsgPersons(userIds));
        getActivity().startActivity(intent);
    }

    Intent mMainServiceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        receiveFileListAdapter = new ReceiveSendFileListAdapter(getActivity());
        sendFileListAdapter = new ReceiveSendFileListAdapter(getActivity());

        mMainServiceIntent = new Intent(getActivity(), MainService.class);
        getActivity().getApplicationContext().bindService(mMainServiceIntent, sConnection,
                Context.BIND_AUTO_CREATE);
        getActivity().getApplicationContext().startService(mMainServiceIntent);

        regBroadcastRecv();
    }

    private MainService mService = null;

    public MainService getMainService() {
        return mService;
    }

    /**
     * MainService服务与当前Activity的绑定连接器
     */
    private ServiceConnection sConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((MainService.ServiceBinder) service).getService();
            System.out.println("Service connected to activity...");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            System.out.println("Service disconnected to activity...");
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unregisterReceiver(broadcastRecv);
        getActivity().getApplicationContext().stopService(mMainServiceIntent);
        getActivity().getApplicationContext().unbindService(sConnection);
//        player.release();
        // AppConnect.getInstance(this).finalize();

    }

    private IntentFilter bFilter = null;
    private FlyGeonMainBroadcastRecv broadcastRecv = null;

    @Override
    public void onShow() {
        super.onShow();
        updateExListAdapter();// 更新在线列表
    }

    /**
     * 广播接收器注册
     */
    private void regBroadcastRecv() {
        broadcastRecv = new FlyGeonMainBroadcastRecv(this);
        bFilter = new IntentFilter();
        bFilter.addAction(Constant.updateMyInformationAction);
        bFilter.addAction(Constant.personHasChangedAction);
        bFilter.addAction(Constant.hasMsgUpdatedAction);
        bFilter.addAction(Constant.receivedSendFileRequestAction);
        bFilter.addAction(Constant.remoteUserRefuseReceiveFileAction);
        bFilter.addAction(Constant.dataReceiveErrorAction);
        bFilter.addAction(Constant.dataSendErrorAction);
        bFilter.addAction(Constant.fileReceiveStateUpdateAction);
        bFilter.addAction(Constant.fileSendStateUpdateAction);
        bFilter.addAction(Constant.receivedTalkRequestAction);
        bFilter.addAction(Constant.remoteUserClosedTalkAction);
        bFilter.addAction(Constant.SpecialComdAction);
        getActivity().getApplication().registerReceiver(broadcastRecv, bFilter);
    }

    /**
     * 获得自已的相关信息
     */
    public void updateMyInfomation() {
        PersonDao.getInstance().createOrUpdate(PadIMApplication.getInstance().me);
    }

    private static Person temppsn =null;//为了从home回到activity时弹出对话框，缓存变量
    
    @Override
    public void onReceive(String type, Object obj) {
        try {
            if (type.equals(Constant.updateMyInformationAction)) {
                updateMyInfomation();
            } else if (type.equals(Constant.dataReceiveErrorAction)
                    || type.equals(Constant.dataSendErrorAction)) {
                Toast.makeText(getActivity(), obj.toString(), Toast.LENGTH_SHORT).show();
            } else if (type.equals(Constant.fileReceiveStateUpdateAction)) {// 收到来自服务层的文件接收状态通知
                updateFileReceiveState();
            } else if (type.equals(Constant.fileSendStateUpdateAction)) {// 收到来自服务层的文件接收状态通知
            	updateFileSendState();
            } else if (type.equals(Constant.receivedTalkRequestAction)) {
                final Person psn = (Person) obj;
                showReceivedTalkRequestDialog(psn);
            } else if (type.equals(Constant.remoteUserClosedTalkAction)) {
                isRemoteUserClosed = true;// 如果接收到远程用户关闭通话指令则把该标记置为true
                Person psn = (Person) obj;
                Toast.makeText(getActivity(), "[" + psn.userName + "]关闭与你的通话框...",
                        Toast.LENGTH_SHORT).show();
            } else if (type.equals(Constant.remoteUserRefuseReceiveFileAction)) {
                Toast.makeText(getActivity(), "对方拒绝接收文件", Toast.LENGTH_SHORT).show();
            } else if (type.equals(Constant.personHasChangedAction)) {
                updateExListAdapter();// 更新在线列表
            } else if (type.equals(Constant.hasMsgUpdatedAction)) {
                updateExListAdapter();
                Log.i("msg","contact 收到msg");
                if (obj instanceof Intent) {
                    Intent intent = (Intent) obj;
                    String msgPersons = intent.getStringExtra("msgPersons");
                    String msgContents = intent.getStringExtra("msgContent");
                    for(int i=0;i<10;i++){
                    	if(msgContents.equals(Constant.specialComd[i])){
                    		AlertNotification(msgContents);
                    		return;
                    	}
                    }
                    
                    if (!isPaused || ChatMsgActivity.chatting) {//当前界面或chat界面
                    	
                        if (msgPersons != null && msgPersons.split(",").length > 2) {
                            EventBus.getDefault().post("newgroupchat");//将页面转到历史记录里面
                            Toast.makeText(getActivity(), "您有新的群聊信息", Toast.LENGTH_LONG).show();
                            notifySound();
                        } else {
                        	Toast.makeText(getActivity(), "您有新消息", Toast.LENGTH_LONG).show();
                        	notifySound();
                        }
                    } else {
                    	if(GroupCheckActivity.inGroup){//建立群组界面
                        	if (msgPersons != null && msgPersons.split(",").length > 2) {
                                EventBus.getDefault().post("newgroupchat");//将页面转到历史记录里面
                                Toast.makeText(getActivity(), "您有新的群聊信息", Toast.LENGTH_LONG).show();
                                notifySound(); //仅有声音
                            } else {
                            	Toast.makeText(getActivity(), "您有新消息", Toast.LENGTH_LONG).show();
                            	notifySound();
                            }
                        }else {//其他界面
                            if (msgPersons != null && msgPersons.split(",").length > 2) {
                                EventBus.getDefault().post("newgroupchat");
                                Toast.makeText(getActivity(), "您有新的群聊信息", Toast.LENGTH_LONG).show();
                                notifyMessage();
                            } else {
                                Toast.makeText(getActivity(), "您有新消息", Toast.LENGTH_LONG).show();
                                notifyMessage();
                                Log.i("msg","contact hasmsg");
                            }
                        }
                    }
                }
            } else if (type.equals(Constant.receivedSendFileRequestAction)) {// 接收到文件发送请求，请求接收文件
            	if(isPaused && !GroupCheckActivity.inGroup && !ChatMsgActivity.chatting){
                	notifyFile();
                	temppsn = (Person) obj;
                	PadIMApplication.getInstance().hasFileTransfer=true;//此处的恢复默认值也可能在ChatMsgActivity的onresume()中
                }else if(isPaused && GroupCheckActivity.inGroup){
                	temppsn = (Person) obj;
                	notifyFile();
                	Toast.makeText(getActivity(), "文件传输请求，请返回到好友列表", Toast.LENGTH_LONG).show();
                }else if(isPaused && ChatMsgActivity.chatting){
                	notifySound();
                }else{
                	 Person psn = (Person) obj;
                     showReceivedSendFileRequestDialog(psn);
                     notifySound();
                }
            }else if (type.equals(Constant.SpecialComdAction)){
            	if (obj instanceof Intent) {
                    Intent intent =(Intent) obj;
                    int index = intent.getIntExtra("special", 0);
                    Log.e("msg","index="+index);
        	        AlertNotification(Constant.specialComd[index]);
                    
            
                            

                    
                    
                    Intent newintent = new Intent(getActivity(), EXIT.class);
                    newintent.putExtra("special", index);
                    Log.i("msg", "ompdsafasd"+index);
                    
                    startActivity(newintent);
                    acquireWakeLock();
                    
                    
                           }
            	}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void acquireWakeLock()
    {
    	Log.i("wakelock","recute");
    	WakeLock wakelock = null;
     	if(wakelock == null)
     	{
     		PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
     	  if (!pm.isScreenOn()) {  
     			    
     			         
     			   wakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP, "PostLocationService");
     			   wakelock.acquire();  
     			  // wakelock.release();
     			}  
     		
     	}
     }
    
    public static Notification messagenotification = null;
    public static Notification fileNotification = null;
    public static NotificationManager messagenotificatiomanager;
    private static int notificationID = 100005;

    Uri note = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    Uri alert = Uri.parse("system/media/audio/notifications/Polaris.ogg");
	
    private void notifySound() {
    	
    	try {
    		final MediaPlayer player = new MediaPlayer();
			player.setDataSource(getActivity(), note);
			final AudioManager audioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
			 if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				 player.setAudioStreamType(AudioManager.STREAM_ALARM);
				 player.setLooping(false);
				 player.prepare();
				 player.start();
				 
			   }
			 player.setOnCompletionListener(new OnCompletionListener(){

				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					player.release();
					Log.i("msg","player release");
				}
				 
			 });
			 
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void notifyFile() {
    	fileNotification = new Notification();
    	fileNotification.icon = R.drawable.icon;
    	fileNotification.tickerText = "新的文件传输";
    	fileNotification.defaults = Notification.DEFAULT_SOUND;
    	fileNotification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
    	messagenotificatiomanager = (NotificationManager) getActivity().getSystemService(
                Context.NOTIFICATION_SERVICE);
    	fileNotification.setLatestEventInfo(getActivity(), "您有新的文件传输任务", "新文件传输", getPendIntent());
        messagenotificatiomanager.notify(1000, fileNotification); 
    }
    
    private void AlertNotification(String s) {
    	messagenotification = new Notification();
        messagenotification.icon = R.drawable.icon;
        messagenotification.tickerText = s;
        messagenotification.defaults |= Notification.DEFAULT_VIBRATE;
        long[] vibrate = {2000, 2000,2000,2000, 2000};
        messagenotification.vibrate = vibrate;
        messagenotification.sound = alert;
        messagenotification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        messagenotificatiomanager = (NotificationManager) getActivity().getSystemService(
                Context.NOTIFICATION_SERVICE);
        messagenotification.setLatestEventInfo(getActivity(), "紧急命令", s, getPendIntent());
        messagenotificatiomanager.notify(notificationID, messagenotification);
        notificationID++;
    }
    
    private void notifyMessage() {
        messagenotification = new Notification();
        messagenotification.icon = R.drawable.icon;
        messagenotification.tickerText = "新消息";
        messagenotification.defaults = Notification.DEFAULT_SOUND;
        messagenotification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        messagenotificatiomanager = (NotificationManager) getActivity().getSystemService(
                Context.NOTIFICATION_SERVICE);
        messagenotification.setLatestEventInfo(getActivity(), "您有新消息", "新消息", getPendIntent());
        messagenotificatiomanager.notify(100000, messagenotification); // id均为同一个，不需要弹出很多个消息框
    }

    private PendingIntent getPendIntent() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), MainActivity.class);

        PendingIntent pendingintent = PendingIntent.getActivity(getActivity(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingintent;
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        Log.i("msg","contact被pause了");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == -1) {
            if (null != data) {
                int selectType = data.getExtras().getInt("selectType");
                Log.i("msg","selectType="+selectType);
                if (selectType == Constant.SELECT_FILE_PATH) {// 如果收到的是文件夹选择模式，说明现在是要保存对方传过来的文件，则把当前选择的文件夹路径返回服务层
                    String fileSavePath = data.getExtras().getString("fileSavePath");
                    Log.i("msg","fileSavePath="+fileSavePath);
                    if (null != fileSavePath) {
                        mService.receiveFiles(fileSavePath);                        
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.folder_can_not_write),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
    	}
    }
    
    @Override
    public void onResume() {
    	isPaused = false;
    	if(messagenotification != null) {
    		messagenotificatiomanager.cancelAll();	
    	}
    	if(fileNotification != null){
    		messagenotificatiomanager.cancelAll();
    	}
    	Log.i("msg","contact resume");
    	if(temppsn != null){
    		Log.i("msg","temppsn="+temppsn.getUserName());
    		showReceivedSendFileRequestDialog(temppsn);
    		if(PadIMApplication.getInstance().hasFileTransfer)
    			PadIMApplication.getInstance().hasFileTransfer=false;
    		temppsn = null;
    	}
        super.onResume();
        updateExListAdapter();// 更新在线列表
        
    }

//    boolean finishedSendFile = PadIMApplication.getInstance().transferFileFinished;// 记录当前这些文件是不是本次已经接收过了

    public void sendFile(){
    	String fileSavePath = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("downloadPath",
                Constant.downloadPath); 
        Log.i("msg","fileSavePath="+fileSavePath);
        if (null != fileSavePath) {
            mService.receiveFiles(fileSavePath);
        } else {
            Toast.makeText(getActivity(), getString(R.string.folder_can_not_write),
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 弹出是否接收文件请求的对话框,弹出一个提示框是否要接收发过来的文件
     */
    public void showReceivedSendFileRequestDialog(final Person psn) {
        try {
            if (!isPaused) {// 如果自身处于可见状态则响应广播,弹出一个提示框是否要接收发过来的文件
                receivedFileNames = mService.getReceivedFileNames();// 从服务层获得所有需要接收的文件的文件名
                Log.i("msg","receivedFileNames.size="+receivedFileNames.size());
                if (receivedFileNames.size() <= 0)
                    return;
                receiveFileListAdapter.setResources(receivedFileNames);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(psn.userName);
                builder.setMessage("正在给您发送文件!");
                builder.setIcon(psn.getHeadIconId());
                View vi = getActivity().getLayoutInflater().inflate(
                        R.layout.request_file_popupwindow_layout, null);
                builder.setView(vi);
                final AlertDialog recDialog = builder.show();
                
                final int userId = Integer.parseInt(psn.userId);
                recDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface arg0) {
                        
                        if (!PadIMApplication.getInstance().transferFileFinished) {// 如果本次文件并未接收就关闭接收窗口，说明放弃本次接收，同时向远程发送一个拒绝接收的指令。
                            Intent intent = new Intent();
                            intent.setAction(Constant.refuseReceiveFileAction);
                            getActivity().sendBroadcast(intent);
                        }else{//接收成功
                        	for(int i=0;i<receivedFileNames.size();i++){
//                        		List<MessageData> messages = null;
//                                if (PadIMApplication.getInstance().msgContainer.containsKey(userId)) {
//                                    messages = PadIMApplication.getInstance().msgContainer.get(userId);
//                                } else {
//                                    messages = new ArrayList<MessageData>();
//                                }
                                MessageData data = new MessageData();
                                data.setTime(System.currentTimeMillis());
                                data.setUnRead(false);
                              //这两句是用来提示有未读信息
//                                messages.add(data);
//                                PadIMApplication.getInstance().msgContainer.put(userId, messages);
                                data.setFrom(PadIMApplication.getInstance().me.getUserId());
                                
                                String msgPersons;
                                String to;
                                List<String> userIds = new ArrayList<String>();
                                userIds.add(userId + "");
                                userIds.add(PadIMApplication.getInstance().me.getUserId());
                                to = userId+"";
                                msgPersons = PadImUtil.getMsgPersons(userIds);
                                data.setContent("成功接收文件："+receivedFileNames.get(i).fileName);
                                Log.i("msg","成功接收文件："+receivedFileNames.get(i).fileName);
                                data.setTo(to);
                                data.setMsgPersons(msgPersons);
                                MessageDao.getInstance().createOrUpdate(data);
                        	}
                        }
                        
                        receivedFileNames.clear();
                        PadIMApplication.getInstance().transferFileFinished = false;// 关闭文件接收对话框，本表示本次文件接收完成，把本次文件接收状态置为false
                    }
                });
                ListView lv = (ListView) vi.findViewById(R.id.receive_file_list);// 需要接收的文件清单
                lv.setAdapter(receiveFileListAdapter);
                final Button btn_ok = (Button) vi.findViewById(R.id.receive_file_okbtn);
                Button btn_cancle = (Button) vi.findViewById(R.id.receive_file_cancel);
                
                // 如果该按钮被点击则打开文件选择器，并设置成文件夹选择模式，选择一个用来接收对方文件的文件夹
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!PadIMApplication.getInstance().transferFileFinished) {// 如果本次文件已经接收过了则不再打开文件夹选择器
//                            Intent intent = new Intent(getActivity(), MyFileManager.class);
//                            intent.putExtra("selectType", Constant.SELECT_FILE_PATH);
//                            startActivityForResult(intent, 0);
                        	sendFile();
                            btn_ok.setEnabled(false);
                        }
                        // dialog.dismiss();
                    }
                });
                // 如果该按钮被点击则向服务层发送用户拒绝接收文件的广播
                btn_cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recDialog.dismiss();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新在线列表
     */
    public void updateExListAdapter() {
        List<Person> onLinePersons = PersonDao.getInstance().getAllPersons(
        		 PadIMApplication.getInstance().me.getUserId());
        adapter.setmList(onLinePersons);
        adapter.notifyDataSetChanged();
    }

    public static boolean isPaused = false;// 判断本身是不是可见
    private ArrayList<FileState> receivedFileNames = null;// 接收到的对方传过来的文件名
    private ReceiveSendFileListAdapter receiveFileListAdapter;
    private ReceiveSendFileListAdapter sendFileListAdapter;
    private ArrayList<FileState> beSendFileNames = null;// 发送到对方的文件名信息

    /**
     * 收到来自服务层的文件发送状态通知后，更新文件发送列表
     */
    public void updateFileSendState() {
    	Log.i("msg","isPaused="+isPaused);
    	if (!isPaused) {
            beSendFileNames = mService.getBeSendFileNames();// 获得当前所有文件发送状态
            sendFileListAdapter.setResources(beSendFileNames);
            sendFileListAdapter.notifyDataSetChanged();// 更新文件发送列表
    	}
    }

    /**
     * 弹出接收语音请求的对话框,当接收到对方语音请求时弹出
     */
    public void showReceivedTalkRequestDialog(final Person psn) {
        try {
            if (!isPaused) {
                isRemoteUserClosed = false;
                String title = String.format(getString(R.string.talk_with), psn.userName);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(PadIMApplication.getInstance().me.userName);
                builder.setMessage(title);
                builder.setIcon(PadIMApplication.getInstance().me.getHeadIconId());
                View vi = getActivity().getLayoutInflater().inflate(R.layout.request_talk_layout,
                        null);
                builder.setView(vi);

                final AlertDialog revTalkDialog = builder.show();
                revTalkDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface arg0) {
                        mService.stopTalk(psn.getIntUserId());
                    }
                });
                final Button talkTtOkBtn = (Button) vi.findViewById(R.id.receive_talk_tt_okbtn);

                final Button talkYsqOkBtn = (Button) vi.findViewById(R.id.receive_talk_ysq_okbtn);
                talkTtOkBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View okBtn) {
                        if (!isRemoteUserClosed) {// 如果远程用户未关闭通话，则向对方发送同意接收通话指令
                            mService.acceptTalk(psn.getIntUserId());
                            okBtn.setEnabled(false);
                            PadIMApplication.getInstance().setVoiceTingtong();
                            talkYsqOkBtn.setEnabled(false);
                        }
                    }
                });

                talkYsqOkBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View okBtn) {
                        if (!isRemoteUserClosed) {// 如果远程用户未关闭通话，则向对方发送同意接收通话指令
                            mService.acceptTalk(psn.getIntUserId());
                            okBtn.setEnabled(false);
                            talkTtOkBtn.setEnabled(false);
                            PadIMApplication.getInstance().setVoiceYangshengqi();
                        }
                    }
                });
                Button talkCancelBtn = (Button) vi.findViewById(R.id.receive_talk_cancel);
                talkCancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View cancelBtn) {
                        revTalkDialog.dismiss();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRemoteUserClosed = false; // 是否远程用户已经关闭了通话。

    /**
     * 收到来自服务层的文件接收状态通知后，更新文件接收列表
     */
    public void updateFileReceiveState() {
        if (!isPaused) {
            receivedFileNames = mService.getReceivedFileNames();// 获得当前所有文件接收状态
            receiveFileListAdapter.setResources(receivedFileNames);
            receiveFileListAdapter.notifyDataSetChanged();// 更新文件接收列表
        }
    }
}

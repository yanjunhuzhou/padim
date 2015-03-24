package com.example.padim;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.padim.adapters.ReceiveSendFileListAdapter;
import com.example.padim.dbutils.MessageDao;
import com.example.padim.dbutils.MessageData;
import com.example.padim.dbutils.Person;
import com.example.padim.dbutils.PersonDao;
import com.example.padim.model.BiaoQing;
import com.example.padim.model.FileName;
import com.example.padim.model.FileState;
import com.example.padim.services.ChartMsgBroadcastRecv;
import com.example.padim.services.MainService;
import com.example.padim.ui.ActionBar;
import com.example.padim.util.BiaoQingParseConfig;
import com.example.padim.util.Constant;
import com.example.padim.util.DateUtil;
import com.example.padim.util.ImReceiver;
import com.example.padim.util.PadImUtil;
import com.example.padim.widget.BiaoQingAdapter;
import com.example.padim.widget.BiaoQingEditText;
import com.example.padim.widget.BiaoQingTextView;

@SuppressLint("NewApi") public class SpecialChatMsgActivity extends Activity implements OnClickListener,
		ImReceiver {

	public static boolean chatting = false;
	private Person me = null;
	private BiaoQingEditText chartMsg = null;
	private Button chartMsgSend = null;
	private ImageView chartMsgBiaoqing = null;
	private LinearLayout chartMsgPanel = null;
	private MainService mService = null;
	private Intent mMainServiceIntent = null;
	private ChartMsgBroadcastRecv broadcastRecv = null;
	private IntentFilter bFilter = null;
	private ScrollView chartMsgScroll = null;

	private boolean isPaused = false;// 判断本身是否可见
	public boolean isRemoteUserClosed = false; // 是否远程用户已经关闭了通话。
	private boolean IsHideKeyBoard = true; //点击屏幕有些地方不隐藏键盘

	private ArrayList<FileState> receivedFileNames = null;// 接收到的对方传过来的文件名
	private ArrayList<FileState> beSendFileNames = null;// 发送到对方的文件名信息
	private ReceiveSendFileListAdapter receiveFileListAdapter = new ReceiveSendFileListAdapter(
			this);
	private ReceiveSendFileListAdapter sendFileListAdapter = new ReceiveSendFileListAdapter(
			this);
	static File recordfile = null;// 全局变量，储存录音文件名字信息,生命周期：录音、在对话框生成消息、结束
	boolean go = false;
	// 当发送或收到消息后把屏幕滚到最后一行
	private final Handler mHandler = new Handler();
	/** 表情列表 */
	public List<BiaoQing> bqList;
	/**
	 * MainService服务与当前Activity的绑定连接器
	 */
	private ServiceConnection sConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((MainService.ServiceBinder) service).getService();
			showMsg();// 如果服务连接成功则获取该用户的消息并显示
			System.out.println("Service connected to activity...");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			System.out.println("Service disconnected to activity...");
		}
	};
	PadIMApplication app;

	String msgPersons;
	// 不包括自己
	List<Person> persons = new ArrayList<Person>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_chart_layout);

		app = PadIMApplication.getInstance();

		Intent intent = getIntent();
		msgPersons = intent.getExtras().getString("msgPersons");
		Log.i("msg","msgPersons="+msgPersons);
		List<MessageData> msgs = MessageDao.getInstance().getMessages(
				msgPersons);

		// 第一次进来全部设置为已读
		
		for (MessageData d : msgs) {
			d.setUnRead(false);
			MessageDao.getInstance().createOrUpdate(d);
		}

		me = app.me;
		me.onLine = true;
		persons.clear();
		bqList = BiaoQingParseConfig.getBiaoQings(getApplicationContext());

		final String[] userIds = PadImUtil.getOtherPersons(me.getUserId(),
				msgPersons).split(",");
		StringBuffer sb = new StringBuffer();
		boolean limit = false; //当对话框的名字超过一定长度后，则不再添加用户名到对话框的名字中
		boolean UserOnline = false ; //用于判断是否有人在线，则显示相应的UI提示
		for (int i = 0; i < userIds.length; i++) {
			Person p = PersonDao.getInstance().getPersonByUserId(userIds[i]);
			if (p != null) {
				persons.add(p);
				if (p.isOnLine()) {
					UserOnline = true;
				}
				String name = p.getUserName();
				if ((sb.length() + name.length() + 1) > 25) {
					limit = true;
					continue;
				} else {
					if (sb.length() > 0) {
						sb.append(",");
					}
//					if (p.getUserId().equals(me.getUserId())) {//userIds不包括自己
//						name = "我";
//					}
					sb.append(name);
				}
			}
		}
		if (limit) {
			getMyActionBar().setTitle(
					sb.toString() + "等" + userIds.length + "人");
		} else {
			getMyActionBar().setTitle(sb.toString());
		}
		
		getMyActionBar().addRight("返回", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SpecialChatMsgActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		chartMsg = (BiaoQingEditText) findViewById(R.id.message_text_editor);
		chartMsgSend = (Button) findViewById(R.id.send_button);
		chartMsgSend.setOnClickListener(this);
		chartMsgBiaoqing = (ImageView) findViewById(R.id.biaoqing_insert);
		chartMsgBiaoqing.setOnClickListener(this);
		chartMsgPanel = (LinearLayout) findViewById(R.id.chart_msg_panel);
		chartMsgScroll = (ScrollView) findViewById(R.id.chart_msg_scroll);
		showMsg();
		// 当前Activity与后台MainService进行绑定
		mMainServiceIntent = new Intent(this, MainService.class);
		bindService(mMainServiceIntent, sConnection, BIND_AUTO_CREATE);
		startService(mMainServiceIntent);
		regBroadcastRecv();

		chartMsgPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE);
				if(imm != null) {
					imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
					
				}
				Log.i("msg", "chartMsgPanel");
			}
			
		});
		final RelativeLayout layout = (RelativeLayout) findViewById(R.id.recording);
		if (UserOnline) {
			layout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						MainService.isrecording = true;
						layout.setBackgroundResource(R.drawable.hold_to_talk_pressed);
						new Thread(new Runnable() {
							@Override
							public void run() {
								recordfile = mService.record();
								go = true;
							}
						}).start();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						MainService.isrecording = false;
						layout.setBackgroundResource(R.drawable.hold_to_talk_normal);
						new Thread(new Runnable(){
							@Override
							public void run() {
								while (!go);   //等待录音线程完全执行完后再继续操作
								go = false;
								for (int i = 0; i < userIds.length; i++) {
									Person p = PersonDao.getInstance()
											.getPersonByUserId(userIds[i]);
									if (p != null && (p.onLine == true)) {
										if(i >0) {
											PadIMApplication.getInstance().single = true;//防止在群组发送时多次触发RecordSendedAction
										}
										String ip = p.getIpAddress();
//										Log.i("msg","sendRecord"+ip);
//										Log.i("msg", "chat single="+PadIMApplication.getInstance().single);
										mService.sendRecord(ip, recordfile);
										
										while(!PadIMApplication.getInstance().voiceMsg); //未发完，循环等待
										
										PadIMApplication.getInstance().voiceMsg = false;
										if(!PadIMApplication.getInstance().single) {//向其他用户发送文字消息提示语音消息到来
											Log.i("msg","已成功发送音频文件");
											Intent intent = new Intent();
											intent.setAction(Constant.RecordSendedAction);
											PadIMApplication.getInstance().sendBroadcast(intent);
										}
									}
								}
								PadIMApplication.getInstance().single = false; //恢复初值，下次使用
							}
						}).start();
					}
					return false;
				}
			});
		} else {
			layout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_UP){
						Toast.makeText(SpecialChatMsgActivity.this, "无人在线", Toast.LENGTH_LONG).show();
					}
					return false;
				}
				
			});
		}
	}

	@Override
	protected void onResume() {
		isPaused = false;
		chatting = true;
		super.onResume();
		if (mService == null) {
			Log.i("msg", "mService=null");
		}
		Log.i("msg", "chat resume");
		Log.i("msg", "PadIMApplication.getInstance().hasFileTransfer="
				+ PadIMApplication.getInstance().hasFileTransfer);
		if (PadIMApplication.getInstance().hasFileTransfer) {
			PadIMApplication.getInstance().hasFileTransfer = false;
			showReceivedSendFileRequestDialog();
		}

		if (ContactsFragment.messagenotification != null) {
			ContactsFragment.messagenotificatiomanager.cancel(100000);
		}
		if (ContactsFragment.fileNotification != null) {
			ContactsFragment.messagenotificatiomanager.cancel(1000);
		}
	}
	
	public void initData() {
		showMsg();
	}

	ActionBar actionBar;

	public ActionBar getMyActionBar() {
		if (actionBar == null) {
			actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// finish();
					InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE);
					if(imm != null) {
						imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
					}
				}
			});
		}
		return actionBar;
	}

	View layout;

	@Override
	public void onClick(View vi) {
		Log.i("msg", "clicked");
		switch (vi.getId()) {
		case R.id.send_button:
			String msg = chartMsg.getText().toString();
			if (null == msg || msg.length() <= 0) {
				Toast.makeText(this, getString(R.string.content_is_empty),
						Toast.LENGTH_SHORT).show();
				return;
			}
			chartMsg.setText("");
			MessageData data = new MessageData();
			data.setContent(msg);
			data.setFrom(me.userId);
			data.setMsgPersons(msgPersons);
			String sendto = PadImUtil.getOtherPersons(me.userId, msgPersons);
			data.setTo(sendto);
			data.setUnRead(false);
			MessageDao.getInstance().createOrUpdate(data);

			showMsg();
			// #在显示的时候不要显示出来
			if (persons.size() >= 2) {
				msg = msg + "#" + msgPersons;
			}
			String[] a = sendto.split(",");
			for (int i = 0; i < a.length; i++) {
				try {
					mService.sendMsg(Integer.valueOf(a[i]), msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// mHandler.post(scrollRunnable);
			break;
		case R.id.biaoqing_insert:
			openBiaoQingDialog();
			// 选择文件
			// Intent intent = new Intent(this, MyFileManager.class);
			// intent.putExtra("selectType", Constant.SELECT_FILES);
			// startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			break;
		}
	}

	/**
	 * 设置表情文本
	 */
	public void setBiaoQingText(BiaoQing biaoQing) {
		chartMsg.setBiaoQingText(biaoQing.getPhrase());
	}

	/** 表情窗口 */
	public PopupWindow biaoQingDialog = null;
	/** 取照片 */
	public LinearLayout sendImage;
	/** 拍照 */
	public LinearLayout captureImage;

	/**
	 * 打开表情窗口
	 */
	public void openBiaoQingDialog() {
		LinearLayout menuView = (LinearLayout) getLayoutInflater().inflate(
				R.layout.biaoqing_grid, null, true);
		GridView bqGrid = (GridView) menuView.findViewById(R.id.biaoqing_grid);
		captureImage = (LinearLayout) menuView.findViewById(R.id.capture_image);
		sendImage = (LinearLayout) menuView.findViewById(R.id.send_image);

		BiaoQingAdapter adapter = new BiaoQingAdapter(getApplicationContext());
		adapter.setList(bqList);
		bqGrid.setAdapter(adapter);
		bqGrid.requestFocus();
		bqGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				BiaoQing biaoQing = bqList.get(arg2);
				setBiaoQingText(biaoQing);
				biaoQingDialog.dismiss();
			}
		});
		bqGrid.setOnKeyListener(new OnKeyListener() {// 焦点到了gridview上，所以需要监听此处的键盘事件。否则会出现不响应键盘事件的情况
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_MENU:
					if (biaoQingDialog != null && biaoQingDialog.isShowing()) {
						biaoQingDialog.dismiss();
					}
					break;
				}
				return true;
			}
		});

		// captureImage.setOnClickListener(clickLis);
		// sendImage.setOnClickListener(clickLis);

		biaoQingDialog = new PopupWindow(menuView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		biaoQingDialog.setBackgroundDrawable(new BitmapDrawable());
		biaoQingDialog.setAnimationStyle(R.style.PopupAnimation);
		biaoQingDialog.showAtLocation(findViewById(R.id.person_chart_layout),
				Gravity.CENTER | Gravity.CENTER, 0, 0);
		biaoQingDialog.update();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chart_menu, menu);
//		if(persons.size() > 1) {
//			Log.i("msg","多人");
//			if(menu.getItem(1) != null){
//				menu.getItem(1).setVisible(true);
//				Log.i("msg","执行");
//			}
//		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Person p = null;
		if (persons.size() > 0) {
			p = PersonDao.getInstance().getPersonByUserId(
					persons.get(0).getUserId());
		}
		
		
		switch (item.getOrder()) {
		case 1:
			Intent intent = new Intent(SpecialChatMsgActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
			break;
			
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (null != data) {
				int selectType = data.getExtras().getInt("selectType");
				if (selectType == Constant.SELECT_FILE_PATH) {// 如果收到的是文件夹选择模式，说明现在是要保存对方传过来的文件，则把当前选择的文件夹路径返回服务层
					String fileSavePath = data.getExtras().getString(
							"fileSavePath");
					Log.i("msg", "fileSavePath=" + fileSavePath);
					if (null != fileSavePath) {
						mService.receiveFiles(fileSavePath);
					} else {
						Toast.makeText(this,
								getString(R.string.folder_can_not_write),
								Toast.LENGTH_SHORT).show();
					}
				} else if (selectType == Constant.SELECT_FILES) {// 如果收到的是文件选择模式，说明现在是要发送文件，则把当前选择的所有文件返回给服务层。
					@SuppressWarnings("unchecked")
					final ArrayList<FileName> files = (ArrayList<FileName>) data
							.getExtras().get("files");
					mService.sendFiles(persons.get(0).getIntUserId(), files);// 把当前选择的所有文件返回给服务层

					// 显示文件发送列表
					beSendFileNames = mService.getBeSendFileNames();// 从服务层获得所有需要接收的文件的文件名
					Log.i("msg",
							"beSendFileNames.size()=" + beSendFileNames.size());
					if (beSendFileNames.size() <= 0)
						return;
					sendFileListAdapter.setResources(beSendFileNames);
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(me.userName);
					builder.setMessage(R.string.start_to_send_file);
					builder.setIcon(me.getHeadIconId());
					View vi = getLayoutInflater().inflate(
							R.layout.request_file_popupwindow_layout, null);
					builder.setView(vi);
					final AlertDialog fileListDialog = builder.show();
					final int toId = persons.get(0).getIntUserId();
					final int fromId = me.getIntUserId();
					fileListDialog
							.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface arg0) {

									Log.i("msg",
											"transferFileFinished"
													+ PadIMApplication
															.getInstance().transferFileFinished);
									if (PadIMApplication.getInstance().transferFileFinished) {
										for (int i = 0; i < beSendFileNames
												.size(); i++) {
											MessageData data = new MessageData();
											data.setTime(System
													.currentTimeMillis());
											data.setUnRead(false);
											data.setContent("成功发送文件："
													+ beSendFileNames.get(i).fileName);
											data.setFrom(me.userId);
											data.setMsgPersons(msgPersons);
											String sendto = PadImUtil
													.getOtherPersons(me.userId,
															msgPersons);
											data.setTo(sendto);
											data.setUnRead(false);
											MessageDao.getInstance()
													.createOrUpdate(data);
											showMsg();
											Log.i("msg", "showMsg");
										}
									}
									PadIMApplication.getInstance().transferFileFinished = false;
									beSendFileNames.clear();
									files.clear();
								}
							});
					ListView lv = (ListView) vi
							.findViewById(R.id.receive_file_list);// 需要接收的文件清单
					lv.setAdapter(sendFileListAdapter);
					Button btn_ok = (Button) vi
							.findViewById(R.id.receive_file_okbtn);
					btn_ok.setVisibility(View.GONE);
					Button btn_cancle = (Button) vi
							.findViewById(R.id.receive_file_cancel);
					// 如果该按钮被点击则打开文件选择器，并设置成文件夹选择模式，选择一个用来接收对方文件的文件夹
					btn_ok.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!PadIMApplication.getInstance().transferFileFinished) {// 如果本次文件已经接收过了则不再打开文件夹选择器
								Intent intent = new Intent(
										SpecialChatMsgActivity.this,
										MyFileManager.class);
								intent.putExtra("selectType",
										Constant.SELECT_FILE_PATH);
								startActivityForResult(intent, 0);
								// btn_ok.setEnabled(false);
							}
						}
					});
					// 如果该按钮被点击则向服务层发送用户拒绝接收文件的广播
					btn_cancle.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							fileListDialog.dismiss();
						}
					});
				}
			}
		}
	}

	// 广播接收器注册
	private void regBroadcastRecv() {
		broadcastRecv = new ChartMsgBroadcastRecv(this);
		bFilter = new IntentFilter();
		bFilter.addAction(Constant.hasMsgUpdatedAction);
		bFilter.addAction(Constant.receivedSendFileRequestAction);
		bFilter.addAction(Constant.fileReceiveStateUpdateAction);
		bFilter.addAction(Constant.fileSendStateUpdateAction);
		bFilter.addAction(Constant.receivedTalkRequestAction);
		bFilter.addAction(Constant.remoteUserRefuseReceiveFileAction);
		bFilter.addAction(Constant.dataReceiveErrorAction);
		bFilter.addAction(Constant.dataSendErrorAction);
		bFilter.addAction(Constant.remoteUserClosedTalkAction);
		bFilter.addAction(Constant.RecordSendedAction);
		registerReceiver(broadcastRecv, bFilter);
	}

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

	/**
	 * 收到来自服务层的文件发送状态通知后，更新文件发送列表
	 */
	public void updateFileSendState() {
		Log.i("msg", "isPaused=" + isPaused);
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
		if (!isPaused) {
			isRemoteUserClosed = false;
			String title = String.format(getString(R.string.talk_with),
					psn.userName);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SpecialChatMsgActivity.this);
			builder.setTitle(psn.userName);
			builder.setMessage(title);
			builder.setIcon(psn.getHeadIconId());
			View vi = getLayoutInflater().inflate(R.layout.request_talk_layout,
					null);
			builder.setView(vi);
			final AlertDialog revTalkDialog = builder.show();
			revTalkDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							mService.stopTalk(psn.getIntUserId());
						}
					});
			final Button talkTtOkBtn = (Button) vi
					.findViewById(R.id.receive_talk_tt_okbtn);
			final Button talkYsqOkBtn = (Button) vi
					.findViewById(R.id.receive_talk_ysq_okbtn);
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
						Log.i("msg", "psn.getIntUserId()=" + psn.getIntUserId());
						okBtn.setEnabled(false);
						talkTtOkBtn.setEnabled(false);
						PadIMApplication.getInstance().setVoiceYangshengqi();
					}
				}
			});
			Button talkCancelBtn = (Button) vi
					.findViewById(R.id.receive_talk_cancel);
			talkCancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View cancelBtn) {
					revTalkDialog.dismiss();
				}
			});
		}
	}

	public void sendFile() {
		String fileSavePath = PreferenceManager.getDefaultSharedPreferences(
				this).getString("downloadPath", Constant.downloadPath);
		Log.i("msg", "fileSavePath=" + fileSavePath);
		if (null != fileSavePath) {
			mService.receiveFiles(fileSavePath);
		} else {
			Toast.makeText(this, getString(R.string.folder_can_not_write),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 弹出是否接收文件请求的对话框,弹出一个提示框是否要接收发过来的文件
	 */
	public void showReceivedSendFileRequestDialog() {
		if (!isPaused) {// 如果自身处于可见状态则响应广播,弹出一个提示框是否要接收发过来的文件
			receivedFileNames = mService.getReceivedFileNames();// 从服务层获得所有需要接收的文件的文件名
			receiveFileListAdapter.setResources(receivedFileNames);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SpecialChatMsgActivity.this);
			builder.setTitle(persons.get(0).userName);
			builder.setMessage(R.string.sending_file_to_you);
			builder.setIcon(persons.get(0).getHeadIconId());
			View vi = getLayoutInflater().inflate(
					R.layout.request_file_popupwindow_layout, null);
			builder.setView(vi);
			final AlertDialog revFileDialog = builder.show();
			final int senderId = Integer.parseInt(persons.get(0).userId);
			final String senderName = persons.get(0).userName;

			revFileDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {

							if (!PadIMApplication.getInstance().transferFileFinished) {// 如果本次文件并未接收就关闭接收窗口，说明放弃本次接收，同时向远程发送一个拒绝接收的指令。
								Intent intent = new Intent();
								intent.setAction(Constant.refuseReceiveFileAction);
								sendBroadcast(intent);
							} else {// 接收完
								for (int i = 0; i < receivedFileNames.size(); i++) {
									MessageData data = new MessageData();
									data.setTime(System.currentTimeMillis());
									data.setUnRead(false);
									data.setContent("成功接收文件："
											+ receivedFileNames.get(i).fileName);
									data.setFrom(me.userId);
									data.setMsgPersons(msgPersons);
									String sendto = PadImUtil.getOtherPersons(
											me.userId, msgPersons);
									data.setTo(sendto);
									data.setUnRead(false);
									MessageDao.getInstance().createOrUpdate(
											data);
									showMsg();
								}
							}
							receivedFileNames.clear();
							PadIMApplication.getInstance().transferFileFinished = false;// 关闭文件接收对话框，本表示本次文件接收完成，把本次文件接收状态置为false
						}
					});
			ListView lv = (ListView) vi.findViewById(R.id.receive_file_list);// 需要接收的文件清单
			lv.setAdapter(receiveFileListAdapter);
			final Button btn_ok = (Button) vi
					.findViewById(R.id.receive_file_okbtn);
			Button btn_cancle = (Button) vi
					.findViewById(R.id.receive_file_cancel);

			// 如果该按钮被点击则打开文件选择器，并设置成文件夹选择模式，选择一个用来接收对方文件的文件夹
			btn_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!PadIMApplication.getInstance().transferFileFinished) {// 如果本次文件已经接收过了则不再打开文件夹选择器
						// Intent intent = new Intent(ChatMsgActivity.this,
						// MyFileManager.class);
						// intent.putExtra("selectType",
						// Constant.SELECT_FILE_PATH);
						// startActivityForResult(intent, 0);
						sendFile();
						btn_ok.setEnabled(false);
					}
				}
			});

			// 如果该按钮被点击则向服务层发送用户拒绝接收文件的广播
			btn_cancle.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					revFileDialog.dismiss();
				}
			});
		}
	}

	// /**
	// * 接受远程语音呼叫
	// * @param personId
	// */
	// public void acceptTalk(int personId) {
	// mService.acceptTalk(personId);
	// }

	public static final long MAX_BETWEEN = 1000 * 6;

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;
		chatting = false;
		if (mService == null) {
			Log.i("msg", "mService=null");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(sConnection);
		unregisterReceiver(broadcastRecv);
	}

	// 根据用户id从service层获得该用户消息，并显示该消息
	public void showMsg() {
		List<MessageData> msgs = MessageDao.getInstance().getMessages(
				msgPersons);
		for (int i = 0; i < persons.size(); i++) {
			List<MessageData> list = app.msgContainer.get(persons.get(i)
					.getIntUserId());
			if (list != null) {
				list.clear();
			}

		}

		if (null != msgs) {
			chartMsgPanel.removeAllViews();
			while (msgs.size() > 0) {
				View view = null;
				final MessageData msg = msgs.remove(msgs.size() - 1);
				if (msg.getFrom().equals(me.getUserId())) {
					view = getLayoutInflater().inflate(R.layout.im_item_to,
							null);
				} else {
					view = getLayoutInflater().inflate(R.layout.im_item_from,
							null);
				}

				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.i("msg", "view oncliced");
					}
				});
				
				TextView messageTimeTV = (TextView) view
						.findViewById(R.id.comment_time);
				final BiaoQingTextView messageContentTV = (BiaoQingTextView) view
						.findViewById(R.id.comment_content);
				messageContentTV.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.i("msgcontent","点击事件成功");
					}
				});

				ImageView messagerAvaterIV = (ImageView) view
						.findViewById(R.id.commenter_avatar);
				TextView userNameTV = (TextView) view
						.findViewById(R.id.user_name);

				for (Person p : persons) {
					if (p.getUserId().equals(msg.getFrom())) {
						if (messagerAvaterIV != null) {
							if (p.isOnLine()) {
								messagerAvaterIV
										.setImageResource(R.drawable.pic_online_male);
							} else {
								messagerAvaterIV
										.setImageResource(R.drawable.pic_offline_male);
							}
						}
						if (userNameTV != null) {
							userNameTV.setText(p.getUserName());
						}
						break;
					}
				}

				messageContentTV.setBiaoQingText(msg.getContent());
				Log.i("msgcontent",msg.getContent());
				
				
				if(msg.getContent().equals("语音消息")) {
					Log.i("msg","音频相关");
					final ImageView red = (ImageView) view.findViewById(R.id.unread_talk);
					if(msg.getFrom().equals(me.getUserId())){

						final String recordname = Environment
								.getExternalStorageDirectory()
								+ File.separator+"PadIM"+File.separator+"voice"+ File.separator+"send"+File.separator
								+ DateUtil.getTimeStr2(msg.getTime()).trim() +".pcm";
						Log.i("msg","recordname="+recordname);
						File file = new File(recordname);
						if (file.exists()) {
							messageContentTV
									.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											Resources dd = getResources();
											messageContentTV.setBackground(dd.getDrawable(R.drawable.sms_out_pressed_bg));
											new Thread() {
												@Override
												public void run() {
													// TODO Auto-generated
													// method stub
													super.run();
													// 播放
													try {
														InputStream in = new FileInputStream(
																recordname);
														BufferedInputStream bis = new BufferedInputStream(
																in);
														DataInputStream din = new DataInputStream(
																bis);

														// 获得音频缓冲区大小
														int bufferSize = android.media.AudioTrack
																.getMinBufferSize(
																		PadIMApplication
																				.getInstance().sampRate,
																		AudioFormat.CHANNEL_CONFIGURATION_MONO,
																		AudioFormat.ENCODING_PCM_16BIT);
														Log.e("",
																"播放缓冲区大小"
																		+ bufferSize);
														// 获得音轨对象
														AudioTrack player = new AudioTrack(
																AudioManager.STREAM_MUSIC,
																PadIMApplication
																		.getInstance().sampRate,
																AudioFormat.CHANNEL_CONFIGURATION_MONO,
																AudioFormat.ENCODING_PCM_16BIT,
																bufferSize,
																AudioTrack.MODE_STREAM);

														// 设置喇叭音量
														player.setStereoVolume(
																1.0f, 1.0f);
														// 开始播放声音
														Log.i("msg","开始播放自己音乐");
														player.play();
														byte[] audio = new byte[160];
														int length = 0;
														while ((length = din
																.read(audio)) > 0 && !isPaused) {
															byte[] temp = audio
																	.clone();
															if (length > 0)
																player.write(
																		audio,
																		0,
																		temp.length);
														}
														player.stop();
														player.release();
														player = null;
														Log.i("msg","停止自己播放音乐");
														in.close();
													} catch (IOException e) {
														e.printStackTrace();
													}

												}
											}.start();
										}

									});
						} else {
							Log.i("msg", "无监听事件");
							messageContentTV.setOnClickListener(new OnClickListener(){

								@Override
								public void onClick(View v) {
									Toast.makeText(getApplicationContext(), "音频消息已删除", Toast.LENGTH_LONG).show();
								}
							});
						}
					} else {
						final MessageData da = msg;
						if(!da.isPlay())
							red.setVisibility(View.VISIBLE);
						final String recordname = Environment
								.getExternalStorageDirectory()
								+ File.separator+"PadIM"+File.separator+"voice"+File.separator+"rec"+File.separator
								+ DateUtil.getTimeStr2(msg.getTime()).trim() + ".pcm";
						File file = new File(recordname);
						Log.i("msg"," recv recordname="+recordname);
						if (file.exists()) {
							Log.i("msg", "文件存在");
							messageContentTV
									.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											da.setPlay(true);
											MessageDao.getInstance().createOrUpdate(da);
											red.setVisibility(View.GONE);
											Resources dd = getResources();
											messageContentTV.setBackground(dd.getDrawable(R.drawable.sms_in_pressed_bg));
											new Thread() {
												@Override
												public void run() {
													// TODO Auto-generated
													// method stub
													super.run();
													// 播放
													try {
														InputStream in = new FileInputStream(
																recordname);
														BufferedInputStream bis = new BufferedInputStream(
																in);
														DataInputStream din = new DataInputStream(
																bis);

														// 获得音频缓冲区大小
														int bufferSize = android.media.AudioTrack
																.getMinBufferSize(
																		PadIMApplication
																				.getInstance().sampRate,
																		AudioFormat.CHANNEL_CONFIGURATION_MONO,
																		AudioFormat.ENCODING_PCM_16BIT);
														Log.e("",
																"播放缓冲区大小"
																		+ bufferSize);
														// 获得音轨对象
														AudioTrack player = new AudioTrack(
																AudioManager.STREAM_MUSIC,
																PadIMApplication
																		.getInstance().sampRate,
																AudioFormat.CHANNEL_CONFIGURATION_MONO,
																AudioFormat.ENCODING_PCM_16BIT,
																bufferSize,
																AudioTrack.MODE_STREAM);

														// 设置喇叭音量
														player.setStereoVolume(
																1.0f, 1.0f);
														// 开始播放声音
														Log.i("msg","开始播放音乐");
														player.play();
														byte[] audio = new byte[160];
														int length = 0;
														while ((length = din
																.read(audio)) > 0) {
															byte[] temp = audio
																	.clone();
															if (length > 0)
																player.write(
																		audio,
																		0,
																		temp.length);
														}
														player.stop();
														player.release();
														player = null;
														Log.i("msg","停止播放音乐");
														in.close();
														
														
														
													} catch (IOException e) {
														e.printStackTrace();
													}

												}
											}.start();
										}

									});
						} else {
							red.setVisibility(View.GONE);
							messageContentTV
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Log.i("msg", "文件不存在");
									Toast.makeText(getApplicationContext(), "音频消息已删除", Toast.LENGTH_LONG).show();
								}
								
							});
						}
					}
				}
				
				messageTimeTV.setText(DateUtil.getTimeStr2(msg.getTime()));

				chartMsgPanel.addView(view);
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						chartMsgScroll.fullScroll(View.FOCUS_DOWN);
					}
				});
			}
		}
	}

	@Override
	public void onReceive(String type, Object obj) {

		if (type.equals(Constant.hasMsgUpdatedAction)) {
			if (!isPaused)
				showMsg();
		} else if (type.equals(Constant.dataReceiveErrorAction)
				|| type.equals(Constant.dataSendErrorAction)) {
			Toast.makeText(this, obj.toString(), Toast.LENGTH_SHORT).show();
		} else if (type.equals(Constant.fileSendStateUpdateAction)) {// 收到来自服务层的文件发送状态通知
			updateFileSendState();
		} else if (type.equals(Constant.fileReceiveStateUpdateAction)) {// 收到来自服务层的文件接收状态通知
			updateFileReceiveState();
		} else if (type.equals(Constant.receivedTalkRequestAction)) {
			final Person psn = (Person) obj;
			showReceivedTalkRequestDialog(psn);
		} else if (type.equals(Constant.remoteUserClosedTalkAction)) {
			isRemoteUserClosed = true;// 如果接收到远程用户关闭通话指令则把该标记置为true
			final Person psn = (Person) obj;
			Toast.makeText(this, "[" + psn.userName + "]关闭与你的通话了...",
					Toast.LENGTH_SHORT).show();
		} else if (type.equals(Constant.remoteUserRefuseReceiveFileAction)) {
			Toast.makeText(this, getString(R.string.refuse_receive_file),
					Toast.LENGTH_SHORT).show();
		} else if (type.equals(Constant.receivedSendFileRequestAction)) {
			showReceivedSendFileRequestDialog();

		} else if ( type.equals(Constant.RecordSendedAction)) {
			MessageData data = new MessageData();
			String msg = "语音消息";
			data.setTime(PadIMApplication.getInstance().sendTime);
			data.setUnRead(false);
			data.setContent(msg);
			data.setFrom(me.userId);
			data.setMsgPersons(msgPersons);
			String sendto = PadImUtil.getOtherPersons(me.userId, msgPersons);
			data.setTo(sendto);
			data.setUnRead(false);
			MessageDao.getInstance().createOrUpdate(data);
			showMsg();
			
			if(persons.size() >= 2){
				msg = msg + "#" +
			msgPersons;
			}
			String[] b = sendto.split(",");	
			for(int i=0;i<b.length;i++){
				try{
					mService.sendMsg(Integer.valueOf(b[i]), msg);
				}catch(Exception e ){
					e.printStackTrace();
				}
			}
			recordfile = null;
		}
	}
}

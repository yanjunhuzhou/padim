package com.example.padim;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.padim.adapters.ViewPagerAdapter;
import com.example.padim.dbutils.MessageDao;
import com.example.padim.dbutils.MessageData;
import com.example.padim.dbutils.Person;
import com.example.padim.dbutils.PersonDao;
import com.example.padim.ui.ActionBar;
import com.example.padim.ui.MyDialog;
import com.example.padim.util.Constant;
import com.example.padim.util.PadImUtil;
import com.example.padim.util.PhoneStateUtil;

import de.greenrobot.event.EventBus;

public class MainActivity extends BasePagerActivity {

    public static final int FRAGMENT_ONE = 0;
    public static final int FRAGMENT_TWO = 1;
    
    private void init() {
        int myUserId = PadImUtil.getMyId();

        PadIMApplication.getInstance().me = PersonDao.getInstance()
                .getPersonByUserId(myUserId + "");
        Person p = PersonDao.getInstance().getPersonByUserId(myUserId + "");
        String IMEI = PhoneStateUtil.getIMEI(PadIMApplication.getInstance()
				.getApplicationContext());
        Log.i("msg","Main IMEI="+IMEI);
        String UserName = IMEI.substring(IMEI.length()-2) + Build.MODEL;
        if (p == null) {
            p = new Person();
            p.setUserName(UserName);
            p.setUserId(myUserId + "");
            p.setHeadIconId(0);
            PersonDao.getInstance().createOrUpdate(p);
            Editor et = PreferenceManager.getDefaultSharedPreferences(this).edit();
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
    
    
@Override
public boolean dispatchTouchEvent(MotionEvent ev) {
	switch (ev.getAction()){
	case MotionEvent.ACTION_DOWN:
		Log.i("msg","按下");
		break;
	case MotionEvent.ACTION_UP:
		if(HistoricalFragment.ImageClicked == 0 && HistoricalFragment.LongClicked ==0){//ss未响应
			HistoricalFragment.ss.setVisibility(View.GONE);
		}
		HistoricalFragment.LongClicked = 0 ;
		Log.i("msg","弹起");
		break;
	}
//	onTouchEvent(ev);
	return super.dispatchTouchEvent(ev);
//	return true;  //为什么viewpager不响应，不移动
//	return false;  //为什么viewpager不响应，不移动
}
    
@Override
public boolean onTouchEvent(MotionEvent event) {
	switch (event.getAction()){
	case MotionEvent.ACTION_DOWN:
		if(HistoricalFragment.ss.getVisibility() == 0) {//visible
			HistoricalFragment.ss.setVisibility(View.INVISIBLE);
			Log.i("msg","touch 处理了图片");
		}else if(HistoricalFragment.ss.getVisibility() == 4) {//invisible
			Log.i("msg","allan");
		} else if(HistoricalFragment.ss.getVisibility() == 8) {
			Log.i("msg","Hellen"); //Gone
		}
		Log.i("msg","onTouchEvent按下");
		break;
	case MotionEvent.ACTION_UP:
		
		Log.i("msg","onTouchEvent弹起");
		break;
	}
	return super.onTouchEvent(event);
}

    @Override
    protected void onCreate(Bundle bundle) {
    	super.onCreate(bundle);
        if (!PadImUtil.isConnected(this)) {
            finish();
            Toast.makeText(getApplicationContext(), "无网络连接，打开失败", Toast.LENGTH_LONG).show();
            return;
        }
        // 确保初始化，有的机子applaication不先运行
        init(); 
        List<Person> list = PersonDao.getInstance().getAllPersons();
        // 所有先变为未上线
        for (Person p : list) {
            p.onLine = false;
            PersonDao.getInstance().createOrUpdate(p);
        }
        
        getMyActionBar().setTitle(getResources().getString(R.string.app_name));
        getMyActionBar().addLeft("退出", new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });

        getMyActionBar().addRight("群组", new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GroupCheckActivity.class);
                startActivity(intent);
            }
        });
        EventBus.getDefault().register(this);

        List<MessageData> temp = MessageDao.getInstance().getHisMessagesGroupByHF();

        for (MessageData t : temp) {
            if (t.isUnRead()) {
                mViewPager.setCurrentItem(1);
                break;
            }
        }
    }

   @Override
protected void onResume() {
	super.onResume();
	Log.i("msg","mainactivity resume");
}
   
   
   
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        new Thread(killProcessRun).start();
    }

    Runnable killProcessRun = new Runnable() {

        @Override
        public void run() {
            try {
                Thread.sleep(300);
                System.out.println("----------------killProcess");
                android.os.Process.killProcess(android.os.Process.myPid());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    public void onEvent(String event) {
        if (event != null) {
            if (event.equals("newgroupchat")) {
                mViewPager.setCurrentItem(1);
            }
        }
    }

    // 设置菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, 0, 0, "设置");
        menu.add(0, 1, 1, "关于");
        menu.add(0, 2, 2, "退出");
        return super.onCreateOptionsMenu(menu);
    }

    // 设置菜单监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId())// 菜单序号
        {
            case 0:
            // 系统设置
            {
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
            }
                break;
            case 1:// 关于程序
            {

                String versionName = null;
                String applicationName = null;
                try {
                    // ---get the package info---
                    PackageManager pm = getPackageManager();
                    PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
                    versionName = pi.versionName;
                    applicationName = (String) pm.getApplicationLabel(pi.applicationInfo);
                    if (versionName == null || versionName.length() <= 0) {
                        versionName = "";
                    }
                    if (applicationName == null || applicationName.length() <= 0)
                        applicationName = "";
                } catch (Exception e) {
                    Log.e("VersionInfo", "Exception", e);
                }
                new AlertDialog.Builder(this).setTitle("关于" + applicationName)
                        .setMessage("版本号: " + versionName)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
                break;
            case 2:// 退出程序
            {
                // 杀掉线程强制退出
            	Intent intent = new Intent (Intent.ACTION_MAIN);
            	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	intent.addCategory(Intent.CATEGORY_HOME);
            	startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
                break;
        }
        return true;
    }

    ActionBar actionBar;

    public ActionBar getMyActionBar() {
        if (actionBar == null) {
            actionBar = (ActionBar) findViewById(R.id.actionbar);
            actionBar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // finish();
                }
            });
        }
        return actionBar;
    }

    @Override
    public ViewPagerAdapter getAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        adapter.addPager("联系人", ContactsFragment.class, bundle);
        bundle = new Bundle();
        adapter.addPager("历史消息", HistoricalFragment.class, bundle);
        bundle = new Bundle();
        adapter.addPager("特殊历史消息",SpecialHistoryFragment.class,bundle);
        return adapter;
    }

    public MyDialog dialog;

    public void showMyDialog(String msg) {
        try {
            dialog = new MyDialog(this);
            dialog.showDialog(this, "温馨提示", msg, "确定", null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMyDialog(String msg, String btnText1, String btnText2,
            View.OnClickListener listener) {
        try {
            dialog = new MyDialog(this);
            dialog.showDialog(this, "温馨提示", msg, btnText1, btnText2, listener, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMyDialog(String title, String msg, String btnText1, String btnText2,
            View.OnClickListener listener) {
        try {
            dialog = new MyDialog(this);
            dialog.showDialog(this, title, msg, btnText1, btnText2, listener, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMyDialog(String title, String msg, String btnText1, String btnText2,
            View.OnClickListener listener, View.OnClickListener listener2) {
        try {
            dialog = new MyDialog(this);
            dialog.showDialog(this, title, msg, btnText1, btnText2, listener, listener2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                // showDialog(DIALOG_EXIT_MSG);
                try {
                    showExitDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void showExitDialog() {
        try {
            MyDialog dialog = new MyDialog(this);
            String text = "是否退出" + getString(R.string.app_name) + "?";
            dialog.showDialog(this, "退出确认", text, "确定", "取消", new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                	Intent intent = new Intent (Intent.ACTION_MAIN);
                	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                	intent.addCategory(Intent.CATEGORY_HOME);
                	startActivity(intent);
                    finish();
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
       
    private void startUserapp() {
    	Intent mIntent = new Intent( );
    	mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	ComponentName comp = new ComponentName("com.example.android.loggin", "com.example.android.loggin.helloactivity");
    	mIntent.setComponent(comp);
    	mIntent.setAction("android.intent.action.VIEW");
    	try {
    	    startActivity(mIntent);
    	} catch (ActivityNotFoundException e) {
    	      Log.w("msg", "No activity to handle assist action.", e);
    	}

    }
    
}



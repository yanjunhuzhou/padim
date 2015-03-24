package com.example.padim;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

/**
 * 
 * 项目名称：PadIM 类名称：SplashActivity 类描述：启动界面 创建人：ystgx 创建时间：2014年3月23日 下午9:31:00
 * 
 * @version
 */
public class SplashActivity extends Activity {

	private String sendPath = Environment.getExternalStorageDirectory()
			+ File.separator+"PadIM"+File.separator+"voice"+File.separator+"send"+File.separator;
	private String revPath = Environment.getExternalStorageDirectory()
			+ File.separator+"PadIM"+File.separator+"voice"+File.separator+"rec"+File.separator;
	private long sevenDay = 7*24*3600*1000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);
		startMain();
		deleteOldFile();

	}

	private void startMain() {
		// TODO Auto-generated method stub
		// 延迟1秒钟加载主页面
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this,
						MainActivity.class);
				startActivity(intent);
				SplashActivity.this.finish();
			}

		}, 2000);
	}
	
	private void deleteOldFile() {
		File path = new File(sendPath);
		File rpath = new File(revPath);
		long now = System.currentTimeMillis();
		
		if(path.exists()) { //不存在则不用删除
			File[] files = path.listFiles();
			if (null != files) {
				for (File file : files) {
					 if (now - file.lastModified() > sevenDay)
						 file.delete();
				}
			}
		}
		
		if(rpath.exists()) {
			File[] files = rpath.listFiles();
			if(null != files) {
				for (File file : files) {
					if(now - file.lastModified() > sevenDay)
						file.delete();
				}
			}
		}
		
	}

}

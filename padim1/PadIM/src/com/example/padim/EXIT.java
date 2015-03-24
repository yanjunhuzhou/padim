package com.example.padim;

import java.awt.Window;

import com.example.padim.util.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
public class EXIT extends Activity {
	TextView messagetext;
     
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
       

		
		
		
		setContentView(R.layout.exit_dialog);
		Intent intent = getIntent();
		int index = intent.getIntExtra("special", -1);
		messagetext = (TextView) findViewById(R.id.mintext);
		Button btn = (Button) findViewById(R.id.exitBtn);
		Log.i("msg", "index="+index);
		messagetext.setText(Constant.specialComd[index]+","+index);
		Log.i("msg","first"+ Constant.specialComd[index]+"index="+index);
		
		// TODO Auto_generated method stub
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			finish(); 
			}

		});

	}
	
	
    protected void onPause(){
    	super.onPause();
         
    	Log.i("msg","onPause is working");
       
    	
    	
    }
    
    protected void onResume(){
    	 android.view.Window win = getWindow();

 	    win.addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
 		            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD  
 		            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON  
 		            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);  
 	 
    	super.onResume();
    	Log.i("msg","onResume is working");
    //	 acquireWakeLock();
    }
    private void acquireWakeLock()
    {
    	Log.i("wakelock","recute");
    	WakeLock wakelock = null;
     	if(wakelock == null)
     	{
     		PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
     	  if (!pm.isScreenOn()) {  
     			    
     			         
     			   wakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP, "PostLocationService");
     			   wakelock.acquire();  
     			  // wakelock.release();
     			}  
     		
     	}
     }
    
  
    
    


	protected void onNewIntent(Intent intent) {
		
		 
		Log.i("msg", "afafaf");
//		intent = getIntent();
		int index = intent.getIntExtra("special", -1);
		Log.i("msg", Constant.specialComd[index]+"index="+index);
//		messagetext.setText("what");
		messagetext.setText(Constant.specialComd[index]+","+index);
		
	}

}

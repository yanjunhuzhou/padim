package com.example.padim;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.widget.Toast;

public class MyFragmentActivity extends FragmentActivity {

	protected boolean isShowing;

	protected Handler handler = new Handler();

	@Override
	protected void onResume() {
		super.onResume();
		try {
			isShowing = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		isShowing = false;
	}

	public void showLongToast(String msg) {
		if (msg != null) {
			Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	public void showToast(int msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

}

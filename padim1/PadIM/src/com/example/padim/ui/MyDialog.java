/**
 * Created on @date 2012-11-21
 */
package com.example.padim.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.padim.R;

/**
 * @author hekk
 */
public class MyDialog extends Dialog {

	/**
	 * @param activity
	 */
	public MyDialog(Activity activity) {
		super(activity);
	}

	Button btn1;
	Button btn2;
	Button btn3;
	TextView title;
	TextView msg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		switch (type) {
		case TYPE_DUIHUAKUANG:
			setContentView(R.layout.mydailog);
			initviews();
			break;

		}
	}

	public void setSelect(View[] view, int index) {
		for (int i = 0; i < view.length; i++) {
			if (i != index) {
				view[i].setSelected(false);
			} else {
				view[i].setSelected(true);
			}
		}
	}

	private int type = TYPE_DUIHUAKUANG;
	private static final int TYPE_DUIHUAKUANG = 1;// 普通对话框

	Handler handler = new Handler();
	String titleStr;
	String msgStr;
	String text1;
	String text2;
	String text3;
	View.OnClickListener listener1;
	View.OnClickListener listener2;
	View.OnClickListener listener3;

	private void initviews() {
		btn1 = (Button) findViewById(R.id.btn1);
		btn2 = (Button) findViewById(R.id.btn2);
		btn3 = (Button) findViewById(R.id.btn3);
		title = (TextView) findViewById(R.id.title);
		msg = (TextView) findViewById(R.id.msg);

		btn1.setText(text1);
		btn2.setText(text2);
		btn3.setText(text3);
		if (text1 != null && text1.length() > 3) {
			btn1.setTextSize(14);
		}
		if (text2 != null && text2.length() > 3) {
			btn2.setTextSize(14);
		}
		if (text3 != null && text3.length() > 3) {
			btn3.setTextSize(14);
		}
		if (!TextUtils.isEmpty(text3)) {
			btn3.setVisibility(View.VISIBLE);
		} else {
			btn3.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(text2)) {
			btn2.setVisibility(View.VISIBLE);
		} else {
			btn2.setVisibility(View.GONE);
		}

		btn1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (listener1 != null) {
						listener1.onClick(v);
					}
					dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		btn2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (listener2 != null) {
						listener2.onClick(v);
					}
					dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		btn3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (listener3 != null) {
						listener3.onClick(v);
					}
					dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		title.setText(titleStr);
		if (msgStr.contains("<br/>")) {
			msg.setText(Html.fromHtml(msgStr));
		} else {
			msg.setText(msgStr);
		}
	}

	public void showDialog(Activity activity, String titleStr, String msgStr,
			String text1, String text2, View.OnClickListener listener1,
			View.OnClickListener listener2) {
		type = TYPE_DUIHUAKUANG;
		this.titleStr = titleStr;
		this.msgStr = msgStr;
		this.text1 = text1;
		this.text2 = text2;
		this.listener1 = listener1;
		this.listener2 = listener2;
		this.activity = activity;
		show();
		setDialogWidth();
	}

	public void showDialog(Activity activity, String titleStr, String msgStr,
			String text1, String text2, String text3,
			View.OnClickListener listener1, View.OnClickListener listener2,
			View.OnClickListener listener3) {
		type = TYPE_DUIHUAKUANG;
		this.titleStr = titleStr;
		this.msgStr = msgStr;
		this.text1 = text1;
		this.text2 = text2;
		this.text3 = text3;
		this.activity = activity;
		this.listener1 = listener1;
		this.listener2 = listener2;
		this.listener3 = listener3;
		show();
		setDialogWidth();
	}

	Activity activity;

	private void setDialogWidth() {
		WindowManager windowManager = activity.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		if (listener3 != null) {
			lp.width = (display.getWidth()/2); // 设置宽度
		} else {
			lp.width = display.getWidth()/2; // 设置宽度
		}

		getWindow().setAttributes(lp);
	}

}

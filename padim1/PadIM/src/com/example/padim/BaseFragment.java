package com.example.padim;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import de.greenrobot.event.EventBus;

public abstract class BaseFragment extends Fragment {

	protected boolean isShowing = false;                           

	public void onShow() {
		isShowing = true;
	}

	public void onEvent(ViewPagerEvent event) {
		if (event.getAction().equals("onShow")) {
			if (event.getName() != null && event.getName().equals(getTagName())) {
				onShow();
			} else {
				isShowing = false;
			}
		}
	}

	public String getTagName() {
		String url = getArguments().getString("KEY_URL");
		return url;
	}

	@Override
	public void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
	}
}

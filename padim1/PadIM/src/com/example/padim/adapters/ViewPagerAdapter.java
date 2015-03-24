package com.example.padim.adapters;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.example.padim.BaseFragment;
import com.example.padim.PadIMApplication;
import com.example.padim.ViewPagerEvent;

import de.greenrobot.event.EventBus;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

	public static final String ACTION = "onShow";

	ArrayList<PagerInfo> list = new ArrayList<PagerInfo>();

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		PagerInfo info = list.get(position);
		BaseFragment fragment = (BaseFragment) Fragment.instantiate(
				PadIMApplication.getInstance(), info.getFragment().getName(),
				info.getBundle());
		return fragment;
	}

	private int lastPosition = -1;

	private boolean isFirst = true;

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		BaseFragment fragment = (BaseFragment) getItem(position);
		// 第一次执行会不正确，屏蔽掉
		if (isFirst) {
			isFirst = false;
			return;
		}
		if (lastPosition != position) {
			lastPosition = position;
			// 调用fragment onshow
			EventBus.getDefault()
					.post(new ViewPagerEvent(ACTION, fragment.getTagName(),
							position));
		}
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public String getPageTitle(int position) {
		return list.get(position).getTitle();
	}

	public void setTitle(int pos, String title) {
		if (list.size() > pos) {
			PagerInfo info = list.get(pos);
			list.set(pos,
					new PagerInfo(title, info.getFragment(), info.getBundle()));
		}
	}

	public void addPager(String title, Class fragment, Bundle bundle) {
		bundle.putString("KEY_URL", title);
		PagerInfo info = new PagerInfo(title, fragment, bundle);
		list.add(info);
	}

	private class PagerInfo {

		private String title;

		private Class fragment;

		private Bundle bundle;

		public PagerInfo(String title, Class fragment, Bundle bundle) {
			this.title = title;
			this.fragment = fragment;
			this.bundle = bundle;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String str) {
			this.title = str;
		}

		public Class getFragment() {
			return fragment;
		}

		public Bundle getBundle() {
			return bundle;
		}

	}
}

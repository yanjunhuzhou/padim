package com.example.padim;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.example.padim.adapters.ViewPagerAdapter;

public abstract class BasePagerActivity extends MyFragmentActivity {
	public abstract ViewPagerAdapter getAdapter();

	protected PagerSlidingTabStrip tabs;
	protected ViewPager mViewPager;

	protected int tabIndex;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(getLayout());
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		tabs.setIndicatorColor(getResources()
				.getColor(R.color.orange_top_color));
		tabs.setOnPageChangeListener(new XonPageChangeListener());
		tabs.setTextSize(20);
		tabs.setTabPaddingLeftRight(12);
		tabs.setShouldExpand(isExpand());
		tabs.setIndicatorHeight(4);
		final int pageMargin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
						.getDisplayMetrics());
		mViewPager.setPageMargin(pageMargin);
		// 最好不要用2，比较卡
		mViewPager.setOffscreenPageLimit(11);
		ViewPagerAdapter adapter = getAdapter();
		if (adapter != null) {
			mViewPager.setAdapter(adapter);
		}
		tabs.setViewPager(mViewPager);
		tabIndex = getIntent().getIntExtra("KEY_INTENT_TAB_CHANGE", 0);

		if (tabIndex > 0) {
			selectePage(tabIndex);
		}

		mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@SuppressWarnings("deprecation")
					@SuppressLint("NewApi")
					@Override
					public void onGlobalLayout() {

						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
							mViewPager.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
						} else {
							mViewPager.getViewTreeObserver()
									.removeOnGlobalLayoutListener(this);
						}
						onGlobalLayoutCompeleteAfterOnCreate();
					}
				});
	}

	protected boolean isExpand() {
		return true;
	}

	protected int getLayout() {
		return R.layout.commen_viewpager_activity;
	}

	protected void onGlobalLayoutCompeleteAfterOnCreate() {

	}

	public int getCurrentIndex() {
		return mViewPager.getCurrentItem();
	}

	public void selectePage(int index) {
		mViewPager.setCurrentItem(index);
	}

	private class XonPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int key) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int index) {
			BasePagerActivity.this.onPageSelected(index);
		}
	}

	protected void onPageSelected(int index) {

	}
}

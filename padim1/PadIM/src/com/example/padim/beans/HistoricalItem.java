package com.example.padim.beans;

import com.example.utils.TimeTransformer;

public class HistoricalItem extends DeviceItem {

	private String mLastContent;
	private String mLastDate;

	public String getmLastContent() {
		return mLastContent;
	}

	public void setmLastContent(String mLastContent) {
		this.mLastContent = mLastContent;
	}

	public String getmLastDate() {
		return mLastDate;
	}

	public long lastDateTime = 0;

	public void setmLastDate(long mLastDate) {
		lastDateTime = mLastDate;
		this.mLastDate = TimeTransformer.longToTime(mLastDate);
	}

}

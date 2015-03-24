/**
 * 
 */
package com.example.padim.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.padim.R;
import com.example.padim.model.BiaoQing;

/**
 *表情grid的adapter 
 *@author zhuft
 *@version 1.0
 *2011-8-23
 */
public class BiaoQingAdapter extends BaseAdapter {

	private class GridHolder {
		ImageView biaoQingImage;
	}

	private Context context;

	private LayoutInflater mInflater;

	/** 表情列表 */
	private List<BiaoQing> bq;

	public BiaoQingAdapter(Context c) {
		super();
		this.context = c;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setList(List<BiaoQing> bq) {
		this.bq = bq;
	}

	public int getCount() {
		// TODO Auto-generated method stub

		return bq.size();
	}

	@Override
	public Object getItem(int index) {

		return bq.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		GridHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.biaoqing_grid_item, null);
			holder = new GridHolder();
			holder.biaoQingImage = (ImageView) convertView.findViewById(R.id.item_image);
			convertView.setTag(holder);
			holder.biaoQingImage.setImageResource(bq.get(index).getImageSourceId());
		} else {
			holder = (GridHolder) convertView.getTag();
		}

		return convertView;
	}

}

package com.example.padim.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.padim.R;
import com.example.padim.dbutils.Person;

/**
 * 
 * 项目名称：PadIM 类名称：CheckedImgAdapter 类描述：横向ListView中的项目适配器 创建人：ystgx
 * 创建时间：2014年3月20日 下午9:33:19
 * 
 * @version
 */
public class CheckedImgAdapter extends BaseAdapter {
	private Context ct;
	private ArrayList<Person> data;

	public ArrayList<Person> getData() {
		return data;
	}

	public CheckedImgAdapter(Context ct, ArrayList<Person> data) {
		this.ct = ct;
		this.data = data;
	}

	public void addImg(Person item) {

		data.add(item);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Person item = data.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(ct).inflate(
					R.layout.checked_contact_item, null);

			viewHolder = new ViewHolder();
			viewHolder.pic = (ImageView) convertView
					.findViewById(R.id.checked_pic);
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.checked_name);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (item.onLine) {
			viewHolder.pic.setImageResource(R.drawable.pic_online_male);
		} else {
			viewHolder.pic.setImageResource(R.drawable.pic_offline_male);
		}

		viewHolder.name.setText(item.getUserName());
		return convertView;
	}

	static class ViewHolder {
		public ImageView pic;
		public TextView name;
	}
}

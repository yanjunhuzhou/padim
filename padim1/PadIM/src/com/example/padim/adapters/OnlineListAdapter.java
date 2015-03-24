package com.example.padim.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.padim.PadIMApplication;
import com.example.padim.R;
import com.example.padim.beans.DeviceItemHolder;
import com.example.padim.dbutils.Person;
import com.example.padim.util.PadImUtil;

/**
 * 
 * 项目名称：PadIM 类名称：OnlineListAdapter 类描述：所有设备列表适配器 创建人：ystgx 创建时间：2014年3月10日
 * 下午1:05:48
 * 
 * @version
 */
public class OnlineListAdapter extends BaseAdapter {
	private Context mContext;
	private List<Person> mList = new ArrayList<Person>();

	public OnlineListAdapter(Context context) {
		this.mContext = context;

	}

	public List<Person> getmList() {
		return mList;
	}

	public void setmList(List<Person> mList) {
		this.mList = mList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		Person item = mList.get(arg0);
//		System.out.println("当前位置为："+arg0);
		DeviceItemHolder showItem = null;
		if (arg1 == null) {
			showItem = new DeviceItemHolder();
			arg1 = LayoutInflater.from(mContext).inflate(
					R.layout.contacts_list_item, arg2, false);
			showItem.pic = (ImageView) arg1.findViewById(R.id.online_item_pic);
			showItem.name = (TextView) arg1.findViewById(R.id.online_item_name);
			showItem.unread = (TextView) arg1.findViewById(R.id.unread);
			showItem.ip = (TextView) arg1.findViewById(R.id.online_item_ip);
			arg1.setTag(showItem);
		} else {
			showItem = (DeviceItemHolder) arg1.getTag();
		}
		int unread = PadIMApplication.getInstance().getMessagesCountById(
				item.getIntUserId());
		if (unread > 0) {
			showItem.unread.setText("(未读" + unread + "条)");
			showItem.unread.setTextSize(20);
		} else {
			showItem.unread.setText("");
		}

		if (item.onLine) {
			showItem.pic.setImageResource(R.drawable.pic_online_male);
		} else {
			showItem.pic.setImageResource(R.drawable.pic_offline_male);
		}
		
		showItem.name.setText(item.getUserName());
		showItem.name.setTextSize(20);
		showItem.ip.setText("("+item.ipAddress+")");
		showItem.ip.setTextSize(20);
		return arg1;
	}

}

package com.example.padim.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.padim.PadIMApplication;
import com.example.padim.R;
import com.example.padim.beans.HistoryItemHolder;
import com.example.padim.dbutils.MessageData;
import com.example.padim.dbutils.Person;
import com.example.padim.dbutils.PersonDao;
import com.example.padim.util.DateUtil;
import com.example.padim.util.PadImUtil;

/**
 * 
 * 项目名称：PadIM 类名称：HistoricalListAdapter 类描述：历史聊天对象的适配器 创建人：ystgx 创建时间：2014年3月10日
 * 下午5:34:53
 * 
 * @version
 */
public class HistoricalListAdapter extends BaseAdapter {
	private Context mContext;
	private List<MessageData> mList = new ArrayList<MessageData>();

	public HistoricalListAdapter(Context context, List<MessageData> list) {
		this.mContext = context;
		if (list != null) {
			this.mList = list;
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	public void setmList(List<MessageData> mList) {
		this.mList = mList;
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
		MessageData item = mList.get(arg0);
		HistoryItemHolder showItem = null;
		if (arg1 == null) {
			showItem = new HistoryItemHolder();
			arg1 = LayoutInflater.from(mContext).inflate(
					R.layout.historical_list_item, arg2, false);
			showItem.pic = (ImageView) arg1
					.findViewById(R.id.historical_item_pic);
			showItem.name = (TextView) arg1
					.findViewById(R.id.historical_item_name);
			showItem.lastTime = (TextView) arg1
					.findViewById(R.id.historical_item_time);
			showItem.lastMsg = (TextView) arg1
					.findViewById(R.id.historical_item_lastmsg);
			arg1.setTag(showItem);
		} else {
			showItem = (HistoryItemHolder) arg1.getTag();
		}
		Log.i("msg", "item.getMsgPersons()="+item.getMsgPersons()+",content"+item.getContent()+",getfrom:"+item.getFrom());
		String others = PadImUtil.getOtherPersons(
				PadIMApplication.getInstance().me.getUserId(),
				item.getMsgPersons());
		boolean isOnLine = false;
		if (others != null) {
			String[] userIds = others.split(",");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < userIds.length; i++) {
				Person p = PersonDao.getInstance()
						.getPersonByUserId(userIds[i]);
				if (p != null) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					if (p.isOnLine()) {
						isOnLine = true;
					}
					sb.append(p.getUserName());
				}
			}
			if (item.isUnRead()) {
				showItem.name.setTextColor(Color.RED);
			} else {
				showItem.name.setTextColor(Color.BLACK);
			}
			showItem.name.setText(sb.toString());
			showItem.name.setTextSize(20);
		}
		if (isOnLine) {
			showItem.pic.setImageResource(R.drawable.pic_online_male);
		} else {
			showItem.pic.setImageResource(R.drawable.pic_offline_male);
		}
		showItem.lastTime.setText(DateUtil.getTimeStr2(item.getTime()));
		showItem.lastTime.setTextSize(20);
		showItem.lastMsg.setText(item.getContent());
		showItem.lastMsg.setTextSize(20);
		return arg1;
	}

}

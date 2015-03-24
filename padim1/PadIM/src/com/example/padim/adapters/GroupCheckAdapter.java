package com.example.padim.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.padim.R;
import com.example.padim.beans.DeviceItem;
import com.example.padim.beans.GroupItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * 
 * 项目名称：PadIM 类名称：GroupCheckAdapter 类描述：选择群聊列表界面的适配器 创建人：ystgx 创建时间：2014年3月20日
 * 下午9:34:08
 * 
 * @version
 */
public class GroupCheckAdapter extends BaseAdapter implements SectionIndexer {
	private List<GroupItem> list = null;
	private Context mContext;
	public Map<String, Boolean> checkedMap = null;

	public GroupCheckAdapter(Context mContext, List<GroupItem> list) {
		this.mContext = mContext;
		this.list = list;

		// 将传入的设备列表中的每个设备对应的ip与checkbox选中与否设置标记
		this.checkedMap = new HashMap<String, Boolean>();
		for (GroupItem item : list) {
			checkedMap.put(item.getUserId(), false);
		}
	}

	/**
	 * 获取checkbox组的选中状态
	 * 
	 */
	public Map<String, Boolean> getCheckedMap() {
		return checkedMap;
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param list
	 */
	public void updateListView(List<GroupItem> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		GroupItem mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(
					R.layout.group_check_item, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.item_name);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.checkBox = (CheckBox) view
					.findViewById(R.id.contactitem_select_cb);
			viewHolder.itemPic = (ImageView) view.findViewById(R.id.item_pic);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);

		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (position == getPositionForSection(section)) {
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		} else {
			viewHolder.tvLetter.setVisibility(View.GONE);
		}

		viewHolder.tvTitle.setText(mContent.getUserName());
		viewHolder.checkBox.setChecked(checkedMap.get(mContent.getUserId()));

		if (mContent.onLine) {
			viewHolder.itemPic.setImageResource(R.drawable.pic_online_male);
		} else {
			viewHolder.itemPic.setImageResource(R.drawable.pic_offline_male);
		}

		return view;

	}

	final static class ViewHolder {
		CheckBox checkBox;
		TextView tvLetter;
		TextView tvTitle;
		ImageView itemPic;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}

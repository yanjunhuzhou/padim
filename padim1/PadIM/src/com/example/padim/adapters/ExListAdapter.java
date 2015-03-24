/**
 * 
 */
package com.example.padim.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.padim.ChatMsgActivity;
import com.example.padim.ContactsFragment;
import com.example.padim.MyFileManager;
import com.example.padim.PadIMApplication;
import com.example.padim.R;
import com.example.padim.dbutils.Person;
import com.example.padim.util.Constant;
import com.example.padim.util.DateUtil;
import com.example.padim.util.PadImUtil;

/**
 * 在线用户列表
 * 
 * @author zhuft
 * @version 1.0 2011-11-6
 */
public class ExListAdapter extends BaseExpandableListAdapter implements
		OnLongClickListener {

	private int[] UnReadColors;// 未读颜色
	private int groupItemHeight = 60;
	private String[] groupIndicatorLabeles = null;

	private AlertDialog dialog = null;

	public Person person = null;
	ContactsFragment fragment;

	public ExListAdapter(ContactsFragment fragment) {
		this.fragment = fragment;

		init();
	}

	private void init() {
		UnReadColors = new int[2];
		UnReadColors[0] = fragment.getActivity().getResources()
				.getColor(R.color.red);
		UnReadColors[1] = fragment.getActivity().getResources()
				.getColor(R.color.search_hint);
		groupItemHeight = (int) fragment.getActivity().getResources()
				.getDimension(R.dimen.group_item_height);
		groupIndicatorLabeles = fragment.getActivity().getResources()
				.getStringArray(R.array.groupIndicatorLabeles);
	}

	// 获得某个用户对象
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		PadIMApplication app = PadIMApplication.getInstance();
		return app.allUser.get(groupPosition).get(
				app.onlineUsersId.get(childPosition));
	}

	// 获得用户在用户列表中的序号
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return PadIMApplication.getInstance().onlineUsersId.get(childPosition);
	}

	// 生成用户布局View
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parentView) {
		View view = null;
		PadIMApplication app = PadIMApplication.getInstance();
		Log.e("", app.allUser.size() + "===" + app.onlineUsersId.size());
		if (groupPosition < app.allUser.size()
				&& childPosition < app.onlineUsersId.size()) {// 如果groupPosition的序号能从children列表中获得一个children对象
			Person person = app.allUser.get(groupPosition).get(
					app.onlineUsersId.get(childPosition));// 获得当前用户实例
			view = fragment.getActivity().getLayoutInflater()
					.inflate(R.layout.person_item_layout, null);// 生成List用户条目布局对象
			view.setOnLongClickListener(this);// 添加长按事件
			view.setOnClickListener(clickLis);
			view.setTag(person);// 添加一个tag标记以便在长按事件和点击事件中根据该标记进行相关处理
			view.setPadding(30, 0, 0, 0);// 设置左边填充空白距离
			ImageView headIconView = (ImageView) view
					.findViewById(R.id.person_head_icon);// 头像
			TextView nickeNameView = (TextView) view
					.findViewById(R.id.person_nickename);// 昵称
			TextView loginTimeView = (TextView) view
					.findViewById(R.id.person_login_time);// 登录时间
			TextView msgCountView = (TextView) view
					.findViewById(R.id.person_msg_count);// 未读信息计数
			// TextView ipaddressView =
			// (TextView)view.findViewById(R.id.person_ipaddress);//IP地址
			headIconView.setImageResource(person.getHeadIconId());
			nickeNameView.setText(person.userName);
			loginTimeView.setText(DateUtil.getTimeStr(person.loginTime));
			String msgCountStr = app.getString(R.string.init_msg_count);
			// 根据用户id从service层获得该用户的消息数量
			int unReadAmount = app.getMessagesCountById(person.getIntUserId());
			msgCountView.setText(String.format(msgCountStr, unReadAmount));
			if (unReadAmount > 0) {
				msgCountView.setTextColor(UnReadColors[0]);
			} else {
				msgCountView.setTextColor(UnReadColors[1]);
			}
			// ipaddressView.setText(person.ipAddress);
			view.setMinimumHeight(groupItemHeight * 3 / 2);

		}
		return view;
	}

	// 获得某个用户组中的用户数
	@Override
	public int getChildrenCount(int groupPosition) {
		int childrenCount = 0;
		PadIMApplication app = PadIMApplication.getInstance();
		if (groupPosition < app.allUser.size())
			childrenCount = app.allUser.get(groupPosition).size();
		return childrenCount;
	}

	// 获得媒个用户组对象
	@Override
	public Object getGroup(int groupPosition) {
		PadIMApplication app = PadIMApplication.getInstance();
		return app.allUser.get(groupPosition);
	}

	// 获得用户组数量,该处的用户组数量返回的是组名称的数量
	@Override
	public int getGroupCount() {
		return groupIndicatorLabeles.length;
	}

	// 获得用户组序号
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	// 生成用户组布局View
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		PadIMApplication app = PadIMApplication.getInstance();
		LinearLayout layout = (LinearLayout) LayoutInflater.from(app).inflate(
				R.layout.expandablelist_item_layout, null);

		ImageView image = (ImageView) layout.findViewById(R.id.expand_icon);
		TextView groupName = (TextView) layout.findViewById(R.id.group_name);
		TextView currentAmount = (TextView) layout
				.findViewById(R.id.current_person_amount);
		layout.setMinimumHeight(groupItemHeight);
		int childrenCount = 0;
		if (groupPosition < app.allUser.size()) {// 如果groupPosition序号能从children列表中获得children对象，则获得该children对象中的用户数量
			childrenCount = app.allUser.get(groupPosition).size();
		}
		groupName.setText(groupIndicatorLabeles[groupPosition]);
		currentAmount.setText("[" + childrenCount + "]");
		if (isExpanded) {
			image.setImageResource(R.drawable.narrow_select);
		} else {
			image.setImageResource(R.drawable.narrow);
		}

		return layout;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private OnClickListener clickLis = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			try {
				PadIMApplication app = PadIMApplication.getInstance();
				switch (view.getId()) {
				case R.id.person_item_layout:// 转到发信息页面
					person = (Person) view.getTag();// 用户列表的childView被点击时
					openChartPage(person);
					if (app.getMessagesById(person.getIntUserId()) != null) {
						app.getMessagesById(person.getIntUserId()).clear();
					}
					break;
				case R.id.long_send_msg:// 长按列表的childView时在弹出的窗口中点击"发送信息"按钮时
					person = (Person) view.getTag();
					openChartPage(person);
					if (null != dialog)
						dialog.dismiss();
					break;
				case R.id.long_send_file:
					Intent intent = new Intent(fragment.getActivity(),
							MyFileManager.class);
					intent.putExtra("selectType", Constant.SELECT_FILES);
					fragment.getActivity().startActivityForResult(intent, 0);
					dialog.dismiss();
					break;
				case R.id.long_click_call_tt:
					// 如果是听筒的话则设置标识
					PadIMApplication.getInstance().setVoiceTingtong();
					person = (Person) view.getTag();
					AlertDialog.Builder builder = new AlertDialog.Builder(
							fragment.getActivity());
					builder.setTitle(app.me.userName);
					String title = String.format(
							app.getString(R.string.talk_with), person.userName);
					builder.setMessage(title);
					builder.setIcon(app.me.getHeadIconId());
					builder.setNegativeButton(app.getString(R.string.close),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface cdialog,
										int which) {
									cdialog.dismiss();
								}
							});
					final AlertDialog callDialog = builder.show();
					callDialog
							.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface arg0) {
									fragment.getMainService().stopTalk(
											person.getIntUserId());
								}
							});
					fragment.getMainService().startTalk(person.getIntUserId());
					dialog.dismiss();
					break;
				case R.id.long_click_call_ysq:
					// 设置扬声器模式
					app.setVoiceYangshengqi();
					person = (Person) view.getTag();
					AlertDialog.Builder builder2 = new AlertDialog.Builder(
							fragment.getActivity());
					builder2.setTitle(app.me.userName);
					String title2 = String.format(
							app.getString(R.string.talk_with), person.userName);
					builder2.setMessage(title2);
					builder2.setIcon(app.me.getHeadIconId());
					builder2.setNegativeButton("关闭",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface cdialog,
										int which) {
									cdialog.dismiss();
								}
							});
					final AlertDialog callDialog2 = builder2.show();
					callDialog2
							.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface arg0) {
									fragment.getMainService().stopTalk(
											person.getIntUserId());
								}
							});
					fragment.getMainService().startTalk(person.getIntUserId());
					dialog.dismiss();
					break;
				case R.id.long_click_cancel:
					dialog.dismiss();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/* 当用户列表被长时间按下时会触发该事件 */
	@Override
	public boolean onLongClick(View view) {
		person = (Person) view.getTag();
		AlertDialog.Builder builder = new AlertDialog.Builder(
				fragment.getActivity());
		builder.setTitle(person.userName);
		// builder.setMessage(R.string.pls_select_opr);
		// builder.setIcon(android.R.drawable.ic_menu_more);
		View vi = fragment.getActivity().getLayoutInflater()
				.inflate(R.layout.person_long_click_layout, null);
		builder.setView(vi);
		dialog = builder.show();

		Button sendMsgBtn = (Button) vi.findViewById(R.id.long_send_msg);
		sendMsgBtn.setTag(person);
		sendMsgBtn.setOnClickListener(clickLis);

		Button sendFileBtn = (Button) vi.findViewById(R.id.long_send_file);
		sendFileBtn.setTag(person);
		sendFileBtn.setOnClickListener(clickLis);

		// 听筒模式呼叫
		Button callTingtongBtn = (Button) vi
				.findViewById(R.id.long_click_call_tt);
		callTingtongBtn.setTag(person);
		callTingtongBtn.setOnClickListener(clickLis);

		// 扬声器模式
		Button callYangshengqiBtn = (Button) vi
				.findViewById(R.id.long_click_call_ysq);
		callYangshengqiBtn.setTag(person);
		callYangshengqiBtn.setOnClickListener(clickLis);

		Button cancelBtn = (Button) vi.findViewById(R.id.long_click_cancel);
		cancelBtn.setTag(person);
		cancelBtn.setOnClickListener(clickLis);

		return true;
	}

	/**
	 * 打开发短信页面
	 * 
	 * @param person
	 */
	private void openChartPage(Person person) {
		Intent intent = new Intent(fragment.getActivity(),
				ChatMsgActivity.class);
		List<String> userIds = new ArrayList<String>();
		userIds.add(person.getUserId());
		userIds.add(PadIMApplication.getInstance().me.getUserId());
		intent.putExtra("msgPersons", PadImUtil.getMsgPersons(userIds));
		fragment.getActivity().startActivity(intent);
	}
}

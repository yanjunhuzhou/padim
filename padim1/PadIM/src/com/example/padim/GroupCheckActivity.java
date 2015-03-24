package com.example.padim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.padim.adapters.CheckedImgAdapter;
import com.example.padim.adapters.GroupCheckAdapter;
import com.example.padim.beans.GroupItem;
import com.example.padim.dbutils.Person;
import com.example.padim.dbutils.PersonDao;
import com.example.padim.ui.ActionBar;
import com.example.padim.ui.ClearEditText;
import com.example.padim.ui.HorizontalListView;
import com.example.padim.ui.SideBar;
import com.example.padim.ui.SideBar.OnTouchingLetterChangedListener;
import com.example.padim.util.PadImUtil;
import com.example.utils.CharacterParser;
import com.example.utils.PinyinComparator;

public class GroupCheckActivity extends Activity {

	public static boolean inGroup = false;
	
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private GroupCheckAdapter adapter;
	private ClearEditText mClearEditText;

	/************** checkbox相关成员 **********************/

	private HorizontalListView checkedContack;
	private CheckedImgAdapter checkedAdapter;
	private Button makesureBtn;
	// private ArrayList<CheckedImg> checkedList;
	private ArrayList<Person> checkedItem;
	private boolean hasMeasured = false;
	private int maxCount = 5;// 这里要是可以根据屏幕计算出来最多容纳多少item就好了.没有找到合适的方法.
	private int countChecked = 0;// 得到选中的数量.

	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private ArrayList<GroupItem> SourceDateList;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_check_activity);
		initViews();
		
	}

	protected void onResume() {
		super.onResume();
		inGroup = true;
	};
	
	protected void onPause() {
		super.onPause();
		inGroup = false;
	};
	
	ActionBar actionBar;

	public ActionBar getMyActionBar() {
		if (actionBar == null) {
			actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// finish();
				}
			});
		}
		return actionBar;
	}

	private void initViews() {
		getMyActionBar().addLeft("返回", new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		getMyActionBar().setTitle("群组");
		SourceDateList = new ArrayList<GroupItem>();// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		List<Person> persons = PersonDao.getInstance().getAllPersons(
				PadIMApplication.getInstance().me.getUserId());
		for (int i = 0; i < persons.size(); i++) {
			Person p = persons.get(i);
			GroupItem item = new GroupItem();
			item.ipAddress = p.ipAddress;
			item.loginTime = p.loginTime;
			item.onLine = p.onLine;
			item.userId = p.userId;
			item.userName = p.userName;
			// 汉字转换成拼音
			
			if( (p.userName.equals("")) || (null==p.getUserName())){
				item.setSortLetters("#");
			}else{
				String pinyin = characterParser.getSelling(p.getUserName());
				String sortString = pinyin.substring(0, 1).toUpperCase();
	
				// 正则表达式，判断首字母是否是英文字母
				if (sortString.matches("[A-Z]")) {
					item.setSortLetters(sortString.toUpperCase());
				} else {
					item.setSortLetters("#");
				}
			}
			SourceDateList.add(item);
		}

		pinyinComparator = new PinyinComparator();
		sideBar = (SideBar) findViewById(R.id.side_bar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}

			}
		});

		sortListView = (ListView) findViewById(R.id.country_lvcountry);

		/************* 修改部分 **********/
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
				// Toast.makeText(getApplication(),
				// ((GroupItem)adapter.getItem(position)).getmName(),
				// Toast.LENGTH_SHORT).show();
				Map<String, Boolean> checkedMap = adapter.getCheckedMap();
				GroupItem item = SourceDateList.get(position);

				CheckBox ckb = (CheckBox) view
						.findViewById(R.id.contactitem_select_cb);
				if (ckb.isChecked()) {
					checkedMap.put(item.getUserId(), false);
					removeByIp(item.getUserId());
					checkedAdapter.notifyDataSetChanged();
				} else {
					checkedContack.setSelection(adapter.getCount() - 1);
					checkedMap.put(item.getUserId(), true);
					addToCheckedList(item);
					checkedAdapter.notifyDataSetChanged();
					checkedContack.setSelection(checkedAdapter.getCount() - 1);
				}
				adapter.notifyDataSetChanged();
			}
		});

		// 根据a-z进行排序源数据
		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new GroupCheckAdapter(this, SourceDateList);
		sortListView.setAdapter(adapter);

		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);

		// 根据输入框输入值的改变来过滤搜索
		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString().toLowerCase());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		/******************* checkebox相关初始化 *******************/

		checkedContack = (HorizontalListView) findViewById(R.id.imgList);
		checkedItem = new ArrayList<Person>();

		// initCheckedList();

		checkedAdapter = new CheckedImgAdapter(this, checkedItem);
		makesureBtn = (Button) findViewById(R.id.makesure_btn);
		checkedContack.setAdapter(checkedAdapter);
		checkedContack.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Person item = checkedItem.get(position);
				Map<String, Boolean> checkedMap = adapter.getCheckedMap();

				checkedMap.put(item.getUserId(), false);
				adapter.notifyDataSetChanged();
				removeByIp(item.getUserId());
				checkedAdapter.notifyDataSetChanged();
			}

		});

		makesureBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (countChecked > 0) {
					List<String> userIds = new ArrayList<String>();
					userIds.add(PadIMApplication.getInstance().me.getUserId());//
					for (Person item : checkedItem) {
						userIds.add(item.getUserId());
					}
//					System.out.println("我的id:"+PadIMApplication.getInstance().me.getUserId());
					Intent intent = new Intent(GroupCheckActivity.this,
							ChatMsgActivity.class);
					intent.putExtra("msgPersons",
							PadImUtil.getMsgPersons(userIds));
					startActivity(intent);
					GroupCheckActivity.this.finish();
				}else {
					Toast.makeText(getApplication(), "请先选择群组成员！", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<GroupItem> filterDateList = new ArrayList<GroupItem>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceDateList;
		} else {
			filterDateList.clear();
			for (GroupItem sortModel : SourceDateList) {
				String name = sortModel.getUserName();
				if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(
								filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}

		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

	/*************** checkbox相关功能 ********************/

	/**
	 * 根据name查询对应的索引.
	 * 
	 * @param name
	 * @return
	 */
	private int findCheckedPositionByIp(String ip) {
		int i = -1;
		for (Person item : checkedItem) {
			i++;
			if (ip.equals(item.getUserId())) {
				return i;
			}
		}
		return -1;
	}

	// private void initCheckedList() {
	// DeviceItem defaultItem = new DeviceItem();
	// defaultItem.setmIp("default");
	// defaultItem.setmName("");
	// checkedItem.add(defaultItem);
	// }
	//

	/**
	 * 移除水平图片.
	 * 
	 * @param _name
	 */
	private void removeByIp(String ip) {
		checkedItem.remove(findCheckedPositionByIp(ip));
		// if (checkedItem.size() == 4) {
		// if (findCheckedPositionByIp("default") == -1) {
		// DeviceItem item = new DeviceItem();
		// item.setmIp("default");
		// item.setmName("");
		// checkedItem.add(item);
		// }
		// }
		makesureBtn.setText("确定(" + --countChecked + ")");
	}

	/**
	 * 选择checkbox，添加水平图片.
	 * 
	 * @param _name
	 * @param _id
	 * @param _touxiang
	 */
	private void addToCheckedList(Person item) {

		if (checkedItem.size() < maxCount) {
			checkedItem.add(item);
		}

		else {
			checkedItem.add(item);
		}

		makesureBtn.setText("确定(" + ++countChecked + ")");
	}

}

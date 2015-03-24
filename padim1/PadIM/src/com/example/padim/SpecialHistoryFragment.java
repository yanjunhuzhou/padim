package com.example.padim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.padim.adapters.HistoricalListAdapter;
import com.example.padim.dbutils.MessageDao;
import com.example.padim.dbutils.MessageData;

public class SpecialHistoryFragment extends BaseFragment {

	protected View mMainView;
	protected static ArrayList<Map<String, Object>> mlistItems;
	private HistoricalListAdapter adapter;
	private ListView listView;
	protected Context mContext;
	public static int ImageClicked = 0; 
	public static int LongClicked = 0;
    public  static ImageView ss = null;
    PadIMApplication application;
    
	public SpecialHistoryFragment() {
		super();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
	}

	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		update();
		Log.i("msg","resume");
	}

	
	public void update() {
		adapter.setmList(getHistoricalItem());
		adapter.notifyDataSetChanged();
		Log.i("msg", "count="+adapter.getCount() );
	}

	@Override
	public void onShow() {
		super.onShow();
		update();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mMainView = inflater.inflate(R.layout.historical_fragment, container,
				false);
		listView = (ListView) mMainView.findViewById(R.id.list);
		adapter = new HistoricalListAdapter(mContext, getHistoricalItem());
		listView.setAdapter(adapter);
		listView.setCacheColorHint(0);
		ss = (ImageView) mMainView.findViewById(R.id.deleteTalk);
		
		OnItemLongClickListener longListener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final int[] location = new int[2];
				Log.i("msg","location1 = "+location[0]);
				int x=0;
				int y=0;
			//	Log.i("msg","location1 = "+location[0]);
				view.getLocationInWindow(location);
			//	Log.i("msg","location2 = "+location[0]);
				LongClicked =1;
//				Log.i("msg","x="+x+" y="+y);
//				Log.i("msg","0="+location[0]+" 1="+location[1]);
//				Log.i("msg","ss="+ss.getHeight());
				
				x = (location[0]+view.getWidth())/2;
				if(ss.getHeight() == 0 || ss.getHeight() == 1){//涓篏one鏃舵病鏈夐珮搴︼紝invisible鏃朵负10
					 y=location[1]-130;
					Log.i("msg","location = " +location[1]);
					Log.i("msg", "view.getHeight1 =" + ss.getHeight());
				} else{
				     y = location[1]-ss.getHeight()-40;
				     Log.i("msg","locationnoe = " +location[1]);
				     Log.i("msg", "view.getHeight2 =" + ss.getHeight());
				     
				}
			//	Log.i("msg","Image x="+x+" y="+y);
				android.widget.RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(60,60);//set by 1
				lp.leftMargin = x;
				lp.topMargin = y;
				ss.setLayoutParams(lp);
				ss.setVisibility(View.VISIBLE);
				
				final MessageData data = getHistoricalItem().get(position);
				ss.setOnTouchListener(new OnTouchListener(){

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							//Log.i("msg","ss 鎸変笅");
							ImageClicked = 1;
							break;
						case MotionEvent.ACTION_UP:
							ss.setVisibility(View.GONE);
							//鍒犻櫎鏁版嵁搴?			
							MessageDao.getInstance().delete(data);
							//鍒犻櫎鐣岄潰
							update();
							ImageClicked = 0;
						//	Log.i("msg","ss 寮硅捣");
							break;
						}
						return true;
					}
					
				});
							
				return true;
			}
			
		};
		
        
		
		OnItemClickListener itemListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(getActivity(), ChatMsgActivity.class);
				MessageData data = getHistoricalItem().get(arg2);
				data.setUnRead(false);
				MessageDao.getInstance().createOrUpdate(data);
				intent.putExtra("msgPersons", data.getMsgPersons());
				// Person person = new Person();
				// person.personId = Integer.valueOf(list.get(arg2).getmIp());
				// person.personNickeName = list.get(arg2).getmName();
				// intent.putExtra("person", person);
				// intent.putExtra("me", PadIMApplication.getInstance().me);
				getActivity().startActivity(intent);
				Log.i("msg","鐐瑰嚮浜嬩欢鍝嶅簲");
			}
		};
		listView.setOnItemClickListener(itemListener);
		listView.setOnItemLongClickListener(longListener);
		return mMainView;
	}

	private List<MessageData> getHistoricalItem() {
		String Persons = null;
		
		if(application == null) {
			Log.i("msg", "aplication si null");
			application = PadIMApplication.getInstance();
		}
		Persons = "-1000,"+application.me.getUserId();
		Log.i("msg","msPersons"+ Persons);
		List<MessageData> list = MessageDao.getInstance().getSpecialHisMessagesGroupByHF

(Persons);//修改调用函数
		
		if (list == null) {
			list = new ArrayList<MessageData>();
		}
		return list;
	
}}

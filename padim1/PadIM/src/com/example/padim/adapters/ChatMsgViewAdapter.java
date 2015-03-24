
package com.example.padim.adapters;

import android.R.integer;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.padim.R;
import com.example.padim.beans.ChatMsgEntity;
import com.example.utils.TimeTransformer;

/**   
*    
* 项目名称：PadIM   
* 类名称：ChatMsgViewAdapter   
* 类描述：对聊天消息条目的适配器  
* 创建人：ystgx   
* 创建时间：2014年3月23日 下午9:32:39   
* @version        
*/
public class ChatMsgViewAdapter extends BaseAdapter{
	
	public static interface IMsgViewType
	{
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}
	
    private static final String TAG = ChatMsgViewAdapter.class.getSimpleName();

    private List<ChatMsgEntity> coll;

    private Context ctx;
    
    private LayoutInflater mInflater;

    public ChatMsgViewAdapter(Context context, List<ChatMsgEntity> coll) {
        ctx = context;
        this.coll = coll;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return coll.size();
    }

    public Object getItem(int position) {
        return coll.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    


	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
	 	ChatMsgEntity entity = coll.get(position);
	 	
	 	if (entity.getIsComMsg())
	 	{
	 		return IMsgViewType.IMVT_COM_MSG;
	 	}else{
	 		return IMsgViewType.IMVT_TO_MSG;
	 	}
	 	
	}


	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	ChatMsgEntity entity = coll.get(position);
    	boolean isComMsg = entity.getIsComMsg();
    		
    	ViewHolder viewHolder = null;	
	    if (convertView == null)
	    {
	    	  if (isComMsg)
			  {
				  convertView = mInflater.inflate(R.layout.chatting_msg_item_left, null);
			  }else{
				  convertView = mInflater.inflate(R.layout.chatting_msg_item_right, null);
			  }

	    	  viewHolder = new ViewHolder();
			  viewHolder.llSendDate = (LinearLayout) convertView.findViewById(R.id.ll_send_date);
			  viewHolder.tvSendDate = (TextView) convertView.findViewById(R.id.tv_send_date);
	    	  viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
			  viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tv_username);
			  viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			  
			  //viewHolder.isComMsg = isComMsg;
			  
			  convertView.setTag(viewHolder);
	    }else{
	        viewHolder = (ViewHolder) convertView.getTag();
	    }
	
	    	//根据position获取
	  	long section = getSectionForPosition(position);
	  	if(position == getPositionForSection(section)){
	  		viewHolder.llSendDate.setVisibility(View.VISIBLE);
	  		viewHolder.tvSendDate.setText(TimeTransformer.longToDate(entity.getTime()));		
	  	}
	  	else{
	  		viewHolder.llSendDate.setVisibility(View.GONE);
	  	}
	    
	    viewHolder.tvSendTime.setText(TimeTransformer.longToHour(entity.getTime()));
	    viewHolder.tvUserName.setText(entity.getName());
	    viewHolder.tvContent.setText(entity.getText());
	    
	    return convertView;
    }
    

    static class ViewHolder { 
        public TextView tvSendTime;
        public TextView tvUserName;
        public TextView tvContent;
        public LinearLayout llSendDate;
        public TextView tvSendDate;
        //public boolean isComMsg = true;
    }

    /**
	 * 找到两条相邻记录不是同一天的位置
	 */
	
	public int getPositionForSection(long time) {
		// TODO Auto-generated method stub
		for (int i = 0; i < getCount(); i++) {
			long sortTime = coll.get(i).getTime();
			Date date1 = new Date(time);
			Date date2 = new Date(sortTime);
			if(date1.getYear() == date2.getYear()
					&& date1.getMonth() == date2.getMonth()
					&& date1.getDate() == date2.getDate())
				return i;
		}
		
		return -1;
	}


	public long getSectionForPosition(int position) {
		// TODO Auto-generated method stub
		return coll.get(position).getTime();
	}




}

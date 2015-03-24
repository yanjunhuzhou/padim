/**
 * 
 */
package com.example.padim.widget;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.example.padim.model.BiaoQing;
import com.example.padim.util.BiaoQingParseConfig;
 

/**
 *自定义的支持表情的EditText 
 *@author zhuft
 *@version 1.0
 *2011-8-25
 */
public class BiaoQingTextView extends TextView {
	
	/** 所有的表情列表 */
	private List<BiaoQing> bq;
	
	/**
	 * @param context
	 */
	public BiaoQingTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public BiaoQingTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		bq = BiaoQingParseConfig.getBiaoQings(context);
	}
	
	/**
	 * 设置包含表情文字的文本
	 * @param text
	 */
	public void setBiaoQingText(CharSequence text){
		setText(txtToImg(new SpannableString(text.toString()), 0));
	}
	
	/**
	 * 文字转换为表情,比如点击一个表情[微笑]则转为对应的微笑表情
	 * @param ss
	 * @param flag 从哪个位置开始解析，用了递归解析
	 * @return
	 */
	public SpannableString txtToImg(SpannableString ss, int flag){
		String content = ss.toString();
		int starts = flag;
		int end = flag;
		
		if(content.indexOf("[", starts) != -1 && content.indexOf("]", end) != -1){
			starts = content.indexOf("[", starts);
			end = content.indexOf("]", end);
			String phrase = content.substring(starts,end + 1);
			int id = 0;
			for (BiaoQing temp : bq) {
				if (temp.getPhrase().equals(phrase)) {
					id = temp.getImageSourceId();
					break;
				}
			}
			if(id != 0){
				Drawable drawable = getContext().getResources().getDrawable(id);  
				if (drawable != null) {
					drawable.setBounds(0, 0, Math.round((float)(drawable.getIntrinsicWidth()*0.6)), Math.round((float)(drawable.getIntrinsicHeight()*0.6))); 
			        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);  
			        ss.setSpan(span, starts,end + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
				}
				flag = end + 1;
				txtToImg(ss, flag);
			}
			
		}
		return ss;
	}

}

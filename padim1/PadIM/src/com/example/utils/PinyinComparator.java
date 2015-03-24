package com.example.utils;

import java.util.Comparator;

import com.example.padim.beans.GroupItem;


/**   
*    
* 项目名称：PadIM   
* 类名称：PinyinComparator   
* 类描述：对选择群聊列表中的ListView中的项目按照拼音排序   
* 创建人：ystgx   
* 创建时间：2014年4月23日 下午9:42:14   
* @version        
*/
public class PinyinComparator implements Comparator<GroupItem> {

	public int compare(GroupItem o1, GroupItem o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}


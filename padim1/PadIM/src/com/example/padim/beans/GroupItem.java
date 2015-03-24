package com.example.padim.beans;

import com.example.padim.dbutils.Person;

public class GroupItem extends Person{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sortLetters;  //显示数据拼音的首字母
	

	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

}

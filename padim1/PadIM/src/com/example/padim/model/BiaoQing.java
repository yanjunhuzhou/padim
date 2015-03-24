/**
 * 
 */
package com.example.padim.model;

import java.io.Serializable;

/**
 *表情对象 
 *@author zhuft
 *@version 1.0
 *2011-8-22
 */
public class BiaoQing implements Serializable {
	/**  */
	private static final long serialVersionUID = 1L;
	
	/** 表情使用的替代文字 ，短语 */
	private String phrase;
    /** 表情图片资源ID */
    private int imageSourceId;
    /** 表情名称 */
    private String imageName;
    
	public String getPhrase() {
		return phrase;
	}
	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}
	public int getImageSourceId() {
		return imageSourceId;
	}
	public void setImageSourceId(int imageSourceId) {
		this.imageSourceId = imageSourceId;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

}

package com.example.padim.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.example.padim.R;
import com.example.padim.model.BiaoQing;
 


/**
 *解析表情配置文件的类 
 *@author zhuft
 *@version 1.0
 *2011-8-23
 */
public class BiaoQingHandler extends DefaultHandler {
	
	private List<BiaoQing> biaoQingList;
	private String tag = null;//正在解析的元素
	
	private BiaoQing biaoQing;
	
	public List<BiaoQing> getBiaoQings(){
		return biaoQingList;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (tag != null) {
			String textArea = new String(ch,start,length);
			
			/**开始解析表情数据*/
			if ("phrase".equals(tag)) {
				biaoQing.setPhrase(textArea);
			} else if ("image_name".equals(tag)) {
				biaoQing.setImageName(textArea);
				//通过反射取得表情的资源ID
				try {
					Field f = (Field)R.drawable.class.getDeclaredField(textArea);
					biaoQing.setImageSourceId(f.getInt(R.drawable.class));
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} 
		}
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		tag = null;
		if ("biaoqings".equals(localName)) {
			
		} else if ("biaoqing".equals(localName)) {
			biaoQingList.add(biaoQing);  
            
			biaoQing = null;  
		} 
	}

	@Override
	public void startDocument() throws SAXException {
		biaoQingList= new ArrayList<BiaoQing>();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if ("biaoqing".equals(localName)) {  
            biaoQing = new BiaoQing();  
        }   
		tag = localName;
	}
}

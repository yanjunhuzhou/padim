/**
 * 
 */
package com.example.padim.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.res.Resources;

import com.example.padim.R;
import com.example.padim.model.BiaoQing;
 

/**
 *取所有表情 
 *@author zhuft
 *@version 1.0
 *2011-8-22
 */
public class BiaoQingParseConfig {
	
	private static List<BiaoQing> biaoQingList;
	
	public static List<BiaoQing> getBiaoQings(Context context){
		if(biaoQingList == null){
			biaoQingList = new ArrayList<BiaoQing>();
			try {  
	            //生成SAX解析对象  
	            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();  
	            //生成xml读取器  
	            XMLReader reader = parser.getXMLReader();  
	            BiaoQingHandler handler = new BiaoQingHandler();  
	            //设置Handler  
	            reader.setContentHandler(handler);  
	            //指定文件，进行解析  
	            Resources res = context.getResources();
	            reader.parse(new InputSource(res.openRawResource(R.raw.biaoqing_config)));  
	            //获取 List<Emotions>  
	            biaoQingList = handler.getBiaoQings(); 
	        } catch (ParserConfigurationException e) {  
	            e.printStackTrace();  
	        } catch (SAXException e) {  
	            e.printStackTrace();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
		}
        return biaoQingList;  
	}
}

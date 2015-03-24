package com.example.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.example.padim.R;

import android.content.Context;
import android.os.Environment;


/**   
*    
* 项目名称：PadIM   
* 类名称：SDFileHelper   
* 类描述：读取SD卡中的配置文件  
* 创建人：ystgx   
* 创建时间：2014年3月26日 下午5:30:01   
* @version        
*/
public class SDFileHelper {
	private Context context;
	/**SD卡是否存在**/
	private boolean hasSDCard;
	private static final String DIR_NAME = "PadIM";
	private static final String FILE_NAME = "config.txt";
	private static String workPath ;
	private static String configPath;
	/**SD卡目录**/
	private String SD_PATH;
	
	public SDFileHelper(Context context){
		this.context=context;
		hasSDCard = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (hasSDCard){
			SD_PATH = android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			workPath = SD_PATH + File.separator 
					+ DIR_NAME ;
			configPath = workPath + File.separator + FILE_NAME;
		}
		
	} 
	
	/**检查SD卡挂载状态，并在指定工作目录中检查是否存在config配置文件，如果没有，系统则创建默认配置**/
	public void initFile () throws Exception{
		if (hasSDCard){
			
			File dir = new File (workPath);
			if(!dir.exists())
				dir.mkdir();
			if(!(new File(configPath)).exists()){
				InputStream is = context.getResources()
						.openRawResource(R.raw.config);
				FileOutputStream fos = new FileOutputStream(configPath);
				byte buffer[] = new byte[1024] ;
				int count = 0;
				while((count = is.read(buffer))>0){
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
				
			}
		}
	}
	

	
}

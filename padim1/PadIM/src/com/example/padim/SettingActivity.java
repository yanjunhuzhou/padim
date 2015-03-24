package com.example.padim;


import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputFilter;

import com.example.padim.dbutils.Person;
import com.example.padim.dbutils.PersonDao;
import com.example.padim.util.Constant;

/**
 * 
 * 项目名称：PadIM 类名称：SettingActivity 类描述：设置界面 创建人：ystgx 创建时间：2014年3月23日 下午9:30:37
 * 
 * @version
 */
public class SettingActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        final EditTextPreference mEditText = (EditTextPreference) findPreference("username");
        mEditText.getEditText().setFilters(new InputFilter[] {
        	new InputFilter.LengthFilter(20)	
        });
        String userName = PadIMApplication.getInstance().me.getUserName();
        mEditText.setDefaultValue(userName);
//        final EditTextPreference pathEt = (EditTextPreference) findPreference("downloadPath");
//        String filePath = PreferenceManager.getDefaultSharedPreferences(this).getString("downloadPath",
//                Constant.downloadPath);
//        pathEt.setDefaultValue(filePath);

    }

    
    @Override
    protected void onPause() {
        super.onPause();
        Person me = PadIMApplication.getInstance().me;
        String userName = PreferenceManager.getDefaultSharedPreferences(this).getString("username",
                me.getUserName());
//        String filePath = PreferenceManager.getDefaultSharedPreferences(this).getString("downloadPath",
//                Constant.downloadPath); 
        me.setUserName(userName);
        PersonDao.getInstance().createOrUpdate(me);
        Intent intent = new Intent();
        intent.setAction(Constant.updateMyInformationAction);
        this.sendBroadcast(intent);
        /*
        File f = new File(filePath);
        if(!f.exists()){
        	Editor et = PreferenceManager.getDefaultSharedPreferences(this).edit();
            et.putString("downloadPath", Constant.downloadPath).commit();
            Toast.makeText(this, filePath+"不存在,"+"路径设置错误!"+"当前下载路径为默认路径"+Constant.downloadPath, Toast.LENGTH_LONG).show();
        }else {
        	if(f.canWrite())
        		Toast.makeText(this, "当前下载路径为"+filePath, Toast.LENGTH_LONG).show();
        	else{
        		Toast.makeText(this, filePath+"没有写权限,"+"路径设置错误!"+"当前下载路径为默认路径"+Constant.downloadPath, Toast.LENGTH_LONG).show();
        		Editor et = PreferenceManager.getDefaultSharedPreferences(this).edit();
                et.putString("downloadPath", Constant.downloadPath).commit();
        	}
        		
        }
       */ 	
    }

}

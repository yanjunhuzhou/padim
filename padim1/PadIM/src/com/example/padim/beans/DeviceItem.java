package com.example.padim.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**   
*    
* 项目名称：PadIM   
* 类名称：DeviceItem   
* 类描述： 设备列表对象   
* 创建人：ystgx   
* 创建时间：2014年4月23日 下午9:37:56   
* @version        
*/
public class DeviceItem implements Parcelable {
	/**用第一位标识用户性别，用第二位标示用户在线状态**/
	public static final int MALE = 0x00;
	public static final int FEMALE = 0x01;
	public static final int OFFLINE = 0x00;
	public static final int ONLINE = 0x02;
	
	private String mIp;
	private String mMac;
	private String mName;
	private int  isOnline;
	private int mSex;
	public DeviceItem() {
		this.mIp = "0.0.0.0";
		this.mMac = "null";
		this.mName = "null";
		this.isOnline = OFFLINE;
		this.mSex = MALE;
	}

	public DeviceItem(DeviceItem item) {
		this.mIp = item.mIp;
		this.mMac = item.mMac;
		this.mName = item.mName;
		this.isOnline = item.isOnline;
		this.mSex = item.mSex;
	}
	
	

	public boolean setAttrByArray(String attr[]){
		
		return true;
	}
	public String getmIp() {
		return mIp;
	}
	public void setmIp(String mIp) {
		this.mIp = mIp;
	}
	public String getmName() {
		return mName;
	}
	public void setmName(String mName) {
		this.mName = mName;
	}

	public int isOnline() {
		return isOnline;
	}
	public void setOnline(int isOnline) {
		this.isOnline = isOnline;
	}
	public String getmMac() {
		return mMac;
	}
	public void setmMac(String mMac) {
		this.mMac = mMac;
	}
	public int getSex() {
		return mSex;
	}
	public void setSex(int sex){
		this.mSex = sex;
	}
	public void setSex(String sex) {
		if(sex.equalsIgnoreCase("male"))
			this.mSex = MALE;
		else if (sex.equalsIgnoreCase("female"))
			this.mSex = FEMALE;
	}
	

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	//保持写入和读出的顺序一致
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeString(mIp);
		arg0.writeString(mMac);
		arg0.writeString(mName);
		arg0.writeInt(isOnline);
		arg0.writeInt(mSex);
	}
	
	private DeviceItem(Parcel source) {
		// TODO Auto-generated constructor stub
		mIp = source.readString();
		mMac = source.readString();
		mName = source.readString();
		isOnline = source.readInt();
		mSex = source.readInt();
	}
	
	
	public static final Parcelable.Creator<DeviceItem> CREATOR
			= new Parcelable.Creator<DeviceItem>() {

				@Override
				public DeviceItem createFromParcel(Parcel source) {
					// TODO Auto-generated method stub
					return new DeviceItem(source);
				}

				@Override
				public DeviceItem[] newArray(int size) {
					// TODO Auto-generated method stub
					return new DeviceItem[size];
				}
			};
		
}

package com.example.padim.test;

import android.util.Log;

import com.example.utils.IPTransformer;

public class iptransformtest {
	public iptransformtest(){
		Log.i("ip", IPTransformer.ipToLong("452452452")+"");
		Log.i("ip", IPTransformer.ipToLong("0.0.0.0")+"");
		Log.i("ip", IPTransformer.ipToLong("192.168.1.1")+"");
		Log.i("ip", IPTransformer.ipToLong("-19.168.1.1")+"");
		Log.i("ip", IPTransformer.ipToLong("sdsdff")+"");
		Log.i("ip", IPTransformer.ipToLong("a.b.c.d")+"");
		Log.i("ip", IPTransformer.ipToLong("625.192.8.1")+"");
		Log.i("ip", IPTransformer.ipToLong("625.192.1")+"");
		String temp;
		if((temp=IPTransformer.longToIp(452452452)) == null)
			Log.i("ip", "null");
		else
			Log.i("ip", temp);
		if((temp=IPTransformer.longToIp(-768)) == null)
			Log.i("ip", "null");
		else
			Log.i("ip", temp);
		if((temp=IPTransformer.longToIp(0)) == null)
			Log.i("ip", "null");
		else
			Log.i("ip", temp);
		
	}
}

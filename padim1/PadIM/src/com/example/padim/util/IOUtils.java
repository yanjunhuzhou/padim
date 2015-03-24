package com.example.padim.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.text.TextUtils;
import android.util.Log;

public final class IOUtils {
	private final String TAG = "IOUtils";
	private static IOUtils instance;
	private HttpURLConnection conn;

	public static final synchronized IOUtils instance() {
		if (instance == null) {
			instance = new IOUtils();
		}
		return instance;
	}

	public String connectJSONServer(Context context, String json,
			String uriString, String password) {
		long startTime = System.currentTimeMillis();
		String value = null;
		try {
			if (this.conn != null)
				this.conn.disconnect();
			if (isNetworkAvailable(context) == 12) {
				java.net.Proxy proxy = new java.net.Proxy(
						java.net.Proxy.Type.HTTP, new InetSocketAddress(
								android.net.Proxy.getDefaultHost(),
								android.net.Proxy.getDefaultPort()));
				this.conn = ((HttpURLConnection) new URL(uriString)
						.openConnection(proxy));
			} else {
				this.conn = ((HttpURLConnection) new URL(uriString)
						.openConnection());
			}
			this.conn.setDoOutput(true);
			this.conn.setRequestMethod("POST");
			this.conn.setRequestProperty("content-type",
					"application/x-www-form-urlencoded");
			this.conn.addRequestProperty("appkey", password);
			this.conn.setConnectTimeout(10000);
			this.conn.setReadTimeout(10000);

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					this.conn.getOutputStream()));
			String decryptResult = CryptTools.encrypt(json,
					getString(password), false);
			writer.write(decryptResult);
			writer.close();

			int code = this.conn.getResponseCode();
			InputStream in = null;
			if (code == 200)
				in = this.conn.getInputStream();
			else {
				in = this.conn.getErrorStream();
			}

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder builder = new StringBuilder();
			value = reader.readLine();
			while (value != null) {
				builder.append(value);
				value = reader.readLine();
			}
			reader.close();
			String result = new String(builder.toString().getBytes("utf-8"));
			if (!TextUtils.isEmpty(result))
				value = CryptTools.decrypt(result, getString(password), true);
			else
				value = null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			value = null;
		} catch (IOException e) {
			e.printStackTrace();
			value = null;
		} catch (Exception e) {
			e.printStackTrace();
			value = null;
		}

		return value;
	}

	public String connectJSONServer2(Context context, String uriString,
			Map<String, String> map) {
		long startTime = System.currentTimeMillis();
		String value = null;
		try {
			if (this.conn != null)
				this.conn.disconnect();
			if (isNetworkAvailable(context) == 12) {
				java.net.Proxy proxy = new java.net.Proxy(
						java.net.Proxy.Type.HTTP, new InetSocketAddress(
								android.net.Proxy.getDefaultHost(),
								android.net.Proxy.getDefaultPort()));
				this.conn = ((HttpURLConnection) new URL(uriString)
						.openConnection(proxy));
			} else {
				this.conn = ((HttpURLConnection) new URL(uriString)
						.openConnection());
			}
			this.conn.setDoOutput(true);
			this.conn.setRequestMethod("POST");
			this.conn.setRequestProperty("content-type",
					"application/x-www-form-urlencoded");
			StringBuffer sb = new StringBuffer();
			if (map != null) {
				Iterator<Entry<String, String>> lt = map.entrySet().iterator();
				while (lt.hasNext()) {
					Entry<String, String> e = lt.next();
					if (sb.length() > 0) {
						sb.append("&");
					} else {
						// sb.append("?");
					}
					sb.append(e.getKey());
					sb.append("=");
					sb.append(e.getValue());
				}
			}

			this.conn.setConnectTimeout(10000);
			this.conn.setReadTimeout(10000);

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					this.conn.getOutputStream()));
			writer.write(sb.toString());
			writer.close();

			int code = this.conn.getResponseCode();
			InputStream in = null;
			if (code == 200)
				in = this.conn.getInputStream();
			else {
				in = this.conn.getErrorStream();
			}

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder builder = new StringBuilder();
			value = reader.readLine();
			while (value != null) {
				builder.append(value);
				value = reader.readLine();
			}
			reader.close();
			String result = new String(builder.toString().getBytes("utf-8"));
			if (!TextUtils.isEmpty(result))
				value = result;
			else
				value = null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			value = null;
		} catch (IOException e) {
			e.printStackTrace();
			value = null;
		} catch (Exception e) {
			e.printStackTrace();
			value = null;
		}

		return value;
	}

	public static String getString(String str) {
		StringBuffer sb = new StringBuffer();
		return sb.toString();
	}

	public static int isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService("connectivity");
		if (connectivity == null)
			return 0;
		try {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				int l = info.length;
				for (int i = 0; i < l; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						if (info[i].getTypeName().equalsIgnoreCase("MOBILE")) {
							if ((Proxy.getDefaultHost() != null)
									&& (!Proxy.getDefaultHost().equals(""))) {
								return 12;
							}

							return 11;
						}

						return 10;
					}
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}

		return 0;
	}
}
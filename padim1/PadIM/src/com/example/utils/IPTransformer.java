package com.example.utils;


/**   
*    
* 项目名称：PadIM   
* 类名称：IPTransformer   
* 类描述：工具类，实现ip点分十进制与整形的相互转换  
* 创建人：ystgx   
* 创建时间：2014年4月2日 下午5:03:32   
* @version        
*/
public class IPTransformer {
	
	/**
	 * 将string型点分十进制ip转换成长整型，如果转换失败，返回-1
	 * @param ip
	 * @return
	 */
	public static long ipToLong(String ip){
		long result = 0;
		
		if (isIp(ip)) {
			String[] ipInArray = ip.split("\\.");
			for (int i = 3; i >= 0; --i) {
				long temp = Long.parseLong(ipInArray[3 - i]);
				result |= temp << (i * 8);
			}
			return result;
		}
		else 
			return -1;
		
	}
	
	
	/**
	 * 将长整形表示的ip转换成string类型的点分十进制ip，失败返回null
	 * @param ip
	 * @return
	 */
	public static String longToIp(long ip ){
		String result = null;
		result = ((ip >> 24) & 0xff) + "."
				+ ((ip >> 16) & 0xff) + "."
				+ ((ip >> 8) & 0xff) + "."
				+ (ip & 0xff);
		
		return isIp(result) ? result:null;  
	}
	
	
	/**
	 * 去掉ip字符串前后的空格
	 * @param ip
	 * @return
	 */
	public static String trimSpaces(String ip){
		while(ip.startsWith(" ")){
			ip = ip.substring(1, ip.length()).trim();
		}
		while(ip.endsWith(" ")){
			ip = ip.substring(0, ip.length()-1).trim();
		}
		return ip;
	}
	
	
	/**
	 * 判断点分十进制ip是否合法
	 * @param ip
	 * @return
	 */
	public static boolean isIp(String ip){
		boolean result = false;
		ip = trimSpaces(ip);
		if(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){
			String s[] = ip.split("\\."); 
            if(Integer.parseInt(s[0])<255) 
                if(Integer.parseInt(s[1])<255) 
                    if(Integer.parseInt(s[2])<255) 
                        if(Integer.parseInt(s[3])<255) 
                            result = true; 
		}
		 return result;
	}
	
}

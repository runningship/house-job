package com.youwei.zjb.house.spider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.bc.sdak.SimpDaoTool;
import org.bc.sdak.utils.LogUtil;
import org.jsoup.nodes.Element;

public class PullDataHelper {

	public final static int errorReportUserId= 36;

	public static String getHttpData(String urlStr ,String site , String encode) throws IOException{
		URL url = new URL(urlStr);
		URLConnection conn = url.openConnection();
//		if(cookies.containsKey(site)){
//			conn.setRequestProperty("Cookie", cookies.get(site));
//		}
		conn.setDefaultUseCaches(false);
		conn.setUseCaches(false);
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);
		conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		String result = IOUtils.toString(conn.getInputStream(),encode);
		
		//获取cookie  
//        Map<String,List<String>> map=conn.getHeaderFields();
//        Set<String> set=map.keySet();  
//        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
//            String key = (String) iterator.next();
//            if(key==null){
//            	continue;
//            }
//            if (key.equals("Set-Cookie")) {  
//                List<String> list = map.get(key);  
//                StringBuilder builder = new StringBuilder();  
//                for (String str : list) {  
//                    builder.append(str).toString();  
//                }  
//                String firstCookie = builder.toString();
//                cookies.put(site, firstCookie);
//            }  
//        }
		
		return result;
	}
	
	public static String uncompress(String str) throws IOException {   
	    if (str == null || str.length() == 0) {   
	      return str;   
	  }   
	   ByteArrayOutputStream out = new ByteArrayOutputStream();   
	   ByteArrayInputStream in = new ByteArrayInputStream(str   
	        .getBytes("ISO-8859-1"));   
	    GZIPInputStream gunzip = new GZIPInputStream(in);   
	    byte[] buffer = new byte[256];   
	    int n;   
	   while ((n = gunzip.read(buffer))>= 0) {   
	    out.write(buffer, 0, n);   
	    }   
	    // toString()使用平台默认编码，也可以显式的指定如toString(&quot;GBK&quot;)   
	    return out.toString();   
	  }
	
	public static Date getPubTime(Element elem){
		try{
			String text = elem.getElementsByClass("qj-renaddr").first().ownText();
			if(StringUtils.isEmpty(text)){
				return null;
			}
			for(String str : text.split(" ")){
				if(str.contains("分钟") || str.contains("小时")){
					text = str;
					break;
				}
			}
			if(text.endsWith("分钟")){
				text = text.replace("分钟","");
				Calendar ca = Calendar.getInstance();
				ca.add(Calendar.MINUTE, 0-Integer.valueOf(text));
				return ca.getTime();
			}else if (text.endsWith("小时")){
				text = text.replace("小时","");
				Calendar ca = Calendar.getInstance();
				ca.add(Calendar.HOUR_OF_DAY, 0-Integer.valueOf(text));
				return ca.getTime();
			}
		}catch(Exception ex){
			LogUtil.warning("获取发布时间失败,"+ elem.outerHtml());
			return null;
		}
		return null;
	}
	
	public static String getHxingByText(String text){
		if(StringUtils.isEmpty(text)){
			return "";
		}
		String str = "";
		int si = text.indexOf("室");
		int ti = text.indexOf("厅");
		int wi = text.indexOf("卫");
		if(si>0){
			str+=text.charAt(si-1)+"室";
		}
		if(ti>0){
			str+=text.charAt(ti-1)+"厅";
		}else{
			str+="0厅";
		}
		if(wi>0){
			str+=text.charAt(wi-1)+"卫";
		}else{
			str+="1卫";
		}
		return str;
	}
}
